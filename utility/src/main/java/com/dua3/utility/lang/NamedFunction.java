package com.dua3.utility.lang;

import com.dua3.cabe.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * An extension of the {@code Function<T,R>} interface that takes a name to be
 * used as the
 * return value of {@code toString()}.
 *
 * @param <T>
 *        type of funcion argument
 * @param <R>
 *        type of function return value
 */
public record NamedFunction<T, R>(@NotNull String name, @NotNull Function<T, R> f) implements Function<T, R> {

    public NamedFunction {
        Objects.requireNonNull(name);
        Objects.requireNonNull(f);
    }

    @Override
    public R apply(T t) {
        return f.apply(t);
    }

    @Override
    public String toString() {
        return name;
    }
}
