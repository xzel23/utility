package com.dua3.utility.math.geometry;

import java.util.Locale;

/**
 * Base class for segments that form a {@link Path2f}.
 */
public abstract class Segment2f {
    private final Path2fImpl path;

    Segment2f(Path2fImpl path) {
        this.path = path;
    }

    /**
     * Retrieves the vertex at the specified index from the associated path.
     *
     * @param idx the index of the vertex to retrieve
     * @return the {@link Vector2f} representing the vertex at the specified index
     */
    protected Vector2f vertex(int idx) {
        return path.vertex(idx);
    }

    /**
     * The identifying name for the type of the segment.
     *
     * @return name of the segment
     */
    public abstract String name();

    /**
     * The segment's starting point.
     *
     * @return the start of the segment
     */
    public abstract Vector2f start();

    /**
     * The segment's end point.
     *
     * @return the end of the segment
     */
    public abstract Vector2f end();

    /**
     * Converts a vertex at a specified index into a string representation.
     * The string representation includes the index and the coordinates of the vertex.
     *
     * @param idx the index of the vertex in the path
     * @return a string representing the vertex in the format "[index](x,y)"
     */
    protected String vertexToString(int idx) {
        Vector2f v = path.vertex(idx);
        return String.format(Locale.ROOT, "[%d](%f,%f)", idx, v.x(), v.y());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
