package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Path2fImpl {
    
    private final List<Vector2f> vertices;
    private final List<Segment2f> segments;
    
    Path2fImpl() {
        vertices = new ArrayList<>();
        segments = new ArrayList<>();
    }
    
    void addVertex(Vector2f v) {
        assert v != null;
        vertices.add(v);
    }

    int vertexCount() {
        return vertices.size();
    }
    
    List<Vector2f> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    List<Segment2f> segments() {
        return Collections.unmodifiableList(segments);
    }

    Vector2f vertex(int idx) {
        return vertices.get(idx);
    }

    void addSegment(Segment2f segment) {
        assert segment != null;
        segments.add(segment);
    }
}

