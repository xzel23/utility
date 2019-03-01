package com.dua3.utility.lang;

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
public class NamedFunction<T, R> implements Function<T, R> {

    /**
     * Create a new named function.
     *
     * @param       <T>
     *              type of funcion argument
     * @param       <R>
     *              type of function return value
     * @param  name
     *              the function name
     * @param  f
     *              the function
     * @return
     *              new NamedFunction instance
     */
    public static <T, R> NamedFunction<T, R> create(String name, Function<T, R> f) {
        return new NamedFunction<>(name, f);
    }

    private final String name;

    private final Function<T, R> f;

    protected NamedFunction(String name, Function<T, R> f) {
        this.name = Objects.requireNonNull(name);
        this.f = Objects.requireNonNull(f);
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
