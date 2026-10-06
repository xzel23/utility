package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectStoresTest {

    @TempDir
    Path tempDir;

    @Test
    void copyBetweenFileStoresUsesFileStoreSemantics() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");
        URI source = URI.create("nested/source.txt");
        URI target = URI.create("copied/result.txt");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(source, "copied content".getBytes(StandardCharsets.UTF_8));

            ObjectStores.copy(sourceStore, source, targetStore, target, ObjectStore.OutputOption.CREATE_NEW);

            assertEquals("copied content", Files.readString(targetRoot.resolve("copied/result.txt")));
            assertEquals("copied content", sourceStore.readString(source));
        }
    }

    @Test
    void moveBetweenFileStoresReplacesTargetAndDeletesSource() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");
        URI source = URI.create("source.txt");
        URI target = URI.create("nested/result.txt");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(source, "new content".getBytes(StandardCharsets.UTF_8));
            targetStore.write(target, "old content".getBytes(StandardCharsets.UTF_8));

            ObjectStores.move(sourceStore, source, targetStore, target, ObjectStore.OutputOption.CREATE_OR_REPLACE);

            assertSame(ObjectStore.ObjectType.MISSING, sourceStore.getInfo(source).type());
            assertEquals("new content", targetStore.readString(target));
        }
    }

    @Test
    void copyAndMoveFallbackWorksForPrefixedStoreViews() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(URI.create("copy.txt"), "copy".getBytes(StandardCharsets.UTF_8));
            sourceStore.write(URI.create("move.txt"), "move".getBytes(StandardCharsets.UTF_8));

            ObjectStore sourceView = sourceStore.prefixed(URI.create(""));
            ObjectStore targetView = targetStore.prefixed(URI.create(""));

            ObjectStores.copy(sourceView, URI.create("copy.txt"), targetView, URI.create("copy.txt"));
            ObjectStores.move(sourceView, URI.create("move.txt"), targetView, URI.create("move.txt"));

            assertEquals("copy", targetStore.readString(URI.create("copy.txt")));
            assertEquals("move", targetStore.readString(URI.create("move.txt")));
            assertSame(ObjectStore.ObjectType.MISSING, sourceStore.getInfo(URI.create("move.txt")).type());
        }
    }

    @Test
    @SuppressWarnings("java:S5778")
    void objectStoreDefaultMethods_copyMoveDeleteRecursivelyAndAccessMode() throws Exception {
        Path root = tempDir.resolve("default-methods");
        try (ObjectStore fileStore = ObjectStores.fileStore(root)) {
            // An ObjectStore delegator that DOES NOT override the interface default copy/move/deleteRecursively
            ObjectStore defaultStore = new ObjectStore() {
                @Override
                public URI getRoot() {
                    return fileStore.getRoot();
                }

                @Override
                public java.util.stream.Stream<ObjectInfo> list(URI path) throws java.io.IOException {
                    return fileStore.list(path);
                }

                @Override
                public java.io.InputStream openInputStream(URI path) throws java.io.IOException {
                    return fileStore.openInputStream(path);
                }

                @Override
                public ObjectInfo getInfo(URI path) throws java.io.IOException {
                    return fileStore.getInfo(path);
                }

                @Override
                public java.nio.channels.ReadableByteChannel openReadableByteChannel(URI path) throws java.io.IOException {
                    return fileStore.openReadableByteChannel(path);
                }

                @Override
                public long write(URI path, java.io.InputStream in, OutputOption... options) throws java.io.IOException {
                    return fileStore.write(path, in, options);
                }

                @Override
                public long write(URI path, byte[] data, int from, int to, OutputOption... options) throws java.io.IOException {
                    return fileStore.write(path, data, from, to, options);
                }

                @Override
                public java.io.OutputStream openOutputStream(URI path, OutputOption... options) throws java.io.IOException {
                    return fileStore.openOutputStream(path, options);
                }

                @Override
                public void createFolder(URI path) throws java.io.IOException {
                    fileStore.createFolder(path);
                }

                @Override
                public java.nio.channels.WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) throws java.io.IOException {
                    return fileStore.openWritableByteChannel(path, options);
                }

                @Override
                public void removeFolder(URI path) throws java.io.IOException {
                    fileStore.removeFolder(path);
                }

                @Override
                public void delete(URI path) throws java.io.IOException {
                    fileStore.delete(path);
                }

                @Override
                public AccessMode getAccessMode() {
                    return AccessMode.READ_AND_WRITE;
                }

                @Override
                public void close() throws java.io.IOException {
                    fileStore.close();
                }
            };

            // Test default copy
            defaultStore.writeString(URI.create("orig.txt"), "hello orig");
            defaultStore.copy(URI.create("orig.txt"), URI.create("copied.txt"), ObjectStore.OutputOption.CREATE_NEW);
            assertEquals("hello orig", defaultStore.readString(URI.create("copied.txt")));
            assertEquals("hello orig", defaultStore.readString(URI.create("orig.txt")));

            // Test default move
            defaultStore.move(URI.create("copied.txt"), URI.create("moved.txt"), ObjectStore.OutputOption.CREATE_NEW);
            assertEquals("hello orig", defaultStore.readString(URI.create("moved.txt")));
            assertSame(ObjectStore.ObjectType.MISSING, defaultStore.getInfo(URI.create("copied.txt")).type());

            // Test default deleteRecursively on DATA
            defaultStore.deleteRecursively(URI.create("moved.txt"));
            assertSame(ObjectStore.ObjectType.MISSING, defaultStore.getInfo(URI.create("moved.txt")).type());

            // Test default deleteRecursively on FOLDER
            defaultStore.createFolder(URI.create("tree/nested"));
            defaultStore.writeString(URI.create("tree/nested/leaf.txt"), "leaf");
            defaultStore.writeString(URI.create("tree/root_leaf.txt"), "root_leaf");
            defaultStore.deleteRecursively(URI.create("tree"));
            assertSame(ObjectStore.ObjectType.MISSING, defaultStore.getInfo(URI.create("tree/nested/leaf.txt")).type());
            assertSame(ObjectStore.ObjectType.MISSING, defaultStore.getInfo(URI.create("tree/root_leaf.txt")).type());

            // Test default deleteRecursively on missing throws ObjectNotFoundException
            org.junit.jupiter.api.Assertions.assertThrows(ObjectNotFoundException.class, () -> defaultStore.deleteRecursively(URI.create("missing")));

            // Test default deleteRecursively on UNKNOWN type throws UnsupportedOperationException
            ObjectStore unknownStore = new ObjectStore() {
                @Override public URI getRoot() { return fileStore.getRoot(); }
                @Override public java.util.stream.Stream<ObjectInfo> list(URI path) { return java.util.stream.Stream.empty(); }
                @Override public java.io.InputStream openInputStream(URI path) { throw new UnsupportedOperationException(); }
                @Override public ObjectInfo getInfo(URI path) { return new ObjectStore.ObjectInfo(path, ObjectStore.ObjectType.UNKNOWN, ObjectStore.ObjectInfo.UNKNOWN_SIZE, Instant.MIN, Instant.MIN); }
                @Override public java.nio.channels.ReadableByteChannel openReadableByteChannel(URI path) { throw new UnsupportedOperationException(); }
                @Override public long write(URI path, java.io.InputStream in, OutputOption... options) { throw new UnsupportedOperationException(); }
                @Override public long write(URI path, byte[] data, int from, int to, OutputOption... options) { throw new UnsupportedOperationException(); }
                @Override public java.io.OutputStream openOutputStream(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
                @Override public void createFolder(URI path) {/* ignored */}
                @Override public java.nio.channels.WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
                @Override public void removeFolder(URI path) {/* ignored */}
                @Override public void delete(URI path) {/* ignored */}
                @Override public AccessMode getAccessMode() { return AccessMode.READ_AND_WRITE; }
                @Override public void close() {/* ignored */}
            };
            org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class, () -> unknownStore.deleteRecursively(URI.create("unknown")));
        }
    }

    @Test
    void objectStoreAccessModesAndAssertions() {
        ObjectStore readOnly = new ObjectStore() {
            @Override public URI getRoot() { return URI.create("file:///tmp/"); }
            @Override public java.util.stream.Stream<ObjectInfo> list(URI path) { return java.util.stream.Stream.empty(); }
            @Override public java.io.InputStream openInputStream(URI path) { return new java.io.ByteArrayInputStream(LangUtil.EMPTY_BYTE_ARRAY); }
            @Override public ObjectInfo getInfo(URI path) { return new ObjectStore.ObjectInfo(URI.create(""), ObjectStore.ObjectType.DATA, ObjectStore.ObjectInfo.UNKNOWN_SIZE, Instant.MIN, Instant.MIN); }
            @Override public java.nio.channels.ReadableByteChannel openReadableByteChannel(URI path) { throw new UnsupportedOperationException(); }
            @Override public long write(URI path, java.io.InputStream in, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public long write(URI path, byte[] data, int from, int to, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public java.io.OutputStream openOutputStream(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public void createFolder(URI path) {/* do nothing */}
            @Override public java.nio.channels.WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public void removeFolder(URI path) {/* do nothing */}
            @Override public void delete(URI path) {/* do nothing */}
            @Override public AccessMode getAccessMode() { return AccessMode.READ; }
            @Override public void close() {/* do nothing */}
        };

        assertTrue(readOnly.isReadable());
        org.junit.jupiter.api.Assertions.assertFalse(readOnly.isWritable());
        readOnly.assertReadable();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, readOnly::assertWritable);

        ObjectStore writeOnly = new ObjectStore() {
            @Override public URI getRoot() { return URI.create("file:///tmp/"); }
            @Override public java.util.stream.Stream<ObjectInfo> list(URI path) { return java.util.stream.Stream.empty(); }
            @Override public java.io.InputStream openInputStream(URI path) { return new java.io.ByteArrayInputStream(LangUtil.EMPTY_BYTE_ARRAY); }
            @Override public ObjectInfo getInfo(URI path) { return new ObjectStore.ObjectInfo(URI.create(""), ObjectStore.ObjectType.DATA, ObjectStore.ObjectInfo.UNKNOWN_SIZE, Instant.MIN, Instant.MIN); }
            @Override public java.nio.channels.ReadableByteChannel openReadableByteChannel(URI path) { throw new UnsupportedOperationException(); }
            @Override public long write(URI path, java.io.InputStream in, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public long write(URI path, byte[] data, int from, int to, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public java.io.OutputStream openOutputStream(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public void createFolder(URI path) {/* do nothing */}
            @Override public java.nio.channels.WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) { throw new UnsupportedOperationException(); }
            @Override public void removeFolder(URI path) {/* do nothing */}
            @Override public void delete(URI path) {/* do nothing */}
            @Override public AccessMode getAccessMode() { return AccessMode.WRITE; }
            @Override public void close() {/* do nothing */}
        };

        org.junit.jupiter.api.Assertions.assertFalse(writeOnly.isReadable());
        assertTrue(writeOnly.isWritable());
        writeOnly.assertWritable();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, writeOnly::assertReadable);
    }
}
