package com.dua3.utility.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class StreamSupplier<V> {

    @FunctionalInterface
    interface InputStreamSupplier<C> {
        InputStream getInputStream(C connection) throws IOException;
    }

    @FunctionalInterface
    interface OutputStreamSupplier<C> {
        OutputStream getOutputStream(C connection) throws IOException;
    }

    private static final StreamSupplier<Object> UNSUPPORTED = def(Object.class, StreamSupplier::inputUnsupported, StreamSupplier::outputUnsupported);

    private static final List<StreamSupplier<?>> streamSuppliers = List.of(
        def(InputStream.class, v -> v, StreamSupplier::outputUnsupported),
        def(OutputStream.class, StreamSupplier::inputUnsupported, v -> v),
        def(URI.class, v -> IoUtil.toURL(v).openStream(), v -> Files.newOutputStream(IoUtil.toPath(v))),
        def(URL.class, URL::openStream, v -> Files.newOutputStream(IoUtil.toPath(v))),
        def(Path.class, Files::newInputStream, Files::newOutputStream),
        def(File.class, v -> Files.newInputStream(v.toPath()), v -> Files.newOutputStream(v.toPath()))
    );

    private static InputStream inputUnsupported(Object o) {
        throw new UnsupportedOperationException("InputStream creation not supported: " + o.getClass().getName());
    }

    private static OutputStream outputUnsupported(Object o) {
        throw new UnsupportedOperationException("OutputStream creation not supported: " + o.getClass().getName());
    }

    private final Class<V> clazz;
    private final InputStreamSupplier<V> iss;
    private final OutputStreamSupplier<V> oss;

    private StreamSupplier(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        this.clazz = clazz;
        this.iss = iss;
        this.oss = oss;
    }

    private static <V> StreamSupplier<V> def(Class<V> clazz, InputStreamSupplier<V> iss, OutputStreamSupplier<V> oss) {
        return new StreamSupplier<>(clazz, iss, oss);
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
}
