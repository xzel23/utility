package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

/**
 * Close path with line segment.
 */
public class ClosePath2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "CLOSE_PATH";

    /**
     * Index of the start point of this segment (actually the last of the vertices).
     */
    private final int p;
    /**
     * Index of the end point of this segment (actually the start of the path).
     */
    private final int q;

    /**
     * Constructor.
     *
     * @param path the path
     * @param p    index of the start point of this segment
     * @param q    index of the end point of this segment
     */
    ClosePath2f(Path2fImpl path, int p, int q) {
        super(path);
        this.p = p;
        this.q = q;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2f start() {
        return path.vertex(p);
    }

    @Override
    public Vector2f end() {
        return path.vertex(q);
    }

    @Override
    public String toString() {
        return "ClosePath2f{" +
                vertexToString(p) + "," +
                vertexToString(q) +
                '}';
    }
}
