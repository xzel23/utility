package com.dua3.utility.options;

import com.dua3.utility.text.TextUtil;
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
class ArgumentsParserTest {

    @Test
    void testFlag() {
        // create the parser
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testFlag")
                .description("Unit test for passing flags on the command line.");
        Option<Boolean> oPrint = builder.addFlag("--print", "print result to terminal", "--print", "-p");
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
                    --print, -p
                            print result to terminal
                
                """;

        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());

        Arguments args = cmd.parse("-p", "hello", "Bob");
        String expectedToString = """
                Arguments{
                  --print
                  "hello" "Bob"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    void testRestrictedOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testChoiceOption")
                .description("Unit test for passing choices on the command line.");

        Option<String> oProduct = builder.optionBuilder("product name", "the product", String.class)
                .param(Param.ofString("product name", "the product", "product", Param.Required.REQUIRED))
                .repetitions(Repetitions.EXACTLY_ONE)
                .build("--product", "-p");

        Option<E> oSize = builder.optionBuilder("Size", "the size of the serving", E.class)
                .param(
                        Param.ofEnum(
                                "serving size",
                                "the size of the serving",
                                "size",
                                Param.Required.REQUIRED,
                                E.class
                        )
                )
                .defaultSupplier(() -> E.GRANDE)
                .build("--size");

        builder.optionBuilder("tag", "the tag", String.class)
                .repetitions(Repetitions.ZERO_OR_MORE)
                .optionalParam(
                        Param.ofString("tag 1", "the main tag", "main tag", Param.Required.OPTIONAL),
                        Param.ofString("tag 2", "the secondary tag", "secondary tag", Param.Required.OPTIONAL),
                        Param.ofStrings("tag n", "additional tags", "tag", Repetitions.between(0, 3)
                        )
                )
                .build("--tags");

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
                    --product, -p <product>    (required)
                            the product
                
                    --size <size>    (optional)
                            the size of the serving
                
                    --tags [<main tag>] [<secondary tag>] [<tag1>] ... [<tag3>]    (zero or more)
                            the tag
                
                """;

        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());

        Arguments args = cmd.parse("--product", "MACCHIATO", "--size", "VENTI");
        String expectedToString = """
                Arguments{
                  --product "MACCHIATO"
                  --size "VENTI"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    void testChoiceOptionRequired() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testChoiceOptionRequired")
                .description("Unit test for passing choices on the command line.");

        Option<String> oProduct = builder.addStringOption(
                "product",
                "set the product name",
                Repetitions.EXACTLY_ONE,
                "product",
                () -> null,
                "--product", "-p"
        );

        Option<E> oSize = builder.addEnumOption(
                "Serving size",
                "set the serving size",
                Repetitions.EXACTLY_ONE,
                "size",
                () -> null,
                E.class,
                "--size"
        );

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
                    --product, -p <product>    (required)
                            set the product name
                
                    --size <size>    (required)
                            set the serving size
                
                """;

        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());
    }

    @Test
    void testOption() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for passing simple options on the command line.");

        Option<String> optionName = builder.addStringOption(
                "name",
                "set name",
                Repetitions.ZERO_OR_ONE,
                "name",
                () -> null,
                "--name", "-n"
        );

        Option<Integer> optionAge = builder.addIntegerOption(
                "age", "set the age", Repetitions.ZERO_OR_ONE, "age", () -> null, "--age", "-a");

        ArgumentsParser cmd = builder.build();

        assertFalse(cmd.parse().get(optionName).isPresent());
        assertFalse(cmd.parse().get(optionAge).isPresent());

        assertEquals("Eve", cmd.parse("-n", "Eve").getOrThrow(optionName));
        assertEquals(30, cmd.parse("--age", "30").getOrThrow(optionAge));

        Arguments eve30 = cmd.parse("-n", "Eve", "--age", "30");
        assertEquals("Eve", eve30.getOrThrow(optionName));
        assertEquals(30, eve30.getOrThrow(optionAge));

        String expected = """
                
                testOption
                ----------
                
                Unit test for passing simple options on the command line.
                
                testOption <options> [<arg> ...]
                
                  <options>:
                    --name, -n <name>    (optional)
                            set name
                
                    --age, -a <age>    (optional)
                            set the age
                
                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());

        Arguments args = cmd.parse("-n", "Eve", "--age", "30");
        String expectedToString = """
                Arguments{
                  --name "Eve"
                  --age "30"
                }""";
        assertEquals(expectedToString, args.toString());
    }

    @Test
    void testOptionRequired() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOptionRequired")
                .description("Unit test for passing simple options on the command line.");
        Option<String> optionName = builder.addStringOption("name", "set name", Repetitions.EXACTLY_ONE,
                "name", () -> null, "--name", "-n");

        Option<Integer> optionAge = builder.addIntegerOption(
                "age", "set the age", Repetitions.ZERO_OR_ONE, "age", () -> null, "--age", "-a");

        ArgumentsParser cmd = builder.build();

        assertThrows(OptionException.class, cmd::parse);

        assertEquals("Eve", cmd.parse("-n", "Eve").getOrThrow(optionName));
        assertThrows(OptionException.class, () -> cmd.parse("--age", "30").getOrThrow(optionAge));

        assertThrows(OptionException.class, () -> cmd.parse("-n", "Eve", "--name", "Bob"));

        Arguments eve30 = cmd.parse("-n", "Eve", "--age", "30");
        assertEquals("Eve", eve30.getOrThrow(optionName));
        assertEquals(30, eve30.getOrThrow(optionAge));

        String expected = """
                
                testOptionRequired
                ------------------
                
                Unit test for passing simple options on the command line.
                
                testOptionRequired <options> [<arg> ...]
                
                  <options>:
                    --name, -n <name>    (required)
                            set name
                
                    --age, -a <age>    (optional)
                            set the age
                
                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());
    }

    @Test
    void testPositionalArgs1() {
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
        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());

        Arguments args1 = cmd.parse();
        assertTrue(args1.positionalArgs().isEmpty());

        Arguments args2 = cmd.parse("abc", "def");
        assertEquals(List.of("abc", "def"), args2.positionalArgs());
    }

    @Test
    void testPositionalArgs2() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testPositionalArgs2")
                .description("Unit test for passing positional arguments on the command line.")
                .positionalArgs(1, 3);
        ArgumentsParser cmd = builder.build();

        String expected = """
                
                testPositionalArgs2
                -------------------
                
                Unit test for passing positional arguments on the command line.
                
                testPositionalArgs2 <arg1> [<arg2>] [<arg3>]
                
                """;
        assertEquals(TextUtil.toSystemLineEnds(expected), cmd.help());

        // min arity is 1!
        assertThrows(ArgumentsException.class, cmd::parse);

        // max arity is 3!
        assertThrows(ArgumentsException.class, () -> cmd.parse("abc", "def", "ghi", "jkl"));

        // these are all valid
        assertEquals(List.of("abc"), cmd.parse("abc").positionalArgs());
        assertEquals(List.of("abc", "def"), cmd.parse("abc", "def").positionalArgs());
        assertEquals(List.of("abc", "def", "ghi"), cmd.parse("abc", "def", "ghi").positionalArgs());
    }

    @Test
    void testOptionHandler() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOptionHandler")
                .description("Unit test for option handling.");

        List<String> yeaSayer = new ArrayList<>();
        builder.optionBuilder("yeasayer", "add a yeasayer", String.class)
                .repetitions(Repetitions.ZERO_OR_MORE)
                .param(Param.ofString("yeasayer", "the yeasayer", "who", Param.Required.REQUIRED))
                .handler(yeaSayer::add)
                .build("-y");

        List<String> naySayer = new ArrayList<>();
        builder.optionBuilder("naysayer", "add a naysayer", String.class)
                .repetitions(Repetitions.ZERO_OR_MORE)
                .param(Param.ofString("naysayer", "the naysayer", "who", Param.Required.REQUIRED))
                .handler(naySayer::add)
                .build("-n");

        ArgumentsParser cmd = builder.build();

        cmd.parse("-y a -n b -n c -n d -y e -n f".split(" ")).handle();

        assertEquals(List.of("a", "e"), yeaSayer);
        assertEquals(List.of("b", "c", "d", "f"), naySayer);
    }

    @Test
    void testStandardOptionOccurrences() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testOption")
                .description("Unit test for passing simple options on the command line.");

        Option<String> optionExactlyTwice = builder.optionBuilder("2x", "2x", String.class)
                .param(
                        Param.ofString("argument", "the argument", "arg", Param.Required.OPTIONAL)
                )
                .repetitions(Repetitions.exactly(2))
                .build("--exactly-twice");

        Option<String> optionAtMostTwice = builder.optionBuilder("0 - 2x", "0 - 2x", String.class)
                .param(
                        Param.ofString("argument", "the argument", "arg", Param.Required.OPTIONAL)
                )
                .repetitions(Repetitions.atMost(2))
                .build("--at-most-twice");

        Option<String> optionTwoOrThreeTimes = builder.optionBuilder("2 - 3x", "2 - 3x", String.class)
                .param(
                        Param.ofString("argument", "the argument", "arg", Param.Required.OPTIONAL)
                )
                .repetitions(Repetitions.between(2, 3))
                .build("--two-or-three-times");

        Option<String> optionAtLeastTwice = builder.optionBuilder("2 - ... x", "2 - ... x", String.class)
                .param(
                        Param.ofString("argument", "the argument", "arg", Param.Required.OPTIONAL)
                )
                .repetitions(Repetitions.atLeast(2))
                .build("--at-least-twice");

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

    @SuppressWarnings("unchecked")
    @Test
    void testStandardOptionArity() {
        ArgumentsParserBuilder builder = ArgumentsParser.builder()
                .name("testStandardOptionArity")
                .description("Unit test for testing opzion arity on the command line.")
                .positionalArgs(0, 0);

        Option<List<String>> optionAtMostTwoArgs = builder.optionBuilder("0 - 2 args", "0 - 2 args", (Class<List<String>>) ((Class) List.class))
                .param(Param.ofStrings("0 - 2 args", "0 - 2 args", "arg", Repetitions.between(0, 2)))
                .mapper(OptionBuilder.toStringListMapper())
                .build("--at-most-two-args");

        Option<String> optionExactlyTwoArgs = builder.optionBuilder("2 args", "2 args", String.class)
                .param(Param.ofStrings("2 args", "2 args", "arg", Repetitions.exactly(2)))
                .build("--exactly-two-args");

        Option<List<String>> optionTwoOrMoreArgs = builder.optionBuilder("2 - ... args", "2 - ... args", (Class<List<String>>) ((Class) List.class))
                .param(Param.ofStrings("2 - ... args", "2 - ... args", "arg", Repetitions.atLeast(2)))
                .mapper(OptionBuilder.toStringListMapper())
                .build("--at-least-two-args");

        ArgumentsParser cmd = builder.build();

        assertEquals(List.of(Collections.emptyList()), cmd.parse("--at-most-two-args").stream(optionAtMostTwoArgs).toList());
        assertEquals(List.of(List.of("A")), cmd.parse("--at-most-two-args", "A").stream(optionAtMostTwoArgs).toList());
        assertEquals(List.of(List.of("A", "B")), cmd.parse("--at-most-two-args", "A", "B").stream(optionAtMostTwoArgs).toList());
        assertThrows(ArgumentsException.class, () -> cmd.parse("--at-most-two-args", "A", "B", "C"));

        assertThrows(OptionException.class, () -> cmd.parse("--exactly-two-args"));
        assertThrows(OptionException.class, () -> cmd.parse("--exactly-two-args", "A"));
        assertEquals(List.of("A B"), cmd.parse("--exactly-two-args", "A", "B").stream(optionExactlyTwoArgs).toList());
        assertThrows(ArgumentsException.class, () -> cmd.parse("--exactly-two-args", "A", "B", "C"));

        assertThrows(OptionException.class, () -> cmd.parse("--at-least-two-args"));
        assertThrows(OptionException.class, () -> cmd.parse("--at-least-two-args", "A"));
        assertEquals(List.of(List.of("A", "B")), cmd.parse("--at-least-two-args", "A", "B").stream(optionTwoOrMoreArgs).toList());
        assertEquals(List.of(List.of("A", "B", "C")), cmd.parse("--at-least-two-args", "A", "B", "C").stream(optionTwoOrMoreArgs).toList());
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum E {
        TALL, GRANDE, VENTI, TRENTA
    }
}
