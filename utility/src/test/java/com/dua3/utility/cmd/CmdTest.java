package com.dua3.utility.cmd;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CmdTest {

    @Test
    public void testFlag() {
        CmdParser cmd = new CmdParser("testFlag", "Unit test for passing flags on the command line.");
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

        String expected = String.format("testFlag%n" +
                                        "--------%n" +
                                        "%n" +
                                        "Unit test for passing flags on the command line.%n" +
                                        "%n" +
                                        "testFlag <options> [arg1] ...%n" +
                                        "%n" +
                                        "    --print%n" +
                                        "    -p%n" +
                                        "            print result to terminal%n" +
                                        "%n");
        assertEquals(expected, cmd.help());
    }

    @Test
    public void testSimpleOption() {
        CmdParser cmd = new CmdParser("testSimpleOption", "Unit test for passing simple options on the command line.");

        SimpleOption<String> optionName = cmd.simpleOption(String.class, "--name", "-n").description("set name");
        SimpleOption<Integer> optionAge = cmd.simpleOption(Integer.class, "--age", "-a");

        assertFalse(cmd.parse().get(optionName).isPresent());
        assertFalse(cmd.parse().get(optionAge).isPresent());

        assertEquals("Eve", cmd.parse("-n", "Eve").getOrThrow(optionName));
        assertEquals(30, cmd.parse("--age", "30").getOrThrow(optionAge));

        assertThrows(CmdException.class, () -> cmd.parse("-n", "Eve", "--name", "Bob"));

        CmdArgs eve30 = cmd.parse("-n", "Eve", "--age", "30");
        assertEquals("Eve", eve30.getOrThrow(optionName));
        assertEquals(30, eve30.getOrThrow(optionAge));

        String expected = String.format("testSimpleOption%n" +
                                        "----------------%n" +
                                        "%n" +
                                        "Unit test for passing simple options on the command line.%n" +
                                        "%n" +
                                        "testSimpleOption <options> [arg1] ...%n" +
                                        "%n" +
                                        "    --age arg%n" +
                                        "    -a arg%n" +
                                        "%n" +
                                        "    --name arg%n" +
                                        "    -n arg%n" +
                                        "            set name%n" +
                                        "%n");
        assertEquals(expected, cmd.help());
    }

    @Test
    public void testPositionalArgs1() {
        CmdParser cmd = new CmdParser("testPositionalArgs1", "Unit test for passing positional arguments on the command line.");

        assertEquals(
                String.format("testPositionalArgs1%n" +
                              "-------------------%n" +
                              "%n" +
                              "Unit test for passing positional arguments on the command line.%n" +
                              "%n" +
                              "testPositionalArgs1 [arg1] ...%n" +
                              "%n")
                , cmd.help());

        CmdArgs args1 = cmd.parse();
        assertTrue(args1.positionalArgs().isEmpty());
        
        CmdArgs args2 = cmd.parse("abc", "def");
        assertEquals(List.of("abc", "def"), args2.positionalArgs());
    }

    @Test
    public void testPositionalArgs2() {
        CmdParser cmd = new CmdParser("testPositionalArgs2", "Unit test for passing positional arguments on the command line.", 1, 3);
        assertEquals(
                String.format("testPositionalArgs2%n" +
                     "-------------------%n" +
                     "%n" +
                     "Unit test for passing positional arguments on the command line.%n" +
                     "%n" +
                     "testPositionalArgs2 arg1 [arg2] ... (up to 3 arguments)%n" +
                     "%n")
                , cmd.help());
        
        // min arity is 1!
        assertThrows(CmdException.class, cmd::parse);

        // max arity is 3!
        assertThrows(CmdException.class, () -> cmd.parse("abc", "def", "ghi", "jkl"));

        // these are all valid
        assertEquals(List.of("abc"), cmd.parse("abc").positionalArgs());
        assertEquals(List.of("abc", "def"), cmd.parse("abc", "def").positionalArgs());
        assertEquals(List.of("abc", "def", "ghi"), cmd.parse("abc", "def", "ghi").positionalArgs());
    }

}
