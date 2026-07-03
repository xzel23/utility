package com.dua3.utility.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Represents a generic object storage interface for managing and interacting
 * with objects stored at specified URIs. The interface provides methods to
 * list objects, read and write data, manage folders, and perform other common
 * storage operations.
 */
public interface WritableObjectStore extends AutoCloseable {

    /**
     * Retrieve the root {@link URI} that serves as the base location for objects managed by this storage.
     * The {@code URI} returned will always be an {@link URI#isAbsolute() absolute} URI.
     *
     * @return the root {@code URI} representing the base location of the storage
     */
    URI getRoot();

    /**
     * Writes data from the specified {@link InputStream} to an object located at the specified URI path
     * within the object store. If the object already exists, its contents may be overwritten.
     *
     * @param path the {@link URI} specifying the location where the input data should be written
     * @param in   the {@link InputStream} providing the data to be written
     * @param options the {@link ObjectStore.ObjectInfo} specifying how to handle existing objects at the output location;
     *                when none are present, {@link ObjectStore.OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link ObjectStore.OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    long write(URI path, InputStream in, ObjectStore.OutputOption... options) throws IOException;

    /**
     * Writes data from the specified {@link InputStream} to an object located at the specified URI path
     * within the object store. If the object already exists, its contents may be overwritten.
     *
     * @param path the {@link URI} specifying the location where the input data should be written
     * @param data the byte array containing the data to be written
     * @param options the {@link ObjectStore.OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link ObjectStore.OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link ObjectStore.OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    default long write(URI path, byte[] data, ObjectStore.OutputOption... options) throws IOException {
        return write(path, data, 0, data.length);
    }

    /**
     * Writes data from the specified {@link InputStream} to an object located at the specified URI path
     * within the object store. If the object already exists, its contents may be overwritten.
     *
     * @param path the {@link URI} specifying the location where the input data should be written
     * @param data the byte array containing the data to be written
     * @param from the starting index in the data array to be written (inclusive)
     * @param to   the ending index in the data array to be written (exclusive)
     * @param options the {@link ObjectStore.OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link ObjectStore.OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link ObjectStore.OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    long write(URI path, byte[] data, int from, int to, ObjectStore.OutputOption... options) throws IOException;

    /**
     * Opens an output stream to write data to the specified URI.
     * This method provides an {@link OutputStream} for writing data
     * to the object located at the specified path. If the object
     * already exists, its contents may be overwritten.
     * <p>
     * If any I/O error occurs while opening the output stream, the operation aborts
     * and the store may be partially modified.
     *
     * @param path the {@link URI} specifying the location where the output data will be written
     * @param options the {@link ObjectStore.OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link ObjectStore.OutputOption#CREATE_NEW} is used.
     * @return an {@link OutputStream} for writing data to the specified URI
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link ObjectStore.OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs while opening the output stream
     */
    OutputStream openOutputStream(URI path, ObjectStore.OutputOption... options) throws IOException;

    /**
     * Creates a folder at the specified URI path within the object store.
     * If the folder already exists, this method does nothing. Missing parent folders are created as needed.
     * If a non-folder object already exists at the specified path, an exception is thrown.
     *
     * @param path the URI representing the path where the folder will be created
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws NotAFolderException if the path or any missing parent points to an existing non-folder object
     * @throws IOException if an I/O error occurs while creating the folder
     */
    void createFolder(URI path) throws IOException;
}
