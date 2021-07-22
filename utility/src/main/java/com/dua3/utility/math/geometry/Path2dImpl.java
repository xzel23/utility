package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Path2dImpl {
    
    private final List<Vector2d> vertices;
    private final List<Segment2d> segments;
    
    Path2dImpl() {
        vertices = new ArrayList<>();
        segments = new ArrayList<>();
    }
    
    void addVertex(Vector2d v) {
        assert v != null;
        vertices.add(v);
    }

    int vertexCount() {
        return vertices.size();
    }
    
    List<Vector2d> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    List<Segment2d> segments() {
        return Collections.unmodifiableList(segments);
    }
    
    Vector2d vertex(int idx) {
        return vertices.get(idx);
    }

    void addSegment(Segment2d segment) {
        assert segment != null;
        segments.add(segment);
    }
}

