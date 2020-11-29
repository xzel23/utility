package com.dua3.utility.text;

import static org.junit.jupiter.api.Assertions.*;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class StyleTest {
    
    @Test
    public void testEquals() {
        Style s1 = Style.create("style", "class", 
                Pair.of(TextAttributes.FONT_FAMILY, "Arial"),
                Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));
        
        String s2Name = new StringBuilder("st").append("yle").toString();
        String s2Class = new StringBuilder("cla").append("ss").toString();
        Style s2 = Style.create(s2Name, s2Class,
                Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD),
                Pair.of(TextAttributes.FONT_FAMILY, "Arial"));
        
        // first make sure the names are equal but not identical 
        assertTrue(s1.name().equals(s2.name()));
        assertFalse(s1.name()==s2.name());

        // s1 and s2 do not possess any properties
        assertEquals(s1, s2);

    }
    
}
