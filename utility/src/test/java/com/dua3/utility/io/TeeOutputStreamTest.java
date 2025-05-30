// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the TeeOutputStream class.
 */
class TeeOutputStreamTest {

    /**
     * Test the write method.
     */
    @Test
    void testWrite() throws IOException {
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();

        try (TeeOutputStream tee = new TeeOutputStream(baos1, true, baos2, true)) {
            // Write a single byte
            tee.write(65); // ASCII 'A'

            // Write multiple bytes
            byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);
            tee.write(data);

            // Write a portion of an array
            tee.write(data, 7, 6); // "World!"
        }

        // Both streams should have the same content
        assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());

        // Verify the content
        String expected = "AHello, World!World!";
        assertEquals(expected, baos1.toString(StandardCharsets.UTF_8));
        assertEquals(expected, baos2.toString(StandardCharsets.UTF_8));
    }

    /**
     * Test the close method with close flags set to true.
     */
    @Test
    void testCloseWithCloseFlagsTrue() throws IOException {
        TestOutputStream tos1 = new TestOutputStream();
        TestOutputStream tos2 = new TestOutputStream();

        TeeOutputStream tee = new TeeOutputStream(tos1, true, tos2, true);
        tee.close();

        assertTrue(tos1.isClosed());
        assertTrue(tos2.isClosed());
        assertTrue(tos1.isFlushed()); // close implies flush
        assertTrue(tos2.isFlushed()); // close implies flush
    }

    /**
     * Test the close method with close flags set to false.
     */
    @Test
    void testCloseWithCloseFlagsFalse() throws IOException {
        TestOutputStream tos1 = new TestOutputStream();
        TestOutputStream tos2 = new TestOutputStream();

        TeeOutputStream tee = new TeeOutputStream(tos1, false, tos2, false);
        tee.close();

        assertFalse(tos1.isClosed());
        assertFalse(tos2.isClosed());
        assertTrue(tos1.isFlushed());
        assertTrue(tos2.isFlushed());
    }

    /**
     * Test the close method with mixed close flags.
     */
    @Test
    void testCloseWithMixedCloseFlags() throws IOException {
        TestOutputStream tos1 = new TestOutputStream();
        TestOutputStream tos2 = new TestOutputStream();

        TeeOutputStream tee = new TeeOutputStream(tos1, true, tos2, false);
        tee.close();

        assertTrue(tos1.isClosed());
        assertFalse(tos2.isClosed());
        assertTrue(tos1.isFlushed());
        assertTrue(tos2.isFlushed());
    }

    /**
     * Test the flush method.
     */
    @Test
    void testFlush() throws IOException {
        try (TestOutputStream tos1 = new TestOutputStream();
             TestOutputStream tos2 = new TestOutputStream();
             TeeOutputStream tee = new TeeOutputStream(tos1, true, tos2, true)) {
            tee.flush();

            assertTrue(tos1.isFlushed());
            assertTrue(tos2.isFlushed());
            assertFalse(tos1.isClosed());
            assertFalse(tos2.isClosed());
        }
    }

    /**
     * A test output stream that tracks whether it has been closed or flushed.
     */
    private static class TestOutputStream extends OutputStream {
        private boolean closed = false;
        private boolean flushed = false;

        @Override
        public void write(int b) {
            // Do nothing
        }

        @Override
        public void close() {
            closed = true;
            flushed = true; // close implies flush
        }

        @Override
        public void flush() {
            flushed = true;
        }

        public boolean isClosed() {
            return closed;
        }

        public boolean isFlushed() {
            return flushed;
        }
    }
}