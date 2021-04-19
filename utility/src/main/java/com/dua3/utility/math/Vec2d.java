/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import java.util.Objects;

/**
 * An immutable 2-dimensional vector using double coordinates.
 */
public final class Vec2d {
    private final double x;
    private final double y;

    public static final Vec2d ORIGIN = new Vec2d(0,0);
    public static final Vec2d POSITIVE_INFINITY = new Vec2d(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
    public static final Vec2d NEGATIVE_INFINITY = new Vec2d(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
    
    private Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Create instance from coordinate values.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public static Vec2d of(double x, double y) {
        return new Vec2d(x,y);
    }

    /**
     * Return result vector of componentwise maximum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise maxima of a and b as components. 
     */
    public static Vec2d max(Vec2d a, Vec2d b) {
        return Vec2d.of(Math.max(a.x, b.x), Math.max(a.y, b.y));
    }

    /**
     * Return result vector of componentwise minimum.
     * @param a first vector
     * @param b second vector
     * @return Vector with the the componentwise minima of a and b as components. 
     */
    public static Vec2d min(Vec2d a, Vec2d b) {
        return Vec2d.of(Math.min(a.x, b.x), Math.min(a.y, b.y));
    }

    /**
     * Scalar product.
     * @param a first vector
     * @param b second vector
     * @return this * v
     */
    public static double scalarProduct(Vec2d a, Vec2d b) {
        return a.x*b.x+a.y*b.y;
    }

    /**
     * Return angle in radians between two vectors.
     * @param a first vector
     * @param b second vector
     * @return the agle in radians 
     */
    public static double angle(Vec2d a, Vec2d b) {
        double nominator = scalarProduct(a, b);
        
        if (nominator==0) {
            return Double.NaN;
        }
        
        double denominator = a.length() * b.length();

        // denominator==0 implies nominator==0, so this should only happen if scalarProdut(9 or length() is broken
        assert denominator!= 0;  
        
        return Math.acos(nominator/denominator);
    }

    /**
     * @return the x-coordinate.
     */
    public double x() {
        return x;
    }

    /**
     *@return the y-coordinate.
     */
    public double y() {
        return y;
    }

    /**
     * Vector addition.
     * @param v the argument
     * @return this + v
     */
    public Vec2d add(Vec2d v) {
        return new Vec2d(x+v.x, y+v.y);    
    }
    
    /**
     * Vector subtraction.
     * @param v the argument
     * @return this - v
     */
    public Vec2d subtract(Vec2d v) {
        return new Vec2d(x-v.x, y-v.y);    
    }
    
    /**
     * Calculate length of vector.
     * @return the length
     */
    public double length() {
        return Math.sqrt(scalarProduct(this, this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2d vec2d = (Vec2d) o;
        return vec2d.x==x && vec2d.y==y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vec2d{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
