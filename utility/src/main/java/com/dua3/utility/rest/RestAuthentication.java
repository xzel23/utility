package com.dua3.utility.rest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Supplies authentication data for a REST connection.
 */
public interface RestAuthentication {

    /**
     * Obtains credentials for a request.
     *
     * @param forceRefresh whether cached credentials must be refreshed
     * @return a stage supplying the credentials
     */
    CompletionStage<RestCredentials> credentials(boolean forceRefresh);

    /**
     * Indicates whether this authentication can obtain refreshed credentials.
     *
     * @return {@code true} for refreshable authentication
     */
    default boolean refreshable() {
        return false;
    }

    /**
     * Creates an authentication mechanism that adds no credentials.
     *
     * @return no authentication
     */
    static RestAuthentication none() {
        return fixed(RestCredentials.none());
    }

    /**
     * Creates HTTP Basic authentication.
     *
     * @param userName the user name
     * @param password the password
     * @return Basic authentication
     */
    static RestAuthentication basic(String userName, String password) {
        Objects.requireNonNull(userName, "userName");
        Objects.requireNonNull(password, "password");
        String value = userName + ':' + password;
        String encoded = Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        return fixed(RestCredentials.header("Authorization", "Basic " + encoded));
    }

    /**
     * Creates static bearer-token authentication.
     *
     * @param token the bearer token
     * @return bearer authentication
     */
    static RestAuthentication bearer(String token) {
        return fixed(RestCredentials.header("Authorization", "Bearer " + Objects.requireNonNull(token, "token")));
    }

    /**
     * Creates bearer-token authentication that refreshes tokens when needed.
     *
     * @param tokenProvider the provider used to obtain current tokens
     * @return refreshable bearer authentication
     */
    static RestAuthentication bearer(AccessTokenProvider tokenProvider) {
        return new BearerTokenAuthentication(tokenProvider);
    }

    /**
     * Creates API-key authentication.
     *
     * @param name the header or query parameter name
     * @param value the API key
     * @param location the header or query location
     * @return API-key authentication
     */
    static RestAuthentication apiKey(String name, String value, RestParameterLocation location) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(value, "value");
        return switch (Objects.requireNonNull(location, "location")) {
            case HEADER -> fixed(RestCredentials.header(name, value));
            case QUERY -> fixed(RestCredentials.queryParameter(name, value));
            default -> throw new IllegalArgumentException("API keys can only be placed in headers or query parameters");
        };
    }

    private static RestAuthentication fixed(RestCredentials credentials) {
        return forceRefresh -> CompletableFuture.completedFuture(credentials);
    }
}
