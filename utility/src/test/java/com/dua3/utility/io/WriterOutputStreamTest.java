package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WriterOutputStreamTest {

    @Test
    void roundTripSimpleAndUnicode_fullWrite() throws IOException {
        String text = "Hello ÄÖÜ äöü ß — € 😀 𝄞 end";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringWriter sw = new StringWriter();
        try (WriterOutputStream wos = new WriterOutputStream(sw)) {
            wos.write(bytes);
        }
        assertEquals(text, sw.toString());
    }

    @Test
    void roundTrip_withSmallChunks() throws IOException {
        String text = "prefix 😀 middle € suffix";
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringWriter sw = new StringWriter();
        try (WriterOutputStream wos = new WriterOutputStream(sw)) {
            // write in tiny chunks to force boundary conditions
            int i = 0;
            while (i < bytes.length) {
                int n = Math.min(1 + (i % 3), 2); // alternate chunk sizes 1 or 2
                wos.write(bytes, i, Math.min(n, bytes.length - i));
                i += n;
            }
        }
        assertEquals(text, sw.toString());
    }

    @Test
    void partialUtf8SequenceAcrossWrites_thenFlush_thenComplete() throws IOException {
        // 😀 U+1F600 is 4 bytes in UTF-8
        String s = "😀";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        assertEquals(4, bytes.length);

        StringWriter sw = new StringWriter();
        try (WriterOutputStream wos = new WriterOutputStream(sw)) {
            // write first 2 bytes of the 4-byte sequence
            wos.write(bytes, 0, 2);
            wos.flush();
            // Incomplete sequence must not have produced any character yet
            assertEquals("", sw.toString());

            // complete the character
            wos.write(bytes, 2, 2);
        }
        assertEquals(s, sw.toString());
    }

    @Test
    void unterminatedSequenceOnClose_replaced() throws IOException {
        // Start a 2-byte sequence for 'Ã' (C3) without the trailing byte
        byte[] incomplete = {(byte) 0xC3};
        StringWriter sw = new StringWriter();
        try (WriterOutputStream wos = new WriterOutputStream(sw)) {
            wos.write(incomplete);
            // no flush here; close should finalize and replace
        }
        assertEquals("\uFFFD", sw.toString());
    }
}
