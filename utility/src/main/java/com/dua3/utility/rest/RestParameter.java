package com.dua3.utility.rest;

import java.util.Objects;

/**
 * Declares a parameter accepted by a REST endpoint.
 *
 * @param name the parameter name
 * @param location the request part containing the parameter
 * @param required whether a request must provide the parameter
 */
public record RestParameter(String name, RestParameterLocation location, boolean required) {

    /**
     * Creates a parameter declaration.
     *
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public RestParameter {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("parameter name must not be blank");
        }
        Objects.requireNonNull(location, "location");
        if (location == RestParameterLocation.PATH && !required) {
            throw new IllegalArgumentException("path parameters must be required");
        }
    }
}
