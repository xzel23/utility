package com.dua3.utility.logging;

import com.dua3.utility.io.LineOutputStream;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * An adapter that listens on output to the standard output streams and passes it on as {@link LogEntry} instances.
 */
public final class SystemAdapter {

    private static PrintStream stdOut = null;
    private static PrintStream stdErr = null;
    
    public static void addSystemListener(LogListener listener) {
        addSystemOutListener(listener);
        addSystemErrListener(listener);
    }

    public static synchronized void addSystemOutListener(LogListener listener) {
        if (stdOut== null) {
            try {
                PrintStream origOut = System.out;
                LineOutputStream lineOut = new LineOutputStream(txt -> listener.entry(new SystemOutLogEntry(txt)));
                PrintStream logOut = new PrintStream(lineOut, true, StandardCharsets.UTF_8.name());
                System.setOut(logOut);
                stdOut = origOut;
            } catch (UnsupportedEncodingException e) {
                // this should not happen since we use UTF-8 which is one of the standard encodings
                throw new IllegalStateException(e);
            }
        }
    }

    public static synchronized void addSystemErrListener(LogListener listener) {
        try {
            PrintStream origErr = System.err;
            LineOutputStream lineOut = new LineOutputStream(txt -> listener.entry(new SystemErrLogEntry(txt)));
            PrintStream logOut = new PrintStream(lineOut,true, StandardCharsets.UTF_8.name());
            System.setErr(logOut);
            stdErr = origErr;
        } catch (UnsupportedEncodingException e) {
            // this should not happen since we use UTF-8 which is one of the standard encodings
            throw new IllegalStateException(e);
        }
    }

    public static synchronized void removeSystemListener(LogListener listener) {
        removeSystemOutListener(listener);
        removeSystemErrListener(listener);
    }

    public static synchronized void removeSystemOutListener(LogListener listener) {
        if (stdOut!=null) {
            System.setOut(stdOut);
            stdOut = null;
        }
    }

    public static synchronized void removeSystemErrListener(LogListener listener) {
        if (stdErr!=null) {
            System.setErr(stdErr);
            stdErr = null;
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
