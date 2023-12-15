package com.dua3.utility.math.geometry;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A path in 2-dimensional space that is defined by joining together different segments.
 */
public final class Path2f {

    /**
     * Creates a new instance of {@link PathBuilder2f}.
     *
     * @return a new instance of {@link PathBuilder2f}
     */
    public static PathBuilder2f builder() {
        return new PathBuilder2f();
    }

    private final Path2fImpl impl;

    Path2f(Path2fImpl impl) {
        this.impl = impl;
    }

    /**
     * Get this path's vertices as a list.
     *
     * @return unmodifiable list of vertices
     */
    public List<Vector2f> vertices() {
        return impl.vertices();
    }

    /**
     * This path's segments as a list.
     *
     * @return unmodifiable list of segments
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
