package com.dua3.utility.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
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
public interface ReadableObjectStore extends AutoCloseable {

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
     * of {@link ObjectStore.ObjectInfo} objects representing the metadata of the folder's contents.
     * <p>
     * <strong>Notes:</strong>
     * <ul>
     * <li>The caller must close the returned stream.
     * <li>Implementations must ensure that the object graph is acyclic, or define how cycles are handled.
     * </ul>
     *
     * @param path the {@code URI} of the folder or object to be listed; must not be {@link URI#isAbsolute() absolute}.
     * @return a stream of {@link ObjectStore.ObjectInfo} instances representing the objects under the specified path
     *
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the path does not point to an existing object
     * @throws NotAFolderException if the path points to a non-folder object
     * @throws IOException if an I/O error occurs while accessing the storage
     */
    Stream<ObjectStore.ObjectInfo> list(URI path) throws IOException;

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
     * Get info about the object pointed to by the provided {@link URI}.
     *
     * @param path the {@code URI} to check
     * @return An {@link Optional} containing an {@link ObjectStore.ObjectInfo} for the object at the {@code URI}, or an empty {@code Optional}
     *         if the {@code URI} does not point to an existing object.
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws IOException if an I/O error occurs while attempting to retrieve the object info.
     */
    Optional<ObjectStore.ObjectInfo> getInfo(URI path) throws IOException;

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
    default Stream<ObjectStore.ObjectInfo> walk(URI start) throws IOException {
        return walk(start, Integer.MAX_VALUE);
    }

    /**
     * Traverses a structure starting from the given {@link URI} and explores its elements up to a specified depth.
     * <p>
     * <strong>Note:</strong> The caller must close the returned stream!
     *
     * @param start the {@code URI} representing the starting point of the structure to be traversed
     * @param maxDepth the maximum depth to which the traversal should occur
     * @return a stream of {@link ObjectStore.ObjectInfo} instances representing the elements encountered during traversal
     *
     * @throws IllegalArgumentException if {@code maxDepth} is less than 0
     * @throws AbsolutePathException if the path is {@link URI#isAbsolute() absolute}
     * @throws IllegalPathException if the path points outside the root of the storage
     * @throws ObjectNotFoundException if the starting point does not point to an existing object
     * @throws IOException if an I/O error occurs during the traversal
     */
    default Stream<ObjectStore.ObjectInfo> walk(URI start, int maxDepth) throws IOException {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth < 0");
        }

        record Node(ObjectStore.ObjectInfo info, int depth) {}

        Deque<Node> stack = new ArrayDeque<>();

        Optional<ObjectStore.ObjectInfo> root = getInfo(start);
        if (root.isEmpty()) {
            throw new ObjectNotFoundException("object does not exist in the object store: " + start);
        }

        stack.push(new Node(root.get(), 0));

        Iterator<ObjectStore.ObjectInfo> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            @Override
            @SuppressWarnings("java:S2272") // NoSuchElementException is thrown in stack.pop()
            public ObjectStore.ObjectInfo next() throws NoSuchElementException, UncheckedIOException {
                Node node = stack.pop();
                ObjectStore.ObjectInfo info = node.info();

                if (info.type() == ObjectStore.ObjectType.FOLDER && node.depth() < maxDepth) {
                    try (Stream<ObjectStore.ObjectInfo> children = list(info.uri())) {
                        List<ObjectStore.ObjectInfo> entries = children.toList();

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
