package com.dua3.utility.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * `HSVColorTest` is used to test the functionality of `HSVColor` class.
 */
class HSVColorTest {

    /**
     * This test providing a ARGB color value and validating if returned `HSVColor` instance has the correct values.
     */
    @Test
    void valueOfTest() {
        // RGB: 169, 169, 169 (grey)
        // HSV: 0, 0, 0.6627
        int argb = 0xffa9a9a9;

        HSVColor hsvColor = HSVColor.valueOf(argb);

        // Hue should be zero for gray scale color
        Assertions.assertEquals(0.0, hsvColor.h(), "Hue doesn't match with the expected value");

        // Saturation should be zero for grey scale color
        Assertions.assertEquals(0.0, hsvColor.s(), "Saturation doesn't match with the expected value");

        // Value or brightness should be ~0.66 for above RGB values
        Assertions.assertEquals(0.6627, hsvColor.v(), 0.01, "Brightness doesn't match with the expected value");

        // Alpha should be ~1 for above ARGB value where alpha component is 255
        Assertions.assertEquals(1, hsvColor.alpha(), "Alpha doesn't match with the expected value");
    }

    @Test
    void testA() {
        HSVColor color = new HSVColor(180, 0.5f, 0.8f, 0.75f);
        Assertions.assertEquals(191, color.a(), "Alpha component doesn't match expected value");
    }

    @Test
    void testIsOpaque() {
        HSVColor opaqueColor = new HSVColor(180, 0.5f, 0.8f, 1.0f);
        HSVColor semiTransparentColor = new HSVColor(180, 0.5f, 0.8f, 0.5f);

        Assertions.assertTrue(opaqueColor.isOpaque(), "Color with alpha=1.0 should be opaque");
        Assertions.assertFalse(semiTransparentColor.isOpaque(), "Color with alpha=0.5 should not be opaque");
    }

    @Test
    void testIsTransparent() {
        HSVColor transparentColor = new HSVColor(180, 0.5f, 0.8f, 0.0f);
        HSVColor semiTransparentColor = new HSVColor(180, 0.5f, 0.8f, 0.5f);

        Assertions.assertTrue(transparentColor.isTransparent(), "Color with alpha=0.0 should be transparent");
        Assertions.assertFalse(semiTransparentColor.isTransparent(), "Color with alpha=0.5 should not be transparent");
    }

    @Test
    void testToHSVColor() {
        HSVColor color = new HSVColor(180, 0.5f, 0.8f, 0.75f);
        HSVColor result = color.toHSVColor();

        Assertions.assertSame(color, result, "toHSVColor should return the same instance");
    }

    @Test
    void testArgb() {
        // Test with a known color
        HSVColor color = new HSVColor(120, 1.0f, 1.0f, 1.0f); // Pure green in HSV
        int argb = color.argb();

        // Extract components
        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = argb & 0xff;

        Assertions.assertEquals(255, a, "Alpha component doesn't match");
        Assertions.assertEquals(0, r, "Red component doesn't match");
        Assertions.assertEquals(255, g, "Green component doesn't match");
        Assertions.assertEquals(0, b, "Blue component doesn't match");
    }

    @Test
    void testBrighter() {
        HSVColor color = new HSVColor(180, 0.5f, 0.4f, 0.75f);
        Color brighterColor = color.brighter();

        Assertions.assertTrue(brighterColor instanceof HSVColor, "Brighter color should be an HSVColor");
        HSVColor brighterHSV = (HSVColor) brighterColor;

        Assertions.assertEquals(color.h(), brighterHSV.h(), "Hue should remain the same");
        Assertions.assertEquals(color.s(), brighterHSV.s(), "Saturation should remain the same");
        Assertions.assertTrue(brighterHSV.v() > color.v(), "Value should increase");
        Assertions.assertEquals(color.alpha(), brighterHSV.alpha(), "Alpha should remain the same");
    }

    @Test
    void testDarker() {
        HSVColor color = new HSVColor(180, 0.5f, 0.8f, 0.75f);
        Color darkerColor = color.darker();

        Assertions.assertTrue(darkerColor instanceof HSVColor, "Darker color should be an HSVColor");
        HSVColor darkerHSV = (HSVColor) darkerColor;

        Assertions.assertEquals(color.h(), darkerHSV.h(), "Hue should remain the same");
        Assertions.assertEquals(color.s(), darkerHSV.s(), "Saturation should remain the same");
        Assertions.assertTrue(darkerHSV.v() < color.v(), "Value should decrease");
        Assertions.assertEquals(color.alpha(), darkerHSV.alpha(), "Alpha should remain the same");
    }

    @Test
    void testToString() {
        HSVColor color = new HSVColor(180, 0.5f, 0.8f, 0.75f);
        String str = color.toString();

        Assertions.assertNotNull(str, "toString should not return null");
        Assertions.assertFalse(str.isEmpty(), "toString should not return empty string");
        Assertions.assertEquals(color.toCss(), str, "toString should return the same as toCss");
    }
}
