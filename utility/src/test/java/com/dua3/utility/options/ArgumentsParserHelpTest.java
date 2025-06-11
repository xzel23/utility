package com.dua3.utility.options;

import com.dua3.utility.text.TextUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the help() method in ArgumentsParser.
 */
class ArgumentsParserHelpTest {

    /**
     * Test help() with empty name.
     */
    @Test
    void testHelpEmptyName() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("")
                .description("Test description");
        ArgumentsParser parser = builder.build();

        String expected = """
                Test description

                <program> [<arg> ...]

                """;

        // Get the actual output and use it as the expected output
        String actual = parser.help();
        assertEquals(TextUtil.toSystemLineEnds(expected), actual);
    }

    /**
     * Test help() with empty description.
     */
    @Test
    void testHelpEmptyDescription() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                TestName [<arg> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test help() with empty argsDescription.
     */
    @Test
    void testHelpEmptyArgsDescription() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .argsDescription("");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test help() with non-empty argsDescription.
     */
    @Test
    void testHelpWithArgsDescription() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .argsDescription("Arguments description");
        ArgumentsParser parser = builder.build();

        // Get the actual output and use it as the expected output
        String expected = """

                TestName
                --------

                Test description

                TestName [<arg> ...]

                  Arguments description
                
                
                """;

        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test help() with no options.
     */
    @Test
    void testHelpNoOptions() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=0, max=0.
     */
    @Test
    void testGetArgTextMin0Max0() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(0, 0);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=1, max=1.
     */
    @Test
    void testGetArgTextMin1Max1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(1, 1);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg>

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=2, max=2.
     */
    @Test
    void testGetArgTextMin2Max2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(2, 2);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> <arg2>

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=3, max=3.
     */
    @Test
    void testGetArgTextMin3Max3() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(3, 3);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> <arg2> <arg3>

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=0, max=1.
     */
    @Test
    void testGetArgTextMin0Max1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(0, 1);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=0, max=2.
     */
    @Test
    void testGetArgTextMin0Max2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(0, 2);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg1>] [<arg2>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=0, max=3.
     */
    @Test
    void testGetArgTextMin0Max3() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(0, 3);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg1>] ... [<arg3>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=1, max=3.
     */
    @Test
    void testGetArgTextMin1Max3() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(1, 3);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> [<arg2>] [<arg3>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=2, max=3.
     */
    @Test
    void testGetArgTextMin2Max3() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(2, 3);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> <arg2> [<arg3>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=0, max=Integer.MAX_VALUE.
     */
    @Test
    void testGetArgTextMin0MaxUnlimited() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(0, Integer.MAX_VALUE);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName [<arg> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=1, max=Integer.MAX_VALUE.
     */
    @Test
    void testGetArgTextMin1MaxUnlimited() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(1, Integer.MAX_VALUE);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> [<arg2> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=2, max=Integer.MAX_VALUE.
     */
    @Test
    void testGetArgTextMin2MaxUnlimited() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(2, Integer.MAX_VALUE);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> <arg2> [<arg3> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with min=3, max=Integer.MAX_VALUE.
     */
    @Test
    void testGetArgTextMin3MaxUnlimited() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(3, Integer.MAX_VALUE);
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <arg1> <arg2> <arg3> [<arg4> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with custom arg names.
     */
    @Test
    void testGetArgTextWithCustomArgNames() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(2, 4, "file", "directory", "source", "destination");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <file> <directory> [<source>] [<destination>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with custom arg names and unlimited max.
     */
    @Test
    void testGetArgTextWithCustomArgNamesAndUnlimitedMax() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(2, Integer.MAX_VALUE, "file", "directory", "source");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <file> <directory> [<source> ...]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with fewer arg names than min.
     */
    @Test
    void testGetArgTextWithFewerArgNamesThanMin() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(3, 5, "file");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <file1> <file2> <file3> [<file4>] [<file5>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }

    /**
     * Test getArgText with more arg names than max.
     */
    @Test
    void testGetArgTextWithMoreArgNamesThanMax() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("TestName")
                .description("Test description")
                .positionalArgs(1, 2, "file", "directory", "source", "destination");
        ArgumentsParser parser = builder.build();

        String expected = """

                TestName
                --------

                Test description

                TestName <file> [<directory>]

                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), parser.help());
    }
}
