package com.dua3.utility.ui;

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
}
