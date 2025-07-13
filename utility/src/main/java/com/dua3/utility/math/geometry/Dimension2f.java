package com.dua3.utility.math.geometry;

/**
 * An immutable 2-dimensional dimension using float coordinates.
 *
 * @param width  the width (>= 0)
 * @param height the height (>= 0)
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
     * Scales the current {@code Dimension2f} object by the specified {@link Scale2f} object.
     *
     * @param s the {@link Scale2f} object representing the scaling factors for the x-axis and y-axis
     * @return a new {@code Dimension2f} object with the scaled width and height
     */
    public Dimension2f scaled(Scale2f s) {
        return new Dimension2f(s.sx() * width, s.sy() * height);
    }

    /**
     * Scales the dimension by a given factor.
     *
     * @param s the scaling factor.
     * @return a new {@code Dimension2f} object with the scaled width and height.
     */
    public Dimension2f scaled(float s) {
        return new Dimension2f(s * width, s * height);
    }

    /**
     * Returns a new {@code Dimension2f} object with a margin added to both width and height.
     * The margin is added equally on both sides, effectively increasing the width and height
     * by double the provided margin value.
     *
     * @param m the margin to be added to the width and height
     * @return a new {@code Dimension2f} object with updated dimensions
     */
    public Dimension2f addMargin(float m) {
        return new Dimension2f(width + 2 * m, height + 2 * m);
    }

    /**
     * Adds a margin to the current width and height with separate margins for each direction.
     *
     * @param mx the margin to be added to the width
     * @param my the margin to be added to the height
     * @return a new {@code Dimension2f} object with the adjusted width and height
     */
    public Dimension2f addMargin(float mx, float my) {
        return new Dimension2f(width + 2 * mx, height + 2 * my);
    }

    /**
     * Computes the maximum of two {@code Dimension2f} objects based on their width and height.
     *
     * @param a the first {@code Dimension2f} object
     * @param b the second {@code Dimension2f} object
     * @return a new {@code Dimension2f} object with the maximum width and height values
     *         from the two input dimensions
     */
    public static Dimension2f max(Dimension2f a, Dimension2f b) {
        return new Dimension2f(Math.max(a.width(), b.width()), Math.max(a.height(), b.height()));
    }

    /**
     * Computes the minimum of two {@code Dimension2f} objects based on their dimensions.
     * The resulting {@code Dimension2f} has the lesser width of the two input dimensions
     * and the greater height of the two input dimensions.
     *
     * @param a the first {@code Dimension2f} object
     * @param b the second {@code Dimension2f} object
     * @return a new {@code Dimension2f} object with the minimum width and maximum height
     *         from the two input dimensions
     */
    public static Dimension2f min(Dimension2f a, Dimension2f b) {
        return new Dimension2f(Math.min(a.width(), b.width()), Math.min(a.height(), b.height()));
    }
}
