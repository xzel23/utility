package com.dua3.utility.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * An abstraction for accessing files stored in different location.
 * <p>
 * This class abstracts accessing files stored in locations such as
 * <ul>
 * <li> directories
 * <li> zip files
 * <li> jar files
 * </ul>
 * The {@link java.net.URL} for a resource returned by {@link Class#getResource(String)} can be converted to a {@link java.nio.file.Path}
 * by calling {@link java.nio.file.Paths#get(URI)}. This however fails if the class that the resource belongs to was
 * loaded from a Jar. To solve this, use the following code:
 * <code><pre>
 * // create a FileSystemView
 * try (FileSystemView fsv = FileSystemView.create(clazz)) {
 *   // resolve the resource path
 *   Path path = fsv.resolve("resource.txt");
 *   ...
 * }
 * </pre></code>
 * @author a5xysq1
 *
 */
public class FileSystemView implements AutoCloseable {

    @FunctionalInterface
    private interface CleanUp {
        void run() throws IOException;
    }

    private final Path root;
    private final CleanUp cleanup;

    private FileSystemView(Path root, CleanUp cleanup) {
        this.cleanup = cleanup;
        this.root=root;
    }

    /**
     * Resolve path.
     * @param path the path to resolve
     * @return the resolved path
     * @see java.nio.file.Path#resolve(Path)
     */
    Path resolve(Path path) {
        return root.resolve(path);
    }

    /**
     * Resolve path.
     * @param path the path to resolve
     * @return the resolved path
     * @see java.nio.file.Path#resolve(String)
     */
    public Path resolve(String path) {
        return root.resolve(path);
    }

    /**
     * Create FileSystemView.
     * @param root the path to the FileSystemView's root. It can either be an existing directory or a zip-file.
     * @return a new FileSystemView
     * @throws IOException if the view cannot be created
     */
    public static FileSystemView create(Path root) throws IOException {
        if (!Files.exists(root)) {
            throw new IOException("Path does not exist: "+root);
        }

        // is it a directory?
        if (Files.isDirectory(root)) {
            return new FileSystemView(root, () -> { /*NOOP*/ });
        }

        // is it a zip?
        if (root.getFileName().endsWith(".zip")||root.getFileName().endsWith(".ZIP")) {
            URI uri = URI.create("jar:" + root.toUri());
            FileSystem zipFs = FileSystems.newFileSystem(uri, Collections.emptyMap());
            Path zipRoot = zipFs.getPath("/");
            return new FileSystemView(zipRoot, zipFs::close);
        }

        // other are not implemented
        throw new IllegalArgumentException("Don't know how to handle this path: "+root);
    }

    /**
     * Create FileSystemView.
     * @param clazz the class relative to which paths should be resolved. Classes loaded from Jar files are supported.
     * @return a new FileSystemView
     * @throws IOException if the view cannot be created
     */
    public static FileSystemView create(Class<?> clazz) throws IOException {
        try {
            String classFile = clazz.getSimpleName()+".class";
            URI uri = clazz.getResource(classFile).toURI();
            switch (uri.getScheme()) {
            case "file":
                return create(Paths.get(uri.resolve(".")));
            case "jar":
                String jar = uri.toString().replaceAll("^jar:(file:.*)!.*$", "$1");
                String jarPath = uri.toString().replaceAll("^jar:file:.*!(.*)"+classFile+"$", "$1");
                URI jarUri = new URI("jar", jar, null);
                FileSystem jarFs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                Path jarRoot = jarFs.getPath(jarPath);
                return new FileSystemView(jarRoot, jarFs::close);
            default:
                throw new IOException("unsupported scheme: "+uri.getScheme());
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        cleanup.run();
    }

}
