/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
class FileTypeTest {

    private static final String TEXT_EXTENSION = "filetypetest-text";
    private static final String ALTERNATE_TEXT_EXTENSION = "filetypetest-alt";
    private static final String DOCUMENT_EXTENSION = "filetypetest-document";
    private static final String WRITABLE_EXTENSION = "filetypetest-writable";
    private static final String COMPOUND_EXTENSION = "filetypetest-compound";

    private static TestFileType<String> textFileType;
    private static TestFileType<TestDocument> documentFileType;
    private static TestFileType<StringBuilder> writableFileType;
    private static TestCompoundFileType compoundFileType;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpTypes() {
        textFileType = new TestFileType<>(
                "FileType test text", OpenMode.READ_AND_WRITE, String.class, TEXT_EXTENSION, ALTERNATE_TEXT_EXTENSION);
        documentFileType = new TestFileType<>(
                "FileType test document", OpenMode.READ, TestDocument.class, DOCUMENT_EXTENSION);
        writableFileType = new TestFileType<>(
                "FileType test writable", OpenMode.READ_AND_WRITE, StringBuilder.class, CharSequence.class, WRITABLE_EXTENSION);
        compoundFileType = new TestCompoundFileType(
                "FileType test compound", OpenMode.READ_AND_WRITE, COMPOUND_EXTENSION);

        assertTrue(FileType.addType(textFileType));
        assertTrue(FileType.addType(documentFileType));
        assertTrue(FileType.addType(writableFileType));
        assertTrue(FileType.addType(compoundFileType));
    }

    @BeforeEach
    void resetTextFileType() {
        textFileType.reset();
    }

    @Test
    void addTypeCallsOnAddOnceAndFileTypesIsAnUnmodifiableLiveView() {
        Collection<FileType<?>> fileTypes = FileType.fileTypes();
        assertTrue(fileTypes.contains(textFileType));
        assertThrows(UnsupportedOperationException.class, fileTypes::clear);

        OnAddTrackingFileType addedFileType = new OnAddTrackingFileType();
        assertTrue(FileType.addType(addedFileType));
        assertEquals(1, addedFileType.onAddCalls);
        assertTrue(fileTypes.contains(addedFileType), "the returned collection must reflect later registrations");

        assertFalse(FileType.addType(addedFileType));
        assertEquals(1, addedFileType.onAddCalls, "onAdd must not run when the type is already registered");
    }

    @Test
    void findsTypesByExtensionAndExcludesCompoundTypes() {
        assertEquals(textFileType, FileType.forExtension(TEXT_EXTENSION).orElseThrow());
        assertEquals(textFileType, FileType.forExtension(ALTERNATE_TEXT_EXTENSION).orElseThrow());
        assertEquals(documentFileType, FileType.forExtension(DOCUMENT_EXTENSION).orElseThrow());
        assertTrue(FileType.forExtension(COMPOUND_EXTENSION).isEmpty());
        assertTrue(FileType.forExtension(TEXT_EXTENSION.toUpperCase()).isEmpty(), "extensions are case-sensitive");
        assertTrue(FileType.forExtension("filetypetest-unknown").isEmpty());
    }

    @Test
    void findsTypesByExtensionAndMode() {
        assertEquals(textFileType, FileType.forExtension(OpenMode.READ, TEXT_EXTENSION).orElseThrow());
        assertEquals(textFileType, FileType.forExtension(OpenMode.WRITE, TEXT_EXTENSION).orElseThrow());
        assertEquals(textFileType, FileType.forExtension(OpenMode.READ_AND_WRITE, TEXT_EXTENSION).orElseThrow());
        assertEquals(documentFileType, FileType.forExtension(OpenMode.READ, DOCUMENT_EXTENSION).orElseThrow());
        assertTrue(FileType.forExtension(OpenMode.WRITE, DOCUMENT_EXTENSION).isEmpty());
        assertTrue(FileType.forExtension(OpenMode.NONE, TEXT_EXTENSION).isEmpty());
    }

    @Test
    void findsAllTypesByExtensionAndReturnsUnmodifiableLists() {
        List<FileType<?>> allTextTypes = FileType.allForExtension(TEXT_EXTENSION);
        assertIterableEquals(List.of(textFileType), allTextTypes);
        assertThrows(UnsupportedOperationException.class, allTextTypes::clear);

        assertIterableEquals(List.of(textFileType), FileType.allForExtension(OpenMode.READ_AND_WRITE, TEXT_EXTENSION));
        assertIterableEquals(List.of(documentFileType), FileType.allForExtension(OpenMode.READ, DOCUMENT_EXTENSION));
        assertTrue(FileType.allForExtension(OpenMode.WRITE, DOCUMENT_EXTENSION).isEmpty());
        assertTrue(FileType.allForExtension(COMPOUND_EXTENSION).isEmpty());
    }

    @Test
    void findsReadersForDocumentTypes() {
        assertEquals(textFileType, FileType.readerForType(String.class).orElseThrow());
        assertEquals(documentFileType, FileType.readerForType(TestDocument.class).orElseThrow());
        assertTrue(FileType.readerForType(CompoundDocument.class).isEmpty(), "first-reader lookup excludes compound types");
        assertTrue(FileType.readerForType(Integer.class).isEmpty());

        List<FileType<? extends TestDocument>> documentReaders = FileType.allReadersForType(TestDocument.class);
        assertIterableEquals(List.of(documentFileType), documentReaders);
        assertThrows(UnsupportedOperationException.class, documentReaders::clear);

        assertIterableEquals(
                List.of(compoundFileType),
                FileType.allReadersForType(CompoundDocument.class),
                "all-reader lookup intentionally includes compound types");
    }

    @Test
    void findsWritersForDocumentTypesUsingWriteableClass() {
        assertEquals(textFileType, FileType.writerForType(String.class).orElseThrow());
        assertEquals(writableFileType, FileType.writerForType(StringBuilder.class).orElseThrow());
        assertEquals(writableFileType, FileType.writerForType(CharSequence.class).orElseThrow());
        assertTrue(FileType.writerForType(TestDocument.class).isEmpty());
        assertTrue(FileType.writerForType(CompoundDocument.class).isEmpty(), "writer lookup excludes compound types");

        List<FileType<? super StringBuilder>> stringBuilderWriters = FileType.allWritersForType(StringBuilder.class);
        assertIterableEquals(List.of(writableFileType), stringBuilderWriters);
        assertThrows(UnsupportedOperationException.class, stringBuilderWriters::clear);
    }

    @Test
    void findsTypesByUriAndPathWithTheRequestedDocumentClass() {
        URI textUri = URI.create("memory:/folder/example." + TEXT_EXTENSION);
        Path textPath = tempDir.resolve("example." + TEXT_EXTENSION);

        assertEquals(textFileType, FileType.forUri(textUri).orElseThrow());
        assertEquals(textFileType, FileType.forUri(textUri, String.class).orElseThrow());
        assertEquals(textFileType, FileType.forPath(textPath, String.class).orElseThrow());
        assertTrue(FileType.forUri(textUri, TestDocument.class).isEmpty());
        assertTrue(FileType.forPath(tempDir.resolve("example.filetypetest-unknown"), String.class).isEmpty());

        URI compoundUri = URI.create("memory:/folder/example." + COMPOUND_EXTENSION);
        assertTrue(FileType.forUri(compoundUri).isEmpty());
        assertEquals(compoundFileType, FileType.forUri(compoundUri, CompoundDocument.class).orElseThrow());
    }

    @Test
    void readsThroughUriAndPathConvenienceMethods() throws IOException {
        Path path = writeFile("read." + TEXT_EXTENSION, "read content");

        assertEquals("read content", textFileType.read(path.toUri()));
        assertEquals(path.toUri(), textFileType.lastReadUri);
        assertEquals("read content", textFileType.read(path.toUri(), type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertEquals(path.toUri(), textFileType.lastReadUri);
        assertEquals("read content", textFileType.read(path));
        assertEquals(path.toUri(), textFileType.lastReadUri);
        assertEquals("read content", textFileType.read(path, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertEquals(path.toUri(), textFileType.lastReadUri);
        assertEquals(4, textFileType.readOptionCalls);
    }

    @Test
    void readsThroughStaticConvenienceMethods() throws IOException {
        Path path = writeFile("static-read." + TEXT_EXTENSION, "static content");

        assertEquals(Optional.of("static content"), FileType.read(path.toUri(), String.class));
        assertEquals(Optional.of("static content"), FileType.read(path.toUri(), String.class, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertEquals(Optional.of("static content"), FileType.read(path, String.class));
        assertEquals(Optional.of("static content"), FileType.read(path, String.class, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertEquals(4, textFileType.readOptionCalls);

        URI unknown = URI.create("memory:/missing.filetypetest-unknown");
        assertTrue(FileType.read(unknown, String.class).isEmpty());
        assertTrue(FileType.read(tempDir.resolve("missing.filetypetest-unknown"), String.class).isEmpty());
    }

    @Test
    void readsFromObjectStoreAndClosesItsStream() throws IOException {
        URI relativeUri = URI.create("stored." + TEXT_EXTENSION);
        TestReadableObjectStore objectStore = new TestReadableObjectStore(relativeUri, "stored content");

        assertEquals(Optional.of("stored content"), FileType.read(objectStore, relativeUri, String.class, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertEquals(relativeUri, objectStore.openedUri);
        assertTrue(objectStore.inputStream.closed);
        assertEquals(relativeUri, textFileType.lastReadUri);

        assertTrue(FileType.read(objectStore, URI.create("missing.filetypetest-unknown"), String.class, type -> Arguments.empty()).isEmpty());
        assertEquals(1, objectStore.openCalls, "an object stream must not be opened when no file type matches");
    }

    @Test
    void readsAndWritesStreamsAndPathConvenienceMethods() throws IOException {
        URI uri = URI.create("memory:/stream." + TEXT_EXTENSION);
        ByteArrayInputStream input = new ByteArrayInputStream("stream content".getBytes(StandardCharsets.UTF_8));

        assertEquals("stream content", textFileType.read(uri, input, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        }));
        assertSame(input, textFileType.lastReadStream);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        textFileType.write("written content", uri, output, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        });
        assertEquals("written content", output.toString(StandardCharsets.UTF_8));
        assertSame(output, textFileType.lastWriteStream);
        assertEquals(uri, textFileType.lastWriteUri);

        Path path = tempDir.resolve("written." + TEXT_EXTENSION);
        textFileType.write("written content", path);
        assertEquals("written content", Files.readString(path));

        textFileType.write("written with options", path, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        });
        assertEquals("written with options", Files.readString(path));
        assertEquals(3, textFileType.writeOptionCalls);
    }

    @Test
    void writesToObjectStoreAndClosesItsStream() throws IOException {
        URI relativeUri = URI.create("stored." + TEXT_EXTENSION);
        TestWritableObjectStore objectStore = new TestWritableObjectStore(relativeUri);

        textFileType.write("stored content", objectStore, relativeUri, type -> {
            assertSame(textFileType, type);
            return Arguments.empty();
        });

        assertEquals(relativeUri, objectStore.openedUri);
        assertEquals(1, objectStore.openCalls);
        assertEquals("stored content", objectStore.outputStream.toString(StandardCharsets.UTF_8));
        assertTrue(objectStore.outputStream.closed);
        assertEquals(relativeUri, textFileType.lastWriteUri);
        assertSame(objectStore.outputStream, textFileType.lastWriteStream);
        assertEquals(1, textFileType.writeOptionCalls);
    }

    @Test
    void getsTypesForModeAndDocumentClass() {
        List<FileType<?>> readableTypes = FileType.getFileTypes(OpenMode.READ);
        assertTrue(readableTypes.containsAll(List.of(textFileType, documentFileType, writableFileType, compoundFileType)));
        assertFalse(FileType.getFileTypes(OpenMode.WRITE).contains(documentFileType));
        assertTrue(FileType.getFileTypes(OpenMode.NONE).containsAll(List.of(textFileType, documentFileType, writableFileType, compoundFileType)));

        List<FileType<String>> readableStringTypes = FileType.getFileTypes(OpenMode.READ, String.class);
        assertIterableEquals(List.of(textFileType), readableStringTypes);
        assertIterableEquals(List.of(writableFileType), FileType.getFileTypes(OpenMode.WRITE, StringBuilder.class));
        assertIterableEquals(List.of(writableFileType), FileType.getFileTypes(OpenMode.WRITE, CharSequence.class));
        assertIterableEquals(List.of(textFileType), FileType.getFileTypes(OpenMode.READ_AND_WRITE, String.class));
        assertTrue(FileType.getFileTypes(OpenMode.NONE, String.class).isEmpty());
    }

    @Test
    void exposesMetadataAndMatchesFilenames() {
        assertEquals("FileType test text", textFileType.getName());
        assertEquals(String.class, textFileType.getDocumentClass());
        assertEquals(String.class, textFileType.getWriteableClass());
        assertEquals(CharSequence.class, writableFileType.getWriteableClass());
        assertIterableEquals(List.of(TEXT_EXTENSION, ALTERNATE_TEXT_EXTENSION), textFileType.getExtensions());
        assertIterableEquals(List.of("*." + TEXT_EXTENSION, "*." + ALTERNATE_TEXT_EXTENSION), textFileType.getExtensionPatterns());
        assertThrows(UnsupportedOperationException.class, () -> textFileType.getExtensions().clear());
        assertThrows(UnsupportedOperationException.class, () -> textFileType.getExtensionPatterns().clear());

        assertTrue(textFileType.matches("document." + TEXT_EXTENSION));
        assertTrue(textFileType.matches("folder/document." + ALTERNATE_TEXT_EXTENSION));
        assertFalse(textFileType.matches("document." + DOCUMENT_EXTENSION));
        assertFalse(textFileType.matches("document"));
        assertFalse(textFileType.matches("document." + TEXT_EXTENSION.toUpperCase()));
        assertFalse(textFileType.isCompound());
        assertTrue(compoundFileType.isCompound());
        assertTrue(textFileType.getSettings().isEmpty());
    }

    @Test
    void reportsSupportedModes() {
        assertTrue(textFileType.isSupported(OpenMode.READ));
        assertTrue(textFileType.isSupported(OpenMode.WRITE));
        assertTrue(textFileType.isSupported(OpenMode.READ_AND_WRITE));
        assertFalse(textFileType.isSupported(OpenMode.NONE));

        assertTrue(documentFileType.isSupported(OpenMode.READ));
        assertFalse(documentFileType.isSupported(OpenMode.WRITE));
        assertFalse(documentFileType.isSupported(OpenMode.READ_AND_WRITE));
        assertFalse(documentFileType.isSupported(OpenMode.NONE));
    }

    @Test
    void comparesAndIdentifiesTypesByTheirDefinition() {
        TestFileType<String> equalType = new TestFileType<>(
                "FileType test text", OpenMode.READ_AND_WRITE, String.class, TEXT_EXTENSION, ALTERNATE_TEXT_EXTENSION);
        TestFileType<String> differentWriteableType = new TestFileType<>(
                "FileType test text", OpenMode.READ_AND_WRITE, String.class, Object.class, TEXT_EXTENSION, ALTERNATE_TEXT_EXTENSION);
        TestFileType<String> earlierType = new TestFileType<>("A FileType test", OpenMode.READ, String.class, "earlier");

        assertEquals(textFileType, textFileType);
        assertEquals(equalType, textFileType);
        assertEquals(equalType.hashCode(), textFileType.hashCode());
        assertNotEquals(differentWriteableType, textFileType);
        assertNotEquals(documentFileType, textFileType);
        assertNotEquals(null, textFileType);
        assertNotEquals("not a file type", textFileType);
        assertTrue(textFileType.compareTo(earlierType) > 0);
        assertTrue(earlierType.compareTo(textFileType) < 0);
        assertEquals(0, textFileType.compareTo(textFileType));
    }

    private Path writeFile(String filename, String content) throws IOException {
        Path path = tempDir.resolve(filename);
        Files.writeString(path, content);
        return path;
    }

    private static class TestFileType<T> extends FileType<T> {
        private URI lastReadUri;
        private InputStream lastReadStream;
        private URI lastWriteUri;
        private OutputStream lastWriteStream;
        private int readOptionCalls;
        private int writeOptionCalls;

        TestFileType(String name, OpenMode mode, Class<T> documentClass, String... extensions) {
            this(name, mode, documentClass, documentClass, extensions);
        }

        TestFileType(String name, OpenMode mode, Class<? extends T> documentClass, Class<? super T> writeableClass, String... extensions) {
            super(name, mode, documentClass, writeableClass, extensions);
        }

        void reset() {
            lastReadUri = null;
            lastReadStream = null;
            lastWriteUri = null;
            lastWriteStream = null;
            readOptionCalls = 0;
            writeOptionCalls = 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T read(URI uri, InputStream in, java.util.function.Function<FileType<? extends T>, Arguments> options) throws IOException {
            lastReadUri = uri;
            lastReadStream = in;
            readOptionCalls++;
            options.apply(this);
            return (T) new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        @Override
        public void write(T document, URI uri, OutputStream out, java.util.function.Function<FileType<? super T>, Arguments> options) throws IOException {
            lastWriteUri = uri;
            lastWriteStream = out;
            writeOptionCalls++;
            options.apply(this);
            out.write(String.valueOf(document).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final class TestCompoundFileType extends TestFileType<CompoundDocument> {
        TestCompoundFileType(String name, OpenMode mode, String... extensions) {
            super(name, mode, CompoundDocument.class, extensions);
        }

        @Override
        public boolean isCompound() {
            return true;
        }
    }

    private static final class OnAddTrackingFileType extends TestFileType<OnAddDocument> {
        private int onAddCalls;

        OnAddTrackingFileType() {
            super("FileType test on add", OpenMode.READ, OnAddDocument.class, "filetypetest-on-add");
        }

        @Override
        protected void onAdd() {
            onAddCalls++;
        }
    }

    private static final class TestReadableObjectStore implements ReadableObjectStore {
        private final URI expectedUri;
        private final byte[] content;
        private URI openedUri;
        private int openCalls;
        private CloseTrackingInputStream inputStream;

        TestReadableObjectStore(URI expectedUri, String content) {
            this.expectedUri = expectedUri;
            this.content = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public URI getRoot() {
            return URI.create("memory:/");
        }

        @Override
        public Stream<ObjectStore.ObjectInfo> list(URI path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream openInputStream(URI path) {
            assertEquals(expectedUri, path);
            openedUri = path;
            openCalls++;
            inputStream = new CloseTrackingInputStream(content);
            return inputStream;
        }

        @Override
        public Optional<ObjectStore.ObjectInfo> getInfo(URI path) {
            return Optional.empty();
        }

        @Override
        public void close() {
            // Nothing to close.
        }
    }

    private static final class TestWritableObjectStore implements WritableObjectStore {
        private final URI expectedUri;
        private URI openedUri;
        private int openCalls;
        private CloseTrackingOutputStream outputStream;

        TestWritableObjectStore(URI expectedUri) {
            this.expectedUri = expectedUri;
        }

        @Override
        public URI getRoot() {
            return URI.create("memory:/");
        }

        @Override
        public long write(URI path, InputStream in, ObjectStore.OutputOption... options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long write(URI path, byte[] data, int from, int to, ObjectStore.OutputOption... options) {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream openOutputStream(URI path, ObjectStore.OutputOption... options) {
            assertEquals(expectedUri, path);
            openedUri = path;
            openCalls++;
            outputStream = new CloseTrackingOutputStream();
            return outputStream;
        }

        @Override
        public void createFolder(URI path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            // Nothing to close.
        }
    }

    private static final class CloseTrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        CloseTrackingInputStream(byte[] content) {
            super(content);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static final class CloseTrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }

    private static final class TestDocument {
    }

    private static final class CompoundDocument {
    }

    private static final class OnAddDocument {
    }
}
