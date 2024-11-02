// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

/**
 * A pair helper class.
 *
 * @param <T1> type of first member
 * @param <T2> type of second member
 */
public record Pair<T1 extends @Nullable Object, T2 extends @Nullable Object>(T1 first, T2 second) implements Map.Entry<T1, T2> {

    /**
     * Add pairs to a map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param m    the map to add the pairs to
     * @param args the pairs to add
     */
    @SafeVarargs
    public static <K, V> void addToMap(Map<? super K, ? super V> m, Pair<K, V>... args) {
        for (Pair<K, V> arg : args) {
            m.put(arg.first, arg.second);
        }
    }

    /**
     * Add pairs to a map.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param m    the map to add the pairs to
     * @param args the pairs to add
     */
    public static <K, V> void addToMap(Map<? super K, ? super V> m, Iterable<Pair<K, V>> args) {
        for (Pair<K, V> arg : args) {
            m.put(arg.first, arg.second);
        }
    }

    /**
     * Create a Pair.
     *
     * @param first  the first member
     * @param second the second member
     * @param <T1>   type of first member
     * @param <T2>   type of second member
     * @return a new Pair
     */
    public static <T1 extends @Nullable Object, T2 extends @Nullable Object> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    /**
     * Create a Pair.
     *
     * @param first  the first member
     * @param second the second member
     * @param <T1>   type of first member
     * @param <T2>   type of second member
     * @return a new Pair
     */
    public static <T1, T2> Pair<T1, T2> ofNonNull(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    /**
     * Create a Pair.
     *
     * @param entry a Map.Entry
     * @param <T1>  type of first member
     * @param <T2>  type of second member
     * @return a new Pair
     */
    public static <T1, T2 extends @Nullable Object> Pair<T1, T2> of(Map.Entry<? extends T1, ? extends T2> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }

    /**
     * Create a Pair.
     *
     * @param first  the first member
     * @param second the remaining members
     * @param <T1>   type of first member
     * @param <T2>   type of remaining members
     * @return a new Pair
     */
    @SafeVarargs
    public static <T1 extends @Nullable Object, T2 extends @Nullable Object> Pair<T1, T2[]> ofArray(T1 first, T2... second) {
        return new Pair<>(first, second);
    }

    /**
     * Map pair to another pair.
     *
     * @param <U1> the result's first component type
     * @param <U2> the result's second component type
     * @param f1   mapper for first component
     * @param f2   mapper for second component
     * @return Pair consisting of the mapped values of this pair
     */
    public <U1 extends @Nullable Object, U2 extends @Nullable Object> Pair<U1, U2> map(Function<? super T1, ? extends U1> f1, Function<? super T2, ? extends U2> f2) {
        return of(f1.apply(first()), f2.apply(second()));
    }

    /**
     * Map pair to another pair.
     *
     * @param <U> the result's component type*
     * @param f   mapper for components
     * @return Pair consisting of the mapped values of this pair
     */
    public <U extends @Nullable Object> Pair<U, U> map(Function<Object, ? extends @Nullable U> f) {
        return of(f.apply(first()), f.apply(second()));
    }

    /**
     * Apply mapping to the first component only.
     *
     * @param <U1> the result's first component type
     * @param f   mapper for first component
     * @return Pair consisting of the mapped first and original second component of this pair
     */
    public <U1 extends @Nullable Object> Pair<U1, T2> mapFirst(Function<? super T1, ? extends U1> f) {
        return of(f.apply(first()), second());
    }

    /**
     * Apply mapping to the second component only.
     *
     * @param <U2> the result's secondt component type
     * @param f   mapper for second component
     * @return Pair consisting of the original first and the mapped second component of this pair
     */
    public <U2 extends @Nullable Object> Pair<T1, U2> mapSecond(Function<? super T2, ? extends U2> f) {
        return of(first(), f.apply(second()));
    }

    @Override
    public T1 getKey() {
        return first;
    }

    @Override
    public T2 getValue() {
        return second;
    }

    /**
     * Required for implementing the Map.Entry interface. DO NOT USE!
     * @param value new value to be stored in this entry
     * @return nothing
     * @throws UnsupportedOperationException when called
     */
    @Override
    @Deprecated
    public T2 setValue(T2 value) {
        throw new UnsupportedOperationException("class is immutable");
    }
}
