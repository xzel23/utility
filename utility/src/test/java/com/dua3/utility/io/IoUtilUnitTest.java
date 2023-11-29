package com.dua3.utility.io;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for methods in the `IoUtil` class.
 */
public final class IoUtilUnitTest {
    private static Path testDirectory;

    @BeforeAll
    static void setup() throws IOException {
        testDirectory = Files.createTempDirectory("ioUtilTest");
        Files.createFile(testDirectory.resolve("test1.txt"));
        Files.createFile(testDirectory.resolve("test2.md"));
        Files.createFile(testDirectory.resolve("test3.txt"));
    }

    @AfterAll
    static void cleanup() throws IOException {
        Files.deleteIfExists(testDirectory.resolve("test1.txt"));
        Files.deleteIfExists(testDirectory.resolve("test2.md"));
        Files.deleteIfExists(testDirectory.resolve("test3.txt"));
        Files.delete(testDirectory);
    }

    @Test
    void testFindFiles() throws IOException {
        // Test to ensure that `findFiles` returns correct files following the glob pattern "*.txt"
        List<Path> txtFiles = IoUtil.findFiles(testDirectory, "*.txt");

        assertTrue(txtFiles.contains(testDirectory.resolve("test1.txt")));
        assertTrue(txtFiles.contains(testDirectory.resolve("test3.txt")));
        assertTrue(txtFiles.size() == 2);
    }

    @Test
    void testFindFilesWithNoMatches() throws IOException {
        // Test to ensure that `findFiles` returns an empty list when there are no matches for the glob pattern
        List<Path> pyFiles = IoUtil.findFiles(testDirectory, "*.py");

        assertTrue(pyFiles.isEmpty());
    }

    /**
     * Test the `replaceExtension` method.
     */
    @Test
    public void testReplaceExtension() {
        // check a case with an extension
        String path1 = "/path/to/file.txt";
        String expected1 = "/path/to/file.pdf";
        String actual1 = IoUtil.replaceExtension(path1, "pdf");
        Assertions.assertEquals(expected1, actual1,
                "Failed to correctly replace extension in \""+path1+"\"");

        // check a case without an extension
        String path2 = "/path/to/file";
        String expected2 = "/path/to/file.pdf";
        String actual2 = IoUtil.replaceExtension(path2, "pdf");
        Assertions.assertEquals(expected2, actual2,
                "Failed to correctly append extension to \""+path2+"\"");

        // check a case with a dot in the path
        String path3 = "/path/.to/file";
        String expected3 = "/path/.to/file.pdf";
        String actual3 = IoUtil.replaceExtension(path3, "pdf");
        Assertions.assertEquals(expected3, actual3,
                "Failed to correctly append extension to \""+path3+"\"");

        // check a case with a special character in the filename
        String path4 = "/path/to/fil*&e.tx&t";
        String expected4 = "/path/to/fil*&e.pdf";
        String actual4 = IoUtil.replaceExtension(path4, "pdf");
        Assertions.assertEquals(expected4, actual4,
                "Failed to correctly replace extension in \""+path4+"\"");
    }
}