package com.dua3.utility.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface that represents a storable image capable of retaining its source data as bytes.
 * <p>
 * This interface extends {@link Image} and provides a method to access the original data
 * of the image in byte array form. It overrides the {@code write} method to write the retained
 * source bytes directly to an output stream.
 */
public interface DataRetainingImage extends Image {

    /**
     * Retrieves the unchanged source data of the storable image.
     *
     * @return a byte array containing the original source data of the image
     */
    Object source();

    /**
     * Writes the source bytes of the image to the specified {@link OutputStream}.
     *
     * @param out the {@link OutputStream} to which the source bytes will be written
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    @Override
    default void write(OutputStream out) throws IOException {
        out.write((byte[]) source());
    }

}
