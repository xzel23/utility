package com.dua3.utility.rest;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An immutable description of one REST operation.
 */
public final class RestEndpoint {
    private static final Pattern TEMPLATE_PARAMETER = Pattern.compile("\\{([^{}]+)}");

    private final String name;
    private final String method;
    private final String pathTemplate;
    private final Map<ParameterKey, RestParameter> parameters;
    private final Set<String> requestContentTypes;
    private final Set<String> responseContentTypes;
    private final Set<Integer> expectedStatuses;
    private final Set<String> securityRequirements;
    private final boolean requestBodyRequired;
    private final boolean retryAfterAuthenticationRefresh;
    private final @Nullable String idempotencyKeyHeader;

    private RestEndpoint(Builder builder, boolean retryAfterAuthenticationRefresh) {
        this.name = builder.name;
        this.method = Objects.requireNonNull(builder.method, "method must not be null");
        this.pathTemplate = Objects.requireNonNull(builder.pathTemplate, "pathTemplate must not be null");
        this.parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
        this.requestContentTypes = Set.copyOf(builder.requestContentTypes);
        this.responseContentTypes = Set.copyOf(builder.responseContentTypes);
        this.expectedStatuses = Set.copyOf(builder.expectedStatuses);
        this.securityRequirements = Set.copyOf(builder.securityRequirements);
        this.requestBodyRequired = builder.requestBodyRequired;
        this.retryAfterAuthenticationRefresh = retryAfterAuthenticationRefresh;
        this.idempotencyKeyHeader = builder.idempotencyKeyHeader;
    }

    /**
     * Creates a builder for an endpoint.
     *
     * @param name the stable operation name
     * @return a new builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Gets the operation name.
     *
     * @return the endpoint name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the HTTP method.
     *
     * @return the upper-case HTTP method
     */
    public String method() {
        return method;
    }

    /**
     * Gets the path template, relative to the REST API base URI.
     *
     * @return the URI template
     */
    public String pathTemplate() {
        return pathTemplate;
    }

    /**
     * Gets the endpoint's parameter declarations.
     *
     * @return parameter declarations in declaration order
     */
    public Collection<RestParameter> parameters() {
        return List.copyOf(parameters.values());
    }

    /**
     * Looks up a parameter declaration.
     *
     * @param name the parameter name
     * @param location the parameter location
     * @return the declaration, when present
     */
    public Optional<RestParameter> parameter(String name, RestParameterLocation location) {
        return Optional.ofNullable(parameters.get(ParameterKey.of(name, location)));
    }

    /**
     * Gets media types accepted for request bodies.
     *
     * @return accepted request media types
     */
    public Set<String> requestContentTypes() {
        return requestContentTypes;
    }

    /**
     * Gets media types described for response bodies.
     *
     * @return response media types
     */
    public Set<String> responseContentTypes() {
        return responseContentTypes;
    }

    /**
     * Gets explicitly expected response status codes.
     *
     * @return expected status codes
     */
    public Set<Integer> expectedStatuses() {
        return expectedStatuses;
    }

    /**
     * Gets names of OpenAPI security schemes associated with this operation.
     *
     * @return security-scheme names
     */
    public Set<String> securityRequirements() {
        return securityRequirements;
    }

    /**
     * Indicates whether this operation requires a request body.
     *
     * @return {@code true} when a body is required
     */
    public boolean requestBodyRequired() {
        return requestBodyRequired;
    }

    /**
     * Indicates whether a repeatable request may be retried after a token refresh.
     *
     * @return {@code true} if a 401 may result in one retry
     */
    public boolean retryAfterAuthenticationRefresh() {
        return retryAfterAuthenticationRefresh;
    }

    /**
     * Gets the idempotency-key header configured for an unsafe retry.
     *
     * @return the header name, or {@code null}
     */
    public @Nullable String idempotencyKeyHeader() {
        return idempotencyKeyHeader;
    }

    boolean hasParameter(String name, RestParameterLocation location) {
        return parameters.containsKey(ParameterKey.of(name, location));
    }

    /**
     * Builder for {@link RestEndpoint} instances.
     */
    public static final class Builder {
        private final String name;
        private @Nullable String method;
        private @Nullable String pathTemplate;
        private final Map<ParameterKey, RestParameter> parameters = new LinkedHashMap<>();
        private final Set<String> requestContentTypes = new LinkedHashSet<>();
        private final Set<String> responseContentTypes = new LinkedHashSet<>();
        private final Set<Integer> expectedStatuses = new LinkedHashSet<>();
        private final Set<String> securityRequirements = new LinkedHashSet<>();
        private boolean requestBodyRequired;
        private @Nullable Boolean retryAfterAuthenticationRefresh;
        private @Nullable String idempotencyKeyHeader;

        private Builder(String name) {
            LangUtil.checkArg(!name.isBlank(), "name must not be blank");
            this.name = name;
        }

        /**
         * Sets the endpoint HTTP method and path template.
         *
         * @param method the HTTP method
         * @param pathTemplate the path template
         * @return this builder
         */
        public Builder method(String method, String pathTemplate) {
            LangUtil.checkArg(!method.isBlank(), "HTTP method must not be blank");
            LangUtil.checkArg(!pathTemplate.isBlank(), "path template must not be blank");
            this.method = method.toUpperCase(Locale.ROOT);
            this.pathTemplate = pathTemplate.startsWith("/") ? pathTemplate : '/' + pathTemplate;
            return this;
        }

        /**
         * Sets a GET endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder get(String pathTemplate) {
            return method("GET", pathTemplate);
        }

        /**
         * Sets a POST endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder post(String pathTemplate) {
            return method("POST", pathTemplate);
        }

        /**
         * Sets a PUT endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder put(String pathTemplate) {
            return method("PUT", pathTemplate);
        }

        /**
         * Sets a PATCH endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder patch(String pathTemplate) {
            return method("PATCH", pathTemplate);
        }

        /**
         * Sets a DELETE endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder delete(String pathTemplate) {
            return method("DELETE", pathTemplate);
        }

        /**
         * Sets a HEAD endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder head(String pathTemplate) {
            return method("HEAD", pathTemplate);
        }

        /**
         * Sets an OPTIONS endpoint.
         *
         * @param pathTemplate the endpoint path template
         * @return this builder
         */
        public Builder options(String pathTemplate) {
            return method("OPTIONS", pathTemplate);
        }

        /**
         * Declares an endpoint parameter.
         *
         * @param name the parameter name
         * @param location the request part containing it
         * @param required whether it is mandatory
         * @return this builder
         */
        public Builder parameter(String name, RestParameterLocation location, boolean required) {
            RestParameter parameter = new RestParameter(name, location, required);
            ParameterKey key = ParameterKey.of(name, location);
            if (parameters.putIfAbsent(key, parameter) != null) {
                throw new IllegalArgumentException("parameter already declared: " + name + " in " + location);
            }
            return this;
        }

        /**
         * Declares a required path parameter.
         *
         * @param name the parameter name
         * @return this builder
         */
        public Builder pathParameter(String name) {
            return parameter(name, RestParameterLocation.PATH, true);
        }

        /**
         * Declares a query parameter.
         *
         * @param name the parameter name
         * @param required whether the parameter is mandatory
         * @return this builder
         */
        public Builder queryParameter(String name, boolean required) {
            return parameter(name, RestParameterLocation.QUERY, required);
        }

        /**
         * Declares an optional query parameter.
         *
         * @param name the parameter name
         * @return this builder
         */
        public Builder queryParameter(String name) {
            return queryParameter(name, false);
        }

        /**
         * Declares a header parameter.
         *
         * @param name the parameter name
         * @param required whether the parameter is mandatory
         * @return this builder
         */
        public Builder headerParameter(String name, boolean required) {
            return parameter(name, RestParameterLocation.HEADER, required);
        }

        /**
         * Declares a cookie parameter.
         *
         * @param name the parameter name
         * @param required whether the parameter is mandatory
         * @return this builder
         */
        public Builder cookieParameter(String name, boolean required) {
            return parameter(name, RestParameterLocation.COOKIE, required);
        }

        /**
         * Adds a supported request media type.
         *
         * @param contentType the request media type
         * @return this builder
         */
        public Builder consumes(String contentType) {
            requestContentTypes.add(validContentType(contentType));
            return this;
        }

        /**
         * Adds a response media type and sends it in the default Accept header.
         *
         * @param contentType the response media type
         * @return this builder
         */
        public Builder produces(String contentType) {
            responseContentTypes.add(validContentType(contentType));
            return this;
        }

        /**
         * Marks a request body as mandatory.
         *
         * @param required whether a body is required
         * @return this builder
         */
        public Builder requestBodyRequired(boolean required) {
            this.requestBodyRequired = required;
            return this;
        }

        /**
         * Adds an expected response status code.
         *
         * @param status the HTTP status code
         * @return this builder
         */
        public Builder expectedStatus(int status) {
            LangUtil.checkArg(100 <= status && status <= 599, "invalid HTTP status: %d", status);
            expectedStatuses.add(status);
            return this;
        }

        /**
         * Adds an OpenAPI security-scheme name used by this operation.
         *
         * @param securityScheme the scheme name
         * @return this builder
         */
        public Builder securityRequirement(String securityScheme) {
            LangUtil.checkArg(!securityScheme.isBlank(), "security scheme must not be blank");
            securityRequirements.add(securityScheme);
            return this;
        }

        /**
         * Configures whether a 401 can trigger one token-refresh retry.
         *
         * <p>For POST and PATCH this requires {@link #idempotencyKeyHeader(String)}. GET, HEAD,
         * OPTIONS, PUT, and DELETE default to {@code true}; POST and PATCH default to {@code false}.</p>
         *
         * @param enabled whether retries are permitted
         * @return this builder
         */
        public Builder retryAfterAuthenticationRefresh(boolean enabled) {
            this.retryAfterAuthenticationRefresh = enabled;
            return this;
        }

        /**
         * Declares the header understood by the server as an idempotency key. A generated UUID is
         * supplied when an unsafe endpoint is retried after an authentication refresh.
         *
         * @param headerName the header name
         * @return this builder
         */
        public Builder idempotencyKeyHeader(String headerName) {
            LangUtil.checkArg(!headerName.isBlank(), "headerName scheme must not be blank");
            this.idempotencyKeyHeader = headerName;
            return this;
        }

        /**
         * Builds the immutable endpoint description.
         *
         * @return the endpoint
         */
        public RestEndpoint build() {
            String endpointMethod = Objects.requireNonNull(method, "HTTP method has not been set");
            Objects.requireNonNull(pathTemplate, "path template has not been set");
            verifyPathParameters();

            boolean defaultRetry = switch (endpointMethod) {
                case "GET", "HEAD", "OPTIONS", "TRACE", "PUT", "DELETE" -> true;
                default -> false;
            };
            boolean retry = retryAfterAuthenticationRefresh != null
                    ? retryAfterAuthenticationRefresh : defaultRetry;
            if (retry && !defaultRetry && idempotencyKeyHeader == null) {
                throw new IllegalStateException(
                        "authentication-refresh retries for " + endpointMethod + " require an idempotency-key header");
            }
            return new RestEndpoint(this, retry);
        }

        private void verifyPathParameters() {
            Matcher matcher = TEMPLATE_PARAMETER.matcher(Objects.requireNonNull(pathTemplate));
            while (matcher.find()) {
                String parameterName = matcher.group(1);
                if (!parameters.containsKey(ParameterKey.of(parameterName, RestParameterLocation.PATH))) {
                    throw new IllegalStateException("path template parameter is not declared: " + parameterName);
                }
            }
        }

        private static String validContentType(String contentType) {
            LangUtil.checkArg(!contentType.isBlank(), "content type must not be blank");
            return contentType;
        }
    }

    private record ParameterKey(String name, RestParameterLocation location) {
        static ParameterKey of(String name, RestParameterLocation location) {
            String normalizedName = location == RestParameterLocation.HEADER ? name.toLowerCase(Locale.ROOT) : name;
            return new ParameterKey(normalizedName, location);
        }
    }
}
