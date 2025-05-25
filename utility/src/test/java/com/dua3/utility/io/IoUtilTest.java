// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void testGetExtension() {
        Path path = Paths.get("folder", "text.txt");
        String expected = "txt";
        String actual = IoUtil.getExtension(path);
        assertEquals(expected, actual);
    }

    @Test
    void testGetExtensionStringArg() {
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
    void testStripExtension() {
        assertEquals("test", IoUtil.stripExtension("test.txt"));
        assertEquals("folder/subfolder/test", IoUtil.stripExtension("folder/subfolder/test.txt"));
        assertEquals("folder/subfolder/test/", IoUtil.stripExtension("folder/subfolder/test.txt/"));
        assertEquals("./folder/subfolder/test", IoUtil.stripExtension("./folder/subfolder/test.txt"));
        assertEquals("./folder/subfolder/test/", IoUtil.stripExtension("./folder/subfolder/test.txt/"));

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
    void testReplaceExtension() {
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


    /**
     * Test replaceExtension with valid input.
     */
    @Test
    void testReplaceExtensionValidInput() {
        String path = "/Users/tester/desktop/file.txt";
        String extension = "docx";
        String expected = "/Users/tester/desktop/file.docx";

        assertEquals(expected, IoUtil.replaceExtension(path, extension));
    }

    /**
     * Test replaceExtension with empty extension.
     */
    @Test
    void testReplaceExtensionEmptyExtension() {
        String path = "/Users/tester/desktop/file.txt";
        String extension = "";
        String expected = "/Users/tester/desktop/file.";

        assertEquals(expected, IoUtil.replaceExtension(path, extension));
    }

    /**
     * Test replaceExtension with non-existing extension.
     */
    @Test
    void testReplaceExtensionNonExistingExtension() {
        String path = "/Users/tester/desktop/file";
        String extension = "txt";
        String expected = "/Users/tester/desktop/file.txt";

        assertEquals(expected, IoUtil.replaceExtension(path, extension));
    }

    /**
     * Test replaceExtension with empty path.
     */
    @Test
    void testReplaceExtensionEmptyPath() {
        String path = "";
        String extension = "docx";

        assertThrows(IllegalArgumentException.class, () -> IoUtil.replaceExtension(path, extension));
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
        if (configuration.equals(Configuration.windows()) && pattern.startsWith("/")) {
            pattern = "c:" + pattern;
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
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {

            // Setup directory structure in mock file system
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));

            String pathStr = "foo/bar/baz/*.txt";
            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/"),
                    getPattern(configuration, pathStr)
            );
            List<Path> resultPaths = resultStream.toList();

            // It should find one matching path only and it should be "/foo/bar/baz/file.txt"
            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/bar/baz/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_absoluteBase_absolutePattern(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
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

            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/bar/baz/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_relativeBase_relativePattern(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            // Setup directory structure in mock file system
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/foo"),
                    getPattern(configuration, "bar/baz/*.txt")
            );
            List<Path> resultPaths = resultStream.toList();

            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/bar/baz/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_relativeBase_relativePatternFileInBaseDir(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            // Setup directory structure in mock file system
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/foo"),
                    getPattern(configuration, "*.txt")
            );
            List<Path> resultPaths = resultStream.toList();

            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_matching_nonmatching_files(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {

            // Setup directory structure in mock FS
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.doc")));

            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/foo"),
                    getPattern(configuration, "bar/baz/*.txt")
            );
            List<Path> resultPaths = resultStream.toList();

            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/bar/baz/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_different_directory_depths(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            // Setup directory structure in mock file system
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/a/b/c")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/a/a_file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/a/b/b_file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/a/b/c/c_file.txt")));

            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/foo"),
                    getPattern(configuration, "**/*.txt")
            );
            List<Path> resultPaths = resultStream.toList();

            assertEquals(3, resultPaths.size());
            assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/a_file.txt")));
            assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/b/b_file.txt")));
            assertTrue(resultPaths.contains(getPath(configuration, fs, "/foo/a/b/c/c_file.txt")));
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void glob_no_glob_symbols(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            // Setup directory structure in mock file system
            Files.createDirectories(fs.getPath(normalize(configuration, "/foo/bar/baz")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/baz/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/bar/file.txt")));
            Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));

            Stream<Path> resultStream = IoUtil.glob(
                    getPath(configuration, fs, "/foo"),
                    getPattern(configuration, "bar/baz/file.txt")
            );
            List<Path> resultPaths = resultStream.toList();

            assertEquals(1, resultPaths.size());
            assertPathEquals(
                    resultPaths.get(0),
                    getPath(configuration, fs, "/foo/bar/baz/file.txt")
            );
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testFindFiles(Configuration configuration) throws IOException {
        // Test to ensure that `findFiles` returns correct files following the glob pattern "*.txt"
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path testDirectory = Files.createDirectories(getPath(configuration, fs, "ioUtilTest"));
            Files.createFile(testDirectory.resolve("test1.txt"));
            Files.createFile(testDirectory.resolve("test2.md"));
            Files.createFile(testDirectory.resolve("test3.txt"));

            List<Path> txtFiles = IoUtil.findFiles(testDirectory, "*.txt");

            assertTrue(txtFiles.contains(testDirectory.resolve("test1.txt")));
            assertTrue(txtFiles.contains(testDirectory.resolve("test3.txt")));
            assertEquals(2, txtFiles.size());
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testFindFilesWithNoMatches(Configuration configuration) throws IOException {
        // Test to ensure that `findFiles` returns an empty list when there are no matches for the glob pattern
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path testDirectory = getPath(configuration, fs, "ioUtilTest");
            Files.createDirectories(testDirectory);
            Files.createFile(testDirectory.resolve("test1.txt"));
            Files.createFile(testDirectory.resolve("test2.md"));
            Files.createFile(testDirectory.resolve("test3.txt"));
            List<Path> pyFiles = IoUtil.findFiles(testDirectory, "*.py");

            assertTrue(pyFiles.isEmpty());
        }
    }

    /**
     * Creates a file system for testing the zip functionality.
     *
     * @param configuration the configuration object for the file system
     * @param rootPath the root path for the created file system
     * @param zipUrl the URL of the zip file to be used for creating the file system
     * @return the created file system
     * @throws IOException if an I/O error occurs during the creation process
     */
    static FileSystem createFileSystemForZipTest(Configuration configuration, String rootPath, URL zipUrl) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(configuration);
        Path data = fs.getPath(normalize(configuration, rootPath));
        Files.createDirectories(data);
        IoUtil.unzip(zipUrl, data);
        return fs;
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testZip(Configuration configuration) throws IOException {
        URL zipUrl = LangUtil.getResourceURL(getClass(), "test.zip");

        String rootPath = "/testZip";
        try (FileSystem fs = createFileSystemForZipTest(configuration, rootPath, zipUrl)) {
            Path root = fs.getPath(normalize(configuration, rootPath));

            Map<String, String> expected = Map.of(
                    "test", "",
                    "test/1", "",
                    "test/1/file.txt", "b4e448e8600fa63f41cc30e5e784f75c",
                    "test/1/empty_directory", "",
                    "test/1/.hidden_file", "62dd8104749e42d09f8ecfde4ea6ca2f",
                    "test/README.md", "46f8fd89ede71401240d2ba07dda83d5"
            );

            // test that the filesystem content is correct (this also test unzip result)
            Path source = root.resolve("test");
            Map<String, String> sourceHashes = createHashes(source);
            assertEquals(expected, sourceHashes);

            // create a zip file "test.zip" with the contents of the "test" folder
            Path destinationZip = root.resolve("test.zip");
            assertDoesNotThrow(() -> IoUtil.zip(destinationZip, source));

            Path destinationFolder = root.resolve("unzipped");
            Files.createDirectories(destinationFolder);
            IoUtil.unzip(destinationZip.toUri().toURL(), destinationFolder);

            Map<String, String> destinationHashes = createHashes(destinationFolder.resolve("test"));
            assertEquals(expected, destinationHashes);
        }
    }

    /**
     * Creates a map of file paths and their corresponding hashes for a given folder.
     * Directories are assigned the empty string.
     *
     * @param dir the path of the folder to create hashes from
     * @throws IOException if an I/O error occurs during the hash creation process
     */
    private Map<String, String> createHashes(Path dir) throws IOException {
        Map<String, String> m = new HashMap<>();
        Path parent = LangUtil.orElse(dir.getParent(), dir.getFileSystem().getPath("."));
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.forEach(p -> {
                String key = parent.relativize(p).normalize().toString().replace("\\", "/");
                if (Files.isDirectory(p)) {
                    m.put(key, "");
                } else {
                    try (InputStream in = Files.newInputStream(p)) {
                        m.put(key, TextUtil.getMD5String(in));
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to read file " + p, e);
                    }
                }
            });
        }
        return m;
    }

    @Test
    void toURIUrlArgumentParsesCorrectly() throws Exception {
        URL url = new URI("https://example.com").toURL();
        URI expected = url.toURI();

        assertEquals(expected, IoUtil.toURI(url));
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    public void toUriPathArgument_AbsolutePath(Configuration configuration) throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path filePath = fs.getPath(normalize(configuration, "/test/path"));

            URI actualUri = IoUtil.toURI(filePath);
            assertEquals(filePath, Paths.get(actualUri));
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    public void toUriPathArgument_RelativePath(Configuration configuration) throws Exception {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path filePath = fileSystem.getPath("test/path");

            Path root = fileSystem.getPath("/");
            URI actualUri = IoUtil.toURI(filePath);
            assertEquals(root.resolve(filePath), Paths.get(root.toUri().resolve(actualUri)));
        }
    }

    @Test
    void testGetFilename() {
        assertEquals("file.txt", IoUtil.getFilename("/path/to/file.txt"));
        assertEquals("file.txt", IoUtil.getFilename("path/to/file.txt"));
        assertEquals("file", IoUtil.getFilename("/path/to/file"));
        assertEquals("file", IoUtil.getFilename("path/to/file"));
        assertEquals("to", IoUtil.getFilename("/path/to/"));
        assertEquals("to", IoUtil.getFilename("path/to/"));
        assertEquals("", IoUtil.getFilename(""));
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testToUnixPath(Configuration configuration) throws Exception {

        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path dir = Files.createDirectories(fs.getPath(normalize(configuration, "/foo")));

            String prefix = configuration.toString().toLowerCase(Locale.ROOT).contains("windows") ? "/c/" : "/";
            assertEquals(prefix + "foo", IoUtil.toUnixPath(dir));

            Path file = Files.createFile(fs.getPath(normalize(configuration, "/foo/file.txt")));
            assertEquals(prefix + "foo/file.txt", IoUtil.toUnixPath(file));
            assertEquals("foo/file.txt", IoUtil.toUnixPath(dir.getParent().relativize(file)));
        }
    }

    @Test
    void testStringInputStream() throws IOException {
        String testString = "test string";
        try (InputStream is = IoUtil.stringInputStream(testString)) {
            byte[] buffer = is.readAllBytes();
            assertEquals(testString, new String(buffer, StandardCharsets.UTF_8));
        }
    }

    @Test
    void testComposedClose() throws Exception {
        boolean[] closed = {false, false};
        AutoCloseable closeable1 = () -> closed[0] = true;
        AutoCloseable closeable2 = () -> closed[1] = true;

        Runnable closeAll = IoUtil.composedClose(closeable1, closeable2);
        closeAll.run();

        assertTrue(closed[0]);
        assertTrue(closed[1]);
    }

    @Test
    void testCloseAll() throws Exception {
        boolean[] closed = {false, false};
        AutoCloseable closeable1 = () -> closed[0] = true;
        AutoCloseable closeable2 = () -> closed[1] = true;

        IoUtil.closeAll(closeable1, closeable2);

        assertTrue(closed[0]);
        assertTrue(closed[1]);
    }
}
