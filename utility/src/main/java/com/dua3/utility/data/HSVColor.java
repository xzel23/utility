package com.dua3.utility.data;

/**
 * {@link Color} implementation that uses the HSV (hue, saturation, value) color model.
 *
 * @param h     the hue in the range [0 .. 360]
 * @param s     the saturation value [0 .. 1]
 * @param v     the brightness value [0 .. 1]
 * @param alpha the alpha value [0 .. 1]
 */
public record HSVColor(float h, float s, float v, float alpha) implements Color {

    /**
     * Create HSVColor from packed ARGB integer value.
     *
     * @param argb the packed ARGB value
     * @return color instance
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static HSVColor valueOf(int argb) {
        float a = ((argb >> 24) & 0xff) / 255.0f;
        float r = ((argb >> 16) & 0xff) / 255.0f;
        float g = ((argb >> 8) & 0xff) / 255.0f;
        float b = (argb & 0xff) / 255.0f;

        float min = Math.min(Math.min(r, g), b);
        float max = Math.max(Math.max(r, g), b);

        float h;
        if (min == max) {
            h = 0.0f;
        } else if (max == r) {
            h = 60.0f * (0.0f + (g - b) / (max - min));
        } else if (max == g) {
            h = 60.0f * (2.0f + (b - r) / (max - min));
        } else if (max == b) {
            h = 60.0f * (4.0f + (r - g) / (max - min));
        } else {
            throw new IllegalStateException("color conversion error");
        }

        if (h < 0) {
            h += 360.0f;
        }

        float s = max == 0 ? 0 : (max - min) / max;

        return new HSVColor(h, s, max, a);
    }

    private static int argbf(float a, float r, float g, float b) {
        int ri = Math.round(r * 255);
        int gi = Math.round(g * 255);
        int bi = Math.round(b * 255);
        int ai = Math.round(a * 255);
        return (ai << 24) + (ri << 16) + (gi << 8) + bi;
    }

    @Override
    public int a() {
        return Math.round(255 * alpha);
    }

    @Override
    public boolean isOpaque() {
        return alpha == 1;
    }

    @Override
    public boolean isTransparent() {
        return alpha == 0;
    }

    @Override
    public HSVColor toHSVColor() {
        return this;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public int argb() {
        int hi = (int) (h / 60);
        float f = h / 60.0f - hi;

        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));

        return switch (hi) {
            case 0, 6 -> argbf(alpha, v, t, p);
            case 1 -> argbf(alpha, q, v, p);
            case 2 -> argbf(alpha, p, v, t);
            case 3 -> argbf(alpha, p, q, v);
            case 4 -> argbf(alpha, t, p, v);
            case 5 -> argbf(alpha, v, p, q);
            default -> throw new IllegalStateException("could not convert to RGB");
        };
    }

    @Override
    public Color brighter() {
        return new HSVColor(h(), s(), Math.min(v() / F_BRIGHTEN, 1), alpha);
    }

    @Override
    public Color darker() {
        return new HSVColor(h(), s(), v() * F_BRIGHTEN, alpha);
    }

    @Override
    public String toString() {
        return toCss();
    }
}
    
