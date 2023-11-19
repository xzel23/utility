package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontDefTest {
    @Test
    void testConstructor() {
        FontDef fontDef = new FontDef();
        assertNull(fontDef.getBold());
        assertNull(fontDef.getColor());
        assertNull(fontDef.getFamily());
        assertNull(fontDef.getItalic());
        assertNull(fontDef.getSize());
        assertNull(fontDef.getStrikeThrough());
        assert(fontDef.isEmpty());
    }

    @Test
    void testColor() {
        Color col = Color.valueOf("red");
        FontDef fontDef = FontDef.color(col);
        assertEquals(fontDef.getColor(), col);
    }

    @Test
    void testFamily() {
        String family = "Times New Roman";
        FontDef fontDef = FontDef.family(family);
        assertEquals(fontDef.getFamily(), family);
    }

    @Test
    void testSize() {
        Float size = 12.0f;
        FontDef fontDef = FontDef.size(size);
        assertEquals(fontDef.getSize(), size);
    }

    @Test
    void testBold() {
        boolean bold = true;
        FontDef fontDef = FontDef.bold(bold);
        assertEquals(bold, fontDef.getBold());
    }

    @Test
    void parseFontspec() {
        String fontspec = "TimesNewRoman-bold-italic-12-red";
        FontDef result = FontDef.parseFontspec(fontspec);
        Assertions.assertEquals("TimesNewRoman", result.getFamily());
        assertTrue(result.getBold());
        assertTrue(result.getItalic());
        Assertions.assertEquals(12, result.getSize());
        Assertions.assertEquals(Color.valueOf("red"), result.getColor());
    }

    @Test
    void parseFontspecThrowsIllegalArgumentExceptionWhenColorInvalid() {
        String fontspec = "TimesNewRoman-bold-italic-12-undefinedColor";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    void parseFontspecThrowsIllegalArgumentExceptionWhenSizeInvalid() {
        String fontspec = "TimesNewRoman-bold-italic-undefinedSize-red";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    public void testParseCssFontDef() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(fd.getSize(), 10.5f); // 14px = 10.5pt
        assertEquals(fd.getColor(), Color.WHITE);
        assertEquals(fd.getFamily(), "Arial");
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    public void testFontspec() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial");
        fd.setBold(true);
        fd.setItalic(true);

        // assuming the fontspec() method returns a font specification in the format 'family-bold/regular/italic-14.0-#FFFFFF'
        String expectedFontSpec = "Arial-bold-italic-*-*-14.0-#ffffff";
        String actualFontSpec = fd.fontspec();

        // put your appropriate assertions here
        assertEquals(expectedFontSpec, actualFontSpec);
    }

    @Test
    void getCssStyle() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial");
        fd.setBold(true);
        fd.setItalic(true);

        String expected = "color: #ffffff; font-size: 14.0pt; font-family: Arial; font-weight: bold; font-style: italic;";
        String actual = fd.getCssStyle();

        assertEquals(expected, actual);
    }
}