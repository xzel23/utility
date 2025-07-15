package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link Font}.
 */
class FontTest {

    @Test
    void testGetFont() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertNotNull(font);
        assertEquals(fontData, font.getFontData());
        assertEquals(color, font.getColor());
    }

    @Test
    void testSimilar() {
        FontData fontData1 = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData fontData2 = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font1 = Font.getFont(fontData1, color);
        Font font2 = Font.getFont(fontData2, color);

        assertTrue(Font.similar(font1, font2));
    }

    @Test
    void testDelta() {
        FontData fontData1 = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData fontData2 = FontData.get("Times New Roman", 14.0f, false, true, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color1 = Color.BLACK;
        Color color2 = Color.RED;

        Font font1 = Font.getFont(fontData1, color1);
        Font font2 = Font.getFont(fontData2, color2);

        FontDef delta = Font.delta(font1, font2);

        assertNotNull(delta);
        assertEquals("Times New Roman", delta.getFamily());
        assertEquals(14.0f, delta.getSize());
        assertTrue(delta.getBold());
        assertEquals(color2, delta.getColor());
    }

    @Test
    void testGetColor() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLUE;

        Font font = Font.getFont(fontData, color);

        assertEquals(color, font.getColor());
    }

    @Test
    void testGetFamily() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals("Arial", font.getFamily());
    }

    @Test
    void testGetFamilies() {
        FontData fontData = FontData.get(List.of("Arial", "Helvetica", "sans-serif"), 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(List.of("Arial", "Helvetica", "sans-serif"), font.getFamilies());
    }

    @Test
    void testGetType() {
        FontData monospacedFontData = FontData.get("Courier New", 12.0f, true, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData proportionalFontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font monospacedFont = Font.getFont(monospacedFontData, color);
        Font proportionalFont = Font.getFont(proportionalFontData, color);

        assertEquals(FontType.MONOSPACED, monospacedFont.getType());
        assertEquals(FontType.PROPORTIONAL, proportionalFont.getType());
    }

    @Test
    void testGetSizeInPoints() {
        FontData fontData = FontData.get("Arial", 12.5f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(12.5f, font.getSizeInPoints());
    }

    @Test
    void testIsBold() {
        FontData boldFontData = FontData.get("Arial", 12.0f, false, true, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData regularFontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font boldFont = Font.getFont(boldFontData, color);
        Font regularFont = Font.getFont(regularFontData, color);

        assertTrue(boldFont.isBold());
        assertFalse(regularFont.isBold());
    }

    @Test
    void testIsItalic() {
        FontData italicFontData = FontData.get("Arial", 12.0f, false, false, true, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData regularFontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font italicFont = Font.getFont(italicFontData, color);
        Font regularFont = Font.getFont(regularFontData, color);

        assertTrue(italicFont.isItalic());
        assertFalse(regularFont.isItalic());
    }

    @Test
    void testIsStrikeThrough() {
        FontData strikeThroughFontData = FontData.get("Arial", 12.0f, false, false, false, false, true, 10.0, 2.0, 12.0, 5.0);
        FontData regularFontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font strikeThroughFont = Font.getFont(strikeThroughFontData, color);
        Font regularFont = Font.getFont(regularFontData, color);

        assertTrue(strikeThroughFont.isStrikeThrough());
        assertFalse(regularFont.isStrikeThrough());
    }

    @Test
    void testIsUnderline() {
        FontData underlineFontData = FontData.get("Arial", 12.0f, false, false, false, true, false, 10.0, 2.0, 12.0, 5.0);
        FontData regularFontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font underlineFont = Font.getFont(underlineFontData, color);
        Font regularFont = Font.getFont(regularFontData, color);

        assertTrue(underlineFont.isUnderline());
        assertFalse(regularFont.isUnderline());
    }

    @Test
    void testToString() {
        FontData fontData = FontData.get("Arial", 12.0f, false, true, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.RED;

        Font font = Font.getFont(fontData, color);

        assertEquals(font.fontspec(), font.toString());
    }

    @Test
    void testEquals() {
        FontData fontData1 = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData fontData2 = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        FontData fontData3 = FontData.get("Times New Roman", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color1 = Color.BLACK;
        Color color2 = Color.RED;

        Font font1 = Font.getFont(fontData1, color1);
        Font font2 = Font.getFont(fontData2, color1);
        Font font3 = Font.getFont(fontData3, color1);
        Font font4 = Font.getFont(fontData1, color2);

        assertEquals(font1, font2);
        assertNotEquals(font1, font3);
        assertNotEquals(font1, font4);
        assertNotEquals(null, font1);
        assertNotEquals("Not a Font", font1);
    }

    @Test
    void testHashCode() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font1 = Font.getFont(fontData, color);
        Font font2 = Font.getFont(fontData, color);

        assertEquals(font1.hashCode(), font2.hashCode());
    }

    @Test
    void testGetCssStyle() {
        FontData fontData = FontData.get("Arial", 12.0f, false, true, true, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.RED;

        Font font = Font.getFont(fontData, color);

        String cssStyle = font.getCssStyle();
        assertTrue(cssStyle.contains("font-family: Arial"));
        assertTrue(cssStyle.contains("font-size: 12.0pt"));
        assertTrue(cssStyle.contains("font-weight: bold"));
        assertTrue(cssStyle.contains("font-style: italic"));
        assertTrue(cssStyle.contains("color: #ff0000"));
    }

    @Test
    void testFontspec() {
        FontData fontData = FontData.get("Arial", 12.0f, false, true, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.RED;

        Font font = Font.getFont(fontData, color);

        String fontspec = font.fontspec();
        assertTrue(fontspec.contains("Arial"));
        assertTrue(fontspec.contains("12.0"));
        assertTrue(fontspec.contains("#ff0000"));
    }

    @Test
    void testToFontDef() {
        FontData fontData = FontData.get("Arial", 12.0f, false, true, true, true, true, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLUE;

        Font font = Font.getFont(fontData, color);
        FontDef fontDef = font.toFontDef();

        assertEquals("Arial", fontDef.getFamily());
        assertEquals(12.0f, fontDef.getSize());
        assertTrue(fontDef.getBold());
        assertTrue(fontDef.getItalic());
        assertTrue(fontDef.getStrikeThrough());
        assertTrue(fontDef.getUnderline());
        assertEquals(color, fontDef.getColor());
    }

    @Test
    void testGetAscent() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.5, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(10.5, font.getAscent());
    }

    @Test
    void testGetDescent() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 3.5, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(3.5, font.getDescent());
    }

    @Test
    void testGetHeight() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 14.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(14.0, font.getHeight());
    }

    @Test
    void testGetSpaceWidth() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 4.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(4.0, font.getSpaceWidth());
    }

    @Test
    void testWithSize() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font resizedFont = font.withSize(14.0f);

        assertEquals(14.0f, resizedFont.getSizeInPoints());
        assertEquals(font.getFamily(), resizedFont.getFamily());
        assertEquals(font.getColor(), resizedFont.getColor());
    }

    @Test
    void testScaled() {
        FontData fontData = FontData.get("Arial", 10.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font scaledFont = font.scaled(1.5f);

        assertEquals(15.0f, scaledFont.getSizeInPoints());
        assertEquals(font.getFamily(), scaledFont.getFamily());
        assertEquals(font.getColor(), scaledFont.getColor());
    }

    @Test
    void testWithBold() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font boldFont = font.withBold(true);

        assertTrue(boldFont.isBold());
        assertEquals(font.getFamily(), boldFont.getFamily());
        assertEquals(font.getSizeInPoints(), boldFont.getSizeInPoints());
        assertEquals(font.getColor(), boldFont.getColor());
    }

    @Test
    void testWithItalic() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font italicFont = font.withItalic(true);

        assertTrue(italicFont.isItalic());
        assertEquals(font.getFamily(), italicFont.getFamily());
        assertEquals(font.getSizeInPoints(), italicFont.getSizeInPoints());
        assertEquals(font.getColor(), italicFont.getColor());
    }

    @Test
    void testWithUnderline() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font underlineFont = font.withUnderline(true);

        assertTrue(underlineFont.isUnderline());
        assertEquals(font.getFamily(), underlineFont.getFamily());
        assertEquals(font.getSizeInPoints(), underlineFont.getSizeInPoints());
        assertEquals(font.getColor(), underlineFont.getColor());
    }

    @Test
    void testWithStrikeThrough() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font strikeThroughFont = font.withStrikeThrough(true);

        assertTrue(strikeThroughFont.isStrikeThrough());
        assertEquals(font.getFamily(), strikeThroughFont.getFamily());
        assertEquals(font.getSizeInPoints(), strikeThroughFont.getSizeInPoints());
        assertEquals(font.getColor(), strikeThroughFont.getColor());
    }

    @Test
    void testWithFamily() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font newFamilyFont = font.withFamily("Times New Roman");

        assertEquals("Times New Roman", newFamilyFont.getFamily());
        assertEquals(font.getSizeInPoints(), newFamilyFont.getSizeInPoints());
        assertEquals(font.getColor(), newFamilyFont.getColor());
    }

    @Test
    void testWithColor() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);
        Font coloredFont = font.withColor(Color.RED);

        assertEquals(Color.RED, coloredFont.getColor());
        assertEquals(font.getFamily(), coloredFont.getFamily());
        assertEquals(font.getSizeInPoints(), coloredFont.getSizeInPoints());
    }

    @Test
    void testGetFontData() {
        FontData fontData = FontData.get("Arial", 12.0f, false, false, false, false, false, 10.0, 2.0, 12.0, 5.0);
        Color color = Color.BLACK;

        Font font = Font.getFont(fontData, color);

        assertEquals(fontData, font.getFontData());
    }
}
