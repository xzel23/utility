package com.dua3.utility.data;

/**
 * Raster image interface.
 */
public interface Image {

    /**
     * Get image width.
     * @return the image width in pixels
     */
    int width();

    /**
     * Get image height.
     * @return the image height in pixels
     */
     int height();

    /**
     * Get image data.
     * The returned array consists of width*height integers each storing a single pixels color encoded in ARGB byte 
     * order.
     * 
     * @return the image width in pixels
     */
     int[] getArgb();

}
