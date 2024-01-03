// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Utility class for Input/Output.
 */
public final class IoUtil {

    private static final Logger LOG = LogManager.getLogger(IoUtil.class);

    private static final Pattern PATTERN_URI = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]+:.*");
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

    static {
        // setup list of charset; use a set to avoid duplicate entries
        Set<Charset> charsets = new LinkedHashSet<>();
        charsets.add(DEFAULT_CHARSET);
        charsets.add(PLATFORM_CHARSET);
        charsets.add(StandardCharsets.ISO_8859_1);
        CHARSETS = charsets.toArray(new Charset[0]);
    }

    private IoUtil() {
        // utility class
    }

    /**
     * Extract the filename from a path given as a String. In addition to the system dependent
     * {@link File#separatorChar}, the forward slash '/' is always considered a separator.
     *
     * @param path the path of the file
     * @return the filename of the path's last element
     */
    public static String getFilename(String path) {
        Pair<Integer, Integer> fi = getFilenameInfo(path);
        return path.substring(fi.first(), fi.second());
    }

    /**
     * Find start and end index of the filename, discarding trailing path separators.
     *
     * @param path the path to get the filename for
     * @return pair with start, end indexes
     */
    private static Pair<Integer, Integer> getFilenameInfo(CharSequence path) {
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
        return Pair.of(start, end);
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
        return getExtension(url.getFile());
    }

    /**
     * Get file extension.
     *
     * @param uri the URI
     * @return the extension
     */
    public static String getExtension(URI uri) {
        return getExtension(uri.getSchemeSpecificPart());
    }

    /**
     * Get file extension.
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
        Pair<Integer, Integer> fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.second());

        return pos < fi.first() ? path : path.substring(0, pos);
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
        LangUtil.check(!path.isEmpty(), () -> new IllegalArgumentException("path must ot be empty"));
        Pair<Integer, Integer> fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.second());

        if (pos < fi.first()) {
            // filename has no extension => insert extension
            return path.substring(0, fi.second()) + '.' + extension + path.substring(fi.second());
        } else {
            // filename has extension => replace extension
            return path.substring(0, pos) + '.' + extension + path.substring(fi.second());
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
        Path parent = path.getParent();

        Path filename = path.getFileName();
        if (filename == null) {
            return path;
        }

        filename = Paths.get(replaceExtension(filename.toString(), extension));

        return parent == null ? filename : parent.resolve(filename);
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
     * Get URI for path.
     *
     * @param path the path
     * @return the URI
     */
    public static URI toURI(Path path) {
        return path.toUri();
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
     * Get Path for URI.
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
        String sep = "";
        StringBuilder sb = new StringBuilder();
        Path root = path.getRoot();
        if (root != null) {
            sb.append(root.toString().replace("\\", sep));
        }
        for (Path p : path) {
            sb.append(sep);
            sb.append(p);
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
        return PATTERN_URI.matcher(s).matches();
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
     * Get InputStream.
     * <p>
     * Supported classes:
     * <ul>
     *     <li>{@link InputStream}
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
     * @param cleaner the cleaner to register the cleanup operation (reset standard output streams)
     * @return AutoCloseable instance (calling close() will reset standard output streams)
     * @throws IOException if an error occurs
     */
    public static synchronized AutoCloseable redirectStandardStreams(Path path, Cleaner cleaner) throws IOException {
        // IMPORTANT: create the cleanup object before redirecting system streams!
        Runnable cleanup = new CleanupSystemStreams();

        Combiner combiner = new Combiner(path, "stdout: ".getBytes(StandardCharsets.UTF_8), "stderr: ".getBytes(StandardCharsets.UTF_8));

        cleaner.register(combiner, cleanup);

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
    public static Runnable composedClose(AutoCloseable... closeables) {
        return () -> {
            Throwable t = null;
            for (AutoCloseable c : closeables) {
                try {
                    if (c != null) {
                        c.close();
                    }
                } catch (Throwable t1) {
                    if (t == null) {
                        t = t1;
                    } else {
                        try {
                            t.addSuppressed(t1);
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }
            if (t != null) {
                sneakyThrow(t);
            }
        };
    }

    /**
     * Throw any exception circumventing language checks for declared exceptions.
     *
     * @param e   the {@link Throwable} to throw
     * @param <E> the generic exception type
     * @throws E always
     */
    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        //noinspection unchecked
        throw (E) e;
    }

    /**
     * Combine two {@link OutputStream} instances into a single stream.
     */
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
            char c = pattern.charAt(i);

            if (c == '\\') {
                // Skip over the escaped character.
                i++;
            } else if (c == '*' || c == '?' || c == '[' || c == '{') {
                return i;
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
}

