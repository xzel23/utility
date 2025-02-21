package com.dua3.utility.data;

import com.dua3.utility.io.IoUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * The {@code CompressedCharacters} class provides functionality to compress and decompress
 * textual data efficiently. It uses the {@link CompressedBytes} class to handle the
 * underlying compression of data represented as bytes.
 *
 * <p>This class is suitable for scenarios where large textual data needs to be stored in
 * a compressed format in memory, allowing for decompression on demand. It supports
 * compressing data from both a {@code CharSequence} and a {@code Reader}.
 */
public class CompressedCharacters {
    private final CompressedBytes data;

    /**
     * Compresses the given {@link CharSequence} into a {@code CompressedCharacters} instance.
     *
     * @param s the {@link CharSequence} to compress
     * @return a {@code CompressedCharacters} instance containing the compressed representation of the input
     */
    public static CompressedCharacters compress(CharSequence s) {
        return new CompressedCharacters(s);
    }

    /**
     * Compresses the content provided by the given {@link Reader} into a
     * {@code CompressedCharacters} object.
     *
     * @param r the {@code link} containing the characters to be compressed
     * @return a {@code CompressedCharacters} instance representing the compressed data
     * @throws IOException if an I/O error occurs during the compression process
     */
    public static CompressedCharacters compress(Reader r) throws IOException {
        return new CompressedCharacters(CompressedBytes.compress(IoUtil.getInputStream(r)));
    }

    /**
     * Constructs a {@code CompressedCharacters} instance by compressing the given {@link CharSequence}.
     *
     * @param s the {@link CharSequence} to be compressed
     */
    private CompressedCharacters(CharSequence s) {
        this.data = CompressedBytes.compress(s.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Constructs a {@code CompressedCharacters} instance from a {@link CompressedBytes} instance.
     * The {@link CompressedBytes} instance represents the compressed form of the textual data,
     * and this constructor enables creating a {@code CompressedCharacters} object directly from
     * that compressed representation.
     *
     * @param data the {@link CompressedBytes} instance containing compressed textual data
     */
    private CompressedCharacters(CompressedBytes data) {
        this.data = data;
    }

    /**
     * Returns the decompressed textual representation of the compressed data as a {@link String}.
     *
     * @return a {@link String} representation of the decompressed data
     */
    @Override
    public String toString() {
        return new String(data.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Converts the underlying compressed byte data into a {@link Reader} for reading the decompressed
     * character stream using UTF-8 encoding.
     *
     * @return a {@link Reader} that provides access to the decompressed character data
     */
    public Reader toReader() {
        return new InputStreamReader(data.inputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Converts the compressed character representation into an {@link InputStream}.
     *
     * @return an {@link InputStream} that provides the stream of bytes using the UTF-8 encoding
     */
    public InputStream inputStream() {
        return data.inputStream();
    }
}
