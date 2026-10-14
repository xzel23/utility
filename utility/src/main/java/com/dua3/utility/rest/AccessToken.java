package com.dua3.utility.rest;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * A bearer access token and its optional expiry time.
 *
 * @param value the token value
 * @param expiresAt the expiry time, or {@code null} when unknown
 */
public record AccessToken(String value, @Nullable Instant expiresAt) {

    /**
     * Creates an access token.
     *
     * @param value the token value
     * @param expiresAt the expiry time, or {@code null} when unknown
     */
    public AccessToken {
        if (value.isBlank()) {
            throw new IllegalArgumentException("token value must not be blank");
        }
    }
}
