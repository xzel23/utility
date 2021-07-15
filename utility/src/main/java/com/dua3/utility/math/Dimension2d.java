package com.dua3.utility.math;

import java.util.Objects;

/**
 * An immutable 2-dimensional dimension using double coordinates.
 */
public final class Dimension2d {
    
    public static Dimension2d of(double w, double h) {
        assert w >= 0 && h >= 0 : "width and height must not be negative";
        return new Dimension2d(w,h);    
    }
    
    private final double width;
    private final double height;

    private Dimension2d(double w, double h) { 
        this.width = w;
        this.height = h;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension2d that = (Dimension2d) o;
        return Double.compare(that.width, width) == 0 && Double.compare(that.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "Dimension2d{" +
               "width=" + width +
               ", height=" + height +
               '}';
    }
}
