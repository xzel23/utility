package com.dua3.utility.rest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

/**
 * An immutable HTTP response returned by a REST request.
 *
 * @param <T> the response body type
 * @param statusCode the HTTP response status code
 * @param headers the HTTP response headers
 * @param body the response body
 * @param uri the URI of the response
 * @param version the HTTP protocol version
 */
public record RestResponse<T>(int statusCode, HttpHeaders headers, T body, URI uri,
                              HttpClient.Version version) {
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
