// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

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

import com.dua3.utility.lang.LangUtil;

/**
 * Utility class for Inpit/Output.
 */
public class IOUtil {

    private IOUtil() {
        // utility class
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
        return getExtension(fname);
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
     * @param  uri
     *             the URI
     * @return     the extension
     */
    public static String getExtension(URI uri) {
        return getExtension(uri.getSchemeSpecificPart());
    }

    /**
     * Get file extension.
     *
     * @param  fname
     *               the filename
     * @return       the extension
     */
    public static String getExtension(String fname) {
        int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(pos + 1);
    }

    /**
     * Remove file extension.
     *
     * @param  fname
     *               the filename
     * @return       filename without extension
     */
    public static String stripExtension(String fname) {
        int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(0, pos);
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
        return new String(Files.readAllBytes(path), cs);
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
    public static void write(Path path, String text, OpenOption... options) throws IOException {
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
        CHARSETS = charsets.toArray(Charset[]::new);
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
     *                           callback to call when a character encoding was
     *                           successfull.
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
     *                           callback to call when a character encoding was
     *                           successfull.
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
    public static OutputStream getOutputStream(Object o) throws IOException {
        return StreamSupplier.getOutputStream(o);
    }
}

class StreamSupplier<V> {

    @FunctionalInterface
    interface InputStreamSupplier<C> {
        InputStream getInputStream(C connection) throws IOException;
    }

    @FunctionalInterface
    interface OutputStreamSupplier<C> {
        OutputStream getOutputStream(C connection) throws IOException;
    }

    private static final StreamSupplier<Object> UNSUPPORTED = def(Object.class, StreamSupplier::inputUnsupported, StreamSupplier::outputUnsupported);

    private static final List<StreamSupplier<?>> streamSuppliers = List.of (
            def(InputStream.class, v -> (InputStream)v, StreamSupplier::outputUnsupported),
            def(OutputStream.class, StreamSupplier::inputUnsupported, v-> (OutputStream) v),
            def(URI.class, v->IOUtil.toURL((URI)v).openStream(), v->Files.newOutputStream(IOUtil.toPath((URI) v))),
            def(URL.class, v->((URL) v).openStream(), v->Files.newOutputStream(IOUtil.toPath((URL) v))),
            def(Path.class, v->Files.newInputStream((Path) v), v->Files.newOutputStream((Path) v)),
            def(File.class, v->Files.newInputStream(((File) v).toPath()), v->Files.newOutputStream(((File) v).toPath()))
    );

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
        for (var s: streamSuppliers) {
            if (s.clazz.isInstance(o)) {
                return (StreamSupplier<? super C>) s;
            }
        }
        return (StreamSupplier<? super C>) UNSUPPORTED;
    }

    public static <C> InputStream getInputStream(C o) throws IOException {
        return supplier(o).iss.getInputStream(o);
    }

    public static OutputStream getOutputStream(Object o) throws IOException {
        return supplier(o).oss.getOutputStream(o);
    }
}
