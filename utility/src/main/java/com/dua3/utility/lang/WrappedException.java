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
    public static final String MARKER = "[WrappedException] ";

    /**
     * Construct new wrapped exception.
     *
     * @param cause the exception to wrap
     */
    public WrappedException(Exception cause) {
        super(cause);
    }

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
