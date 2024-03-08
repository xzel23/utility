package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the LineOutputStream class.
 */
public class LineOutputStreamTest {

    /**
     * Test for the write(int b) method.
     *
     * This test checks that the write(int b) method correctly writes bytes to the output stream and splits them into lines.
     */
    @Test
    public void testWrite_UnixLineEnd() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            String textToWrite = "Hello\nWorld\n";
            textToWrite.chars().forEach(out::write);
        }

        assertEquals(3, outputLines.size());
        assertEquals("Hello", outputLines.get(0));
        assertEquals("World", outputLines.get(1));
        assertEquals("", outputLines.get(2));
    }

    @Test
    public void testWrite_WindowsLineEnd() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            String textToWrite = "Hello\r\nWorld\r\n";
            textToWrite.chars().forEach(out::write);
        }

        assertEquals(3, outputLines.size());
        assertEquals("Hello", outputLines.get(0));
        assertEquals("World", outputLines.get(1));
        assertEquals("", outputLines.get(2));
    }

    /**
     * Test for the close() method.
     *
     * This test ensures that the close() method flushes the current line, if any, to the output.
     */
    @Test
    public void testClose() throws IOException {
        List<String> outputLines = new ArrayList<>();
        var out = new LineOutputStream(outputLines::add);
        String textToWrite = "Hello World";

        textToWrite.chars().forEach(out::write);

        out.close();

        assertEquals(1, outputLines.size());
        assertEquals("Hello World", outputLines.get(0));
    }
}