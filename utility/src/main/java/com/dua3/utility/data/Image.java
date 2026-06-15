package com.dua3.utility.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Raster image interface.
 */
public interface Image {

    /**
     * Get image width.
     *
     * @return the image width in pixels
     */
    int width();

    /**
     * Get image height.
     *
     * @return the image height in pixels
     */
    int height();

    /**
     * Get image data.
     * The returned array consists of width*height integers each storing a single pixels color encoded in ARGB byte
     * order using premultiplied alpha.
     *
     * @return the image width in pixels
     */
    int[] getArgb();

    /**
     * Provides the default file extension for images handled by this implementation.
     *
     * @return the default file extension, which is "png"
     */
    default String defaultExtension() {
        return "png";
    }

    /**
     * Returns the MIME type of the image.
     *
     * @return the MIME type as a String, defaulting to "image/png"
     */
    default String mimeType() {
        return "image/png";
    }

    /**
     * Writes the current image to the specified output stream using its MIME type.
     *
     * @param out the output stream to write the image data to; must not be null
     * @throws IOException if an I/O error occurs during the writing process
     */
    default void write(OutputStream out) throws IOException {
        ImageUtil.getInstance().write(this, out, mimeType());
    }
}
