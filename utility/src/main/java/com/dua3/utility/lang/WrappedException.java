// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.cabe.annotations.NotNull;

import java.io.Serial;

/**
 * RuntimeException that wraps an unchecked exception.
 */
public class WrappedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public WrappedException(@NotNull Exception cause) {
        super(cause);
    }

    @Override
    public synchronized Exception getCause() {
        return (Exception) super.getCause();
    }

    @Override
    public String toString() {
        return "WrappedException(" + super.getCause().toString() + ")";
    }
}
