package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

/**
 * Move current position.
 * <p>
 * <strong>NOTE: </strong> Only valid at start of path.
 */
public class MoveTo2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "MOVE_TO";

    /**
     * Index of current point.
     */
    private final int idx;

    /**
     * Constructor.
     * @param path the path
     * @param idx index of current point
     */
    public MoveTo2f(@NotNull Path2fImpl path, int idx) {
        super(path);
        this.idx = idx;
    }

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public @NotNull Vector2f start() {
        return path.vertex(idx);
    }

    @Override
    public @NotNull Vector2f end() {
        return path.vertex(idx);
    }

    @Override
    public @NotNull String toString() {
        return "MoveTo2f{" +
               vertexToString(idx) +
               '}';
    }
}
