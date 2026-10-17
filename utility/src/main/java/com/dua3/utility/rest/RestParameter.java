package com.dua3.utility.rest;

import com.dua3.utility.lang.LangUtil;

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
        LangUtil.checkArg(!name.isBlank(), "name must not be blank");
        if (location == RestParameterLocation.PATH && !required) {
            throw new IllegalArgumentException("path parameters must be required");
        }
    }
}
