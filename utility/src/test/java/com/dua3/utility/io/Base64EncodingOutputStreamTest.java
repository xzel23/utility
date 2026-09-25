package com.dua3.utility.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SuppressFBWarnings
class Base64EncodingOutputStreamTest {

    @Test
    void testNullArguments() {
        assertThrows(Throwable.class, () -> new Base64EncodingOutputStream(null));
        assertThrows(Throwable.class, () -> new Base64EncodingOutputStream(null, Base64.getEncoder()));
        assertThrows(Throwable.class, () -> new Base64EncodingOutputStream(new ByteArrayOutputStream(), null));
    }

    @Test
    void testEmptyStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bos)) {
            // write nothing
        }
        assertEquals(0, bos.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 10, 50, 100, 3071, 3072, 3073, 6144, 10000})
    void testEncodingMatchesStandardEncoder(int size) throws IOException {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);

        byte[] expected = Base64.getEncoder().encode(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bos)) {
            out.write(data);
        }

        byte[] actual = bos.toByteArray();
        assertArrayEquals(expected, actual);
        assertArrayEquals(data, Base64.getDecoder().decode(actual));
    }

    @Test
    void testSingleByteWrites() throws IOException {
        byte[] data = "Hello, World! Testing Base64EncodingOutputStream single-byte write.".getBytes(StandardCharsets.UTF_8);
        byte[] expected = Base64.getEncoder().encode(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bos)) {
            for (byte b : data) {
                out.write(b & 0xFF);
            }
        }

        assertArrayEquals(expected, bos.toByteArray());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 7, 16, 64, 512, 1024, 4096})
    void testChunkedWrites(int chunkSize) throws IOException {
        byte[] data = new byte[10000];
        new Random(123).nextBytes(data);
        byte[] expected = Base64.getEncoder().encode(data);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bos)) {
            int offset = 0;
            while (offset < data.length) {
                int len = Math.min(chunkSize, data.length - offset);
                out.write(data, offset, len);
                offset += len;
            }
        }

        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void testCustomEncoders() throws IOException {
        byte[] data = new byte[1024];
        new Random(456).nextBytes(data);

        // URL Encoder
        byte[] expectedUrl = Base64.getUrlEncoder().encode(data);
        ByteArrayOutputStream bosUrl = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bosUrl, Base64.getUrlEncoder())) {
            out.write(data);
        }
        assertArrayEquals(expectedUrl, bosUrl.toByteArray());

        // Without padding
        byte[] expectedNoPadding = Base64.getEncoder().withoutPadding().encode(data);
        ByteArrayOutputStream bosNoPadding = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bosNoPadding, Base64.getEncoder().withoutPadding())) {
            out.write(data);
        }
        assertArrayEquals(expectedNoPadding, bosNoPadding.toByteArray());

        // MIME Encoder
        byte[] expectedMime = Base64.getMimeEncoder().encode(data);
        ByteArrayOutputStream bosMime = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bosMime, Base64.getMimeEncoder())) {
            out.write(data);
        }
        assertArrayEquals(expectedMime, bosMime.toByteArray());
    }

    @Test
    void testFlushAndClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream out = new Base64EncodingOutputStream(bos);
        out.write(new byte[]{1, 2, 3});
        out.flush();
        // A complete 3-byte tuple encodes to 4 bytes and should be flushed
        assertEquals(4, bos.size());
        out.close();
        assertEquals(4, bos.size());
    }

    @Test
    void testCloseClosesUnderlyingStream() throws IOException {
        AtomicBoolean closed = new AtomicBoolean(false);
        ByteArrayOutputStream underlying = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        OutputStream out = new Base64EncodingOutputStream(underlying);
        assertFalse(closed.get());
        out.close();
        assertTrue(closed.get());
    }

    @Test
    void testInteroperabilityWithInputStream() throws IOException {
        byte[] data = new byte[5000];
        new Random(789).nextBytes(data);

        // Encode via Base64EncodingOutputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream out = new Base64EncodingOutputStream(bos)) {
            out.write(data);
        }
        byte[] encodedViaOutputStream = bos.toByteArray();

        // Encode via Base64EncodingInputStream
        byte[] encodedViaInputStream;
        try (InputStream in = new Base64EncodingInputStream(new ByteArrayInputStream(data))) {
            encodedViaInputStream = in.readAllBytes();
        }

        assertArrayEquals(encodedViaOutputStream, encodedViaInputStream);
    }
}
