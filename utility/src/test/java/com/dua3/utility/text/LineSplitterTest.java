package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the LineSplitter class.
 */
class LineSplitterTest {

    @Test
    void testProcess() throws IOException {
        // Test with simple text
        String text = "Hello world";
        int maxWidth = 20;
        boolean isHardWrap = false;
        String spaceChar = " ";

        Supplier<StringBuilder> bufferFactory = StringBuilder::new;
        Function<StringBuilder, String> readBuffer = StringBuilder::toString;
        ToIntFunction<StringBuilder> bufferLength = StringBuilder::length;

        List<List<String>> result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        // Check that the result is not empty
        assertFalse(result.isEmpty(), "Result should not be empty");
        if (!result.isEmpty()) {
            // Check that the first paragraph has one line
            assertEquals(1, result.get(0).size(), "Paragraph should have 1 line");
            // Check that the line contains the expected text
            assertEquals("Hello world", result.get(0).get(0), "Line should be 'Hello world'");
        }

        // Test with text requiring wrapping
        text = "This is a longer text that should be wrapped to multiple lines";
        maxWidth = 20;

        result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        // Check that the result is not empty
        assertFalse(result.isEmpty(), "Result should not be empty");
        if (!result.isEmpty()) {
            // Check that the first paragraph has multiple lines
            assertTrue(result.get(0).size() > 1, "Paragraph should have multiple lines");
            // Check that each line is within the maxWidth
            for (String line : result.get(0)) {
                assertTrue(line.length() <= maxWidth, "Each line should be <= maxWidth");
            }
        }

        // Test with hard wrap
        isHardWrap = true;

        result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        // Check that the result is not empty
        assertFalse(result.isEmpty(), "Result should not be empty");
        if (!result.isEmpty()) {
            // Check that the first paragraph has multiple lines
            assertTrue(result.get(0).size() > 1, "Paragraph should have multiple lines");
            // Check that each line is within the maxWidth
            for (String line : result.get(0)) {
                assertTrue(line.length() <= maxWidth, "Each line should be <= maxWidth");
            }
        }

        // Test with multiple paragraphs
        text = "Paragraph 1\n\nParagraph 2";
        isHardWrap = false;

        result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        // Check that the result has two paragraphs
        assertEquals(2, result.size(), "Should have 2 paragraphs");
        // Check the content of the paragraphs
        assertEquals("Paragraph 1", result.get(0).get(0), "First paragraph should be 'Paragraph 1'");
        assertEquals("Paragraph 2", result.get(1).get(0), "Second paragraph should be 'Paragraph 2'");
    }

    @Test
    void testProcessWithEmptyText() throws IOException {
        String text = "";
        int maxWidth = 20;
        boolean isHardWrap = false;
        String spaceChar = " ";

        Supplier<StringBuilder> bufferFactory = StringBuilder::new;
        Function<StringBuilder, String> readBuffer = StringBuilder::toString;
        ToIntFunction<StringBuilder> bufferLength = StringBuilder::length;

        List<List<String>> result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        // The actual behavior is that an empty list is returned for empty text
        assertEquals(0, result.size(), "Should have 0 paragraphs for empty text");
    }

    @Test
    void testProcessWithLongWord() throws IOException {
        // Test with a word longer than maxWidth
        String text = "Supercalifragilisticexpialidocious";
        int maxWidth = 10;
        boolean isHardWrap = true; // Hard wrap should break the word
        String spaceChar = " ";

        Supplier<StringBuilder> bufferFactory = StringBuilder::new;
        Function<StringBuilder, String> readBuffer = StringBuilder::toString;
        ToIntFunction<StringBuilder> bufferLength = StringBuilder::length;

        List<List<String>> result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        assertEquals(1, result.size(), "Should have 1 paragraph");
        assertTrue(result.get(0).size() > 1, "Paragraph should have multiple lines");
        for (String line : result.get(0)) {
            assertTrue(line.length() <= maxWidth, "Each line should be <= maxWidth");
        }

        // Test with soft wrap (should not break the word)
        isHardWrap = false;

        result = LineSplitter.process(text, maxWidth, isHardWrap, spaceChar, bufferFactory, readBuffer, bufferLength);

        assertEquals(1, result.size(), "Should have 1 paragraph");
        assertEquals(1, result.get(0).size(), "Paragraph should have 1 line for soft wrap");
        assertTrue(result.get(0).get(0).length() > maxWidth, "Line should be longer than maxWidth for soft wrap");
    }
}
