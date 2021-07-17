package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public class FillAndStrokePath2d extends Segment2d {

    public static final String NAME = "FILL_AND_STROKE_PATH";

    private final int idx;
    
    FillAndStrokePath2d(Path2dImpl path, int idx) {
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

}
