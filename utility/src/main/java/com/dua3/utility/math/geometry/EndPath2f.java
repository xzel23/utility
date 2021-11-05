package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

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
    EndPath2f(@NotNull Path2fImpl path, int v) {
        super(path);
        this.v = v;
    }

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public @NotNull Vector2f start() {
        return path.vertex(v);
    }

    @Override
    public @NotNull Vector2f end() {
        return start();
    }
    
}
