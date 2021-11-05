package com.dua3.utility.logging;

import com.dua3.utility.io.LineOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * An adapter that listens on output to the standard output streams and passes it on as {@link LogEntry} instances.
 */
public final class SystemAdapter {

    private static @Nullable PrintStream stdOut = null;
    private static @Nullable PrintStream stdErr = null;

    private SystemAdapter() {
    }

    public static void addSystemListener(@NotNull LogListener listener) {
        addSystemOutListener(listener);
        addSystemErrListener(listener);
    }

    public static void addSystemOutListener(@NotNull LogListener listener) {
        synchronized (SystemAdapter.class) {
            if (stdOut == null) {
                try {
                    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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
    }

    public static void addSystemErrListener(@NotNull LogListener listener) {
        synchronized (SystemAdapter.class) {
            try {
                @SuppressWarnings("UseOfSystemOutOrSystemErr")
                PrintStream origErr = System.err;
                LineOutputStream lineOut = new LineOutputStream(txt -> listener.entry(new SystemErrLogEntry(txt)));
                PrintStream logOut = new PrintStream(lineOut, true, StandardCharsets.UTF_8.name());
                System.setErr(logOut);
                stdErr = origErr;
            } catch (UnsupportedEncodingException e) {
                // this should not happen since we use UTF-8 which is one of the standard encodings
                throw new IllegalStateException(e);
            }
        }
    }

    public static void removeSystemListener(LogListener listener) {
        synchronized (SystemAdapter.class) {
            removeSystemOutListener(listener);
            removeSystemErrListener(listener);
        }
    }

    public static void removeSystemOutListener(LogListener listener) {
        synchronized (SystemAdapter.class) {
            if (stdOut != null) {
                System.setOut(stdOut);
                stdOut = null;
            }
        }
    }

    public static void removeSystemErrListener(LogListener listener) {
        synchronized (SystemAdapter.class) {
            if (stdErr != null) {
                System.setErr(stdErr);
                stdErr = null;
            }
        }
    }

    abstract static class SystemLogEntry extends AbstractLogEntry<String> {
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
        public @NotNull String level() {
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
        public @NotNull Category category() {
            return Category.INFO;
        }

        @Override
        public @NotNull String logger() {
            return "System.out";
        }
    }

    static class SystemErrLogEntry extends SystemLogEntry {
        SystemErrLogEntry(String text) {
            super(text);
        }

        @Override
        public @NotNull String logger() {
            return "System.err";
        }

        @Override
        public @NotNull Category category() {
            return Category.WARNING;
        }
    }
}
