package com.dua3.utility.text;

public interface FontUtil<F> {
    class Bounds {
        public final double width;
        public final double height;

        public Bounds(double w, double h) {
            this.width = w;
            this.height = h;
        }
    }

    /**
     * Convert font.
     *
     * @param  f
     *           the font
     * @return
     *           the font implementation
     */
    F convert(Font f);

    /**
     * Get text bounds.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text bounds
     */
    Bounds getTextBounds(String s, Font f);

    /**
     * Get text width.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text width
     */
    default double getTextWidth(String s, Font f) {
        return getTextBounds(s, f).width;
    }

    /**
     * Get text height.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text height
     */
    default double getTextHeight(String s, Font f) {
        return getTextBounds(s, f).height;
    }
}