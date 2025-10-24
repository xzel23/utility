package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StreamSupplierTest {

    @TempDir
    Path tmpDir;

    @Test
    void inputStream_supportedForInput_only() throws Exception {
        byte[] data = "hello input".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);

        InputStream supplied = StreamSupplier.getInputStream(in);
        assertSame(in, supplied);
        assertArrayEquals(data, supplied.readAllBytes());

        Throwable ex = assertThrows(Throwable.class, () -> StreamSupplier.getOutputStream(in));
        assertTrue(ex instanceof UnsupportedOperationException);
    }

    @Test
    void outputStream_supportedForOutput_only() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStream supplied = StreamSupplier.getOutputStream(out);
        assertSame(out, supplied);
        supplied.write("hello output".getBytes(StandardCharsets.UTF_8));
        supplied.flush();
        assertEquals("hello output", out.toString(StandardCharsets.UTF_8));

        Throwable ex = assertThrows(Throwable.class, () -> StreamSupplier.getInputStream(out));
        assertTrue(ex instanceof UnsupportedOperationException);
    }

    @Test
    void uri_supportedForBoth() throws Exception {
        Path file = Files.createTempFile(tmpDir, "ss-uri-", ".txt");
        URI uri = file.toUri();

        // write
        try (OutputStream os = StreamSupplier.getOutputStream(uri)) {
            os.write("from uri".getBytes(StandardCharsets.UTF_8));
        }

        // read
        try (InputStream is = StreamSupplier.getInputStream(uri)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("from uri", s);
        }
    }

    @Test
    void url_supportedForBoth_whenFileUrl() throws Exception {
        Path file = Files.createTempFile(tmpDir, "ss-url-", ".txt");
        URL url = file.toUri().toURL();

        try (OutputStream os = StreamSupplier.getOutputStream(url)) {
            os.write("from url".getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = StreamSupplier.getInputStream(url)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("from url", s);
        }
    }

    @Test
    void path_supportedForBoth() throws Exception {
        Path file = Files.createTempFile(tmpDir, "ss-path-", ".txt");

        try (OutputStream os = StreamSupplier.getOutputStream(file)) {
            os.write("from path".getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = StreamSupplier.getInputStream(file)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("from path", s);
        }
    }

    @Test
    void file_supportedForBoth() throws Exception {
        Path p = Files.createTempFile(tmpDir, "ss-file-", ".txt");
        File file = p.toFile();

        try (OutputStream os = StreamSupplier.getOutputStream(file)) {
            os.write("from file".getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream is = StreamSupplier.getInputStream(file)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("from file", s);
        }
    }

    @Test
    void reader_supportedForInput_only_and_closesUnderlyingReader() throws Exception {
        String text = "Rädersätze 🚴‍♀️"; // test some unicode
        class TestReader extends Reader {
            private final Reader delegate = new StringReader(text);
            private boolean closed;
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return delegate.read(cbuf, off, len);
            }
            @Override
            public void close() throws IOException {
                closed = true;
                delegate.close();
            }
        }
        TestReader reader = new TestReader();

        try (InputStream is = StreamSupplier.getInputStream(reader)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, s);
        }
        // verify reader was closed by closing the stream
        assertTrue(reader.closed);

        Throwable ex = assertThrows(Throwable.class, () -> StreamSupplier.getOutputStream(new StringReader(text)));
        assertTrue(ex instanceof UnsupportedOperationException);
    }

    @Test
    void unicode_roundtrip_whenWritingVia_getOutputStream_forAllSupportedTypes() throws Exception {
        String text = "Rädersätze 🚴‍♀️ — café naïve 🧪✨";

        // OutputStream passthrough
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStream os = StreamSupplier.getOutputStream(baos)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(text, baos.toString(StandardCharsets.UTF_8));

        // Path
        Path path = Files.createTempFile(tmpDir, "ss-unicode-path-", ".txt");
        try (OutputStream os = StreamSupplier.getOutputStream(path)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream is = StreamSupplier.getInputStream(path)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, s);
        }

        // File
        File file = Files.createTempFile(tmpDir, "ss-unicode-file-", ".txt").toFile();
        try (OutputStream os = StreamSupplier.getOutputStream(file)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream is = StreamSupplier.getInputStream(file)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, s);
        }

        // URI
        Path pUri = Files.createTempFile(tmpDir, "ss-unicode-uri-", ".txt");
        URI uri = pUri.toUri();
        try (OutputStream os = StreamSupplier.getOutputStream(uri)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream is = StreamSupplier.getInputStream(uri)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, s);
        }

        // URL (file url)
        Path pUrl = Files.createTempFile(tmpDir, "ss-unicode-url-", ".txt");
        URL url = pUrl.toUri().toURL();
        try (OutputStream os = StreamSupplier.getOutputStream(url)) {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        }
        try (InputStream is = StreamSupplier.getInputStream(url)) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, s);
        }
    }

    @Test
    void reader_inputStream_readByteArrayWithOffsetAndLen_behavesCorrectly() throws Exception {
        String text = "Rädersätze 🚴‍♀️ — café naïve 🧪✨";

        try (InputStream is = StreamSupplier.getInputStream(new StringReader(text))) {
            byte[] tmp = new byte[5];

            // len==0 must return 0 and not read anything
            assertEquals(0, is.read(tmp, 0, 0));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n;
            while ((n = is.read(tmp, 1, 3)) != -1) { // read into a non-zero offset
                baos.write(tmp, 1, n);
            }

            // after EOF, read must return -1
            assertEquals(-1, is.read(tmp, 2, 2));

            String s = baos.toString(StandardCharsets.UTF_8);
            assertEquals(text, s);
        }
    }

    @Test
    void reader_inputStream_danglingHighSurrogate_isReplacedAndThenEof() throws Exception {
        // Single unpaired high surrogate; UTF-8 encoding should yield replacement character
        String dangling = new String(new char[]{'\uD83D'});
        try (InputStream is = StreamSupplier.getInputStream(new StringReader(dangling))) {
            byte[] data = is.readAllBytes();
            String s = new String(data, StandardCharsets.UTF_8);
            // Java's UTF-8 encoder replaces unmappable surrogates with '?'
            assertEquals("?", s);
            // Repeated EOF reads must stay -1
            assertEquals(-1, is.read());
            assertEquals(-1, is.read());
        }
    }

    @Test
    void reader_inputStream_surrogatePairSplitAcrossReads_roundTrips() throws Exception {
        // 😀 U+1F600 represented as high surrogate D83D and low surrogate DE00
        final String emoji = "\uD83D\uDE00";
        // Custom reader that returns one char per read to force split across chunks
        class OneByOneReader extends Reader {
            private final char[] chars = emoji.toCharArray();
            private int idx = 0;
            @Override
            public int read(char[] cbuf, int off, int len) {
                if (idx >= chars.length) {
                    return -1;
                }
                if (len == 0) {
                    return 0;
                }
                cbuf[off] = chars[idx++];
                return 1;
            }
            @Override
            public void close() { /* no-op */ }
        }
        try (InputStream is = StreamSupplier.getInputStream(new OneByOneReader())) {
            // Mix read() and read(byte[],..)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int first = is.read();
            assertNotEquals(-1, first);
            baos.write(first);
            byte[] buf = new byte[8];
            int n = is.read(buf, 0, buf.length);
            if (n > 0) {
                baos.write(buf, 0, n);
            }
            String s = baos.toString(StandardCharsets.UTF_8);
            assertEquals(emoji, s);
            // EOF checks
            assertEquals(-1, is.read(buf));
            assertEquals(-1, is.read());
        }
    }
}