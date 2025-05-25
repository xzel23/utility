package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class StyleTest {

    @SuppressWarnings("StringBufferReplaceableByString")
    @Test
    void testEquals() {
        Style s1 = Style.create("style",
                Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE),
                Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));

        String s2Name = new StringBuilder("st").append("yle").toString();
        Style s2 = Style.create(s2Name,
                Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD),
                Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE));

        // first make sure the names are equal but not identical 
        assertEquals(s2.name(), s1.name());
        assertNotSame(s1.name(), s2.name());

        // s1 and s2 do not possess any properties
        assertEquals(s1, s2);
    }

}
