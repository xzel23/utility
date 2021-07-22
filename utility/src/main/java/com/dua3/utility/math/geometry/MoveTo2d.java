package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

/**
 * Move current position.
 * <p>
 * <strong>NOTE: </strong> Only valid at start of path.
 */
public class MoveTo2d extends Segment2d {

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
     * @param path the path
     * @param idx index of current point
     */
    public MoveTo2d(Path2dImpl path, int idx) {
        super(path);
        this.idx = idx;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2d start() {
        return path.vertex(idx);
    }

    @Override
    public Vector2d end() {
        return path.vertex(idx);
    }

    @Override
    public String toString() {
        return "MoveTo2d{" +
               vertexToString(idx) +
               '}';
    }
}
