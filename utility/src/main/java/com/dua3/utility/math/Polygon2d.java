package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface Polygon2d {
    
    static Polygon2d ofVertices(Vec2d... v) {
        return new Poly2d(v);
    }
    
    int vertexCount();
    
    List<Vec2d> vertices();
    
}

class Poly2d implements Polygon2d {
    private final Vec2d[] vertices;
    
    Poly2d(Vec2d... v) {
        LangUtil.check(v.length>=3, "polygon must have at leat 3 vertices");
        this.vertices = v;
    }

    @Override
    public int vertexCount() {
        return vertices.length;
    }

    @Override
    public List<Vec2d> vertices() {
        return List.of(vertices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poly2d poly2d = (Poly2d) o;
        return Arrays.equals(vertices, poly2d.vertices);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vertices);
    }

    @Override
    public String toString() {
        return "Poly2d{" +
               "vertices=" + Arrays.toString(vertices) +
               '}';
    }
}