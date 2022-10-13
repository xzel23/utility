package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A path in 2-dimensional space that is defined by joining together different segments.
 */
public final class Path2f {

    private final Path2fImpl impl;

    Path2f(Path2fImpl impl) {
        this.impl = impl;
    }

    /**
     * Get list of vertices of this path.
     * @return list of vertices
     */
    public List<Vector2f> vertices() {
        return impl.vertices();
    }

    /**
     * Get list of segments of this path.
     * @return list of segments
     */
    public List<Segment2f> segments() {
        return impl.segments();
    }

    @Override
    public String toString() {
        return segments().stream()
                .map(Objects::toString)
                .collect(Collectors.joining("\n        ", "Path2f{ ", " }"));
    }
}
