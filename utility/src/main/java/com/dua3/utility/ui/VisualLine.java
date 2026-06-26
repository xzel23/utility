package com.dua3.utility.ui;

import java.util.Arrays;

/**
 * Visual line in source-coordinate space.
 *
 * @param start      source start (inclusive)
 * @param end        source end (inclusive, caret-right boundary)
 * @param top        line top in pixels
 * @param height     line height in pixels
 * @param boundaries x boundaries for caret positions in {@code [start, end]}
 */
public record VisualLine(int start, int end, double top, double height, double[] boundaries) {
    /**
     * Constructor.
     *
     * @param start      line start
     * @param end        line end
     * @param top        line top
     * @param height     line height
     * @param boundaries x boundaries
     */
    public VisualLine {
        boundaries = boundaries.clone();
    }

    /**
     * Number of source characters on this line.
     *
     * @return line length
     */
    public int length() {
        return Math.max(0, end - start);
    }

    /**
     * Left-most x coordinate for this line.
     *
     * @return minimum x
     */
    public double minX() {
        return boundaries.length == 0 ? 0.0 : boundaries[0];
    }

    /**
     * Right-most x coordinate for this line.
     *
     * @return maximum x
     */
    public double maxX() {
        return boundaries.length == 0 ? 0.0 : boundaries[boundaries.length - 1];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VisualLine that)) return false;

        return start == that.start &&
                end == that.end &&
                Double.compare(that.top, top) == 0 &&
                Double.compare(that.height, height) == 0 &&
                Arrays.equals(boundaries, that.boundaries);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(start);
        result = 31 * result + Integer.hashCode(end);
        result = 31 * result + Double.hashCode(top);
        result = 31 * result + Double.hashCode(height);
        result = 31 * result + Arrays.hashCode(boundaries);
        return result;
    }
}
