package com.dua3.utility.options;

import com.dua3.utility.lang.LangUtil;

/**
 * Represents a range of repetitions with a minimum and maximum value.
 * <p>
 * This record encapsulates constraints where the number of repetitions is bounded
 * by the given minimum and maximum values. It also provides well-known constants
 * for common repetition ranges and utility methods for creating specific repetition ranges.
 * <p>
 * Instances of this record are immutable.
 *
 * @param min the minimum number of repetitions, must be greater than or equal to 0
 * @param max the maximum number of repetitions, must be greater than or equal to min
 */
public record Repetitions(int min, int max) {
    public static final Repetitions ZERO = new Repetitions(0, 0);
    public static final Repetitions ZERO_OR_ONE = new Repetitions(0, 1);
    public static final Repetitions ZERO_OR_MORE = new Repetitions(0, Integer.MAX_VALUE);
    public static final Repetitions EXACTLY_ONE = new Repetitions(1, 1);
    public static final Repetitions ONE_OR_MORE = new Repetitions(1, Integer.MAX_VALUE);

    public Repetitions {
        LangUtil.check(min >= 0, () -> new IllegalArgumentException("min must be >= 0"));
        LangUtil.check(max >= min, () -> new IllegalArgumentException("max must be >= min"));
    }

    /**
     * Creates a {@code Repetitions} instance with the specified minimum and maximum values.
     *
     * @param min the minimum number of repetitions, must be greater than or equal to 0
     * @param max the maximum number of repetitions, must be greater than or equal to {@code min}
     * @return a {@code Repetitions} instance representing the range [min, max]
     * @throws IllegalArgumentException if {@code min < 0} or {@code max < min}
     */
    public static Repetitions between(int min, int max) {
        return new Repetitions(min, max);
    }

    /**
     * Creates a {@code Repetitions} instance where the minimum and maximum number
     * of repetitions are exactly the same value.
     *
     * @param n the exact number of repetitions, must be greater than or equal to 0
     * @return a {@code Repetitions} instance with both minimum and maximum repetitions set to {@code n}
     */
    public static Repetitions exactly(int n) {
        return new Repetitions(n, n);
    }

    /**
     * Creates a {@code Repetitions} instance representing a range of repetitions with
     * no upper limit and a specified minimum number of repetitions.
     *
     * @param n the minimum number of repetitions, must be greater than or equal to 0
     * @return a {@code Repetitions} instance with the specified minimum repetitions
     *         and an unlimited maximum
     */
    public static Repetitions atLeast(int n) {
        return new Repetitions(n, Integer.MAX_VALUE);
    }

    /**
     * Creates a {@code Repetitions} instance representing a range with a maximum of {@code n} repetitions
     * and a minimum of zero repetitions.
     *
     * @param n the maximum number of repetitions, must be greater than or equal to 0
     * @return a {@code Repetitions} object with a minimum of 0 and the specified maximum of {@code n}
     */
    public static Repetitions atMost(int n) {
        return new Repetitions(0, n);
    }
}
