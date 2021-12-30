package com.dua3.utility.math.geometry;

/**
 * An immutable 2-dimensional dimension using float coordinates.
 */
public record Dimension2f(float width, float height) {

    public Dimension2f {
        assert width >=0;
        assert height >=0;
    }

    /**
     * Get instance.
     * @param w the width
     * @param h the height
     * @return instance with given width and height
     */
    public static Dimension2f of(float w, float h) {
        return new Dimension2f(w,h);    
    }
    
}
