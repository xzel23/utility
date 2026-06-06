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
    private final URI uri;

    /** The magic bytes stored for inspection. */
    private final long magicBytes;

    /** A flag tracking state to enforce single-use consumption. */
    private boolean consumed = false;

    // We maintain either the underlying path OR the protected stream pipeline
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
     * Factory method for local files. Zero-copy friendly.
     */
    private static Payload fromPath(URI uri, Path path) throws IOException {
        // Read 8 bytes efficiently using NIO channel
        long magic;
        SeekableByteChannel chnl = null;
        try {
            chnl =  Files.newByteChannel(path, StandardOpenOption.READ);
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
    private static Payload fromStream(URI uri, InputStream originalStream) throws IOException {
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
    private Payload(URI uri, @Nullable ReadableByteChannel channel, @Nullable InputStream protectedStream, long magicBytes) {
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
    public URI uri() { return uri; }

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

    private static long bytesToLong(byte[] b, int bytesRead) {
        long value = 0;
        for (int i = 0; i < Math.min(bytesRead, 8); i++) {
            value = (value << 8) | (b[i] & 0xFF);
        }
        if (bytesRead < 8) value <<= (8 - bytesRead) * 8;
        return value;
    }

    private static long bytesToLong(ByteBuffer buffer) {
        long value = 0;
        int bytesRead = buffer.remaining();
        for (int i = 0; i < bytesRead; i++) {
            value = (value << 8) | (buffer.get() & 0xFF);
        }
        if (bytesRead < 8) value <<= (8 - bytesRead) * 8;
        return value;
    }

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
