// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dua3.utility.data.Pair;

public class TextUtilTest {

    @Test
    public void testTransform() {
        String template = "Hello ${NAME}.";

        String expected = "Hello Axel.";
        String actual = TextUtil.transform(template, s -> s.equals("NAME") ? "Axel" : null);

        assertEquals(expected, actual);
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final List<Pair<String, byte[]>> byteArrayHexStringTestData = Arrays.asList(
            Pair.of("00", new byte[] { 0x00 }),
            Pair.of("a0cafe", new byte[] { (byte) 0xa0, (byte) 0xca, (byte) 0xfe }));

    @Test
    public void testByteArrayToHexString() {
        for (Pair<String, byte[]> entry : byteArrayHexStringTestData) {
            assertEquals(entry.first(), TextUtil.byteArrayToHexString(entry.second()));
        }
    }

    @Test
    public void testHexStringToByteArray() {
        for (Pair<String, byte[]> entry : byteArrayHexStringTestData) {
            assertArrayEquals(entry.second(), TextUtil.hexStringToByteArray(entry.first()));
        }
    }

    @Test
    public void testAlign() {
        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.LEFT));
        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.CENTER));
        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.RIGHT));

        assertEquals("   ", TextUtil.align("",3, TextUtil.Alignment.LEFT));
        assertEquals("   ", TextUtil.align("",3, TextUtil.Alignment.CENTER));
        assertEquals("   ", TextUtil.align("",3, TextUtil.Alignment.RIGHT));

        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.LEFT));
        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.CENTER));
        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.RIGHT));

        assertEquals("abc  ", TextUtil.align("abc",5, TextUtil.Alignment.LEFT));
        assertEquals(" abc ", TextUtil.align("abc",5, TextUtil.Alignment.CENTER));
        assertEquals("  abc", TextUtil.align("abc",5, TextUtil.Alignment.RIGHT));

        assertEquals("abcd ", TextUtil.align("abcd",5, TextUtil.Alignment.LEFT));
        assertEquals("abcd ", TextUtil.align("abcd",5, TextUtil.Alignment.CENTER));
        assertEquals(" abcd", TextUtil.align("abcd",5, TextUtil.Alignment.RIGHT));

        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.LEFT, '_'));
        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.CENTER, '_'));
        assertEquals("", TextUtil.align("",0, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("___", TextUtil.align("",3, TextUtil.Alignment.LEFT, '_'));
        assertEquals("___", TextUtil.align("",3, TextUtil.Alignment.CENTER, '_'));
        assertEquals("___", TextUtil.align("",3, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.LEFT, '_'));
        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.CENTER, '_'));
        assertEquals("abc", TextUtil.align("abc",1, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abc__", TextUtil.align("abc",5, TextUtil.Alignment.LEFT, '_'));
        assertEquals("_abc_", TextUtil.align("abc",5, TextUtil.Alignment.CENTER, '_'));
        assertEquals("__abc", TextUtil.align("abc",5, TextUtil.Alignment.RIGHT, '_'));

        assertEquals("abcd_", TextUtil.align("abcd",5, TextUtil.Alignment.LEFT, '_'));
        assertEquals("abcd_", TextUtil.align("abcd",5, TextUtil.Alignment.CENTER, '_'));
        assertEquals("_abcd", TextUtil.align("abcd",5, TextUtil.Alignment.RIGHT, '_'));
    }
    
    @Test
    public void testIndent() {
        assertEquals("", TextUtil.indent("",0));
        assertEquals("", TextUtil.indent("",1));
        assertEquals("", TextUtil.indent("",2));

        assertEquals("test", TextUtil.indent("test",0));
        assertEquals(" test", TextUtil.indent("test",1));
        assertEquals("  test", TextUtil.indent("test",2));

        assertEquals("test\ntest", TextUtil.indent("test\ntest",0));
        assertEquals(" test\n test", TextUtil.indent("test\ntest",1));
        assertEquals("  test\n  test", TextUtil.indent("test\ntest",2));

        assertEquals("test\r\ntest", TextUtil.indent("test\r\ntest",0));
        assertEquals(" test\r\n test", TextUtil.indent("test\r\ntest",1));
        assertEquals("  test\r\n  test", TextUtil.indent("test\r\ntest",2));

        assertEquals("test\r\ntest\ntest\r\n test", TextUtil.indent("test\r\ntest\ntest\r\n test",0));
        assertEquals(" test\r\n test\n test\r\n  test", TextUtil.indent("test\r\ntest\ntest\r\n test",1));
        assertEquals("  test\r\n  test\n  test\r\n   test", TextUtil.indent("test\r\ntest\ntest\r\n test",2));
    }
}
