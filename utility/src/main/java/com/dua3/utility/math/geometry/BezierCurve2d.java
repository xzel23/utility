package com.dua3.utility.math.geometry;

public final class BezierCurve2d extends AbstractCurve2d {
    public static final String NAME = "BEZIER";

    BezierCurve2d(Path2dImpl path, int c0, int c1, int c2, int c3) {
        super(path, c0, c1, c2, c3);
    }

    @Override
    public String name() {
        return NAME;
    }
}
