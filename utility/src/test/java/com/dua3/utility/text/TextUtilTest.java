// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextUtilTest {

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final List<Pair<String, byte[]>> byteArrayHexStringTestData = List.of(
            Pair.of("00", new byte[]{0x00}),
            Pair.of("a0cafe", new byte[]{(byte) 0xa0, (byte) 0xca, (byte) 0xfe}));

    @Test
    public void testTransform() {
        String template = "Hello ${NAME}.";

        String expected = "Hello Axel.";
        String actual = TextUtil.transform(template, s -> s.equals("NAME") ? "Axel" : null);

        assertEquals(expected, actual);
    }

    @Test
    public void testAlign() {
        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.LEFT));
        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.CENTER));
        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.RIGHT));

        assertEquals("   ", TextUtil.align("", 3, TextUtil.Alignment.LEFT));
        assertEquals("   ", TextUtil.align("", 3, TextUtil.Alignment.CENTER));
        assertEquals("   ", TextUtil.align("", 3, TextUtil.Alignment.RIGHT));

        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.LEFT));
        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.CENTER));
        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.RIGHT));

        assertEquals("abc  ", TextUtil.align("abc", 5, TextUtil.Alignment.LEFT));
        assertEquals(" abc ", TextUtil.align("abc", 5, TextUtil.Alignment.CENTER));
        assertEquals("  abc", TextUtil.align("abc", 5, TextUtil.Alignment.RIGHT));

        assertEquals("abcd ", TextUtil.align("abcd", 5, TextUtil.Alignment.LEFT));
        assertEquals("abcd ", TextUtil.align("abcd", 5, TextUtil.Alignment.CENTER));
        assertEquals(" abcd", TextUtil.align("abcd", 5, TextUtil.Alignment.RIGHT));

        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.LEFT, '_'));
        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.CENTER, '_'));
        assertEquals("", TextUtil.align("", 0, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("___", TextUtil.align("", 3, TextUtil.Alignment.LEFT, '_'));
        assertEquals("___", TextUtil.align("", 3, TextUtil.Alignment.CENTER, '_'));
        assertEquals("___", TextUtil.align("", 3, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.LEFT, '_'));
        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.CENTER, '_'));
        assertEquals("abc", TextUtil.align("abc", 1, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abc__", TextUtil.align("abc", 5, TextUtil.Alignment.LEFT, '_'));
        assertEquals("_abc_", TextUtil.align("abc", 5, TextUtil.Alignment.CENTER, '_'));
        assertEquals("__abc", TextUtil.align("abc", 5, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abcd_", TextUtil.align("abcd", 5, TextUtil.Alignment.LEFT, '_'));
        assertEquals("abcd_", TextUtil.align("abcd", 5, TextUtil.Alignment.CENTER, '_'));
        assertEquals("_abcd", TextUtil.align("abcd", 5, TextUtil.Alignment.RIGHT, '_'));
    }

    @Test
    void testEscapeHTML() {
        String normalString = "<div>Test Content</div>";
        String escapedString = TextUtil.escapeHTML(normalString);
        assertEquals("&lt;div&gt;Test Content&lt;/div&gt;", escapedString);

        String stringWithAmpersand = "Tom & Jerry";
        escapedString = TextUtil.escapeHTML(stringWithAmpersand);
        assertEquals("Tom &amp; Jerry", escapedString);

        String specialCharactersString = "< > & \" ' /";
        escapedString = TextUtil.escapeHTML(specialCharactersString);
        assertEquals("&lt; &gt; &amp; &quot; &apos; /", escapedString);
    }

    @Test
    void testDecodeFontSize() {
        // Test with "pt"
        Assertions.assertEquals(10.0f, TextUtil.decodeFontSize("10pt"), 0.001);

        // Test with "em"
        Assertions.assertEquals(120.0f, TextUtil.decodeFontSize("10em"), 0.001);

        // Test with "px"
        Assertions.assertEquals(7.5f, TextUtil.decodeFontSize("10px"), 0.001);

        // Test with "%"
        Assertions.assertEquals(1.2f, TextUtil.decodeFontSize("10%"), 0.001);

        // Test with unknown unit
        Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtil.decodeFontSize("10abc"));

        // Test with "vw"
        Assertions.assertEquals(120.0f, TextUtil.decodeFontSize("10vw"), 0.001);

        // Test with empty string
        Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtil.decodeFontSize(""));
    }
}
