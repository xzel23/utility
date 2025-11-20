package com.dua3.utility.fx;

import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontData;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontUtil;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
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
        String family = getDefaultFontFamily();

        // Create a font definition
        FontDef fontDef = new FontDef();
        fontDef.setFamily(family);
        fontDef.setSize(16.0f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        // Create a font from the definition
        Font font = FontUtil.getInstance().getFont(fontDef);

        // Convert to JavaFX font
        javafx.scene.text.Font fxFont = fontUtil.convert(font);

        assertNotNull(fxFont);
        assertEquals(family, fxFont.getFamily());
        assertEquals(16.0, fxFont.getSize(), 0.001);
        String styles = fxFont.getStyle().toLowerCase(Locale.ROOT);
        assertTrue(styles.contains("bold"));
        assertTrue(styles.contains("italic"));
    }

    @Test
    void testConvertFontFromFx() {
        String family = getDefaultFontFamily();

        // Create a JavaFX font
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(family, FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Convert to utility Font
        Font font = fontUtil.convert(fxFont);

        assertNotNull(font);
        assertEquals(16.0f, font.getSizeInPoints(), 0.001);
        assertTrue(font.isBold());
        assertTrue(font.isItalic());
        assertTrue(font.getFamilies().contains(family));
    }

    @Test
    void testGetFontData() {
        // Create a JavaFX font
        String family = getDefaultFontFamily();

        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(family, FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Convert to utility Font
        Font font = fontUtil.convert(fxFont);

        // Get the font data
        FontData fontData = font.getFontData();

        assertNotNull(fontData);
        assertEquals(16.0f, fontData.size(), 0.001);
        assertTrue(fontData.bold());
        assertTrue(fontData.italic());
        assertTrue(fontData.families().contains(family));
    }

    /**
     * Determines the default font family based on the detected platform.
     * For Windows and macOS platforms, the default is "Arial".
     * For all other platforms, the default is "Liberation Sans".
     *
     * @return the default font family as a non-null string
     */
    private static @NonNull String getDefaultFontFamily() {
        return switch (Platform.currentPlatform()) {
            case WINDOWS, MACOS -> "Arial";
            default -> "Liberation Sans";
        };
    }

    @Test
    void testGetFontDef() {
        String family = getDefaultFontFamily();

        // Create a JavaFX font
        javafx.scene.text.Font fxFont = javafx.scene.text.Font.font(family, FontWeight.BOLD, FontPosture.ITALIC, 16);

        // Get the font definition
        FontDef fontDef = fontUtil.getFontDef(fxFont);

        assertNotNull(fontDef);
        assertEquals(16.0f, fontDef.getSize(), 0.001);
        assertTrue(fontDef.getBold());
        assertTrue(fontDef.getItalic());
        assertEquals(family, fontDef.getFamily());
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
        String family = getDefaultFontFamily();

        // Create a font definition with different properties
        FontDef fontDef = new FontDef();
        fontDef.setFamily(family);
        fontDef.setSize(16.0f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        // Derive a new font from the default font
        Font derivedFont = fontUtil.deriveFont(defaultFont, fontDef);

        assertNotNull(derivedFont);
        assertEquals(family, derivedFont.getFamily());
        assertEquals(16.0f, derivedFont.getSizeInPoints(), 0.001);
        assertTrue(derivedFont.isBold());
        assertTrue(derivedFont.isItalic());
    }

    @Test
    void testGetFxFont() {
        String family = getDefaultFontFamily();

        // This is a private method, so we test it indirectly through convert
        FontDef fontDef = new FontDef();
        fontDef.setFamily(family);
        fontDef.setSize(16.0f);
        fontDef.setBold(true);
        fontDef.setItalic(true);

        Font font = FontUtil.getInstance().getFont(fontDef);
        javafx.scene.text.Font fxFont = fontUtil.convert(font);

        assertNotNull(fxFont);
        assertEquals(16.0, fxFont.getSize(), 0.001);
        assertEquals(family + " Bold Italic", fxFont.getName());
    }
}
