package com.dua3.utility.math.geometry;

/**
 * Record holding scaling factors for x- and y-axis.
 *
 * @param sx the x-scaling factor
 * @param sy the y-scaling factor
 */
public record Scale2f(float sx, float sy) {
    private static final Scale2f IDENTITY = new Scale2f(1.0f, 1.0f);

    /**
     * Returns the identity scale {@code Scale2f} object.
     * The identity scale is a {@code Scale2f} object with scaling factors of 1 for both the x and y axes.
     *
     * @return the identity scale {@code Scale2f} object
     */
    public static Scale2f identity() {
        return IDENTITY;
    }

    /**
     * Constructs a new {@code Scale2f} object with the specified scaling factor s for both x and y axes.
     *
     * @param s the scaling factor
     */
    public Scale2f(float s) {
        this(s, s);
    }

    /**
     * Creates a new {@code Scale2f} instance with specified scaling factors.
     *
     * @param sx the scaling factor for the x-axis
     * @param sy the scaling factor for the y-axis
     * @return a new {@code Scale2f} instance with the given scaling factors
     */
    public static Scale2f of(float sx, float sy) {
        return new Scale2f(sx, sy);
    }

    /**
     * Creates a new {@code Scale2f} object with the specified uniform scaling factor.
     *
     * @param s the scaling factor for both the x and y axes
     * @return a new {@code Scale2f} object with the specified scaling factor applied to both axes
     */
    public static Scale2f of(float s) {
        return new Scale2f(s);
    }

    /**
     * Multiplies the current scale by another scale.
     *
     * @param other the scale to multiply with
     * @return a new Scale object with the multiplied scale factors
     */
    public Scale2f multiply(Scale2f other) {
        return new Scale2f(sx * other.sx, sy * other.sy);
    }

    /**
     * Multiplies the scale factors by the specified values and returns a new Scale object with the multiplied values.
     *
     * @param sx the scaling factor for the sx-axis
     * @param sy the scaling factor for the sy-axis
     * @return a new Scale object with the multiplied values
     */
    public Scale2f multiply(float sx, float sy) {
        return new Scale2f(this.sx * sx, this.sy * sy);
    }

    /**
     * Multiplies the scale factors of the current Scale object by a scalar value.
     *
     * @param s the scalar value to multiply the scale factors by
     * @return a new Scale object with the multiplied scale factors
     */
    public Scale2f multiply(float s) {
        return new Scale2f(sx * s, sy * s);
    }
}
