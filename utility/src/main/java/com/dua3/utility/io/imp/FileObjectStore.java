package com.dua3.utility.io.imp;

import com.dua3.utility.io.AbsolutePathException;
import com.dua3.utility.io.FolderNotEmptyException;
import com.dua3.utility.io.IllegalPathException;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.NotAFolderException;
import com.dua3.utility.io.ObjectExistsException;
import com.dua3.utility.io.ObjectNotFoundException;
import com.dua3.utility.io.ObjectStore;
import com.dua3.utility.io.ReadableObjectStore;
import com.dua3.utility.io.WritableObjectStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
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
 * <p>
 * <strong>Note:</strong> This implementation does not support symbolic links.
 */
public final class FileObjectStore implements ObjectStore {
    private static final Logger LOG = LogManager.getLogger(FileObjectStore.class);

    private static final StandardCopyOption[] EMPTY_STANDARD_COPY_OPTIONS = {};

    private final Path root;
    private final URI rootUri;
    private final AccessMode accessMode;

    /**
     * Constructs a {@code FileObjectStore} with the specified root directory.
     * It normalizes and ensures the creation of the root directory as a valid absolute path.
     *
     * @param root the path to the root directory of the file object store
     * @param accessMode the access level for the file object store
     * @throws IOException if an I/O error occurs while creating or accessing the directory
     */
    private FileObjectStore(Path root, AccessMode accessMode) throws IOException {
        this.root = Files.createDirectories(root).toAbsolutePath().normalize();
        this.rootUri = this.root.toUri();
        this.accessMode = accessMode;
        LOG.debug("Created FileObjectStore with root {}", root);
    }

    /**
     * Creates a new instance of a readable object store using the specified root directory.
     *
     * @param root the path to the root directory for the readable object store; must not be {@code null}
     * @return a new instance of {@code ReadableObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static ReadableObjectStore newReadableObjectStore(Path root) throws IOException {
        return new FileObjectStore(root, AccessMode.READ);
    }

    /**
     * Creates a new instance of a writable object store using the specified root directory.
     *
     * @param root the path to the root directory for the writable object store; must not be {@code null}
     * @return a new instance of {@code WritableObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static WritableObjectStore newWritableObjectStore(Path root) throws IOException {
        return new FileObjectStore(root, AccessMode.WRITE);
    }

    /**
     * Creates a new instance of a file object store using the specified root directory.
     *
     * @param root the path to the root directory for the object store; must not be {@code null}
     * @return a new instance of {@code FileObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static FileObjectStore newObjectStore(Path root) throws IOException {
        return new FileObjectStore(root, AccessMode.READ_AND_WRITE);
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    @Override
    public URI getRoot() {
        return rootUri;
    }

    @Override
    @SuppressWarnings({"java:S2095", "resource"}) // caller closes the stream
    public Stream<ObjectInfo> list(URI path) throws IOException {
        assertReadable();
        try {
            return Files.list(resolveRegularFolder(path))
                    .sorted(Comparator.comparing(Path::getFileName, Comparator.comparing(Path::toString)))
                    .map(this::toObjectInfoUnchecked);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public long write(URI path, InputStream in, OutputOption... options) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        StandardCopyOption[] copyOptions = getCopyOptions(options, resolved);

        createParent(resolved);
        return Files.copy(in, resolved, copyOptions);
    }

    @Override
    public void copy(URI source, URI target, OutputOption... options) throws IOException {
        copyTo(this, source, target, options);
    }

    @Override
    public void move(URI source, URI target, OutputOption... options) throws IOException {
        moveTo(this, source, target, options);
    }

    /**
     * Performs a file-system copy to another file-backed store.
     *
     * @param targetStore The target FileObjectStore where the data will be copied to.
     * @param source The URI of the source file within the current store.
     * @param target The URI of the destination file within the target store.
     * @param options Additional options specifying how the copy should be done.
     * @throws IOException If an I/O error occurs during the copy operation.
     */
    public void copyTo(FileObjectStore targetStore, URI source, URI target, OutputOption... options) throws IOException {
        assertReadable();
        Path sourcePath = resolveRegularData(source);

        targetStore.assertWritable();
        Path targetPath = targetStore.resolve(target);
        StandardCopyOption[] copyOptions = targetStore.getCopyOptions(options, targetPath);

        createParent(targetPath);
        Files.copy(sourcePath, targetPath, copyOptions);
    }

    /**
     * Performs a file-system move to another file-backed store.
     *
     * @param targetStore The target FileObjectStore where the file will be moved.
     * @param source The URI of the source file to be moved.
     * @param target The URI of the target location where the file will be moved.
     * @param options An array of OutputOption that defines how the move operation should be performed.
     * @throws IOException If an I/O error occurs during the move operation.
     */
    public void moveTo(FileObjectStore targetStore, URI source, URI target, OutputOption... options) throws IOException {
        assertReadable();
        Path sourcePath = resolveRegularData(source);

        targetStore.assertWritable();
        Path targetPath = targetStore.resolve(target);
        StandardCopyOption[] copyOptions = targetStore.getCopyOptions(options, targetPath);

        createParent(targetPath);
        Files.move(sourcePath, targetPath, copyOptions);
    }

    @Override
    public long write(URI path, byte[] data, int from, int to, OutputOption... options) throws IOException {
        assertWritable();

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
        assertReadable();
        return Files.newInputStream(resolveRegularData(path), StandardOpenOption.READ);
    }

    /**
     * Resolves the given URI to a {@code Path} and ensures it corresponds to a regular data object.
     *
     * @param path the URI to be resolved; must not refer to a symbolic link or be non-existent
     * @return the resolved {@code Path} that validates as a regular file
     * @throws IOException if an I/O error occurs during resolution or validation
     */
    private Path resolveRegularData(URI path) throws IOException {
        Path resolved = resolve(path);

        try {
            BasicFileAttributes attrs = Files.readAttributes(resolved, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attrs.isSymbolicLink()) {
                throw new IOException("File is a symbolic link: " + path);
            }
            if (!attrs.isRegularFile()) {
                throw new IOException("Not a data object: " + path);
            }
        } catch (NoSuchFileException e) {
            throw new ObjectNotFoundException(path.toString());
        }

        return resolved;
    }

    /**
     * Resolves the given URI to a {@code Path} and ensures it corresponds to a folder object.
     *
     * @param path the URI to be resolved; must not refer to a symbolic link or be non-existent
     * @return the resolved {@code Path} that validates as a regular folder
     * @throws IOException if an I/O error occurs during resolution or validation
     */
    private Path resolveRegularFolder(URI path) throws IOException {
        Path resolved = resolve(path);

        try {
            BasicFileAttributes attrs = Files.readAttributes(resolved, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (attrs.isSymbolicLink()) {
                throw new IOException("File is a symbolic link: " + path);
            }
            if (!attrs.isDirectory()) {
                throw new IOException("Not a folder object: " + path);
            }
        } catch (NoSuchFileException e) {
            throw new ObjectNotFoundException(path.toString());
        }

        return resolved;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public OutputStream openOutputStream(URI path, OutputOption... options) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        OpenOption[] soo = getOpenOptions(options, resolved);

        createParent(resolved);
        return Files.newOutputStream(resolved, soo);
    }

    /**
     * Constructs and returns an array of OpenOption elements based on the provided OutputOption array
     * and the resolved file path. This method first determines the effective output option that ensures
     * the resolved file path is writable and then sets the required open options accordingly.
     *
     * @param options an array of OutputOption that specifies the desired file write behavior
     * @param resolved the Path of the file for which the open options are being determined
     * @return an array of OpenOption elements that specify how the file should be opened
     * @throws IOException if an I/O error occurs or if the file cannot be written to
     */
    private OpenOption[] getOpenOptions(OutputOption[] options, Path resolved) throws IOException {
        OutputOption effectiveOption = ensureCanWrite(resolved, options);
        return effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
                : new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
    }

    private StandardCopyOption[] getCopyOptions(OutputOption[] options, Path resolved) throws IOException {
        OutputOption effectiveOption = ensureCanWrite(resolved, options);
        return effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                : EMPTY_STANDARD_COPY_OPTIONS;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void createFolder(URI path) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        ObjectInfo oi = toObjectInfo(resolved);
        if (oi != null && oi.type() != ObjectType.FOLDER) {
            throw new NotAFolderException(path.toString());
        }
        Files.createDirectories(resolved);
    }

    @Override
    public WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        OpenOption[] soo = getOpenOptions(options, resolved);

        createParent(resolved);

        return Files.newByteChannel(resolved, soo);
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void removeFolder(URI path) throws IOException {
        assertWritable();
        try {
            Files.delete(resolveRegularFolder(path));
        } catch (DirectoryNotEmptyException e) {
            throw new FolderNotEmptyException(path.toString(), e);
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public Optional<ObjectInfo> getInfo(URI path) throws IOException {
        assertReadable();
        return Optional.ofNullable(toObjectInfo(resolve(path)));
    }

    @Override
    public SeekableByteChannel openReadableByteChannel(URI path) throws IOException {
        assertReadable();
        return Files.newByteChannel(resolveRegularData(path), StandardOpenOption.READ);
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void delete(URI path) throws IOException {
        assertWritable();

        ObjectInfo oi = toObjectInfo(resolve(path));
        if (oi == null) {
            throw new ObjectNotFoundException(path.toString());
        }
        try {
            Files.delete(resolve(path));
        } catch (NoSuchFileException e) {
            throw new ObjectNotFoundException(path.toString(), e);
        }
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void deleteRecursively(URI path) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        ObjectInfo oi = toObjectInfo(resolved);
        if (oi == null) {
            throw new ObjectNotFoundException(path.toString());
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
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public void close() {
        LOG.debug("Closing FileObjectStore with root {}", root);
        // nothing to close
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(root=" + rootUri + ", accessMode=" + accessMode + ")";
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
    @SuppressWarnings("OverlyBroadThrowsClause")
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
    @SuppressWarnings("OverlyBroadThrowsClause")
    private OutputOption ensureCanWrite(Path path, OutputOption... options) throws IOException {
        Set<OutputOption> optionSet = Set.of(options);
        OutputOption outputOption = switch (optionSet.size()) {
            case 0 -> OutputOption.CREATE_NEW;
            case 1 -> optionSet.iterator().next();
            default -> throw new IllegalArgumentException("Multiple incompatible output options specified: " + Arrays.toString(options));
        };

        ObjectInfo oi = toObjectInfo(path);
        if (oi != null) {
            if (outputOption == OutputOption.CREATE_NEW) {
                throw new ObjectExistsException(path.toString());
            }
            if (oi.type() == ObjectType.FOLDER) {
                throw new IOException("Cannot write data directly to folder: " + path);
            }
        }

        return outputOption;
    }

    /**
     * Converts the specified path into an {@link ObjectInfo}, extracting metadata
     * such as creation time, last modified time, size, type, and relative URI.
     *
     * @param path the {@link Path} representing the file or directory; must not be null
     * @return an {@link ObjectInfo} containing the metadata of the specified path,
     *         or {@code null}, if the object does not exist
     * @throws IOException if an I/O error occurs while reading file attributes or the file is a symbolic link
     */
    private @Nullable ObjectInfo toObjectInfo(Path path) throws IOException {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (NoSuchFileException e) {
            return null;
        }

        if (attributes.isSymbolicLink()) {
            throw new IOException("Path points to a symbolic link: " + path);
        }

        Path relativePath = root.relativize(path);
        String normalized = IoUtil.toUnixPath(relativePath);
        if (attributes.isDirectory() && !normalized.isEmpty() && !normalized.endsWith("/")) {
            normalized += "/";
        }
        URI uri;
        try {
            uri = new URI(null, null, normalized, null);
        } catch (URISyntaxException e) {
            throw new IOException("could not create URI for path: " + normalized, e);
        }
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
            ObjectInfo objectInfo = toObjectInfo(path);
            if (objectInfo == null) {
                throw new UncheckedIOException(new ObjectNotFoundException(path.toString()));
            }
            return objectInfo;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Ensures that the parent directory of the specified path exists by creating
     * all nonexistent parent directories. If the parent directory already exists,
     * no changes are made.
     *
     * @param resolved the path for which the parent directory will be created;
     *                 must not be null
     * @throws IOException if an I/O error occurs while creating the directories
     */
    private static void createParent(Path resolved) throws IOException {
        Path parent = resolved.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
