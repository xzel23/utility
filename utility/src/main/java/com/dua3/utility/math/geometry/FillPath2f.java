package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

/**
 * Fill and stroke path.
 */
public class FillPath2f extends Segment2f {

    /**
     * Segment type name.
     */
    public static final String NAME = "FILL_PATH";

    /**
     * Index of current vertex.
     */
    private final int idx;

    /**
     * The fill rule.
     */
    private final FillRule fillRule;

    /**
     * Constructor.
     * @param path the path
     * @param idx index of current vertex
     * @param fillRule the fill rule to use
     */
    FillPath2f(@NotNull Path2fImpl path, int idx, FillRule fillRule) {
        super(path);
        this.idx = idx;
        this.fillRule = fillRule;
    }

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public Vector2f start() {
        return path.vertex(idx);
    }

    @Override
    public Vector2f end() {
        return path.vertex(idx);
    }

    /**
     * Get fill rule.
     * @return the fill rule
     */
    public FillRule fillRule() {
        return fillRule;
    }

}
