package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractObjectStoreTest {

    @TempDir
    Path tempDir;

    protected abstract ObjectStore createStore(Path root) throws IOException;

    @Test
    void getRoot_isAbsolute() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            assertTrue(store.getRoot().isAbsolute());
        }
    }

    @Test
    void createFolder_andList() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("a/b"));

            List<ObjectStore.ObjectInfo> rootEntries;
            try (var stream = store.list(URI.create(""))) {
                rootEntries = stream.toList();
            }

            assertEquals(1, rootEntries.size());
            assertEquals(URI.create("a/"), rootEntries.getFirst().uri());
            assertEquals(ObjectStore.ObjectType.FOLDER, rootEntries.getFirst().type());
        }
    }

    @Test
    void list_throwsForMissingObject() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            assertThrows(ObjectNotFoundException.class, () -> store.list(URI.create("missing")));
        }
    }

    @Test
    void writeAndRead_viaInputStreamMethod() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
            long written = store.write(URI.create("folder/data.txt"), new ByteArrayInputStream(data));
            assertEquals(data.length, written);

            byte[] actual;
            try (InputStream in = store.openInputStream(URI.create("folder/data.txt"))) {
                actual = in.readAllBytes();
            }

            assertArrayEquals(data, actual);
        }
    }

    @Test
    void writeByteArray_withBounds() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            byte[] data = "0123456789".getBytes(StandardCharsets.UTF_8);
            long written = store.write(URI.create("slice.txt"), data, 2, 6, ObjectStore.OutputOption.CREATE_NEW);
            assertEquals(4, written);

            try (InputStream in = store.openInputStream(URI.create("slice.txt"))) {
                assertArrayEquals("2345".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
        }
    }

    @Test
    void openOutputStream_createsData() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            try (OutputStream out = store.openOutputStream(URI.create("x/y.txt"), ObjectStore.OutputOption.CREATE_NEW)) {
                out.write("data".getBytes(StandardCharsets.UTF_8));
            }

            try (InputStream in = store.openInputStream(URI.create("x/y.txt"))) {
                assertArrayEquals("data".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
        }
    }

    @Test
    void createNew_failsIfObjectExists() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("exists.txt"), "1".getBytes(StandardCharsets.UTF_8));
            assertThrows(ObjectExistsException.class, () -> store.write(URI.create("exists.txt"), "2".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_NEW));
        }
    }

    @Test
    void getInfo_returnsMetadataForDataAndFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("folder"));
            store.write(URI.create("folder/data.bin"), new byte[]{1, 2, 3});

            ObjectStore.ObjectInfo folderInfo = store.getInfo(URI.create("folder")).orElseThrow();
            assertEquals(ObjectStore.ObjectType.FOLDER, folderInfo.type());

            ObjectStore.ObjectInfo dataInfo = store.getInfo(URI.create("folder/data.bin")).orElseThrow();
            assertEquals(ObjectStore.ObjectType.DATA, dataInfo.type());
            assertEquals(3, dataInfo.size());
            assertNotNull(dataInfo.created());
            assertNotNull(dataInfo.lastModified());

            assertTrue(store.getInfo(URI.create("folder/missing.bin")).isEmpty());
        }
    }

    @Test
    void delete_andRemoveFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("f"));
            store.write(URI.create("f/data.txt"), "x".getBytes(StandardCharsets.UTF_8));

            store.delete(URI.create("f/data.txt"));
            assertTrue(store.getInfo(URI.create("f/data.txt")).isEmpty());

            store.removeFolder(URI.create("f"));
            assertTrue(store.getInfo(URI.create("f")).isEmpty());
        }
    }

    @Test
    void removeFolder_failsForNonEmptyFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("f"));
            store.write(URI.create("f/a.txt"), "x".getBytes(StandardCharsets.UTF_8));
            assertThrows(FolderNotEmptyException.class, () -> store.removeFolder(URI.create("f")));
        }
    }

    @Test
    void deleteRecursively_removesTree() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("a/b/c.txt"), "x".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            store.write(URI.create("a/b/d.txt"), "y".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            store.deleteRecursively(URI.create("a"));
            assertTrue(store.getInfo(URI.create("a")).isEmpty());
        }
    }

    @Test
    void walk_returnsExpectedEntriesAndRespectsDepth() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("a/b/c.txt"), "1".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            store.write(URI.create("a/d.txt"), "2".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            List<URI> all;
            try (var s = store.walk(URI.create("a"))) {
                all = s.map(ObjectStore.ObjectInfo::uri).toList();
            }

            assertTrue(all.contains(URI.create("a")) || all.contains(URI.create("a/")));
            assertTrue(all.contains(URI.create("a/b/")) || all.contains(URI.create("a/b")));
            assertTrue(all.contains(URI.create("a/b/c.txt")));
            assertTrue(all.contains(URI.create("a/d.txt")));

            List<URI> depth1;
            try (var s = store.walk(URI.create("a"), 1)) {
                depth1 = s.map(ObjectStore.ObjectInfo::uri).toList();
            }

            assertFalse(depth1.contains(URI.create("a/b/c.txt")));
            assertTrue(depth1.contains(URI.create("a/d.txt")));
        }
    }

    @Test
    void methods_rejectAbsoluteAndIllegalPaths() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI absolute = URI.create("file:///tmp/x");
            URI illegal = URI.create("../outside");

            assertThrows(AbsolutePathException.class, () -> store.getInfo(absolute));
            assertThrows(IllegalPathException.class, () -> store.getInfo(illegal));
        }
    }
}