package com.dua3.utility.io;

import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class FileTypeTest {

    private static TestFileType textFileType;
    private static TestFileType documentFileType;
    private static TestCompoundFileType compoundFileType;

    @BeforeAll
    static void setUp() {
        // Create test file types
        textFileType = new TestFileType("Text", OpenMode.READ_AND_WRITE, String.class, "txt", "text");
        documentFileType = new TestFileType("Document", OpenMode.READ, TestDocument.class, "doc", "docx");
        compoundFileType = new TestCompoundFileType("All Files", OpenMode.READ_AND_WRITE, Object.class, "txt", "doc");

        FileType.addType(textFileType);
        FileType.addType(documentFileType);
        FileType.addType(compoundFileType);
    }

    @Test
    void testFileTypes() {
        Collection<FileType<?>> fileTypes = FileType.fileTypes();

        assertNotNull(fileTypes);
        assertTrue(fileTypes.contains(textFileType));
        assertTrue(fileTypes.contains(documentFileType));
        assertTrue(fileTypes.contains(compoundFileType));

        // Test immutability
        assertThrows(UnsupportedOperationException.class, fileTypes::clear);
    }

    @Test
    void testForExtension() {
        Optional<FileType<?>> result = FileType.forExtension("txt");

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());

        // Should not find compound file types
        Optional<FileType<?>> compoundResult = FileType.forExtension("doc");
        assertTrue(compoundResult.isPresent());
        assertEquals(documentFileType, compoundResult.get()); // documentFileType is not compound
    }

    @Test
    void testForExtensionWithMode() {
        Optional<FileType<?>> readResult = FileType.forExtension(OpenMode.READ, "txt");
        assertTrue(readResult.isPresent());
        assertEquals(textFileType, readResult.get());

        Optional<FileType<?>> writeResult = FileType.forExtension(OpenMode.WRITE, "doc");
        assertFalse(writeResult.isPresent()); // documentFileType only supports READ
    }

    @Test
    void testForExtensionNotFound() {
        Optional<FileType<?>> result = FileType.forExtension("unknown");
        assertFalse(result.isPresent());
    }

    @Test
    void testAllForExtension() {
        List<FileType<?>> results = FileType.allForExtension("txt");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(textFileType));

        // Test immutability
        assertThrows(UnsupportedOperationException.class, results::clear);
    }

    @Test
    void testAllForExtensionWithMode() {
        List<FileType<?>> readResults = FileType.allForExtension(OpenMode.READ, "txt");
        assertTrue(readResults.contains(textFileType));

        List<FileType<?>> writeResults = FileType.allForExtension(OpenMode.WRITE, "doc");
        assertFalse(writeResults.contains(documentFileType)); // documentFileType doesn't support WRITE
    }

    @Test
    void testReaderForType() {
        Optional<FileType<? extends String>> result = FileType.readerForType(String.class);

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());
    }

    @Test
    void testAllReadersForType() {
        List<FileType<? extends String>> results = FileType.allReadersForType(String.class);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(textFileType));
    }

    @Test
    void testWriterForType() {
        Optional<FileType<? super String>> result = FileType.writerForType(String.class);

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());
    }

    @Test
    void testAllWritersForType() {
        List<FileType<? super String>> results = FileType.allWritersForType(String.class);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(textFileType));
    }

    @Test
    void testForUri() {
        URI uri = URI.create("file:///test.txt");
        Optional<FileType<?>> result = FileType.forUri(uri);

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());
    }

    @Test
    void testForUriWithClass() {
        URI uri = URI.create("file:///test.txt");
        Optional<FileType<String>> result = FileType.forUri(uri, String.class);

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());
    }

    @Test
    void testForPath() {
        Path path = Paths.get("test.txt");
        Optional<FileType<String>> result = FileType.forPath(path, String.class);

        assertTrue(result.isPresent());
        assertEquals(textFileType, result.get());
    }

    @Test
    void testGetFileTypesWithMode() {
        List<FileType<?>> readTypes = FileType.getFileTypes(OpenMode.READ);
        assertTrue(readTypes.contains(textFileType));
        assertTrue(readTypes.contains(documentFileType));

        List<FileType<?>> writeTypes = FileType.getFileTypes(OpenMode.WRITE);
        assertTrue(writeTypes.contains(textFileType));
        assertFalse(writeTypes.contains(documentFileType));
    }

    @Test
    void testGetFileTypesWithModeAndClass() {
        List<FileType<String>> stringTypes = FileType.getFileTypes(OpenMode.READ, String.class);
        assertEquals(1, stringTypes.size());
        assertTrue(stringTypes.contains(textFileType));
    }

    @Test
    void testGetExtensions() {
        List<String> extensions = textFileType.getExtensions();
        assertEquals(List.of("txt", "text"), extensions);

        // Test immutability
        assertThrows(UnsupportedOperationException.class, extensions::clear);
    }

    @Test
    void testGetExtensionPatterns() {
        List<String> patterns = textFileType.getExtensionPatterns();
        assertEquals(List.of("*.txt", "*.text"), patterns);
    }

    @Test
    void testIsSupported() {
        assertTrue(textFileType.isSupported(OpenMode.READ));
        assertTrue(textFileType.isSupported(OpenMode.WRITE));
        assertTrue(textFileType.isSupported(OpenMode.READ_AND_WRITE));
        assertFalse(textFileType.isSupported(OpenMode.NONE));

        assertTrue(documentFileType.isSupported(OpenMode.READ));
        assertFalse(documentFileType.isSupported(OpenMode.WRITE));
    }

    @Test
    void testMatches() {
        assertTrue(textFileType.matches("test.txt"));
        assertTrue(textFileType.matches("document.text"));
        assertFalse(textFileType.matches("test.doc"));
        assertFalse(textFileType.matches("test"));
    }

    @Test
    void testIsCompound() {
        assertFalse(textFileType.isCompound());
        assertTrue(compoundFileType.isCompound());
    }

    @Test
    void testGetSettings() {
        Collection<?> settings = textFileType.getSettings();
        assertNotNull(settings);
        assertTrue(settings.isEmpty());
    }

    @Test
    void testEquals() {
        assertTrue(textFileType.equals(textFileType), "equals with same type");
        assertFalse(textFileType.equals(documentFileType), "equals with different type");
        assertNotEquals(null, textFileType, "equals with null");
        assertFalse(textFileType.equals("not a file type"), "equals with non-FileType object");
    }

    @Test
    void testHashCode() {
        TestFileType sameType = new TestFileType("Text", OpenMode.READ_AND_WRITE, String.class, "txt", "text");
        assertEquals(textFileType.hashCode(), sameType.hashCode());
    }

    @Test
    void testCompareTo() {
        assertTrue(textFileType.compareTo(documentFileType) > 0); // "Text" > "Another"
        assertTrue(documentFileType.compareTo(textFileType) < 0);
        assertEquals(0, textFileType.compareTo(textFileType));
    }

    @Test
    void testReadMethods() throws IOException {
        URI uri = URI.create("file:///test.txt");
        Path path = Paths.get("test.txt");

        String expectedContent = "test content";
        textFileType.setTestContent(expectedContent);

        // Test read(URI)
        Object result1 = textFileType.read(uri);
        assertEquals(expectedContent, result1);

        // Test read(URI, options)
        Object result2 = textFileType.read(uri, ft -> Arguments.empty());
        assertEquals(expectedContent, result2);

        // Test read(Path)
        Object result3 = textFileType.read(path);
        assertEquals(expectedContent, result3);

        // Test read(Path, options)
        Object result4 = textFileType.read(path, ft -> Arguments.empty());
        assertEquals(expectedContent, result4);
    }

    @Test
    void testWriteMethods() {
        URI uri = URI.create("file:///test.txt");
        Path path = Paths.get("test.txt");
        String content = "test content";

        // Test write(URI, document)
        assertDoesNotThrow(() -> textFileType.write(uri, content));

        // Test write(URI, document, options)
        assertDoesNotThrow(() -> textFileType.write(uri, content, ft -> Arguments.empty()));

        // Test write(Path, document)
        assertDoesNotThrow(() -> textFileType.write(path, content));

        // Test write(Path, document, options)
        assertDoesNotThrow(() -> textFileType.write(path, content, ft -> Arguments.empty()));
    }

    // Test implementations
    private static class TestFileType<T> extends FileType<T> {
        private T testContent;

        public TestFileType(String name, OpenMode mode, Class<T> cls, String... extensions) {
            super(name, mode, cls, extensions);
        }

        public TestFileType(String name, OpenMode mode, Class<T> cls, Class<T> clsWriteable, String... extensions) {
            super(name, mode, cls, clsWriteable, extensions);
        }

        public void setTestContent(T content) {
            this.testContent = content;
        }

        @Override
        public T read(URI uri, Function<FileType<? extends T>, Arguments> options) throws IOException {
            return testContent;
        }

        @Override
        public void write(URI uri, T document, Function<FileType<? super T>, Arguments> options) throws IOException {
            // Mock implementation - just store the document
            this.testContent = document;
        }
    }

    private static class TestDocument {
        private final String content;

        public TestDocument(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    private static class TestDocumentFileType extends FileType<TestDocument> {
        public TestDocumentFileType(String name, OpenMode mode, Class<TestDocument> cls, String... extensions) {
            super(name, mode, cls, extensions);
        }

        @Override
        public TestDocument read(URI uri, Function<FileType<? extends TestDocument>, Arguments> options) throws IOException {
            return new TestDocument("test document content");
        }

        @Override
        public void write(URI uri, TestDocument document, Function<FileType<? super TestDocument>, Arguments> options) throws IOException {
            throw new UnsupportedOperationException("Write not supported");
        }
    }

    private static class TestCompoundFileType extends FileType<Object> {
        public TestCompoundFileType(String name, OpenMode mode, Class<Object> cls, String... extensions) {
            super(name, mode, cls, extensions);
        }

        @Override
        public boolean isCompound() {
            return true;
        }

        @Override
        public Object read(URI uri, Function<FileType<? extends Object>, Arguments> options) throws IOException {
            return new Object();
        }

        @Override
        public void write(URI uri, Object document, Function<FileType<? super Object>, Arguments> options) throws IOException {
            // Mock implementation
        }
    }
}