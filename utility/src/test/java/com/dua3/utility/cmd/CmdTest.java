package com.dua3.utility.cmd;

import org.junit.jupiter.api.Test;
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
        
        String expected = String.format("testFlag%n" +
                                        "--------%n" +
                                        "%n" +
                                        "Unit test for passing flags on the command line.%n" +
                                        "%n" +
                                        "testFlag <options> [arg1] ...%n" +
                                        "%n" +
                                        "    --print %n" +
                                        "    -p %n" +
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
        
        assertThrows(CmdException.class, () -> {
            cmd.parse("-n", "Eve", "--name", "Bob");
        });
        
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
}
