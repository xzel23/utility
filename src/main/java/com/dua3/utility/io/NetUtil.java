package com.dua3.utility.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

public class NetUtil {

    private NetUtil() {
        // nop: utility class
    }

    /**
     * Register a custom URLHandler that deviates all non-local URL-connections to a local path.
     * @param clazz
     *   The class relative to which the resources are stored.
     */
    public static void registerLocalURLHandler(Class<?> clazz) {
        URL.setURLStreamHandlerFactory(new LocalURLStreamHandlerFactory(clazz));
    }

	/**
	 * Reads a complete resource into a string.
	 *
	 * @param url
	 *            URL to read from
	 * @param cs
	 *            Charset
	 * @return
	 *         String with the complete content read from URL
	 * @throws IOException
	 *         if an I/O error occurs
	 */
	public static String readContent(URL url, Charset cs) throws IOException {
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), cs))) {
	        return reader.lines().collect(Collectors.joining("\n"));
	    }
	}

}
