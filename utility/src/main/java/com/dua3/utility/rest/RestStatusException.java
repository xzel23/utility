package com.dua3.utility.rest;

import java.util.Objects;

/**
 * Thrown by {@link RestResponse#requireSuccess()} for a non-success HTTP status.
 */
public final class RestStatusException extends RuntimeException {
    /**
     * The REST response associated with this exception.
     */
    private final transient RestResponse<?> response;

    /**
     * Creates an exception for a response.
     *
     * @param response the non-success response
     */
    public RestStatusException(RestResponse<?> response) {
        super("REST request to " + Objects.requireNonNull(response, "response").uri()
                + " failed with HTTP status " + response.statusCode());
        this.response = response;
    }

    /**
     * Gets the response that caused the exception.
     *
     * @return the failed response
     */
    public RestResponse<?> response() {
        return response;
    }
}
