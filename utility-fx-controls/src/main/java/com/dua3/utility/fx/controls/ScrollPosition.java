package com.dua3.utility.fx.controls;

/**
 * The ScrollPosition record combines the scroll positions for horizontal and vertical scrollbars.
 *
 * @param hValue the horizontal scroll value
 * @param vValue the vertical scroll value
 */
public record ScrollPosition(double hValue, double vValue) {
    public static final ScrollPosition ORIGIN = new ScrollPosition(0, 0);
}
