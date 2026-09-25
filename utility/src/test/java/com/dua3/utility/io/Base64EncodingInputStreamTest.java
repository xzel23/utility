package com.dua3.utility.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SuppressFBWarnings
class Base64EncodingInputStreamTest {

    @Test
    void testNullArguments() {
        assertThrows(Throwable.class, () -> new Base64EncodingInputStream(null));
        assertThrows(Throwable.class, () -> new Base64EncodingInputStream(null, Base64.getEncoder()));
        assertThrows(Throwable.class, () -> new Base64EncodingInputStream(new ByteArrayInputStream(new byte[0]), null));
    }

    @Test
    void testEmptyStream() throws IOException {
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(new byte[0]))) {
            byte[] result = in.readAllBytes();
            assertEquals(0, result.length);
            assertEquals(-1, in.read());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 10, 50, 100, 3071, 3072, 3073, 6144, 10000})
    void testEncodingMatchesStandardEncoder(int size) throws IOException {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);

        byte[] expected = Base64.getEncoder().encode(data);

        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            byte[] actual = in.readAllBytes();
            assertArrayEquals(expected, actual);
            assertArrayEquals(data, Base64.getDecoder().decode(actual));
        }
    }

    @Test
    void testSingleByteReads() throws IOException {
        byte[] data = "Hello, World! Testing Base64EncodingInputStream.".getBytes(StandardCharsets.UTF_8);
        byte[] expected = Base64.getEncoder().encode(data);

        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                bos.write(b);
            }
            assertArrayEquals(expected, bos.toByteArray());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 7, 16, 64, 512, 1024, 4096})
    void testChunkedReads(int chunkSize) throws IOException {
        byte[] data = new byte[10000];
        new Random(123).nextBytes(data);
        byte[] expected = Base64.getEncoder().encode(data);

        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[chunkSize];
            int n;
            while ((n = in.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, n);
            }
            assertArrayEquals(expected, bos.toByteArray());
        }
    }

    @Test
    void testReadZeroBytes() throws IOException {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[10];
            assertEquals(0, in.read(buf, 0, 0));
        }
    }

    @Test
    void testReadOutOfBounds() throws IOException {
        byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            byte[] buf = new byte[10];
            assertThrows(IndexOutOfBoundsException.class, () -> in.read(buf, -1, 5));
            assertThrows(IndexOutOfBoundsException.class, () -> in.read(buf, 0, 15));
            assertThrows(IndexOutOfBoundsException.class, () -> in.read(buf, 5, 10));
        }
    }

    @Test
    void testCustomEncoders() throws IOException {
        byte[] data = new byte[1024];
        new Random(456).nextBytes(data);

        // URL Encoder
        byte[] expectedUrl = Base64.getUrlEncoder().encode(data);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data), Base64.getUrlEncoder())) {
            assertArrayEquals(expectedUrl, in.readAllBytes());
        }

        // Without padding
        byte[] expectedNoPadding = Base64.getEncoder().withoutPadding().encode(data);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data), Base64.getEncoder().withoutPadding())) {
            assertArrayEquals(expectedNoPadding, in.readAllBytes());
        }

        // MIME Encoder
        byte[] expectedMime = Base64.getMimeEncoder().encode(data);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data), Base64.getMimeEncoder())) {
            assertArrayEquals(expectedMime, in.readAllBytes());
        }
    }

    @Test
    void testAvailable() throws IOException {
        byte[] data = "Some non-empty text".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            assertTrue(in.available() > 0, "available() should be > 0 when underlying stream has bytes");
            in.readAllBytes();
            assertEquals(0, in.available(), "available() should be 0 when EOF reached");
        }
    }

    @Test
    void testCloseClosesUnderlyingStream() throws IOException {
        AtomicBoolean closed = new AtomicBoolean(false);
        InputStream underlying = new ByteArrayInputStream(new byte[10]) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        InputStream in = new Base64EncodingInputStream(underlying);
        assertFalse(closed.get());
        in.close();
        assertTrue(closed.get());
    }
}
