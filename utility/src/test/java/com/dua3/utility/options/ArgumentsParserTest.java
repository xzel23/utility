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
        ArgumentsParser cmd = new ArgumentsParser("testFlag", "Unit test for passing flags on the command line.");
        Flag oPrint = cmd.flag("--print", "-p").description("print result to terminal");

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
                                          
                testFlag <options> [arg1] ...
                                          
                    --print
                    -p
                            print result to terminal
                                          
                """;

        assertEquals(expected, cmd.help());
    }

    @Test
    public void testChoiceOption() {
        ArgumentsParser cmd = new ArgumentsParser("testChoiceOption", "Unit test for passing choices on the command line.");

        ChoiceOption<E> oSize = cmd.choiceOption(E.class, "--size").defaultValue(E.GRANDE);
        ChoiceOption<E> oSizeRequired = cmd.choiceOption(E.class, "--SIZE");

        assertEquals(Optional.of(E.VENTI), cmd.parse("--product MACCHIATO --size VENTI".split(" ")).get(oSize));
        assertEquals(E.VENTI, cmd.parse("--product MACCHIATO --size VENTI".split(" ")).getOrThrow(oSize));
        assertEquals(Optional.of(E.GRANDE), cmd.parse("--product MACCHIATO".split(" ")).get(oSize));
        assertEquals(E.GRANDE, cmd.parse("--product MACCHIATO".split(" ")).getOrThrow(oSize));

        assertEquals(Optional.of(E.VENTI), cmd.parse("--product MACCHIATO --SIZE VENTI".split(" ")).get(oSizeRequired));
        assertEquals(E.VENTI, cmd.parse("--product MACCHIATO --SIZE VENTI".split(" ")).getOrThrow(oSizeRequired));
        assertEquals(Optional.empty(), cmd.parse("--product MACCHIATO".split(" ")).get(oSizeRequired));
        assertThrows(OptionException.class, () -> cmd.parse("--product MACCHIATO".split(" ")).getOrThrow(oSizeRequired));
    }

    @Test
    public void testSimpleOption() {
        ArgumentsParser cmd = new ArgumentsParser("testSimpleOption", "Unit test for passing simple options on the command line.");

        SimpleOption<String> optionName = cmd.simpleOption(String.class, "--name", "-n").description("set name");
        SimpleOption<Integer> optionAge = cmd.simpleOption(Integer.class, "--age", "-a");

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
                                          
                testSimpleOption <options> [arg1] ...
                                          
                    --age arg
                    -a arg
                                          
                    --name arg
                    -n arg
                            set name
                            
                """;
        assertEquals(expected, cmd.help());
    }

    @Test
    public void testSimpleOptionRequired() {
        ArgumentsParser cmd = new ArgumentsParser("testSimpleOptionRequired", "Unit test for passing simple options on the command line.");

        SimpleOption<String> optionName = cmd.simpleOption(String.class, "--name", "-n").description("set name").required();
        SimpleOption<Integer> optionAge = cmd.simpleOption(Integer.class, "--age", "-a");

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
                                          
                testSimpleOptionRequired <options> [arg1] ...
                                          
                    --age arg
                    -a arg
                                          
                    --name arg
                    -n arg
                            set name
                            
                """;
        assertEquals(expected, cmd.help());
    }

    @Test
    public void testPositionalArgs1() {
        ArgumentsParser cmd = new ArgumentsParser("testPositionalArgs1", "Unit test for passing positional arguments on the command line.");

        String expected = """
                                              
                testPositionalArgs1
                -------------------
                                              
                Unit test for passing positional arguments on the command line.
                                              
                testPositionalArgs1 [arg1] ...
                                          
                """;
        assertEquals(expected, cmd.help());

        Arguments args1 = cmd.parse();
        assertTrue(args1.positionalArgs().isEmpty());

        Arguments args2 = cmd.parse("abc", "def");
        assertEquals(List.of("abc", "def"), args2.positionalArgs());
    }

    @Test
    public void testPositionalArgs2() {
        ArgumentsParser cmd = new ArgumentsParser("testPositionalArgs2", "Unit test for passing positional arguments on the command line.", 1, 3);

        String expected = """
                                          
                testPositionalArgs2
                -------------------
                                          
                Unit test for passing positional arguments on the command line.
                                          
                testPositionalArgs2 arg1 [arg2] ... (up to 3 arguments)
                                          
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
        ArgumentsParser cmd = new ArgumentsParser("testOptionHandler", "Unit test for option handling.");

        List<String> yeaSayer = new ArrayList<>();
        List<String> naySayer = new ArrayList<>();

        cmd.option(String.class, "-y").arity(1).handler(yeaSayer::addAll);
        cmd.option(String.class, "-n").arity(1).handler(naySayer::addAll);

        cmd.parse("-y a -n b -n c -n d -y e -n f".split(" ")).handle();

        assertEquals(List.of("a", "e"), yeaSayer);
        assertEquals(List.of("b", "c", "d", "f"), naySayer);
    }

    @Test
    public void testStandardOptionOccurrences() {
        ArgumentsParser cmd = new ArgumentsParser("testSimpleOption", "Unit test for passing simple options on the command line.");

        Option<String> optionExactlyTwice = cmd.option(String.class, "--exactly-twice").occurrence(2);
        Option<String> optionAtMostTwice = cmd.option(String.class, "--at-most-twice").occurrence(0, 2);
        Option<String> optionTwoOrThreeTimes = cmd.option(String.class, "--two-or-three-times").occurrence(2, 3);
        Option<String> optionAtLeastTwice = cmd.option(String.class, "--at-least-twice").occurrence(2, Integer.MAX_VALUE);

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
        ArgumentsParser cmd = new ArgumentsParser("testSimpleOption", "Unit test for passing simple options on the command line.", 0, 0);

        Option<String> optionAtMostTwoArgs = cmd.option(String.class, "--at-most-two-args").arity(0, 2);
        Option<String> optionExactlyTwoArgs = cmd.option(String.class, "--exactly-two-args").arity(2);
        Option<String> optionTwoOrMoreArgs = cmd.option(String.class, "--two-or-more-args").arity(2, Integer.MAX_VALUE);

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
