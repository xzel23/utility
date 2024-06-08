package com.dua3.utility.math.geometry;

/**
 * An immutable 2-dimensional dimension using float coordinates.
 */
public record Dimension2f(float width, float height) {

    /**
     * Constructor.
     *
     * @param width  the width (>= 0)
     * @param height the height (>= 0)
     */
    public Dimension2f {
        assert width >= 0 : "width is negative: " + width;
        assert height >= 0 : "height is negative: " + height;
    }

    /**
     * Get instance.
     *
     * @param w the width
     * @param h the height
     * @return instance with given width and height
     */
    public static Dimension2f of(float w, float h) {
        return new Dimension2f(w, h);
    }

    /**
     * Scales the current Dimension2f object by the specified Scale2f object.
     *
     * @param s the Scale2f object representing the scaling factors for the x-axis and y-axis
     * @return a new Dimension2f object with the scaled width and height
     */
    public Dimension2f scaled(Scale2f s) {
        return new Dimension2f(s.sx() * width, s.sy() * height);
    }

    /**
     * Scales the dimension by a given factor.
     *
     * @param s the scaling factor.
     * @return a new Dimension2f object with the scaled width and height.
     */
    public Dimension2f scaled(float s) {
        return new Dimension2f(s * width, s * height);
    }
}
