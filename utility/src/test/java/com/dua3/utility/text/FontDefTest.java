// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.dua3.utility.text.FontDef.parseColor;
import static com.dua3.utility.text.FontDef.parseFontSize;
import static com.dua3.utility.text.FontDef.parseFontStyle;
import static com.dua3.utility.text.FontDef.parseFontWeight;

public class FontDefTest {
    @Test
    public void testParseColor() {
        Assertions.assertNull(parseColor("inherit"), "inherit should return null");
        Assertions.assertEquals(Color.RED, parseColor("red"), "red should return Color.RED");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseColor("invalid"), "Invalid color string should throw an exception");
    }

    @Test
    public void testParseFontWeight() {
        Assertions.assertNull(parseFontWeight("inherit"), "inherit should return null");
        Assertions.assertEquals(Boolean.TRUE, parseFontWeight("bold"), "bold should return Boolean.TRUE");
        Assertions.assertEquals(Boolean.FALSE, parseFontWeight("normal"), "normal should return Boolean.FALSE");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontWeight("invalid"), "Invalid font weight string should throw an exception");
    }

    @Test
    public void testParseFontStyle() {
        Assertions.assertNull(parseFontStyle("inherit"), "inherit should return null");
        Assertions.assertEquals(Boolean.TRUE, parseFontStyle("italic"), "italic should return Boolean.TRUE");
        Assertions.assertEquals(Boolean.TRUE, parseFontStyle("oblique"), "oblique should return Boolean.TRUE");
        Assertions.assertEquals(Boolean.FALSE, parseFontStyle("normal"), "normal should return Boolean.FALSE");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontStyle("invalid"), "Invalid font style string should throw an exception");
    }

    @Test
    public void testParseFontSize() {
        Assertions.assertNull(parseFontSize("inherit"), "inherit should return null");
        Assertions.assertEquals(10.0f, parseFontSize("10pt"), "10pt should return 10.0");
        Assertions.assertEquals(24.0f, parseFontSize("2em"), "2em should return 24.0");
        Assertions.assertEquals(0.75f, parseFontSize("1px"), "1px should return 0.75");
        Assertions.assertEquals(1.2f, parseFontSize("10%"), 1E-6, "10% should return 1.2");
        Assertions.assertEquals(24.0f, parseFontSize("2vw"), "2vw should return 24.0");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("10abc"), "Invalid unit should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize(""), "Empty string should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("abcpt"), "Invalid number should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("abc%"), "Invalid number should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("abcem"), "Invalid number should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("abcpx"), "Invalid number should throw an exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> parseFontSize("abcvw"), "Invalid number should throw an exception");
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
