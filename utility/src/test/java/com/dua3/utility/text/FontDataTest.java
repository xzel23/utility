package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FontData} class.
 */
class FontDataTest {

    /**
     * Test the constructor and basic properties of FontData.
     */
    @Test
    void testConstructorAndProperties() {
        List<String> families = List.of("Arial", "Helvetica");
        float size = 12.0f;
        boolean monospaced = false;
        boolean bold = true;
        boolean italic = false;
        boolean underline = true;
        boolean strikeThrough = false;

        FontDef fontDef = new FontDef();
        fontDef.setFamilies(families);
        fontDef.setSize(size);
        fontDef.setBold(bold);
        fontDef.setItalic(italic);
        fontDef.setUnderline(underline);
        fontDef.setStrikeThrough(strikeThrough);

        String fontspec = fontDef.fontspec() + "-*";
        String cssStyle = fontDef.getCssStyle();
        double ascent = 10.0;
        double descent = 3.0;
        double height = 15.0;
        double spaceWidth = 5.0;

        FontData fontData = new FontData(
                families,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                fontDef,
                fontspec,
                cssStyle,
                ascent,
                descent,
                height,
                spaceWidth
        );

        // Test all properties
        assertEquals(families, fontData.families());
        assertEquals(size, fontData.size());
        assertEquals(monospaced, fontData.monospaced());
        assertEquals(bold, fontData.bold());
        assertEquals(italic, fontData.italic());
        assertEquals(underline, fontData.underline());
        assertEquals(strikeThrough, fontData.strikeThrough());
        // The FontData constructor removes the "-*" suffix from fontspec
        String expectedFontspec = fontspec.substring(0, fontspec.length() - 2);
        assertEquals(expectedFontspec, fontData.fontspec());
        assertEquals(cssStyle, fontData.cssStyle());
        assertEquals(ascent, fontData.ascent());
        assertEquals(descent, fontData.descent());
        assertEquals(height, fontData.height());
        assertEquals(spaceWidth, fontData.spaceWidth());
    }

    /**
     * Test the get method with a single family name.
     */
    @Test
    void testGetWithSingleFamily() {
        String family = "Times New Roman";
        float size = 14.0f;
        boolean monospaced = false;
        boolean bold = true;
        boolean italic = true;
        boolean underline = false;
        boolean strikeThrough = false;
        double ascent = 12.0;
        double descent = 4.0;
        double height = 18.0;
        double spaceWidth = 6.0;

        FontData fontData = FontData.get(
                family,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                ascent,
                descent,
                height,
                spaceWidth
        );

        assertEquals(List.of("Times New Roman"), fontData.families());
        assertEquals(size, fontData.size());
        assertEquals(monospaced, fontData.monospaced());
        assertEquals(bold, fontData.bold());
        assertEquals(italic, fontData.italic());
        assertEquals(underline, fontData.underline());
        assertEquals(strikeThrough, fontData.strikeThrough());
        assertEquals(ascent, fontData.ascent());
        assertEquals(descent, fontData.descent());
        assertEquals(height, fontData.height());
        assertEquals(spaceWidth, fontData.spaceWidth());
    }

    /**
     * Test the get method with multiple family names.
     */
    @Test
    void testGetWithMultipleFamilies() {
        List<String> families = List.of("Arial", "Helvetica", "SansSerif");
        float size = 10.0f;
        boolean monospaced = true;
        boolean bold = false;
        boolean italic = false;
        boolean underline = true;
        boolean strikeThrough = true;
        double ascent = 8.0;
        double descent = 2.0;
        double height = 12.0;
        double spaceWidth = 4.0;

        FontData fontData = FontData.get(
                families,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                ascent,
                descent,
                height,
                spaceWidth
        );

        assertEquals(families, fontData.families());
        assertEquals(size, fontData.size());
        assertEquals(monospaced, fontData.monospaced());
        assertEquals(bold, fontData.bold());
        assertEquals(italic, fontData.italic());
        assertEquals(underline, fontData.underline());
        assertEquals(strikeThrough, fontData.strikeThrough());
        assertEquals(ascent, fontData.ascent());
        assertEquals(descent, fontData.descent());
        assertEquals(height, fontData.height());
        assertEquals(spaceWidth, fontData.spaceWidth());
    }

    /**
     * Test the descentSigned method.
     */
    @Test
    void testDescentSigned() {
        List<String> families = List.of("Arial");
        float size = 12.0f;
        boolean monospaced = false;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikeThrough = false;

        FontDef fontDef = new FontDef();
        fontDef.setFamilies(families);
        fontDef.setSize(size);
        fontDef.setBold(bold);
        fontDef.setItalic(italic);
        fontDef.setUnderline(underline);
        fontDef.setStrikeThrough(strikeThrough);

        String fontspec = fontDef.fontspec();
        String cssStyle = fontDef.getCssStyle();
        double ascent = 10.0;
        double descent = 3.0;
        double height = 15.0;
        double spaceWidth = 5.0;

        FontData fontData = new FontData(
                families,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                fontDef,
                fontspec,
                cssStyle,
                ascent,
                descent,
                height,
                spaceWidth
        );

        assertEquals(-descent, fontData.descentSigned());
    }

    /**
     * Test the similar method.
     */
    @Test
    void testSimilar() {
        // Create two similar fonts
        FontData font1 = FontData.get(
                "Arial",
                12.0f,
                false,
                true,
                false,
                true,
                false,
                10.0,
                3.0,
                15.0,
                5.0
        );

        FontData font2 = FontData.get(
                "Arial",
                12.0f,
                false,
                true,
                false,
                true,
                false,
                11.0, // Different ascent
                4.0,  // Different descent
                16.0, // Different height
                6.0   // Different spaceWidth
        );

        // These should be similar because similar() only compares families, size, and style attributes
        assertTrue(FontData.similar(font1, font2));

        // Create a different font
        FontData font3 = FontData.get(
                "Times New Roman", // Different family
                12.0f,
                false,
                true,
                false,
                true,
                false,
                10.0,
                3.0,
                15.0,
                5.0
        );

        assertFalse(FontData.similar(font1, font3));

        // Create another different font
        FontData font4 = FontData.get(
                "Arial",
                14.0f, // Different size
                false,
                true,
                false,
                true,
                false,
                10.0,
                3.0,
                15.0,
                5.0
        );

        assertFalse(FontData.similar(font1, font4));
    }

    /**
     * Test the delta method.
     */
    @Test
    void testDelta() {
        FontData font1 = FontData.get(
                "Arial",
                12.0f,
                false,
                true,
                false,
                true,
                false,
                10.0,
                3.0,
                15.0,
                5.0
        );

        FontData font2 = FontData.get(
                "Times New Roman",
                14.0f,
                false,
                false,
                true,
                true,
                true,
                10.0,
                3.0,
                15.0,
                5.0
        );

        FontDef delta = FontData.delta(font1, font2);

        // Check that delta contains only the changed values
        assertEquals(List.of("Times New Roman"), delta.getFamilies());
        assertEquals(14.0f, delta.getSize());
        assertFalse(delta.getBold());
        assertTrue(delta.getItalic());
        assertEquals(true, delta.getStrikeThrough());

        // Test with null values
        FontDef deltaWithNull = FontData.delta(null, font2);
        assertEquals(List.of("Times New Roman"), deltaWithNull.getFamilies());
        assertEquals(14.0f, deltaWithNull.getSize());
        assertFalse(deltaWithNull.getBold());
        assertTrue(deltaWithNull.getItalic());
        assertTrue(deltaWithNull.getUnderline());
        assertTrue(deltaWithNull.getStrikeThrough());

        FontDef deltaWithNull2 = FontData.delta(font1, null);
        assertTrue(deltaWithNull2.isEmpty());
    }

    /**
     * Test the fontDef method.
     */
    @Test
    void testFontDef() {
        List<String> families = List.of("Arial");
        float size = 12.0f;
        boolean monospaced = false;
        boolean bold = true;
        boolean italic = false;
        boolean underline = true;
        boolean strikeThrough = false;

        FontDef originalFontDef = new FontDef();
        originalFontDef.setFamilies(families);
        originalFontDef.setSize(size);
        originalFontDef.setBold(bold);
        originalFontDef.setItalic(italic);
        originalFontDef.setUnderline(underline);
        originalFontDef.setStrikeThrough(strikeThrough);

        String fontspec = originalFontDef.fontspec();
        String cssStyle = originalFontDef.getCssStyle();
        double ascent = 10.0;
        double descent = 3.0;
        double height = 15.0;
        double spaceWidth = 5.0;

        FontData fontData = new FontData(
                families,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                originalFontDef,
                fontspec,
                cssStyle,
                ascent,
                descent,
                height,
                spaceWidth
        );

        FontDef copiedFontDef = fontData.fontDef();

        // Verify that it's a copy, not the same instance
        assertNotSame(originalFontDef, copiedFontDef);

        // Verify that the copy has the same values
        assertEquals(originalFontDef.getFamilies(), copiedFontDef.getFamilies());
        assertEquals(originalFontDef.getSize(), copiedFontDef.getSize());
        assertEquals(originalFontDef.getBold(), copiedFontDef.getBold());
        assertEquals(originalFontDef.getItalic(), copiedFontDef.getItalic());
        assertEquals(originalFontDef.getUnderline(), copiedFontDef.getUnderline());
        assertEquals(originalFontDef.getStrikeThrough(), copiedFontDef.getStrikeThrough());
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        FontData fontData = FontData.get(
                "Arial",
                12.0f,
                false,
                true,
                false,
                true,
                false,
                10.0,
                3.0,
                15.0,
                5.0
        );

        assertEquals(fontData.fontspec(), fontData.toString());
    }

    /**
     * Test the family method.
     */
    @Test
    void testFamily() {
        List<String> families = List.of("Arial", "Helvetica", "SansSerif");
        float size = 12.0f;
        boolean monospaced = false;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikeThrough = false;

        FontDef fontDef = new FontDef();
        fontDef.setFamilies(families);
        fontDef.setSize(size);
        fontDef.setBold(bold);
        fontDef.setItalic(italic);
        fontDef.setUnderline(underline);
        fontDef.setStrikeThrough(strikeThrough);

        String fontspec = fontDef.fontspec();
        String cssStyle = fontDef.getCssStyle();
        double ascent = 10.0;
        double descent = 3.0;
        double height = 15.0;
        double spaceWidth = 5.0;

        FontData fontData = new FontData(
                families,
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                fontDef,
                fontspec,
                cssStyle,
                ascent,
                descent,
                height,
                spaceWidth
        );

        // The family method should return the first family in the list
        assertEquals("Arial", fontData.family());
    }
}
