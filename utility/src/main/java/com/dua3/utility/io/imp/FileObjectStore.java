package com.dua3.utility.io.imp;

import com.dua3.utility.io.AbsolutePathException;
import com.dua3.utility.io.FolderNotEmptyException;
import com.dua3.utility.io.IllegalPathException;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.NotAFolderException;
import com.dua3.utility.io.ObjectExistsException;
import com.dua3.utility.io.ObjectNotFoundException;
import com.dua3.utility.io.ObjectStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A file-based implementation of the {@code ObjectStore} interface, which allows
 * for storing and managing objects (files and folders) within a predetermined root directory.
 * This implementation ensures path validation and enforces that all operations remain
 * confined to the root directory of the store.
 * <p>
 * This class is immutable and thread-safe, meaning its methods can be called safely
 * by multiple threads concurrently.
 */
public final class FileObjectStore implements ObjectStore {

    private final Path root;
    private final URI rootUri;

    /**
     * Constructs a {@code FileObjectStore} with the specified root directory.
     * It normalizes and ensures the creation of the root directory as a valid absolute path.
     *
     * @param root the path to the root directory of the file object store; must not be {@code null}
     * @throws IOException if an I/O error occurs while creating or accessing the directory
     */
    public FileObjectStore(Path root) throws IOException {
        this.root = Files.createDirectories(root).toAbsolutePath().normalize();
        this.rootUri = this.root.toUri();
    }

    @Override
    public URI getRoot() {
        return rootUri;
    }

    @Override
    @SuppressWarnings({"java:S2095", "resource"}) // caller closes the stream
    public Stream<ObjectInfo> list(URI path) throws IOException {
        Path resolved = resolve(path);

        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new ObjectNotFoundException(String.valueOf(path));
        }
        if (!Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new NotAFolderException(String.valueOf(path));
        }

        try {
            return Files.list(resolved)
                    .sorted(Comparator.comparing(Path::getFileName, Comparator.comparing(Path::toString)))
                    .map(this::toObjectInfoUnchecked);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public long write(URI path, InputStream in, OutputOption... options) throws IOException {
        Path resolved = resolve(path);
        OutputOption effectiveOption = ensureCanWrite(resolved, options);
        Path parent = resolved.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        StandardCopyOption[] copyOptions = effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[]{};
        return Files.copy(in, resolved, copyOptions);
    }

    @Override
    public long write(URI path, byte[] data, int from, int to, OutputOption... options) throws IOException {
        int length = to - from;
        if (from < 0 || to < from || to > data.length) {
            throw new IndexOutOfBoundsException("invalid bounds: from=" + from + ", to=" + to + ", length=" + data.length);
        }
        try (OutputStream out = openOutputStream(path, options)) {
            out.write(data, from, length);
        }
        return length;
    }

    @Override
    public InputStream openInputStream(URI path) throws IOException {
        Path resolved = resolve(path);
        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new ObjectNotFoundException(String.valueOf(path));
        }
        if (!Files.isRegularFile(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("not a data object: " + path);
        }
        return Files.newInputStream(resolved, StandardOpenOption.READ);
    }

    @Override
    public OutputStream openOutputStream(URI path, OutputOption... options) throws IOException {
        Path resolved = resolve(path);
        OutputOption effectiveOption = ensureCanWrite(resolved, options);
        Path parent = resolved.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        OpenOption[] soo = effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
                : new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
        return Files.newOutputStream(resolved, soo);
    }

    @Override
    public void createFolder(URI path) throws IOException {
        Path resolved = resolve(path);
        if (Files.exists(resolved, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new NotAFolderException(String.valueOf(path));
        }
        Files.createDirectories(resolved);
    }

    @Override
    public void removeFolder(URI path) throws IOException {
        Path resolved = resolve(path);
        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new ObjectNotFoundException(String.valueOf(path));
        }
        if (!Files.isDirectory(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new NotAFolderException(String.valueOf(path));
        }
        try {
            Files.delete(resolved);
        } catch (DirectoryNotEmptyException e) {
            throw new FolderNotEmptyException(String.valueOf(path), e);
        }
    }

    @Override
    public Optional<ObjectInfo> getInfo(URI path) throws IOException {
        Path resolved = resolve(path);
        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            return Optional.empty();
        }
        return Optional.of(toObjectInfo(resolved));
    }

    @Override
    public void delete(URI path) throws IOException {
        Path resolved = resolve(path);
        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new ObjectNotFoundException(String.valueOf(path));
        }
        try {
            Files.delete(resolved);
        } catch (NoSuchFileException e) {
            throw new ObjectNotFoundException(String.valueOf(path), e);
        }
    }

    @Override
    public void deleteRecursively(URI path) throws IOException {
        Path resolved = resolve(path);

        if (Files.notExists(resolved, LinkOption.NOFOLLOW_LINKS)) {
            throw new ObjectNotFoundException(String.valueOf(path));
        }

        try (Stream<Path> stream = Files.walk(resolved)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public void close() {
        // nothing to close
    }

    /**
     * Resolves the given URI to a {@code Path} object relative to the root directory of the file object store.
     * This method ensures that the resolved path is within the bounds of the root directory and does not allow
     * absolute URIs or paths that escape the root.
     *
     * @param path the URI to be resolved; must not be absolute and must represent a path relative to the root
     * @return the resolved {@code Path}, normalized and validated to lie within the root directory
     * @throws IllegalPathException if the provided URI is invalid, absolute, or resolves to a path outside the root
     * @throws AbsolutePathException if the provided URI is absolute
     */
    private Path resolve(URI path) throws IllegalPathException {
        if (path.isAbsolute()) {
            throw new AbsolutePathException("absolute path not allowed: " + path);
        }

        Path relative;
        try {
            relative = Path.of(path.getPath() == null ? "" : path.getPath());
        } catch (RuntimeException e) {
            throw new IllegalPathException("invalid path: " + path, e);
        }

        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalPathException("path points outside root: " + path);
        }
        return resolved;
    }

    /**
     * Ensures that the specified path is suitable for writing operations, taking into account
     * the provided output options. This method checks for the existence and type of the file
     * at the specified path and throws exceptions if writing is not allowed.
     *
     * @param path    the path to the file or directory to be checked for write operations; must not be null
     * @param options output options specifying the desired behavior for file creation or replacement
     * @return the effective output option to be used for writing operations
     *
     * @throws IllegalArgumentException if multiple incompatible output options are specified
     * @throws IOException           if the specified path is a directory or another I/O error occurs
     * @throws ObjectExistsException if the specified path exists and the output option is {@code OutputOption.CREATE_NEW}
     *
     */
    private OutputOption ensureCanWrite(Path path, OutputOption... options) throws IOException {
        Set<OutputOption> optionSet = Set.of(options);
        OutputOption outputOption = switch (optionSet.size()) {
            case 0 -> OutputOption.CREATE_NEW;
            case 1 -> optionSet.iterator().next();
            default -> throw new IllegalArgumentException("multiple incompatible output options specified: " + Arrays.toString(options));
        };

        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && outputOption == OutputOption.CREATE_NEW) {
            throw new ObjectExistsException(path.toString());
        }
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("cannot write to folder: " + path);
        }

        return outputOption;
    }

    /**
     * Converts the specified path into an {@link ObjectInfo}, extracting metadata
     * such as creation time, last modified time, size, type, and relative URI.
     *
     * @param path the {@link Path} representing the file or directory; must not be null
     * @return an {@link ObjectInfo} containing the metadata of the specified path
     * @throws IOException if an I/O error occurs while reading file attributes
     */
    private ObjectInfo toObjectInfo(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        Path relativePath = root.relativize(path);
        String normalized = IoUtil.toUnixPath(relativePath);
        if (attributes.isDirectory() && !normalized.isEmpty() && !normalized.endsWith("/")) {
            normalized += "/";
        }
        URI uri = URI.create(normalized);
        return new ObjectInfo(
                uri,
                attributes.isDirectory() ? ObjectType.FOLDER : ObjectType.DATA,
                attributes.isDirectory() ? ObjectInfo.UNKNOWN_SIZE : attributes.size(),
                attributes.creationTime().toInstant(),
                attributes.lastModifiedTime().toInstant()
        );
    }

    /**
     * Converts the specified {@link Path} into an {@link ObjectInfo} without enforcing
     * the checked exception handling required for I/O operations. This method wraps
     * any {@link IOException} that occurs during the conversion into an {@link UncheckedIOException}.
     *
     * @param path the {@link Path} representing the file or directory; must not be null
     * @return an {@link ObjectInfo} containing the metadata of the specified path
     * @throws UncheckedIOException if an I/O error occurs while reading file attributes
     */
    private ObjectInfo toObjectInfoUnchecked(Path path) throws UncheckedIOException {
        try {
            return toObjectInfo(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}