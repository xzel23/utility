package com.dua3.utility.test.text;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.dua3.utility.text.TextUtil;

public class TextUtilTest {
    
    @Test
    public void testTransfrom() {
        String template = "Hello ${NAME}.";
        
        String expected = "Hello Axel.";
        String actual = TextUtil.transform(template, s -> s.equals("NAME") ? "Axel" : null);
        
        assertEquals(expected, actual);
    }
    
}
