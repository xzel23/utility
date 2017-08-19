package com.dua3.utility.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
        String fname = path.getFileName().toString();
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

    private IOUtil() {
        // utility class
    }
}
