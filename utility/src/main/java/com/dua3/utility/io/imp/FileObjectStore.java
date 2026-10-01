package com.dua3.utility.io.imp;

import com.dua3.utility.io.AbsolutePathException;
import com.dua3.utility.io.FolderNotEmptyException;
import com.dua3.utility.io.IllegalPathException;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.io.NotADataObjectException;
import com.dua3.utility.io.NotAFolderException;
import com.dua3.utility.io.ObjectExistsException;
import com.dua3.utility.io.ObjectNotFoundException;
import com.dua3.utility.io.ObjectStore;
import com.dua3.utility.io.ReadableObjectStore;
import com.dua3.utility.io.WritableObjectStore;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.WrappedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
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

    private final URI root;
    private final AccessMode accessMode;
    private final LangUtil.AutoCloseableSupplier<Path> rootPath;

    /**
     * Constructs a {@code FileObjectStore} with the specified root directory.
     * It normalizes and ensures the creation of the root directory as a valid absolute path.
     *
     * @param rootPath the path to the root directory of the file object store
     * @param accessMode the access level for the file object store
     * @throws IOException if an I/O error occurs while creating or accessing the directory
     */
    private FileObjectStore(Path rootPath, AccessMode accessMode, boolean lazy) throws IOException {
        assert rootPath.equals(normalizeRoot(rootPath)) : "Root path is not absolute or notnormalized";

        this.root = toRootUri(rootPath);
        this.accessMode = accessMode;
        if (lazy) {
            this.rootPath = LangUtil.cache(LangUtil.unchecked(() -> checkOrCreateDirectory(rootPath, accessMode)), ignored -> {});
        } else {
            Path path = checkOrCreateDirectory(rootPath, accessMode);
            this.rootPath = LangUtil.autoCloseableSupplier(path);
        }

        LOG.debug("Created FileObjectStore with root {}", rootPath);
    }

    /**
     * Converts the given root path to a URI, ensuring that the URI
     * path ends with a forward slash ('/').
     *
     * @param rootPath the root path to be converted to a URI
     * @return a URI representing the given root path, ensuring it ends with a '/'
     */
    private static URI toRootUri(Path rootPath) {
        URI uri = rootPath.toUri();
        String uriStr = uri.toString();
        return uriStr.endsWith("/") ? uri : URI.create(uriStr + "/");
    }

    private static Path normalizeRoot(Path root) {
        return root.toAbsolutePath().normalize();
    }

    private static Path checkOrCreateDirectory(Path path, AccessMode accessMode) throws IOException {
        switch (accessMode) {
            case READ -> LangUtil.check(Files.isDirectory(path), () -> new IOException("Root path does not exist or is not a directory: " + path));
            case WRITE, READ_AND_WRITE -> Files.createDirectories(path);
        }
        return path.toAbsolutePath().normalize();
    }

    /**
     * Creates a new instance of a readable object store using the specified root directory.
     *
     * @param root the path to the root directory for the readable object store; must not be {@code null}
     * @return a new instance of {@code ReadableObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static ReadableObjectStore newReadableObjectStore(Path root) throws IOException {
        return newReadableObjectStore(root, false);
    }

    /**
     * Creates a new instance of a ReadableObjectStore object.
     *
     * @param root the root directory path where the objects are stored
     * @param lazy a boolean flag indicating whether to load objects lazily
     * @return a new ReadableObjectStore instance configured with the specified root path and lazy loading option
     * @throws IOException if an I/O error occurs initializing the object store
     */
    public static ReadableObjectStore newReadableObjectStore(Path root, boolean lazy) throws IOException {
        return new FileObjectStore(normalizeRoot(root), AccessMode.READ, lazy);
    }

    /**
     * Creates a new instance of a writable object store using the specified root directory.
     *
     * @param root the path to the root directory for the writable object store; must not be {@code null}
     * @return a new instance of {@code WritableObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static WritableObjectStore newWritableObjectStore(Path root) throws IOException {
        return newWritableObjectStore(root, false);
    }

    /**
     * Creates a new instance of WritableObjectStore at the specified root path.
     * This store allows for objects to be written to the file system.
     *
     * @param root the root directory path where the object store will be created
     * @param lazy if true, defers loading resources until needed; if false, loads immediately
     * @return a new WritableObjectStore configured with the specified root and loading strategy
     * @throws IOException if an I/O error occurs during the creation of the object store
     */
    public static WritableObjectStore newWritableObjectStore(Path root, boolean lazy) throws IOException {
        return new FileObjectStore(normalizeRoot(root), AccessMode.WRITE, lazy);
    }

    /**
     * Creates a new instance of a file object store using the specified root directory.
     *
     * @param root the path to the root directory for the object store; must not be {@code null}
     * @return a new instance of {@code FileObjectStore} initialized with the given root directory
     * @throws IOException if an I/O error occurs during the initialization of the store
     */
    public static FileObjectStore newObjectStore(Path root) throws IOException {
        return newObjectStore(root, false);
    }

    /**
     * Creates a new instance of FileObjectStore with the specified root path and lazy loading option.
     *
     * @param root the root path of the object store
     * @param lazy a boolean indicating whether the object store should employ lazy loading
     * @return a new instance of FileObjectStore configured with the specified parameters
     * @throws IOException if an I/O error occurs while creating the object store
     */
    public static FileObjectStore newObjectStore(Path root, boolean lazy) throws IOException {
        return new FileObjectStore(normalizeRoot(root), AccessMode.READ_AND_WRITE, lazy);
    }

    @Override
    public URI getRoot() {
        return root;
    }

    @Override
    @SuppressWarnings({"java:S2095", "resource"}) // caller closes the stream
    public Stream<ObjectInfo> list(URI path) throws IOException {
        assertReadable();
        try {
            return Files.list(resolveRegularFolder(path, false))
                    .sorted(Comparator.comparing(Path::getFileName, Comparator.comparing(Path::toString)))
                    .map(this::getObjectInfoUnchecked);
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @Override
    public long write(URI path, InputStream in, OutputOption... options) throws IOException {
        assertWritable();

        try {
            Path resolved = resolve(path);
            StandardCopyOption[] copyOptions = getCopyOptions(options);

            createParent(resolved);
            return Files.copy(in, resolved, copyOptions);
        } catch (Exception e) {
            throw mapException(e);
        }
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
        try {
            assertReadable();
            Path sourcePath = resolveRegularData(source, false);

            targetStore.assertWritable();
            Path targetPath = targetStore.resolve(target);
            StandardCopyOption[] copyOptions = getCopyOptions(options);

            createParent(targetPath);
            Files.copy(sourcePath, targetPath, copyOptions);
        } catch (Exception e) {
            throw mapException(e);
        }
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
        try {
            assertReadable();
            Path sourcePath = resolveRegularData(source, false);

            targetStore.assertWritable();
            Path targetPath = targetStore.resolve(target);
            StandardCopyOption[] copyOptions = FileObjectStore.getCopyOptions(options);

            createParent(targetPath);
            Files.move(sourcePath, targetPath, copyOptions);
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @SuppressWarnings("java:S6916")
    private Path resolveRegularData(URI path, boolean allowUnknown) throws IOException {
        Path sourcePath = resolve(path);
        switch (getObjectInfo(sourcePath).type()) {
            case MISSING -> throw new ObjectNotFoundException("Source file does not exist: " + path);
            case FOLDER -> throw new NotADataObjectException("Not a data object: " + path);
            case DATA -> {/* ignore */}
            case UNKNOWN -> {
                if (!allowUnknown) {
                    throw new UnsupportedOperationException("cannot process object of unknown type: " + path);
                }
            }
        }
        return sourcePath;
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
        } catch (Exception e) {
            throw mapException(e);
        }
        return length;
    }

    @Override
    public InputStream openInputStream(URI path) throws IOException {
        assertReadable();
        Path resolved = resolveRegularData(path, true);
        return Files.newInputStream(resolved, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Resolves the given URI to a {@code Path} and ensures it corresponds to a folder object.
     *
     * @param path the URI to be resolved; must not refer to a symbolic link or be non-existent
     * @return the resolved {@code Path} that validates as a regular folder
     * @throws IOException if an I/O error occurs during resolution or validation
     */
    @SuppressWarnings({"OverlyBroadThrowsClause", "java:S6916"})
    private Path resolveRegularFolder(URI path, boolean allowUnknownType) throws IOException {
        Path resolved = resolve(path);
        switch (getObjectInfo(resolved).type()) {
            case FOLDER -> {/* ignore */}
            case DATA -> throw new NotAFolderException(path.toString());
            case MISSING -> throw new ObjectNotFoundException(path.toString());
            case UNKNOWN -> {
                if (!allowUnknownType) {
                    throw new UnsupportedOperationException("cannot operate on object of unknown type: " + path);
                }
            }
        }
        return resolved;
    }

    @Override
    public OutputStream openOutputStream(URI path, OutputOption... options) throws IOException {
        try {
            assertWritable();

            Path resolved = resolve(path);
            OpenOption[] soo = getOpenOptions(options);

            createParent(resolved);
            return Files.newOutputStream(resolved, soo);
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    /**
     * Constructs and returns an array of OpenOption elements based on the provided OutputOption array
     * and the resolved file path. This method first determines the effective output option that ensures
     * the resolved file path is writable and then sets the required open options accordingly.
     *
     * @param options an array of OutputOption that specifies the desired file write behavior
     * @return an array of OpenOption elements that specify how the file should be opened
     */
    private static OpenOption[] getOpenOptions(OutputOption[] options) {
        OutputOption effectiveOption = getWriteOption(options);
        return effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
                : new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW};
    }

    /**
     * Determines the standard copy options based on the provided output options.
     *
     * @param options an array of output options that determine how the file should be written.
     * @return an array of StandardCopyOption where StandardCopyOption.REPLACE_EXISTING is included if
     *         OutputOption.CREATE_OR_REPLACE is set in the provided options; otherwise, an empty array is returned.
     */
    private static StandardCopyOption[] getCopyOptions(OutputOption[] options) {
        OutputOption effectiveOption = getWriteOption(options);
        return effectiveOption == OutputOption.CREATE_OR_REPLACE
                ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                : EMPTY_STANDARD_COPY_OPTIONS;
    }

    @Override
    public void createFolder(URI path) throws IOException {
        assertWritable();
        try {
            Files.createDirectories(resolve(path));
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @Override
    public WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) throws IOException {
        assertWritable();

        Path resolved = resolve(path);
        OpenOption[] soo = getOpenOptions(options);

        createParent(resolved);

        return Files.newByteChannel(resolved, soo);
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void removeFolder(URI path) throws IOException {
        assertWritable();
        try {
            Files.delete(resolveRegularFolder(path, false));
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @Override
    public ObjectInfo getInfo(URI path) throws IOException {
        assertReadable();
        return getObjectInfo(resolve(path));
    }

    @Override
    public SeekableByteChannel openReadableByteChannel(URI path) throws IOException {
        assertReadable();
        return Files.newByteChannel(resolveRegularData(path, true), StandardOpenOption.READ);
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @Override
    public void delete(URI path) throws IOException {
        assertWritable();
        try {
            Files.delete(resolve(path));
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @Override
    public void deleteRecursively(URI path) throws IOException {
        assertWritable();
        try (Stream<Path> stream = Files.walk(resolve(path))) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (Exception e) {
            throw mapException(e);
        }
    }

    @Override
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing FileObjectStore with root {}", Paths.get(root));
        rootPath.close();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(root=" + root + ", accessMode=" + accessMode + ")";
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
    public Path resolve(URI path) throws IOException {
        if (path.isAbsolute()) {
            throw new AbsolutePathException("absolute path not allowed: " + path);
        }

        Path relative;
        try {
            relative = Path.of(path.getPath() == null ? "" : path.getPath());
        } catch (RuntimeException e) {
            throw new IllegalPathException("invalid path: " + path, e);
        }

        Path resolved = getRootPath().resolve(relative).normalize();
        if (!resolved.startsWith(getRootPath())) {
            throw new IllegalPathException("path points outside root: " + path);
        }
        return resolved;
    }

    /**
     * Determines the write option from the provided array of {@link OutputOption} values.
     * If no options are provided, it defaults to {@link OutputOption#CREATE_NEW}.
     * If a single option is provided, it returns that option.
     * If multiple options are provided, an exception is thrown due to incompatibility.
     *
     * @param options an array of {@link OutputOption} values to select the write option from
     * @return the chosen {@link OutputOption}, which is {@link OutputOption#CREATE_NEW} if no options are provided,
     *         or the single provided option if only one is specified
     * @throws IllegalArgumentException if multiple options are provided, indicating incompatible choices
     */
    private static OutputOption getWriteOption(OutputOption... options) {
        Set<OutputOption> optionSet = Set.of(options);
        return switch (optionSet.size()) {
            case 0 -> OutputOption.CREATE_NEW;
            case 1 -> optionSet.iterator().next();
            default -> throw new IllegalArgumentException("Multiple incompatible output options specified: " + Arrays.toString(options));
        };
    }

    /**
     * Retrieves information about a file system object located at the specified path.
     *
     * @param path the path of the file system object for which information is to be retrieved
     * @return an ObjectInfo containing details about the file system object, or null if the object does not exist
     * @throws IOException if there is an error accessing the file system object or if the path is a symbolic link
     */
    private ObjectInfo getObjectInfo(Path path) throws IOException {
        Path relativePath = getRootPath().relativize(path);
        String normalized = IoUtil.toUnixPath(relativePath);

        ObjectType objectType = ObjectType.UNKNOWN;
        long size = ObjectInfo.UNKNOWN_SIZE;
        Instant created = Instant.MIN;
        Instant modified = Instant.MIN;
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes.isSymbolicLink()) {
                throw new IOException("Path points to a symbolic link: " + path);
            }
            objectType = attributes.isDirectory() ? ObjectType.FOLDER : ObjectType.DATA;
            size = attributes.size();
            created = attributes.creationTime().toInstant();
            modified = attributes.lastModifiedTime().toInstant();
            LOG.debug("read attribute for path path: {}", path);
        } catch (NoSuchFileException ignored) {
            LOG.debug("missing object with path: {}", path);
            objectType = ObjectType.MISSING;
        } catch (FileSystemException e) {
            LOG.warn("could not read file attributes for path: {}", path, e);
        }

        if (objectType == ObjectType.FOLDER && !normalized.isEmpty() && !normalized.endsWith("/")) {
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
                objectType,
                size,
                created,
                modified
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
    private ObjectInfo getObjectInfoUnchecked(Path path) throws UncheckedIOException {
        try {
            return getObjectInfo(path);
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

    private Path getRootPath() throws IOException {
        try {
            return rootPath.get();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static IOException mapException(Exception e) throws IOException {
        return switch (e) {
            case NoSuchFileException e1-> new ObjectNotFoundException(e1);
            case FileNotFoundException e1-> new ObjectNotFoundException(e1);
            case FileAlreadyExistsException e1-> new ObjectExistsException(e1);
            case DirectoryNotEmptyException e1 -> new FolderNotEmptyException(e1);
            case IOException e1-> e1;
            case UncheckedIOException e1-> mapException(e1.getCause());
            case WrappedException e1 -> mapException(e1.getCause());
            case RuntimeException e1 -> throw e1;
            default -> throw new WrappedException(e);
        };
    }
}
