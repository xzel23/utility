package com.dua3.utility.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a generic object storage interface for managing and interacting
 * with objects stored at specified URIs. The interface provides methods to
 * list objects, read and write data, manage folders, and perform other common
 * storage operations.
 */
public interface ObjectStore extends AutoCloseable {
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
     * Retrieve the root {@link URI} that serves as the base location for objects managed by this storage.
     * The {@code URI} returned will always be an {@link URI#isAbsolute() absolute} URI.
     *
     * @return the root {@code URI} representing the base location of the storage
     */
    URI getRoot();

    /**
     * Lists the objects located at the specified path within the storage.
     * If the path corresponds to a folder, the method will provide a stream
     * of {@link ObjectInfo} objects representing the metadata of the folder's contents.
     * <p>
     * <strong>Notes:</strong>
     * <ul>
     * <li>The caller must close the returned stream.
     * <li>Implementations must ensure that the object graph is acyclic, or define how cycles are handled.
     * </ul>
     *
     * @param path the {@code URI} of the folder or object to be listed; must not be {@link URI#isAbsolute() absolute}.
     * @return a stream of {@link ObjectInfo} instances representing the objects under the specified path
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws NotAFolderException if the path points to a non-folder object
     * @throws IOException if an I/O error occurs while accessing the storage
     */
    Stream<ObjectInfo> list(URI path) throws IOException;

    /**
     * Writes data from the specified {@link InputStream} to an object located at the specified URI path
     * within the object store. If the object already exists, its contents may be overwritten.
     *
     * @param path the {@link URI} specifying the location where the input data should be written
     * @param in   the {@link InputStream} providing the data to be written
     * @param options the {@link OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    long write(URI path, InputStream in, OutputOption... options) throws IOException;

    /**
     * Writes data from the specified {@link InputStream} to an object located at the specified URI path
     * within the object store. If the object already exists, its contents may be overwritten.
     *
     * @param path the {@link URI} specifying the location where the input data should be written
     * @param data the byte array containing the data to be written
     * @param options the {@link OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    default long write(URI path, byte[] data, OutputOption... options) throws IOException {
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
     * @param options the {@link OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link OutputOption#CREATE_NEW} is used.
     * @return the number of bytes successfully written to the object
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs during the write operation
     */
    long write(URI path, byte[] data, int from, int to, OutputOption... options) throws IOException;

    /**
     * Opens an input stream to read data from the specified URI.
     * The method returns an {@link InputStream} for reading the contents
     * of the object at the given path.
     *
     * @param path the URI of the object to be read
     * @return an {@link InputStream} to read data from the specified URI
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws IOException if the object at the path is not a data object or an I/O error occurs while opening the input stream
     */
    InputStream openInputStream(URI path) throws IOException;

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
     * @param options the {@link OutputOption} specifying how to handle existing objects at the output location;
     *                when none are present, {@link OutputOption#CREATE_NEW} is used.
     * @return an {@link OutputStream} for writing data to the specified URI
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectExistsException if the path points to an existing object and the {@link OutputOption#CREATE_NEW} option is specified
     * @throws IOException if an I/O error occurs while opening the output stream
     */
    OutputStream openOutputStream(URI path, OutputOption... options) throws IOException;

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
     * Get info about the object pointed to by the provided {@link URI}.
     *
     * @param path the {@code URI} to check
     * @return An {@link Optional} containing an {@link ObjectInfo} for the object at the {@code URI}, or an empty {@code Optional}
     *         if the {@code URI} does not point to an existing object.
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws IOException if an I/O error occurs while attempting to retrieve the object info.
     */
    Optional<ObjectInfo> getInfo(URI path) throws IOException;

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

    /**
     * Traverses a directory structure starting at the given path and returns a stream of ObjectInfo instances
     * representing the files and directories encountered during the traversal.
     * <p>
     * <strong>Note:</strong> The caller must close the returned stream!
     *
     * @param start the starting URI of the directory to traverse
     * @return a stream of ObjectInfo objects corresponding to the entries in the directory structure
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws IOException if an I/O error occurs.
     */
    default Stream<ObjectInfo> walk(URI start) throws IOException {
        return walk(start, Integer.MAX_VALUE);
    }

    /**
     * Traverses a structure starting from the given {@link URI} and explores its elements up to a specified depth.
     * <p>
     * <strong>Note:</strong> The caller must close the returned stream!
     *
     * @param start the {@code URI} representing the starting point of the structure to be traversed
     * @param maxDepth the maximum depth to which the traversal should occur
     * @return a stream of {@link ObjectInfo} instances representing the elements encountered during traversal
     *
     * @throws IllegalArgumentException if {@code maxDepth} is less than 0
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the starting point does not point to an existing object
     * @throws IOException if an I/O error occurs during the traversal
     */
    default Stream<ObjectInfo> walk(URI start, int maxDepth) throws IOException {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth < 0");
        }

        record Node(ObjectInfo info, int depth) {}

        Deque<Node> stack = new ArrayDeque<>();

        Optional<ObjectInfo> root = getInfo(start);
        if (root.isEmpty()) {
            throw new ObjectNotFoundException("object does not exist in the object store: " + start);
        }

        stack.push(new Node(root.get(), 0));

        Iterator<ObjectInfo> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            @SuppressWarnings("java:S2272") // NoSuchElementException is thrown in stack.pop()
            public ObjectInfo next() throws NoSuchElementException, UncheckedIOException {
                Node node = stack.pop();
                ObjectInfo info = node.info();

                if (info.type() == ObjectType.FOLDER && node.depth() < maxDepth) {
                    try (Stream<ObjectInfo> children = list(info.uri())) {
                        List<ObjectInfo> entries = children.toList();

                        // Reverse so traversal order matches list()
                        for (int i = entries.size() - 1; i >= 0; i--) {
                            stack.push(new Node(entries.get(i), node.depth() + 1));
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                return info;
            }
        };

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.NONNULL),
                false
        );
    }
}
