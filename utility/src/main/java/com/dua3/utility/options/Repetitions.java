package com.dua3.utility.options;

import com.dua3.utility.lang.LangUtil;

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

    public static Repetitions between(int min, int max) {
        return new Repetitions(min, max);
    }

    public static Repetitions exactly(int n) {
        return new Repetitions(n, n);
    }

    public static Repetitions atLeast(int n) {
        return new Repetitions(n, Integer.MAX_VALUE);
    }

    public static Repetitions atMost(int n) {
        return new Repetitions(0, n);
    }
}
