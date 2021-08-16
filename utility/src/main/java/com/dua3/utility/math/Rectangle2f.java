/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;

public record Rectangle2f(float x, float y, float width, float height) {

    public Rectangle2f {
        assert width>=0;
        assert height>=0;
    }
    
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

    public static Rectangle2f of(float x, float y, float w, float h) {
        LangUtil.check(w>=0 && h>=0, "w and h must not be negative: w=%f, h=%f", w, h);
        return new Rectangle2f(x, y, w, h);
    }

    public static Rectangle2f of(Vector2f p, Dimension2f d) {
        return Rectangle2f.of(p.x(), p.y(), d.width(), d.height());    
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
    
}
