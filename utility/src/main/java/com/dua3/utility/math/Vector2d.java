/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import java.util.Objects;

/**
 * An immutable 2-dimensional vector using float coordinates.
 */
public final class Vector2d {
    private final float x;
    private final float y;

    public static final Vector2d ORIGIN = new Vector2d(0,0);
    public static final Vector2d POSITIVE_INFINITY = new Vector2d(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    public static final Vector2d NEGATIVE_INFINITY = new Vector2d(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    
    private Vector2d(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create instance from coordinate values.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public static Vector2d of(float x, float y) {
        return new Vector2d(x,y);
    }

    /**
     * Return result vector of componentwise maximum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise maxima of a and b as components. 
     */
    public static Vector2d max(Vector2d a, Vector2d b) {
        return Vector2d.of(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Return result vector of componentwise minimum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise minima of a and b as components. 
     */
    public static Vector2d min(Vector2d a, Vector2d b) {
        return Vector2d.of(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Scalar product.
     * @param a first vector
     * @param b second vector
     * @return this * v
     */
    public static float scalarProduct(Vector2d a, Vector2d b) {
        return a.x*b.x+a.y*b.y;
    }

    /**
     * Return angle in radians between two vectors.
     * @param a first vector
     * @param b second vector
     * @return the angle in radians 
     */
    public static double angle(Vector2d a, Vector2d b) {
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
     * @return the x-coordinate.
     */
    public float x() {
        return x;
    }

    /**
     *@return the y-coordinate.
     */
    public float y() {
        return y;
    }

    /**
     * Vector addition.
     * @param v the argument
     * @return this + v
     */
    public Vector2d add(Vector2d v) {
        return new Vector2d(x + v.x, y + v.y);    
    }
    
    /**
     * Vector subtraction.
     * @param v the argument
     * @return this - v
     */
    public Vector2d subtract(Vector2d v) {
        return new Vector2d(x - v.x, y - v.y);    
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
            
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2d vec2d = (Vector2d) o;
        return vec2d.x==x && vec2d.y==y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2d{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
