/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

/**
 * An immutable 2-dimensional vector using float coordinates.
 */
public record Vector2f(float x, float y) {

    /**
     * The origin of the coordinate space.
     */
    public static final Vector2f ORIGIN = new Vector2f(0, 0);

    /**
     * The vector (1,0).
     */
    public static final Vector2f ONE_ZERO = new Vector2f(1, 0);

    /**
     * The vector (1,1).
     */
    public static final Vector2f ONE_ONE = new Vector2f(1, 1);

    /**
     * The vector (0,1).
     */
    public static final Vector2f ZERO_ONE = new Vector2f(0, 1);

    /**
     * The vector (-1,1).
     */
    public static final Vector2f MINUS_ONE_ONE = new Vector2f(-1, 1);

    /**
     * The vector (-1,0).
     */
    public static final Vector2f MINUS_ONE_ZERO = new Vector2f(-1, 0);

    /**
     * The vector (-1,-1).
     */
    public static final Vector2f MINUS_ONE_MINUS_ONE = new Vector2f(-1, -1);

    /**
     * The vector (0,-1).
     */
    public static final Vector2f ZERO_MINUS_ONE = new Vector2f(0, -1);

    /**
     * The vector (1,-1).
     */
    public static final Vector2f ONE_MINUS_ONE = new Vector2f(1, -1);

    /**
     * Create instance from coordinate values.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return Vector with the components x and y
     */
    public static Vector2f of(float x, float y) {
        return new Vector2f(x, y);
    }

    /**
     * Return result vector of componentwise maximum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the componentwise maxima of a and b as components.
     */
    public static Vector2f max(Vector2f a, Vector2f b) {
        return of(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Return result vector of componentwise minimum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the componentwise minima of a and b as components.
     */
    public static Vector2f min(Vector2f a, Vector2f b) {
        return of(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Scalar product.
     * @param a first vector
     * @param b second vector
     * @return this * v
     */
    public static float scalarProduct(Vector2f a, Vector2f b) {
        return a.x * b.x + a.y * b.y;
    }

    /**
     * Return angle in radians between two vectors.
     * @param a first vector
     * @param b second vector
     * @return the angle in radians 
     */
    public static double angle(Vector2f a, Vector2f b) {
        float nominator = scalarProduct(a, b);

        if (nominator == 0) {
            return Float.NaN;
        }

        double denominator = a.length() * b.length();

        // denominator==0 implies nominator==0, so this should only happen if scalarProduct() or length() is broken
        assert denominator != 0 : "denominator is 0, arguments: a=" + a + ", b=" + b;

        return Math.acos(nominator / denominator);
    }

    /**
     * Vector addition.
     * @param v the argument
     * @return this + v
     */
    public Vector2f add(Vector2f v) {
        return new Vector2f(x + v.x, y + v.y);
    }

    /**
     * Vector subtraction.
     * @param v the argument
     * @return this - v
     */
    public Vector2f subtract(Vector2f v) {
        return new Vector2f(x - v.x, y - v.y);
    }

    /**
     * Calculate length of vector.
     * @return the length
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Compute the vector's angle in radians.
     * @return angle in radians in the range [-π,π]
     */
    public double angle() {
        return Math.atan2(y(), x());
    }

}
