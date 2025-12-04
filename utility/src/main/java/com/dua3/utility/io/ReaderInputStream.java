package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * An adapter that converts a {@link Reader} into an {@link InputStream} by encoding
 * character data to UTF-8 bytes.
 * <ul>
 *   <li>An API accepts only InputStream but you have character data from a Reader</li>
 *   <li>You need to serialize character streams for network transmission or storage</li>
 *   <li>You need to bridge character-based and byte-based I/O operations</li>
 * </ul>
 * <p>
 * The implementation handles:
 * <ul>
 *   <li>UTF-8 encoding of characters to bytes</li>
 *   <li>Proper handling of surrogate pairs to avoid splitting them across chunk boundaries</li>
 *   <li>Efficient buffering of both character and byte data</li>
 *   <li>Correct EOF handling even when trailing surrogates are present</li>
 * </ul>
 */
public final class ReaderInputStream extends InputStream {
    private final Reader reader;
    private byte[] buffer;
    private int bufferPos;
    private int bufferLen;
    private final char[] cbuf;
    private int carry; // stores a trailing high surrogate to be processed with the next read
    private boolean eof;

    /**
     * Constructs a ReaderInputStream that reads characters from the provided Reader and converts them to bytes.
     *
     * @param reader the Reader from which characters will be read and converted to bytes
     */
    public ReaderInputStream(Reader reader) {
        this.reader = reader;
        buffer = LangUtil.EMPTY_BYTE_ARRAY;
        bufferPos = 0;
        bufferLen = 0;
        cbuf = new char[2048];
        carry = -1;
        eof = false;
    }

    private void refill() throws IOException {
        if (bufferPos < bufferLen) {
            return; // still have data
        }
        bufferLen = 0;
        bufferPos = 0;

        while (bufferLen == 0 && !eof) {
            int off = 0;
            if (carry != -1) {
                cbuf[0] = (char) carry;
                off = 1;
                carry = -1;
            }

            int n = reader.read(cbuf, off, cbuf.length - off);
            if (n == -1) {
                eof = off != 1;
                if (!eof) {
                    // dangling surrogate from previous chunk; encode it as-is, UTF-8 encoder will replace invalid surrogate
                    buffer = new String(cbuf, 0, 1).getBytes(StandardCharsets.UTF_8);
                    bufferLen = buffer.length;
                }
                return;
            }

            n += off;

            // If the last char is a high surrogate and there is no following low surrogate in this chunk,
            // keep it for the next iteration to avoid splitting surrogate pairs across chunk boundaries.
            if (n > 0 && Character.isHighSurrogate(cbuf[n - 1])) {
                carry = cbuf[n - 1];
                n -= 1;
            }

            if (n > 0) {
                buffer = new String(cbuf, 0, n).getBytes(StandardCharsets.UTF_8);
                bufferLen = buffer.length;
            }
            // else loop again to read more data
        }
    }

    @Override
    public int read() throws IOException {
        refill();
        if (bufferPos >= bufferLen) {
            return -1;
        }
        return buffer[bufferPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Bounds checking as per InputStream contract
        if ((off | len) < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }

        int totalRead = 0;
        while (len > 0) {
            if (bufferPos >= bufferLen) {
                refill();
                if (bufferPos >= bufferLen) {
                    // No more data available (EOF)
                    break;
                }
            }

            int toCopy = Math.min(len, bufferLen - bufferPos);
            System.arraycopy(buffer, bufferPos, b, off, toCopy);
            bufferPos += toCopy;
            off += toCopy;
            len -= toCopy;
            totalRead += toCopy;
        }

        if (totalRead == 0) {
            // If nothing was read and we're at EOF, return -1
            return -1;
        }
        return totalRead;
    }

    @Override
    public void close() throws IOException {
        try {
            reader.close();
        } finally {
            super.close();
        }
    }
}
