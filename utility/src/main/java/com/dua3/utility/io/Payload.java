package com.dua3.utility.io;

import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Represents a payload abstraction for handling resources identified by a URI.
 * <p>
 * This class supports both file-based and stream-based resources, providing
 * high-performance zero-copy optimizations for files and fallback mechanisms
 * for streams. It also extracts "magic bytes" to identify the type or format
 * of the payload content. The class is immutable and supports lazy initialization
 * for its resource handling components.
 * <p>
 * The {@code Payload} instances can be created via a factory method:
 * - {@link #fromUri(URI)}: Determines the resource type based on the URI scheme and creates the payload.
 */
public final class Payload implements AutoCloseable {

    /** the URI of the resource. */
    private final @Nullable URI uri;

    /** The magic bytes stored for inspection. */
    private final long magicBytes;

    /** A flag tracking state to enforce single-use consumption. */
    private boolean consumed = false;

    // We maintain either the underlying path or the protected stream pipeline
    private @Nullable InputStream protectedStream;
    private @Nullable ReadableByteChannel channel;

    /**
     * Factory method to create a {@code Payload} instance from a given {@code URI}.
     * This method determines the type of the resource (local file, remote stream, etc.)
     * based on the URI scheme and delegates to the appropriate factory method.
     *
     * @param uri the {@code URI} of the resource to create the payload from.
     *            If the scheme is {@code "file"} or {@code null}, the resource
     *            is treated as a local file. For other schemes (e.g., HTTP, HTTPS, JAR),
     *            the resource is treated as a stream.
     * @return a {@code Payload} instance representing the resource.
     * @throws IOException if an I/O error occurs while accessing or reading the resource.
     */
    public static Payload fromUri(URI uri) throws IOException {
        return switch (uri.getScheme()) {
            case "file" -> fromPath(uri, Path.of(uri));
            case null -> fromPath(uri, Path.of(uri));
            default -> fromStream(uri, IoUtil.openInputStream(uri));
        };
    }

    /**
     * Creates a {@code Payload} instance from the provided {@code InputStream}.
     * This method serves as a factory to build a {@code Payload} object containing
     * the data from the input stream.
     *
     * @param in the {@code InputStream} to create the {@code Payload} object from.
     *           The input stream is consumed and wrapped inside the {@code Payload}.
     * @return a {@code Payload} instance representing the resource encapsulated in the stream.
     * @throws IOException if an I/O error occurs while reading or processing the input stream.
     */
    public static Payload fromInputStream(InputStream in) throws IOException {
        return fromStream(null, in);
    }

    /**
     * Factory method for local files. Zero-copy friendly.
     */
    private static Payload fromPath(@Nullable URI uri, Path path) throws IOException {
        // Read 8 bytes efficiently using NIO channel
        long magic;
        SeekableByteChannel chnl = null;
        try {
            chnl = Files.newByteChannel(path, StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(8);
            chnl.read(buffer);
            buffer.flip();
            magic = buffer.remaining() >= 8 ? buffer.getLong() : bytesToLong(buffer);
            chnl.position(0);
            Payload payload = new Payload(uri, chnl, null, magic);
            chnl = null;
            return payload;
        } finally {
            if (chnl != null) {
                chnl.close();
            }
        }
    }

    /**
     * Factory method for streaming data (Network, Sockets).
     */
    private static Payload fromStream(@Nullable URI uri, InputStream originalStream) throws IOException {
        byte[] header = new byte[8];
        int bytesRead = originalStream.readNBytes(header, 0, 8);
        long magic = bytesToLong(header, bytesRead);

        InputStream stitchedStream = bytesRead > 0
                ? new SequenceInputStream(new ByteArrayInputStream(header, 0, bytesRead), originalStream)
                : originalStream;

        return new Payload(uri, null, stitchedStream, magic);
    }

    /**
     * Constructs a {@code Payload} instance with the specified parameters.
     *
     * @param uri the {@code URI} representing the resource associated with the payload.
     * @param protectedStream the {@code InputStream} for the resource, or {@code null}
     *                        if the resource does not originate from a stream.
     * @param magicBytes a long value representing the payload's "magic bytes,"
     *                   typically used for type/format identification.
     */
    // Private constructor called by factories
    private Payload(@Nullable URI uri, @Nullable ReadableByteChannel channel, @Nullable InputStream protectedStream, long magicBytes) {
        assert (channel == null) ^ (protectedStream == null) : "Exactly one of channel or protectedStream must be non-null";

        this.uri = uri;
        this.channel = channel;
        this.protectedStream = protectedStream;
        this.magicBytes = magicBytes;
    }

    /**
     * Retrieves the URI associated with this payload instance.
     *
     * @return the {@code URI} representing the resource associated with the payload.
     */
    public Optional<URI> uri() { return Optional.ofNullable(uri); }

    /**
     * Retrieves the magic bytes associated with this payload instance.
     * This method returns a long value representing a signature or identifier
     * typically used for type or format identification of the payload.
     *
     * @return the {@code long} value representing the magic bytes of the payload.
     */
    public long magic8Bytes() { return magicBytes; }

    /**
     * Provides an {@code InputStream} for the current instance.
     * <p>
     * <strong>Note:</strong> Calling this method consumes the payload. any attempt to access the payload again
     * will result in an exception.
     *
     * @return an {@code InputStream} representing the resource associated with this payload.
     * @throws IOException if an I/O error occurs while creating or retrieving the stream.
     * @throws IllegalStateException if the payload has already been consumed.
     */
    public InputStream stream() throws IOException {
        consume();
        if (protectedStream == null) {
            assert channel != null : "channel must be initialized if protectedStream is null";
            protectedStream = IoUtil.getInputStream(channel);
            channel = null; // prevent double close
        }
        return protectedStream;
    }

    /**
     * Provides a {@link ReadableByteChannel} for accessing the content of the payload.
     * <p>
     * <strong>Note:</strong> Calling this method consumes the payload. any attempt to access the payload again
     * will result in an exception.
     *
     * @return a {@link ReadableByteChannel} instance for reading the payload content.
     * @throws IOException if an I/O error occurs during channel creation or access.
     * @throws IllegalStateException if the payload has already been consumed.
     */
    public ReadableByteChannel channel() throws IOException {
        consume();
        if (channel == null) {
            assert protectedStream != null : "protectedStream must be initialized if channel is null";
            channel = Channels.newChannel(protectedStream);
            protectedStream = null;
        }
        return channel;
    }

    /**
     * Converts an array of bytes into a long value. The number of bytes to consider
     * for the conversion is specified by the {@code bytesRead} parameter. If fewer than
     * 8 bytes are read, the result is left-padded with zeros.
     * <p>
     * Bytes are read from the array in big-endian order.
     *
     * @param b the array of bytes to be converted.
     * @param bytesRead the number of bytes to read from the array. This value should
     *                  be between 0 and 8, inclusive.
     * @return the long value obtained by combining the specified bytes.
     */
    private static long bytesToLong(byte[] b, int bytesRead) {
        long value = 0;
        for (int i = 0; i < Math.min(bytesRead, 8); i++) {
            value = (value << 8) | (b[i] & 0xFF);
        }
        if (bytesRead < 8) value <<= (8 - bytesRead) * 8;
        return value;
    }

    /**
     * Converts the remaining bytes in the given {@code ByteBuffer} into a {@code long} value.
     * If the number of remaining bytes is less than 8, the result is right-padded with zeros.
     * <p>
     * Bytes are read in big-endian order.
     *
     * @param buffer the {@code ByteBuffer} containing the bytes to convert. The buffer's
     *               position will be updated as bytes are read.
     * @return the {@code long} value represented by the bytes in the buffer.
     */
    private static long bytesToLong(ByteBuffer buffer) {
        long value = 0;
        int bytesRead = buffer.remaining();
        for (int i = 0; i < bytesRead; i++) {
            value = (value << 8) | (buffer.get() & 0xFF);
        }
        if (bytesRead < 8) value <<= (8 - bytesRead) * 8;
        return value;
    }

    /**
     * Closes the resources associated with this payload instance.
     * <p>
     * This method ensures that both the {@code channel} and the {@code protectedStream}
     * are properly closed, releasing any resources associated with them.
     *
     * @throws IOException if an I/O error occurs while closing the resources.
     */
    @Override
    public void close() throws IOException {
        IoUtil.closeAll(channel, protectedStream);
    }

    /**
     * Marks the payload as consumed to prevent its content from being accessed multiple times.
     * This method ensures that the payload content is only accessed either through a
     * stream or a channel, throwing an exception if a subsequent attempt is made.
     *
     * @throws IllegalStateException if the payload has already been marked as consumed.
     */
    private void consume() {
        if (consumed) {
            throw new IllegalStateException("Payload content has already been consumed via stream() or channel().");
        }
        this.consumed = true;
    }
}
