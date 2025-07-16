package com.dua3.utility.options;

import com.dua3.utility.data.ConversionException;
import com.dua3.utility.data.Converter;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Param class.
 */
class ParamTest {

    /**
     * The filesystem configurations to use in tests.
     * @return stream of file system configurations
     */
    public static Stream<Configuration> fileSystemConfigurations() {
        return Stream.of(Configuration.unix(), Configuration.windows(), Configuration.osX());
    }

    /**
     * Normalize a path for testing, i.e., makes sure that absolute windows paths are
     * prefixed with a drive letter.
     * @param configuration the {@link Configuration} according to which the path should be normalized
     * @param pathStr the path as a String
     * @return the normalized path
     */
    private static String normalize(Configuration configuration, String pathStr) {
        if (configuration.equals(Configuration.windows())) {
            if (pathStr.startsWith("/")) {
                pathStr = "C:" + pathStr;
            }
        }
        return pathStr;
    }

    /**
     * Get a path for testing, i.e., makes sure that absolute windows paths are
     * prefixed with a drive letter.
     * @param configuration the {@link Configuration} according to which the path should be normalized
     * @param fs the file system
     * @param pathStr the path as a String
     * @return the normalized path
     */
    private static Path getPath(Configuration configuration, FileSystem fs, String pathStr) {
        return fs.getPath(normalize(configuration, pathStr));
    }

    @Test
    void testOfString() {
        Param<String> param = Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED);

        assertEquals("String Param", param.displayName());
        assertEquals("A string parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(String.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());
    }

    @Test
    void testOfStringWithPredicate() {
        Predicate<String> predicate = s -> s.length() > 3;
        Param<String> param = Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED, predicate);

        assertEquals("String Param", param.displayName());
        assertEquals("A string parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(String.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test validation
        Optional<String> validResult = param.validate().apply("valid");
        assertTrue(validResult.isEmpty());

        Optional<String> invalidResult = param.validate().apply("ab");
        assertTrue(invalidResult.isPresent());
        assertTrue(invalidResult.get().contains("invalid value"));
    }

    @Test
    void testOfStringWithRegex() {
        Param<String> param = Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED, "[a-z]+");

        assertEquals("String Param", param.displayName());
        assertEquals("A string parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(String.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test validation
        Optional<String> validResult = param.validate().apply("valid");
        assertTrue(validResult.isEmpty());

        Optional<String> invalidResult = param.validate().apply("INVALID");
        assertTrue(invalidResult.isPresent());
        assertTrue(invalidResult.get().contains("invalid value"));
    }

    @Test
    void testOfBoolean() {
        Param<Boolean> param = Param.ofBoolean("Boolean Param", "A boolean parameter", "arg", Param.Required.REQUIRED);

        assertEquals("Boolean Param", param.displayName());
        assertEquals("A boolean parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Boolean.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(true, param.converter().a2b().apply(new String[]{"true"}));
        assertEquals(true, param.converter().a2b().apply(new String[]{"TRUE"}));
        assertEquals(true, param.converter().a2b().apply(new String[]{"True"}));

        assertEquals(false, param.converter().a2b().apply(new String[]{"false"}));
        assertEquals(false, param.converter().a2b().apply(new String[]{"FALSE"}));
        assertEquals(false, param.converter().a2b().apply(new String[]{"False"}));

        assertEquals(false, param.converter().a2b().apply(new String[]{"1"}));
        assertEquals(false, param.converter().a2b().apply(new String[]{"0"}));
    }

    @Test
    void testOfInt() {
        Param<Integer> param = Param.ofInt("Int Param", "An integer parameter", "arg", Param.Required.REQUIRED);

        assertEquals("Int Param", param.displayName());
        assertEquals("An integer parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Integer.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(42, param.converter().a2b().apply(new String[]{"42"}));
        assertEquals(-10, param.converter().a2b().apply(new String[]{"-10"}));

        // Test invalid conversion
        assertThrows(ConversionException.class, () -> param.converter().a2b().apply(new String[]{"invalid"}));
    }

    @Test
    void testOfIntWithPredicate() {
        Predicate<Integer> predicate = i -> i > 0 && i < 100;
        Param<Integer> param = Param.ofInt("Int Param", "An integer parameter", "arg", Param.Required.REQUIRED, predicate);

        assertEquals("Int Param", param.displayName());
        assertEquals("An integer parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Integer.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test validation
        Optional<String> validResult = param.validate().apply(42);
        assertTrue(validResult.isEmpty());

        Optional<String> invalidResult = param.validate().apply(200);
        assertTrue(invalidResult.isPresent());
        assertTrue(invalidResult.get().contains("invalid value"));
    }

    @Test
    void testOfLong() {
        Param<Long> param = Param.ofLong("Long Param", "A long parameter", "arg", Param.Required.REQUIRED);

        assertEquals("Long Param", param.displayName());
        assertEquals("A long parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Long.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(42L, param.converter().a2b().apply(new String[]{"42"}));
        assertEquals(-10L, param.converter().a2b().apply(new String[]{"-10"}));

        // Test invalid conversion
        assertThrows(ConversionException.class, () -> param.converter().a2b().apply(new String[]{"invalid"}));
    }

    @Test
    void testOfDouble() {
        Param<Double> param = Param.ofDouble("Double Param", "A double parameter", "arg", Param.Required.REQUIRED);

        assertEquals("Double Param", param.displayName());
        assertEquals("A double parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Double.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(42.5, param.converter().a2b().apply(new String[]{"42.5"}));
        assertEquals(-10.25, param.converter().a2b().apply(new String[]{"-10.25"}));

        // Test invalid conversion
        assertThrows(ConversionException.class, () -> param.converter().a2b().apply(new String[]{"invalid"}));
    }

    @Test
    void testOfPath() {
        Param<Path> param = Param.ofPath("Path Param", "A path parameter", "arg", Param.Required.REQUIRED);

        assertEquals("Path Param", param.displayName());
        assertEquals("A path parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Path.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(Paths.get("/tmp/test"), param.converter().a2b().apply(new String[]{"/tmp/test"}));
    }

    @ParameterizedTest
    @MethodSource("fileSystemConfigurations")
    void testOfPathWithPredicate(Configuration configuration) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(configuration)) {
            String tmpDir = "/tmp";
            String testDir = tmpDir + "/test";
            String homeDir = "/home";
            String homeTestDir = homeDir + "/test";

            // Create a path for the tmp directory
            Path tmpPath = getPath(configuration, fs, tmpDir);

            // Create a predicate that checks if the path is in the tmp directory
            Predicate<Path> predicate = p -> {
                // Convert both paths to absolute paths to ensure consistent comparison
                Path absolutePath = p.isAbsolute() ? p : p.toAbsolutePath();
                Path absoluteTmpPath = tmpPath.isAbsolute() ? tmpPath : tmpPath.toAbsolutePath();

                // Check if the absolute path starts with the absolute tmp path
                String pathStr = absolutePath.toString();
                String tmpPathStr = absoluteTmpPath.toString();

                return pathStr.startsWith(tmpPathStr);
            };

            Param<Path> param = Param.ofPath("Path Param", "A path parameter", "arg", Param.Required.REQUIRED, predicate);

            assertEquals("Path Param", param.displayName());
            assertEquals("A path parameter", param.description());
            assertEquals("arg", param.argName());
            assertSame(Path.class, param.targetType());
            assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
            assertFalse(param.hasAllowedValues());

            // Test validation with a valid path (in tmp directory)
            Path validPath = getPath(configuration, fs, testDir);
            Optional<String> validResult = param.validate().apply(validPath);
            assertTrue(validResult.isEmpty(), "Path " + validPath + " should be valid");

            // Test validation with an invalid path (not in tmp directory)
            Path invalidPath = getPath(configuration, fs, homeTestDir);

            Optional<String> invalidResult = param.validate().apply(invalidPath);
            assertTrue(invalidResult.isPresent(), "Path " + invalidPath + " should be invalid");
            assertTrue(invalidResult.get().contains("invalid value"));
        }
    }

    @Test
    void testOfUri() {
        Param<URI> param = Param.ofUri("URI Param", "A URI parameter", "arg", Param.Required.REQUIRED);

        assertEquals("URI Param", param.displayName());
        assertEquals("A URI parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(URI.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(URI.create("https://example.com"), param.converter().a2b().apply(new String[]{"https://example.com"}));
    }

    @Test
    void testOfUriWithPredicate() {
        Predicate<URI> predicate = uri -> uri.getScheme().equals("https");
        Param<URI> param = Param.ofUri("URI Param", "A URI parameter", "arg", Param.Required.REQUIRED, predicate);

        assertEquals("URI Param", param.displayName());
        assertEquals("A URI parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(URI.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test validation
        Optional<String> validResult = param.validate().apply(URI.create("https://example.com"));
        assertTrue(validResult.isEmpty());

        Optional<String> invalidResult = param.validate().apply(URI.create("http://example.com"));
        assertTrue(invalidResult.isPresent());
        assertTrue(invalidResult.get().contains("invalid value"));
    }

    @Test
    void testOfConstants() {
        List<String> allowedValues = Arrays.asList("one", "two", "three");
        Param<String> param = Param.ofConstants("Constants Param", "A constants parameter", "arg", Param.Required.REQUIRED, String.class, allowedValues);

        assertEquals("Constants Param", param.displayName());
        assertEquals("A constants parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(String.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertTrue(param.hasAllowedValues());
        assertEquals(allowedValues, param.allowedValues());

        // Test conversion
        assertEquals("one", param.converter().a2b().apply(new String[]{"one"}));
    }

    @Test
    void testOfConstantsWithConverter() {
        List<Integer> allowedValues = Arrays.asList(1, 2, 3);
        Converter<String, Integer> converter = Converter.create(Integer::parseInt, String::valueOf);
        Param<Integer> param = Param.ofConstants("Constants Param", "A constants parameter", "arg", Param.Required.REQUIRED, Integer.class, converter, allowedValues);

        assertEquals("Constants Param", param.displayName());
        assertEquals("A constants parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Integer.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertTrue(param.hasAllowedValues());
        assertEquals(allowedValues, param.allowedValues());

        // Test conversion
        assertEquals(1, param.converter().a2b().apply(new String[]{"1"}));
    }

    @Test
    void testOfEnum() {
        Param<TestEnum> param = Param.ofEnum("Enum Param", "An enum parameter", "arg", Param.Required.REQUIRED, TestEnum.class);

        assertEquals("Enum Param", param.displayName());
        assertEquals("An enum parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(TestEnum.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertTrue(param.hasAllowedValues());
        assertEquals(Arrays.asList(TestEnum.values()), param.allowedValues());

        // Test conversion
        assertEquals(TestEnum.ONE, param.converter().a2b().apply(new String[]{"ONE"}));

        // Test invalid conversion
        assertThrows(IllegalArgumentException.class, () -> param.converter().a2b().apply(new String[]{"FOUR"}));
    }

    @Test
    void testOfObject() {
        Converter<String, Integer> converter = Converter.create(Integer::parseInt, String::valueOf);
        Param<Integer> param = Param.ofObject("Object Param", "An object parameter", "arg", Param.Required.REQUIRED, Integer.class, converter);

        assertEquals("Object Param", param.displayName());
        assertEquals("An object parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(Integer.class, param.targetType());
        assertEquals(Repetitions.EXACTLY_ONE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        assertEquals(42, param.converter().a2b().apply(new String[]{"42"}));

        // Test invalid conversion
        assertThrows(NumberFormatException.class, () -> param.converter().a2b().apply(new String[]{"invalid"}));
    }

    @Test
    void testOfStrings() {
        Param<List<String>> param = Param.ofStrings("Strings Param", "A strings parameter", "arg", Repetitions.ONE_OR_MORE);

        assertEquals("Strings Param", param.displayName());
        assertEquals("A strings parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(List.class, param.targetType());
        assertEquals(Repetitions.ONE_OR_MORE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        List<String> result = param.converter().a2b().apply(new String[]{"one", "two", "three"});
        assertEquals(3, result.size());
        assertEquals("one", result.get(0));
        assertEquals("two", result.get(1));
        assertEquals("three", result.get(2));
    }

    @Test
    void testOfList() {
        Converter<String, Integer> elementConverter = Converter.create(Integer::parseInt, String::valueOf);
        Param<List<Integer>> param = Param.ofList("List Param", "A list parameter", "arg", elementConverter, Repetitions.ONE_OR_MORE);

        assertEquals("List Param", param.displayName());
        assertEquals("A list parameter", param.description());
        assertEquals("arg", param.argName());
        assertSame(List.class, param.targetType());
        assertEquals(Repetitions.ONE_OR_MORE, param.argRepetitions());
        assertFalse(param.hasAllowedValues());

        // Test conversion
        List<Integer> result = param.converter().a2b().apply(new String[]{"1", "2", "3"});
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
    }

    @Test
    void testGetText() {
        Param<String> param = Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED);

        assertEquals("test value", param.getText("test value"));
        assertEquals("", param.getText(""));
    }

    @Test
    void testGetTextWithDelimiterAndQuotes() {
        Param<String> param = Param.ofString("String Param", "A string parameter", "arg", Param.Required.REQUIRED);

        assertEquals("'test value'", param.getText("test value", ", ", "'", "'"));
        assertEquals("'test, value'", param.getText("test, value", ", ", "'", "'"));
        assertEquals("''", param.getText("", ", ", "'", "'"));
    }

    @Test
    void testRequiredToRepetitions() {
        assertEquals(Repetitions.EXACTLY_ONE, Param.Required.REQUIRED.toRepetitions());
        assertEquals(Repetitions.ZERO_OR_ONE, Param.Required.OPTIONAL.toRepetitions());
    }
}
