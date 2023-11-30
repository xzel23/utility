// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
     * The filesystem configurations to use in tests.
     * @return stream of file system configurationss
     */
    public static Stream<Configuration> jimFsConfigurations() {
        return Stream.of(Configuration.unix(), Configuration.windows(), Configuration.osX());
    }

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

    private static String normalize(Configuration configuration, String pathStr) {
        if (configuration.equals(Configuration.windows())) {
            if (pathStr.startsWith("/")) {
                pathStr = "c:" + pathStr;
            }
            pathStr = pathStr.replace("/", "\\");
        }
        return pathStr;
    }

    /**
     * Get a normalized pattern for testing, i. e. makes sure that absolute windows patterns are
     * prefixed with a drive letter. Note that slashes a pattern separators are retained.
     * @param configuration the {@link Configuration} according to which the path should be normalized
     * @param pattern the path as a String
     * @return the normalized patern
     */
    private static String getPattern(Configuration configuration, String pattern) {
        if (configuration.equals(Configuration.windows())) {
            if (pattern.startsWith("/")) {
                pattern = "c:" + pattern;
            }
        }
        return pattern;
    }

    /**
     * Get a path for testing, i. e. makes sure that absolute windows paths are
     * prefixed with a drive letter and use backslashes as separators.
     * @param configuration the {@link Configuration} according to which the path should be normalized
     * @param pathStr the path as a String
     * @return the normalized path
     */
    private static Path getPath(Configuration configuration, FileSystem fs, String pathStr) {
        return fs.getPath(normalize(configuration, pathStr));
    }

    private static void assertPathEquals(Path a, Path b) {
        assertEquals(String.valueOf(a), String.valueOf(b));
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));

        String pathStr = "foo/bar/baz/*.txt";
        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/"),
                getPattern(configuration, pathStr)
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        // It should find one matching path only and it should be "/foo/bar/baz/file.txt"
        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/bar/baz/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_absoluteBase_absolutePattern(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "/foo/bar/baz/*.txt")
        );
        List<Path> resultPaths = resultStream.toList();

        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/bar/baz/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_relativeBase_relativePattern(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "bar/baz/*.txt")
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/bar/baz/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_relativeBase_relativePatternFileInBaseDir(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "*.txt")
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_matching_nonmatching_files(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock FS
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.doc")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "bar/baz/*.txt")
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/bar/baz/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_different_directory_depths(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/a/b/c")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/a/a_file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/a/b/b_file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/a/b/c/c_file.txt")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "**/*.txt")
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertEquals(3, resultPaths.size());
        assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/a_file.txt")));
        assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/b/b_file.txt")));
        assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/b/c/c_file.txt")));
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_no_glob_symbols(Configuration configuration) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);

        // Setup directory structure in mock file system
        Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
        Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

        Stream<Path> resultStream = IoUtil.glob(
                getPath(configuration, fs, "/foo"),
                getPattern(configuration, "bar/baz/file.txt")
        );
        List<Path> resultPaths = resultStream.collect(Collectors.toList());

        assertTrue(resultPaths.size() == 1);
        assertPathEquals(
                resultPaths.get(0),
                getPath(configuration, fs, "/foo/bar/baz/file.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testFindFiles(Configuration configuration) throws IOException {
        // Test to ensure that `findFiles` returns correct files following the glob pattern "*.txt"
        FileSystem fs = Jimfs.newFileSystem(configuration);
        Path testDirectory = Files.createDirectories(getPath(configuration, fs, "ioUtilTest"));
        Files.createFile(testDirectory.resolve("test1.txt"));
        Files.createFile(testDirectory.resolve("test2.md"));
        Files.createFile(testDirectory.resolve("test3.txt"));

        List<Path> txtFiles = IoUtil.findFiles(testDirectory, "*.txt");

        assertTrue(txtFiles.contains(testDirectory.resolve("test1.txt")));
        assertTrue(txtFiles.contains(testDirectory.resolve("test3.txt")));
        assertTrue(txtFiles.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testFindFilesWithNoMatches(Configuration configuration) throws IOException {
        // Test to ensure that `findFiles` returns an empty list when there are no matches for the glob pattern
        FileSystem fs = Jimfs.newFileSystem(configuration);
        Path testDirectory = getPath(configuration, fs, "ioUtilTest");
        Files.createDirectories(testDirectory);
        Files.createFile(testDirectory.resolve("test1.txt"));
        Files.createFile(testDirectory.resolve("test2.md"));
        Files.createFile(testDirectory.resolve("test3.txt"));
        List<Path> pyFiles = IoUtil.findFiles(testDirectory, "*.py");

        assertTrue(pyFiles.isEmpty());
    }

}
