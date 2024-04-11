package com.dua3.utility.options;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
public class ArgumentsParserTest {

    @Test
    public void testFlag() {
        // create the parser
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testFlag")
                .description("Unit test for passing flags on the command line.");
        Flag oPrint = builder.flag("--print", "-p").description("print result to terminal");
        ArgumentsParser cmd = builder.build();

        assertFalse(cmd.parse().isSet(oPrint));

        assertTrue(cmd.parse("-p").isSet(oPrint));
        assertTrue(cmd.parse("--print").isSet(oPrint));

        assertTrue(cmd.parse("hello", "-p").isSet(oPrint));
        assertTrue(cmd.parse("hello", "--print").isSet(oPrint));

        assertTrue(cmd.parse("-p", "hello").isSet(oPrint));
        assertTrue(cmd.parse("--print", "hello").isSet(oPrint));

        assertTrue(cmd.parse("-p", "hello", "Bob").isSet(oPrint));
        assertTrue(cmd.parse("--print", "hello", "Bob").isSet(oPrint));

        assertFalse(cmd.parse("hello", "Bob").isSet(oPrint));

        String expected = """
                
                testFlag
                --------
                                
                Unit test for passing flags on the command line.
                                
                testFlag <options> [<arg> ...]
                                
                  <options>:
                    --print|-p
                            print result to terminal
                                
                """;

        assertEquals(expected, cmd.help());

        Arguments args = cmd.parse("-p", "hello", "Bob");
        String expectedToString = """
                Arguments{
                  --print
                  "hello" "Bob"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    public void testChoiceOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testChoiceOption")
                .description("Unit test for passing choices on the command line.");
        SimpleOption<String> oProduct = builder.simpleOption(String.class, "--product", "-p")
                .description("the product")
                .displayName("product name")
                .required();
        ChoiceOption<E> oSize = builder.choiceOption(E.class, "--size").defaultValue(E.GRANDE);
        ArgumentsParser cmd = builder.build();

        assertEquals("MACCHIATO", cmd.parse("--product MACCHIATO --size VENTI".split(" ")).getOrThrow(oProduct));
        assertEquals("MACCHIATO", cmd.parse("--size VENTI --product MACCHIATO".split(" ")).getOrThrow(oProduct));

        assertEquals(Optional.of(E.VENTI), cmd.parse("--product MACCHIATO --size VENTI".split(" ")).get(oSize));
        assertEquals(E.VENTI, cmd.parse("--product MACCHIATO --size VENTI".split(" ")).getOrThrow(oSize));
        assertEquals(Optional.of(E.GRANDE), cmd.parse("--product MACCHIATO".split(" ")).get(oSize));
        assertEquals(E.GRANDE, cmd.parse("--product MACCHIATO".split(" ")).getOrThrow(oSize));

        String expected = """
                
                testChoiceOption
                ----------------
                
                Unit test for passing choices on the command line.
                
                testChoiceOption <options> [<arg> ...]
                
                  <options>:
                    --product|-p <arg>
                            the product
                
                    --size <arg>
                
                """;

        assertEquals(expected, cmd.help());

        Arguments args = cmd.parse("--product", "MACCHIATO", "--size", "VENTI");
        String expectedToString = """
                Arguments{
                  --product "MACCHIATO"
                  --size "VENTI"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    public void testChoiceOptionRequired() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testChoiceOptionRequired")
                .description("Unit test for passing choices on the command line.");
        ChoiceOption<E> oSize = builder.choiceOption(E.class, "--size")
                .argName("size")
                .required();
        SimpleOption<String> oProduct = builder.simpleOption(String.class, "--product", "-p")
                .description("set the product name")
                .displayName("product name")
                .required();
        ArgumentsParser cmd = builder.build();

        assertEquals(Optional.of(E.VENTI), cmd.parse("--product MACCHIATO --size VENTI".split(" ")).get(oSize));
        assertEquals(E.TALL, cmd.parse("--size TALL --product MACCHIATO".split(" ")).getOrThrow(oSize));
        assertThrows(OptionException.class, () -> cmd.parse("--product MACCHIATO".split(" ")).get(oProduct));
        assertThrows(OptionException.class, () -> cmd.parse("--size TALL".split(" ")));

        String expected = """
                
                testChoiceOptionRequired
                ------------------------
                
                Unit test for passing choices on the command line.
                
                testChoiceOptionRequired <options> [<arg> ...]
                
                  <options>:
                    --product|-p <arg>
                            set the product name
                
                    --size <size>
                
                """;

        assertEquals(expected, cmd.help());
    }

    @Test
    public void testSimpleOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testSimpleOption")
                .description("Unit test for passing simple options on the command line.");
        SimpleOption<String> optionName = builder.simpleOption(String.class, "--name", "-n")
                .description("set name")
                .argName("name");
        SimpleOption<Integer> optionAge = builder.simpleOption(Integer.class, "--age", "-a")
                .argName("age");
        ArgumentsParser cmd = builder.build();

        assertFalse(cmd.parse().get(optionName).isPresent());
        assertFalse(cmd.parse().get(optionAge).isPresent());

        assertEquals("Eve", cmd.parse("-n", "Eve").getOrThrow(optionName));
        assertEquals(30, cmd.parse("--age", "30").getOrThrow(optionAge));

        Arguments eve30 = cmd.parse("-n", "Eve", "--age", "30");
        assertEquals("Eve", eve30.getOrThrow(optionName));
        assertEquals(30, eve30.getOrThrow(optionAge));

        String expected = """
                
                testSimpleOption
                ----------------
                
                Unit test for passing simple options on the command line.
                
                testSimpleOption <options> [<arg> ...]
                
                  <options>:
                    --age|-a <age>
                
                    --name|-n <name>
                            set name
                
                """;
        assertEquals(expected, cmd.help());

        Arguments args = cmd.parse("-n", "Eve", "--age", "30");
        String expectedToString = """
                Arguments{
                  --name "Eve"
                  --age "30"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    public void testSimpleOptionRequired() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testSimpleOptionRequired")
                .description("Unit test for passing simple options on the command line.");
        SimpleOption<String> optionName = builder.simpleOption(String.class, "--name", "-n")
                .description("set name")
                .argName("name")
                .required();
        SimpleOption<Integer> optionAge = builder.simpleOption(Integer.class, "--age", "-a")
                .argName("age");
        ArgumentsParser cmd = builder.build();

        assertThrows(OptionException.class, cmd::parse);

        assertEquals("Eve", cmd.parse("-n", "Eve").getOrThrow(optionName));
        assertThrows(OptionException.class, () -> cmd.parse("--age", "30").getOrThrow(optionAge));

        assertThrows(OptionException.class, () -> cmd.parse("-n", "Eve", "--name", "Bob"));

        Arguments eve30 = cmd.parse("-n", "Eve", "--age", "30");
        assertEquals("Eve", eve30.getOrThrow(optionName));
        assertEquals(30, eve30.getOrThrow(optionAge));

        String expected = """
                
                testSimpleOptionRequired
                ------------------------
                
                Unit test for passing simple options on the command line.
                
                testSimpleOptionRequired <options> [<arg> ...]
                
                  <options>:
                    --age|-a <age>
                
                    --name|-n <name>
                            set name
                
                """;
        assertEquals(expected, cmd.help());
    }

    @Test
    public void testPositionalArgs1() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testPositionalArgs1")
                .description("Unit test for passing positional arguments on the command line.");
        ArgumentsParser cmd = builder.build();

        String expected = """
                
                testPositionalArgs1
                -------------------
                
                Unit test for passing positional arguments on the command line.
                
                testPositionalArgs1 [<arg> ...]
                
                """;
        assertEquals(expected, cmd.help());

        Arguments args1 = cmd.parse();
        assertTrue(args1.positionalArgs().isEmpty());

        Arguments args2 = cmd.parse("abc", "def");
        assertEquals(List.of("abc", "def"), args2.positionalArgs());
    }

    @Test
    public void testPositionalArgs2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testPositionalArgs2")
                .description("Unit test for passing positional arguments on the command line.")
                .positionalArgs(1, 3);
        ArgumentsParser cmd = builder.build();

        String expected = """
                
                testPositionalArgs2
                -------------------
                
                Unit test for passing positional arguments on the command line.
                
                testPositionalArgs2 <arg1> [... <arg3>]
                
                """;
        assertEquals(expected, cmd.help());

        // min arity is 1!
        assertThrows(OptionException.class, cmd::parse);

        // max arity is 3!
        assertThrows(OptionException.class, () -> cmd.parse("abc", "def", "ghi", "jkl"));

        // these are all valid
        assertEquals(List.of("abc"), cmd.parse("abc").positionalArgs());
        assertEquals(List.of("abc", "def"), cmd.parse("abc", "def").positionalArgs());
        assertEquals(List.of("abc", "def", "ghi"), cmd.parse("abc", "def", "ghi").positionalArgs());
    }

    @Test
    public void testOptionHandler() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOptionHandler")
                .description("Unit test for option handling.");
        List<String> yeaSayer = new ArrayList<>();
        builder.option(String.class, "-y").arity(1).handler(yeaSayer::addAll);
        List<String> naySayer = new ArrayList<>();
        builder.option(String.class, "-n").arity(1).handler(naySayer::addAll);
        ArgumentsParser cmd = builder.build();

        cmd.parse("-y a -n b -n c -n d -y e -n f".split(" ")).handle();

        assertEquals(List.of("a", "e"), yeaSayer);
        assertEquals(List.of("b", "c", "d", "f"), naySayer);
    }

    @Test
    public void testStandardOptionOccurrences() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testSimpleOption")
                .description("Unit test for passing simple options on the command line.");
        Option<String> optionExactlyTwice = builder.option(String.class, "--exactly-twice").occurrence(2);
        Option<String> optionAtMostTwice = builder.option(String.class, "--at-most-twice").occurrence(0, 2);
        Option<String> optionTwoOrThreeTimes = builder.option(String.class, "--two-or-three-times").occurrence(2, 3);
        Option<String> optionAtLeastTwice = builder.option(String.class, "--at-least-twice").occurrence(2, Integer.MAX_VALUE);
        ArgumentsParser cmd = builder.build();

        // test occurrences
        Arguments e1 = cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice"
        );
        assertEquals(2, e1.stream(optionExactlyTwice).count());
        assertEquals(0, e1.stream(optionAtMostTwice).count());
        assertEquals(2, e1.stream(optionTwoOrThreeTimes).count());
        assertEquals(2, e1.stream(optionAtLeastTwice).count());

        Arguments e2 = cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        );
        assertEquals(2, e2.stream(optionExactlyTwice).count());
        assertEquals(2, e2.stream(optionAtMostTwice).count());
        assertEquals(3, e2.stream(optionTwoOrThreeTimes).count());
        assertEquals(3, e2.stream(optionAtLeastTwice).count());

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        ));

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        ));

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        ));

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        ));

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice",
                "--at-least-twice",
                "--at-least-twice"
        ));

        assertThrows(OptionException.class, () -> cmd.parse(
                "--exactly-twice",
                "--exactly-twice",
                "--at-most-twice",
                "--at-most-twice",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--two-or-three-times",
                "--at-least-twice"
        ));
    }

    @Test
    public void testStandardOptionArity() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testStandardOptionArity")
                .description("Unit test for testing opzion arity on the command line.")
                .positionalArgs(0, 0);
        Option<String> optionAtMostTwoArgs = builder.option(String.class, "--at-most-two-args").arity(0, 2);
        Option<String> optionExactlyTwoArgs = builder.option(String.class, "--exactly-two-args").arity(2);
        Option<String> optionTwoOrMoreArgs = builder.option(String.class, "--two-or-more-args").arity(2, Integer.MAX_VALUE);
        ArgumentsParser cmd = builder.build();

        assertEquals(List.of(Collections.emptyList()), cmd.parse("--at-most-two-args").stream(optionAtMostTwoArgs).toList());
        assertEquals(List.of(List.of("A")), cmd.parse("--at-most-two-args", "A").stream(optionAtMostTwoArgs).toList());
        assertEquals(List.of(List.of("A", "B")), cmd.parse("--at-most-two-args", "A", "B").stream(optionAtMostTwoArgs).toList());
        assertThrows(OptionException.class, () -> cmd.parse("--at-most-two-args", "A", "B", "C"));

        assertThrows(OptionException.class, () -> cmd.parse("--exactly-two-args"));
        assertThrows(OptionException.class, () -> cmd.parse("--exactly-two-args", "A"));
        assertEquals(List.of(List.of("A", "B")), cmd.parse("--exactly-two-args", "A", "B").stream(optionExactlyTwoArgs).toList());
        assertThrows(OptionException.class, () -> cmd.parse("--exactly-two-args", "A", "B", "C"));

        assertThrows(OptionException.class, () -> cmd.parse("--two-or-more-args"));
        assertThrows(OptionException.class, () -> cmd.parse("--two-or-more-args", "A"));
        assertEquals(List.of(List.of("A", "B")), cmd.parse("--two-or-more-args", "A", "B").stream(optionTwoOrMoreArgs).toList());
        assertEquals(List.of(List.of("A", "B", "C")), cmd.parse("--two-or-more-args", "A", "B", "C").stream(optionTwoOrMoreArgs).toList());
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum E {
        TALL, GRANDE, VENTI, TRENTA
    }
}
