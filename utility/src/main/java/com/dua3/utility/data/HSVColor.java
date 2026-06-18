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
        } else { // max == b
            h = 60.0f * (4.0f + (r - g) / (max - min));
        }

        if (h < 0) {
            h += 360.0f;
        }

        float s = max == 0 ? 0 : (max - min) / max;

        return new HSVColor(h, s, max, a);
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
            case 0, 6 -> RGBColor.argbf(alpha, v, t, p);
            case 1 -> RGBColor.argbf(alpha, q, v, p);
            case 2 -> RGBColor.argbf(alpha, p, v, t);
            case 3 -> RGBColor.argbf(alpha, p, q, v);
            case 4 -> RGBColor.argbf(alpha, t, p, v);
            case 5 -> RGBColor.argbf(alpha, v, p, q);
            default -> throw new IllegalStateException("could not convert to RGB");
        };
    }

    @Override
    public HSVColor brighter() {
        return toHSLColor().brighter().toHSVColor();
    }

    @Override
    public HSVColor darker() {
        return toHSLColor().darker().toHSVColor();
    }

    @Override
    public String toString() {
        return toCss();
    }

    @Override
    public HSLColor toHSLColor() {
        float c = v * s;
        float l = v - c / 2.0f;

        float denominator = 1.0f - Math.abs(2.0f * l - 1.0f);
        float sl = denominator == 0.0f ? 0.0f : c / denominator;

        return new HSLColor(h, sl, l, alpha);
    }
}
    
