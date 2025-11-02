package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A utility class that provides functionalities for producing
 * {@link InputStream} or {@link OutputStream} instances from various object types.
 * This is suitable for objects that represent resources such as files, paths,
 * URIs, and readers.
 * <p>
 * The specific implementation for each input or output stream operation is
 * determined based on the type of the provided object. Unsupported object
 * types will result in an {@link UnsupportedOperationException} when accessed.
 *
 * @param <V> The type of object handled by this instance.
 */
final class StreamSupplier<V> {

    private static final StreamSupplier<Object> UNSUPPORTED = def(Object.class, StreamSupplier::inputUnsupported, StreamSupplier::outputUnsupported);
    private static final List<StreamSupplier<?>> streamSuppliers = List.of(
            def(InputStream.class, v -> v, StreamSupplier::outputUnsupported),
            def(OutputStream.class, StreamSupplier::inputUnsupported, v -> v),
            def(URI.class, v -> IoUtil.toURL(v).openStream(), v -> Files.newOutputStream(IoUtil.toPath(v))),
            def(URL.class, URL::openStream, v -> Files.newOutputStream(IoUtil.toPath(v))),
            def(Path.class, Files::newInputStream, Files::newOutputStream),
            def(File.class, v -> Files.newInputStream(v.toPath()), v -> Files.newOutputStream(v.toPath())),
            def(Reader.class, StreamSupplier::readerToInputStream, StreamSupplier::outputUnsupported)
    );
    private final Class<V> clazz;
    private final InputStreamSupplier<V> iss;
    private final OutputStreamSupplier<V> oss;

    private StreamSupplier(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        this.clazz = clazz;
        this.iss = iss;
        this.oss = oss;
    }

    private static InputStream inputUnsupported(Object o) {
        throw new UnsupportedOperationException("InputStream creation not supported: " + o.getClass().getName());
    }

    private static OutputStream outputUnsupported(Object o) {
        throw new UnsupportedOperationException("OutputStream creation not supported: " + o.getClass().getName());
    }

    private static <V> StreamSupplier<V> def(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        return new StreamSupplier<>(clazz, iss, oss);
    }

    private static InputStream readerToInputStream(Reader reader) {
        return new UnicodeReaderInputStream(reader);
    }

    @SuppressWarnings("unchecked")
    private static <C> StreamSupplier<? super C> supplier(C o) {
        return streamSuppliers.stream()
                .filter(s -> s.clazz.isInstance(o))
                .findFirst().<StreamSupplier<? super C>>map(s -> (StreamSupplier<? super C>) s)
                .orElse(UNSUPPORTED);
    }

    public static <C> InputStream getInputStream(C o) throws IOException {
        return supplier(o).iss.getInputStream(o);
    }

    public static OutputStream getOutputStream(Object o) throws IOException {
        return supplier(o).oss.getOutputStream(o);
    }

    @FunctionalInterface
    interface InputStreamSupplier<C> {
        InputStream getInputStream(C connection) throws IOException;
    }

    @FunctionalInterface
    interface OutputStreamSupplier<C> {
        OutputStream getOutputStream(C connection) throws IOException;
    }

    private static final class UnicodeReaderInputStream extends InputStream {
        private final Reader reader;
        private byte[] buffer;
        private int bufferPos;
        private int bufferLen;
        private final char[] cbuf;
        private int carry; // stores a trailing high surrogate to be processed with the next read
        private boolean eof;

        private UnicodeReaderInputStream(Reader reader) {
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
}
