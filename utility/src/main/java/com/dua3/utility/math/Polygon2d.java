package com.dua3.utility.math;

import com.dua3.utility.lang.LangUtil;

import java.util.Arrays;
import java.util.List;

public interface Polygon2d {
    
    static Polygon2d ofVertices(Vec2d... v) {
        return new Poly2d(v);
    }
    
    int vertexCount();
    
    List<Vec2d> vertices();

    default Polygon2d transform(AffineTransformation at) {
        List<Vec2d> vs = vertices();
        Vec2d[] vt = new Vec2d[vs.size()];
        for (int i = 0; i < vs.size(); i++) {
            vt[i] = at.transform(vs.get(i));
        }
        return new Poly2d(vt);
    }
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

    public Polygon2d transform(AffineTransformation at) {
        Vec2d[] vt = new Vec2d[vertices.length];
        for (int i=0; i<vertices.length; i++) {
            vt[i] = at.transform(vertices[i]);
        }
        return new Poly2d(vt);
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
