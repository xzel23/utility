package com.dua3.utility.logging;

import com.dua3.utility.io.LineOutputStream;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;

public final class SystemAdapter {

    public static void addSystemListener(LogListener listener) {
        addSystemOutListener(listener);
        addSystemErrListener(listener);
    }

    public static void addSystemOutListener(LogListener listener) {
        try {
            LineOutputStream lineOut = new LineOutputStream(txt -> listener.entry(new SystemOutLogEntry(txt)));
            PrintStream logOut = new PrintStream(lineOut,true, StandardCharsets.UTF_8.name());
            System.setOut(logOut);
        } catch (UnsupportedEncodingException e) {
            // this should not happen since we use UTF-8 which is one of the standard encodings
            throw new IllegalStateException(e);
        }
    }

    public static void addSystemErrListener(LogListener listener) {
        try {
            LineOutputStream lineOut = new LineOutputStream(txt -> listener.entry(new SystemErrLogEntry(txt)));
            PrintStream logOut = new PrintStream(lineOut,true, StandardCharsets.UTF_8.name());
            System.setErr(logOut);
        } catch (UnsupportedEncodingException e) {
            // this should not happen since we use UTF-8 which is one of the standard encodings
            throw new IllegalStateException(e);
        }
    }

    static abstract class SystemLogEntry extends AbstractLogEntry<String> {
        private final String text;
        private final long millis;

        SystemLogEntry(String text) {
            this.text = text;
            this.millis = System.currentTimeMillis();
        }

        @Override
        public long millis() {
            return millis;
        }

        @Override
        public String message() {
            return text;
        }

        @Override
        public Optional<IThrowable> cause() {
            return Optional.empty();
        }

        @Override
        public String level() {
            return category().name();
        }

        @Override
        public String getNative() {
            return text;
        }
    }

    static class SystemOutLogEntry extends SystemLogEntry {
        SystemOutLogEntry(String text) {
            super(text);
        }

        @Override
        public Category category() {
            return Category.INFO;
        }

        @Override
        public String logger() {
            return "System.out";
        }
    }

    static class SystemErrLogEntry extends SystemLogEntry {
        SystemErrLogEntry(String text) {
            super(text);
        }

        @Override
        public String logger() {
            return "System.err";
        }

        @Override
        public Category category() {
            return Category.WARNING;
        }
    }
}
