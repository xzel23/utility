package com.dua3.utility.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation that writes to two different streams.
 */
public class TeeOutputStream extends OutputStream {
    private final OutputStream o1;
    private final OutputStream o2;
    private final boolean close1;
    private final boolean close2;

    /**
     * Constructor.
     *
     * @param o1     the first {@link OutputStream}
     * @param close1 if true, calling {@link #close()} will close stream 1
     * @param o2     the second {@link OutputStream}
     * @param close2 if true, calling {@link #close()} will close stream 2
     */
    public TeeOutputStream(OutputStream o1, boolean close1, OutputStream o2, boolean close2) {
        this.o1 = o1;
        this.close1 = close1;
        this.o2 = o2;
        this.close2 = close2;
    }

    @Override
    public void write(int b) throws IOException {
        o1.write(b);
        o2.write(b);
    }

    @Override
    public void close() throws IOException {
        if (close1) {
            o1.close();
        } else {
            o1.flush();
        }
        if (close2) {
            o2.close();
        } else {
            o2.flush();
        }
    }

    @Override
    public void flush() throws IOException {
        o1.flush();
        o2.flush();
    }
}
