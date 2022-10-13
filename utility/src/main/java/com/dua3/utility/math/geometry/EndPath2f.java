package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

/**
 * End of path.
 */
public class EndPath2f extends Segment2f {

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
    EndPath2f(Path2fImpl path, int v) {
        super(path);
        this.v = v;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2f start() {
        return path.vertex(v);
    }

    @Override
    public Vector2f end() {
        return start();
    }

}
