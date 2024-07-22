package com.dua3.utility.math.geometry;

/**
 * Close path with line segment.
 */
public class ClosePath2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "CLOSE_PATH";

    /**
     * Start point index of this segment (actually the last of the vertices).
     */
    private final int p;
    /**
     * End point index of this segment (actually the start of the path).
     */
    private final int q;

    /**
     * Constructor.
     *
     * @param path the path
     * @param p    start point index of this segment
     * @param q    end point index of this segment
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
        return vertex(p);
    }

    @Override
    public Vector2f end() {
        return vertex(q);
    }

    @SuppressWarnings("MagicCharacter")
    @Override
    public String toString() {
        return "ClosePath2f{" +
                vertexToString(p) + "," +
                vertexToString(q) +
                '}';
    }
}
