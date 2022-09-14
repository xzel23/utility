package com.dua3.utility.logging;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.AnsiCode;
import org.slf4j.event.Level;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;

public class ConsoleHandler implements LogEntryHandler {
    private static final String NEWLINE = "%n".formatted();
    private static final Pair<String, String> NO_ESCAPE_SEQUENCES = Pair.of("", "");
    private static final String ESC_RESET = AnsiCode.reset();

    private final PrintStream out;
    private final Map<Level, Pair<String,String>> brackets = new EnumMap<>(Level.class);

    public ConsoleHandler(PrintStream out, boolean colored) {
        this.out = out;
        if (colored) {
            this.brackets.put(Level.TRACE, Pair.of(AnsiCode.fg(Color.DARKGRAY), ESC_RESET));
            this.brackets.put(Level.DEBUG, Pair.of(AnsiCode.fg(Color.BLACK), ESC_RESET));
            this.brackets.put(Level.INFO, Pair.of(AnsiCode.fg(Color.BLUE), ESC_RESET));
            this.brackets.put(Level.WARN, Pair.of(AnsiCode.fg(Color.ORANGERED), ESC_RESET));
            this.brackets.put(Level.ERROR, Pair.of(AnsiCode.fg(Color.DARKRED), ESC_RESET));
        }
    }

    @Override
    public void handleEntry(LogEntry entry) {
        var esc = brackets.getOrDefault(entry.level(), NO_ESCAPE_SEQUENCES);
        out.append(esc.first()).append(String.valueOf(entry)).append(esc.second()).append(NEWLINE);
    }

}
