/*
 * Copyright (c) 2026. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;
import java.util.stream.Stream;

/** Internal implementations of the prefixed object-store views. */
final class PrefixedObjectStores {

    private PrefixedObjectStores() {
        // utility class
    }

    static ReadableObjectStore readable(ReadableObjectStore delegate, URI prefix) {
        return new PrefixedReadableObjectStore(delegate, Prefix.create(delegate.getRoot(), prefix));
    }

    static WritableObjectStore writable(WritableObjectStore delegate, URI prefix) {
        return new PrefixedWritableObjectStore(delegate, Prefix.create(delegate.getRoot(), prefix));
    }

    static ObjectStore object(ObjectStore delegate, URI prefix) {
        return new PrefixedObjectStore(delegate, Prefix.create(delegate.getRoot(), prefix));
    }

    private static final class PrefixedReadableObjectStore implements ReadableObjectStore {
        private final ReadableObjectStore delegate;
        private final Prefix prefix;

        private PrefixedReadableObjectStore(ReadableObjectStore delegate, Prefix prefix) {
            this.delegate = delegate;
            this.prefix = prefix;
        }

        @Override
        public URI getRoot() {
            return prefix.root();
        }

        @Override
        public Stream<ObjectStore.ObjectInfo> list(URI path) throws IOException {
            return delegate.list(prefix.resolve(path)).map(prefix::relative);
        }

        @Override
        public InputStream openInputStream(URI path) throws IOException {
            return delegate.openInputStream(prefix.resolve(path));
        }

        @Override
        public Optional<ObjectStore.ObjectInfo> getInfo(URI path) throws IOException {
            return delegate.getInfo(prefix.resolve(path)).map(prefix::relative);
        }

        @Override
        public ReadableByteChannel openReadableByteChannel(URI path) throws IOException {
            return delegate.openReadableByteChannel(prefix.resolve(path));
        }

        @Override
        public void close() {
            // This view does not own the delegate.
        }
    }

    private static final class PrefixedWritableObjectStore implements WritableObjectStore {
        private final WritableObjectStore delegate;
        private final Prefix prefix;

        private PrefixedWritableObjectStore(WritableObjectStore delegate, Prefix prefix) {
            this.delegate = delegate;
            this.prefix = prefix;
        }

        @Override
        public URI getRoot() {
            return prefix.root();
        }

        @Override
        public long write(URI path, InputStream in, ObjectStore.OutputOption... options) throws IOException {
            return delegate.write(prefix.resolve(path), in, options);
        }

        @Override
        public long write(URI path, byte[] data, int from, int to, ObjectStore.OutputOption... options) throws IOException {
            return delegate.write(prefix.resolve(path), data, from, to, options);
        }

        @Override
        public OutputStream openOutputStream(URI path, ObjectStore.OutputOption... options) throws IOException {
            return delegate.openOutputStream(prefix.resolve(path), options);
        }

        @Override
        public void createFolder(URI path) throws IOException {
            delegate.createFolder(prefix.resolve(path));
        }

        @Override
        public WritableByteChannel openWritableByteChannel(URI path, ObjectStore.OutputOption... options) throws IOException {
            return delegate.openWritableByteChannel(prefix.resolve(path), options);
        }

        @Override
        public void close() {
            // This view does not own the delegate.
        }
    }

    private static final class PrefixedObjectStore implements ObjectStore {
        private final ObjectStore delegate;
        private final Prefix prefix;

        private PrefixedObjectStore(ObjectStore delegate, Prefix prefix) {
            this.delegate = delegate;
            this.prefix = prefix;
        }

        @Override
        public URI getRoot() {
            return prefix.root();
        }

        @Override
        public Stream<ObjectInfo> list(URI path) throws IOException {
            return delegate.list(prefix.resolve(path)).map(prefix::relative);
        }

        @Override
        public InputStream openInputStream(URI path) throws IOException {
            return delegate.openInputStream(prefix.resolve(path));
        }

        @Override
        public Optional<ObjectInfo> getInfo(URI path) throws IOException {
            return delegate.getInfo(prefix.resolve(path)).map(prefix::relative);
        }

        @Override
        public ReadableByteChannel openReadableByteChannel(URI path) throws IOException {
            return delegate.openReadableByteChannel(prefix.resolve(path));
        }

        @Override
        public long write(URI path, InputStream in, OutputOption... options) throws IOException {
            return delegate.write(prefix.resolve(path), in, options);
        }

        @Override
        public long write(URI path, byte[] data, int from, int to, OutputOption... options) throws IOException {
            return delegate.write(prefix.resolve(path), data, from, to, options);
        }

        @Override
        public OutputStream openOutputStream(URI path, OutputOption... options) throws IOException {
            return delegate.openOutputStream(prefix.resolve(path), options);
        }

        @Override
        public void createFolder(URI path) throws IOException {
            delegate.createFolder(prefix.resolve(path));
        }

        @Override
        public WritableByteChannel openWritableByteChannel(URI path, ObjectStore.OutputOption... options) throws IOException {
            return delegate.openWritableByteChannel(prefix.resolve(path), options);
        }

        @Override
        public void removeFolder(URI path) throws IOException {
            delegate.removeFolder(prefix.resolve(path));
        }

        @Override
        public void delete(URI path) throws IOException {
            delegate.delete(prefix.resolve(path));
        }

        @Override
        public void deleteRecursively(URI path) throws IOException {
            delegate.deleteRecursively(prefix.resolve(path));
        }

        @Override
        public AccessMode getAccessMode() {
            return delegate.getAccessMode();
        }

        @Override
        public void close() {
            // This view does not own the delegate.
        }
    }

    private record Prefix(URI path, URI root) {
        private static Prefix create(URI delegateRoot, URI prefix) {
            if (prefix.isAbsolute() || prefix.getPath() == null || prefix.getPath().startsWith("/")
                    || prefix.getQuery() != null || prefix.getFragment() != null) {
                throw new IllegalArgumentException("prefix must be a relative folder path: " + prefix);
            }
            URI folder = asFolder(prefix).normalize();
            if (folder.getPath().startsWith("../") || "..".equals(folder.getPath())) {
                throw new IllegalArgumentException("prefix must not point outside the store root: " + prefix);
            }
            return new Prefix(folder, delegateRoot.resolve(folder));
        }

        private URI resolve(URI path) throws IllegalPathException {
            if (path.isAbsolute()) {
                throw new AbsolutePathException("absolute path not allowed: " + path);
            }
            if (path.getQuery() != null || path.getFragment() != null || path.getPath() == null || path.getPath().startsWith("/")) {
                throw new IllegalPathException("invalid relative path: " + path);
            }
            URI resolved = this.path.resolve(path).normalize();
            if (!resolved.toString().startsWith(this.path.toString())) {
                throw new IllegalPathException("path points outside the prefixed store root: " + path);
            }
            return resolved;
        }

        private ObjectStore.ObjectInfo relative(ObjectStore.ObjectInfo info) {
            if (path.toString().isEmpty()) {
                return info;
            }
            URI uri = info.uri().normalize();
            URI relative = path.relativize(uri);
            if (relative.equals(uri)) {
                if (samePath(uri, path)) {
                    relative = URI.create("");
                } else {
                    throw new IllegalStateException("delegate returned an object outside the prefixed store root: " + uri);
                }
            }
            return new ObjectStore.ObjectInfo(relative, info.type(), info.size(), info.created(), info.lastModified());
        }

        private static boolean samePath(URI first, URI second) {
            return withoutTrailingSlash(first.toString()).equals(withoutTrailingSlash(second.toString()));
        }

        private static String withoutTrailingSlash(String value) {
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }

        private static URI asFolder(URI path) {
            String value = path.toString();
            return value.isEmpty() || value.endsWith("/") ? path : URI.create(value + "/");
        }
    }
}
