package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontDefTest {
    @Test
    void testNewInstance() {
        FontDef fontDef = new FontDef();
        assertNull(fontDef.getBold());
        assertNull(fontDef.getColor());
        assertNull(fontDef.getFamily());
        assertNull(fontDef.getItalic());
        assertNull(fontDef.getSize());
        assertNull(fontDef.getStrikeThrough());
        assert (fontDef.isEmpty());
    }

    @Test
    void testCreateWithBasicColor() {
        Color col = Color.valueOf("red");
        FontDef fontDef = FontDef.color(col);
        assertEquals(fontDef.getColor(), col);
    }

    @Test
    void testCreateWithValidColor() {
        Color expectedColor = Color.BLUE;
        FontDef fontDef = FontDef.color(expectedColor);
        assertEquals(expectedColor, fontDef.getColor());
    }

    @Test
    void testCreateWithNullColor() {
        FontDef fontDef = FontDef.color(null);
        assertNull(fontDef.getColor());
    }

    @Test
    void testCreateWithOnlyColorSet() {
        Color expectedColor = Color.GREEN;
        FontDef fontDef = FontDef.color(expectedColor);
        assertEquals(expectedColor, fontDef.getColor());
        assertNull(fontDef.getSize());
        assertNull(fontDef.getFamily());
        assertNull(fontDef.getBold());
        assertNull(fontDef.getItalic());
        assertNull(fontDef.getStrikeThrough());
        assertFalse(fontDef.isEmpty());
    }

    @Test
    void testCreateWithRedColor() {
        FontDef fontDef = FontDef.color(Color.RED);
        assertEquals(Color.RED, fontDef.getColor());
    }

    @Test
    void testCreateWithFamily() {
        String family = "Times New Roman";
        FontDef fontDef = FontDef.family(family);
        assertEquals(family, fontDef.getFamily());
    }

    @Test
    void testCreateWithSize() {
        Float size = 12.0f;
        FontDef fontDef = FontDef.size(size);
        assertEquals(size, fontDef.getSize());
    }

    @Test
    void testCreateWithBold() {
        boolean bold = true;
        FontDef fontDef = FontDef.bold(bold);
        assertEquals(bold, fontDef.getBold());
    }

    @Test
    void testParseFontspec() {
        String fontspec = "TimesNewRoman-bold-italic-12-red";
        FontDef result = FontDef.parseFontspec(fontspec);
        assertEquals("TimesNewRoman", result.getFamily());
        assertTrue(result.getBold());
        assertTrue(result.getItalic());
        assertEquals(12, result.getSize());
        assertEquals(Color.valueOf("red"), result.getColor());
    }

    @Test
    void testParseFontspecWithInvalidColor() {
        String fontspec = "TimesNewRoman-bold-italic-12-undefinedColor";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    void testParseFontspecWithInvalidSize() {
        String fontspec = "TimesNewRoman-bold-italic-undefinedSize-red";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    void testParseCssBasic() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Arial", fd.getFamily());
        assertEquals(List.of("Arial"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithQuotedFont() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: \"Times New Roman\"; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Times New Roman", fd.getFamily());
        assertEquals(List.of("Times New Roman"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithMultipleFamilies() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial, Helvetica, SansSerif; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Arial", fd.getFamily());
        assertEquals(List.of("Arial", "Helvetica", "SansSerif"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithInherit() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: inherit; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertNull(fd.getFamily());
        assertNull(fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithInheritAfterFamily() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial, inherit; font-weight: bold; font-style: italic; }";

        assertThrows(IllegalArgumentException.class, () -> FontDef.parseCssFontDef(fontdef));
    }

    @Test
    void testParseCssWithInheritBeforeFamily() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: inherit, inherit; font-weight: bold; font-style: italic; }";

        assertThrows(IllegalArgumentException.class, () -> FontDef.parseCssFontDef(fontdef));
    }

    @Test
    void testGenerateFontspec() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial, Helvetica, SansSerif");
        fd.setBold(true);
        fd.setItalic(true);

        // assuming the fontspec() method returns a font specification in the format 'family-bold/regular/italic-14.0-#FFFFFF'
        String expectedFontSpec = "Arial-bold-italic-*-*-14.0-#ffffff";
        String actualFontSpec = fd.fontspec();

        // put your appropriate assertions here
        assertEquals(expectedFontSpec, actualFontSpec);
    }

    @Test
    void testGenerateCssStyle() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial");
        fd.setBold(true);
        fd.setItalic(true);

        String expected = "font-family: Arial; font-size: 14.0pt; font-weight: bold; font-style: italic; color: #ffffff;";
        String actual = fd.getCssStyle();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("fontArguments")
    void testConvertFontToFontDef(Font font) {
        // Test with a Font set
        FontDef fd = font.toFontDef();
        assertNotNull(fd);

        assertEquals(font.getFamily(), fd.getFamily());
        assertEquals(font.getSizeInPoints(), fd.getSize());
        assertEquals(font.getColor(), fd.getColor());
        assertEquals(font.isBold(), fd.getBold());
        assertEquals(font.isItalic(), fd.getItalic());
        assertEquals(font.isUnderline(), fd.getUnderline());
        assertEquals(font.isStrikeThrough(), fd.getStrikeThrough());
    }

    private static Stream<Font> fontArguments() {
        return Stream.of(
                FontUtil.getInstance().getFont("Arial-12"),
                FontUtil.getInstance().getFont("Times-17-bold").withColor(Color.DARKBLUE),
                FontUtil.getInstance().getFont("Arial-12-underline"),
                FontUtil.getInstance().getFont("Arial-12-strikethrough"),
                FontUtil.getInstance().getFont("Arial-12-italic"),
                FontUtil.getInstance().getFont("Helvetica-10-bold-underline-strikethrough-italic").withColor(Color.WHITE)
        );
    }
}