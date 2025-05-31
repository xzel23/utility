package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link RGBColor}.
 */
class RGBColorTest {

    @Test
    void testConstructorWithRGB() {
        // Create a color with RGB values
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test RGB components
        assertEquals(100, color.r());
        assertEquals(150, color.g());
        assertEquals(200, color.b());
        assertEquals(255, color.a()); // Default alpha should be 255
    }

    @Test
    void testConstructorWithRGBA() {
        // Create a color with RGBA values
        RGBColor color = new RGBColor(100, 150, 200, 128);
        
        // Test RGBA components
        assertEquals(100, color.r());
        assertEquals(150, color.g());
        assertEquals(200, color.b());
        assertEquals(128, color.a());
    }

    @Test
    void testValueOf() {
        // Create a color using valueOf
        int argb = 0x80C0FF00; // ARGB: 128, 192, 255, 0
        RGBColor color = RGBColor.valueOf(argb);
        
        // Test RGBA components
        assertEquals(192, color.r());
        assertEquals(255, color.g());
        assertEquals(0, color.b());
        assertEquals(128, color.a());
    }

    @Test
    void testArgbMethod() {
        // Create a color
        RGBColor color = new RGBColor(192, 255, 0, 128);
        
        // Test argb method
        int argb = color.argb();
        assertEquals(0x80C0FF00, argb);
    }

    @Test
    void testRgbMethod() {
        // Create a color using rgb method
        int rgb = 0x00C0FF00; // RGB: 192, 255, 0
        RGBColor color = RGBColor.rgb(rgb);
        
        // Test RGB components
        assertEquals(192, color.r());
        assertEquals(255, color.g());
        assertEquals(0, color.b());
        assertEquals(255, color.a()); // Alpha should be 255
    }

    @Test
    void testAlpha() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200, 128);
        
        // Test alpha method
        assertEquals(128 / 255.0f, color.alpha(), 0.001f);
    }

    @Test
    void testAf() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200, 128);
        
        // Test af method
        assertEquals(128 / 255.0f, color.af(), 0.001f);
    }

    @Test
    void testBf() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test bf method
        assertEquals(200 / 255.0f, color.bf(), 0.001f);
    }

    @Test
    void testGf() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test gf method
        assertEquals(150 / 255.0f, color.gf(), 0.001f);
    }

    @Test
    void testRf() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test rf method
        assertEquals(100 / 255.0f, color.rf(), 0.001f);
    }

    @Test
    void testBrighter() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Get brighter color
        Color brighterColor = color.brighter();
        
        // Test that it's an RGBColor
        assertTrue(brighterColor instanceof RGBColor);
        RGBColor brighterRGB = (RGBColor) brighterColor;
        
        // Test that components are brighter
        assertTrue(brighterRGB.r() > color.r());
        assertTrue(brighterRGB.g() > color.g());
        assertTrue(brighterRGB.b() > color.b());
        assertEquals(color.a(), brighterRGB.a()); // Alpha should remain the same
    }

    @Test
    void testDarker() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Get darker color
        Color darkerColor = color.darker();
        
        // Test that it's an RGBColor
        assertTrue(darkerColor instanceof RGBColor);
        RGBColor darkerRGB = (RGBColor) darkerColor;
        
        // Test that components are darker
        assertTrue(darkerRGB.r() < color.r());
        assertTrue(darkerRGB.g() < color.g());
        assertTrue(darkerRGB.b() < color.b());
        assertEquals(color.a(), darkerRGB.a()); // Alpha should remain the same
    }

    @Test
    void testEquals() {
        // Create two identical colors
        RGBColor color1 = new RGBColor(100, 150, 200, 128);
        RGBColor color2 = new RGBColor(100, 150, 200, 128);
        
        // Create a different color
        RGBColor color3 = new RGBColor(200, 150, 100, 128);
        
        // Test equals
        assertEquals(color1, color2);
        assertNotEquals(color1, color3);
        assertNotEquals("not a color", color1);
    }

    @Test
    void testIsOpaque() {
        // Create opaque and non-opaque colors
        RGBColor opaqueColor = new RGBColor(100, 150, 200, 255);
        RGBColor semiTransparentColor = new RGBColor(100, 150, 200, 128);
        
        // Test isOpaque
        assertTrue(opaqueColor.isOpaque());
        assertFalse(semiTransparentColor.isOpaque());
    }

    @Test
    void testIsTransparent() {
        // Create transparent and non-transparent colors
        RGBColor transparentColor = new RGBColor(100, 150, 200, 0);
        RGBColor semiTransparentColor = new RGBColor(100, 150, 200, 128);
        
        // Test isTransparent
        assertTrue(transparentColor.isTransparent());
        assertFalse(semiTransparentColor.isTransparent());
    }

    @Test
    void testToRGBColor() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test toRGBColor
        RGBColor result = color.toRGBColor();
        assertSame(color, result);
    }

    @Test
    void testHashCode() {
        // Create two identical colors
        RGBColor color1 = new RGBColor(100, 150, 200, 128);
        RGBColor color2 = new RGBColor(100, 150, 200, 128);
        
        // Create a different color
        RGBColor color3 = new RGBColor(200, 150, 100, 128);
        
        // Test hashCode
        assertEquals(color1.hashCode(), color2.hashCode());
        assertNotEquals(color1.hashCode(), color3.hashCode());
    }

    @Test
    void testToString() {
        // Create a color
        RGBColor color = new RGBColor(100, 150, 200);
        
        // Test toString
        String str = color.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
        assertEquals(color.toCss(), str);
    }
}