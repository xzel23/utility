package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

/**
 * Set Clip path.
 * <p>
 * This segment does not add any vertices to the path. It is a command that indicates that the current
 * path should be used as the clipping region for subsequent drawing calls.
 */
public class ClipPath2f extends Segment2f {

    /**
     * Name of segment type.
     */
    public static final String NAME = "CLIP_PATH";

    private final int idx;
    private final FillRule fillRule;

    /**
     * Constructor.
     * @param path the path instance
     * @param idx index of the current node
     * @param fillRule the {@link FillRule} to use
     */
    ClipPath2f(@NotNull Path2fImpl path, int idx, @NotNull FillRule fillRule) {
        super(path);
        this.idx = idx;
        this.fillRule = fillRule;
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

    /**
     * Get the {@link FillRule}.
     * @return the fill rule
     */
    public @NotNull FillRule fillRule() {
        return fillRule;
    }

}
