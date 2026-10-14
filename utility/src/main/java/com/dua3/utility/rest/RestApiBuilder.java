package com.dua3.utility.rest;

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builder for immutable {@link RestApi} definitions.
 */
public final class RestApiBuilder {
    private static final Logger LOG = LogManager.getLogger(RestApiBuilder.class);
    private @Nullable URI baseUri;
    private @Nullable HttpClient httpClient;
    private @Nullable Duration connectTimeout;
    private Duration requestTimeout = Duration.ofSeconds(30);
    private final Map<String, List<String>> defaultHeaders = new LinkedHashMap<>();
    private final Map<String, RestEndpoint> endpoints = new LinkedHashMap<>();

    RestApiBuilder() {
        // Use RestApi.builder().
    }

    /**
     * Sets the base URI for all endpoint paths.
     *
     * @param baseUri the absolute API base URI
     * @return this builder
     */
    public RestApiBuilder baseUri(URI baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    /**
     * Sets the base URI for all endpoint paths.
     *
     * @param baseUri the absolute API base URI
     * @return this builder
     */
    public RestApiBuilder baseUri(String baseUri) {
        return baseUri(URI.create(baseUri));
    }

    /**
     * Uses a caller-supplied HTTP client.
     *
     * @param httpClient the HTTP client
     * @return this builder
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    public RestApiBuilder httpClient(HttpClient httpClient) {
        if (this.httpClient != null) {
            throw new IllegalStateException("HTTP client already set");
        }
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Sets the connect timeout used when this builder creates the HTTP client.
     *
     * @param timeout the connect timeout
     * @return this builder
     */
    public RestApiBuilder connectTimeout(Duration timeout) {
        this.connectTimeout = positive(timeout, "connectTimeout");
        return this;
    }

    /**
     * Sets the default timeout for individual REST requests.
     *
     * @param timeout the request timeout
     * @return this builder
     */
    public RestApiBuilder requestTimeout(Duration timeout) {
        this.requestTimeout = positive(timeout, "requestTimeout");
        return this;
    }

    /**
     * Adds a default request header.
     *
     * @param name the header name
     * @param value the header value
     * @return this builder
     */
    public RestApiBuilder defaultHeader(String name, String value) {
        LangUtil.checkArg(!name.isBlank(), "name must not be blank");
        defaultHeaders.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
        return this;
    }

    /**
     * Adds an endpoint definition.
     *
     * @param endpoint the endpoint
     * @return this builder
     */
    public RestApiBuilder endpoint(RestEndpoint endpoint) {
        if (endpoints.putIfAbsent(endpoint.name(), endpoint) != null) {
            throw new IllegalArgumentException("endpoint already declared: " + endpoint.name());
        }
        return this;
    }

    /**
     * Creates, configures, and adds an endpoint.
     *
     * @param name the endpoint name
     * @param configurator the endpoint configurator
     * @return this builder
     */
    public RestApiBuilder endpoint(String name, Consumer<RestEndpoint.Builder> configurator) {
        RestEndpoint.Builder builder = RestEndpoint.builder(name);
        configurator.accept(builder);
        return endpoint(builder.build());
    }

    /**
     * Imports an OpenAPI 3 definition from a JSON or YAML document.
     *
     * @param document the OpenAPI document
     * @param format the document format, or {@link OpenApiFormat#AUTO}
     * @return this builder
     * @throws IOException if the document cannot be read or parsed
     * @throws UnsupportedOperationException if the required Jackson 3 component is unavailable
     */
    public RestApiBuilder fromOpenApi(InputStream document, OpenApiFormat format) throws IOException {
        LOG.debug("Loading OpenAPI definition with format {}", format);
        OpenApiImporter.importInto(this, document, format);
        LOG.debug("OpenAPI definition loaded; {} endpoint(s) configured", endpoints.size());
        return this;
    }

    /**
     * Imports an OpenAPI 3 definition, detecting JSON or YAML from the file content.
     *
     * @param document the OpenAPI document
     * @return this builder
     * @throws IOException if the document cannot be read or parsed
     */
    public RestApiBuilder fromOpenApi(InputStream document) throws IOException {
        return fromOpenApi(document, OpenApiFormat.AUTO);
    }

    /**
     * Imports an OpenAPI 3 definition from a file. The extension selects JSON or YAML when known.
     *
     * @param document the OpenAPI document file
     * @return this builder
     * @throws IOException if the document cannot be read or parsed
     */
    public RestApiBuilder fromOpenApi(Path document) throws IOException {
        OpenApiFormat format = formatForPath(document);
        try (InputStream input = Files.newInputStream(document)) {
            return fromOpenApi(input, format);
        }
    }

    /**
     * Builds a REST API definition.
     *
     * @return the REST API
     */
    public RestApi build() {
        URI apiBaseUri = Objects.requireNonNull(baseUri,
                "base URI has not been set and was not supplied by the OpenAPI document");
        HttpClient client = httpClient;
        if (client == null) {
            HttpClient.Builder clientBuilder = HttpClient.newBuilder();
            if (connectTimeout != null) {
                clientBuilder.connectTimeout(connectTimeout);
            }
            client = clientBuilder.build();
        }
        LOG.debug("Building REST API with base URI {} and {} endpoint(s)", apiBaseUri, endpoints.size());
        return new RestApi(apiBaseUri, client, requestTimeout, defaultHeaders, endpoints);
    }

    boolean hasBaseUri() {
        return baseUri != null;
    }

    void baseUriIfUnset(URI value) {
        if (baseUri == null && value.isAbsolute()) {
            baseUri(value);
        }
    }

    private static Duration positive(Duration value, String name) {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static OpenApiFormat formatForPath(Path path) {
        return switch (IoUtil.getExtension(path).toLowerCase(Locale.ROOT)) {
            case "yaml", "yml" -> OpenApiFormat.YAML;
            case "json" -> OpenApiFormat.JSON;
            default -> OpenApiFormat.AUTO;
        };
    }
}
