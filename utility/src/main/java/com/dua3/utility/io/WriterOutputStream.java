package com.dua3.utility.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * An adapter that converts {@link OutputStream} writes into character writes to a {@link Writer}
 * by decoding UTF-8 bytes. This class is the counterpart to {@link ReaderInputStream} and
 * carefully handles multi-byte sequences and surrogate pairs across write boundaries.
 * <p>
 * The implementation uses a {@link CharsetDecoder} configured for UTF-8 with replacement on
 * malformed or unmappable input, matching the behavior of {@code ReaderInputStream} which relies
 * on the platform UTF-8 encoder with replacement for invalid input.
 */
public final class WriterOutputStream extends OutputStream {
    private final Writer writer;
    private final CharsetDecoder decoder;

    // Buffer to accumulate incoming bytes and feed the decoder across write calls
    private final ByteBuffer byteBuffer;
    // Buffer to receive decoded characters before writing to the underlying writer
    private final CharBuffer charBuffer;
    private boolean closed;

    /**
     * Create a WriterOutputStream that decodes written UTF-8 bytes and forwards the resulting
     * characters to the given writer.
     *
     * @param writer the target writer receiving decoded characters
     */
    public WriterOutputStream(Writer writer) {
        this.writer = writer;
        this.decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        // Reasonable default buffer sizes; kept modest similar to ReaderInputStream
        this.byteBuffer = ByteBuffer.allocate(4096);
        this.charBuffer = CharBuffer.allocate(2048);
        this.closed = false;
    }

    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        if (!byteBuffer.hasRemaining()) {
            // run a decode cycle to make space
            decodeToWriter(false);
            byteBuffer.compact();
            if (!byteBuffer.hasRemaining()) {
                // Fallback: flush characters and try again
                flush();
            }
        }
        byteBuffer.put((byte) (b & 0xFF));
        // Attempt to decode immediately for responsiveness (does not force completion of partial sequences)
        decodeToWriter(false);
        byteBuffer.compact();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if ((off | len) < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        while (len > 0) {
            int toPut = Math.min(len, byteBuffer.remaining());
            byteBuffer.put(b, off, toPut);
            off += toPut;
            len -= toPut;

            // Prepare for decoding
            decodeToWriter(false);
            // Make room for more input bytes while preserving any partial sequence
            byteBuffer.compact();

            if (len > 0 && !byteBuffer.hasRemaining()) {
                // Char buffer may be full; write it out and continue
                flushCharBuffer();
            }
        }
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        // Decode what can be decoded without forcing end-of-input
        decodeToWriter(false);
        byteBuffer.compact();
        flushCharBuffer();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        IOException thrown = null;
        try {
            // Final decode pass with endOfInput=true to flush any pending partial sequence
            decodeToWriter(true);
            flushCharBuffer();
        } catch (IOException ioe) {
            thrown = ioe;
        }
        try {
            writer.close();
        } catch (IOException ioe) {
            if (thrown == null) {
                thrown = ioe;
            }
        } finally {
            closed = true;
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    private void decodeToWriter(boolean endOfInput) throws IOException {
        // Switch byte buffer to reading mode
        byteBuffer.flip();
        while (true) {
            CoderResult result = decoder.decode(byteBuffer, charBuffer, endOfInput);
            if (result.isOverflow()) {
                // Output buffer full, write chars and continue
                flushCharBuffer();
                continue;
            }
            if (result.isUnderflow()) {
                break;
            }
            // Malformed or unmappable: decoder is configured to REPLACE, so advance and continue
            // but if for some reason it didn't advance due to buffer state, ensure progress by flushing
            if (charBuffer.position() == charBuffer.capacity()) {
                flushCharBuffer();
            }
        }

        if (endOfInput) {
            // After signalling end, flush remaining state from the decoder
            while (true) {
                CoderResult cr = decoder.flush(charBuffer);
                if (cr.isOverflow()) {
                    flushCharBuffer();
                    continue;
                }
                break;
            }
        }
    }

    private void flushCharBuffer() throws IOException {
        if (charBuffer.position() == 0) {
            return;
        }
        charBuffer.flip();
        int remaining = charBuffer.remaining();
        if (remaining > 0) {
            writer.write(charBuffer.array(), charBuffer.position(), remaining);
        }
        charBuffer.clear();
    }
}
