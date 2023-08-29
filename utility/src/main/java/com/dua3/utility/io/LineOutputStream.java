package com.dua3.utility.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An OutputStream implementation that splits the input into lines and passes these on to a processor.
 */
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
    private final Consumer<String> processor;
    private byte[] buf;
    private int count;

    /**
     * Creates a new LineOutputStream.
     *
     * @param processor the consumer function to process each line of output.
     */
    public LineOutputStream(Consumer<String> processor) {
        this.buf = new byte[INITIAL_BUFFER_SIZE];
        this.count = 0;
        this.processor = Objects.requireNonNull(processor);
    }

    private void flushLine() {
        synchronized (lock) {
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
