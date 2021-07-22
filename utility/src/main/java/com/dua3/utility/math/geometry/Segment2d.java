package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

/**
 * Base class for segments that form a {@link Path2d}.
 */
public abstract class Segment2d {
    protected final Path2dImpl path;
    
    protected Segment2d(Path2dImpl path) {
        assert path != null;
        this.path = path;
    }

    /**
     * The identifying name for the type of the segment.
     * @return name of the segment
     */
    public abstract String name();

    /**
     * The segment's starting point.
     * @return the start of the segment 
     */
    public abstract Vector2d start();

    /**
     * The segment's end point.
     * @return the end of the segment 
     */
    public abstract Vector2d end();
    
    protected String vertexToString(int idx) {
        Vector2d v = path.vertex(idx);
        return String.format("[%d](%f,%f)", idx, v.x(), v.y());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
