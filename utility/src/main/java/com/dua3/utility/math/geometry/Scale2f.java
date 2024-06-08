package com.dua3.utility.math.geometry;

/**
 * Record holding scaling factors for x- and y-axis.
 *
 * @param x the x-scaling factor
 * @param y the y-scaling factor
 */
public record Scale2f(float x, float y) {
    /**
     * Multiplies the current scale by another scale.
     *
     * @param other the scale to multiply with
     * @return a new Scale object with the multiplied scale factors
     */
    public Scale2f multiply(Scale2f other) {
        return new Scale2f(x * other.x, y * other.y);
    }

    /**
     * Multiplies the scale factors by the specified values and returns a new Scale object with the multiplied values.
     *
     * @param sx the scaling factor for the x-axis
     * @param sy the scaling factor for the y-axis
     * @return a new Scale object with the multiplied values
     */
    public Scale2f multiply(float sx, float sy) {
        return new Scale2f(x * sx, y * sy);
    }

    /**
     * Multiplies the scale factors of the current Scale object by a scalar value.
     *
     * @param s the scalar value to multiply the scale factors by
     * @return a new Scale object with the multiplied scale factors
     */
    public Scale2f multiply(float s) {
        return new Scale2f(x * s, y * s);
    }
}
