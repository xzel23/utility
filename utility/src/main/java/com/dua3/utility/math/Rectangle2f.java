/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;

/**
 * Immutable rectangle with float coordinates.
 */
public record Rectangle2f(float x, float y, float width, float height) {

    public Rectangle2f {
        assert width>=0;
        assert height>=0;
    }

    /**
     * Create rectangle from two vectors.
     * @param p the first corner
     * @param q the second corner
     * @return rectangle
     */
    public static Rectangle2f withCorners(Vector2f p, Vector2f q) {
        float x,y,w,h;
        if (p.x()<=q.x()) {
            x = p.x();
            w = q.x()-p.x();
        } else {
            x = q.x();
            w = p.x()-q.x();
        }
        if (p.y()<=q.y()) {
            y = p.y();
            h = q.y()-p.y();
        } else {
            y = q.y();
            h = p.y()-q.y();
        }
        return new Rectangle2f(x,y,w,h);
    }

    /**
     * Create rectangle from coordinates and dimension. Both width and height must be non-negative.
     * @param x the x-coordinate 
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     * @return rectangle
     */
    public static Rectangle2f of(float x, float y, float w, float h) {
        LangUtil.check(w>=0 && h>=0, "w and h must not be negative: w=%f, h=%f", w, h);
        return new Rectangle2f(x, y, w, h);
    }

    /**
     * Create rectangle from corner and dimension.
     * @param p the vector to the corner 
     * @param d the rectangle dimension
     * @return rectangle
     */
    public static Rectangle2f of(Vector2f p, Dimension2f d) {
        return Rectangle2f.of(p.x(), p.y(), d.width(), d.height());    
    }

    /**
     * The minimum of the two x-coordinates.
     * @return minimum x-coordinate
     */
    public float xMin() {
        return x;
    }

    /**
     * The minimum of the two y-coordinates.
     * @return minimum y-coordinate
     */
    public float yMin() {
        return y;
    }

    /**
     * The maximum of the two x-coordinates.
     * @return maximum x-coordinate
     */
    public float xMax() {
        return x+width;
    }

    /**
     * The maximum of the two y-coordinates.
     * @return maximum y-coordinate
     */
    public float yMax() {
        return y+height;
    }
    
}
