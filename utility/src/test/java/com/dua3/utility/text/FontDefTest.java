// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.dua3.utility.text.FontDef.parseFontSize;

public class FontDefTest {
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
        Assertions.assertThrows(NumberFormatException.class, () -> parseFontSize("abcpt"), "Invalid number should throw an exception");
        Assertions.assertThrows(NumberFormatException.class, () -> parseFontSize("abc%"), "Invalid number should throw an exception");
        Assertions.assertThrows(NumberFormatException.class, () -> parseFontSize("abcem"), "Invalid number should throw an exception");
        Assertions.assertThrows(NumberFormatException.class, () -> parseFontSize("abcpx"), "Invalid number should throw an exception");
        Assertions.assertThrows(NumberFormatException.class, () -> parseFontSize("abcvw"), "Invalid number should throw an exception");
    }

}
