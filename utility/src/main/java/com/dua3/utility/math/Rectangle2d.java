/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2d;

import java.util.Objects;

public final class Rectangle2d {
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    private Rectangle2d(float x, float y, float w, float h) {
        assert w>=0;
        assert h>=0;
        
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public static Rectangle2d withCorners(Vector2d p, Vector2d q) {
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
        return new Rectangle2d(x,y,w,h);
    }

    public static Rectangle2d of(float x, float y, float w, float h) {
        LangUtil.check(w>=0 && h>=0, "w and h must not be negative: w=%f, h=%f", w, h);
        return new Rectangle2d(x, y, w, h);
    }

    public static Rectangle2d of(Vector2d p, Dimension2d d) {
        return Rectangle2d.of(p.x(), p.y(), d.width(), d.height());    
    } 
    
    public float xMin() {
        return x;
    }

    public float yMin() {
        return y;
    }

    public float xMax() {
        return x+width;
    }

    public float yMax() {
        return y+height;
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
        Rectangle2d rectangle = (Rectangle2d) o;
        return Float.compare(rectangle.x, x) == 0 && Float.compare(rectangle.y, y) == 0 && Float.compare(rectangle.width, width) == 0 && Float.compare(rectangle.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Rectangle2d{" +
               "x=" + x +
               ", y=" + y +
               ", width=" + width +
               ", height=" + height +
               '}';
    }

}
