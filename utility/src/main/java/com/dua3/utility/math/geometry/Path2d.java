package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Path2d extends Path2dImpl {
    
    private final Path2dImpl impl;
    
    Path2d(Path2dImpl impl) {
        this.impl = impl;
    }
    
    public List<Vector2d> verctices() {
        return impl.vertices();
    }

    public List<Segment2d> segments() {
        return impl.segments();
    }

    @Override
    public String toString() {
        return segments().stream()
                .map(Objects::toString)
                .collect(Collectors.joining("\n        ", "Path2d{ ", " }"));
    }
}
