package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

/**
 * End of path.
 */
public class EndPath2d extends Segment2d {

    /**
     * Segment type name.
     */
    public static final String NAME = "END_PATH";

    /**
     * Index of current node.
     */
    private final int v;

    /**
     * Constructor.
     * @param path the path
     * @param v index of vertex in path
     */
    EndPath2d(Path2dImpl path, int v) {
        super(path);
        this.v = v;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2d start() {
        return path.vertex(v);
    }

    @Override
    public Vector2d end() {
        return start();
    }
    
}
