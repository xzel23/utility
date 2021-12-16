/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import com.dua3.cabe.annotations.NotNull;

/**
 * An immutable 2-dimensional vector using float coordinates.
 */
public record Vector2f(float x, float y) {

    public static final Vector2f ORIGIN = new Vector2f(0,0);

    /**
     * Create instance from coordinate values.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public static Vector2f of(float x, float y) {
        return new Vector2f(x,y);
    }

    /**
     * Return result vector of componentwise maximum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise maxima of a and b as components. 
     */
    public static Vector2f max(@NotNull Vector2f a, @NotNull Vector2f b) {
        return Vector2f.of(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Return result vector of componentwise minimum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise minima of a and b as components. 
     */
    public static Vector2f min(@NotNull Vector2f a, @NotNull Vector2f b) {
        return Vector2f.of(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Scalar product.
     * @param a first vector
     * @param b second vector
     * @return this * v
     */
    public static float scalarProduct(@NotNull Vector2f a, @NotNull Vector2f b) {
        return a.x*b.x+a.y*b.y;
    }

    /**
     * Return angle in radians between two vectors.
     * @param a first vector
     * @param b second vector
     * @return the angle in radians 
     */
    public static double angle(@NotNull Vector2f a, @NotNull Vector2f b) {
        float nominator = scalarProduct(a, b);
        
        if (nominator==0) {
            return Float.NaN;
        }
        
        double denominator = a.length() * b.length();

        // denominator==0 implies nominator==0, so this should only happen if scalarProdut() or length() is broken
        assert denominator != 0;  
        
        return Math.acos(nominator/denominator);
    }

    /**
     * Vector addition.
     * @param v the argument
     * @return this + v
     */
    public Vector2f add(@NotNull Vector2f v) {
        return new Vector2f(x + v.x, y + v.y);    
    }
    
    /**
     * Vector subtraction.
     * @param v the argument
     * @return this - v
     */
    public Vector2f subtract(@NotNull Vector2f v) {
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
     * Compute the avector's angle in radians.
     * @return angle in radians in the range [-π,π]
     */
    public double angle() {
        return Math.atan2(y(), x());
    }
            
}
