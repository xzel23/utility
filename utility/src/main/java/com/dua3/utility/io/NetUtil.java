// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.cabe.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Network related utility class.
 */
public final class NetUtil {
    private static final Logger LOG = LoggerFactory.getLogger(NetUtil.class);

    private NetUtil() {
        // nop: utility class
    }

    /**
     * Reads a complete resource into a string.
     *
     * @param  url
     *                     URL to read from
     * @param  cs
     *                     Charset
     * @return
     *                     String with the complete content read from URL
     * @throws IOException
     *                     if an I/O error occurs
     */
    public static String readContent(URL url, Charset cs) throws IOException {
        try (var in = url.openStream()) {
            return new String(in.readAllBytes(), cs);
        }
    }

    /**
     * Check if two URLS are the same.
     *
     * @param  u1 the first URL
     * @param  u2 the second URL
     * @return    true, if both are {@code null}, or if the decoded String
     *            representations of u1 and u2 are equal
     */
    public static boolean sameURL(@Nullable URL u1, @Nullable URL u2) {
        if (u1 == u2) {
            return true;
        }

        if (u1 == null || u2 == null) {
            return false;
        }

        return URLDecoder.decode(u1.toString(), StandardCharsets.UTF_8)
                .equals(URLDecoder.decode(u2.toString(), StandardCharsets.UTF_8));
    }

    /** The void URL. */
    static final URL VOID_URL;

    static {
        try {
            URLStreamHandler handler = new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) {
                    throw new UnsupportedOperationException("openConnection() is not supported");
                }
            };
            VOID_URL = new URL("null", "", 0, "", handler);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get an instance of an invalid (yet not {@code null}) URL that cannot be
     * referenced.
     * The void URL will have the protocol set to the text {@code "null"}.
     *
     * @return the void URL
     */
    public static URL voidURL() {
        return VOID_URL;
    }
}
