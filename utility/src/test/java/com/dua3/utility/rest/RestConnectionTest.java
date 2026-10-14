package com.dua3.utility.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 20, unit = TimeUnit.SECONDS)
class RestConnectionTest {

    @Test
    void sendsDeclaredParametersBodyAndMergedHeaders() throws Exception {
        try (LoopbackRestServer server = new LoopbackRestServer(request -> LoopbackRestServer.Response.text(201, "created"))) {
            RestEndpoint endpoint = RestEndpoint.builder("updateWidget")
                    .post("widgets/{id}")
                    .pathParameter("id")
                    .queryParameter("tag")
                    .headerParameter("X-Request-Id", true)
                    .cookieParameter("session", true)
                    .requestBodyRequired(true)
                    .consumes("text/plain")
                    .produces("application/json")
                    .build();
            RestApi api = RestApi.builder()
                    .baseUri(server.baseUri().resolve("v1/"))
                    .defaultHeader("X-Default", "default")
                    .endpoint(endpoint)
                    .build();

            RestResponse<String> response = api.connect(RestAuthentication.apiKey(
                            "api_key", "top secret", RestParameterLocation.QUERY))
                    .request("updateWidget")
                    .pathParameter("id", "blue/green")
                    .queryParameter("tag", "red blue")
                    .queryParameter("tag", "orange")
                    .headerParameter("x-request-id", "request-42")
                    .cookieParameter("session", "abc")
                    .header("X-Default", "request")
                    .body(RestBody.text("payload"))
                    .execute(HttpResponse.BodyHandlers.ofString());

            assertEquals(201, response.statusCode());
            assertEquals("created", response.body());
            assertTrue(response.isSuccessful());
            assertSame(response, response.requireSuccess());
            assertEquals(1, server.requests().size());

            LoopbackRestServer.ReceivedRequest request = server.requests().getFirst();
            assertEquals("POST", request.method());
            assertEquals("/v1/widgets/blue%2Fgreen", request.target().getRawPath());
            assertEquals("tag=red+blue&tag=orange&api_key=top+secret", request.target().getRawQuery());
            assertEquals("payload", request.bodyAsUtf8());
            assertEquals("request", request.header("X-Default"));
            assertEquals("request-42", request.header("X-Request-Id"));
            assertEquals("session=abc", request.header("Cookie"));
            assertEquals("text/plain; charset=utf-8", request.header("Content-Type"));
            assertEquals("application/json", request.header("Accept"));
        }
    }

    @Test
    void rejectsInvalidRequestContractsBeforeSending() {
        RestEndpoint endpoint = RestEndpoint.builder("create")
                .post("items/{id}")
                .pathParameter("id")
                .queryParameter("filter", true)
                .headerParameter("X-Trace", true)
                .cookieParameter("session", true)
                .requestBodyRequired(true)
                .consumes("application/json")
                .build();
        RestRequest request = api("https://example.test/api/", endpoint).connect().request("create");

        assertThrows(IllegalStateException.class, request::prepare);
        assertThrows(IllegalArgumentException.class, () -> request.queryParameter("undeclared", "value"));
        assertThrows(IllegalArgumentException.class, () -> request.pathParameter("undeclared", "one"));

        RestRequest unsupportedContentType = api("https://example.test/api/", endpoint).connect().request("create")
                .pathParameter("id", "one")
                .queryParameter("filter", "all")
                .headerParameter("X-Trace", "trace")
                .cookieParameter("session", "session")
                .body(RestBody.text("plain text"));
        CompletionException exception = assertThrows(CompletionException.class,
                () -> unsupportedContentType.executeAsync(HttpResponse.BodyHandlers.ofString()).join());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void refreshesBearerTokenAndReusesGeneratedIdempotencyKey() throws Exception {
        AtomicInteger providerCalls = new AtomicInteger();
        AtomicInteger bodyHandlerCalls = new AtomicInteger();
        try (LoopbackRestServer server = new LoopbackRestServer(request ->
                "Bearer initial".equals(request.header("Authorization"))
                        ? LoopbackRestServer.Response.text(401, "expired")
                        : LoopbackRestServer.Response.text(200, "accepted"))) {
            RestEndpoint endpoint = RestEndpoint.builder("createPayment")
                    .post("payments")
                    .requestBodyRequired(true)
                    .consumes("text/plain")
                    .retryAfterAuthenticationRefresh(true)
                    .idempotencyKeyHeader("Idempotency-Key")
                    .build();
            RestAuthentication authentication = RestAuthentication.bearer(() -> {
                int call = providerCalls.incrementAndGet();
                return java.util.concurrent.CompletableFuture.completedFuture(new AccessToken(
                        call == 1 ? "initial" : "refreshed", Instant.now().plusSeconds(60)));
            });
            RestApi api = api(server.baseUri(), endpoint);

            RestResponse<String> response = api.connect(authentication)
                    .request("createPayment")
                    .body(RestBody.inputStream(() -> new ByteArrayInputStream("payment".getBytes(StandardCharsets.UTF_8)),
                            "text/plain"))
                    .executeAsync(() -> {
                        bodyHandlerCalls.incrementAndGet();
                        return HttpResponse.BodyHandlers.ofString();
                    })
                    .join();

            assertEquals(200, response.statusCode());
            assertEquals("accepted", response.body());
            assertEquals(2, providerCalls.get());
            assertEquals(2, bodyHandlerCalls.get());
            assertEquals(2, server.requests().size());
            assertEquals("Bearer initial", server.requests().get(0).header("Authorization"));
            assertEquals("Bearer refreshed", server.requests().get(1).header("Authorization"));
            assertEquals("payment", server.requests().get(0).bodyAsUtf8());
            assertEquals("payment", server.requests().get(1).bodyAsUtf8());
            assertEquals(server.requests().get(0).header("Idempotency-Key"),
                    server.requests().get(1).header("Idempotency-Key"));
        }
    }

    @Test
    void copiesByteArrayBodiesBeforeSending() throws Exception {
        byte[] source = "original".getBytes(StandardCharsets.UTF_8);
        RestBody body = RestBody.bytes(source, "text/plain");
        source[0] = 'X';

        try (LoopbackRestServer server = new LoopbackRestServer(request -> LoopbackRestServer.Response.text(204, ""))) {
            RestEndpoint endpoint = RestEndpoint.builder("upload")
                    .post("upload")
                    .requestBodyRequired(true)
                    .consumes("text/plain")
                    .build();

            RestResponse<String> response = api(server.baseUri(), endpoint).connect()
                    .request("upload")
                    .body(body)
                    .execute(HttpResponse.BodyHandlers.ofString());

            assertEquals(204, response.statusCode());
            assertEquals("original", server.requests().getFirst().bodyAsUtf8());
        }
    }

    @Test
    void doesNotRetryUnsafeOrNonRepeatableRequestsAfterUnauthorizedResponse() throws Exception {
        AtomicInteger unsafeProviderCalls = new AtomicInteger();
        try (LoopbackRestServer server = new LoopbackRestServer(request -> LoopbackRestServer.Response.text(401, "no"))) {
            RestEndpoint endpoint = RestEndpoint.builder("create")
                    .post("items")
                    .requestBodyRequired(true)
                    .consumes("text/plain")
                    .build();
            RestAuthentication authentication = tokenAuthentication(unsafeProviderCalls);

            RestResponse<String> response = api(server.baseUri(), endpoint).connect(authentication)
                    .request("create")
                    .body(RestBody.text("body"))
                    .execute(HttpResponse.BodyHandlers.ofString());

            assertEquals(401, response.statusCode());
            assertEquals(1, unsafeProviderCalls.get());
            assertEquals(1, server.requests().size());
        }

        AtomicInteger nonRepeatableProviderCalls = new AtomicInteger();
        try (LoopbackRestServer server = new LoopbackRestServer(request -> LoopbackRestServer.Response.text(401, "no"))) {
            RestEndpoint endpoint = RestEndpoint.builder("create")
                    .post("items")
                    .requestBodyRequired(true)
                    .consumes("text/plain")
                    .retryAfterAuthenticationRefresh(true)
                    .idempotencyKeyHeader("Idempotency-Key")
                    .build();
            RestAuthentication authentication = tokenAuthentication(nonRepeatableProviderCalls);

            RestResponse<String> response = api(server.baseUri(), endpoint).connect(authentication)
                    .request("create")
                    .body(RestBody.ofPublisher(HttpRequest.BodyPublishers.ofString("body"), false, "text/plain"))
                    .execute(HttpResponse.BodyHandlers.ofString());

            assertEquals(401, response.statusCode());
            assertEquals(1, nonRepeatableProviderCalls.get());
            assertEquals(1, server.requests().size());
        }
    }

    @Test
    void mapsJsonRequestAndResponse() throws Exception {
        try (LoopbackRestServer server = new LoopbackRestServer(request -> {
            assertEquals("{\"name\":\"Ada\"}", request.bodyAsUtf8());
            return LoopbackRestServer.Response.json(200, "{\"answer\":42}");
        })) {
            RestEndpoint endpoint = RestEndpoint.builder("json")
                    .post("json")
                    .requestBodyRequired(true)
                    .consumes("application/json")
                    .produces("application/json")
                    .build();

            RestResponse<?> response = api(server.baseUri(), endpoint).connect()
                    .request("json")
                    .json(Map.of("name", "Ada"))
                    .executeJson(Map.class);

            assertEquals(200, response.statusCode());
            assertEquals(Map.of("answer", 42), response.body());
        }
    }

    @Test
    void exposesNonSuccessfulResponsesForExplicitHandling() throws Exception {
        try (LoopbackRestServer server = new LoopbackRestServer(request -> LoopbackRestServer.Response.text(418, "teapot"))) {
            RestEndpoint endpoint = RestEndpoint.builder("status").get("status").build();
            RestResponse<String> response = api(server.baseUri(), endpoint).connect()
                    .request("status")
                    .execute(HttpResponse.BodyHandlers.ofString());

            assertFalse(response.isSuccessful());
            RestStatusException exception = assertThrows(RestStatusException.class, response::requireSuccess);
            assertEquals(response, exception.response());
            assertTrue(exception.getMessage().contains("418"));
        }
    }

    private static RestAuthentication tokenAuthentication(AtomicInteger calls) {
        return RestAuthentication.bearer(() -> java.util.concurrent.CompletableFuture.completedFuture(new AccessToken(
                "token-" + calls.incrementAndGet(), Instant.now().plusSeconds(60))));
    }

    private static RestApi api(java.net.URI baseUri, RestEndpoint endpoint) {
        return RestApi.builder().baseUri(baseUri).endpoint(endpoint).build();
    }

    private static RestApi api(String baseUri, RestEndpoint endpoint) {
        return RestApi.builder().baseUri(baseUri).endpoint(endpoint).build();
    }
}
