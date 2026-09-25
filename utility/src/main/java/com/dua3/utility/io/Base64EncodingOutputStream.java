package com.dua3.utility.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Objects;

/**
 * An OutputStream that encodes bytes written to it into Base64 ASCII bytes and writes them to the underlying OutputStream.
 */
public final class Base64EncodingOutputStream extends OutputStream {

    private final OutputStream out;

    /**
     * Constructs a new {@code Base64EncodingOutputStream} wrapping the specified output stream using the default Base64 encoder.
     *
     * @param out the underlying output stream to write Base64 encoded bytes to
     */
    public Base64EncodingOutputStream(OutputStream out) {
        this(out, Base64.getEncoder());
    }

    /**
     * Constructs a new {@code Base64EncodingOutputStream} wrapping the specified output stream using the given Base64 encoder.
     *
     * @param out     the underlying output stream to write Base64 encoded bytes to
     * @param encoder the Base64 encoder to use
     */
    public Base64EncodingOutputStream(OutputStream out, Base64.Encoder encoder) {
        Objects.requireNonNull(out, "out must not be null");
        Objects.requireNonNull(encoder, "encoder must not be null");
        this.out = encoder.wrap(out);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
