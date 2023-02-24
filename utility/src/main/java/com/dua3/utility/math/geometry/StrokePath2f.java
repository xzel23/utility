package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

/**
 * Stroke path.
 */
public class StrokePath2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "STROKE_PATH";

    /**
     * Index of current point.
     */
    private final int idx;

    /**
     * Constructor
     *
     * @param path the path
     * @param idx  index of current point
     */
    StrokePath2f(Path2fImpl path, int idx) {
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

}
