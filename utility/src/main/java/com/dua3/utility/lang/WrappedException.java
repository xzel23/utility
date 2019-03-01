// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

/**
 * RuntimeException that wraps an unchecked exception.
 */
public class WrappedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public WrappedException(Exception cause) {
        super(cause);
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }

    @Override
    public String toString() {
        return "WrappedException(" + super.getCause().toString() + ")";
    }
}