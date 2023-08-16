// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the FileSystemView class.
 */
public class IoUtilTest {

    /**
     * Test getting extension for a Path.
     */
    @Test
    public void testGetExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "txt";
        String actual = IoUtil.getExtension(path);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetExtensionStringArg() {
        assertEquals("txt", IoUtil.getExtension("test.txt"));
        assertEquals("txt", IoUtil.getExtension("folder/subfolder/test.txt"));
        assertEquals("txt", IoUtil.getExtension("folder/subfolder/test.txt/"));
        assertEquals("txt", IoUtil.getExtension("./folder/subfolder/test.txt"));
        assertEquals("txt", IoUtil.getExtension("./folder/subfolder/test.txt/"));

        assertEquals("def", IoUtil.getExtension("test.txt.def"));

        assertEquals("", IoUtil.getExtension("test"));
        assertEquals("", IoUtil.getExtension("folder/subfolder/test"));
        assertEquals("", IoUtil.getExtension("folder/subfolder/test/"));
        assertEquals("", IoUtil.getExtension("./folder/subfolder/test"));
        assertEquals("", IoUtil.getExtension("./folder/subfolder/test/"));
        assertEquals("", IoUtil.getExtension("/test"));
        assertEquals("", IoUtil.getExtension("./test"));
        assertEquals("", IoUtil.getExtension("../test"));
    }

    @Test
    public void testStripExtension() {
        assertEquals("test", IoUtil.stripExtension("test.txt"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test.txt"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test.txt/"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test.txt"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test.txt/"));

        assertEquals("test.txt", IoUtil.stripExtension("test.txt.def"));

        assertEquals("test", IoUtil.stripExtension("test"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test"));
        assertEquals("folder/subfolder/test/", IoUtil.stripExtension("folder/subfolder/test/"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test"));
        assertEquals("./folder/subfolder/test/", IoUtil.stripExtension("./folder/subfolder/test/"));
        assertEquals("/test", IoUtil.stripExtension("/test"));
        assertEquals("./test", IoUtil.stripExtension("./test"));
        assertEquals("../test", IoUtil.stripExtension("../test"));
    }

    @Test
    public void testReplaceExtension() {
        assertEquals("test.xyz", IoUtil.replaceExtension("test.txt", "xyz"));
        assertEquals("folder/subfolder/test.xyz", IoUtil.replaceExtension("folder/subfolder/test.txt", "xyz"));
        assertEquals("folder/subfolder/test.xyz/", IoUtil.replaceExtension("folder/subfolder/test.txt/", "xyz"));
        assertEquals("./folder/subfolder/test.xyz", IoUtil.replaceExtension("./folder/subfolder/test.txt", "xyz"));
        assertEquals("./folder/subfolder/test.xyz/", IoUtil.replaceExtension("./folder/subfolder/test.txt/", "xyz"));

        assertEquals("test.txt.abc", IoUtil.replaceExtension("test.txt.def", "abc"));

        assertEquals("test.xyz", IoUtil.replaceExtension("test", "xyz"));
        assertEquals("folder/subfolder/test.xyz", IoUtil.replaceExtension("folder/subfolder/test", "xyz"));
        assertEquals("folder/subfolder/test.xyz/", IoUtil.replaceExtension("folder/subfolder/test/", "xyz"));
        assertEquals("./folder/subfolder/test.xyz", IoUtil.replaceExtension("./folder/subfolder/test", "xyz"));
        assertEquals("./folder/subfolder/test.xyz/", IoUtil.replaceExtension("./folder/subfolder/test/", "xyz"));
        assertEquals("/test.xyz", IoUtil.replaceExtension("/test", "xyz"));
        assertEquals("./test.xyz", IoUtil.replaceExtension("./test", "xyz"));
        assertEquals("../test.xyz", IoUtil.replaceExtension("../test", "xyz"));
    }

    @Test
    void glob() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/"), "foo/bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        // It should find one matching path only and it should be "/foo/bar/baz/file.txt"
        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/bar/baz/file.txt"));
    }

    @Test
    void glob_absoluteBase_absolutePattern() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("/foo/bar/file.txt"));
        Files.createFile(fs.getPath("/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "/foo/bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/bar/baz/file.txt"));
    }

    @Test
    void glob_relativeBase_relativePattern() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("/foo/bar/file.txt"));
        Files.createFile(fs.getPath("/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/bar/baz/file.txt"));
    }

    @Test
    void glob_relativeBase_relativePatternFileInBaseDir() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("/foo/bar/file.txt"));
        Files.createFile(fs.getPath("/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/file.txt"));
    }

    @Test
    void glob_matching_nonmatching_files() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock FS
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.doc"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/bar/baz/file.txt"));
    }

    @Test
    void glob_different_directory_depths() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/a/b/c"));
        Files.createFile(fs.getPath("/foo/a/a_file.txt"));
        Files.createFile(fs.getPath("/foo/a/b/b_file.txt"));
        Files.createFile(fs.getPath("/foo/a/b/c/c_file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "**/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertEquals(3, resultPaths.size());
        assertTrue(resultPaths.contains(fs.getPath("/foo/a/a_file.txt")));
        assertTrue(resultPaths.contains(fs.getPath("/foo/a/b/b_file.txt")));
        assertTrue(resultPaths.contains(fs.getPath("/foo/a/b/c/c_file.txt")));
    }

    @Test
    void glob_no_glob_symbols() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("/foo/bar/baz"));
        Files.createFile(fs.getPath("/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("/foo/bar/file.txt"));
        Files.createFile(fs.getPath("/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("/foo"), "bar/baz/file.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("/foo/bar/baz/file.txt"));
    }

    @Test
    void glob_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/"), "foo/bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        // It should find one matching path only, and it should be "C:\foo\bar\baz\file.txt"
        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\bar\\baz\\file.txt"));
    }

    @Test
    void glob_absoluteBase_absolutePattern_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("C:/foo/bar/file.txt"));
        Files.createFile(fs.getPath("C:/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "C:/foo/bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\bar\\baz\\file.txt"));
    }

    @Test
    void glob_relativeBase_relativePattern_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("C:/foo/bar/file.txt"));
        Files.createFile(fs.getPath("C:/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\bar\\baz\\file.txt"));
    }

    @Test
    void glob_relativeBase_relativePatternFileInBaseDir_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("C:/foo/bar/file.txt"));
        Files.createFile(fs.getPath("C:/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\file.txt"));
    }

    @Test
    void glob_matching_nonmatching_files_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock FS
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.doc"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "bar/baz/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\bar\\baz\\file.txt"));
    }

    @Test
    void glob_different_directory_depths_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/a/b/c"));
        Files.createFile(fs.getPath("C:/foo/a/a_file.txt"));
        Files.createFile(fs.getPath("C:/foo/a/b/b_file.txt"));
        Files.createFile(fs.getPath("C:/foo/a/b/c/c_file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "**/*.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertEquals(3, resultPaths.size());
        assertTrue(resultPaths.contains(fs.getPath("C:/foo/a/a_file.txt")));
        assertTrue(resultPaths.contains(fs.getPath("C:/foo/a/b/b_file.txt")));
        assertTrue(resultPaths.contains(fs.getPath("C:/foo/a/b/c/c_file.txt")));
    }

    @Test
    void glob_no_glob_symbols_Windows() throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.windows());

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath("C:/foo/bar/baz"));
        Files.createFile(fs.getPath("C:/foo/bar/baz/file.txt"));
        Files.createFile(fs.getPath("C:/foo/bar/file.txt"));
        Files.createFile(fs.getPath("C:/foo/file.txt"));

        Stream<Path> resultStream = IoUtil.glob(fs.getPath("C:/foo"), "bar/baz/file.txt");
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertTrue(resultPaths.get(0).toString().equals("C:\\foo\\bar\\baz\\file.txt"));
    }
}
