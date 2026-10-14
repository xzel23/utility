package com.dua3.utility.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Headers and query parameters contributed by an authentication mechanism.
 *
 * @param headers HTTP headers
 * @param queryParameters URI query parameters
 */
public record RestCredentials(Map<String, List<String>> headers,
                              Map<String, List<String>> queryParameters) {

    /**
     * Creates immutable credentials.
     *
     * @param headers HTTP headers
     * @param queryParameters URI query parameters
     */
    public RestCredentials {
        headers = immutableCopy(headers, "headers");
        queryParameters = immutableCopy(queryParameters, "queryParameters");
    }

    /**
     * Returns empty credentials.
     *
     * @return empty credentials
     */
    public static RestCredentials none() {
        return new RestCredentials(Map.of(), Map.of());
    }

    /**
     * Creates credentials containing one HTTP header.
     *
     * @param name the header name
     * @param value the header value
     * @return credentials with the header
     */
    public static RestCredentials header(String name, String value) {
        return new RestCredentials(Map.of(name, List.of(value)), Map.of());
    }

    /**
     * Creates credentials containing one query parameter.
     *
     * @param name the query parameter name
     * @param value the query parameter value
     * @return credentials with the query parameter
     */
    public static RestCredentials queryParameter(String name, String value) {
        return new RestCredentials(Map.of(), Map.of(name, List.of(value)));
    }

    private static Map<String, List<String>> immutableCopy(Map<String, List<String>> values, String name) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            Objects.requireNonNull(key, () -> name + " contains a null name");
            Objects.requireNonNull(value, () -> name + " contains a null value list");
            List<String> copiedValues = new ArrayList<>(value.size());
            for (String item : value) {
                copiedValues.add(Objects.requireNonNull(item, () -> name + " contains a null value"));
            }
            copy.put(key, List.copyOf(copiedValues));
        });
        return Map.copyOf(copy);
    }
}
