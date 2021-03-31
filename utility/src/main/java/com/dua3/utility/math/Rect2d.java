/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;

import java.util.List;
import java.util.Objects;

public class Rect2d implements Polygon2d {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    private Rect2d(double x, double y, double w, double h) {
        assert w>=0;
        assert h>=0;
        
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public static Rect2d withCorners(Vec2d p, Vec2d q) {
        double x,y,w,h;
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
        return new Rect2d(x,y,w,h);
    }

    public static Rect2d of(double x, double y, double w, double h) {
        LangUtil.check(w>=0 && h>=0, "w and h must not be negative: w=%f, h=%f", w, h);
        return new Rect2d(x, y, w, h);
    }

    public double xMin() {
        return x;
    }

    public double yMin() {
        return y;
    }

    public double xMax() {
        return x+width;
    }

    public double yMax() {
        return y+height;
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
        Rect2d rectangle = (Rect2d) o;
        return Double.compare(rectangle.x, x) == 0 && Double.compare(rectangle.y, y) == 0 && Double.compare(rectangle.width, width) == 0 && Double.compare(rectangle.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Rect2d{" +
               "x=" + x +
               ", y=" + y +
               ", width=" + width +
               ", height=" + height +
               '}';
    }

    @Override
    public int vertexCount() {
        return 4;
    }

    @Override
    public List<Vec2d> vertices() {
        return List.of(
                Vec2d.of(x,y),
                Vec2d.of(x+width,y),
                Vec2d.of(x+width,y+height),
                Vec2d.of(x,y+height)
        );
    }
}
