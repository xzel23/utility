// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import java.io.Serial;

/**
 * RuntimeException that wraps an unchecked exception.
 */
public class WrappedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String MARKER = "[WrappedException] ";

    /**
     * Construct a new {@code WappedException} instance.
     *
     * @param cause the exception to wrap
     */
    public WrappedException(Exception cause) {
        super(cause);
    }

    /**
     * Get the cause of this exception.
     *
     * @return the {@link Exception} wrapped by this instance; this method will never return {@code null}
     */
    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }

    @Override
    public String getMessage() {
        return MARKER + super.getMessage();
    }

    @Override
    public String toString() {
        return MARKER + super.getCause().toString();
    }
}
