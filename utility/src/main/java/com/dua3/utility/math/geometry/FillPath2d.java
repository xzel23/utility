package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

/**
 * Fill and stroke path.
 */
public class FillPath2d extends Segment2d {

    /**
     * Segment type name.
     */
    public static final String NAME = "FILL_PATH";

    /**
     * Index of current vertex.
     */
    private final int idx;

    /**
     * The fill rule.
     */
    private final FillRule fillRule;

    /**
     * Constructor.
     * @param path the path
     * @param idx index of current vertex
     * @param fillRule the fill rule to use
     */
    FillPath2d(Path2dImpl path, int idx, FillRule fillRule) {
        super(path);
        this.idx = idx;
        this.fillRule = fillRule;
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

    /**
     * Get fill rule.
     * @return the fill rule
     */
    public FillRule fillRule() {
        return fillRule;
    }

}
