package com.dua3.utility.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A common interface for storable objects.
 */
public interface Storable {
    /**
     * The standard file extension for this type of object.
     *
     * @return the file extension
     */
    String defaultExtension();

    /**
     * Get the MIME type of this object.
     *
     * @return the MIME type
     */
    String mimeType();

    /**
     * Writes the object to the specified {@code OutputStream}.
     *
     * @param out the {@code OutputStream} to which the object will be written
     * @throws IOException if an I/O error occurs while writing to the stream
     */
    void write(OutputStream out) throws IOException;
}
