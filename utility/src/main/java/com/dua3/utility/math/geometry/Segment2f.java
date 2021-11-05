package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for segments that form a {@link Path2f}.
 */
public abstract class Segment2f {
    protected final @NotNull Path2fImpl path;
    
    protected Segment2f(@NotNull Path2fImpl path) {
        assert path != null;
        this.path = path;
    }

    /**
     * The identifying name for the type of the segment.
     * @return name of the segment
     */
    public abstract @NotNull String name();

    /**
     * The segment's starting point.
     * @return the start of the segment 
     */
    public abstract @NotNull Vector2f start();

    /**
     * The segment's end point.
     * @return the end of the segment 
     */
    public abstract @NotNull Vector2f end();
    
    protected @NotNull String vertexToString(int idx) {
        Vector2f v = path.vertex(idx);
        return String.format("[%d](%f,%f)", idx, v.x(), v.y());
    }

    @Override
    public @NotNull String toString() {
        return getClass().getSimpleName();
    }
}
