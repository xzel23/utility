// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

/**
 * Color in ARGB format.
 */
public final class RGBColor implements Color {

    static final int SHIFT_A = 24;
    static final int SHIFT_B = 0;
    static final int SHIFT_G = 8;
    static final int SHIFT_R = 16;
    /**
     * This color's ARGB value.
     */
    private final int argb;

    /**
     * Create a new Color.
     *
     * @param r red (0..255)
     * @param g green (0..255)
     * @param b blue (0..255)
     */
    public RGBColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Create a new Color.
     *
     * @param r red (0..255)
     * @param g green (0..255)
     * @param b blue (0..255)
     * @param a alpha (0..255)
     */
    public RGBColor(int r, int g, int b, int a) {
        argb = shiftComponentValue(a, SHIFT_A) + shiftComponentValue(r, SHIFT_R) + shiftComponentValue(g, SHIFT_G)
                + shiftComponentValue(b, SHIFT_B);
    }

    /**
     * Constructor.
     *
     * @param argb the ARGB value
     */
    RGBColor(int argb) {
        this.argb = argb;
    }

    private static int shiftComponentValue(int value, int bits) {
        LangUtil.check(value >= 0 && value <= 255, () -> new IllegalArgumentException("value out of range: " + value));
        return value << bits;
    }

    /**
     * Create color from ARGB value.
     *
     * @param argb the ARGB value
     * @return the color instance
     */
    public static RGBColor valueOf(int argb) {
        return argb(argb);
    }

    /**
     * Create color from ARGB value.
     *
     * @param argb the ARGB value
     * @return the color instance
     */
    public static RGBColor argb(int argb) {
        return new RGBColor(argb);
    }

    /**
     * Create color from RGB value.
     *
     * @param rgb the RGB value (the highest 16 bits are ignored)
     * @return the color instance
     */
    public static RGBColor rgb(int rgb) {
        return new RGBColor(0xff000000 | rgb);
    }

    @Override
    public float alpha() {
        return a() / 255.0f;
    }

    @Override
    public int a() {
        return (argb >> SHIFT_A) & 0xff;
    }

    /**
     * Get alpha component of color.
     *
     * @return alpha component as float (between 0.0 and 1.0)
     */
    public float af() {
        return a() / 255.0f;
    }

    @Override
    public int argb() {
        return argb;
    }

    /**
     * Get blue component of color.
     *
     * @return blue component
     */
    public int b() {
        return (argb >> SHIFT_B) & 0xff;
    }

    /**
     * Get blue component of color.
     *
     * @return blue component as float (between 0.0 and 1.0)
     */
    public float bf() {
        return b() / 255.0f;
    }

    @Override
    public RGBColor brighter() {
        int r = r();
        int g = g();
        int b = b();
        int alpha = a();

        int i = (int) (1.0 / (1.0 - F_BRIGHTEN));
        if (r == 0 && g == 0 && b == 0) {
            return new RGBColor(i, i, i, alpha);
        }

        if (r > 0 && r < i) {
            r = i;
        }
        if (g > 0 && g < i) {
            g = i;
        }
        if (b > 0 && b < i) {
            b = i;
        }

        //noinspection NumericCastThatLosesPrecision
        return new RGBColor(Math.min((int) (r / F_BRIGHTEN), 255), Math.min((int) (g / F_BRIGHTEN), 255),
                Math.min((int) (b / F_BRIGHTEN), 255), alpha);
    }

    @Override
    public RGBColor darker() {
        //noinspection NumericCastThatLosesPrecision
        return new RGBColor((int) (r() * F_BRIGHTEN), (int) (g() * F_BRIGHTEN), (int) (b() * F_BRIGHTEN), a());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof RGBColor other)) {
            return false;
        }
        return other.argb == argb;
    }

    /**
     * Get green component of color.
     *
     * @return green component
     */
    public int g() {
        return (argb >> SHIFT_G) & 0xff;
    }

    /**
     * Get green component of color.
     *
     * @return green component as float (between 0.0 and 1.0)
     */
    public float gf() {
        return g() / 255.0f;
    }

    /**
     * Get red component of color.
     *
     * @return red component
     */
    public int r() {
        return (argb >> SHIFT_R) & 0xff;
    }

    /**
     * Get red component of color.
     *
     * @return red component as float (between 0.0 and 1.0)
     */
    public float rf() {
        return r() / 255.0f;
    }

    @Override
    public boolean isOpaque() {
        return (argb & 0xff000000) == 0xff000000;
    }

    @Override
    public boolean isTransparent() {
        return (argb & 0xff000000) == 0x00000000;
    }

    @Override
    public RGBColor toRGBColor() {
        return this;
    }

    @Override
    public int hashCode() {
        return argb;
    }

    @Override
    public String toString() {
        return toCss();
    }
}
