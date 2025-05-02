package com.dua3.utility.data;

import java.util.Arrays;

/**
 * ImageBuffer represents an implementation of the Image interface.
 * This class allows for manipulation of image data stored in a one-dimensional array.
 * Each pixel's color is encoded as an ARGB value.
 * <p>
 * The class provides methods for accessing and modifying individual pixels, drawing
 * horizontal and vertical lines, and filling rectangular areas with a specific color.
 * <p>
 * Note that premultiplied alpha is used throughout the library.
 */
public class ImageBuffer implements Image {
    private final int[] data;
    private final int width;
    private final int height;

    /**
     * Construct a new instance.
     * @param data      the pixel data to use; no copy is created, the class manipulates the data directly
     * @param width     the width in pixels
     * @param height    the height in pixels
     */
    public ImageBuffer(int[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public final int width() {
        return width;
    }

    @Override
    public final int height() {
        return height;
    }

    @Override
    public final int[] getArgb() {
        return data;
    }

    /**
     * Get the color at the given position.
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the color as ARGB value
     */
    public final int get(int x, int y) {
        assert x >= 0 && x < width && y >= 0 && y < height;
        return data[y * width + x];
    }

    /**
     * Set the color at the given position.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param argb the color as ARGB value
     */
    public final void set(int x, int y, int argb) {
        assert x >= 0 && x < width && y >= 0 && y < height;
        data[y * width + x] = argb;
    }

    /**
     * Draw a horizontal line.
     * @param x the x coordinate of the leftmost pixel
     * @param y the y coordinate
     * @param w the width of the line
     * @param argb the color as ARGB value
     */
    public final void hline(int x, int y, int w, int argb) {
        assert x >= 0 && x + w <= width && y >= 0 && y < height && w >= 0;
        Arrays.fill(data, y * width + x, y * width + x + w, argb);
    }

    /**
     * Draw a vertical line.
     * @param x the x coordinate
     * @param y the y coordinate of the topmost pixel
     * @param h the height of the line
     * @param argb the color as ARGB value
     */
    public final void vline(int x, int y, int h, int argb) {
        assert x >= 0 && x < width && y >= 0 && y + h <= height && h >= 0;
        for (int y0 = y; y0 < y + h; y0++) {
            data[y0 * width + x] = argb;
        }
    }

    /**
     * Fill the given rectangle with the given color.
     * @param x the x coordinate of the left most pixel
     * @param y the y coordinate of the topmost pixel
     * @param w the height of the rectangle
     * @param h the height of the rectangle
     * @param argb the color as ARGB value
     */
    public void fill(int x, int y, int w, int h, int argb) {
        for (int y0 = y; y0 < y + h; y0++) {
            hline(x, y0, w, argb);
        }
    }

}
