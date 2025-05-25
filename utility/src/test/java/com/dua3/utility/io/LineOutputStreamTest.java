package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the LineOutputStream class.
 */
class LineOutputStreamTest {

    @Test
    void testWrite_UnixLineEnd() throws IOException {
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
    void testWrite_WindowsLineEnd() throws IOException {
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
     * <p>
     * This test ensures that the close() method flushes the current line, if any, to the output.
     */
    @Test
    void testClose() throws IOException {
        List<String> outputLines = new ArrayList<>();
        var out = new LineOutputStream(outputLines::add);
        String textToWrite = "Hello World";

        textToWrite.chars().forEach(out::write);

        out.close();

        assertEquals(1, outputLines.size());
        assertEquals("Hello World", outputLines.get(0));
    }

    /**
     * Test the write method with a single character that is not a newline.
     *
     * @throws IOException on I/O error
     */
    @Test
    void testWrite_SingleCharacter() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            out.write('A'); // write a single character
        }
        assertEquals(1, outputLines.size());
        assertEquals("A", outputLines.get(0)); // ensure the single character is captured
    }

    /**
     * Test the write method with an empty line (a single newline character).
     *
     * @throws IOException on I/O error
     */
    @Test
    void testWrite_EmptyLine() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            out.write('\n'); // write a single newline
        }
        assertEquals(2, outputLines.size());
        assertEquals("", outputLines.get(0));
        assertEquals("", outputLines.get(1));
    }

    /**
     * Test the write method with multiple newlines written sequentially.
     *
     * @throws IOException on I/O error
     */
    @Test
    void testWrite_MultipleNewlines() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            String textToWrite = "\n\n\n"; // multiple newlines
            textToWrite.chars().forEach(out::write);
        }
        assertEquals(4, outputLines.size());
        assertEquals("", outputLines.get(0));
        assertEquals("", outputLines.get(1));
        assertEquals("", outputLines.get(2));
        assertEquals("", outputLines.get(3));
    }

    /**
     * Test the write method with a large input to verify buffer resizing and line flushing.
     *
     * @throws IOException on I/O error
     */
    @Test
    void testWrite_LargeInput() throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (var out = new LineOutputStream(outputLines::add)) {
            char[] largeInput = new char[LineOutputStream.MAX_BUFFER_SIZE * 2];
            Arrays.fill(largeInput, 'A');
            String textToWrite = new String(largeInput) + "\n"; // ensure ends with a newline
            textToWrite.chars().forEach(out::write);
        }
        assertEquals(2, outputLines.size());
        assertEquals("A".repeat(LineOutputStream.MAX_BUFFER_SIZE * 2), outputLines.get(0));
        assertEquals("", outputLines.get(1));
    }
}