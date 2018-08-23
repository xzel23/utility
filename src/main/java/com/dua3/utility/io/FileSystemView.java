package com.dua3.utility.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.dua3.utility.lang.LangUtil;

/**
 * An abstraction for accessing files stored in different location.
 * <p>
 * This class abstracts accessing files stored in locations such as
 * <ul>
 * <li>directories
 * <li>zip files
 * <li>jar files
 * </ul>
 * The {@link java.net.URL} for a resource returned by {@link Class#getResource(String)} can be converted to a
 * {@link java.nio.file.Path}
 * by calling {@link java.nio.file.Paths#get(URI)}. This however fails if the class that the resource belongs to was
 * loaded from a Jar. To solve this, use the following code:
 *
 * <pre>
 * <code>
 * // create a FileSystemView
 * try (FileSystemView fsv = FileSystemView.create(clazz)) {
 *   // resolve the resource path
 *   Path path = fsv.resolve("resource.txt");
 *   ...
 * }
 * </code>
 * </pre>
 *
 * @author Axel Howind
 *
 */
public class FileSystemView implements AutoCloseable {

    public enum Flags {
        CREATE_IF_MISSING
    }

    @FunctionalInterface
    private interface CleanUp {
        void run() throws IOException;
    }

    /**
     * Create FileSystemView.
     *
     * @param root
     *              the path to the FileSystemView's root. It can either be an existing directory or a zip-file.
     * @param flags
     *              the {@link Flags} to use
     * @return a new FileSystemView
     * @throws IOException
     *             if the view cannot be created
     */
    public static FileSystemView create(Path root, Flags... flags) throws IOException {
        Objects.requireNonNull(root);

        List<Flags> flagList = Arrays.asList(flags);
        boolean createIfMissing = flagList.contains(Flags.CREATE_IF_MISSING);

        // determine type
        boolean exists = Files.exists(root);
        LangUtil.check(exists  || createIfMissing, "Path does not exist: %s", root);

        boolean hasZipExtension = IOUtil.getExtension(root).equalsIgnoreCase("zip");
        boolean isDirectory = Files.isDirectory(root) || !exists && !hasZipExtension;
        boolean isZip = !isDirectory && hasZipExtension;

        // is it a zip?
        if (isZip) {
            return forArchive(root, flags);
        }

        if (isDirectory) {
            return forDirectory(Files.createDirectories(root));
        }

        // other are not implemented
        throw new IllegalArgumentException("Don't know how to handle this path: " + root);
    }

    /**
     * Create a FileSystemView for a file in Zip-Format.
     *
     * @param root
     *            denotes the Zip-File
     * @param flags
     *              the {@link Flags} to use
     * @return
     *         FileSystemView
     * @throws IOException
     *          if the file does not exist or an I/O error occurs
     */
    public static FileSystemView forArchive(Path root, Flags... flags) throws IOException {
        List<Flags> flagList = Arrays.asList(flags);

        Map<String, String> env = new HashMap<>();
        boolean exists = Files.notExists(root);
        boolean create =  flagList.contains(Flags.CREATE_IF_MISSING) && !exists;
        env.put("create", String.valueOf(create));

        URI uri = URI.create("jar:" + root.toUri());
        return createFileSystemView(FileSystems.newFileSystem(uri, Collections.emptyMap()), "/");
    }

    /**
     * Create FileSystemView.
     *
     * @param clazz
     *            the class relative to which paths should be resolved. Classes loaded from Jar files are supported.
     * @return a new FileSystemView
     * @throws IOException
     *             if the view cannot be created
     */
    public static FileSystemView forClass(Class<?> clazz) throws IOException {
        try {
            String classFile = clazz.getSimpleName() + ".class";
            URI uri = clazz.getResource(classFile).toURI();
            switch (uri.getScheme()) {
            case "file":
                return create(Paths.get(uri.resolve(".")));
            case "jar":
                String jarUriStr = java.net.URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name());
                String jar = jarUriStr.replaceAll("^jar:(file:.*)!.*$", "$1");
                String jarPath = jarUriStr.replaceAll("^jar:file:.*!(.*)" + classFile + "$", "$1");
                URI jarUri = new URI("jar", jar, null);
                return createFileSystemView(FileSystems.newFileSystem(jarUri, Collections.emptyMap()), jarPath);
            case "jrt":
                String jrtUriStr = java.net.URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name());
                String jrtPath = jrtUriStr.replaceAll("^jrt:/(.*)" + classFile + "$", "$1");
                URI jrtUri = URI.create("jrt:/");
                return createFileSystemView(FileSystems.newFileSystem(jrtUri, Collections.emptyMap()), jrtPath);
            default:
                throw new IOException("unsupported scheme: " + uri.getScheme());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Create a FileSystemView for an existing directory.
     *
     * @param root
     *            the directory that will be root of this view
     * @return
     *         FileSystemView
     * @throws IOException
     *          if the directory denoted by {@code path} does not exist or an I/O error occurs
     */
    public static FileSystemView forDirectory(Path root) throws IOException {
        return new FileSystemView(root, () -> { /* NOOP */ }, root.toString());
    }

    private static FileSystemView createFileSystemView(FileSystem fs, String path) {
        Path root = fs.getPath(path);
        return new FileSystemView(root, fs::close, "["+fs.toString()+"]"+path);
    }

    private final Path root;
    private final String name;
    private final CleanUp cleanup;

    private FileSystemView(Path root, CleanUp cleanup, String name) {
        this.cleanup = cleanup;
        this.root = root.toAbsolutePath();
        this.name = name;
    }

    @Override
    public void close() throws IOException {
        cleanup.run();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get this FileSystemView's root.
     *
     * @return the root path
     */
    public Path getRoot() {
        return root;
    }

    /**
     * Resolve path.
     *
     * @param path
     *            the path to resolve
     * @return the resolved path
     * @see java.nio.file.Path#resolve(String)
     */
    public Path resolve(String path) {
        Path resolvedPath = root.resolve(path).normalize();
        assertThatResolvedPathIsValid(resolvedPath, path);
        return resolvedPath;
    }

    private void assertThatResolvedPathIsValid(Path resolvedPath, Object originalPath) {
        LangUtil.check(resolvedPath.toAbsolutePath().startsWith(root), "Path is not in this FileSystemViews subtree: %s", originalPath);
    }

    /**
     * Resolve path.
     *
     * @param path
     *            the path to resolve
     * @return the resolved path
     * @see java.nio.file.Path#resolve(Path)
     */
    Path resolve(Path path) {
        Path resolvedPath = root.resolve(path).normalize();
        assertThatResolvedPathIsValid(resolvedPath, path);
        return resolvedPath;
    }

}
