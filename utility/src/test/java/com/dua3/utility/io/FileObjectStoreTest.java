package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5778")
class FileObjectStoreTest extends AbstractObjectStoreTest {
    @Override
    protected ObjectStore createStore(Path root) throws IOException {
        return FileObjectStore.newObjectStore(root);
    }

    @Test
    void publicFactoryCreatesFileStore() throws Exception {
        try (ObjectStore store = ObjectStores.fileStore(tempDir.resolve("factory-store"))) {
            assertTrue(store.getRoot().isAbsolute());
            assertEquals(ObjectStore.AccessMode.READ_AND_WRITE, store.getAccessMode());
        }
    }

    @Test
    void resolve_validRelativeUri() throws Exception {
        Path root = tempDir.resolve("resolve-valid");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertEquals(root, store.resolve(URI.create("")));
            assertEquals(root, store.resolve(URI.create(".")));
            assertEquals(root.resolve("file.txt"), store.resolve(URI.create("file.txt")));
            assertEquals(root.resolve("a/b/c.txt"), store.resolve(URI.create("a/b/c.txt")));
            assertEquals(root.resolve("a/b.txt"), store.resolve(URI.create("a/../a/b.txt")));
            assertEquals(root.resolve("sub"), store.resolve(URI.create("sub/")));
        }
    }

    @Test
    void resolve_rejectsAbsoluteUri() throws Exception {
        Path root = tempDir.resolve("resolve-abs");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertThrows(AbsolutePathException.class, () -> store.resolve(URI.create("file:///tmp/test.txt")));
            assertThrows(AbsolutePathException.class, () -> store.resolve(URI.create("http://example.com/test.txt")));
            assertThrows(AbsolutePathException.class, () -> store.resolve(URI.create("https://localhost/a/b")));
        }
    }

    @Test
    void resolve_rejectsPathsOutsideRoot() throws Exception {
        Path root = tempDir.resolve("resolve-outside");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertThrows(IllegalPathException.class, () -> store.resolve(URI.create("..")));
            assertThrows(IllegalPathException.class, () -> store.resolve(URI.create("../outside")));
            assertThrows(IllegalPathException.class, () -> store.resolve(URI.create("sub/../../outside")));
        }
    }

    @Test
    void readableObjectStore_accessModeAndRoot() throws Exception {
        Path root = tempDir.resolve("readable-access");
        try (ReadableObjectStore readable = FileObjectStore.newReadableObjectStore(root)) {
            assertEquals(ObjectStore.AccessMode.READ, ((FileObjectStore) readable).getAccessMode());
            assertTrue(readable.getRoot().isAbsolute());
        }

        try (ReadableObjectStore readable = ObjectStores.readableFileStore(root)) {
            assertEquals(ObjectStore.AccessMode.READ, ((FileObjectStore) readable).getAccessMode());
            assertTrue(readable.getRoot().isAbsolute());
        }
    }

    @Test
    void readableObjectStore_allowsReadOperations() throws Exception {
        Path root = tempDir.resolve("readable-ops");
        try (ObjectStore seed = FileObjectStore.newObjectStore(root)) {
            seed.writeString(URI.create("hello.txt"), "hello world\nsecond line");
            seed.createFolder(URI.create("sub"));
            seed.write(URI.create("sub/nested.bin"), new byte[]{1, 2, 3});
        }

        try (ReadableObjectStore readable = FileObjectStore.newReadableObjectStore(root)) {
            assertEquals("hello world\nsecond line", readable.readString(URI.create("hello.txt")));
            assertArrayEquals("hello world\nsecond line".getBytes(StandardCharsets.UTF_8), readable.readAllBytes(URI.create("hello.txt")));

            try (InputStream in = readable.openInputStream(URI.create("hello.txt"))) {
                assertArrayEquals("hello world\nsecond line".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }

            try (ReadableByteChannel channel = readable.openReadableByteChannel(URI.create("sub/nested.bin"))) {
                ByteBuffer buf = ByteBuffer.allocate(3);
                while (buf.hasRemaining()) {
                    assertTrue(channel.read(buf) >= 0);
                }
                assertArrayEquals(new byte[]{1, 2, 3}, buf.array());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertEquals(3, readable.transferTo(URI.create("sub/nested.bin"), out));
            assertArrayEquals(new byte[]{1, 2, 3}, out.toByteArray());

            try (var lines = readable.lines(URI.create("hello.txt"))) {
                assertEquals(List.of("hello world", "second line"), lines.toList());
            }

            var fileInfo = readable.getInfo(URI.create("hello.txt")).orElseThrow();
            assertEquals(ObjectStore.ObjectType.DATA, fileInfo.type());

            var folderInfo = readable.getInfo(URI.create("sub")).orElseThrow();
            assertEquals(ObjectStore.ObjectType.FOLDER, folderInfo.type());

            try (var list = readable.list(URI.create(""))) {
                List<URI> uris = list.map(ObjectStore.ObjectInfo::uri).toList();
                assertTrue(uris.contains(URI.create("hello.txt")));
                assertTrue(uris.contains(URI.create("sub/")));
            }

            try (var walk = readable.walk(URI.create(""))) {
                List<URI> uris = walk.map(ObjectStore.ObjectInfo::uri).toList();
                assertTrue(uris.contains(URI.create("hello.txt")));
                assertTrue(uris.contains(URI.create("sub/nested.bin")));
            }

            try (var glob = readable.glob("*.txt")) {
                assertEquals(List.of(URI.create("hello.txt")), glob.toList());
            }
        }
    }

    @Test
    void readableObjectStore_rejectsWriteOperations() throws Exception {
        Path root = tempDir.resolve("readable-reject-write");
        try (ObjectStore seed = FileObjectStore.newObjectStore(root)) {
            seed.writeString(URI.create("file.txt"), "data");
            seed.createFolder(URI.create("folder"));
        }

        try (ReadableObjectStore readable = FileObjectStore.newReadableObjectStore(root)) {
            FileObjectStore fos = (FileObjectStore) readable;
            assertThrows(IllegalStateException.class, () -> fos.write(URI.create("new.txt"), new ByteArrayInputStream(new byte[0])));
            assertThrows(IllegalStateException.class, () -> fos.write(URI.create("new.txt"), new byte[]{1, 2}));
            assertThrows(IllegalStateException.class, () -> fos.write(URI.create("new.txt"), new byte[]{1, 2}, 0, 1));
            assertThrows(IllegalStateException.class, () -> fos.writeString(URI.create("new.txt"), "text"));
            assertThrows(IllegalStateException.class, () -> fos.openOutputStream(URI.create("new.txt")));
            assertThrows(IllegalStateException.class, () -> fos.openWritableByteChannel(URI.create("new.txt")));
            assertThrows(IllegalStateException.class, () -> fos.createFolder(URI.create("newFolder")));
            assertThrows(IllegalStateException.class, () -> fos.removeFolder(URI.create("folder")));
            assertThrows(IllegalStateException.class, () -> fos.delete(URI.create("file.txt")));
            assertThrows(IllegalStateException.class, () -> fos.deleteRecursively(URI.create("folder")));
            assertThrows(IllegalStateException.class, () -> fos.copy(URI.create("file.txt"), URI.create("copied.txt")));
            assertThrows(IllegalStateException.class, () -> fos.move(URI.create("file.txt"), URI.create("moved.txt")));
        }
    }

    @Test
    void writableObjectStore_accessModeAndRoot() throws Exception {
        Path root = tempDir.resolve("writable-access");
        try (WritableObjectStore writable = FileObjectStore.newWritableObjectStore(root)) {
            assertEquals(ObjectStore.AccessMode.WRITE, ((FileObjectStore) writable).getAccessMode());
            assertTrue(writable.getRoot().isAbsolute());
        }

        try (WritableObjectStore writable = ObjectStores.writableFileStore(root)) {
            assertEquals(ObjectStore.AccessMode.WRITE, ((FileObjectStore) writable).getAccessMode());
            assertTrue(writable.getRoot().isAbsolute());
        }
    }

    @Test
    void writableObjectStore_allowsWriteOperations() throws Exception {
        Path root = tempDir.resolve("writable-ops");
        try (WritableObjectStore writable = FileObjectStore.newWritableObjectStore(root)) {
            writable.createFolder(URI.create("sub"));
            assertTrue(Files.isDirectory(root.resolve("sub")));

            writable.write(URI.create("sub/stream.txt"), new ByteArrayInputStream("stream".getBytes(StandardCharsets.UTF_8)));
            assertEquals("stream", Files.readString(root.resolve("sub/stream.txt")));

            writable.write(URI.create("sub/bytes.txt"), "bytes".getBytes(StandardCharsets.UTF_8));
            assertEquals("bytes", Files.readString(root.resolve("sub/bytes.txt")));

            writable.write(URI.create("sub/slice.txt"), "0123456789".getBytes(StandardCharsets.UTF_8), 2, 6);
            assertEquals("2345", Files.readString(root.resolve("sub/slice.txt")));

            writable.writeString(URI.create("sub/string.txt"), "string");
            assertEquals("string", Files.readString(root.resolve("sub/string.txt")));

            try (OutputStream out = writable.openOutputStream(URI.create("sub/stream-out.txt"))) {
                out.write("stream-out".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("stream-out", Files.readString(root.resolve("sub/stream-out.txt")));

            try (WritableByteChannel channel = writable.openWritableByteChannel(URI.create("sub/channel.bin"))) {
                channel.write(ByteBuffer.wrap(new byte[]{1, 2, 3}));
            }
            assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(root.resolve("sub/channel.bin")));

            FileObjectStore fos = (FileObjectStore) writable;
            fos.delete(URI.create("sub/bytes.txt"));
            assertFalse(Files.exists(root.resolve("sub/bytes.txt")));

            fos.createFolder(URI.create("emptyFolder"));
            assertTrue(Files.isDirectory(root.resolve("emptyFolder")));
            fos.removeFolder(URI.create("emptyFolder"));
            assertFalse(Files.exists(root.resolve("emptyFolder")));

            fos.deleteRecursively(URI.create("sub"));
            assertFalse(Files.exists(root.resolve("sub")));
        }
    }

    @Test
    void writableObjectStore_rejectsReadOperations() throws Exception {
        Path root = tempDir.resolve("writable-reject-read");
        try (ObjectStore seed = FileObjectStore.newObjectStore(root)) {
            seed.writeString(URI.create("file.txt"), "data");
            seed.createFolder(URI.create("folder"));
        }

        try (WritableObjectStore writable = FileObjectStore.newWritableObjectStore(root)) {
            FileObjectStore fos = (FileObjectStore) writable;
            assertThrows(IllegalStateException.class, () -> fos.list(URI.create("")));
            assertThrows(IllegalStateException.class, () -> fos.getInfo(URI.create("file.txt")));
            assertThrows(IllegalStateException.class, () -> fos.openInputStream(URI.create("file.txt")));
            assertThrows(IllegalStateException.class, () -> fos.openReadableByteChannel(URI.create("file.txt")));
            assertThrows(IllegalStateException.class, () -> fos.copy(URI.create("file.txt"), URI.create("copy.txt")));
            assertThrows(IllegalStateException.class, () -> fos.move(URI.create("file.txt"), URI.create("move.txt")));
        }
    }

    @Test
    void copyToAndMoveTo_betweenStores() throws Exception {
        Path sourceRoot = tempDir.resolve("copyto-source");
        Path targetRoot = tempDir.resolve("copyto-target");

        try (FileObjectStore sourceStore = FileObjectStore.newObjectStore(sourceRoot);
             FileObjectStore targetStore = FileObjectStore.newObjectStore(targetRoot)) {
            sourceStore.writeString(URI.create("data.txt"), "cross store content");

            sourceStore.copyTo(targetStore, URI.create("data.txt"), URI.create("copy/target.txt"));
            assertEquals("cross store content", targetStore.readString(URI.create("copy/target.txt")));
            assertEquals("cross store content", sourceStore.readString(URI.create("data.txt")));

            sourceStore.moveTo(targetStore, URI.create("data.txt"), URI.create("move/target.txt"));
            assertEquals("cross store content", targetStore.readString(URI.create("move/target.txt")));
            assertTrue(sourceStore.getInfo(URI.create("data.txt")).isEmpty());
        }
    }

    @Test
    void copyToAndMoveTo_errorConditions() throws Exception {
        Path sourceRoot = tempDir.resolve("copyto-err-source");
        Path targetRoot = tempDir.resolve("copyto-err-target");

        try (FileObjectStore sourceStore = FileObjectStore.newObjectStore(sourceRoot);
             FileObjectStore targetStore = FileObjectStore.newObjectStore(targetRoot)) {
            sourceStore.writeString(URI.create("file.txt"), "source data");
            sourceStore.createFolder(URI.create("dir"));
            targetStore.writeString(URI.create("exists.txt"), "existing");

            assertThrows(ObjectNotFoundException.class, () -> sourceStore.copyTo(targetStore, URI.create("missing.txt"), URI.create("out.txt")));
            assertThrows(IOException.class, () -> sourceStore.copyTo(targetStore, URI.create("dir"), URI.create("out.txt")));
            assertThrows(ObjectExistsException.class, () -> sourceStore.copyTo(targetStore, URI.create("file.txt"), URI.create("exists.txt"), ObjectStore.OutputOption.CREATE_NEW));

            assertThrows(ObjectNotFoundException.class, () -> sourceStore.moveTo(targetStore, URI.create("missing.txt"), URI.create("out.txt")));
            assertThrows(IOException.class, () -> sourceStore.moveTo(targetStore, URI.create("dir"), URI.create("out.txt")));
            assertThrows(ObjectExistsException.class, () -> sourceStore.moveTo(targetStore, URI.create("file.txt"), URI.create("exists.txt"), ObjectStore.OutputOption.CREATE_NEW));

            try (FileObjectStore readOnlyTarget = (FileObjectStore) FileObjectStore.newReadableObjectStore(targetRoot)) {
                assertThrows(IllegalStateException.class, () -> sourceStore.copyTo(readOnlyTarget, URI.create("file.txt"), URI.create("out.txt")));
                assertThrows(IllegalStateException.class, () -> sourceStore.moveTo(readOnlyTarget, URI.create("file.txt"), URI.create("out.txt")));
            }

            try (FileObjectStore writeOnlySource = (FileObjectStore) FileObjectStore.newWritableObjectStore(sourceRoot)) {
                assertThrows(IllegalStateException.class, () -> writeOnlySource.copyTo(targetStore, URI.create("file.txt"), URI.create("out.txt")));
                assertThrows(IllegalStateException.class, () -> writeOnlySource.moveTo(targetStore, URI.create("file.txt"), URI.create("out.txt")));
            }
        }
    }

    @Test
    void writeByteArray_boundsValidation() throws Exception {
        Path root = tempDir.resolve("write-bounds");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            byte[] data = new byte[10];
            assertThrows(IndexOutOfBoundsException.class, () -> store.write(URI.create("x.txt"), data, -1, 5));
            assertThrows(IndexOutOfBoundsException.class, () -> store.write(URI.create("x.txt"), data, 5, 4));
            assertThrows(IndexOutOfBoundsException.class, () -> store.write(URI.create("x.txt"), data, 0, 11));
        }
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    @Test
    void ensureCanWrite_validation() throws Exception {
        Path root = tempDir.resolve("ensure-can-write");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertThrows(IllegalArgumentException.class, () -> store.write(
                    URI.create("x.txt"),
                    new byte[0],
                    ObjectStore.OutputOption.CREATE_NEW,
                    ObjectStore.OutputOption.CREATE_OR_REPLACE
            ));

            store.createFolder(URI.create("folder"));
            assertThrows(IOException.class, () -> store.write(
                    URI.create("folder"),
                    new byte[]{1},
                    ObjectStore.OutputOption.CREATE_OR_REPLACE
            ));
        }
    }

    @Test
    void createFolder_whenDataObjectExistsThrowsNotAFolderException() throws Exception {
        Path root = tempDir.resolve("create-folder-err");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            store.writeString(URI.create("data"), "content");
            assertThrows(NotAFolderException.class, () -> store.createFolder(URI.create("data")));
        }
    }

    @Test
    void removeFolder_errorConditions() throws Exception {
        Path root = tempDir.resolve("remove-folder-err");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertThrows(ObjectNotFoundException.class, () -> store.removeFolder(URI.create("missingFolder")));

            store.writeString(URI.create("file.txt"), "content");
            assertThrows(IOException.class, () -> store.removeFolder(URI.create("file.txt")));
        }
    }

    @Test
    void deleteAndRecursiveDelete_missingObjectThrows() throws Exception {
        Path root = tempDir.resolve("delete-missing");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            assertThrows(ObjectNotFoundException.class, () -> store.delete(URI.create("missing.txt")));
            assertThrows(ObjectNotFoundException.class, () -> store.deleteRecursively(URI.create("missing.txt")));
        }
    }

    @Test
    void openInputStreamAndChannel_folderOrMissingThrows() throws Exception {
        Path root = tempDir.resolve("open-input-err");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            store.createFolder(URI.create("folder"));

            assertThrows(IOException.class, () -> store.openInputStream(URI.create("folder")));
            assertThrows(IOException.class, () -> store.openReadableByteChannel(URI.create("folder")));

            assertThrows(ObjectNotFoundException.class, () -> store.openInputStream(URI.create("missing.txt")));
            assertThrows(ObjectNotFoundException.class, () -> store.openReadableByteChannel(URI.create("missing.txt")));
        }
    }

    @Test
    void list_errorConditions() throws Exception {
        Path root = tempDir.resolve("list-err");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            store.writeString(URI.create("file.txt"), "content");
            assertThrows(IOException.class, () -> store.list(URI.create("file.txt")));
            assertThrows(ObjectNotFoundException.class, () -> store.list(URI.create("missing")));
        }
    }

    @Test
    void toStringAndClose() throws Exception {
        Path root = tempDir.resolve("to-string");
        try (FileObjectStore store = FileObjectStore.newObjectStore(root)) {
            String str = store.toString();
            assertTrue(str.contains("FileObjectStore"));
            assertTrue(str.contains(store.getRoot().toString()));
            assertTrue(str.contains("READ_AND_WRITE"));

            store.close();
            // closing again is safe
            //noinspection RedundantExplicitClose
            store.close();
        }
    }
}
