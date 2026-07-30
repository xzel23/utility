package com.dua3.utility.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
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
     * Closes this store and releases its resources.
     *
     * @throws IOException if the store cannot be closed
     */
    @Override
    void close() throws IOException;

    /**
     * Retrieve the root {@link URI} that serves as the base location for objects managed by this storage.
     * The {@code URI} returned will always be an {@link URI#isAbsolute() absolute} URI.
     *
     * @return the root {@code URI} representing the base location of the storage
     */
    URI getRoot();

    /**
     * Returns a readable view rooted at a folder in this store.
     * <p>
     * Paths passed to the returned store are resolved relative to {@code prefix};
     * paths returned by the view are relative to that same folder. Closing the
     * view does not close this store.
     *
     * @param prefix the relative folder used as the root of the returned view
     * @return a readable store view rooted at {@code prefix}
     * @throws IllegalArgumentException if {@code prefix} is not a valid relative folder path
     */
    default ReadableObjectStore prefixed(URI prefix) {
        return PrefixedObjectStores.readable(this, prefix);
    }

    /**
     * Returns the objects matching a glob pattern relative to this store's root.
     * <p>
     * Pattern matching uses the {@code glob:} syntax defined by {@link PathMatcher}. The pattern is
     * matched against each object's serialized relative URI. Therefore, reserved characters and literal
     * glob characters in object names must be percent-encoded.
     * <p>
     * The returned stream can contain both folders and data objects. The caller must close it after use.
     *
     * @param pattern the relative glob pattern
     * @return a stream of root-relative URIs matching {@code pattern}
     * @throws IllegalPathException if the pattern is not a valid relative path in this store
     * @throws IOException if an I/O error occurs while accessing the store
     */
    default Stream<URI> glob(String pattern) throws IOException {
        return glob(URI.create(""), pattern);
    }

    /**
     * Returns the objects matching a glob pattern relative to a path in this store.
     * <p>
     * Pattern matching uses the {@code glob:} syntax defined by {@link PathMatcher}. The pattern is
     * matched against each object's serialized relative URI. Therefore, reserved characters and literal
     * glob characters in object names must be percent-encoded.
     * <p>
     * The returned URIs remain relative to this store's root, rather than to {@code relativeUri}. The
     * returned stream can contain both folders and data objects. The caller must close it after use.
     *
     * @param relativeUri the relative path used as the search base
     * @param pattern the glob pattern relative to {@code relativeUri}
     * @return a stream of root-relative URIs matching {@code pattern}
     * @throws AbsolutePathException if {@code relativeUri} is absolute
     * @throws IllegalPathException if {@code relativeUri} or {@code pattern} does not identify a path within this store
     * @throws IOException if an I/O error occurs while accessing the store
     */
    default Stream<URI> glob(URI relativeUri, String pattern) throws IOException {
        GlobAdapter<URI> adapter = new GlobAdapter<>(
                "/",
                ReadableObjectStore::resolveGlobPath,
                uri -> getInfo(uri).isPresent(),
                uri -> walk(uri).map(ObjectStore.ObjectInfo::uri),
                ReadableObjectStore::uriGlobMatcher,
                (ignored, uri) -> uri
        );
        return new Glob<>(adapter).glob(relativeUri, pattern);
    }

    private static URI resolveGlobPath(URI base, String path) throws IOException {
        URI normalizedBase = validateRelativeGlobUri(base, "glob base");
        if (path.startsWith("/")) {
            throw new IllegalPathException("glob pattern must be relative: " + path);
        }

        URI relative;
        try {
            relative = URI.create(path);
        } catch (IllegalArgumentException e) {
            throw new IllegalPathException("invalid glob pattern: " + path, e);
        }
        if (relative.isAbsolute() || relative.getRawAuthority() != null || relative.getQuery() != null
                || relative.getFragment() != null || relative.getPath() == null || relative.getPath().startsWith("/")) {
            throw new IllegalPathException("glob pattern must be a relative URI path: " + path);
        }

        if (path.isEmpty()) {
            return normalizedBase;
        }

        String baseText = normalizedBase.toString();
        URI folderBase = baseText.isEmpty() || baseText.endsWith("/") ? normalizedBase : URI.create(baseText + "/");
        return validateRelativeGlobUri(folderBase.resolve(relative), "glob pattern");
    }

    private static URI validateRelativeGlobUri(URI uri, String description) throws IllegalPathException {
        if (uri.isAbsolute()) {
            throw new AbsolutePathException("absolute path not allowed: " + uri);
        }

        URI normalized = uri.normalize();
        String path = normalized.getPath();
        if (normalized.getRawAuthority() != null || normalized.getQuery() != null || normalized.getFragment() != null
                || path == null || path.startsWith("/") || path.equals("..") || path.startsWith("../")) {
            throw new IllegalPathException(description + " must identify a path within the object store: " + uri);
        }
        return normalized;
    }

    private static Predicate<URI> uriGlobMatcher(URI fixedBase, String globPart) {
        String fixedPath = fixedBase.toString();
        String globPattern = fixedPath.isEmpty() ? globPart.substring(1) : fixedPath + globPart;

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        return uri -> matcher.matches(Path.of(uri.toString()));
    }

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

    /**
     * Reads all lines from a file at the specified path using UTF-8 encoding.
     *
     * @param path the URI of the file to read
     * @return a Stream of lines from the file
     * @throws IOException if an I/O error occurs opening the file
     */
    default Stream<String> lines(URI path) throws IOException {
        return lines(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads all lines from a file at the given URI using the specified character set.
     * The method opens an input stream to the file, utilizes a BufferedReader to read the lines,
     * and returns them as a Stream of Strings.
     *
     * <p><strong>Note:</strong> The caller must close the stream!
     *
     * @param path the URI of the file to be read
     * @param cs the Charset to use for decoding the file
     * @return a Stream of Strings, each representing a line in the file
     * @throws IOException if an I/O error occurs while opening the input stream or reading the file
     */
    default Stream<String> lines(URI path, Charset cs) throws IOException {
        InputStream in = openInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, cs));
        return reader.lines().onClose(() -> {
            try {
                IoUtil.closeAll(reader, in);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Transfers data from the specified URI to the given {@link OutputStream}.
     * This method opens an input stream to read from the URI and transfers
     * the data to the provided output stream.
     *
     * @param path the {@link URI} of the object to be transferred
     * @param out the {@link OutputStream} to which data should be written
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs while reading from the URI or writing to the output stream
     */
    default long transferTo(URI path, OutputStream out) throws IOException {
        try (InputStream in = openInputStream(path)) {
            return in.transferTo(out);
        }
    }

    /**
     * Reads all bytes from a URI and returns them in a byte array.
     *
     * @param path the URI of the object to be read
     * @return a byte array containing all the bytes from the specified URI
     * @throws IOException if an I/O error occurs while reading from the URI
     */
    default byte[] readAllBytes(URI path) throws IOException {
        try (InputStream in = openInputStream(path)) {
            return in.readAllBytes();
        }
    }

    /**
     * Reads the content of a file located at the specified URI into a String using UTF-8 encoding.
     *
     * @param path the URI of the file to be read
     * @return the content of the file as a String
     * @throws IOException if an I/O error occurs while reading the file
     */
    default String readString(URI path) throws IOException {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads the content of the specified URI as a string using the given character set.
     *
     * @param path the URI of the data object to be read
     * @param cs the character set to be used for decoding the bytes into characters
     * @return a String representing the content of the specified URI
     * @throws IOException if an I/O error occurs while reading from the URI
     */
    default String readString(URI path, Charset cs) throws IOException {
        return new String(readAllBytes(path), cs);
    }
}
