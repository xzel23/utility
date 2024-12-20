package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * An extension of the {@code Function<T,R>} interface that takes a name to be
 * used as the
 * return value of {@code toString()}.
 *
 * @param <T> type of function argument
 * @param <R> type of function return value
 */
public record NamedFunction<T extends @Nullable Object, R extends @Nullable Object>(String name,
                                                                                    Function<@Nullable T, @Nullable R> f) implements Function<@Nullable T, @Nullable R> {
    @Override
    public R apply(T t) {
        return f.apply(t);
    }

    @Override
    public String toString() {
        return name;
    }
}
