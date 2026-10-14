package com.dua3.utility.rest;

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
 */
public final class RestApi {
    private final URI baseUri;
    private final HttpClient httpClient;
    private final Duration requestTimeout;
    private final Map<String, List<String>> defaultHeaders;
    private final Map<String, RestEndpoint> endpoints;

    RestApi(URI baseUri, HttpClient httpClient, Duration requestTimeout,
            Map<String, List<String>> defaultHeaders, Map<String, RestEndpoint> endpoints) {
        this.baseUri = normalizeBaseUri(baseUri);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
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

    Map<String, List<String>> defaultHeaders() {
        return defaultHeaders;
    }

    private static URI normalizeBaseUri(URI value) {
        Objects.requireNonNull(value, "baseUri");
        if (!value.isAbsolute() || value.getScheme() == null || value.getHost() == null) {
            throw new IllegalArgumentException("base URI must be an absolute HTTP URI: " + value);
        }
        if (value.getQuery() != null || value.getFragment() != null) {
            throw new IllegalArgumentException("base URI must not contain a query or fragment: " + value);
        }
        String normalized = value.toString();
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
