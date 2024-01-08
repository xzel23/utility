package com.dua3.utility.math.geometry;

/**
 * Bézier curve Path segment.
 */
public final class Curve2f extends AbstractCurve2f {
    /**
     * Name of this segment type.
     */
    public static final String NAME = "CURVE";

    /**
     * Constructor.
     * <p>
     * The control points are passed as indices into the path's vertex list.
     *
     * @param path the path this curve belongs to
     * @param b0   first control point
     * @param b1   second control point
     * @param b2   third control point
     */
    Curve2f(Path2fImpl path, int b0, int b1, int b2) {
        super(path, b0, b1, b2);
    }

    /**
     * Constructor.
     * <p>
     * The control points are passed as indices into the path's vertex list.
     *
     * @param path the path this curve belongs to
     * @param b0   first control point
     * @param b1   second control point
     * @param b2   third control point
     * @param b3   fourth control point
     */
    Curve2f(Path2fImpl path, int b0, int b1, int b2, int b3) {
        super(path, b0, b1, b2, b3);
    }

    @Override
    public String name() {
        return NAME;
    }
}
