package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WrappedExceptionTest {

    @Test
    void testWrappedExceptionProperties() {
        IOException cause = new IOException("Disk failure");
        WrappedException wrapped = new WrappedException(cause);

        assertSame(cause, wrapped.getCause());
        assertTrue(wrapped.getMessage().startsWith("[WrappedException] "));
        assertTrue(wrapped.getMessage().contains("Disk failure"));
        assertTrue(wrapped.toString().startsWith("[WrappedException] "));
        assertTrue(wrapped.toString().contains("java.io.IOException: Disk failure"));
    }
}
