// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for Input/Output.
 */
@SuppressWarnings("MagicCharacter")
public final class IoUtil {

    private static final Logger LOG = LogManager.getLogger(IoUtil.class);

    private static final Predicate<String> IS_URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]+:.*").asMatchPredicate();
    /**
     * The default character encoding.
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /**
     * The default character encoding.
     */
    private static final Charset PLATFORM_CHARSET = Charset.defaultCharset();
    /**
     * The character encodings used to load files.
     */
    private static final Charset[] CHARSETS;
    /**
     * The property holding the path to the user home.
     */
    private static final Path USER_HOME = Paths.get(System.getProperty("user.home"));

    static {
        // setup list of charsets; use a set to avoid duplicate entries
        Set<Charset> charsets = new LinkedHashSet<>();
        charsets.add(DEFAULT_CHARSET);
        charsets.add(PLATFORM_CHARSET);
        charsets.add(StandardCharsets.ISO_8859_1);
        CHARSETS = charsets.toArray(new Charset[0]);
    }

    private IoUtil() { /* utility class */ }

    private record FileNameInfo(int idxStart, int idxEnd) {}

    /**
     * Extract the filename from a path given as a String. In addition to the system dependent
     * {@link File#separatorChar}, the forward slash '/' is always considered a separator.
     *
     * @param path the path of the file
     * @return the filename of the path's last element
     */
    public static String getFilename(String path) {
        FileNameInfo fi = getFilenameInfo(path);
        return path.substring(fi.idxStart(), fi.idxEnd());
    }

    /**
     * Find start and end index of the filename, discarding trailing path separators.
     *
     * @param path the path to get the filename for
     * @return pair with start, end indexes
     */
    private static FileNameInfo getFilenameInfo(CharSequence path) {
        // trim trailing separators
        int end = path.length();
        while (end > 0 && isSeparatorChar(path.charAt(end - 1))) {
            end--;
        }

        // find start of filename
        int start = end;
        while (start > 0 && !isSeparatorChar(path.charAt(start - 1))) {
            start--;
        }
        return new FileNameInfo(start, end);
    }

    /**
     * Test whether {@code c} is a separator character.
     * <p>
     * In addition to the system dependent
     * {@link File#separatorChar}, the forward slash '/' is always considered a separator.
     *
     * @param c the char to test
     * @return true if separator
     */
    public static boolean isSeparatorChar(char c) {
        return c == '/' || c == File.separatorChar;
    }

    /**
     * Get file extension.
     *
     * @param path the path
     * @return the extension
     */
    public static String getExtension(Path path) {
        Path fnamePath = path.getFileName();

        if (fnamePath == null) {
            return "";
        }

        String fname = fnamePath.toString();
        return getExtensionUnsafe(fname);
    }

    /**
     * Get file extension.
     *
     * @param url the URL
     * @return the extension
     */
    public static String getExtension(URL url) {
        return getExtension(toURI(url));
    }

    /**
     * Get file extension.
     *
     * @param uri the URI
     * @return the extension
     */
    public static String getExtension(URI uri) {
        return getExtension(uri.getPath());
    }

    /**
     * Get file extension.
     * <p>
     * Note that trailing path separators are ignored. That means that both {@code getExtension("test.txt")}
     * and {@code getExtension("test.txt/")} return `"txt"`.
     *
     * @param path the path
     * @return the extension
     */
    public static String getExtension(String path) {
        return getExtensionUnsafe(getFilename(path));
    }

    /**
     * Get file extension.
     * <p>
     * <em>NOTE:</em> {@code fname} must not contain path separators.
     *
     * @param fname the filename
     * @return the extension
     */
    private static String getExtensionUnsafe(String fname) {
        int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(pos + 1);
    }

    /**
     * Remove file extension.
     *
     * @param path the file path
     * @return filename without extension
     */
    public static String stripExtension(String path) {
        FileNameInfo fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.idxEnd());

        return pos < fi.idxStart() ? path : path.substring(0, pos) + path.substring(fi.idxEnd());
    }

    /**
     * Replace file extension.
     * <p>
     * If the filename doesn't have an extension, it will be appended.
     *
     * @param path      the file path
     * @param extension the new file extension
     * @return filename with replaced extension
     */
    public static String replaceExtension(String path, String extension) {
        LangUtil.checkArg(!path.isEmpty(), () -> "path must ot be empty");
        FileNameInfo fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.idxEnd());

        if (pos < fi.idxStart()) {
            // filename has no extension => insert extension
            return path.substring(0, fi.idxEnd()) + '.' + extension + path.substring(fi.idxEnd());
        } else {
            // filename has extension => replace extension
            return path.substring(0, pos) + '.' + extension + path.substring(fi.idxEnd());
        }
    }

    /**
     * Replace the file extension.
     * <p>
     * If the filename doesn't have an extension, it will be appended.
     *
     * @param path      the file path
     * @param extension the new file extension
     * @return filename with replaced extension
     */
    public static Path replaceExtension(Path path, String extension) {
        Path filename = path.getFileName();
        if (filename == null) {
            return path;
        }

        filename = path.getFileSystem().getPath(replaceExtension(filename.toString(), extension));

        return path.resolveSibling(filename);
    }

    /**
     * Read content of URL into String.
     *
     * @param url the url
     * @param cs  the Charset
     * @return content of url
     * @throws IOException if content could not be read
     */
    public static String read(URL url, Charset cs) throws IOException {
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Read content of URI into String.
     *
     * @param uri the uri
     * @param cs  the Charset
     * @return content of uri
     * @throws IOException if content could not be read
     */
    public static String read(URI uri, Charset cs) throws IOException {
        try (InputStream in = openInputStream(uri)) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Open InputStream for URI-
     *
     * @param uri the URI
     * @return InputStream
     * @throws IOException on error
     */
    public static InputStream openInputStream(URI uri) throws IOException {
        if (uri.isAbsolute()) {
            return uri.toURL().openStream();
        } else {
            return Files.newInputStream(Paths.get(uri));
        }
    }

    /**
     * Get stream of lines from InputStream instance.
     *
     * @param in the stream to read from
     * @param cs the Charset to use
     * @return stream of lines
     */
    public static Stream<String> lines(InputStream in, Charset cs) {
        return new BufferedReader(new InputStreamReader(in, cs)).lines();
    }

    /**
     * Get URL for string.
     *
     * @param url the URL as string
     * @return the URL
     * @throws IllegalArgumentException if conversion fails
     */
    public static URL toURL(String url) {
        try {
            return toURI(url).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("string could not be converted to URL: " + url, e);
        }
    }

    /**
     * Get URL for path.
     *
     * @param path the path
     * @return the URL
     * @throws IllegalArgumentException if conversion fails
     */
    public static URL toURL(Path path) {
        return toURL(toURI(path));
    }

    /**
     * Get the {@link URI} for a path.
     * <p>
     * For absolute paths, the result is identical to {@link Path#toUri()}.
     * For relative paths, a relative URI is returned.
     *
     * @param path the path
     * @return the URI
     */
    public static URI toURI(Path path) {
        try {
            return path.isAbsolute() ? path.toUri() : new URI(null, null, toUnixPath(path), null, null);
        } catch (URISyntaxException e) {
            // this should not happen
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get URL for URI.
     *
     * @param uri the URI
     * @return the URL
     * @throws IllegalArgumentException if conversion fails
     */
    public static URL toURL(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert {@link URI} to {@link Path}.
     *
     * @param uri the URI
     * @return the Path
     */
    public static Path toPath(URI uri) {
        return Paths.get(uri);
    }

    /**
     * Get URI for URL.
     *
     * @param url the URL
     * @return the URI
     * @throws IllegalArgumentException if conversion fails
     */
    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get URI for URL.
     *
     * @param url the URL
     * @return the URI
     * @throws IllegalArgumentException if conversion fails
     */
    public static Path toPath(URL url) {
        return Paths.get(toURI(url));
    }

    /**
     * Convert path to a string using the unix path separator (forward slash).
     *
     * @param path the path
     * @return the path as a string with path components separated by forward slashes
     */
    public static String toUnixPath(Path path) {
        FileSystem fs = path.getFileSystem();
        String separator = fs.getSeparator();

        if (separator.equals("/")) {
            return path.toString();
        }

        StringBuilder sb = new StringBuilder();
        Path root = path.getRoot();
        String sep = "";
        if (root != null) {
            String rootStr = root.toString();
            if (rootStr.endsWith(separator)) {
                rootStr = rootStr.substring(0, rootStr.length() - 1);
            }
            if (!rootStr.isEmpty() && rootStr.charAt(rootStr.length() - 1) == ':') {
                rootStr = rootStr.substring(0, rootStr.length() - 1);
                rootStr = rootStr.replace(separator, "/");
                if (!rootStr.startsWith(separator)) {
                    rootStr = "/" + rootStr;
                }
                sb.append(rootStr);
            } else {
                sb.append(root.toString().replace(separator, "/"));
            }
            sep = "/";
        }
        for (Path p : path) {
            sb.append(sep);
            sb.append(p.toString().replace(separator, "\\" + separator));
            sep = "/";
        }
        return sb.toString();
    }

    /**
     * Check if string denotes a URI.
     *
     * @param s the string
     * @return true, if string denotes a URI
     */
    private static boolean isURI(String s) {
        return IS_URI.test(s);
    }

    /**
     * Convert string to URI.
     *
     * @param s the string
     * @return the URI
     */
    public static URI toURI(String s) {
        if (isURI(s)) {
            return URI.create(s);
        } else {
            return Paths.get(s).toUri();
        }
    }

    /**
     * Convert string to Path.
     *
     * @param s the string
     * @return the Path
     */
    public static Path toPath(String s) {
        if (isURI(s)) {
            return Paths.get(URI.create(s));
        } else {
            return Paths.get(s);
        }
    }

    /**
     * Delete a file or directory recursively.
     *
     * @param path the file or directory to delete
     * @throws IOException if a file or directory could not be deleted
     */
    public static void deleteRecursive(Path path) throws IOException {
        try (Stream<Path> files = Files.walk(path, FileVisitOption.FOLLOW_LINKS)) {
            //noinspection DataFlowIssue - false positive; Stream elements are guaranteed to be non-null
            files
                    .sorted(Comparator.reverseOrder())
                    .forEach(LangUtil.uncheckedConsumer(Files::deleteIfExists));
        } catch (UncheckedIOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Load text with unknown character encoding.
     * Tries to load text into a String. Several encodings are tried in the order
     * given in the parameters to this method. On success, calls `onCharsetDetected`
     * to report back the encoding and returns the text.
     *
     * @param path              the path to load the text from
     * @param onCharsetDetected callback to inform about the detected encoding.
     * @param charsets          the encodings to try
     * @return the text read
     * @throws IOException if an exception occurs during loading the data
     */
    public static String loadText(
            Path path,
            Consumer<? super Charset> onCharsetDetected,
            Charset... charsets)
            throws IOException {
        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(path));
        data.mark();
        for (Charset cs : charsets) {
            try {
                data.reset();
                String text = cs.newDecoder()
                        .decode(data)
                        .toString();
                onCharsetDetected.accept(cs);
                return text;
            } catch (@SuppressWarnings("unused") CharacterCodingException e) {
                // ignore exception and try the next encoding
                LOG.debug("unsuccessfully tried encoding {}", cs.name());
            }
        }

        throw new IOException("no matching encoding found for " + path);
    }

    /**
     * Load text with unknown character encoding.
     * Tries to load text into a String. These encodings are tried sequentially:
     * - UTF-8
     * - the default encoding for the platform
     * - ISO 8859-1
     *
     * @param path              the path to load the text from
     * @param onCharsetDetected callback to inform about the detected encoding.
     * @return the text read
     * @throws IOException if an exception occurs during loading the data
     */
    public static String loadText(Path path, Consumer<? super Charset> onCharsetDetected) throws IOException {
        return loadText(path, onCharsetDetected, CHARSETS);
    }

    /**
     * Get an {@link InputStream} for a variety of source objects.
     * <p>
     * Supported classes:
     * <ul>
     *     <li>{@link java.io.InputStream}</li>
     *     <li>{@link java.io.Reader} (wrapped and encoded as UTF-8)</li>
     *     <li>{@link java.net.URI}</li>
     *     <li>{@link java.net.URL}</li>
     *     <li>{@link java.nio.file.Path}</li>
     *     <li>{@link java.io.File}</li>
     * </ul>
     * <p>
     * Notes for Reader sources:
     * <ul>
     *     <li>Characters are encoded to bytes using UTF-8.</li>
     *     <li>Surrogate pairs are preserved across buffer boundaries.</li>
     *     <li>An unpaired trailing high surrogate at end of input is replaced by the UTF-8 encoder with '?' (0x3F).</li>
     *     <li>Closing the returned stream also closes the underlying reader.</li>
     * </ul>
     * <p>
     * If {@code o} is {@code null}, {@link InputStream#nullInputStream()} is returned.
     *
     * @param o the source object
     * @return the input stream for the given source
     * @throws UnsupportedOperationException if the object type is not supported
     * @throws IOException if the type is supported but an I/O error occurs during stream creation
     */
    public static InputStream getInputStream(@Nullable Object o) throws IOException {
        return o == null ? InputStream.nullInputStream() : StreamSupplier.getInputStream(o);
    }

    /**
     * Get OutputStream.
     * <p>
     * Supported classes:
     * <ul>
     *     <li>{@link OutputStream}
     *     <li>{@link URI}
     *     <li>{@link URL}
     *     <li>{@link Path}
     *     <li>{@link File}
     * </ul>
     *
     * @param o object
     * @return InputStream
     * @throws UnsupportedOperationException if the object type is not supported
     * @throws IOException                   if the type is supported but an IOException occurs during stream creation
     */
    public static OutputStream getOutputStream(@Nullable Object o) throws IOException {
        return o == null ? OutputStream.nullOutputStream() : StreamSupplier.getOutputStream(o);
    }

    /**
     * Get {@link InputStream} instance that reads from a string. Characters are encoded using UTF-8.
     *
     * @param s the string to read from
     * @return InputStream instance
     */
    public static InputStream stringInputStream(String s) {
        return stringInputStream(s, StandardCharsets.UTF_8);
    }

    /**
     * Get {@link InputStream} instance that reads from a string.
     *
     * @param s  the string to read from
     * @param cs the charset to use
     * @return InputStream instance
     */
    public static InputStream stringInputStream(String s, Charset cs) {
        return new ByteArrayInputStream(s.getBytes(cs));
    }

    /**
     * Redirect {@code System.out} and {@code System.err} to a file
     *
     * @param path    path to the output file
     * @return AutoCloseable instance (calling close() will reset standard output streams)
     * @throws IOException if an error occurs
     */
    public static synchronized AutoCloseable redirectStandardStreams(Path path) throws IOException {
        // IMPORTANT: create the cleanup object before redirecting system streams!
        Runnable cleanup = new CleanupSystemStreams();

        Combiner combiner = new Combiner(path, "stdout: ".getBytes(StandardCharsets.UTF_8), "stderr: ".getBytes(StandardCharsets.UTF_8));

        LangUtil.registerForCleanup(combiner, cleanup);

        System.setOut(new PrintStream(combiner.streamA(), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(combiner.streamB(), true, StandardCharsets.UTF_8));

        return combiner;
    }

    /**
     * Create a Runnable that closes multiple {@link AutoCloseable} instances. Suppressed exceptions are added to the
     * first exception encountered using the {@link Throwable#addSuppressed(Throwable)} method.
     *
     * @param closeables the {@link AutoCloseable} instances to close
     * @return Runnable instance that closes all passed arguments when run
     */
    public static Runnable composedClose(@Nullable AutoCloseable... closeables) {
        return () -> doCloseAll(Arrays.asList(closeables));
    }

    /**
     * Create a Runnable that closes multiple {@link AutoCloseable} instances. Suppressed exceptions are added to the
     * first exception encountered using the {@link Throwable#addSuppressed(Throwable)} method.
     *
     * @param closeables collection holding the {@link AutoCloseable} instances to close
     * @return Runnable instance that closes all passed arguments when run
     */
    public static Runnable composedClose(Collection<? extends @Nullable AutoCloseable> closeables) {
        return () -> doCloseAll(closeables);
    }

    /**
     * Closes all provided AutoCloseable resources.
     *
     * @param closeables an array of AutoCloseable resources to be closed
     * @throws IOException if an I/O error occurs during the closing of resources.
     */
    @SuppressWarnings("RedundantThrows")
    public static void closeAll(@Nullable AutoCloseable... closeables) throws IOException {
        doCloseAll(LangUtil.asUnmodifiableList(closeables));
    }

    /**
     * Closes all the AutoCloseable resources provided in the Iterable.
     *
     * @param closeables a collection of AutoCloseable resources to be closed.
     * @throws IOException if an I/O error occurs during the closing of resources.
     */
    @SuppressWarnings("RedundantThrows")
    public static void closeAll(Collection<? extends @Nullable AutoCloseable> closeables) throws IOException {
        doCloseAll(closeables);
    }

    /**
     * Closes all AutoCloseable objects in the provided collection.
     * <p>
     * Any exceptions thrown while closing individual objects are suppressed and added to
     * the primary exception, which is then rethrown.
     *
     * @param closeables a collection of AutoCloseable objects to be closed
     */
    private static void doCloseAll(Iterable<? extends @Nullable AutoCloseable> closeables) {
        Throwable t = null;
        for (AutoCloseable c : closeables) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception t1) {
                if (t == null) {
                    t = t1;
                } else {
                    try {
                        t.addSuppressed(t1);
                    } catch (Exception ignore) {
                        // do nothing
                    }
                }
            }
        }
        if (t != null) {
            sneakyThrow(t);
        }
    }

    /**
     * Throw any exception circumventing language checks for declared exceptions.
     *
     * @param e   the {@link Throwable} to throw
     * @param <E> the generic exception type
     * @throws E always
     */
    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    /**
     * Compresses the given files and directories into a zip file and writes it to the specified output stream.
     *
     * @param outputFile the path to the output zip file
     * @param root the root directory for relative paths of files and directories
     * @throws IOException if an error occurs during the compression process
     */
    public static void zip(Path outputFile, Path root) throws IOException {
        zip(outputFile, root, String.valueOf(root.getFileName()));
    }

    /**
     * Compresses the given files and directories into a zip file and writes it to the specified output stream.
     *
     * @param outputFile the path to the output zip file
     * @param root       the root directory for relative paths of files and directories
     * @param filter     the predicate used to filter the paths to include in the zip file
     * @throws IOException if an error occurs during the compression process
     */
    public static void zip(Path outputFile, Path root, Predicate<? super Path> filter) throws IOException {
        zip(outputFile, root, String.valueOf(root.getFileName()), filter);
    }

    /**
     * Compresses the given files and directories into a zip file and writes it to the specified output stream.
     *
     * @param outputFile the output file to write the zip data to
     * @param root the root directory for relative paths of files and directories
     * @param zipRoot the root directory within the zip file
     * @throws IOException if an error occurs during the compression process
     */
    public static void zip(Path outputFile, Path root, String zipRoot) throws IOException {
        try (OutputStream out = Files.newOutputStream(outputFile);
             Stream<Path> paths = Files.walk(root)) {
            zip(out, root, zipRoot, paths);
        }
    }

    /**
     * Compresses the given files and directories into a zip file and writes it to the specified output stream.
     *
     * @param outputFile the path to the output zip file
     * @param root the root directory for relative paths of files and directories
     * @param zipRoot the root directory within the zip file
     * @param filter the predicate used to filter the paths to include in the zip file
     * @throws IOException if an error occurs during the compression process
     */
    public static void zip(Path outputFile, Path root, String zipRoot, Predicate<? super Path> filter) throws IOException {
        try (OutputStream out = Files.newOutputStream(outputFile);
             Stream<Path> paths = Files.walk(root).filter(filter)) {
            zip(out, root, zipRoot, paths);
        }
    }

    /**
     * Compresses the given files and directories into a zip file and writes it to the specified output stream.
     *
     * @param out the output stream to write the zip data to
     * @param root the root directory for relative paths of files and directories
     * @param zipRoot the root directory within the zip file
     * @param paths the stream of paths to include in the zip file
     * @throws IOException if an error occurs during the compression process
     */
    public static void zip(OutputStream out, Path root, String zipRoot, Stream<? extends Path> paths) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            paths.map(p -> relativizeZipPath(root, p))
                    .forEach(p -> {
                        Path realPath = root.resolve(p);
                        String zipName = toZipName(zipRoot, p);
                        try {
                            if (Files.isDirectory(realPath)) {
                                zip.putNextEntry(new ZipEntry(zipName + "/"));
                                zip.closeEntry();
                            } else {
                                ZipEntry entry = new ZipEntry(zipName);
                                entry.setTime(Files.getLastModifiedTime(realPath).toMillis());
                                zip.putNextEntry(entry);
                                try (InputStream in = Files.newInputStream(realPath)) {
                                    in.transferTo(zip);
                                }
                                zip.closeEntry();
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /** Default maximum number of files to extract from a zip archive */
    public static final long DEFAULT_MAX_FILES = 1000;

    /** Default maximum total bytes to extract from a zip archive */
    public static final long DEFAULT_MAX_BYTES = 1_000_000_000L; // 1 GB

    /** Default maximum compression ratio for zip extraction */
    public static final double DEFAULT_MAX_COMPRESSION_RATIO = 100.0;

    /**
     * Unzips the contents of a ZIP file to the specified destination directory.
     * If the entry is a file, it will be extracted. If the entry is a directory, the directory will be created.
     * <p>
     * This method uses default safety limits to protect against zip bombs and other malicious zip files.
     * The default limits are:
     * <ul>
     *   <li>Maximum number of files: {@value #DEFAULT_MAX_FILES}</li>
     *   <li>Maximum total bytes: {@value #DEFAULT_MAX_BYTES}</li>
     *   <li>Maximum compression ratio: {@value #DEFAULT_MAX_COMPRESSION_RATIO}</li>
     * </ul>
     * <p>
     * The implementation enforces these limits during streaming, without buffering the entire file content
     * in memory. It also does not rely solely on the compressed size reported in the zip file, which can
     * be spoofed in malicious zip files.
     *
     * @param zipUrl The URL of the ZIP file to unzip.
     * @param destination The destination directory where the contents will be extracted to.
     * @throws IOException if an I/O error occurs while reading or writing the ZIP file.
     * @throws IllegalArgumentException if the destination is not an existing directory.
     * @throws ZipException if the extraction exceeds any of the safety limits.
     * @see #unzip(URL, Path, long, long, double)
     */
    public static void unzip(URL zipUrl, Path destination) throws IOException {
        unzip(zipUrl, destination, DEFAULT_MAX_FILES, DEFAULT_MAX_BYTES, DEFAULT_MAX_COMPRESSION_RATIO);
    }

    /**
     * Unzips the contents of a ZIP file to the specified destination directory with safety limits.
     * If the entry is a file, it will be extracted. If the entry is a directory, the directory will be created.
     * <p>
     * This method includes safety parameters to protect against zip bombs and other malicious zip files:
     * <ul>
     *   <li>maxFiles: Maximum number of files to extract</li>
     *   <li>maxBytes: Maximum total bytes to extract</li>
     *   <li>maxCompressionRatio: Maximum allowed compression ratio</li>
     * </ul>
     * <p>
     * The implementation enforces these limits during streaming, without buffering the entire file content
     * in memory. It also does not rely solely on the compressed size reported in the zip file, which can
     * be spoofed in malicious zip files.
     *
     * @param zipUrl The URL of the ZIP file to unzip.
     * @param destination The destination directory where the contents will be extracted to.
     * @param maxFiles The maximum number of files to extract.
     * @param maxBytes The maximum total bytes to extract.
     * @param maxCompressionRatio The maximum allowed compression ratio.
     * @throws IOException if an I/O error occurs while reading or writing the ZIP file.
     * @throws IllegalArgumentException if the destination is not an existing directory.
     * @throws ZipException if the extraction exceeds any of the safety limits.
     */
    public static void unzip(URL zipUrl, Path destination, long maxFiles, long maxBytes, double maxCompressionRatio) throws IOException {
        LangUtil.checkArg(Files.isDirectory(destination), "Destination does not exist or is not a directory: " + destination);

        FileSystem fs = destination.getFileSystem();

        try (InputStream in = zipUrl.openStream();
             ZipInputStream zipInputStream = new ZipInputStream(in)) {

            ZipEntry entry;
            long fileCount = 0;
            long totalBytesExtracted = 0;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();

                // Normalize and validate entry path (ZIP slip prevention)
                Path normalizedEntryPath = fs.getPath(entryName).normalize();
                if (normalizedEntryPath.isAbsolute() || normalizedEntryPath.startsWith("..")) {
                    throw new ZipException("Invalid zip entry path: " + entryName);
                }

                Path destinationPath = destination.resolve(normalizedEntryPath).normalize();
                if (!destinationPath.startsWith(destination)) {
                    throw new ZipException("Resolved path escapes destination directory: " + destinationPath);
                }

                // File count limit
                fileCount++;
                if (fileCount > maxFiles) {
                    throw new ZipException("Maximum number of files exceeded: " + maxFiles);
                }

                // Create directories
                if (entry.isDirectory()) {
                    Files.createDirectories(destinationPath);
                    continue;
                }

                // Refuse to overwrite existing symlinks
                if (Files.exists(destinationPath) && Files.isSymbolicLink(destinationPath)) {
                    throw new ZipException("Refusing to overwrite symbolic link: " + destinationPath);
                }

                // Create parent directories
                Path parent = destinationPath.getParent();
                if (parent == null) {
                    throw new ZipException("Invalid zip entry - path has no parent: " + entryName);
                }
                Files.createDirectories(parent);

                // Extract file with limits
                long compressedSize = entry.getCompressedSize();
                long remainingBytes = maxBytes - totalBytesExtracted;

                try (OutputStream fileOut = Files.newOutputStream(destinationPath);
                     LimitedOutputStream limitedOut = new LimitedOutputStream(fileOut, remainingBytes, maxCompressionRatio, compressedSize)) {

                    zipInputStream.transferTo(limitedOut);
                    totalBytesExtracted += limitedOut.getBytesWritten();
                }

                zipInputStream.closeEntry();
            }
        }
    }

    /**
     * Relativize the given path relative to the specified root path.
     *
     * @param root the root path against which to relativize the path
     * @param path the path to be relativized
     * @return the relativized path
     * @throws UncheckedIOException if the relativized path is not a sibling of the root path
     */
    private static Path relativizeZipPath(Path root, Path path) {
        Path relativizedPath = root.relativize(path).normalize();
        if (relativizedPath.startsWith("..")) {
            throw new UncheckedIOException(new IOException(path + " is not a sibling of " + root));
        }
        return relativizedPath;
    }

    /**
     * Convert the given path to a directory name within a zip.
     *
     * @param ziproot the root directory of the zip
     * @param p the path to be converted
     * @return the directory name within the zip
     */
    private static String toZipName(String ziproot, Path p) {
        String canonicalPathString = IntStream.range(0, p.getNameCount())
                .mapToObj(p::getName)
                .map(Path::toString)
                .collect(Collectors.joining("/"));
        return canonicalPathString.isEmpty() ? ziproot : ziproot + "/" + canonicalPathString;
    }

    /**
     * Output stream wrapper that enforces byte limits and compression ratio.
     */
    static class LimitedOutputStream extends FilterOutputStream {
        private final long maxBytes;
        private final double maxCompressionRatio;
        private final long compressedSize;
        private long bytesWritten = 0;

        LimitedOutputStream(OutputStream out, long maxBytes, double maxCompressionRatio, long compressedSize) {
            super(out);
            this.maxBytes = maxBytes;
            this.maxCompressionRatio = maxCompressionRatio;
            this.compressedSize = compressedSize;
        }

        @Override
        public void write(int b) throws IOException {
            checkLimits(1);
            out.write(b);
            bytesWritten++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkLimits(len);
            out.write(b, off, len);
            bytesWritten += len;
        }

        private void checkLimits(int newBytes) throws IOException {
            if (bytesWritten + newBytes > maxBytes) {
                throw new ZipException("Uncompressed size exceeds allowed limit: " + maxBytes);
            }
            if (compressedSize > 0 && ((double) (bytesWritten + newBytes)) / compressedSize > maxCompressionRatio) {
                throw new ZipException("Compression ratio exceeds allowed limit: " + maxCompressionRatio);
            }
        }

        public long getBytesWritten() {
            return bytesWritten;
        }
    }

    /**
     * Creates a comparator that performs lexicographic comparison of {@link Path} instances
     * based on the specified locale. The comparison considers the roots of the paths, followed
     * by their individual path segments, in a lexicographic order determined by the given locale.
     * Paths that are {@code null} are considered less than non-null paths.
     * <p>
     * Note: The returned Comparator is not threadsafe.
     *
     * @param locale the locale to be used for lexicographic comparison of path names. Must not be null.
     * @return a comparator that compares {@link Path} objects lexicographically based on their root
     *         and individual path segments, using the provided locale for ordering.
     */
    public static Comparator<@Nullable Path> lexicalPathComparator(Locale locale) {
        Comparator<String> comparator = TextUtil.lexicographicComparator(locale);

        return (p1, p2) -> {
            if (Objects.equals(p1, p2)) {
                return 0;
            }

            int cmp = comparePathRoots(p1, p2, comparator);
            if (cmp != 0) {
                return cmp;
            }

            assert p1 != null && p2 != null; // null is handled in comparePathRoots()

            int nc1 = p1.getNameCount();
            int nc2 = p2.getNameCount();
            int n = Math.min(nc1, nc2);
            for (int i = 0; i < n; i++) {
                String p1n = p1.getName(i).toString();
                String p2n = p2.getName(i).toString();
                cmp = comparator.compare(p1n, p2n);
                if (cmp != 0) {
                    return cmp;
                }
            }

            return Integer.compare(nc1, nc2);
        };
    }

    /**
     * Compares the root components of two given paths using the specified comparator.
     * If either path is null, it assigns a priority such that a null path is considered less than a non-null path.
     *
     * @param p1 the first path to compare; may be null
     * @param p2 the second path to compare; may be null
     * @param comparator the comparator used to compare the string representations of the path roots
     * @return a negative integer, zero, or a positive integer as the first path root is less than,
     *         equal to, or greater than the second path root. If one path is null, a non-null path is considered greater.
     */
    private static int comparePathRoots(@Nullable Path p1, @Nullable Path p2, Comparator<? super String> comparator) {
        if (p1 == null) {
            return -1;
        }

        if (p2 == null) {
            return 1;
        }

        String root1 = Objects.requireNonNullElse(p1.getRoot(), "").toString();
        String root2 = Objects.requireNonNullElse(p2.getRoot(), "").toString();
        return comparator.compare(root1, root2);
    }

    /**
     * Combine two {@link OutputStream} instances into a single stream.
     */
    @SuppressWarnings("MagicCharacter")
    private static class Combiner implements AutoCloseable {

        public final byte[] prefixA;
        public final byte[] prefixB;

        private final OutputStream out;

        private final ByteArrayOutputStream baosA = new ByteArrayOutputStream(128);
        private final ByteArrayOutputStream baosB = new ByteArrayOutputStream(128);

        Combiner(Path path, byte[] prefixA, byte[] prefixB) throws IOException {
            this(Files.newOutputStream(path), prefixA, prefixB);
        }

        Combiner(OutputStream out, byte[] prefixA, byte[] prefixB) {
            this.out = out;
            this.prefixA = prefixA;
            this.prefixB = prefixB;
        }

        void writeA(int b) throws IOException {
            baosA.write(b);
            if (b == '\n') {
                flushA();
            }
        }

        void writeB(int b) throws IOException {
            baosB.write(b);
            if (b == '\n') {
                flushB();
            }
        }

        void flushA() throws IOException {
            out.write(prefixA);
            out.write(baosA.toByteArray());
            baosA.reset();
        }

        void flushB() throws IOException {
            out.write(prefixB);
            out.write(baosB.toByteArray());
            baosA.reset();
        }

        OutputStream streamA() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    writeA(b);
                }
            };
        }

        OutputStream streamB() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    writeB(b);
                }
            };
        }

        @Override
        public void close() throws IOException {
            try {
                flushA();
                flushB();
            } finally {
                out.flush();
            }
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static class CleanupSystemStreams implements Runnable {
        private final PrintStream sOut;
        private final PrintStream sErr;

        CleanupSystemStreams() {
            this.sOut = System.out;
            this.sErr = System.err;
        }

        @Override
        public void run() {
            System.setOut(sOut);
            System.setErr(sErr);
        }
    }

    /**
     * Generate a stream of paths matching a glob pattern.
     * For details on pattern syntax refer to the {@link PathMatcher} documentation.
     * <p>
     * The returned paths are {@link Path} objects that are obtained as if by {@link
     * Path#resolve(Path) resolving} the relative path against {@code base}.
     * <p>
     * Make sure to close the stream after processing!
     *
     * @param base    the base directory
     * @param pattern the pattern
     * @return stream of matching paths.
     * @throws IOException if an I/O error is thrown when accessing the file system
     */
    public static Stream<Path> glob(Path base, String pattern) throws IOException {
        FileSystem fs = base.getFileSystem();

        // Find the last path separator before the first glob character
        int firstGlobCharIndex;
        int lastDirSeparatorIndex;
        if (fs.getSeparator().equals("\\")) {
            // glob characters can't be escaped in this case
            firstGlobCharIndex = findFirstGlobChar(pattern);
            lastDirSeparatorIndex = pattern.lastIndexOf('/', firstGlobCharIndex);
        } else {
            // this should work for all path separators that aren't backslashes
            firstGlobCharIndex = findFirstUnescapedGlobChar(pattern);
            lastDirSeparatorIndex = pattern.lastIndexOf(fs.getSeparator(), firstGlobCharIndex);
        }

        // Find fixed part prefix of glob pattern
        String fixedPart = (lastDirSeparatorIndex == -1)
                ? ""
                : pattern.substring(0, lastDirSeparatorIndex);

        Path fixedPath = fs.getPath(fixedPart).normalize();
        if (!fixedPath.isAbsolute()) {
            fixedPath = base.resolve(fixedPath).toAbsolutePath().normalize();
        }

        String globPart = (lastDirSeparatorIndex == -1)
                ? "/" + pattern
                : pattern.substring(lastDirSeparatorIndex);

        String globPattern = (fixedPath + globPart).replace(fs.getSeparator(), "/");

        if (firstGlobCharIndex == pattern.length()) {
            // fastpath: pattern does not contain glob characters
            Path path = fs.getPath(globPattern);
            return Files.exists(path) ? Stream.of(path).map(p -> normalizePath(base, p)) : Stream.empty();
        }

        PathMatcher pathMatcher = fs.getPathMatcher("glob:" + globPattern);
        //noinspection resource: caller should clean up
        return Files.walk(fixedPath)
                .filter(pathMatcher::matches)
                .map(p -> normalizePath(base, p));
    }

    /**
     * Returned path should be created the same as the relative path resolved by base.
     * @param base the base path
     * @param p the path
     * @return the normalized path
     */
    private static Path normalizePath(Path base, Path p) {
        // When a fixed path prefix was extracted in glob, p and base have different root
        // and relativize will throw an exception. To make sure all paths share a common root,
        // call relativize with absolute paths.
        return base.resolve(base.toAbsolutePath().relativize(p.toAbsolutePath()));
    }

    private static int findFirstGlobChar(String pattern) {
        int idx = TextUtil.indexOfFirst(pattern, "*?[{");
        return idx >= 0 ? idx : pattern.length();
    }

    private static int findFirstUnescapedGlobChar(String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            switch (pattern.charAt(i)) {
                case '*', '?', '[', '{' -> {return i;}
                case '\\' -> i++; // Skip over the escaped character
                default -> { /* nothing to do */ }
            }
        }
        return pattern.length();
    }

    /**
     * Generate a list of paths matching a glob pattern, analogue to {@link #glob(Path, String)}.
     *
     * @param base    the base directory
     * @param pattern the pattern
     * @return stream of matching paths.
     * @throws IOException if an I/O error is thrown when accessing the file system
     */
    public static List<Path> findFiles(Path base, String pattern) throws IOException {
        try (var stream = glob(base, pattern)) {
            return stream.toList();
        }
    }

    /**
     * Creates a secure temporary directory with the given prefix.
     * <p>
     * <strong>On non-POSIX systems like WINDOWS, no exception is thrown if permissions can not be set!</strong>
     *
     * @param prefix the prefix for the name of the temporary directory
     * @return the path to the newly created temporary directory
     * @throws IOException if an I/O error occurs, the temporary directory cannot be created or the permissions set.
     */
    public static Path createSecureTempDirectory(String prefix) throws IOException {
        Path tempDir;
        switch (Platform.currentPlatform()) {
            case LINUX, MACOS -> {
                FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                tempDir = Files.createTempDirectory(prefix, attr);
            }
            default -> {
                tempDir = Files.createTempDirectory(prefix);
                setTempPermissionsNonPosix(tempDir, true);
            }
        }
        LOG.trace("created temp directory {}", tempDir);
        return tempDir;
    }

    /**
     * Creates a secure temporary directory with specified permissions depending on the operating system.
     *
     * @param dir the path to the parent directory, may be {@code null} in which case the default temporary-file directory is used.
     * @param prefix the prefix string to be used in generating the directory's name; may be a {@code null} string.
     * @return the path to the created temporary directory.
     * @throws IOException if an I/O error occurs, the temporary directory cannot be created or the permissions set.
     */
    public static Path createSecureTempDirectory(Path dir, String prefix) throws IOException {
        Path tempDir;
        switch (Platform.currentPlatform()) {
            case LINUX, MACOS -> {
                FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                tempDir = Files.createTempDirectory(dir, prefix, attr);
            }
            default -> {
                tempDir = Files.createTempDirectory(dir, prefix);
                setTempPermissionsNonPosix(tempDir, true);
            }
        }
        LOG.trace("created secure temp directory {}", tempDir);
        return tempDir;
    }

    /**
     * Creates a secure temporary directory and schedules it for deletion upon JVM exit.
     *
     * @param prefix the prefix string to be used in generating the directory's name; may be null
     * @return the path to the newly created temporary directory
     * @throws IOException if an I/O error occurs, the temporary directory cannot be created or the permissions set.
     */
    public static Path createSecureTempDirectoryAndDeleteOnExit(String prefix) throws IOException {
        Path tempDir = createSecureTempDirectory(prefix);
        deleteRecursiveOnExit(tempDir);
        return tempDir;
    }

    /**
     * Creates a secure temporary directory and schedules it for deletion upon JVM exit.
     *
     * @param dir the parent directory in which the temporary directory is to be created, or null for the system default temporary directory
     * @param prefix the prefix string to be used in generating the directory's name; may be null
     * @return the path to the newly created temporary directory
     * @throws IOException if an I/O error occurs, the temporary directory cannot be created or the permissions set.
     */
    public static Path createSecureTempDirectoryAndDeleteOnExit(Path dir, String prefix) throws IOException {
        Path tempDir = createSecureTempDirectory(dir, prefix);
        deleteRecursiveOnExit(tempDir);
        return tempDir;
    }

    /**
     * Sets the permissions for a temporary file or directory in a non-POSIX compliant file system.
     * This method ensures that the directory is readable, writable, and executable by owner only.
     *
     * @param tempDir the path to the temporary directory whose permissions are to be set
     * @param isDirectory true indicates permissions should be set for a directory, not a file
     */
    private static void setTempPermissionsNonPosix(Path tempDir, boolean isDirectory) {
        File asFile = tempDir.toFile();
        String type = isDirectory ? "directory" : "file";
        if (!asFile.setReadable(true, true)) {
            LOG.warn("Failed to set the temp {} as readable for the owner only: {}", type, tempDir);
        }
        if (!asFile.setWritable(true, true)) {
            LOG.warn("Failed to set the temp {} as writable for the owner only: {}", type, tempDir);
        }
        if (isDirectory) {
            if (!asFile.setExecutable(true, true)) {
                LOG.warn("Failed to set the temp {} as executable for the owner only: {}", type, tempDir);
            }
        } else {
            if (!asFile.setExecutable(false)) {
                LOG.warn("Failed to set the temp {} as not executable: {}", type, tempDir);
            }
        }
    }

    /**
     * Schedules the deletion of the specified directory and its contents when the JVM exits.
     *
     * @param dir the path of the directory to be deleted recursively on JVM exit
     */
    public static void deleteRecursiveOnExit(Path dir) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                deleteRecursive(dir);
            } catch (IOException e) {
                LOG.warn("could not delete temp directory {}", dir);
            }
        }));
    }

    /**
     * Retrieves the directory path for storing application data, tailored to the
     * current platform's conventions. The directory location varies based on the
     * operating system:
     * - On Windows, it utilizes the %APPDATA% environment variable or the user's home directory as a fallback.
     * - On macOS, it uses the Library/Application Support directory within the user's home folder.
     * - On other platforms, the XDG_CONFIG_HOME environment variable or a default .config directory under the user's home folder is used.
     *
     * @param appName the name of the application for which the data directory is being retrieved. It is used to append
     *                a subfolder specific to the application.
     * @return the platform-specific Path representing the directory where the application's data should be stored.
     *         The returned path will include a subdirectory with the given application name.
     */
    private static Path getApplicationDataDirPath(String appName) {
        return switch (Platform.currentPlatform()) {
            case WINDOWS -> Objects.requireNonNullElse(
                    LangUtil.mapNonNull(System.getenv("APPDATA"), Paths::get),
                    USER_HOME
            ).resolve(appName);
            case MACOS -> USER_HOME
                    .resolve("Library")
                    .resolve("Application Support")
                    .resolve(appName);
            default -> Objects.requireNonNullElse(
                    LangUtil.mapNonNull(System.getenv("XDG_CONFIG_HOME"), Paths::get),
                    USER_HOME.resolve(".config")
            ).resolve(appName);
        };
    }

    /**
     * Ensures the existence of a data directory for the specified application.
     * If the directory does not exist, it will be created.
     *
     * @param appName the name of the application for which the data directory is required
     * @return the {@code Path} of the data directory
     * @throws IOException if an I/O error occurs while creating the directory
     */
    public static Path getApplicationDataDir(String appName) throws IOException {
        Path dataDir = getApplicationDataDirPath(appName);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
        return dataDir;
    }

    /**
     * Retrieves the user's home directory as a {@link Path}.
     * This method obtains the path to the user's home directory from the
     * "user.home" system property. If the property is not set,
     * it defaults to the current directory (".").
     *
     * @return a {@link Path} representing the user's home directory
     */
    public static Path getUserHome() {
        return USER_HOME;
    }

    /**
     * Represents a set of rules governing valid file names depending on the platform.
     * Each rule defines a condition to test the validity of a file name and specifies
     * which platforms the rule applies to.
     */
    public enum FileNameRule {
        /**
         * Empty filenames: disallowed on all platforms.
         */
        EMPTY_FILENAME(s -> !s.isEmpty(), Platform.values()),

        /**
         * Blank filenames: disallowed on Windows; formally allowed on MacOS, but problematic.
         */
        BLANK_FILENAME(s -> !s.isBlank(), Platform.WINDOWS, Platform.MACOS),

        /**
         * Leading or trailing whitespace; technically allowed on Windows, but mapped to a different filename.
         */
        LEADING_OR_TRAILING_WHITESPACE(s -> s.strip().length() == s.length(), Platform.WINDOWS),
        /**
         * Tailing dot; technically allowed on Windows, but mapped to a different filename.
         */
        TRAILING_DOT(s -> !s.endsWith("."), Platform.WINDOWS),

        /**
         * Forbidden characters in filenames on Windows (might be incomplete).
         */
        FORBIDDEN_CHARS_WINDOWS(s -> !TextUtil.containsAnyOf(s, "<>:\"/\\|?*\r\n\0"), Platform.WINDOWS),
        /**
         * Forbidden characters in filenames on macOS (might be incomplete).
         */
        FORBIDDEN_CHARS_MACOS(s -> !TextUtil.containsAnyOf(s, "/:\n\0"), Platform.MACOS),
        /**
         * Forbidden characters in filenames on Linux (might be incomplete).
         */
        FORBIDDEN_CHARS_LINUX(s -> !TextUtil.containsAnyOf(s, "/\0"), Platform.LINUX),
        /**
         * Forbidden filenames in Windows; these are reserved by the system.
         */
        FORBIDDEN_NAMES_WINDOWS(s ->
                !stripExtension(s).toLowerCase(Locale.ROOT)
                        .matches("con|prn|aux|nul|(com|lpt)[1-9]"),
                Platform.WINDOWS
        );

        private final Predicate<String> predicate;
        private final Platform[] appliesTo;

        FileNameRule(Predicate<String> predicate, Platform... appliesTo) {
            this.predicate = predicate;
            this.appliesTo = appliesTo;
        }

        /**
         * Tests whether a given filename satisfies the rules represented by this instance.
         *
         * @param filename the filename to test
         * @return true if the filename satisfies the rules; otherwise false
         */
        public boolean test(String filename) {
            return predicate.test(filename);
        }

        /**
         * Tests whether a given filename satisfies the rules represented by this instance
         * for a specific platform.
         *
         * @param filename the filename to test
         * @param platform the platform to check the filename against
         * @return true if the filename satisfies the rules for the specified platform;
         *         otherwise false
         */
        public boolean test(String filename, Platform platform) {
            return switch (platform) {
                case UNKNOWN -> test(filename);
                default -> LangUtil.isNoneOf(platform, appliesTo) || test(filename);
            };
        }
    }

    /**
     * Validates whether a given file name adheres to all the rules defined for the specified platform.
     *
     * @param filename the name of the file to be validated
     * @param platform the platform for which the file name validation is being performed
     * @return true if the file name is valid according to all platform-specific rules, false otherwise
     */
    public static boolean isValidFileName(String filename, Platform platform) {
        for (var rule : FileNameRule.values()) {
            if (!rule.test(filename, platform)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given filename adheres to all defined portable file name rules.
     * <p>
     * Use this method if files are shared between platforms or intended to be stored on network or portable drives.
     *
     * @param filename the file name to be validated
     * @return true if the file name passes all the rules, false otherwise
     */
    public static boolean isPortableFileName(String filename) {
        for (var rule : FileNameRule.values()) {
            if (!rule.test(filename)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a Writer instance for the provided Appendable. If the given
     * Appendable is already of type Writer, it returns it directly. Otherwise,
     * it wraps the Appendable in an instance of AppendableWriter and returns that.
     *
     * @param app the Appendable to be converted or wrapped as a Writer
     * @return a Writer instance corresponding to the provided Appendable
     */
    public static Writer getWriter(Appendable app) {
        if (app instanceof Writer writer) {
            return writer;
        }
        return new AppendableWriter(app);
    }

    private static class AppendableWriter extends Writer {
        private final Appendable appendable;

        AppendableWriter(Appendable appendable) {
            this.appendable = appendable;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            appendable.append(new String(cbuf, off, len));
        }

        @Override
        public void flush() throws IOException {
            // No action needed for Appendable
        }

        @Override
        public void close() throws IOException {
            // No action needed for Appendable
        }
    }
}
