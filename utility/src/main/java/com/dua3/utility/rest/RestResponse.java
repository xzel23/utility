package com.dua3.utility.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * An immutable HTTP response returned by a REST request.
 *
 * @param <T> the response body type
 */
public record RestResponse<T>(int statusCode, HttpHeaders headers, T body, URI uri,
                              HttpClient.Version version) {

    /**
     * Creates a REST response.
     */
    public RestResponse {
        Objects.requireNonNull(headers, "headers");
        Objects.requireNonNull(uri, "uri");
        Objects.requireNonNull(version, "version");
    }

    /**
     * Indicates whether the response status is in the 2xx range.
     *
     * @return {@code true} for a successful HTTP response
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Returns this response when it is successful, otherwise throws a status exception.
     *
     * @return this response
     * @throws RestStatusException if the status is outside the 2xx range
     */
    public RestResponse<T> requireSuccess() {
        if (!isSuccessful()) {
            throw new RestStatusException(this);
        }
        return this;
    }

    static <T> RestResponse<T> from(HttpResponse<T> response) {
        return new RestResponse<>(response.statusCode(), response.headers(), response.body(), response.uri(), response.version());
    }
}
