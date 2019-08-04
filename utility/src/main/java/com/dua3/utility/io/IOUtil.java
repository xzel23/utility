// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
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
        return getExtension(Paths.get(uri));
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
}
