package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

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
    void writeString_usesUtf8AndReturnsByteCount() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            String text = "Grüße 🌍\n第二行";

            assertEquals(text.getBytes(StandardCharsets.UTF_8).length, store.writeString(path, text));
            assertEquals(text, store.readString(path));
            assertArrayEquals(text.getBytes(StandardCharsets.UTF_8), store.readAllBytes(path));
        }
    }

    @Test
    void writeString_usesRequestedCharsetAndAcceptsAnyCharSequence() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            Charset charset = StandardCharsets.UTF_16LE;
            CharSequence text = new StringBuilder("äöü");

            assertEquals(text.toString().getBytes(charset).length,
                    store.writeString(path, text, charset));
            assertEquals(text.toString(), store.readString(path, charset));
        }
    }

    @Test
    void writeString_nullWritesStringNull() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("null.txt");

            assertEquals(4, store.writeString(path, null));
            assertEquals("null", store.readString(path));
        }
    }

    @Test
    void writeString_defaultsToCreateNewAndSupportsReplace() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            store.writeString(path, "old");

            assertThrows(ObjectExistsException.class, () -> store.writeString(path, "new"));
            assertEquals(3, store.writeString(path, "new", ObjectStore.OutputOption.CREATE_OR_REPLACE));
            assertEquals("new", store.readString(path));
        }
    }

    @Test
    void lines_readsAllLinesWithDefaultAndRequestedCharset() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI utf8Path = URI.create("lines.txt");
            store.writeString(utf8Path, "one\r\ntwo\nthree\r\nfour");
            try (var lines = store.lines(utf8Path)) {
                assertEquals(List.of("one", "two", "three", "four"), lines.toList());
            }

            URI utf16Path = URI.create("lines-utf16.txt");
            store.writeString(utf16Path, "erste\nzweite\n第三", StandardCharsets.UTF_16);
            try (var lines = store.lines(utf16Path, StandardCharsets.UTF_16)) {
                assertEquals(List.of("erste", "zweite", "第三"), lines.toList());
            }
        }
    }

    @Test
    void lines_emptyFileReturnsEmptyStream() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("empty.txt");
            store.writeString(path, "");

            try (var lines = store.lines(path)) {
                assertTrue(lines.toList().isEmpty());
            }
        }
    }

    @Test
    void transferToCopiesBytesAndReturnsByteCount() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("data.bin");
            byte[] expected = {0, 1, 2, 127, -1};
            store.write(path, expected);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            assertEquals(expected.length, store.transferTo(path, out));
            assertArrayEquals(expected, out.toByteArray());
        }
    }

    @Test
    void readConvenienceMethods_propagateMissingObject() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI missing = URI.create("missing");

            assertThrows(IOException.class, () -> store.readAllBytes(missing));
            assertThrows(IOException.class, () -> store.readString(missing));
            assertThrows(IOException.class, () -> store.transferTo(missing, new ByteArrayOutputStream()));
            assertThrows(IOException.class, () -> store.lines(missing));
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
    void writeByteArray_forwardsOutputOptions() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("exists.txt");
            store.write(path, "old".getBytes(StandardCharsets.UTF_8));

            store.write(path, "new".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            try (InputStream in = store.openInputStream(path)) {
                assertArrayEquals("new".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
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
    void list_encodesReservedCharactersInObjectUris() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("folder/data%20file.txt");
            store.write(path, "data".getBytes(StandardCharsets.UTF_8));

            try (var entries = store.list(URI.create("folder"))) {
                assertEquals(List.of(path), entries.map(ObjectStore.ObjectInfo::uri).toList());
            }
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
    void glob_findsDataAndFoldersRelativeToTheStoreRoot() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("reports/current/result.txt"), "current".getBytes(StandardCharsets.UTF_8));
            store.write(URI.create("reports/current/result.json"), "current".getBytes(StandardCharsets.UTF_8));
            store.write(URI.create("reports/archive/result.txt"), "archive".getBytes(StandardCharsets.UTF_8));
            store.write(URI.create("reports/data%20file.txt"), "space".getBytes(StandardCharsets.UTF_8));

            try (Stream<URI> matches = store.glob("reports/*")) {
                assertEquals(
                        List.of(URI.create("reports/archive/"), URI.create("reports/current/"), URI.create("reports/data%20file.txt")),
                        matches.toList()
                );
            }

            try (Stream<URI> matches = store.glob(URI.create("reports/current"), "*.txt")) {
                assertEquals(List.of(URI.create("reports/current/result.txt")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/**/result.txt")) {
                assertEquals(
                        List.of(URI.create("reports/archive/result.txt"), URI.create("reports/current/result.txt")),
                        matches.sorted().toList()
                );
            }

            try (Stream<URI> matches = store.glob("reports/data%20*.txt")) {
                assertEquals(List.of(URI.create("reports/data%20file.txt")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/current/result.json")) {
                assertEquals(List.of(URI.create("reports/current/result.json")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/*.bin")) {
                assertTrue(matches.toList().isEmpty());
            }
        }
    }

    @Test
    void glob_rejectsPathsOutsideTheStore() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            assertThrows(IllegalPathException.class, () -> store.glob("/reports/*.txt"));
            assertThrows(AbsolutePathException.class, () -> store.glob(URI.create("file:///tmp"), "*.txt"));
            assertThrows(IllegalPathException.class, () -> store.glob(URI.create("../../outside"), "*.txt"));
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
