package com.dua3.utility.swing;

import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import com.dua3.utility.options.Arguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the SwingFileFilter class.
 * <p>
 * These tests verify that the filter correctly accepts or rejects files based on the FileType.
 */
class SwingFileFilterTest {

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

    @BeforeAll
    static void setUp() {
        // Create mock FileTypes
        FileType.addType(new TestFileType<>("Text", OpenMode.READ_AND_WRITE, String.class, "txt", "text"));
        FileType.addType(new TestFileType<>("Document", OpenMode.READ, String.class, "doc", "docx"));
    }

    /**
     * Test the getFilters method.
     * <p>
     * Note: This test is modified to use a mock FileType since there might not be any
     * registered FileTypes for String.class with OpenMode.READ.
     */
    @Test
    void testGetFilters() {
        // Create a list with our mock filters
        List<SwingFileFilter<String>> filters = SwingFileFilter.getFilters(OpenMode.READ, String.class);

        // Verify filters were created
        assertNotNull(filters, "Filters should not be null");
        assertFalse(filters.isEmpty(), "Filters should not be empty");

        // Verify each filter has a FileType
        for (SwingFileFilter<String> filter : filters) {
            assertNotNull(filter.getFileType(), "FileType should not be null");
        }
    }

    /**
     * Test the accept method with a File parameter.
     */
    @Test
    void testAcceptFile() {
        // Create a filter for text files
        List<SwingFileFilter<String>> filters = SwingFileFilter.getFilters(OpenMode.READ, String.class);
        SwingFileFilter<String> textFilter = findFilterForExtension(filters, "txt");

        // Verify the filter was found
        assertNotNull(textFilter, "Text filter should not be null");

        // Test with a text file
        File textFile = new File("test.txt");
        assertTrue(textFilter.accept(textFile), "Text filter should accept a text file");

        // Test with a non-text file
        File nonTextFile = new File("test.jpg");
        assertFalse(textFilter.accept(nonTextFile), "Text filter should not accept a non-text file");
    }

    /**
     * Test the accept method with a directory and name parameters.
     */
    @Test
    void testAcceptDirAndName() {
        // Create a filter for text files
        List<SwingFileFilter<String>> filters = SwingFileFilter.getFilters(OpenMode.READ, String.class);
        SwingFileFilter<String> textFilter = findFilterForExtension(filters, "txt");

        // Verify the filter was found
        assertNotNull(textFilter, "Text filter should not be null");

        // Test with a text file name
        File dir = new File(".");
        String textFileName = "test.txt";
        assertTrue(textFilter.accept(dir, textFileName), "Text filter should accept a text file name");

        // Test with a non-text file name
        String nonTextFileName = "test.jpg";
        assertFalse(textFilter.accept(dir, nonTextFileName), "Text filter should not accept a non-text file name");
    }

    /**
     * Test the getDescription method.
     */
    @Test
    void testGetDescription() {
        // Create a filter for text files
        List<SwingFileFilter<String>> filters = SwingFileFilter.getFilters(OpenMode.READ, String.class);
        SwingFileFilter<String> textFilter = findFilterForExtension(filters, "txt");

        // Verify the filter was found
        assertNotNull(textFilter, "Text filter should not be null");

        // Verify the description
        String description = textFilter.getDescription();
        assertNotNull(description, "Description should not be null");
        assertFalse(description.isEmpty(), "Description should not be empty");
    }

    /**
     * Test the getFileType method.
     */
    @Test
    void testGetFileType() {
        // Create a filter for text files
        List<SwingFileFilter<String>> filters = SwingFileFilter.getFilters(OpenMode.READ, String.class);
        SwingFileFilter<String> textFilter = findFilterForExtension(filters, "txt");

        // Verify the filter was found
        assertNotNull(textFilter, "Text filter should not be null");

        // Verify the FileType
        FileType<String> fileType = textFilter.getFileType();
        assertNotNull(fileType, "FileType should not be null");
        assertTrue(fileType.matches("test.txt"), "FileType should match a text file");
        assertFalse(fileType.matches("test.jpg"), "FileType should not match a non-text file");
    }

    /**
     * Helper method to find a filter for a specific extension.
     *
     * @param filters the list of filters to search
     * @param extension the extension to search for
     * @return the filter for the extension, or null if not found
     */
    private SwingFileFilter<String> findFilterForExtension(List<SwingFileFilter<String>> filters, String extension) {
        for (SwingFileFilter<String> filter : filters) {
            if (filter.accept(new File("test." + extension))) {
                return filter;
            }
        }
        return null;
    }
}
