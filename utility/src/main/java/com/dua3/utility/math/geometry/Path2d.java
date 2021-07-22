package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A path in 2-dimensional space that is defined by joining together different segments.
 */
public final class Path2d {
    
    private final Path2dImpl impl;
    
    Path2d(Path2dImpl impl) {
        this.impl = impl;
    }

    /**
     * Get list of vertices of this path.
     * @return list of vertices
     */
    public List<Vector2d> vertices() {
        return impl.vertices();
    }

    /**
     * Get list of segments of this path.
     * @return list of segments
     */
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
