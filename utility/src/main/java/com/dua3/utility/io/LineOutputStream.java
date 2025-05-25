package com.dua3.utility.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * An OutputStream implementation that splits the input into lines and passes these on to a processor.
 */
@SuppressWarnings("MagicCharacter")
public class LineOutputStream extends OutputStream {
    /**
     * The initial size of the buffer.
     * <p>
     * This constant represents the initial size (in bytes) of a buffer. It is used to allocate memory for storing data
     * when initializing a buffer.
     */
    public static final int INITIAL_BUFFER_SIZE = 128;
    /**
     * Maximum buffer size constant.
     * <p>
     * This constant represents the maximum size (in bytes) that a buffer can have.
     */
    public static final int MAX_BUFFER_SIZE = 1024;

    private final Object lock = new Object();
    private final Consumer<? super String> processor;
    private byte[] buf;
    private int count;

    /**
     * Creates a new LineOutputStream.
     *
     * @param processor the consumer function to process each line of output.
     */
    public LineOutputStream(Consumer<? super String> processor) {
        this.buf = new byte[INITIAL_BUFFER_SIZE];
        this.count = 0;
        this.processor = processor;
    }

    private void flushLine() {
        synchronized (lock) {
            // remove line end
            if (count > 0 && buf[count - 1] == '\n') {
                count--;
                if (count > 0 && buf[count - 1] == '\r') {
                    count--;
                }
            }
            String text = new String(buf, 0, count, StandardCharsets.UTF_8);
            processor.accept(text);

            count = 0;
            if (buf.length > MAX_BUFFER_SIZE) {
                buf = new byte[INITIAL_BUFFER_SIZE];
            }
        }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public void write(int b) {
        synchronized (lock) {
            ensureCapacity(count + 1);
            buf[count++] = (byte) b;

            if (b == '\n') {
                flushLine();
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        synchronized (lock) {
            int start = off;
            int end = off + len;
            for (int i = off; i < end; i++) {
                if (b[i] == '\n') {
                    // Ensure capacity and copy bytes up to line end
                    int segLen = i - start + 1;
                    ensureCapacity(count + segLen);
                    System.arraycopy(b, start, buf, count, segLen);
                    count += segLen;
                    flushLine();
                    start = i + 1;
                }
            }
            // Copy any remaining bytes
            if (start < end) {
                int segLen = end - start;
                ensureCapacity(count + segLen);
                System.arraycopy(b, start, buf, count, segLen);
                count += segLen;
            }
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = buf.length;

        if (minCapacity <= oldCapacity) {
            return;
        }

        int newCapacity = Math.max(minCapacity, 2 * oldCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    @Override
    public void close() throws IOException {
        flushLine();
        super.close();
    }
}
