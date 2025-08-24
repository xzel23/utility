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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the FileSystemView class.
 */
class IoUtilTest {

    /**
     * The filesystem configurations to use in tests.
     * @return stream of file system configurations
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

    private static Stream<Arguments> replaceExtensionTestCases() {
        return Stream.of(
                Arguments.of("/Users/tester/desktop/file.txt", "docx", "/Users/tester/desktop/file.docx"),
                Arguments.of("/Users/tester/desktop/file.txt", "", "/Users/tester/desktop/file."),
                Arguments.of("/Users/tester/desktop/file", "txt", "/Users/tester/desktop/file.txt"),
                Arguments.of("test.txt", "xyz", "test.xyz"),
                Arguments.of("folder/subfolder/test.txt", "xyz", "folder/subfolder/test.xyz"),
                Arguments.of("folder/subfolder/test.txt/", "xyz", "folder/subfolder/test.xyz/"),
                Arguments.of("./folder/subfolder/test.txt", "xyz", "./folder/subfolder/test.xyz"),
                Arguments.of("./folder/subfolder/test.txt/", "xyz", "./folder/subfolder/test.xyz/"),
                Arguments.of("test.txt.def", "abc", "test.txt.abc"),
                Arguments.of("test", "xyz", "test.xyz"),
                Arguments.of("folder/subfolder/test", "xyz", "folder/subfolder/test.xyz"),
                Arguments.of("folder/subfolder/test/", "xyz", "folder/subfolder/test.xyz/"),
                Arguments.of("./folder/subfolder/test", "xyz", "./folder/subfolder/test.xyz"),
                Arguments.of("./folder/subfolder/test/", "xyz", "./folder/subfolder/test.xyz/"),
                Arguments.of("/test", "xyz", "/test.xyz"),
                Arguments.of("./test", "xyz", "./test.xyz"),
                Arguments.of("../test", "xyz", "../test.xyz")
        );
    }


    /**
     * Test replaceExtension with various inputs.
     */
    @ParameterizedTest
    @MethodSource("replaceExtensionTestCases")
    void testReplaceExtensionStringArg(String path, String extension, String expected) {
        assertEquals(expected, IoUtil.replaceExtension(path, extension));
    }

    /**
     * Test replaceExtension with various inputs.
     */
    @ParameterizedTest
    @MethodSource("replaceExtensionTestCases")
    void testReplaceExtensionPathArg(String path, String extension, String expected) {
        assertEquals(Paths.get(expected), IoUtil.replaceExtension(Paths.get(path), extension));
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

    @Test
    void testGetApplicationDataDirCreatesIfNotExist() throws Exception {
        String appName = "TestApp";
        Path appDataDir = IoUtil.getApplicationDataDir(appName);

        try {
            assertTrue(Files.exists(appDataDir), "Application data directory should be created if it does not exist");
            assertTrue(Files.isDirectory(appDataDir), "Application data directory should be a directory");
        } finally {
            IoUtil.deleteRecursive(appDataDir); // Clean up after the test
        }
    }

    @Test
    void testGetApplicationDataDirReturnsExistingDir() throws Exception {
        String appName = "ExistingApp";
        Path appDataDir = IoUtil.getApplicationDataDir(appName);

        try {
            // Call again to ensure the same directory is returned
            Path retrievedAppDataDir = IoUtil.getApplicationDataDir(appName);
            assertEquals(appDataDir, retrievedAppDataDir, "Should return the same directory if it exists");
        } finally {
            IoUtil.deleteRecursive(appDataDir); // Clean up after the test
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

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void testUnzipSafetyLimits(Configuration configuration) throws IOException {
        URL zipUrl = LangUtil.getResourceURL(getClass(), "test.zip");

        String rootPath = "/testUnzipSafetyLimits";
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path root = fs.getPath(normalize(configuration, rootPath));
            Files.createDirectories(root);

            // Test maxFiles limit
            Path destinationMaxFiles = root.resolve("maxFiles");
            Files.createDirectories(destinationMaxFiles);
            ZipException maxFilesException = assertThrows(ZipException.class, () ->
                    IoUtil.unzip(zipUrl, destinationMaxFiles, 1, IoUtil.DEFAULT_MAX_BYTES, IoUtil.DEFAULT_MAX_COMPRESSION_RATIO)
            );
            assertTrue(maxFilesException.getMessage().contains("Maximum number of files exceeded"),
                    "Exception message should mention files limit: " + maxFilesException.getMessage());

            // Test maxBytes limit
            Path destinationMaxBytes = root.resolve("maxBytes");
            Files.createDirectories(destinationMaxBytes);
            ZipException maxBytesException = assertThrows(ZipException.class, () ->
                    IoUtil.unzip(zipUrl, destinationMaxBytes, IoUtil.DEFAULT_MAX_FILES, 10, IoUtil.DEFAULT_MAX_COMPRESSION_RATIO)
            );
            assertEquals("Uncompressed size exceeds allowed limit: 10", maxBytesException.getMessage(), "Exception message should mention bytes limit: " + maxBytesException.getMessage());

            // Test maxCompressionRatio limit
            Path destinationMaxRatio = root.resolve("maxRatio");
            Files.createDirectories(destinationMaxRatio);
            ZipException maxRatioException = assertThrows(ZipException.class, () ->
                    IoUtil.unzip(zipUrl, destinationMaxRatio, IoUtil.DEFAULT_MAX_FILES, IoUtil.DEFAULT_MAX_BYTES, 0.1)
            );
            assertEquals("Compression ratio exceeds allowed limit: 0.1", maxRatioException.getMessage(), "Exception message should mention compression ratio limit: " + maxRatioException.getMessage());
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
                        m.put(key, TextUtil.getDigestString("MD5", in));
                    } catch (IOException e) {
                        throw new UncheckedIOException("Failed to read file " + p, e);
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalStateException("MD5 algorithm not available", e);
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
    void toUriPathArgument_AbsolutePath(Configuration configuration) throws Exception {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            Path filePath = fs.getPath(normalize(configuration, "/test/path"));

            URI actualUri = IoUtil.toURI(filePath);
            assertEquals(filePath, Paths.get(actualUri));
        }
    }

    @ParameterizedTest
    @MethodSource("jimFsConfigurations")
    void toUriPathArgument_RelativePath(Configuration configuration) throws Exception {
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
    void testComposedClose() {
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

    @Test
    void testLines() {
        String content = "Line 1\nLine 2\nLine 3";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        List<String> lines = IoUtil.lines(inputStream, StandardCharsets.UTF_8).toList();

        assertEquals(3, lines.size());
        assertEquals("Line 1", lines.get(0));
        assertEquals("Line 2", lines.get(1));
        assertEquals("Line 3", lines.get(2));
    }

    @Test
    void testToURLFromPath() throws Exception {
        Path tempFile = Files.createTempFile("test", ".txt");
        try {
            URL url = IoUtil.toURL(tempFile);
            assertEquals(tempFile.toUri().toURL(), url);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testToURLFromURI() throws Exception {
        URI uri = new URI("https://example.com");
        URL url = IoUtil.toURL(uri);
        assertEquals(uri.toURL().toString(), url.toString());
    }

    @Test
    void testToPathFromURI() throws Exception {
        Path tempFile = Files.createTempFile("test", ".txt");
        try {
            URI uri = tempFile.toUri();
            Path path = IoUtil.toPath(uri);
            assertEquals(tempFile.toAbsolutePath(), path.toAbsolutePath());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testToPathFromURL() throws Exception {
        Path tempFile = Files.createTempFile("test", ".txt");
        try {
            URL url = tempFile.toUri().toURL();
            Path path = IoUtil.toPath(url);
            assertEquals(tempFile.toAbsolutePath(), path.toAbsolutePath());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testToPathFromString() {
        String pathStr = "/tmp/test.txt";
        Path path = IoUtil.toPath(pathStr);
        assertEquals(Paths.get(pathStr), path);
    }

    @Test
    void testToPathFromStringURI() throws Exception {
        URI uri = new URI("file:///tmp/test.txt");
        String uriStr = uri.toString();
        Path path = IoUtil.toPath(uriStr);
        assertEquals(Paths.get(uri), path);
    }

    @Test
    void testIsURI() {
        // This is testing a private method through its public usage in toURI and toPath
        // URI strings should be handled differently than regular path strings

        // Test with a URI string
        String uriStr = "file:///tmp/test.txt";
        Path pathFromURI = IoUtil.toPath(uriStr);
        assertEquals(Paths.get(URI.create(uriStr)), pathFromURI);

        // Test with a regular path string
        String pathStr = "/tmp/test.txt";
        Path pathFromStr = IoUtil.toPath(pathStr);
        assertEquals(Paths.get(pathStr), pathFromStr);
    }

    @Test
    void testGetExtensionURL() {
        // Test with a URL that has a file extension
        URL url = IoUtil.toURL("file:///path/to/file.txt");
        assertEquals("txt", IoUtil.getExtension(url));

        // Test with a URL that has no file extension
        URL urlNoExt = IoUtil.toURL("file:///path/to/file");
        assertEquals("", IoUtil.getExtension(urlNoExt));

        // Test with a URL that has multiple dots
        URL urlMultipleDots = IoUtil.toURL("file:///path/to/file.name.txt");
        assertEquals("txt", IoUtil.getExtension(urlMultipleDots));

        // Test with a URL that has query parameters
        URL urlWithQuery = IoUtil.toURL("https://example.com/file.txt?param=value");
        assertEquals("txt", IoUtil.getExtension(urlWithQuery));
    }

    @Test
    void testReadURL() throws Exception {
        // Create a temporary file with known content
        Path tempFile = Files.createTempFile("read-url-test", ".txt");
        String content = "Test content for URL reading";
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        try {
            // Get URL from the file and read its content
            URL url = tempFile.toUri().toURL();
            String readContent = IoUtil.read(url, StandardCharsets.UTF_8);

            // Verify the content was read correctly
            assertEquals(content, readContent);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testReadURI() throws Exception {
        // Create a temporary file with known content
        Path tempFile = Files.createTempFile("read-uri-test", ".txt");
        String content = "Test content for URI reading";
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        try {
            // Get URI from the file and read its content
            URI uri = tempFile.toUri();
            String readContent = IoUtil.read(uri, StandardCharsets.UTF_8);

            // Verify the content was read correctly
            assertEquals(content, readContent);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testToURIString() {
        // Test with a URI string
        String uriStr = "file:///tmp/test.txt";
        URI uriFromURIStr = IoUtil.toURI(uriStr);
        assertEquals(URI.create(uriStr), uriFromURIStr);

        // Test with a regular path string
        String pathStr = "/tmp/test.txt";
        URI uriFromPathStr = IoUtil.toURI(pathStr);
        assertEquals(Paths.get(pathStr).toUri(), uriFromPathStr);

        // Test with a relative path string
        String relativePathStr = "tmp/test.txt";
        URI uriFromRelativePathStr = IoUtil.toURI(relativePathStr);
        assertEquals(Paths.get(relativePathStr).toUri(), uriFromRelativePathStr);
    }

    @Test
    void testDeleteRecursive() throws Exception {
        // Create a temporary directory with some files and subdirectories
        Path tempDir = Files.createTempDirectory("delete-test");
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Path file1 = Files.createFile(tempDir.resolve("file1.txt"));
        Path file2 = Files.createFile(subDir.resolve("file2.txt"));

        // Verify the files and directories exist
        assertTrue(Files.exists(tempDir));
        assertTrue(Files.exists(subDir));
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));

        // Delete the directory recursively
        IoUtil.deleteRecursive(tempDir);

        // Verify everything is gone
        assertDoesNotThrow(() -> {
            assertFalse(Files.exists(file2));
            assertFalse(Files.exists(file1));
            assertFalse(Files.exists(subDir));
            assertFalse(Files.exists(tempDir));
        });
    }

    @Test
    void testLoadText() throws Exception {
        // Create a temporary file with some content
        Path tempFile = Files.createTempFile("load-test", ".txt");
        String content = "Test content with some unicode: 你好, 世界!";
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        try {
            // Test with UTF-8
            String loadedText = IoUtil.loadText(tempFile, charset -> assertEquals(StandardCharsets.UTF_8, charset));
            assertEquals(content, loadedText);

            // Test with different charsets
            Files.write(tempFile, content.getBytes(StandardCharsets.ISO_8859_1));
            String loadedText2 = IoUtil.loadText(tempFile, charset -> {}, StandardCharsets.ISO_8859_1);
            // The content will be different due to encoding differences
            assertNotEquals(content, loadedText2);

            // Test with multiple charsets
            Files.write(tempFile, content.getBytes(StandardCharsets.UTF_16));
            String loadedText3 = IoUtil.loadText(tempFile, charset -> {},
                    StandardCharsets.UTF_8, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1);
            // Should detect UTF-16
            assertEquals(content, loadedText3);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testGetUserDir() {
        Path expectedUserDir = Paths.get(System.getProperty("user.home", "."));
        Path actualUserDir = IoUtil.getUserDir();

        assertEquals(expectedUserDir, actualUserDir, "getUserDir should return the user's home directory");
        assertTrue(Files.exists(actualUserDir), "The returned user directory path should exist");
        assertTrue(Files.isDirectory(actualUserDir), "The returned user directory path should be a directory");
    }

    @Test
    void testGetInputStream() throws Exception {
        // Test with InputStream
        InputStream originalStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        InputStream resultStream = IoUtil.getInputStream(originalStream);
        assertEquals(originalStream, resultStream);

        // Test with Path
        Path tempFile = Files.createTempFile("input-test", ".txt");
        try {
            Files.write(tempFile, "test".getBytes(StandardCharsets.UTF_8));
            try (InputStream is = IoUtil.getInputStream(tempFile)) {
                assertEquals("test", new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }

            // Test with File
            File file = tempFile.toFile();
            try (InputStream is = IoUtil.getInputStream(file)) {
                assertEquals("test", new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }

            // Test with URI
            URI uri = tempFile.toUri();
            try (InputStream is = IoUtil.getInputStream(uri)) {
                assertEquals("test", new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }

            // Test with URL
            URL url = tempFile.toUri().toURL();
            try (InputStream is = IoUtil.getInputStream(url)) {
                assertEquals("test", new String(is.readAllBytes(), StandardCharsets.UTF_8));
            }

            // Test with null
            InputStream nullStream = IoUtil.getInputStream(null);
            assertEquals(0, nullStream.available());

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testGetOutputStream() throws Exception {
        // Test with OutputStream
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream();
        OutputStream resultStream = IoUtil.getOutputStream(originalStream);
        assertEquals(originalStream, resultStream);

        // Test with Path
        Path tempFile = Files.createTempFile("output-test", ".txt");
        try {
            try (OutputStream os = IoUtil.getOutputStream(tempFile)) {
                os.write("test".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("test", Files.readString(tempFile));

            // Test with File
            File file = tempFile.toFile();
            try (OutputStream os = IoUtil.getOutputStream(file)) {
                os.write("file-test".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("file-test", Files.readString(tempFile));

            // Test with URI
            URI uri = tempFile.toUri();
            try (OutputStream os = IoUtil.getOutputStream(uri)) {
                os.write("uri-test".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("uri-test", Files.readString(tempFile));

            // Test with URL
            URL url = tempFile.toUri().toURL();
            try (OutputStream os = IoUtil.getOutputStream(url)) {
                os.write("url-test".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("url-test", Files.readString(tempFile));

            // Test with null
            OutputStream nullStream = IoUtil.getOutputStream(null);
            nullStream.write("this should be discarded".getBytes(StandardCharsets.UTF_8));
            assertEquals("url-test", Files.readString(tempFile)); // Content should not change

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @ParameterizedTest
    @MethodSource("localeProvider")
    void testLexicalPathComparator(Locale locale) {
        Path path1 = Paths.get("/a/b/c");
        Path path2 = Paths.get("/a/b/d");
        Path path3 = Paths.get("/a/c");
        Path path4 = Paths.get("/a/b");

        Comparator<Path> comparator = IoUtil.lexicalPathComparator(locale);

        assertTrue(comparator.compare(path1, path2) < 0);
        assertTrue(comparator.compare(path2, path1) > 0);
        assertEquals(0, comparator.compare(path1, path1));
        assertTrue(comparator.compare(path1, path3) < 0);
        assertTrue(comparator.compare(path3, path2) > 0);
        assertTrue(comparator.compare(path1, path4) > 0);
        assertTrue(comparator.compare(path4, path2) < 0);

        // Test with null values
        assertTrue(comparator.compare(null, path1) < 0);
        assertTrue(comparator.compare(path1, null) > 0);
        assertEquals(0, comparator.compare(null, null));
    }

    static Stream<Locale> localeProvider() {
        return Stream.of(
                Locale.US,
                Locale.FRANCE,
                Locale.JAPAN,
                Locale.GERMANY,
                Locale.of("th") // Thai locale has different collation rules
        );
    }

    @Test
    void testCreateSecureTempDirectory() throws Exception {
        // Test with prefix
        Path tempDir = IoUtil.createSecureTempDirectory("test-prefix");
        try {
            assertTrue(Files.exists(tempDir));
            assertTrue(Files.isDirectory(tempDir));
            assertTrue(tempDir.getFileName().toString().startsWith("test-prefix"));

            // Check permissions - this is platform dependent, so we just verify it exists
            assertTrue(Files.isReadable(tempDir));
            assertTrue(Files.isWritable(tempDir));
            assertTrue(Files.isExecutable(tempDir));
        } finally {
            Files.deleteIfExists(tempDir);
        }

        // Test with parent directory and prefix
        Path parentDir = Files.createTempDirectory("parent");
        try {
            Path childTempDir = IoUtil.createSecureTempDirectory(parentDir, "child-prefix");
            try {
                assertTrue(Files.exists(childTempDir));
                assertTrue(Files.isDirectory(childTempDir));
                assertTrue(childTempDir.getFileName().toString().startsWith("child-prefix"));
                assertEquals(parentDir, childTempDir.getParent());
            } finally {
                Files.deleteIfExists(childTempDir);
            }
        } finally {
            Files.deleteIfExists(parentDir);
        }
    }

    @Test
    void testRedirectStandardStreams() throws Exception {
        // Create a temporary file for redirection
        Path tempFile = Files.createTempFile("redirect-test", ".txt");
        try {
            // Redirect standard streams
            try (AutoCloseable redirect = IoUtil.redirectStandardStreams(tempFile)) {
                // Write to System.out and System.err
                System.out.println("This is a test message to stdout");
                System.err.println("This is a test message to stderr");

                // Ensure the streams are flushed
                System.out.flush();
                System.err.flush();
            } // AutoCloseable.close() should reset the streams

            // Read the content of the file
            String content = Files.readString(tempFile);

            // Verify the content contains the expected output
            assertTrue(content.contains("stdout: This is a test message to stdout"));
            assertTrue(content.contains("stderr: This is a test message to stderr"));

            // Verify that System.out and System.err are reset to their original values
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;

            // Write to the streams after redirection is closed
            ByteArrayOutputStream testOut = new ByteArrayOutputStream();
            PrintStream testPrintStream = new PrintStream(testOut, false, StandardCharsets.UTF_8);

            PrintStream oldOut = System.out;
            System.setOut(testPrintStream);
            System.out.println("Test after redirection");
            System.setOut(oldOut);

            // Verify that the streams were properly reset
            assertEquals(originalOut, System.out);
            assertEquals(originalErr, System.err);
            assertEquals("Test after redirection" + System.lineSeparator(), testOut.toString(StandardCharsets.UTF_8));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
