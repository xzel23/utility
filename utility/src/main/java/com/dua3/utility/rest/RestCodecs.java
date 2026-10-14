package com.dua3.utility.rest;

/**
 * Factory methods for optional REST codecs.
 */
public final class RestCodecs {

    private RestCodecs() {
        // Utility class.
    }

    /**
     * Creates a JSON codec backed by Jackson 3.
     *
     * @return a Jackson-backed JSON codec
     * @throws UnsupportedOperationException if Jackson 3 databind is unavailable
     */
    public static RestCodec jacksonJson() {
        return JacksonRestCodec.json();
    }
}
