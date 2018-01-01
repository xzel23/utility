package com.dua3.utility.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * Utility class for Inpit/Output.
 */
public class IOUtil {

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
     * Functional interface for IO operations.
     * @param <T>
     *  the operation's result type
     * @see IOUtil#wrapIO(IOOperation)
     */
    @FunctionalInterface
    public static interface IOOperation<T> {
        T run() throws IOException;
    }
    
    /**
     * A helper method for use in lambda expressions that wraps IO operations and converts
     * thrown IOException to UncheckedIOExcetion.
     * @param <T>
     *  the return type of the wrapped operation
     * @param op
     *  the operation to wrap
     * @return
     *  the result of the wrapped operation
     */
    public static <T> T wrapIO(IOOperation<T> op) {
        try {
            return op.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private IOUtil() {
        // utility class
    }
}
