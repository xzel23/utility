package com.dua3.utility.math.geometry;

import java.util.Objects;

/**
 * An immutable 2-dimensional dimension using float coordinates.
 */
public final class Dimension2d {
    
    public static Dimension2d of(float w, float h) {
        assert w >= 0 && h >= 0 : "width and height must not be negative";
        return new Dimension2d(w,h);    
    }
    
    private final float width;
    private final float height;

    private Dimension2d(float w, float h) { 
        this.width = w;
        this.height = h;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension2d that = (Dimension2d) o;
        return Float.compare(that.width, width) == 0 && Float.compare(that.height, height) == 0;
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
