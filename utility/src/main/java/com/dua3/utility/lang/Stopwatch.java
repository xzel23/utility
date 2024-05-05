// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple stopwatch class.
 */
public class Stopwatch {

    private final Object name;
    private final Instant start;
    private final Format format = Format.STANDARD;
    private Instant startSplit;

    /**
     * Construct instance.
     *
     * @param name the name for this instance; it is included in {@code toString()}
     */
    protected Stopwatch(String name) {
        this.name = name;
        this.start = this.startSplit = Instant.now();
    }

    /**
     * Construct instance.
     *
     * @param name the name for this instance; it is included in {@code toString()}
     */
    protected Stopwatch(Supplier<String> name) {
        this.name = new LazyName(name);
        this.start = this.startSplit = Instant.now();
    }

    /**
     * Create new instance.
     *
     * @param name the name
     * @return new instance
     */
    public static Stopwatch create(String name) {
        return new Stopwatch(name);
    }

    /**
     * Create new instance.
     *
     * @param name the name supplier
     * @return new instance
     */
    public static Stopwatch create(Supplier<String> name) {
        return new Stopwatch(name);
    }

    /**
     * Create new instance.
     *
     * @param name    the name
     * @param onClose the action to perform when close() is called
     * @return new instance
     */
    public static AutoCloseableStopWatch create(String name, Consumer<? super Stopwatch> onClose) {
        return new AutoCloseableStopWatch(name, onClose);
    }

    /**
     * Create new instance.
     *
     * @param name    the name supplier
     * @param onClose the action to perform when close() is called
     * @return new instance
     */
    public static AutoCloseableStopWatch create(Supplier<String> name, Consumer<? super Stopwatch> onClose) {
        return new AutoCloseableStopWatch(name, onClose);
    }

    /**
     * Get the name of this StopWatch instance.
     * @return the name of this instance
     */
    public String getName() {
        return name.toString();
    }

    /**
     * Get start instant.
     *
     * @return the instant when this stopwatch was created
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Get start instant of current split.
     *
     * @return the instant of the last split set in this stopwatch or the instant when this stopwatch was created of no split has been set
     */
    public Instant getStartSplit() {
        return startSplit;
    }

    /**
     * Get elapsed time in current split.
     *
     * @param newSplit if true, start a new split
     * @return the duration since the start of the current split
     */
    public Duration elapsedSplit(boolean newSplit) {
        Instant now = Instant.now();
        Duration duration = Duration.between(startSplit, now);
        if (newSplit) {
            startSplit = now;
        }
        return duration;
    }

    /**
     * Get elapsed time.
     *
     * @return the duration since this instance was created
     */
    public Duration elapsed() {
        Instant now = Instant.now();
        return Duration.between(start, now);
    }

    @Override
    public String toString() {
        return "[" + name + "] current split: " + elapsedStringSplit(false) + " total: " + elapsedString();
    }

    /**
     * Get elapsed time as a string.
     *
     * @return string with elapsed time
     */
    public String elapsedString() {
        return format.format(elapsed());
    }

    /**
     * Get elapsed time in current split as a string.
     *
     * @param newSplit if true, reset the stopwatch
     * @return string with elapsed time
     */
    public String elapsedStringSplit(boolean newSplit) {
        return format.format(elapsedSplit(newSplit));
    }

    /**
     * Create a Supplier for use in log messages (formatting is only done when {@link Supplier#get()} is called).
     *
     * @param fmt the format to use
     * @return Supplier that returns the state of the stopwatch at time of invocation
     */
    public Object logElapsed(Format fmt) {
        Instant instant = Instant.now();
        return new Object() {
            @Override
            public String toString() {
                return fmt.format(Duration.between(start, instant));
            }
        };
    }

    /**
     * Create a Supplier for use in log messages (formatting is only done when {@link Supplier#get()} is called).
     *
     * @param fmt      the format to use
     * @param newSplit if true, start a new split
     * @return Supplier that returns the state of the stopwatch at time of invocation
     */
    public Object logElapsedSplit(Format fmt, boolean newSplit) {
        Instant startOfSplit = startSplit;
        Instant instant = Instant.now();
        return new ToStringProxy(fmt, startOfSplit, instant);
    }

    /**
     * Enum defining the different output formats.
     */
    public enum Format {
        /**
         * standard format, same as {@link #HOURS_MINUTES_SECONDS_MILLIS}.
         */
        STANDARD {
            @Override
            public String format(Duration d) {
                boolean negative = d.isNegative();

                long seconds = d.getSeconds();
                long absSeconds = Math.abs(seconds);
                int nano = Math.abs(d.getNano());

                long hr = absSeconds / 3600;
                long min = (absSeconds % 3600) / 60;
                double sec = (absSeconds % 60) + nano / 1_000_000_000.0;

                String positive = String.format(
                        Locale.ROOT,
                        "%d:%02d:%06.3f",
                        hr,
                        min,
                        sec);

                return negative ? "-" + positive : positive;
            }
        },
        /**
         * h:mm:ss.sss.
         */
        HOURS_MINUTES_SECONDS_MILLIS {
            @Override
            public String format(Duration d) {
                return STANDARD.format(d);
            }
        },
        /**
         * m:ss.sss.
         */
        MINUTES_SECONDS_MILLIS {
            @Override
            public String format(Duration d) {
                boolean negative = d.isNegative();

                long seconds = d.getSeconds();
                long absSeconds = Math.abs(seconds);
                int nano = Math.abs(d.getNano());

                long min = absSeconds / 60;
                double sec = (absSeconds % 60) + nano / 1_000_000_000.0;

                String positive = String.format(
                        Locale.ROOT,
                        "%dm:%06.3fs",
                        min,
                        sec);

                return negative ? "-" + positive : positive;
            }
        },
        /**
         * Seconds formatted as floating point value.
         */
        SECONDS_MILLIS {
            @Override
            public String format(Duration d) {
                boolean negative = d.isNegative();

                long seconds = d.getSeconds();
                long absSeconds = Math.abs(seconds);
                int nano = Math.abs(d.getNano());

                double sec = absSeconds + nano / 1_000_000_000.0;

                String positive = String.format(
                        Locale.ROOT,
                        "%.3fs",
                        sec);

                return negative ? "-" + positive : positive;
            }
        },
        /**
         * Milliseconds formatted as floating point value.
         */
        MILLIS {
            @Override
            public String format(Duration d) {
                boolean negative = d.isNegative();

                long seconds = d.getSeconds();
                long absSeconds = Math.abs(seconds);
                int nano = Math.abs(d.getNano());

                double millis = (absSeconds + nano / 1_000_000_000.0) * 1000.0;

                String positive = String.format(
                        Locale.ROOT,
                        "%.3fms",
                        millis);

                return negative ? "-" + positive : positive;
            }
        };

        /**
         * Format a duration.
         *
         * @param d the duration
         * @return d formatted as a string
         */
        public abstract String format(Duration d);
    }

    /**
     * A stopwatch that can be used in a try-with-resources block and automatically closed.
     */
    public static class AutoCloseableStopWatch extends Stopwatch implements AutoCloseable {
        private final Consumer<? super Stopwatch> onClose;

        protected AutoCloseableStopWatch(String name, Consumer<? super Stopwatch> onClose) {
            super(name);
            this.onClose = onClose;
        }

        protected AutoCloseableStopWatch(Supplier<String> name, Consumer<? super Stopwatch> onClose) {
            super(name);
            this.onClose = onClose;
        }

        @Override
        public void close() {
            onClose.accept(this);
        }
    }

    private static class LazyName {
        private final Supplier<String> name;
        String n;

        public LazyName(Supplier<String> name) {
            this.name = name;
        }

        @Override
        public String toString() {
            if (n == null) {
                n = name.get();
            }
            return n;
        }
    }

    private static class ToStringProxy {
        private final Format fmt;
        private final Instant startOfSplit;
        private final Instant instant;

        public ToStringProxy(Format fmt, Instant startOfSplit, Instant instant) {
            this.fmt = fmt;
            this.startOfSplit = startOfSplit;
            this.instant = instant;
        }

        @Override
        public String toString() {
            return fmt.format(Duration.between(startOfSplit, instant));
        }
    }
}
