package com.dua3.utility.rest;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * A reusable authenticated connection to a {@link RestApi}.
 */
public final class RestConnection {
    /**
     * Constant representing the key for specifying the content type in HTTP headers.
     */
    public static final String CONTENT_TYPE = "Content-Type";
    /**
     * Constant representing the "Accept" header for HTTP requests.
     */
    public static final String ACCEPT = "Accept";
    /**
     * Represents the HTTP method "GET".
     */
    public static final String GET = "GET";
    /**
     * Represents the constant value "HEAD".
     */
    public static final String HEAD = "HEAD";
    /**
     * Represents the HTTP method "OPTIONS".
     */
    public static final String OPTIONS = "OPTIONS";
    /**
     * Constant representing the TRACE HTTP method.
     */
    public static final String TRACE = "TRACE";
    /**
     * Represents the HTTP method PUT.
     */
    public static final String PUT = "PUT";
    /**
     * Represents the HTTP method DELETE.
     */
    public static final String DELETE = "DELETE";

    private final RestApi api;
    private final RestAuthentication authentication;

    RestConnection(RestApi api, RestAuthentication authentication) {
        this.api = Objects.requireNonNull(api, "api");
        this.authentication = Objects.requireNonNull(authentication, "authentication");
    }

    /**
     * Starts a request for a named endpoint.
     *
     * @param endpointName the operation name
     * @return a request builder
     * @throws IllegalArgumentException if the endpoint is unknown
     */
    public RestRequest request(String endpointName) {
        RestEndpoint endpoint = api.endpoint(endpointName)
                .orElseThrow(() -> new IllegalArgumentException("unknown REST endpoint: " + endpointName));
        return new RestRequest(this, endpoint);
    }

    /**
     * Starts a request for an endpoint definition belonging to this API.
     *
     * @param endpoint the endpoint definition
     * @return a request builder
     * @throws IllegalArgumentException if the endpoint name is not part of this API
     */
    public RestRequest request(RestEndpoint endpoint) {
        if (api.endpoint(endpoint.name()).orElse(null) != endpoint) {
            throw new IllegalArgumentException("endpoint does not belong to this REST API: " + endpoint.name());
        }
        return new RestRequest(this, endpoint);
    }

    Duration defaultRequestTimeout() {
        return api.requestTimeout();
    }

    <T> CompletableFuture<RestResponse<T>> executeAsync(RestRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        return executeAsync(request, () -> bodyHandler);
    }

    <T> CompletableFuture<RestResponse<T>> executeAsync(RestRequest request,
                                                        Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier) {
        RestRequest.Prepared prepared = request.prepare();
        // A retry must carry the same key as the original request.
        ensureIdempotencyKey(prepared);
        return authentication.credentials(false)
                .thenCompose(credentials -> send(prepared, credentials, bodyHandlerSupplier))
                .thenCompose(response -> retryAfterUnauthorized(prepared, response, bodyHandlerSupplier))
                .toCompletableFuture();
    }

    <T> RestResponse<T> execute(RestRequest request, HttpResponse.BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException {
        return execute(request, () -> bodyHandler);
    }

    <T> RestResponse<T> execute(RestRequest request, Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier)
            throws IOException, InterruptedException {
        try {
            return executeAsync(request, bodyHandlerSupplier).get();
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IOException("REST request failed", cause);
        }
    }

    private <T> CompletionStage<RestResponse<T>> retryAfterUnauthorized(RestRequest.Prepared prepared,
                                                                        RestResponse<T> response,
                                                                        Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier) {
        if (response.statusCode() != 401 || !authentication.refreshable()
                || !prepared.endpoint().retryAfterAuthenticationRefresh() || !prepared.body().repeatable()) {
            return CompletableFuture.completedFuture(response);
        }
        return authentication.credentials(true).thenCompose(credentials -> send(prepared, credentials, bodyHandlerSupplier));
    }

    private <T> CompletableFuture<RestResponse<T>> send(RestRequest.Prepared prepared, RestCredentials credentials,
                                                        Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier) {
        HttpRequest request;
        HttpResponse.BodyHandler<T> bodyHandler;
        try {
            request = buildRequest(prepared, credentials);
            bodyHandler = Objects.requireNonNull(bodyHandlerSupplier.get(), "bodyHandlerSupplier returned null");
        } catch (RuntimeException exception) {
            return CompletableFuture.failedFuture(exception);
        }
        return api.httpClient().sendAsync(request, bodyHandler).thenApply(RestResponse::from);
    }

    private HttpRequest buildRequest(RestRequest.Prepared prepared, RestCredentials credentials) {
        URI uri = requestUri(prepared.path(), mergeParameters(prepared.queryParameters(), credentials.queryParameters()));
        Map<String, List<String>> headers = new LinkedHashMap<>();
        mergeHeaders(headers, api.defaultHeaders(), false);
        mergeHeaders(headers, prepared.headers(), true);
        mergeHeaders(headers, credentials.headers(), true);

        if (!prepared.endpoint().responseContentTypes().isEmpty() && !containsHeader(headers, ACCEPT)) {
            putHeader(headers, ACCEPT, List.of(String.join(", ", prepared.endpoint().responseContentTypes())));
        }
        if (prepared.body().contentType() != null && !containsHeader(headers, CONTENT_TYPE)) {
            putHeader(headers, CONTENT_TYPE, List.of(prepared.body().contentType()));
        }
        validateContentType(prepared.endpoint(), headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(prepared.timeout());
        headers.forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
        return builder.method(prepared.endpoint().method(), prepared.body().newPublisher()).build();
    }

    private URI requestUri(String path, Map<String, List<String>> queryParameters) {
        URI uri = api.baseUri().resolve(path.startsWith("/") ? path.substring(1) : path);
        if (queryParameters.isEmpty()) {
            return uri;
        }
        StringBuilder query = new StringBuilder(uri.getRawQuery() == null ? "" : uri.getRawQuery());
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            for (String value : entry.getValue()) {
                if (!query.isEmpty()) {
                    query.append('&');
                }
                query.append(urlEncode(entry.getKey())).append('=').append(urlEncode(value));
            }
        }
        return URI.create(uri.toString().substring(0, uri.toString().indexOf('?') >= 0
                ? uri.toString().indexOf('?') : uri.toString().length()) + '?' + query);
    }

    private static Map<String, List<String>> mergeParameters(Map<String, List<String>> first,
                                                             Map<String, List<String>> second) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        appendParameters(result, first);
        appendParameters(result, second);
        return result;
    }

    private static void appendParameters(Map<String, List<String>> target, Map<String, List<String>> source) {
        source.forEach((name, values) -> target.computeIfAbsent(name, ignored -> new ArrayList<>()).addAll(values));
    }

    private static void mergeHeaders(Map<String, List<String>> target, Map<String, List<String>> source,
                                     boolean replace) {
        source.forEach((name, values) -> {
            if (replace) {
                putHeader(target, name, values);
            } else if (!containsHeader(target, name)) {
                putHeader(target, name, values);
            }
        });
    }

    private static void putHeader(Map<String, List<String>> headers, String name, List<String> values) {
        String oldName = headerName(headers, name);
        if (oldName != null) {
            headers.remove(oldName);
        }
        headers.put(name, List.copyOf(values));
    }

    private static boolean containsHeader(Map<String, List<String>> headers, String name) {
        return headerName(headers, name) != null;
    }

    private static @Nullable String headerName(Map<String, List<String>> headers, String soughtName) {
        for (String name : headers.keySet()) {
            if (name.equalsIgnoreCase(soughtName)) {
                return name;
            }
        }
        return null;
    }

    private static void validateContentType(RestEndpoint endpoint, Map<String, List<String>> headers) {
        if (endpoint.requestContentTypes().isEmpty()) {
            return;
        }
        String headerName = headerName(headers, CONTENT_TYPE);
        if (headerName == null) {
            if (endpoint.requestBodyRequired()) {
                throw new IllegalStateException("endpoint requires a Content-Type from " + endpoint.requestContentTypes());
            }
            return;
        }
        String contentType = headers.get(headerName).getFirst().split(";", 2)[0].trim();
        boolean accepted = endpoint.requestContentTypes().stream()
                .map(type -> type.split(";", 2)[0].trim())
                .anyMatch(type -> type.equalsIgnoreCase(contentType));
        if (!accepted) {
            throw new IllegalArgumentException("endpoint does not accept Content-Type " + contentType);
        }
    }

    private static boolean isUnsafe(String method) {
        return switch (method) {
            case GET, HEAD, OPTIONS, TRACE, PUT, DELETE -> false;
            default -> true;
        };
    }

    private void ensureIdempotencyKey(RestRequest.Prepared prepared) {
        String header = prepared.endpoint().idempotencyKeyHeader();
        if (header != null
                && prepared.endpoint().retryAfterAuthenticationRefresh()
                && isUnsafe(prepared.endpoint().method())
                && !containsHeader(prepared.headers(), header)
                && !containsHeader(api.defaultHeaders(), header)) {
            putHeader(prepared.headers(), header, List.of(UUID.randomUUID().toString()));
        }
    }

    static String encodePathSegment(String value) {
        return urlEncode(value).replace("+", "%20");
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
