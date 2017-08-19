package com.dua3.utility;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ColorTest {
    
    @Test
    public void testToStringAndValueOfWithHex() {
        for (Color c : Color.values()) {
            String hex = c.toString();
            Assert.assertTrue(hex.matches("#[a-f0-9]{8}"));
            
            Color d = Color.valueOf(hex);
            Assert.assertEquals(c, d);
        }
    }
    
    @Test
    public void testValueOfWithName() {
        for (Map.Entry<String, Color> entry : Color.palette().entrySet()) {
            String name = entry.getKey();
            Color expected = entry.getValue();
            Color colorByName = Color.valueOf(name);
            
            Assert.assertEquals(expected, colorByName);
        }
    }
    
    @Test
    public void testValueOfWithRgb() {
        for (Color c : Color.values()) {
            Color expected = c;
            
            int r = c.r();
            int g = c.g();
            int b = c.b();
            String text = String.format("rgb(%d,%d,%d)", r, g, b);
            Color actual = Color.valueOf(text);
            
            Assert.assertEquals(expected, actual);
        }
    }
    
    @Test
    public void testValueOfWithRgba() {
        for (Color c : Color.values()) {
            Color expected = c;
            
            int r = c.r();
            int g = c.g();
            int b = c.b();
            int a = c.a();
            String text = String.format("rgba(%d,%d,%d,%d)", r, g, b, a);
            Color actual = Color.valueOf(text);
            
            Assert.assertEquals(expected, actual);
        }
    }
    
}
