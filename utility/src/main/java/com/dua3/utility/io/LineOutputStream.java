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
    public static final int INITIAL_BUFFER_SIZE = 128;
    public static final int MAX_BUFFER_SIZE = 1024;
    
    private byte[] buf;
    private int count;
    private final Consumer<String> processor;

    public LineOutputStream(Consumer<String> processor) {
        this.buf = new byte[INITIAL_BUFFER_SIZE];
        this.count = 0;
        this.processor = Objects.requireNonNull(processor);
    }
    
    private synchronized  void flushToLog() {
        String text = new String(buf, 0, count, StandardCharsets.UTF_8);
        processor.accept(text);

        count = 0;
        if (buf.length > MAX_BUFFER_SIZE) {
            buf = new byte[INITIAL_BUFFER_SIZE];
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        ensureCapacity(count+1);
        buf[count++] = (byte) b;
        
        if (b=='\n') {
            flushToLog();
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = buf.length;
        
        if (minCapacity<=oldCapacity) {
            return;
        }
        
        int newCapacity = Math.max(minCapacity, 2*oldCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    @Override
    public void close() throws IOException {
        flushToLog();
        super.close();
    }
}