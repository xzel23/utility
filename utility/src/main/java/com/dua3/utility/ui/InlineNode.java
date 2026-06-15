package com.dua3.utility.ui;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Inline-node metadata wrapper.
 *
 * @param <N> wrapped node type
 */
public final class InlineNode<N> {

    private final N wrapped;
    private final String mimeType;
    private final byte[] data;

    /**
     * Create an inline node wrapper.
     *
     * @param wrapped wrapped object
     * @param mimeType payload MIME type
     * @param data payload data
     */
    public InlineNode(N wrapped, String mimeType, byte[] data) {
        this.wrapped = wrapped;
        this.mimeType = mimeType;
        this.data = data.clone();
    }

    /**
     * Returns the MIME type.
     *
     * @return MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the payload bytes.
     *
     * @return payload bytes
     */
    public byte[] getData() {
        return data.clone();
    }

    /**
     * Returns the wrapped object.
     *
     * @return wrapped object
     */
    public N getWrapped() {
        return wrapped;
    }

    /**
     * Encodes image pixels as a compact ARGB payload.
     * Format: {@code int width, int height, int[width*height] argb}.
     *
     * @param image image
     * @return encoded bytes
     */
    public static byte[] encodeArgbImageData(Image image) {
        int width = image.width();
        int height = image.height();
        int[] argb = image.getArgb();
        if (width <= 0 || height <= 0 || argb.length != width * height) {
            throw new IllegalArgumentException("invalid image dimensions or pixel data length");
        }

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * (2 + argb.length));
        buffer.putInt(width);
        buffer.putInt(height);
        for (int pixel : argb) {
            buffer.putInt(pixel);
        }
        return buffer.array();
    }

    /**
     * Decodes ARGB payload created by {@link #encodeArgbImageData(Image)}.
     *
     * @param data encoded bytes
     * @return decoded image
     */
    public static Image decodeArgbImageData(byte[] data) {
        Objects.requireNonNull(data, "data");

        if (data.length < Integer.BYTES * 2) {
            throw new IllegalArgumentException("invalid image payload");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        int width = buffer.getInt();
        int height = buffer.getInt();
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("invalid image dimensions in payload");
        }

        int pixelCount = width * height;
        int expectedBytes = Integer.BYTES * (2 + pixelCount);
        if (data.length != expectedBytes) {
            throw new IllegalArgumentException("invalid payload size");
        }

        int[] argb = new int[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            argb[i] = buffer.getInt();
        }
        return ImageUtil.getInstance().createImage(width, height, argb);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof InlineNode<?> other)) {
            return false;
        }

        return Objects.equals(wrapped, other.wrapped)
                && Objects.equals(mimeType, other.mimeType)
                && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(wrapped, mimeType);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
