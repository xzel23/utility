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
     * Scalar product.
     * @param v the argument
     * @return this * v
     */
    public double scalarProduct(Vec2d v) {
        return x*v.x+y*v.y;    
    }

    /**
     * Calculate length of vector.
     * @return the length
     */
    public double length() {
        return Math.sqrt(scalarProduct(this));
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
