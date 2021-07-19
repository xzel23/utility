package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public class EndPath2d extends Segment2d {

    public static final String NAME = "END_PATH";

    private final int p;

    EndPath2d(Path2dImpl path, int p) {
        super(path);
        this.p = p;
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
        return start();
    }
    
}
