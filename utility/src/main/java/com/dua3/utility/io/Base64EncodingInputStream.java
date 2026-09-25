package com.dua3.utility.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * An InputStream that encodes bytes from an underlying InputStream into Base64 ASCII bytes.
 */
public final class Base64EncodingInputStream extends InputStream {
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final InputStream in;
    private final Base64.Encoder encoder;
    private final byte[] inBuf = new byte[3072]; // multiple of 3
    private int inRem = 0;
    private byte[] outBuf = EMPTY_BYTE_ARRAY;
    private int outPos = 0;
    private boolean eof = false;

    /**
     * Constructs a new {@code Base64EncodingInputStream} wrapping the specified input stream using the default Base64 encoder.
     *
     * @param in the underlying input stream to read unencoded bytes from
     */
    public Base64EncodingInputStream(InputStream in) {
        this(in, Base64.getEncoder());
    }

    /**
     * Constructs a new {@code Base64EncodingInputStream} wrapping the specified input stream using the given Base64 encoder.
     *
     * @param in      the underlying input stream to read unencoded bytes from
     * @param encoder the Base64 encoder to use
     */
    public Base64EncodingInputStream(InputStream in, Base64.Encoder encoder) {
        this.in = Objects.requireNonNull(in, "in must not be null");
        this.encoder = Objects.requireNonNull(encoder, "encoder must not be null");
    }

    @Override
    public int read() throws IOException {
        if (!ensureBuffer()) {
            return -1;
        }
        return outBuf[outPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, b.length);
        if (len == 0) {
            return 0;
        }
        if (!ensureBuffer()) {
            return -1;
        }
        int available = outBuf.length - outPos;
        int toCopy = Math.min(available, len);
        System.arraycopy(outBuf, outPos, b, off, toCopy);
        outPos += toCopy;
        return toCopy;
    }

    private boolean ensureBuffer() throws IOException {
        while (outPos >= outBuf.length) {
            if (eof && inRem == 0) {
                return false;
            }
            if (!eof) {
                int read = in.read(inBuf, inRem, inBuf.length - inRem);
                if (read == -1) {
                    eof = true;
                } else {
                    inRem += read;
                }
            }
            if (eof) {
                if (inRem == 0) {
                    return false;
                }
                outBuf = encoder.encode(Arrays.copyOf(inBuf, inRem));
                outPos = 0;
                inRem = 0;
            } else {
                int bytesToEncode = inRem - (inRem % 3);
                if (bytesToEncode != 0) {
                    byte[] chunk = (bytesToEncode == inBuf.length) ? inBuf : Arrays.copyOf(inBuf, bytesToEncode);
                    outBuf = encoder.encode(chunk);
                    outPos = 0;
                    int rem = inRem - bytesToEncode;
                    if (rem > 0) {
                        System.arraycopy(inBuf, bytesToEncode, inBuf, 0, rem);
                    }
                    inRem = rem;
                }
            }
        }
        return true;
    }

    @Override
    public int available() throws IOException {
        int avail = outBuf.length - outPos;
        if (avail > 0) {
            return avail;
        }
        return in.available() > 0 ? 1 : 0;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
