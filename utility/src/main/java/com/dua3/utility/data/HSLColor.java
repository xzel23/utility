package com.dua3.utility.data;

/**
 * {@link Color} implementation that uses the HSL (hue, saturation, lightness) color model.
 *
 * @param h     the hue in the range [0 .. 360]
 * @param s     the saturation value [0 .. 1]
 * @param l     the lightness value [0 .. 1]
 * @param alpha the alpha value [0 .. 1]
 */
public record HSLColor(float h, float s, float l, float alpha) implements Color {

    public static HSLColor valueOf(int argb) {
        float a = ((argb >> 24) & 0xff) / 255.0f;
        float r = ((argb >> 16) & 0xff) / 255.0f;
        float g = ((argb >> 8) & 0xff) / 255.0f;
        float b = (argb & 0xff) / 255.0f;

        float min = Math.min(Math.min(r, g), b);
        float max = Math.max(Math.max(r, g), b);

        float h;
        float s;
        float l = (max + min) / 2.0f;

        if (max == min) {
            h = 0.0f;
            s = 0.0f;
        } else {
            float d = max - min;

            s = l > 0.5f
                    ? d / (2.0f - max - min)
                    : d / (max + min);

            if (max == r) {
                h = (g - b) / d + (g < b ? 6.0f : 0.0f);
            } else if (max == g) {
                h = (b - r) / d + 2.0f;
            } else {
                h = (r - g) / d + 4.0f;
            }

            h *= 60.0f;
        }

        return new HSLColor(h, s, l, a);
    }

    private static int argbf(float a, float r, float g, float b) {
        int ri = Math.round(r * 255);
        int gi = Math.round(g * 255);
        int bi = Math.round(b * 255);
        int ai = Math.round(a * 255);
        return (ai << 24) + (ri << 16) + (gi << 8) + bi;
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) {
            t += 1;
        }
        if (t > 1) {
            t -= 1;
        }

        if (t < 1f / 6f) {
            return p + (q - p) * 6 * t;
        }
        if (t < 1f / 2f) {
            return q;
        }
        if (t < 2f / 3f) {
            return p + (q - p) * (2f / 3f - t) * 6;
        }

        return p;
    }

    @Override
    public int a() {
        return Math.round(alpha * 255);
    }

    @Override
    public boolean isOpaque() {
        return alpha == 1.0f;
    }

    @Override
    public boolean isTransparent() {
        return alpha == 0.0f;
    }

    public HSLColor toHSLColor() {
        return this;
    }

    @Override
    public int argb() {
        float r;
        float g;
        float b;

        if (s == 0) {
            r = l;
            g = l;
            b = l;
        } else {
            float q = l < 0.5f
                    ? l * (1 + s)
                    : l + s - l * s;

            float p = 2 * l - q;

            float hk = h / 360.0f;

            r = hueToRgb(p, q, hk + 1f / 3f);
            g = hueToRgb(p, q, hk);
            b = hueToRgb(p, q, hk - 1f / 3f);
        }

        return argbf(alpha, r, g, b);
    }

    @Override
    public Color brighter() {
        return new HSLColor(
                h,
                s,
                l + (1.0f - l) * (1.0f - F_BRIGHTEN),
                alpha
        );
    }

    @Override
    public Color darker() {
        return new HSLColor(
                h,
                s,
                l * F_BRIGHTEN,
                alpha
        );
    }

    @Override
    public String toString() {
        return toCss();
    }
}
