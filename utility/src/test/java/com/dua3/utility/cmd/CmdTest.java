package com.dua3.utility.cmd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CmdTest {

    @Test
    public void testFlag() {
        CmdParser cmd = new CmdParser("test flag", "Unit test for passing flags on the command line.");
        Flag oPrint = cmd.flag("--print", "-p").description("print result to terminal");

        assertFalse(cmd.parse().isSet(oPrint));
        
        assertTrue(cmd.parse("-p").isSet(oPrint));
        assertTrue(cmd.parse("--print").isSet(oPrint));
        
        assertTrue(cmd.parse("hello", "-p").isSet(oPrint));
        assertTrue(cmd.parse("hello", "--print").isSet(oPrint));
        
        assertTrue(cmd.parse("-p", "hello").isSet(oPrint));
        assertTrue(cmd.parse("--print", "hello").isSet(oPrint));
        
        String expected = String.format("test flag%n" +
                                        "---------%n" +
                                        "%n" +
                                        "Unit test for passing flags on the command line.%n" +
                                        "%n" +
                                        "--print%n" +
                                        "-p%n" +
                                        "    print result to terminal%n" +
                                        "%n");
        assertEquals(expected, cmd.help());
    }
}
