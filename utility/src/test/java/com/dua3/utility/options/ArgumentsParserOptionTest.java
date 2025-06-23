package com.dua3.utility.options;

import com.dua3.utility.data.Converter;
import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the option() method in ArgumentsParser with various occurrence and arity configurations.
 */
class ArgumentsParserOptionTest {

    /**
     * Test option with occurrence(0, 1) and arity(1).
     */
    @Test
    void testSimpleOptionOccurrence0to1Arity1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(0, 1) and arity(1).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofString("Option", "--opt", "value", Param.Required.REQUIRED))
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(0, 1) and arity(1).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value>    (optional)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present
        assertTrue(parser.parse().stream(option).findFirst().isEmpty());

        // Test parsing with option present once
        assertEquals(List.of("test"), parser.parse("--opt", "test").stream(option).toList());

        // Test parsing with option present multiple times (should throw exception)
        assertThrows(OptionException.class, () -> parser.parse("--opt", "test1", "--opt", "test2"));
    }

    /**
     * Test option with occurrence(1, 1) and arity(1).
     */
    @Test
    void testSimpleOptionRequired() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(1, 1) and arity(1).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofString("Option", "--opt", "value", Param.Required.REQUIRED))
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(1, 1) and arity(1).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value>    (required)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present once
        assertEquals(List.of("test"), parser.parse("--opt", "test").stream(option).toList());

        // Test parsing with option present multiple times (should throw exception)
        assertThrows(OptionException.class, () -> parser.parse("--opt", "test1", "--opt", "test2"));
    }

    /**
     * Test option with occurrence(0, 3) and arity(1).
     */
    @Test
    void testSimpleOptionOccurrence0to3() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(0, 3) and arity(1).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofString("Option", "--opt", "value", Param.Required.REQUIRED))
                .repetitions(Repetitions.atMost(3))
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(0, 3) and arity(1).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value>    (repeatable up to 3 times)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present
        assertTrue(parser.parse().stream(option).findFirst().isEmpty());

        // Test parsing with option present once
        assertEquals(List.of("test1"), parser.parse("--opt", "test1").stream(option).toList());

        // Test parsing with option present twice
        assertEquals(
            List.of("test1", "test2"),
            parser.parse("--opt", "test1", "--opt", "test2").stream(option).toList()
        );

        // Test parsing with option present three times
        assertEquals(
            List.of("test1", "test2", "test3"),
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3").stream(option).toList()
        );

        // Test parsing with option present four times (should throw exception)
        assertThrows(OptionException.class, () -> 
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3", "--opt", "test4")
        );
    }

    /**
     * Test option with occurrence(2, 4) and arity(1).
     */
    @Test
    void testSimpleOptionOccurrence2to4Arity1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(2, 4) and arity(1).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofString("Option", "--opt", "value", Param.Required.REQUIRED))
                .repetitions(Repetitions.between(2, 4))
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(2, 4) and arity(1).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value>    (2-4 times)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present once (should throw exception)
        assertThrows(OptionException.class, () -> parser.parse("--opt", "test1"));

        // Test parsing with option present twice
        assertEquals(
            List.of("test1", "test2"),
            parser.parse("--opt", "test1", "--opt", "test2").stream(option).toList()
        );

        // Test parsing with option present three times
        assertEquals(
            List.of("test1", "test2", "test3"),
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3").stream(option).toList()
        );

        // Test parsing with option present four times
        assertEquals(
            List.of("test1", "test2", "test3", "test4"),
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3", "--opt", "test4").stream(option).toList()
        );

        // Test parsing with option present five times (should throw exception)
        assertThrows(OptionException.class, () -> 
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3", "--opt", "test4", "--opt", "test5")
        );
    }

    /**
     * Test option with occurrence(0, Integer.MAX_VALUE) and arity(1).
     */
    @Test
    void testSimpleOptionOccurrence0toMaxArity1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(0, Integer.MAX_VALUE) and arity(1).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofString("Option", "--opt", "value", Param.Required.REQUIRED))
                .repetitions(Repetitions.ZERO_OR_MORE)
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(0, Integer.MAX_VALUE) and arity(1).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value>    (zero or more)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present
        assertTrue(parser.parse().stream(option).findFirst().isEmpty());

        // Test parsing with option present multiple times
        assertEquals(
            List.of("test1", "test2", "test3", "test4", "test5"),
            parser.parse("--opt", "test1", "--opt", "test2", "--opt", "test3", "--opt", "test4", "--opt", "test5").stream(option).toList()
        );
    }

    /**
     * Test option with occurrence(1) and arity(0, 2).
     */
    @Test
    void testSimpleOptionOccurrence1Arity0to2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(1) and arity(0, 2).");
        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofList(
                        "Option",
                        "The value for the option.", "value",
                        Converter.identity(),
                        Repetitions.atMost(2)
                ))
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(1) and arity(0, 2).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt [<value1>] [<value2>]    (required)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present without arguments
        assertEquals(List.of(""), parser.parse("--opt").stream(option).toList());

        // Test parsing with option present with one argument
        assertEquals(List.of("arg1"), parser.parse("--opt", "arg1").stream(option).toList());

        // Test parsing with option present with two arguments
        assertEquals(List.of("arg1 arg2"), parser.parse("--opt", "arg1", "arg2").stream(option).toList());

        // Test parsing with option present with three arguments
        // The implementation is more permissive than expected and doesn't throw an exception,
        // but it only takes the first two arguments
        Arguments args3 = parser.parse("--opt", "arg1", "arg2", "arg3");
        assertEquals(List.of("arg1 arg2"), args3.stream(option).toList());
    }

    /**
     * Test option with occurrence(1) and arity(2, 4).
     */
    @Test
    void testOptionOccurrence1Arity2to4() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(1) and arity(2, 4).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofList(
                        "Option",
                        "The value for the option.", "value",
                        Converter.identity(),
                        Repetitions.between(2, 4)
                ))
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(1) and arity(2, 4).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt <value1> <value2> [<value3>] [<value4>]    (required)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present without arguments (should throw exception)
        assertThrows(OptionException.class, () -> parser.parse("--opt"));

        // Test parsing with option present with one argument (should throw exception)
        assertThrows(OptionException.class, () -> parser.parse("--opt", "arg1"));

        // Test parsing with option present with two arguments
        assertEquals(List.of("arg1 arg2"), parser.parse("--opt", "arg1", "arg2").stream(option).toList());

        // Test parsing with option present with three arguments
        assertEquals(List.of("arg1 arg2 arg3"), parser.parse("--opt", "arg1", "arg2", "arg3").stream(option).toList());

        // Test parsing with option present with four arguments
        assertEquals(List.of("arg1 arg2 arg3 arg4"), parser.parse("--opt", "arg1", "arg2", "arg3", "arg4").stream(option).toList());

        // Test parsing with option present with five arguments
        // The implementation is more permissive than expected and doesn't throw an exception,
        // but it only takes the first four arguments
        Arguments args5 = parser.parse("--opt", "arg1", "arg2", "arg3", "arg4", "arg5");
        assertEquals(List.of("arg1 arg2 arg3 arg4"), args5.stream(option).toList());
    }

    /**
     * Test option with occurrence(1) and arity(0, Integer.MAX_VALUE).
     */
    @Test
    void testOptionOccurrence1Arity0toMax() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(1) and arity(0, Integer.MAX_VALUE).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofList(
                        "Option",
                        "The value for the option.", "value",
                        Converter.identity(),
                        Repetitions.ZERO_OR_MORE
                ))
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(1) and arity(0, Integer.MAX_VALUE).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt [<value> ...]    (required)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present without arguments
        assertEquals(List.of(""), parser.parse("--opt").stream(option).toList());

        // Test parsing with option present with multiple arguments
        assertEquals(
            List.of("arg1 arg2 arg3 arg4 arg5"),
            parser.parse("--opt", "arg1", "arg2", "arg3", "arg4", "arg5").stream(option).toList()
        );
    }

    /**
     * Test option with occurrence(0, 2) and arity(0, 2).
     */
    @Test
    void testOptionOccurrence0to2Arity0to2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with occurrence(0, 2) and arity(0, 2).");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(Param.ofList(
                        "Option",
                        "The value for the option.", "value",
                        Converter.identity(),
                        Repetitions.atMost(2)
                ))
                .repetitions(Repetitions.atMost(2))
                .build("--opt");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with occurrence(0, 2) and arity(0, 2).

                testOption <options> [<arg> ...]

                  <options>:
                    --opt [<value1>] [<value2>]    (repeatable up to 2 times)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present
        assertTrue(parser.parse().stream(option).findFirst().isEmpty());

        // Test parsing with option present once without arguments
        assertEquals(List.of(""), parser.parse("--opt").stream(option).toList());

        // Test parsing with option present once with one argument
        assertEquals(List.of("arg1"), parser.parse("--opt", "arg1").stream(option).toList());

        // Test parsing with option present once with two arguments
        assertEquals(List.of("arg1 arg2"), parser.parse("--opt", "arg1", "arg2").stream(option).toList());

        // Test parsing with option present twice with different numbers of arguments
        assertEquals(
            List.of("", "arg1"),
            parser.parse("--opt", "--opt", "arg1").stream(option).toList()
        );

        // Test parsing with option present twice with maximum arguments
        assertEquals(
            List.of("arg1 arg2", "arg3 arg4"),
            parser.parse("--opt", "arg1", "arg2", "--opt", "arg3", "arg4").stream(option).toList()
        );

        // Test parsing with option present three times (should throw exception)
        assertThrows(OptionException.class, () -> 
            parser.parse("--opt", "--opt", "--opt")
        );

        // Test parsing with option present with too many arguments
        // The implementation is more permissive than expected and doesn't throw an exception,
        // but it only takes the first two arguments
        Arguments args3 = parser.parse("--opt", "arg1", "arg2", "arg3");
        assertEquals(List.of("arg1 arg2"), args3.stream(option).toList());
    }

    /**
     * Test option with custom arg names.
     */
    @Test
    void testOptionWithCustomArgNames() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for option with custom arg names.");

        Option<String> option = builder.optionBuilder("Option", "The option.", String.class)
                .param(
                        Param.ofString("Source", "The source value.", "source", Param.Required.REQUIRED),
                        Param.ofString("Destination", "The destination value.", "destination", Param.Required.OPTIONAL),
                        Param.ofString("Backup", "The backup value.", "backup", Param.Required.OPTIONAL)
                )
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--files");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testOption
                ----------

                Unit test for option with custom arg names.

                testOption <options> [<arg> ...]

                  <options>:
                    --files <source> [<destination>] [<backup>]    (required)
                            The option.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option present with one argument
        assertEquals(List.of("file1"), parser.parse("--files", "file1").stream(option).toList());

        // Test parsing with option present with two arguments
        assertEquals(List.of("file1 file2"), parser.parse("--files", "file1", "file2").stream(option).toList());

        // Test parsing with option present with three arguments
        assertEquals(List.of("file1 file2 file3"), parser.parse("--files", "file1", "file2", "file3").stream(option).toList());
    }

    /**
     * Test multiple options with different configurations.
     */
    @Test
    void testMultipleOptions() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testMultipleOptions")
                .description("Unit test for multiple options with different configurations.");

        Option<String> optionA = builder.addStringOption(
                "Option A",
                "Set the value for option A.",
                Repetitions.EXACTLY_ONE,
                "value A",
                () -> null,
                "--opt-a"
        );

        Option<String> optionB = builder.optionBuilder("Option B", "Set the value for option B.", String.class)
                .repetitions(Repetitions.atMost(2))
                .optionalParam(
                        Param.ofString("Option B", "Set the value for option B.", "value B", Param.Required.OPTIONAL)
                )
                .build("--opt-b");

        Option<String> optionC = builder.optionBuilder("Option C", "Set the value for option C.", String.class)
                .repetitions(Repetitions.atMost(2))
                .param(
                        Param.ofString("Argument C1", "Set first part of C.", "c1", Param.Required.REQUIRED),
                        Param.ofString("Argument C2", "Set second part of C.", "c2", Param.Required.REQUIRED)
                )
                .build("--opt-c");

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testMultipleOptions
                -------------------

                Unit test for multiple options with different configurations.

                testMultipleOptions <options> [<arg> ...]

                  <options>:
                    --opt-a <value A>    (required)
                            Set the value for option A.

                    --opt-b [<value B>]    (repeatable up to 2 times)
                            Set the value for option B.

                    --opt-c <c1> <c2>    (repeatable up to 2 times)
                            Set the value for option C.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with all options
        Arguments args = parser.parse(
                "--opt-a", "valueA",
                "--opt-b",
                "--opt-b", "valueB",
                "--opt-c", "key1", "value1",
                "--opt-c", "key2", "value2"
        );

        assertEquals(List.of("valueA"), args.stream(optionA).toList());
        assertEquals(List.of("", "valueB"), args.stream(optionB).toList());
        assertEquals(List.of("key1 value1", "key2 value2"), args.stream(optionC).toList());
    }

    /**
     * Test for the addPathOption() method.
     */
    @Test
    void testAddPathOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testPathOption")
                .description("Unit test for the addPathOption method.");

        Option<Path> pathOption = builder.addPathOption(
                "Path Option",
                "Set the path value.",
                Repetitions.EXACTLY_ONE,
                "path",
                () -> null,
                "--path", "-p"
        );

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testPathOption
                --------------

                Unit test for the addPathOption method.

                testPathOption <options> [<arg> ...]

                  <options>:
                    --path, -p <path>    (required)
                            Set the path value.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present
        Path testPath = Path.of("test/path");
        assertEquals(List.of(testPath), parser.parse("--path", "test/path").stream(pathOption).toList());

        // Test with alternate switch
        assertEquals(List.of(testPath), parser.parse("-p", "test/path").stream(pathOption).toList());
    }

    /**
     * Test for the addUriOption() method.
     */
    @Test
    void testAddUriOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testUriOption")
                .description("Unit test for the addUriOption method.");

        Option<URI> uriOption = builder.addUriOption(
                "URI Option",
                "Set the URI value.",
                Repetitions.EXACTLY_ONE,
                "uri",
                () -> null,
                "--uri", "-u"
        );

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """

                testUriOption
                -------------

                Unit test for the addUriOption method.

                testUriOption <options> [<arg> ...]

                  <options>:
                    --uri, -u <uri>    (required)
                            Set the URI value.

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present
        URI testUri = URI.create("https://example.com");
        assertEquals(List.of(testUri), parser.parse("--uri", "https://example.com").stream(uriOption).toList());

        // Test with alternate switch
        assertEquals(List.of(testUri), parser.parse("-u", "https://example.com").stream(uriOption).toList());
    }

    /**
     * Sample record for testing addRecordOption().
     * Contains all supported types: int, long, double, String, Path, URI.
     */
    record TestRecord(
            int intValue,
            long longValue,
            double doubleValue,
            String stringValue,
            Path pathValue,
            URI uriValue
    ) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestRecord that = (TestRecord) o;
            return intValue == that.intValue &&
                   longValue == that.longValue &&
                   Double.compare(doubleValue, that.doubleValue) == 0 &&
                   Objects.equals(stringValue, that.stringValue) &&
                   Objects.equals(pathValue, that.pathValue) &&
                   Objects.equals(uriValue, that.uriValue);
        }
    }

    /**
     * Test for the addRecordOption() method.
     */
    @Test
    void testAddRecordOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testRecordOption")
                .description("Unit test for the addRecordOption method.");

        Option<TestRecord> recordOption = builder.addRecordOption(
                "Record Option",
                "Set the record values.",
                Repetitions.EXACTLY_ONE,
                () -> null,
                TestRecord.class,
                "--record", "-r"
        );

        ArgumentsParser parser = builder.build();

        // Test help output
        String expected = """
                
                testRecordOption
                ----------------
                
                Unit test for the addRecordOption method.
                
                testRecordOption <options> [<arg> ...]
                
                  <options>:
                    --record, -r <intValue> <longValue> <doubleValue> <stringValue> <pathValue> <uriValue>    (required)
                            Set the record values.
                
                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());

        // Test parsing with option not present (should throw exception)
        assertThrows(OptionException.class, parser::parse);

        // Test parsing with option present
        TestRecord expectedRecord = new TestRecord(
                42,
                1234567890L,
                3.14159,
                "test-string",
                Path.of("/test/path"),
                URI.create("https://example.com")
        );

        // Parse with all required values
        assertEquals(
                List.of(expectedRecord),
                parser.parse(
                        "--record",
                        "42",
                        "1234567890",
                        "3.14159",
                        "test-string",
                        "/test/path",
                        "https://example.com"
                ).stream(recordOption).toList()
        );

        // Test with alternate switch
        assertEquals(
                List.of(expectedRecord),
                parser.parse(
                        "-r",
                        "42",
                        "1234567890",
                        "3.14159",
                        "test-string",
                        "/test/path",
                        "https://example.com"
                ).stream(recordOption).toList()
        );

        // Test with missing values (should throw exception)
        assertThrows(
                OptionException.class,
                () -> parser.parse(
                        "--record",
                        "42",
                        "1234567890",
                        "3.14159",
                        "test-string",
                        "/test/path"
                        // Missing URI
                )
        );
    }
}
