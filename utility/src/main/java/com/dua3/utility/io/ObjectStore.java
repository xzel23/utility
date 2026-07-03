package com.dua3.utility.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Represents a generic object storage interface for managing and interacting
 * with objects stored at specified URIs. The interface provides methods to
 * list objects, read and write data, manage folders, and perform other common
 * storage operations.
 */
public interface ObjectStore extends ReadableObjectStore, WritableObjectStore {
    /**
     * Enumeration representing the type of objects.
     */
    enum ObjectType {
        /**
         * Represents a folder object type.
         */
        FOLDER,
        /**
         * Represents a data object type.
         */
        DATA
    }

    /**
     * Specifies options for writing data to a specified output location.
     * These options control the behavior when writing to a location that may or may not already contain an object.
     */
    enum OutputOption {
        /**
         * Fail if an object already exists at the output location, otherwise create a new one.
         */
        CREATE_NEW,
        /**
         * Create a new object if one does not already exist at the output location, otherwise replace it.
         */
        CREATE_OR_REPLACE
    }

    /**
     * A record representing metadata information about an object in an {@code ObjectStore}.
     * This record provides details such as the object's URI, size, and whether it is a folder.
     *
     * @param uri         the {@link URI} of the object relative to the root of the {@code ObjectStore}
     * @param type        the type of the object
     * @param size        for data objects the size of the object in bytes or {@link #UNKNOWN_SIZE} if unknown;
     *                    for folders, implementation defined for folders
     * @param created     the timestamp when the object was created
     * @param lastModified the timestamp when the object was last modified
     */
    record ObjectInfo(URI uri, ObjectType type, long size, Instant created, Instant lastModified) {
        /**
         * A constant representing an unknown or unspecified size.
         */
        public static  final long UNKNOWN_SIZE = -1L;
    }

    /**
     * Removes the specified folder from the object store. Fails if the folder is not empty.
     *
     * @param path the {@link URI} representing the path of the folder to be removed;
     *             must not be an absolute URI and must point within the root of the storage.
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws NotAFolderException if the path does not point to a folder
     * @throws IOException if an I/O error occurs during the removal process.
     */
    void removeFolder(URI path) throws IOException;

    /**
     * Deletes the object or folder located at the specified {@link URI} path within the object store.
     * If the specified path corresponds to a non-empty folder, an {@link IOException} is thrown.
     *
     * @param path the {@link URI} representing the location of the object or folder to be deleted
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws IOException if an I/O error occurs while performing the delete operation
     */
    void delete(URI path) throws IOException;

    /**
     * Deletes an object at the specified path recursively.
     * If the path refers to a folder, all its contents, including subfolders,
     * are deleted. If the path refers to a file, the file is deleted.
     * If any deletion fails, the operation aborts and the store may be partially modified.
     *
     * @param path the {@link URI} representing the location of the object or folder to be deleted
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws IOException if an I/O error occurs while deleting the contents
     */
    default void deleteRecursively(URI path) throws IOException {
        switch (getInfo(path).map(ObjectInfo::type).orElse(null)) {
            case null -> throw new ObjectNotFoundException(String.valueOf(path));
            case DATA -> delete(path);
            case FOLDER -> {
                try (Stream<ObjectInfo> children = list(path)) {
                    children.forEach(child -> {
                        try {
                            if (child.type() == ObjectType.FOLDER) {
                                deleteRecursively(child.uri());
                            } else {
                                delete(child.uri());
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
            }
        }
    }

}
