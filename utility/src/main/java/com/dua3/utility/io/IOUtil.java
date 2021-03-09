// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utility class for Input/Output.
 */
public final class IOUtil {

    private IOUtil() {
        // utility class
    }

    /**
     * Extract the filename from a path given as a String. In addition to the system dependent
     * {@link File#separatorChar}, the forward slash '/' is always considered a separator.
     * @param path the path of the file
     * @return the filename of the last element of the path
     */
    public static String getFilename(String path) {
        Pair<Integer, Integer> fi = getFilenameInfo(path);
        return path.substring(fi.first, fi.second);
    }

    /**
     * Find start and end index of the filename, discarding trailing path separators.
     * @param path the path to get the filename for
     * @return pair with start, end indices
     */
    private static Pair<Integer, Integer> getFilenameInfo(CharSequence path) {
        // trim trailing separators
        int end = path.length();
        while (end>0 && isSeparatorChar(path.charAt(end-1))) {
            end--;
        }

        // find start of filename
        int start = end;
        while (start>0 && !isSeparatorChar(path.charAt(start-1))) {
            start--;
        }
        return Pair.of(start,end);
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
        return c=='/' || c==File.separatorChar;
    }
    
    /**
     * Get file extension.
     *
     * @param  path
     *              the path
     * @return      the extension
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
     * @param  url
     *             the URL
     * @return     the extension
     */
    public static String getExtension(URL url) {
        return getExtension(url.getFile());
    }

    /**
     * Get file extension.
     *
     * @param  uri the URI
     * @return     the extension
     */
    public static String getExtension(URI uri) {
        return getExtension(uri.getSchemeSpecificPart());
    }

    /**
     * Get file extension.
     *
     * @param  path  the path
     * @return       the extension
     */
    public static String getExtension(String path) {
        return getExtensionUnsafe(getFilename(path));
    }

    /**
     * Get file extension.
     * <p>
     * <em>NOTE:</em> {@code fname} must not contain path separators.
     * @param  fname the filename
     * @return       the extension
     */
    private static String getExtensionUnsafe(String fname) {
        int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(pos + 1);
    }

    /**
     * Remove file extension.
     *
     * @param  path
     *               the file path
     * @return       filename without extension
     */
    public static String stripExtension(String path) {
        Pair<Integer, Integer> fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.second);

        return pos < fi.first ? path : path.substring(0, pos);
    }

    /**
     * Replace file extension.
     * <p>
     * If the filename doesn't have an extension, it will be appended.
     *
     * @param  path
     *               the file path
     * @param extension
     *              the new file extension
     * @return       filename with replaced extension
     */
    public static String replaceExtension(String path, String extension) {
        Pair<Integer, Integer> fi = getFilenameInfo(path);

        // find dot
        int pos = path.lastIndexOf('.', fi.second);

        if (pos < fi.first) {
            // filename has no extension => insert extension
            return path.substring(0, fi.second)+'.'+extension+path.substring(fi.second);
        } else {
            // filename has extension => replace extension
            return path.substring(0, pos) + '.' + extension + path.substring(fi.second);
        }
    }
    
    /**
     * Replace file extension.
     * <p>
     * If the filename doesn't have an extension, it will be appended.
     *
     * @param  path
     *               the file path
     * @param extension
     *              the new file extension
     * @return       filename with replaced extension
     */
    public static Path replaceExtension(Path path, String extension) {
        Path parent = path.getParent();
        
        Path filename = path.getFileName();
        if (filename==null) {
            return path;
        }
        
        filename = Paths.get(replaceExtension(filename.toString(), extension));
        
        return parent == null ? filename : parent.resolve(filename);
    }

    /**
     * Read content of path into String.
     *
     * @param  path
     *                     the Path
     * @param  cs
     *                     the Charset
     * @return             content of path
     * @throws IOException
     *                     if content could not be read
     */
    public static String read(Path path, Charset cs) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(cs);

        byte[] ba = Files.readAllBytes(path);
        return new String(ba, cs);
    }

    /**
     * Read content of URL into String.
     *
     * @param  url
     *                     the url
     * @param  cs
     *                     the Charset
     * @return             content of url
     * @throws IOException
     *                     if content could not be read
     */
    public static String read(URL url, Charset cs) throws IOException {
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Read content of URI into String.
     *
     * @param  uri
     *                     the uri
     * @param  cs
     *                     the Charset
     * @return             content of uri
     * @throws IOException
     *                     if content could not be read
     */
    public static String read(URI uri, Charset cs) throws IOException {
        try (InputStream in = openInputStream(uri)) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Open InputStream for URI-
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
     * Copy all Bytes from InputStream to OutputStream.
     * @param in the InputStream to read from
     * @param out the outputStream to write to
     * @throws IOException if an error occurs
     */
    public static void copyAllBytes(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = in.read(buf)) > 0) {
            out.write(buf, 0, length);
        }
    }

    /**
     * Get stream of lines from InputStream instance.
     * @param in the stream to read from
     * @param cs the Charset to use
     * @return stream of lines
     */
    public static Stream<String> lines(InputStream in, Charset cs) {
        return new BufferedReader(new InputStreamReader(in, cs)).lines();
    }

    /**
     * Write content String to a path.
     *
     * @param  path
     *                     the Path
     * @param  text
     *                     the text
     * @param  options
     *                     the options to use (see {@link OpenOption})
     * @throws IOException
     *                     if something goes wrong
     */
    public static void write(Path path, CharSequence text, OpenOption... options) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, options)) {
            writer.append(text);
        }
    }

    /**
     * Get URL for path.
     *
     * @param  path                  the path
     * @return                       the URL
     * @throws IllegalArgumentException if conversion fails
     */
    public static URL toURL(Path path) {
        return toURL(toURI(path));
    }

    /**
     * Get URI for path.
     *
     * @param  path                  the path
     * @return                       the URI
     */
    public static URI toURI(Path path) {
        return path.toUri();
    }

    /**
     * Get URL for URI.
     *
     * @param  uri                   the URI
     * @return                       the URL
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
     * @param  uri                   the URI
     * @return                       the Path
     */
    public static Path toPath(URI uri) {
        return Paths.get(uri);
    }

    /**
     * Get URI for URL.
     *
     * @param  url                   the URL
     * @return                       the URI
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
     * @param  url                   the URL
     * @return                       the URI
     * @throws IllegalArgumentException if conversion fails
     */
    public static Path toPath(URL url) {
        return Paths.get(toURI(url));
    }

    /**
     * Convert path to a string using the unix path separator (forward slash). 
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
        for (Path p: path) {
            sb.append(sep);
            sb.append(p);
            sep = "/";
        }
        return sb.toString();
    }
    
    /**
     * Check if string denotes a URI.
     * @param s the string
     * @return true if string denotes a URI
     */
    private static boolean isURI(String s) {
        return s.matches("^[a-z][a-z0-9]+:.*");
    }

    /**
     * Convert string to URI.
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
     * @param  path        the file or directory to delete
     * @throws IOException if a file or directory could not be deleted
     */
    public static void deleteRecursive(Path path) throws IOException {
        try (Stream<Path> files = Files.walk(path, FileVisitOption.FOLLOW_LINKS)) {
            files
                    .sorted(Comparator.reverseOrder())
                    .forEach(LangUtil.uncheckedConsumer(Files::deleteIfExists));
        } catch (UncheckedIOException e) {
            throw new IOException(e.getCause());
        }
    }

    /** The default character encoding. */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** The default character encoding. */
    private static final Charset PLATFORM_CHARSET = Charset.defaultCharset();

    /** The character encodings used to load files. */
    private static final Charset[] CHARSETS;

    static {
        // setup list of charset; use a set to avoid duplicate entries
        Set<Charset> charsets = new LinkedHashSet<>();
        charsets.add(DEFAULT_CHARSET);
        charsets.add(PLATFORM_CHARSET);
        charsets.add(StandardCharsets.ISO_8859_1);
        CHARSETS = charsets.toArray(new Charset[0]);
    }

    /**
     * Load text with unknown character encoding.
     * Tries to load text into a String. Several encodings are tried in the order
     * given in the parameters to this method. On success, calls `onCharsetDetected`
     * to report back the encoding and returns the text.
     *
     * @param  path
     *                           the path to load the text from
     * @param  onCharsetDetected
     *                           callback to inform about the detected encoding.
     * @param  charsets
     *                           the encodings to try
     * @return
     *                           the text read
     * @throws IOException
     *                           if an exception occurs during loading the data
     */
    public static String loadText(
            Path path,
            Consumer<Charset> onCharsetDetected,
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
     * @param  path
     *                           the path to load the text from
     * @param  onCharsetDetected
     *                           callback to inform about the detected encoding.
     * @return
     *                           the text read
     * @throws IOException
     *                           if an exception occurs during loading the data
     */
    public static String loadText(Path path, Consumer<Charset> onCharsetDetected) throws IOException {
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
     * @param o
     *  object
     * @return
     *  InputStream
     * @throws UnsupportedOperationException
     *  if the object type is not supported
     * @throws IOException
     *  if the type is supported but an IOException occurs during stream creation
     */
    public static InputStream getInputStream(Object o) throws IOException {
        return StreamSupplier.getInputStream(o);
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
     * @param o
     *  object
     * @return
     *  InputStream
     * @throws UnsupportedOperationException
     *  if the object type is not supported
     * @throws IOException
     *  if the type is supported but an IOException occurs during stream creation
     */
    public static OutputStream getOutputStream(Object o) throws IOException {
        return StreamSupplier.getOutputStream(o);
    }

}

final class StreamSupplier<V> {

    @FunctionalInterface
    interface InputStreamSupplier<C> {
        InputStream getInputStream(C connection) throws IOException;
    }

    @FunctionalInterface
    interface OutputStreamSupplier<C> {
        OutputStream getOutputStream(C connection) throws IOException;
    }

    private static final StreamSupplier<Object> UNSUPPORTED = def(Object.class, StreamSupplier::inputUnsupported, StreamSupplier::outputUnsupported);

    private static final List<StreamSupplier<?>> streamSuppliers;
    
    // complicated initialization code because Java 8 does not support List.of
    static {
        List<StreamSupplier<?>> list = new ArrayList<>();
        list.add(def(InputStream.class, v -> v, StreamSupplier::outputUnsupported));
        list.add(def(OutputStream.class, StreamSupplier::inputUnsupported, v-> v));
        list.add(def(URI.class, v->IOUtil.toURL(v).openStream(), v->Files.newOutputStream(IOUtil.toPath(v))));
        list.add(def(URL.class, URL::openStream, v->Files.newOutputStream(IOUtil.toPath(v))));
        list.add(def(Path.class, Files::newInputStream, Files::newOutputStream));
        list.add(def(File.class, v->Files.newInputStream(v.toPath()), v->Files.newOutputStream(v.toPath())));
        streamSuppliers = list;
    }

    private static InputStream inputUnsupported(Object o) {
        throw new UnsupportedOperationException("InputStream creation not supported: "+o.getClass().getName());
    }

    private static OutputStream outputUnsupported(Object o) {
        throw new UnsupportedOperationException("OutputStream creation not supported: "+o.getClass().getName());
    }

    private final Class<V> clazz;
    private final InputStreamSupplier<V> iss;
    private final OutputStreamSupplier<V> oss;

    private StreamSupplier(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        this.clazz = clazz;
        this.iss = iss;
        this.oss = oss;
    }

    private static <V> StreamSupplier<V> def(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        return new StreamSupplier<>(clazz, iss, oss);
    }

    @SuppressWarnings("unchecked")
    private static <C> StreamSupplier<? super C> supplier(C o) {
        return streamSuppliers.stream()
                .filter(s -> s.clazz.isInstance(o))
                .findFirst().<StreamSupplier<? super C>>map(s -> (StreamSupplier<? super C>) s)
                .orElse(UNSUPPORTED);
    }

    public static <C> InputStream getInputStream(C o) throws IOException {
        return supplier(o).iss.getInputStream(o);
    }

    public static OutputStream getOutputStream(Object o) throws IOException {
        return supplier(o).oss.getOutputStream(o);
    }

}
