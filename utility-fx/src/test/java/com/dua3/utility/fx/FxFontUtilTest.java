package com.dua3.utility.fx;

import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontData;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FxFontUtil class.
 */
class FxFontUtilTest extends FxTestBase {

    private FxFontUtil fontUtil;
    private Font defaultFont;

    @BeforeEach
    void setUp() {
        fontUtil = FxFontUtil.getInstance();
        defaultFont = fontUtil.getDefaultFont();
    }

    @Test
    void testGetInstance() {
        assertNotNull(fontUtil);
        // Test singleton pattern
        assertSame(fontUtil, FxFontUtil.getInstance());
    }

    @Test
    void testGetDefaultFont() {
        assertNotNull(defaultFont);
    }

    @Test
    void testConvertFontToFx() {
        // Create a font definition
        FontDef fontDef = new FontDef();
        fontDef.setFamily("Arial");
        fontDef.setSize(16f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        // Create a font from the definition
        Font font = FontUtil.getInstance().getFont(fontDef);

        // Convert to JavaFX font
        javafx.scene.text.Font fxFont = fontUtil.convert(font);

        assertNotNull(fxFont);
        assertEquals(16.0, fxFont.getSize(), 0.001);
        // Note: We can't directly check bold/italic on JavaFX Font objects
    }

    @Test
    void testConvertFontFromFx() {
        // Create a JavaFX font
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Convert to utility Font
        Font font = fontUtil.convert(fxFont);

        assertNotNull(font);
        assertEquals(16f, font.getSizeInPoints(), 0.001);
        assertTrue(font.isBold());
        assertTrue(font.isItalic());
        assertTrue(font.getFamilies().contains("Arial"));
    }

    @Test
    void testGetFontData() {
        // Create a JavaFX font
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Convert to utility Font
        Font font = fontUtil.convert(fxFont);

        // Get the font data
        FontData fontData = font.getFontData();

        assertNotNull(fontData);
        assertEquals(16.0f, fontData.size(), 0.001);
        assertTrue(fontData.bold());
        assertTrue(fontData.italic());
        assertTrue(fontData.families().contains("Arial"));
    }

    @Test
    void testGetFontDef() {
        // Create a JavaFX font
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font("Arial", FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Get the font definition
        FontDef fontDef = fontUtil.getFontDef(fxFont);

        assertNotNull(fontDef);
        assertEquals(16f, fontDef.getSize(), 0.001);
        assertTrue(fontDef.getBold());
        assertTrue(fontDef.getItalic());
        assertEquals("Arial", fontDef.getFamily());
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
    void testGetTextWidth() {
        String text = "Test String";
        double width = fontUtil.getTextWidth(text, defaultFont);

        assertTrue(width > 0);
    }

    @Test
    void testGetTextHeight() {
        String text = "Test String";
        double height = fontUtil.getTextHeight(text, defaultFont);

        assertTrue(height > 0);
    }

    @Test
    void testLoadFonts() throws Exception {
        try (InputStream in = FxFontUtilTest.class.getResourceAsStream("NotoSansCJK-Regular.ttc")) {
            List<Font> fonts = fontUtil.loadFonts(in);
            assertEquals(10, fonts.size());
        }
    }

    @Test
    void testLoadFontAs() throws Exception {
        try (InputStream in = FxFontUtilTest.class.getResourceAsStream("bitstream_vera_sans/Vera.ttf")) {
            Font font = fontUtil.loadFontAs(in, defaultFont);
            assertNotNull(font);
            assertEquals(defaultFont.fontspec(), font.fontspec());
        }
    }

    @Test
    void testGetFamilies() {
        SequencedCollection<String> families = fontUtil.getFamilies();

        assertNotNull(families);
        assertFalse(families.isEmpty(), "No font families found");

        // Test getFamilies with specific types
        SequencedCollection<String> monospacedFamilies = fontUtil.getFamilies(FontUtil.FontTypes.MONOSPACED);
        SequencedCollection<String> proportionalFamilies = fontUtil.getFamilies(FontUtil.FontTypes.PROPORTIONAL);

        assertNotNull(monospacedFamilies);
        assertNotNull(proportionalFamilies);
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

    @Test
    void testGetFxFont() {
        // This is a private method, so we test it indirectly through convert
        FontDef fontDef = new FontDef();
        fontDef.setFamily("Arial");
        fontDef.setSize(16f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        Font font = FontUtil.getInstance().getFont(fontDef);
        javafx.scene.text.Font fxFont = fontUtil.convert(font);

        assertNotNull(fxFont);
        assertEquals(16.0, fxFont.getSize(), 0.001);
        assertEquals("Arial Bold Italic", fxFont.getName());
    }
}
