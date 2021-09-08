package com.dua3.utility.options;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CmdTest {

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

    enum E {
        TALL, GRANDE, VENTI, TRENTA
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

        assertThrows(OptionException.class, () -> cmd.parse("-n", "Eve", "--name", "Bob"));

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
    
}
