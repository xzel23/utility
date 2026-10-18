package com.dua3.utility.rest;

import com.dua3.utility.lang.LangUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable REST API definition backed by Java's {@link HttpClient}.
 *
 * <p>A client created by this API is closed when the API is closed. A client
 * supplied to {@link RestApiBuilder#httpClient(HttpClient)} remains owned by
 * the caller and is not closed by this API.</p>
 */
public final class RestApi implements AutoCloseable {
    private final URI baseUri;
    private final HttpClient httpClient;
    private final boolean ownsHttpClient;
    private final Duration requestTimeout;
    private final Map<String, List<String>> defaultHeaders;
    private final Map<String, RestEndpoint> endpoints;

    RestApi(URI baseUri, HttpClient httpClient, boolean ownsHttpClient, Duration requestTimeout,
            Map<String, List<String>> defaultHeaders, Map<String, RestEndpoint> endpoints) {
        this.baseUri = normalizeBaseUri(baseUri);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.ownsHttpClient = ownsHttpClient;
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        this.defaultHeaders = immutableHeaders(defaultHeaders);
        this.endpoints = Map.copyOf(endpoints);
    }

    /**
     * Creates a REST API builder.
     *
     * @return a new builder
     */
    public static RestApiBuilder builder() {
        return new RestApiBuilder();
    }

    /**
     * Gets the base URI used for endpoint paths.
     *
     * @return the normalized base URI
     */
    public URI baseUri() {
        return baseUri;
    }

    /**
     * Gets the HTTP client used by new connections.
     *
     * <p>The client is owned by this API when it was created by the builder.
     * When supplied with {@link RestApiBuilder#httpClient(HttpClient)}, it is
     * owned by the caller.</p>
     *
     * @return the HTTP client
     */
    public HttpClient httpClient() {
        return httpClient;
    }

    /**
     * Gets the default per-request timeout.
     *
     * @return the request timeout
     */
    public Duration requestTimeout() {
        return requestTimeout;
    }

    /**
     * Looks up an endpoint by name.
     *
     * @param name the endpoint name
     * @return the endpoint, when present
     */
    public Optional<RestEndpoint> endpoint(String name) {
        return Optional.ofNullable(endpoints.get(name));
    }

    /**
     * Gets all endpoint definitions keyed by operation name.
     *
     * @return immutable endpoint definitions
     */
    public Map<String, RestEndpoint> endpoints() {
        return endpoints;
    }

    /**
     * Opens a connection without authentication.
     *
     * @return a REST connection
     */
    public RestConnection connect() {
        return connect(RestAuthentication.none());
    }

    /**
     * Opens a connection using supplied authentication.
     *
     * @param authentication request authentication
     * @return a REST connection
     */
    public RestConnection connect(RestAuthentication authentication) {
        return new RestConnection(this, Objects.requireNonNull(authentication, "authentication"));
    }

    /**
     * Closes the HTTP client when this API created it.
     *
     * <p>A caller supplied through {@link RestApiBuilder#httpClient(HttpClient)}
     * is not closed.</p>
     */
    @Override
    public void close() {
        if (ownsHttpClient) {
            httpClient.close();
        }
    }

    Map<String, List<String>> defaultHeaders() {
        return defaultHeaders;
    }

    private static URI normalizeBaseUri(URI baseUri) {
        String scheme = baseUri.getScheme();
        LangUtil.checkArg(
                baseUri.isAbsolute() && scheme != null
                        && baseUri.getHost() != null
                        && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")),
                "baseUri must be an absolute HTTP URI: %s", baseUri
        );
        LangUtil.checkArg(
                baseUri.getQuery() == null && baseUri.getFragment() == null,
                "baseUri must not contain a query or fragment"
        );

        String normalized = baseUri.toString();
        return URI.create(normalized.endsWith("/") ? normalized : normalized + '/');
    }

    private static Map<String, List<String>> immutableHeaders(Map<String, List<String>> values) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        values.forEach((name, headers) -> {
            Objects.requireNonNull(name, "header name");
            Objects.requireNonNull(headers, "header values");
            copy.put(name, List.copyOf(new ArrayList<>(headers)));
        });
        return Map.copyOf(copy);
    }
}
