package com.dua3.utility.data;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A bearer holding data, combining the {@link Supplier} and {@link Consumer} interfaces.
 * @param <T> the generic data type
 */
interface Bearer<T> extends Consumer<T>, Supplier<T> {
    /**
     * Create Bearer.
     * @param get the getter
     * @param set the setter
     * @return new Bearer instance
     * @param <T> the generic data type
     */
    static <T> Bearer<T> create(Supplier<? extends T> get, Consumer<? super T> set) {
        return new Bearer<>() {
            @Override
            public void accept(T t) {
                set.accept(t);
            }

            @Override
            public T get() {
                return get.get();
            }
        };
    }
}