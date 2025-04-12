package com.dua3.utility.fx.controls;

/**
 * The ScrollPosition record combines the scroll positions for horizontal and vertical scrollbars.
 *
 * @param hValue the horizontal scroll value
 * @param vValue the vertical scroll value
 */
public record ScrollPosition(double hValue, double vValue) {
    /**
     * A constant {@code ScrollPosition} instance representing the origin point (0, 0),
     * where both the horizontal and vertical scroll values are set to 0.
     */
    public static final ScrollPosition ORIGIN = new ScrollPosition(0, 0);
}
