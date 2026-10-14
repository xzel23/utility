package com.dua3.utility.rest;

/**
 * The serialization format of an OpenAPI document.
 */
public enum OpenApiFormat {
    /** Detect JSON from the first non-whitespace character and otherwise use YAML. */
    AUTO,
    /** JSON OpenAPI document. */
    JSON,
    /** YAML OpenAPI document. */
    YAML
}
