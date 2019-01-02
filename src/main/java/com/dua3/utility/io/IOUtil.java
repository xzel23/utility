package com.dua3.utility.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Comparator;
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
     * @param path
     *            the path
     * @return the extension
     */
    public static String getExtension(Path path) {
        Path fnamePath = path.getFileName();

        if (fnamePath==null) {
            return "";
        }

        String fname = fnamePath.toString();
        return getExtension(fname);
    }

    /**
     * Get file extension.
     *
     * @param url
     *            the URL
     * @return the extension
     */
    public static String getExtension(URL url) {
        return getExtension(url.getFile());
    }

    /**
     * Get file extension.
     *
     * @param fname
     *            the filename
     * @return the extension
     */
	public static String getExtension(String fname) {
		int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(pos + 1);
	}

    /**
     * Remove file extension.
     *
     * @param fname
     *            the filename
     * @return filename without extension
     */
    public static String stripExtension(String fname) {
        int pos = fname.lastIndexOf('.');
        return pos < 0 ? "" : fname.substring(0, pos);
    }

    /**
     * Read content of path into String.
     *
     * @param path
     *            the Path
     * @param cs
     *            the Charset
     * @return content of path
     * @throws IOException
     *             if content could not be read
     */
    public static String read(Path path, Charset cs) throws IOException {
        return new String(Files.readAllBytes(path), cs);
    }

    /**
     * Read content of URL into String.
     *
     * @param url
     *            the url
     * @param cs
     *            the Charset
     * @return content of url
     * @throws IOException
     *             if content could not be read
     */
    public static String read(URL url, Charset cs) throws IOException {     
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Write content String to a path.
     *
     * @param path
     *            the Path
     * @param text
     *            the text
     * @param options
     *            the options to use (see {@link OpenOption})
     * @throws IOException
     *             if something goes wrong
     */
    public static void write(Path path, String text, OpenOption... options) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, options)) {
            writer.append(text);
        }
    }

    /**
     * Get URL for path.
     * @param path the path
     * @return the URL
     * @throws IllegalStateException if conversion fails
     */
    public static URL toURL(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Delete a file or directory recursively.
     * @param path the file or directory to delete
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

    /**
     * Load text with unknown character encoding.
     * 
     * Tries to load text into a String. Several encodings are tried in the order
     * given in the parameters to this method. On success, calls `onCharsetDetected`
     * to report back the encoding and returns the text.
     * 
     * In case no matching encoding was found, `onNoMatchingEncoding` is called to
     * handle the data. It's up to the user to either throw `IOException` or
     * encode the text.
     * 
     * @param path
     *  the path to load the text from
     * @param onCharsetDetected
     *  callback to call when a character encoding was successfull.
     * @param onNoMatchingEncoding
     *  callback to call when no encoding was found
     * @param charsets
     *  the encodings to try
     * @return
     *  the text read
     * @throws IOException
     *  if an exception occurs during loading the data
     */
    public static String loadText(
        Path path, 
        Consumer<Charset> onCharsetDetected, 
        java.util.function.Function<ByteBuffer, String> onNoMatchingEncoding,
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
            } catch (CharacterCodingException e) {
                // ignore
            }
        }

        return onNoMatchingEncoding.apply(data);
    }

}
