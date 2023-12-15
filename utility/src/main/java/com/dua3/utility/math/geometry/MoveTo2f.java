package com.dua3.utility.math.geometry;

/**
 * Move current position.
 * <p>
 * <strong>NOTE: </strong> Only valid at start of path.
 */
public class MoveTo2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "MOVE_TO";

    /**
     * Index of current point.
     */
    private final int idx;

    /**
     * Constructor.
     *
     * @param path the path
     * @param idx  index of current point
     */
    MoveTo2f(Path2fImpl path, int idx) {
        super(path);
        this.idx = idx;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2f start() {
        return path.vertex(idx);
    }

    @Override
    public Vector2f end() {
        return path.vertex(idx);
    }

    @Override
    public String toString() {
        return "MoveTo2f{" +
                vertexToString(idx) +
                '}';
    }
}
