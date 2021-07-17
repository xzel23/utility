package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public final class Line2d extends Segment2d {
    public static final String NAME = "LINE";

    final int a;
    final int b;

    Line2d(Path2dImpl path, int a, int b) {
        super(path);
        this.a = a;
        this.b = b;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2d start() {
        return path.vertex(a);
    }

    @Override
    public Vector2d end() {
        return path.vertex(b);
    }
}
