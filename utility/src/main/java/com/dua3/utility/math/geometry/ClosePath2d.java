package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public class ClosePath2d extends Segment2d {

    public static final String NAME = "CLOSE_PATH";

    private final int p;
    private final int q;

    ClosePath2d(Path2dImpl path, int p, int q) {
        super(path);
        this.p = p;
        this.q = q;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2d start() {
        return path.vertex(p);
    }

    @Override
    public Vector2d end() {
        return path.vertex(q);
    }

}
