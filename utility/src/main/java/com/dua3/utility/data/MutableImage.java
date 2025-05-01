package com.dua3.utility.data;

/**
 * Represents an image that can be modified by directly accessing and manipulating pixel data.
 * This interface extends the {@link Image} interface to provide writable functionality.
 */
public interface MutableImage extends Image {
    /**
     * Retrieves the buffer that holds the pixel data of the writable image.
     *
     * @return the {@code ImageBuffer} instance containing pixel data, along with width, height,
     *         and access methods to read or modify pixel values.
     */
    ImageBuffer getBuffer();
}
