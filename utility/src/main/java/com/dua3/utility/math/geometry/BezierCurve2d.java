package com.dua3.utility.math.geometry;

/**
 * Beziér curve Path segment.
 */
public final class BezierCurve2d extends AbstractCurve2d {
    /**
     * Name of this type of segment.
     */
    public static final String NAME = "BEZIER";

    /**
     * Constructor.
     * <p>
     * The control points are passed as indices into the path's vertex list.
     * 
     * @param path the path this curve belongs to
     * @param b0 first control point
     * @param b1 second control point
     * @param b2 third control point
     * @param b3 fourth control point
     */
    BezierCurve2d(Path2dImpl path, int b0, int b1, int b2, int b3) {
        super(path, b0, b1, b2, b3);
    }

    @Override
    public String name() {
        return NAME;
    }
}
