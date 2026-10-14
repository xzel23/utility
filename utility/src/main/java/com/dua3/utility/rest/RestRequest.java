package com.dua3.utility.rest;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mutable builder for one invocation of a {@link RestEndpoint}.
 *
 * <p>A request may be reused after execution when its body is repeatable. It is not thread-safe.</p>
 */
public final class RestRequest {
    private static final Pattern TEMPLATE_PARAMETER = Pattern.compile("\\{([^{}]+)}");

    private final RestConnection connection;
    private final RestEndpoint endpoint;
    private final Map<String, List<String>> pathParameters = new LinkedHashMap<>();
    private final Map<String, List<String>> queryParameters = new LinkedHashMap<>();
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private final Map<String, List<String>> cookies = new LinkedHashMap<>();

    private @Nullable RestBody body;
    private @Nullable Duration timeout;

    RestRequest(RestConnection connection, RestEndpoint endpoint) {
        this.connection = connection;
        this.endpoint = endpoint;
    }

    /**
     * Sets a declared path parameter.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return this request
     */
    public RestRequest pathParameter(String name, Object value) {
        return setDeclaredParameter(pathParameters, name, value, RestParameterLocation.PATH, false);
    }

    /**
     * Adds a value for a declared query parameter. Repeated calls produce repeated query parameters.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return this request
     */
    public RestRequest queryParameter(String name, Object value) {
        return setDeclaredParameter(queryParameters, name, value, RestParameterLocation.QUERY, true);
    }

    /**
     * Sets a declared header parameter.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return this request
     */
    public RestRequest headerParameter(String name, Object value) {
        return setDeclaredParameter(headers, name, value, RestParameterLocation.HEADER, false);
    }

    /**
     * Sets a declared cookie parameter.
     *
     * @param name the parameter name
     * @param value the parameter value
     * @return this request
     */
    public RestRequest cookieParameter(String name, Object value) {
        return setDeclaredParameter(cookies, name, value, RestParameterLocation.COOKIE, false);
    }

    /**
     * Sets an arbitrary HTTP request header. This is useful for conditional requests and tracing.
     *
     * @param name the header name
     * @param value the header value
     * @return this request
     */
    public RestRequest header(String name, String value) {
        LangUtil.checkArg(!name.isBlank(), "name must not be blank");
        put(headers, name, value, false);
        return this;
    }

    /**
     * Sets the request body.
     *
     * @param body the request body
     * @return this request
     */
    public RestRequest body(RestBody body) {
        this.body = body;
        return this;
    }

    /**
     * Serializes a request value with the supplied codec.
     *
     * @param codec the codec
     * @param value the value to serialize
     * @return this request
     */
    public RestRequest body(RestCodec codec, Object value) {
        return body(codec.write(value));
    }

    /**
     * Serializes a request value as JSON using optional Jackson 3 support.
     *
     * @param value the value to serialize
     * @return this request
     * @throws UnsupportedOperationException if Jackson 3 databind is unavailable
     */
    public RestRequest json(Object value) {
        return body(RestCodecs.jacksonJson(), value);
    }

    /**
     * Overrides the API's default request timeout.
     *
     * @param timeout the positive timeout
     * @return this request
     */
    public RestRequest timeout(Duration timeout) {
        LangUtil.checkArg(timeout.isPositive(), "timeout must be positive");
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the configured idempotency key explicitly.
     *
     * @param key the idempotency key
     * @return this request
     * @throws IllegalStateException if the endpoint has no idempotency-key header
     */
    public RestRequest idempotencyKey(String key) {
        String header = endpoint.idempotencyKeyHeader();
        if (header == null) {
            throw new IllegalStateException("endpoint has no idempotency-key header");
        }
        return header(header, key);
    }

    /**
     * Executes the request synchronously.
     *
     * @param bodyHandler the response body handler
     * @param <T> the response body type
     * @return the response
     * @throws IOException if the request fails
     * @throws InterruptedException if interrupted while waiting
     */
    public <T> RestResponse<T> execute(HttpResponse.BodyHandler<T> bodyHandler)
            throws IOException, InterruptedException {
        return connection.execute(this, bodyHandler);
    }

    /**
     * Executes the request synchronously with a fresh body handler per attempt.
     *
     * @param bodyHandlerSupplier supplies response body handlers
     * @param <T> the response body type
     * @return the response
     * @throws IOException if the request fails
     * @throws InterruptedException if interrupted while waiting
     */
    public <T> RestResponse<T> execute(Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier)
            throws IOException, InterruptedException {
        return connection.execute(this, bodyHandlerSupplier);
    }

    /**
     * Executes the request asynchronously.
     *
     * @param bodyHandler the response body handler
     * @param <T> the response body type
     * @return a future for the response
     */
    public <T> CompletableFuture<RestResponse<T>> executeAsync(HttpResponse.BodyHandler<T> bodyHandler) {
        return connection.executeAsync(this, bodyHandler);
    }

    /**
     * Executes the request asynchronously with a fresh body handler per attempt.
     *
     * @param bodyHandlerSupplier supplies response body handlers
     * @param <T> the response body type
     * @return a future for the response
     */
    public <T> CompletableFuture<RestResponse<T>> executeAsync(
            Supplier<? extends HttpResponse.BodyHandler<T>> bodyHandlerSupplier) {
        return connection.executeAsync(this, bodyHandlerSupplier);
    }

    /**
     * Executes the request and deserializes its response as JSON using optional Jackson 3 support.
     *
     * @param type the response type
     * @param <T> the response type
     * @return the response
     * @throws IOException if the request fails
     * @throws InterruptedException if interrupted while waiting
     * @throws UnsupportedOperationException if Jackson 3 databind is unavailable
     */
    public <T> RestResponse<T> executeJson(Class<T> type) throws IOException, InterruptedException {
        RestCodec codec = RestCodecs.jacksonJson();
        return execute(() -> codec.bodyHandler(type));
    }

    /**
     * Executes the request asynchronously and deserializes its response as JSON.
     *
     * @param type the response type
     * @param <T> the response type
     * @return a future for the response
     * @throws UnsupportedOperationException if Jackson 3 databind is unavailable
     */
    public <T> CompletableFuture<RestResponse<T>> executeJsonAsync(Class<T> type) {
        RestCodec codec = RestCodecs.jacksonJson();
        return executeAsync(() -> codec.bodyHandler(type));
    }

    Prepared prepare() {
        validateRequiredParameters();
        if (endpoint.requestBodyRequired() && body == null) {
            throw new IllegalStateException("endpoint requires a request body: " + endpoint.name());
        }

        String path = endpoint.pathTemplate();
        for (Map.Entry<String, List<String>> entry : pathParameters.entrySet()) {
            path = path.replace("{" + entry.getKey() + '}', RestConnection.encodePathSegment(entry.getValue().getFirst()));
        }
        Matcher matcher = TEMPLATE_PARAMETER.matcher(path);
        if (matcher.find()) {
            throw new IllegalStateException("no value provided for path parameter: " + matcher.group(1));
        }

        Map<String, List<String>> preparedHeaders = copy(headers);
        if (!cookies.isEmpty()) {
            String cookieValue = cookies.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + '=' + value))
                    .reduce((first, second) -> first + "; " + second)
                    .orElseThrow();
            appendHeader(preparedHeaders, "Cookie", cookieValue);
        }
        return new Prepared(endpoint, path, copy(queryParameters), preparedHeaders,
                body != null ? body : RestBody.empty(), timeout != null ? timeout : connection.defaultRequestTimeout());
    }

    private RestRequest setDeclaredParameter(Map<String, List<String>> target, String name, Object value,
                                             RestParameterLocation location, boolean append) {
        if (!endpoint.hasParameter(name, location)) {
            throw new IllegalArgumentException("endpoint " + endpoint.name() + " has no " + location
                    + " parameter named " + name);
        }
        put(target, name, String.valueOf(value), append, location == RestParameterLocation.HEADER);
        return this;
    }

    private void validateRequiredParameters() {
        for (RestParameter parameter : endpoint.parameters()) {
            if (parameter.required()) {
                Map<String, List<String>> values = switch (parameter.location()) {
                    case PATH -> pathParameters;
                    case QUERY -> queryParameters;
                    case HEADER -> headers;
                    case COOKIE -> cookies;
                };
                if (!contains(values, parameter.name(), parameter.location() == RestParameterLocation.HEADER)) {
                    throw new IllegalStateException("required " + parameter.location() + " parameter is missing: " + parameter.name());
                }
            }
        }
    }

    private static void put(Map<String, List<String>> values, String name, String value, boolean append) {
        put(values, name, value, append, false);
    }

    private static void put(Map<String, List<String>> values, String name, String value, boolean append,
                            boolean caseInsensitive) {
        String currentName = findName(values, name, caseInsensitive);
        if (currentName != null && !append) {
            values.remove(currentName);
        }
        values.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
    }

    private static void appendHeader(Map<String, List<String>> values, String name, String value) {
        String currentName = findName(values, name, true);
        if (currentName == null) {
            values.put(name, new ArrayList<>(List.of(value)));
        } else {
            values.get(currentName).add(value);
        }
    }

    private static boolean contains(Map<String, List<String>> values, String name, boolean caseInsensitive) {
        return findName(values, name, caseInsensitive) != null;
    }

    private static @Nullable String findName(Map<String, List<String>> values, String name, boolean caseInsensitive) {
        for (String candidate : values.keySet()) {
            if (caseInsensitive ? candidate.equalsIgnoreCase(name) : candidate.equals(name)) {
                return candidate;
            }
        }
        return null;
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> source) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        source.forEach((name, values) -> result.put(name, new ArrayList<>(values)));
        return result;
    }

    record Prepared(RestEndpoint endpoint, String path, Map<String, List<String>> queryParameters,
                    Map<String, List<String>> headers, RestBody body, Duration timeout) {
    }
}
