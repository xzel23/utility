package com.dua3.utility.rest;

import com.dua3.utility.lang.LangUtil;
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
        LangUtil.checkArg(!value.isBlank(), "value must not be blank");
    }
}
