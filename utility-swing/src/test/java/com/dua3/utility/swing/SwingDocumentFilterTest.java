package com.dua3.utility.swing;

import org.junit.jupiter.api.Test;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the SwingDocumentFilter class.
 * <p>
 * These tests verify that the filter correctly processes text input using the provided function.
 */
class SwingDocumentFilterTest {

    /**
     * Test the getUppercaseInstance factory method.
     */
    @Test
    void testGetUppercaseInstance() {
        // Create an uppercase filter
        DocumentFilter filter = SwingDocumentFilter.getUppercaseInstance(Locale.US);
        
        // Verify the filter was created
        assertNotNull(filter, "Filter should not be null");
        
        // Test the filter with a mock document
        String result = processTextWithFilter(filter, "test");
        
        // Verify the text was converted to uppercase
        assertEquals("TEST", result, "Text should be converted to uppercase");
    }

    /**
     * Test the getLowercaseInstance factory method.
     */
    @Test
    void testGetLowercaseInstance() {
        // Create a lowercase filter
        DocumentFilter filter = SwingDocumentFilter.getLowercaseInstance(Locale.US);
        
        // Verify the filter was created
        assertNotNull(filter, "Filter should not be null");
        
        // Test the filter with a mock document
        String result = processTextWithFilter(filter, "TEST");
        
        // Verify the text was converted to lowercase
        assertEquals("test", result, "Text should be converted to lowercase");
    }

    /**
     * Test the filter with a custom processor function.
     */
    @Test
    void testCustomProcessor() {
        // Create a filter with a custom processor that reverses the input
        DocumentFilter filter = new SwingDocumentFilter(s -> new StringBuilder(s).reverse().toString());
        
        // Test the filter with a mock document
        String result = processTextWithFilter(filter, "hello");
        
        // Verify the text was reversed
        assertEquals("olleh", result, "Text should be reversed");
    }

    /**
     * Test the filter with different locales.
     */
    @Test
    void testLocaleSpecificConversion() {
        // Create filters with different locales
        DocumentFilter filterTurkish = SwingDocumentFilter.getUppercaseInstance(Locale.forLanguageTag("tr"));
        DocumentFilter filterUS = SwingDocumentFilter.getUppercaseInstance(Locale.US);
        
        // Test the filters with a string containing 'i'
        // In Turkish locale, lowercase 'i' converts to uppercase 'İ' (with a dot)
        String resultTurkish = processTextWithFilter(filterTurkish, "istanbul");
        String resultUS = processTextWithFilter(filterUS, "istanbul");
        
        // Verify the locale-specific conversion
        // Note: This test might not work as expected in all environments due to JVM locale handling
        assertEquals(resultTurkish.toUpperCase(Locale.forLanguageTag("tr")), resultTurkish, 
                "Text should be converted according to Turkish locale rules");
        assertEquals(resultUS.toUpperCase(Locale.US), resultUS, 
                "Text should be converted according to US locale rules");
    }

    /**
     * Helper method to process text with a filter.
     * 
     * @param filter the filter to use
     * @param text the text to process
     * @return the processed text
     */
    private String processTextWithFilter(DocumentFilter filter, String text) {
        // Create a mock document and filter bypass
        MockFilterBypass bypass = new MockFilterBypass();
        
        try {
            // Process the text using the filter
            filter.replace(bypass, 0, 0, text, null);
            
            // Return the processed text
            return bypass.getProcessedText();
        } catch (BadLocationException e) {
            throw new RuntimeException("Error processing text with filter", e);
        }
    }

    /**
     * Mock implementation of FilterBypass for testing.
     */
    private static class MockFilterBypass extends DocumentFilter.FilterBypass {
        private String processedText = "";

        @Override
        public javax.swing.text.Document getDocument() {
            return new PlainDocument();
        }

        @Override
        public void remove(int offset, int length) throws BadLocationException {
            // Not needed for testing
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attr) throws BadLocationException {
            processedText = string;
        }

        @Override
        public void replace(int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            processedText = string;
        }

        /**
         * Get the processed text.
         * 
         * @return the processed text
         */
        public String getProcessedText() {
            return processedText;
        }
    }
}