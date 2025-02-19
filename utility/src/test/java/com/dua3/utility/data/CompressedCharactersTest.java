package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompressedCharactersTest {

    /**
     * Tests the compress(CharSequence) method for a valid input string.
     * Verifies that compressed data is not null.
     */
    @Test
    void testCompressWithCharSequence() {
        String input = "This is a test string for compression.";
        CompressedCharacters result = CompressedCharacters.compress(input);
        assertEquals(input, result.toString(), "CompressedCharacters.compress(CharSequence) restore the original input");
    }

    /**
     * Tests the compress(Reader) method for a valid Reader object.
     * Verifies that compressed data is not null.
     */
    @Test
    void testCompressWithReader() throws IOException {
        String input = "This is another test string for compression.";
        try (Reader reader = new StringReader(input)) {
            CompressedCharacters result = CompressedCharacters.compress(reader);
            assertEquals(input, result.toString(), "CompressedCharacters.compress(Reader) should restore the original input");
        }
    }

    /**
     * Tests the compress(Reader) method with an empty input from the Reader.
     * Verifies that compressed data is not null.
     */
    @Test
    void testCompressWithEmptyReader() throws IOException {
        try (Reader emptyReader = new StringReader("")) {
            CompressedCharacters result = CompressedCharacters.compress(emptyReader);
            assertTrue(result.toString().isEmpty(), "CompressedCharacters.compress(Reader) should return empty String for an empty Reader");
        }
    }

    /**
     * Tests the toReader() method to ensure it returns a valid Reader object
     * that matches the original input string after decompression.
     */
    @Test
    void testToReader() throws IOException {
        String input = "Testing toReader method.";
        CompressedCharacters compressed = CompressedCharacters.compress(input);

        try (Reader reader = compressed.toReader()) {
            StringBuilder output = new StringBuilder();
            int charRead;
            while ((charRead = reader.read()) != -1) {
                output.append((char) charRead);
            }

            assertEquals(input, output.toString(), "Decompressed Reader content should match the original input.");
        }
    }

    /**
     * Tests the toString() method to ensure it correctly returns the decompressed string.
     */
    @Test
    void testToString() {
        String input = "Testing toString method.";
        CompressedCharacters compressed = CompressedCharacters.compress(input);
        String decompressed = compressed.toString();
        assertEquals(input, decompressed, "toString() should return the original input string.");
    }
}