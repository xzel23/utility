package com.dua3.utility.test.text;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import com.dua3.utility.Pair;
import com.dua3.utility.text.TextUtil;

public class TextUtilTest {
    
    @Test
    public void testTransfrom() {
        String template = "Hello ${NAME}.";
        
        String expected = "Hello Axel.";
        String actual = TextUtil.transform(template, s -> s.equals("NAME") ? "Axel" : null);
        
        assertEquals(expected, actual);
    }

    private static final List<Pair<String, byte[]>> byteArrayHexStringTestData = List.of(
			Pair.of("00", new byte[]{0x00}),
			Pair.of("a0cafe", new byte[]{(byte)0xa0, (byte)0xca, (byte)0xfe})
        );
        
    @Test
    public void testByteArrayToHexString() {
		for (var entry: byteArrayHexStringTestData) {
			assertEquals(entry.first, TextUtil.byteArrayToHexString(entry.second));
		}
    }

    @Test
    public void testHexStringToByteArray() {
		for (var entry: byteArrayHexStringTestData) {
			assertArrayEquals(entry.second, TextUtil.hexStringToByteArray(entry.first));
		}
    }

}
