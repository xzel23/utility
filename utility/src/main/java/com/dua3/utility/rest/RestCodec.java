package com.dua3.utility.rest;

import java.net.http.HttpResponse;

/**
 * Serializes request values and deserializes response values for a REST API.
 */
public interface RestCodec {

    /**
     * Serializes a value into a request body.
     *
     * @param value the value to serialize
     * @return a repeatable request body
     */
    RestBody write(Object value);

    /**
     * Creates a body handler that deserializes a response into {@code type}.
     *
     * @param type the result type
     * @param <T> the result type
     * @return a response body handler
     */
    <T> HttpResponse.BodyHandler<T> bodyHandler(Class<T> type);

    /**
     * Gets the media type produced by this codec.
     *
     * @return the media type
     */
    String contentType();
}
