package com.dua3.utility.text;

import com.dua3.utility.io.AnsiCode;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnsiCodeTest {

    @Test
    public void testStyles() {
        String actual =
                String.format(Locale.ROOT, "This is %sbold%s text\n", AnsiCode.bold(true), AnsiCode.bold(false))
                        + String.format(Locale.ROOT, "This is %sunderlined%s text\n", AnsiCode.underline(true), AnsiCode.underline(false))
                        + String.format(Locale.ROOT, "This is %sstrikethrough%s text\n", AnsiCode.strikeThrough(true), AnsiCode.strikeThrough(false))
                        + String.format(Locale.ROOT, "This is %sitalic%s text\n", AnsiCode.italic(true), AnsiCode.italic(false));

        System.out.println(actual);

        String expected = """
                This is \u001B[1mbold\u001B[22m text
                This is \u001B[4munderlined\u001B[24m text
                This is \u001B[9mstrikethrough\u001B[29m text
                This is \u001B[3mitalic\u001B[23m text
                """;

        assertEquals(expected, actual);
    }
}
