package com.dua3.utility.awt;

import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AwtFontUtil class.
 */
class AwtFontUtilTest {

    private AwtFontUtil fontUtil;
    private Font defaultFont;

    @BeforeEach
    void setUp() {
        fontUtil = AwtFontUtil.getInstance();
        defaultFont = fontUtil.getDefaultFont();
    }

    @Test
    void testGetInstance() {
        assertNotNull(fontUtil);
        // Test singleton pattern
        assertSame(fontUtil, AwtFontUtil.getInstance());
    }

    @Test
    void testGetDefaultFont() {
        assertNotNull(defaultFont);
    }

    @Test
    void testStringBounds() {
        String text = "Test String";
        java.awt.geom.Rectangle2D bounds = fontUtil.stringBounds(text, defaultFont);
        assertNotNull(bounds);
        assertTrue(bounds.getWidth() > 0);
        assertTrue(bounds.getHeight() > 0);
    }

    @Test
    void testGetTextDimension() {
        String text = "Test String";
        Rectangle2f dimension = fontUtil.getTextDimension(text, defaultFont);
        assertNotNull(dimension);
        assertTrue(dimension.width() > 0);
        assertTrue(dimension.height() > 0);
    }

    @Test
    void testGetTextHeight() {
        String text = "Test String";
        double height = fontUtil.getTextHeight(text, defaultFont);
        assertTrue(height > 0);
    }

    @Test
    void testGetTextWidth() {
        String text = "Test String";
        double width = fontUtil.getTextWidth(text, defaultFont);
        assertTrue(width > 0);
    }

    @Test
    void testGetFamilies() {
        SequencedCollection<String> families = fontUtil.getFamilies();

        assertNotNull(families);

        // Check if we have at least one font
        if (GraphicsEnvironment.isHeadless()) {
            // In headless mode, we might not have any fonts
            System.out.println("Running in headless mode, skipping font count assertions");
        } else {
            assertTrue(families.size() > 0, "No fonts found");
        }

        // Test getFamilies with specific types
        SequencedCollection<String> monospacedFamilies = fontUtil.getFamilies(FontUtil.FontTypes.MONOSPACED);
        SequencedCollection<String> proportionalFamilies = fontUtil.getFamilies(FontUtil.FontTypes.PROPORTIONAL);

        assertNotNull(monospacedFamilies);
        assertNotNull(proportionalFamilies);
    }

    @Test
    void testConvert() {
        // Test convert from AWT Font to Font
        java.awt.Font awtFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 12);
        Font font = fontUtil.convert(awtFont);
        assertNotNull(font);

        // Test convert from Font to AWT Font
        java.awt.Font convertedAwtFont = fontUtil.convert(font);
        assertNotNull(convertedAwtFont);
    }

    @Test
    void testDeriveFont() {
        // Create a font definition with different properties
        FontDef fontDef = new FontDef();
        fontDef.setFamily("Arial");
        fontDef.setSize(16f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        // Derive a new font from the default font
        Font derivedFont = fontUtil.deriveFont(defaultFont, fontDef);

        assertNotNull(derivedFont);
        assertEquals(16f, derivedFont.getSizeInPoints(), 0.001);
        assertTrue(derivedFont.isBold());
        assertTrue(derivedFont.isItalic());
    }

    // Note: getFontData is a private method, so we test it indirectly through convert
    @Test
    void testConvertFontProperties() {
        java.awt.Font awtFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 12);
        Font font = fontUtil.convert(awtFont);

        assertNotNull(font);
        assertEquals(12f, font.getSizeInPoints(), 0.001);
        assertFalse(font.isBold());
        assertFalse(font.isItalic());

        // Test with bold and italic
        java.awt.Font boldItalicFont = new java.awt.Font("Arial", java.awt.Font.BOLD | java.awt.Font.ITALIC, 14);
        Font boldItalicConverted = fontUtil.convert(boldItalicFont);

        assertNotNull(boldItalicConverted);
        assertEquals(14f, boldItalicConverted.getSizeInPoints(), 0.001);
        assertTrue(boldItalicConverted.isBold());
        assertTrue(boldItalicConverted.isItalic());
    }

    @Test
    void testLoadFonts() throws Exception {
        try (InputStream in = AwtFontUtilTest.class.getResourceAsStream("NotoSansCJK-Regular.ttc")) {
            List<Font> fonts = fontUtil.loadFonts(in);
            assertEquals(10, fonts.size());
        }
    }

    @Test
    void testLoadFontAs() throws Exception {
        try (InputStream in = AwtFontUtilTest.class.getResourceAsStream("bitstream_vera_sans/Vera.ttf")) {
            Font font = fontUtil.loadFontAs(in, defaultFont);
            assertNotNull(font);
            assertEquals(defaultFont.fontspec(), font.fontspec());
        }
    }
}
