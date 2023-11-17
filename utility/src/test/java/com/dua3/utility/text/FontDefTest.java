package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        Assertions.assertTrue(result.getBold());
        Assertions.assertTrue(result.getItalic());
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
}