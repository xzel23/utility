package com.dua3.utility.rest;

import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5778")
class RestEndpointTest {

    @Test
    void validatesTemplatesParametersAndResponseMetadata() {
        assertThrows(IllegalStateException.class, () -> RestEndpoint.builder("missingPathParameter")
                .get("items/{id}")
                .build());
        //noinspection ResultOfObjectAllocationIgnored
        assertThrows(IllegalArgumentException.class,
                () -> new RestParameter("id", RestParameterLocation.PATH, false));
        assertThrows(IllegalArgumentException.class, () -> RestEndpoint.builder("invalidStatus")
                .get("items")
                .expectedStatus(99));
        assertThrows(IllegalArgumentException.class, () -> RestEndpoint.builder("invalidContentType")
                .get("items")
                .consumes(" "));

        RestEndpoint endpoint = RestEndpoint.builder("getItem")
                .get("items/{id}")
                .pathParameter("id")
                .headerParameter("X-Trace", true)
                .expectedStatus(200)
                .expectedStatus(204)
                .produces("application/json")
                .build();

        assertEquals("GET", endpoint.method());
        assertEquals("/items/{id}", endpoint.pathTemplate());
        assertEquals(List.of("id", "X-Trace"), endpoint.parameters().stream().map(RestParameter::name).toList());
        assertTrue(endpoint.parameter("x-trace", RestParameterLocation.HEADER).isPresent());
        assertEquals(List.of(200, 204), endpoint.expectedStatuses().stream().sorted().toList());
    }

    @Test
    void onlyEnablesUnsafeRefreshRetryWithIdempotencySupport() {
        assertTrue(RestEndpoint.builder("read").get("items").build().retryAfterAuthenticationRefresh());
        assertFalse(RestEndpoint.builder("create").post("items").build().retryAfterAuthenticationRefresh());
        assertThrows(IllegalStateException.class, () -> RestEndpoint.builder("create")
                .post("items")
                .retryAfterAuthenticationRefresh(true)
                .build());

        RestEndpoint endpoint = RestEndpoint.builder("create")
                .post("items")
                .retryAfterAuthenticationRefresh(true)
                .idempotencyKeyHeader("Idempotency-Key")
                .build();
        assertTrue(endpoint.retryAfterAuthenticationRefresh());
        assertEquals("Idempotency-Key", endpoint.idempotencyKeyHeader());
    }

    @Test
    void validatesHttpBaseUrisAndNormalizesTheBasePath() {
        RestApi api = RestApi.builder().baseUri("https://example.test/api").build();
        assertEquals("https://example.test/api/", api.baseUri().toString());

        assertThrows(IllegalArgumentException.class,
                () -> RestApi.builder().baseUri("ftp://example.test/api").build());
        assertThrows(IllegalArgumentException.class,
                () -> RestApi.builder().baseUri("https://example.test/api?query=value").build());
        assertThrows(IllegalArgumentException.class,
                () -> RestApi.builder().baseUri("/relative").build());
    }

    @Test
    void builderRetainsConfiguredValuesAndRejectsDuplicateDefinitions() {
        RestEndpoint endpoint = RestEndpoint.builder("read").get("items").build();
        HttpClient client = HttpClient.newHttpClient();
        RestApiBuilder builder = RestApi.builder()
                .baseUri("https://example.test/api")
                .connectTimeout(Duration.ofSeconds(2))
                .requestTimeout(Duration.ofSeconds(5))
                .defaultHeader("X-Default", "value")
                .httpClient(client)
                .endpoint(endpoint)
                .endpoint("delete", definition -> definition.delete("items/{id}").pathParameter("id"));

        assertThrows(IllegalStateException.class, () -> builder.httpClient(client));
        assertThrows(IllegalArgumentException.class, () -> builder.endpoint(endpoint));
        assertThrows(IllegalArgumentException.class, () -> RestApi.builder().connectTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> RestApi.builder().requestTimeout(Duration.ofSeconds(-1)));
        assertThrows(IllegalArgumentException.class, () -> RestApi.builder().defaultHeader(" ", "value"));

        RestApi api = builder.build();
        assertSame(client, api.httpClient());
        assertEquals(Duration.ofSeconds(5), api.requestTimeout());
        assertEquals(Set.of("read", "delete"), api.endpoints().keySet());
        assertThrows(UnsupportedOperationException.class, () -> api.endpoints().clear());
        assertThrows(IllegalArgumentException.class, () -> api.connect().request("unknown"));
        assertThrows(IllegalArgumentException.class,
                () -> api.connect().request(RestEndpoint.builder("read").get("other").build()));
    }
}
