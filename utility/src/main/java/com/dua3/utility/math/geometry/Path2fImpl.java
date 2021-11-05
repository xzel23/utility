package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Path2fImpl {
    
    private final @NotNull List<Vector2f> vertices;
    private final @NotNull List<Segment2f> segments;
    
    Path2fImpl() {
        vertices = new ArrayList<>();
        segments = new ArrayList<>();
    }
    
    void addVertex(@NotNull Vector2f v) {
        assert v != null;
        vertices.add(v);
    }

    int vertexCount() {
        return vertices.size();
    }
    
    @NotNull List<Vector2f> vertices() {
        return Collections.unmodifiableList(vertices);
    }

    @NotNull List<Segment2f> segments() {
        return Collections.unmodifiableList(segments);
    }
    
    Vector2f vertex(int idx) {
        return vertices.get(idx);
    }

    void addSegment(@NotNull Segment2f segment) {
        assert segment != null;
        segments.add(segment);
    }
}

