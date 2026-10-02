package com.dua3.utility.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract test class providing standard tests for {@link ObjectStore} implementations.
 */
@SuppressWarnings({"java:S100", "java:S112", "java:S5960", "ProhibitedExceptionDeclared", "OverlyBroadThrowsClause"})
// accept for test code
public abstract class AbstractObjectStoreTest {

    @TempDir
    protected Path tempDir;

    protected abstract ObjectStore createStore(Path root) throws IOException;

    @Test
    void getRoot_isAbsolute() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            Assertions.assertTrue(store.getRoot().isAbsolute());
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

            Assertions.assertEquals(1, rootEntries.size());
            Assertions.assertEquals(URI.create("a/"), rootEntries.getFirst().uri());
            Assertions.assertEquals(ObjectStore.ObjectType.FOLDER, rootEntries.getFirst().type());
        }
    }

    @Test
    void list_throwsForMissingObject() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            Assertions.assertThrows(ObjectNotFoundException.class, () -> store.list(URI.create("missing")));
        }
    }

    @Test
    void writeAndRead_viaInputStreamMethod() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
            long written = store.write(URI.create("folder/data.txt"), new ByteArrayInputStream(data));
            Assertions.assertEquals(data.length, written);

            byte[] actual;
            try (InputStream in = store.openInputStream(URI.create("folder/data.txt"))) {
                actual = in.readAllBytes();
            }

            Assertions.assertArrayEquals(data, actual);
        }
    }

    @Test
    void writeString_usesUtf8AndReturnsByteCount() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            String text = "Grüße 🌍\n第二行";

            Assertions.assertEquals(text.getBytes(StandardCharsets.UTF_8).length, store.writeString(path, text));
            Assertions.assertEquals(text, store.readString(path));
            Assertions.assertArrayEquals(text.getBytes(StandardCharsets.UTF_8), store.readAllBytes(path));
        }
    }

    @Test
    void writeString_usesRequestedCharsetAndAcceptsAnyCharSequence() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            Charset charset = StandardCharsets.UTF_16LE;
            CharSequence text = new StringBuilder("äöü");

            Assertions.assertEquals(text.toString().getBytes(charset).length,
                    store.writeString(path, text, charset));
            Assertions.assertEquals(text.toString(), store.readString(path, charset));
        }
    }

    @Test
    void writeString_nullWritesStringNull() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("null.txt");

            Assertions.assertEquals(4, store.writeString(path, null));
            Assertions.assertEquals("null", store.readString(path));
        }
    }

    @Test
    void writeString_defaultsToCreateNewAndSupportsReplace() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("text.txt");
            store.writeString(path, "old");

            Assertions.assertThrows(ObjectExistsException.class, () -> store.writeString(path, "new"));
            Assertions.assertEquals(3, store.writeString(path, "new", ObjectStore.OutputOption.CREATE_OR_REPLACE));
            Assertions.assertEquals("new", store.readString(path));
        }
    }

    @Test
    void lines_readsAllLinesWithDefaultAndRequestedCharset() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI utf8Path = URI.create("lines.txt");
            store.writeString(utf8Path, "one\r\ntwo\nthree\r\nfour");
            try (var lines = store.lines(utf8Path)) {
                Assertions.assertEquals(List.of("one", "two", "three", "four"), lines.toList());
            }

            URI utf16Path = URI.create("lines-utf16.txt");
            store.writeString(utf16Path, "erste\nzweite\n第三", StandardCharsets.UTF_16);
            try (var lines = store.lines(utf16Path, StandardCharsets.UTF_16)) {
                Assertions.assertEquals(List.of("erste", "zweite", "第三"), lines.toList());
            }
        }
    }

    @Test
    void lines_emptyFileReturnsEmptyStream() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("empty.txt");
            store.writeString(path, "");

            try (var lines = store.lines(path)) {
                Assertions.assertTrue(lines.toList().isEmpty());
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
            Assertions.assertEquals(expected.length, store.transferTo(path, out));
            Assertions.assertArrayEquals(expected, out.toByteArray());
        }
    }

    @Test
    void readConvenienceMethods_propagateMissingObject() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI missing = URI.create("missing");

            Assertions.assertThrows(IOException.class, () -> store.readAllBytes(missing));
            Assertions.assertThrows(IOException.class, () -> store.readString(missing));
            Assertions.assertThrows(IOException.class, () -> store.transferTo(missing, new ByteArrayOutputStream()));
            Assertions.assertThrows(IOException.class, () -> store.lines(missing));
        }
    }

    @Test
    void writeByteArray_withBounds() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            byte[] data = "0123456789".getBytes(StandardCharsets.UTF_8);
            long written = store.write(URI.create("slice.txt"), data, 2, 6, ObjectStore.OutputOption.CREATE_NEW);
            Assertions.assertEquals(4, written);

            try (InputStream in = store.openInputStream(URI.create("slice.txt"))) {
                Assertions.assertArrayEquals("2345".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
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
                Assertions.assertArrayEquals("data".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
        }
    }

    @Test
    void byteChannels_readAndWriteData() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("channel/data.bin");
            byte[] expected = "channel data".getBytes(StandardCharsets.UTF_8);

            try (WritableByteChannel out = store.openWritableByteChannel(path, ObjectStore.OutputOption.CREATE_NEW)) {
                out.write(ByteBuffer.wrap(expected));
            }

            ByteBuffer actual = ByteBuffer.allocate(expected.length);
            try (ReadableByteChannel in = store.openReadableByteChannel(path)) {
                while (actual.hasRemaining()) {
                    Assertions.assertTrue(in.read(actual) >= 0);
                }
            }
            Assertions.assertArrayEquals(expected, actual.array());
        }
    }

    @Test
    void copy_copiesDataAndKeepsSource() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI source = URI.create("source.bin");
            URI target = URI.create("folder/target.bin");
            store.write(source, "copied".getBytes(StandardCharsets.UTF_8));

            store.copy(source, target, ObjectStore.OutputOption.CREATE_NEW);

            Assertions.assertEquals("copied", store.readString(source));
            Assertions.assertEquals("copied", store.readString(target));

            store.write(source, "updated".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            store.copy(source, target, ObjectStore.OutputOption.CREATE_OR_REPLACE);
            Assertions.assertEquals("updated", store.readString(target));
        }
    }

    @Test
    void move_copiesDataAndDeletesSource() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI source = URI.create("source.bin");
            URI target = URI.create("folder/target.bin");
            store.write(source, "moved".getBytes(StandardCharsets.UTF_8));
            store.write(target, "old target".getBytes(StandardCharsets.UTF_8));

            store.move(source, target, ObjectStore.OutputOption.CREATE_OR_REPLACE);

            Assertions.assertSame(ObjectStore.ObjectType.MISSING, store.getInfo(source).type());
            Assertions.assertEquals("moved", store.readString(target));
        }
    }

    @Test
    void createNew_failsIfObjectExists() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("exists.txt"), "1".getBytes(StandardCharsets.UTF_8));
            Assertions.assertThrows(ObjectExistsException.class, () -> store.write(URI.create("exists.txt"), "2".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_NEW));
        }
    }

    @Test
    void writeByteArray_forwardsOutputOptions() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("exists.txt");
            store.write(path, "old".getBytes(StandardCharsets.UTF_8));

            store.write(path, "new".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            try (InputStream in = store.openInputStream(path)) {
                Assertions.assertArrayEquals("new".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
        }
    }

    @Test
    void getInfo_returnsMetadataForDataAndFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("folder"));
            store.write(URI.create("folder/data.bin"), new byte[]{1, 2, 3});

            ObjectStore.ObjectInfo folderInfo = store.getInfo(URI.create("folder"));
            Assertions.assertEquals(ObjectStore.ObjectType.FOLDER, folderInfo.type());

            ObjectStore.ObjectInfo dataInfo = store.getInfo(URI.create("folder/data.bin"));
            Assertions.assertEquals(ObjectStore.ObjectType.DATA, dataInfo.type());
            Assertions.assertEquals(3, dataInfo.size());
            Assertions.assertNotNull(dataInfo.created());
            Assertions.assertNotNull(dataInfo.lastModified());

            Assertions.assertSame(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("folder/missing.bin")).type());
        }
    }

    @Test
    void list_encodesReservedCharactersInObjectUris() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("folder/data%20file.txt");
            store.write(path, "data".getBytes(StandardCharsets.UTF_8));

            try (var entries = store.list(URI.create("folder"))) {
                Assertions.assertEquals(List.of(path), entries.map(ObjectStore.ObjectInfo::uri).toList());
            }
        }
    }

    @Test
    void delete_andRemoveFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("f"));
            store.write(URI.create("f/data.txt"), "x".getBytes(StandardCharsets.UTF_8));

            store.delete(URI.create("f/data.txt"));
            Assertions.assertSame(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("f/data.txt")).type());

            store.removeFolder(URI.create("f"));
            Assertions.assertSame(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("f")).type());
        }
    }

    @Test
    void removeFolder_failsForNonEmptyFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.createFolder(URI.create("f"));
            store.write(URI.create("f/a.txt"), "x".getBytes(StandardCharsets.UTF_8));
            Assertions.assertThrows(FolderNotEmptyException.class, () -> store.removeFolder(URI.create("f")));
        }
    }

    @Test
    void deleteRecursively_removesTree() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("a/b/c.txt"), "x".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            store.write(URI.create("a/b/d.txt"), "y".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            store.deleteRecursively(URI.create("a"));
            Assertions.assertSame(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("a")).type());
        }
    }

    @Test
    void walk_returnsExpectedEntriesAndRespectsDepth() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            store.write(URI.create("a/b/c.txt"), "1".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            store.write(URI.create("a/d.txt"), "2".getBytes(StandardCharsets.UTF_8), ObjectStore.OutputOption.CREATE_OR_REPLACE);

            List<URI> all;
            try (var stream = store.walk(URI.create("a"))) {
                all = stream.map(ObjectStore.ObjectInfo::uri).toList();
            }

            Assertions.assertTrue(all.contains(URI.create("a")) || all.contains(URI.create("a/")));
            Assertions.assertTrue(all.contains(URI.create("a/b/")) || all.contains(URI.create("a/b")));
            Assertions.assertTrue(all.contains(URI.create("a/b/c.txt")));
            Assertions.assertTrue(all.contains(URI.create("a/d.txt")));

            List<URI> depth1;
            try (var stream = store.walk(URI.create("a"), 1)) {
                depth1 = stream.map(ObjectStore.ObjectInfo::uri).toList();
            }

            Assertions.assertFalse(depth1.contains(URI.create("a/b/c.txt")));
            Assertions.assertTrue(depth1.contains(URI.create("a/d.txt")));
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
                Assertions.assertEquals(
                        List.of(URI.create("reports/archive/"), URI.create("reports/current/"), URI.create("reports/data%20file.txt")),
                        matches.toList()
                );
            }

            try (Stream<URI> matches = store.glob(URI.create("reports/current"), "*.txt")) {
                Assertions.assertEquals(List.of(URI.create("reports/current/result.txt")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/**/result.txt")) {
                Assertions.assertEquals(
                        List.of(URI.create("reports/archive/result.txt"), URI.create("reports/current/result.txt")),
                        matches.sorted().toList()
                );
            }

            try (Stream<URI> matches = store.glob("reports/data%20*.txt")) {
                Assertions.assertEquals(List.of(URI.create("reports/data%20file.txt")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/current/result.json")) {
                Assertions.assertEquals(List.of(URI.create("reports/current/result.json")), matches.toList());
            }

            try (Stream<URI> matches = store.glob("reports/*.bin")) {
                Assertions.assertTrue(matches.toList().isEmpty());
            }
        }
    }

    @Test
    void glob_matchesLiteralPathContainingSpaces() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("sample%20data/regression-case.json");
            store.write(path, "sample".getBytes(StandardCharsets.UTF_8));

            try (Stream<URI> matches = store.glob("sample data/regression-case.json")) {
                Assertions.assertEquals(List.of(path), matches.toList());
            }
        }
    }

    @Test
    void glob_matchesWildcardPathContainingSpaces() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI jsonPath = URI.create("sample%20data/regression-case.json");
            URI textPath = URI.create("sample%20data/regression-case.txt");
            store.write(jsonPath, "sample".getBytes(StandardCharsets.UTF_8));
            store.write(textPath, "sample".getBytes(StandardCharsets.UTF_8));

            try (Stream<URI> matches = store.glob("sample data/*.json")) {
                Assertions.assertEquals(List.of(jsonPath), matches.toList());
            }
        }
    }

    @Test
    void glob_rejectsPathsOutsideTheStore() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            Assertions.assertThrows(IllegalPathException.class, () -> store.glob("/reports/*.txt"));
            Assertions.assertThrows(AbsolutePathException.class, () -> store.glob(URI.create("file:///tmp"), "*.txt"));
            Assertions.assertThrows(IllegalPathException.class, () -> store.glob(URI.create("../../outside"), "*.txt"));
        }
    }

    @Test
    void methods_rejectAbsoluteAndIllegalPaths() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI absolute = URI.create("file:///tmp/x");
            URI illegal = URI.create("../outside");

            Assertions.assertThrows(AbsolutePathException.class, () -> store.getInfo(absolute));
            Assertions.assertThrows(IllegalPathException.class, () -> store.getInfo(illegal));
        }
    }

    @Test
    void writeInputStream_outputOptionsBehavior() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("stream-opt.txt");
            store.writeString(path, "initial");

            Assertions.assertThrows(ObjectExistsException.class, () ->
                    store.write(path, new ByteArrayInputStream("second".getBytes(StandardCharsets.UTF_8))));
            Assertions.assertThrows(ObjectExistsException.class, () ->
                    store.write(path, new ByteArrayInputStream("third".getBytes(StandardCharsets.UTF_8)), ObjectStore.OutputOption.CREATE_NEW));

            store.write(path, new ByteArrayInputStream("replaced".getBytes(StandardCharsets.UTF_8)), ObjectStore.OutputOption.CREATE_OR_REPLACE);
            Assertions.assertEquals("replaced", store.readString(path));
        }
    }

    @Test
    void openOutputStream_outputOptionsBehavior() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("out-opt.txt");
            store.writeString(path, "initial");

            Assertions.assertThrows(ObjectExistsException.class, () -> store.openOutputStream(path));
            Assertions.assertThrows(ObjectExistsException.class, () -> store.openOutputStream(path, ObjectStore.OutputOption.CREATE_NEW));

            try (OutputStream out = store.openOutputStream(path, ObjectStore.OutputOption.CREATE_OR_REPLACE)) {
                out.write("replaced".getBytes(StandardCharsets.UTF_8));
            }
            Assertions.assertEquals("replaced", store.readString(path));
        }
    }

    @Test
    void openInputStreamAndChannel_throwsNotADataObjectForFolder() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI folder = URI.create("folder");
            store.createFolder(folder);

            Assertions.assertThrows(NotADataObjectException.class, () -> store.openInputStream(folder));
            Assertions.assertThrows(NotADataObjectException.class, () -> store.openReadableByteChannel(folder));
        }
    }

    @Test
    void listAndRemoveFolder_throwsNotAFolderForData() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI file = URI.create("file.txt");
            store.writeString(file, "content");

            Assertions.assertThrows(NotAFolderException.class, () -> store.list(file));
            Assertions.assertThrows(NotAFolderException.class, () -> store.removeFolder(file));
        }
    }

    @Test
    void copyAndMove_throwsNotADataObjectForFolderSource() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI folder = URI.create("folder");
            store.createFolder(folder);

            Assertions.assertThrows(NotADataObjectException.class, () -> store.copy(folder, URI.create("target.txt")));
            Assertions.assertThrows(NotADataObjectException.class, () -> store.move(folder, URI.create("target.txt")));
        }
    }

    @Test
    void walk_throwsForMissingObject() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            Assertions.assertThrows(ObjectNotFoundException.class, () -> store.walk(URI.create("missing")));
            Assertions.assertThrows(ObjectNotFoundException.class, () -> store.walk(URI.create("missing"), 2));
        }
    }

    @Test
    void getInfo_rootAndMissing() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            ObjectStore.ObjectInfo rootInfo = store.getInfo(URI.create(""));
            Assertions.assertEquals(ObjectStore.ObjectType.FOLDER, rootInfo.type());

            ObjectStore.ObjectInfo missingInfo = store.getInfo(URI.create("nonexistent"));
            Assertions.assertSame(ObjectStore.ObjectType.MISSING, missingInfo.type());
            Assertions.assertEquals(URI.create("nonexistent"), missingInfo.uri());
            Assertions.assertEquals(ObjectStore.ObjectInfo.UNKNOWN_SIZE, missingInfo.size());
            Assertions.assertEquals(Instant.MIN, missingInfo.created());
            Assertions.assertEquals(Instant.MIN, missingInfo.lastModified());
        }
    }

    @Test
    void readableByteChannel_seekableOperations() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("seekable.bin");
            byte[] data = "0123456789".getBytes(StandardCharsets.UTF_8);
            store.write(path, data);

            try (ReadableByteChannel channel = store.openReadableByteChannel(path)) {
                if (channel instanceof SeekableByteChannel sbc) {
                    Assertions.assertEquals(data.length, sbc.size());
                    Assertions.assertEquals(0, sbc.position());

                    // Seek and read
                    Assertions.assertSame(sbc, sbc.position(4));
                    Assertions.assertEquals(4, sbc.position());
                    ByteBuffer buf = ByteBuffer.allocate(3);
                    Assertions.assertEquals(3, sbc.read(buf));
                    Assertions.assertArrayEquals("456".getBytes(StandardCharsets.UTF_8), buf.array());
                    Assertions.assertEquals(7, sbc.position());

                    // Read to end and beyond
                    Assertions.assertSame(sbc, sbc.position(data.length));
                    ByteBuffer endBuf = ByteBuffer.allocate(2);
                    Assertions.assertEquals(-1, sbc.read(endBuf));

                    // Invalid position
                    Assertions.assertThrows(IllegalArgumentException.class, () -> sbc.position(-1));

                    // Read-only channel operations throw NonWritableChannelException
                    Assertions.assertThrows(NonWritableChannelException.class, () -> sbc.write(ByteBuffer.wrap(new byte[]{1})));
                    Assertions.assertThrows(NonWritableChannelException.class, () -> sbc.truncate(0));
                }
            }
        }
    }

    @Test
    @SuppressWarnings("java:S2095") // needed for test
    void readableByteChannel_closedChannelThrows() throws Exception {
        try (ObjectStore store = createStore(tempDir.resolve("store"))) {
            URI path = URI.create("closed-channel.bin");
            byte[] data = "closed-test".getBytes(StandardCharsets.UTF_8);
            store.write(path, data);

            ReadableByteChannel channel = store.openReadableByteChannel(path);
            Assertions.assertTrue(channel.isOpen());
            channel.close();
            Assertions.assertFalse(channel.isOpen());

            ByteBuffer buf = ByteBuffer.allocate(4);
            Assertions.assertThrows(ClosedChannelException.class, () -> channel.read(buf));

            if (channel instanceof SeekableByteChannel sbc) {
                Assertions.assertThrows(ClosedChannelException.class, sbc::position);
                Assertions.assertThrows(ClosedChannelException.class, () -> sbc.position(0));
                Assertions.assertThrows(ClosedChannelException.class, sbc::size);
                Assertions.assertThrows(ClosedChannelException.class, () -> sbc.write(ByteBuffer.wrap(new byte[]{1})));
                Assertions.assertThrows(ClosedChannelException.class, () -> sbc.truncate(0));
            }
        }
    }
}
