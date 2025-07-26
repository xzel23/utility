package com.dua3.utility.data;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * The {@code CompressedBytes} class provides functionality to compress and decompress byte arrays
 * or input streams using the Deflate compression algorithm. Instances of this class store the
 * compressed data and offer an interface to retrieve the decompressed data through an InputStream.
 *
 * <p>This class is intended to be used when relatively large amounts of binary data have to be kept in memory
 * but are not frequently needed to be uncompressed.
 *
 * <p>The compressed data is stored in an internal byte array inflated on demand. If the deflated data need more space
 * than the original data, the data is stored uncompressed. The first byte of the internal array is set to 0 if
 * the data is stored uncompressed, 1 otherwise.
 */
public final class CompressedBytes {
    private static final Logger LOG = LogManager.getLogger(CompressedBytes.class);

    private final byte[] data;

    /**
     * Compresses the given byte array using the Deflate compression algorithm.
     *
     * @param data the byte array to be compressed
     * @return a {@code CompressedBytes} instance containing the compressed data
     */
    public static CompressedBytes compress(byte[] data) {
        return new CompressedBytes(data, false);
    }

    /**
     * Compresses the data from the given InputStream using the Deflate compression algorithm and
     * returns a {@code CompressedBytes} instance containing the compressed data.
     *
     * @param data the InputStream containing the data to be compressed
     * @return a {@code CompressedBytes} instance that contains the compressed data
     */
    public static CompressedBytes compress(InputStream data) {
        return new CompressedBytes(data);
    }

    /**
     * Loads compressed data from the given InputStream and returns a {@code CompressedBytes} instance.
     *
     * @param in the InputStream containing the compressed data to be loaded
     * @return a {@code CompressedBytes} instance representing the loaded compressed data
     * @throws IOException if an I/O error occurs while reading from the InputStream
     */
    public static CompressedBytes loadCompressedData(InputStream in) throws IOException {
        return loadCompressedData(in.readAllBytes());
    }

    /**
     * Loads a compressed representation of the given byte array and wraps it in a {@code CompressedBytes} instance.
     *
     * @param data the byte array containing the compressed data
     * @return a {@code CompressedBytes} instance wrapping the given compressed data
     * @throws IllegalArgumentException if the input data is empty
     */
    public static CompressedBytes loadCompressedData(byte[] data) {
        if (data.length < 1) {
            throw new IllegalArgumentException("data is empty");
        }
        CompressedBytes compressedBytes = new CompressedBytes(data, true);

        assert ((BooleanSupplier) () -> {
            try {
                compressedBytes.toByteArray();
                return true;
            } catch (Exception e) {
                return false;
            }
        }).getAsBoolean() : "invalid data";

        return compressedBytes;
    }

    /**
     * Constructs a CompressedBytes instance by either compressing the given data
     * using the Deflate algorithm or storing it uncompressed if compression is not
     * efficient. This constructor handles compression based on the provided flag.
     *
     * @param data the byte array to be compressed or stored uncompressed
     * @param isCompressed a flag indicating whether the provided data is already compressed
     * @throws IllegalArgumentException if the data size exceeds allowed limits
     * @throws IllegalStateException if an I/O error occurs during compression
     */
    private CompressedBytes(byte[] data, boolean isCompressed) {
        if (isCompressed) {
            LangUtil.checkArg(data.length > 0, () -> "compressed data cannot have length 0");
            this.data = data;
            return;
        }

        if (data.length > Integer.MAX_VALUE - 5) {
            throw new IllegalArgumentException("data is too large");
        }

        byte[] compressedData;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(1); // add marker for compressed data
            try (DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
                dos.write(data);
            }
            compressedData = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (compressedData.length < data.length) {
            this.data = compressedData;
        } else {
            this.data = new byte[data.length + 1];
            // marker for uncompressed data, should be initialized to zero anyway
            assert this.data[0] == 0 : "unexpected uninitialized marker";
            System.arraycopy(data, 0, this.data, 1, data.length);
        }
        logCompressionRatio(data.length);
    }

    /**
     * Constructs a {@code CompressedBytes} instance by compressing the data
     * from the provided InputStream using the Deflate compression algorithm.
     *
     * @param in the InputStream containing the data to be compressed
     * @throws IllegalStateException if an I/O error occurs during compression
     */
    private CompressedBytes(InputStream in) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bos.write(1); // add marker for compressed data
            long nBytes;
            try (DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
                nBytes = in.transferTo(dos) + 1;
            }
            this.data = bos.toByteArray();
            logCompressionRatio(nBytes);
        } catch (IOException e) {
            // this should never happen for in-memory data
            throw new IllegalStateException(e);
        }
    }

    /**
     * Logs the compression ratio by comparing the size of the original data in bytes to the
     * size of the compressed data. It also calculates the percentage of compression achieved.
     *
     * @param nBytes the size of the original data in bytes
     */
    private void logCompressionRatio(long nBytes) {
        LOG.trace("compressed data from {} to {} bytes {}%",
                () -> nBytes,
                () -> this.data.length,
                () -> (100.0 * this.data.length) / Math.max(1, nBytes)
        );
    }

    /**
     * Determines if the data held in this instance is in a compressed state.
     *
     * @return {@code true} if the data is compressed, otherwise {@code false}.
     */
    public boolean isCompressed() {
        return data[0] != 0;
    }

    /**
     * Provides an InputStream that decompresses the internal byte array using the Inflate algorithm.
     *
     * @return an InputStream that allows reading the original data
     */
    public InputStream inputStream() {
        InputStream in = new ByteArrayInputStream(data);
        try {
            return switch (in.read()) {
                case 0 -> in;
                case 1 -> new InflaterInputStream(in);
                default -> throw new IllegalStateException("invalid marker in compressed data");
            };
        } catch (IOException e) {
            // this should never happen for in-memory data
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts the compressed data stored in this instance into a byte array by decompressing it.
     *
     * <p>Note that this method must always create a new byte array to return the data, even if the data is uncompressed.
     * In general, it is better to use {@code inputStream()} as it needs less memory when working on large data.
     *
     * @return a byte array representing the decompressed data
     * @throws IllegalStateException if an I/O error occurs while reading the decompressed data
     */
    public byte[] toByteArray() {
        if (isCompressed()) {
            try (InputStream in = inputStream()) {
                return in.readAllBytes();
            } catch (IOException e) {
                // this should never happen for in-memory data
                throw new IllegalStateException(e);
            }
        } else {
            return Arrays.copyOfRange(data, 1, data.length);
        }
    }

    /**
     * Returns the internal compressed data as a byte array.
     *
     * @return a byte array containing the compressed data
     */
    public byte[] getCompressedData() {
        return data;
    }
}
