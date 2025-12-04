package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReaderInputStreamTest {

    @Test
    void roundTripSimpleAndUnicode() throws IOException {
        String text = "Hello ÄÖÜ äöü ß — € 😀 𝄞 end"; // includes BMP and non-BMP
        try (Reader reader = new StringReader(text);
             InputStream in = new ReaderInputStream(reader)) {
            byte[] bytes = in.readAllBytes();
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            assertEquals(text, decoded);
        }
    }

    @Test
    void handlesSurrogatePairAcrossInternalBufferBoundary() throws IOException {
        // Build a string that puts a high surrogate at the end of the first internal chunk
        // ReaderInputStream uses a 2048-char internal buffer; the last char kept if it's a high surrogate
        int internalBuf = 2048;
        StringBuilder sb = new StringBuilder();
        sb.append("a".repeat(internalBuf - 1));
        // Append a surrogate pair (non-BMP character)
        String emoji = "\uD83D\uDE00"; // 😀 U+1F600
        sb.append(emoji);
        String text = sb.toString();

        try (InputStream in = new ReaderInputStream(new StringReader(text))) {
            String decoded = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(text, decoded);
        }
    }

    @Test
    void danglingHighSurrogateAtEofIsReplaced() throws IOException {
        // Construct a Java String containing a single unpaired high surrogate.
        char[] ca = {'\uD83D'}; // high surrogate without low surrogate
        String s = new String(ca);

        try (InputStream in = new ReaderInputStream(new StringReader(s))) {
            byte[] bytes = in.readAllBytes();
            // Encoding should use replacement char U+FFFD
            String decoded = new String(bytes, StandardCharsets.UTF_8);
            // Depending on JVM encoder behavior, invalid surrogate may map to U+FFFD or '?'
            assertTrue("\uFFFD".equals(decoded) || "?".equals(decoded),
                    () -> "Unexpected replacement for invalid surrogate: '" + decoded + "'");
        }
    }

    @Test
    void supportsIncrementalReads() throws IOException {
        String text = "prefix 😀 suffix";
        try (InputStream in = new ReaderInputStream(new StringReader(text))) {
            byte[] buf = new byte[3];
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            int n;
            while ((n = in.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
            String decoded = bos.toString(StandardCharsets.UTF_8);
            assertEquals(text, decoded);
        }
    }
}
