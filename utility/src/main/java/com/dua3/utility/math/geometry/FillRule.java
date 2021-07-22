package com.dua3.utility.math.geometry;

/**
 * Fill rule.
 * <p>
 * The fill rule defines how to determine if a point is considered inside or outside the path.
 * Filling a path will paint pixels according to the fill rule.
 */
public enum FillRule {
    /**
     * Even/odd rule: A point is considered inside the path if a ray pointing away from the point
     * intersects an even number of times with the path, and outside otherwise.
     */
    EVEN_ODD,

    /**
     * Non-zero rule: A point is considered inside the path if a ray pointing away from the point
     * intersects at least once with the path, and outside otherwise.
     */
    NON_ZERO
}
