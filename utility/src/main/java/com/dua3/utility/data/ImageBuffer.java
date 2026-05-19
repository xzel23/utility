package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

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
 *
 * @param data   the pixel data to use; no copy is created, the class manipulates the data directly
 * @param width  the width in pixels
 * @param height the height in pixels
 */
public record ImageBuffer(int[] data, int width, int height) implements Image {

    @Override
    public int[] getArgb() {
        return data;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return (o instanceof ImageBuffer(int[] data1, int width1, int height1))
                && width == width1 && height == height1 && Arrays.equals(data, data1);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + width;
        result = 17 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return "ImageBuffer[width=" + width + ", height=" + height + ", data=" + Arrays.toString(data) + "]";
    }

    /**
     * Retrieves the integer value from the internal data array at the specified
     * x and y coordinates without bounds checking.
     *
     * @param x the x coordinate of the desired pixel
     * @param y the y coordinate of the desired pixel
     * @return the integer value from the data array at the specified coordinates
     */
    private int getUnchecked(int x, int y) {
        return data[y * width + x];
    }

    /**
     * Sets the ARGB color value of the pixel at the specified coordinates in the internal data array
     * without performing bounds checking.
     *
     * @param x    the x coordinate of the pixel
     * @param y    the y coordinate of the pixel
     * @param argb the color value in ARGB format to be set at the specified pixel
     */
    private void setUnchecked(int x, int y, int argb) {
        data[y * width + x] = argb;
    }

    /**
     * Draw a horizontal line.
     *
     * @param x    the x coordinate of the leftmost pixel
     * @param y    the y coordinate
     * @param w    the width of the line
     * @param argb the color as ARGB value
     */
    private void hlineUnchecked(int x, int y, int w, int argb) {
        Arrays.fill(data, y * width + x, y * width + x + w, argb);
    }

    /**
     * Draw a vertical line.
     *
     * @param x    the x coordinate
     * @param y    the y coordinate of the topmost pixel
     * @param h    the height of the line
     * @param argb the color as ARGB value
     */
    private void vlineUnchecked(int x, int y, int h, int argb) {
        for (int y0 = y; y0 < y + h; y0++) {
            setUnchecked(x, y0, argb);
        }
    }

    /**
     * Get the color at the given position.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the color as ARGB value
     */
    public int get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        return getUnchecked(x, y);
    }

    /**
     * Set the color at the given position.
     *
     * @param x    the x coordinate
     * @param y    the y coordinate
     * @param argb the color as ARGB value
     */
    public void set(int x, int y, int argb) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        setUnchecked(x, y, argb);
    }

    /**
     * Draw a horizontal line.
     *
     * @param x    the x coordinate of the leftmost pixel
     * @param y    the y coordinate
     * @param w    the width of the line
     * @param argb the color as ARGB value
     */
    public void hline(int x, int y, int w, int argb) {
        if (x < 0 || x + w >= width || y < 0 || y >= height || w < 0) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        hlineUnchecked(x, y, w, argb);
    }

    /**
     * Draw a vertical line.
     *
     * @param x    the x coordinate
     * @param y    the y coordinate of the topmost pixel
     * @param h    the height of the line
     * @param argb the color as ARGB value
     */
    public void vline(int x, int y, int h, int argb) {
        if (x < 0 || x >= width || y < 0 || y + h >= height || h < 0) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        vlineUnchecked(x, y, h, argb);
    }

    /**
     * Fill the given rectangle with the given color.
     *
     * @param x    the x coordinate of the left most pixel
     * @param y    the y coordinate of the topmost pixel
     * @param w    the width of the rectangle
     * @param h    the height of the rectangle
     * @param argb the color as ARGB value
     */
    public void fill(int x, int y, int w, int h, int argb) {
        if (x < 0 || y < 0 || w < 0 || h < 0 || x + w >= width || y + h >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        for (int y0 = y; y0 < y + h; y0++) {
            hline(x, y0, w, argb);
        }
    }

    /**
     * Draws the outline of a rectangle at the specified position with the specified dimensions and color.
     *
     * @param x    the x coordinate of the top-left corner of the rectangle
     * @param y    the y coordinate of the top-left corner of the rectangle
     * @param w    the width of the rectangle
     * @param h    the height of the rectangle
     * @param argb the color of the rectangle edges as an ARGB value
     */
    public void rect(int x, int y, int w, int h, int argb) {
        if (x < 0 || y < 0 || w < 0 || h < 0 || x + w >= width || y + h >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
        }
        if (w > 0) {
            hlineUnchecked(x, y, w, argb);
            if (h > 1) {
                hlineUnchecked(x, y + h - 1, w, argb);
            }
            if (h > 2) {
                vlineUnchecked(x, y + 1, h - 2, argb);
                if (w > 1) {
                    vlineUnchecked(x + w - 1, y + 1, h - 2, argb);
                }
            }
        }
    }

}
