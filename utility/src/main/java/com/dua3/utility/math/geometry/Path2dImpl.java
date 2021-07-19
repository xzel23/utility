package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Path2dImpl {
    
    private final List<Vector2d> vertices;
    private final List<Segment2d> segments;
    
    public Path2dImpl() {
        vertices = new ArrayList<>();
        segments = new ArrayList<>();
    }
    
    public int addVertex(Vector2d v) {
        assert v != null;
        vertices.add(v);
        return vertices.size()-1;
    }

    public int vertexCount() {
        return vertices.size();
    }
    
    public List<Vector2d> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Segment2d> segments() {
        return Collections.unmodifiableList(segments);
    }
    
    public Vector2d vertex(int idx) {
        return vertices.get(idx);
    }

    public void addSegment(Segment2d segment) {
        assert segment != null;
        segments.add(segment);
    }
}

