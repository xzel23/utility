package com.dua3.utility.rest;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("java:S5778")
class RestApiTest {

    @Test
    void requestAcceptsPositiveTimeoutAndRejectsNonPositiveTimeouts() {
        RestRequest request = api(RestEndpoint.builder("getItems").get("/items").build())
                .connect()
                .request("getItems");

        assertEquals(Duration.ofSeconds(1), request.timeout(Duration.ofSeconds(1)).prepare().timeout());
        assertThrows(IllegalArgumentException.class, () -> request.timeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> request.timeout(Duration.ofSeconds(-1)));
    }

    @Test
    void importedOperationWithoutRequestBodyIsSupported() throws IOException {
        String document = """
                {
                  "openapi": "3.1.0",
                  "servers": [{"url": "https://example.test/v1"}],
                  "paths": {
                    "/items": {
                      "get": {
                        "operationId": "listItems",
                        "responses": {"200": {"description": "OK"}}
                      }
                    }
                  }
                }
                """;

        RestApi api = RestApi.builder()
                .fromOpenApi(new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8)), OpenApiFormat.JSON)
                .build();

        RestEndpoint endpoint = api.endpoint("listItems").orElseThrow();
        assertFalse(endpoint.requestBodyRequired());
        assertEquals("/items", endpoint.pathTemplate());
    }

    @Test
    void builderRejectsBlankMethodPathAndHeaderNames() {
        assertThrows(IllegalArgumentException.class, () -> RestEndpoint.builder("invalid").method(" ", "/items"));
        assertThrows(IllegalArgumentException.class, () -> RestEndpoint.builder("invalid").method("GET", " "));

        RestRequest request = api(RestEndpoint.builder("getItems").get("/items").build())
                .connect()
                .request("getItems");
        assertThrows(IllegalArgumentException.class, () -> request.header(" ", "value"));
    }

    @Test
    void parametersRetainDeclarationOrder() {
        RestEndpoint endpoint = RestEndpoint.builder("ordered")
                .get("/items")
                .queryParameter("first")
                .headerParameter("second", false)
                .cookieParameter("third", false)
                .build();

        assertEquals(List.of("first", "second", "third"),
                endpoint.parameters().stream().map(RestParameter::name).toList());
    }

    private static RestApi api(RestEndpoint endpoint) {
        return RestApi.builder()
                .baseUri("https://example.test/api")
                .endpoint(endpoint)
                .build();
    }
}
