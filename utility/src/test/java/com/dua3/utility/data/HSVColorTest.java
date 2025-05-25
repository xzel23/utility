package com.dua3.utility.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * `HSVColorTest` is used to test the functionality of `HSVColor` class.
 * Specifically, this test focuses on the `valueOf` method, which converts an ARGB integer to `HSVColor`.
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
}