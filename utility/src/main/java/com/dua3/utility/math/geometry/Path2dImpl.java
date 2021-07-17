package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Path2dImpl {
    
    private final List<Vector2d> vertices;
    private final List<Segment2d> segments;
    
    private int current = -1;
    private int startOfSubPath = -1;
    
    public Path2dImpl() {
        vertices = new ArrayList<>();
        segments = new ArrayList<>();
    }
    
    public int addVertex(Vector2d v) {
        assert v != null;
        vertices.add(v);
        assert vertices.size()-2 == current;
        return ++current;
    }
    
    public List<Vector2d> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Segment2d> segments() {
        return Collections.unmodifiableList(segments);
    }
    
    public Vector2d current() {
        assert current >= 0;
        return vertices.get(current);
    }
    
    public void moveTo(Vector2d p) {
        startOfSubPath = addVertex(p);
        segments.add(new MoveTo2d(this, startOfSubPath));
    }
    
    public void lineTo(Vector2d v) {
        int p = current;
        int q = addVertex(v);
        segments.add(new Line2d(this, p, q));
    }
    
    public void curveTo(Vector2d p1, Vector2d p2,Vector2d p3) {
        assert !vertices.isEmpty();
        int c0 = current;
        int c1 = addVertex(p1);
        int c2 = addVertex(p2);
        int c3 = addVertex(p3);
        segments.add(new BezierCurve2d(this, c0, c1, c2, c3));
    }
    
    public void closePath() {
        segments.add(new ClosePath2d(this, current, startOfSubPath));
    }

    public Vector2d vertex(int idx) {
        return vertices.get(idx);
    }

    public void addSegment(Segment2d segment) {
        assert segment != null;
        segments.add(segment);
    }
}

