// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A pair helper class.
 *
 * @param <T1>
 *        type of first member
 * @param <T2>
 *        type of second member
 */
public record Pair<T1, T2>(T1 first, T2 second) {

    /**
     * Add pairs to a map.
     * @param <K>
     *                the key type
     * @param <V>
     *                the value type
     *
     * @param    m
     *                the map to add the pairs to
     * @param    args
     *                the pairs to add
     */
    @SafeVarargs
    public static <K, V> void addToMap(@NotNull Map<? super K, ? super V> m, Pair<K, V> @NotNull ... args) {
        for (Pair<K, V> arg : args) {
            m.put(arg.first, arg.second);
        }
    }

    /**
     * Add pairs to a map.
     * @param <K>
     *                the key type
     * @param <V>
     *                the value type
     *
     * @param    m
     *                the map to add the pairs to
     * @param    args
     *                the pairs to add
     */
    public static <K, V> void addToMap(@NotNull Map<? super K, ? super V> m, @NotNull Iterable<Pair<K, V>> args) {
        for (Pair<K, V> arg : args) {
            m.put(arg.first, arg.second);
        }
    }

    /**
     * Create a Pair.
     *
     * @param  first
     *                the first member
     * @param  second
     *                the second member
     * @param         <T1>
     *                type of first member
     * @param         <T2>
     *                type of second member
     * @return        a new Pair
     */
    public static <T1, T2> @NotNull Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    /**
     * Create a Pair.
     *
     * @param  entry
     *                a Map.Entry
     * @param         <T1>
     *                type of first member
     * @param         <T2>
     *                type of second member
     * @return        a new Pair
     */
    public static <T1, T2> @NotNull Pair<T1, T2> of(Map.@NotNull Entry<? extends T1, ? extends T2> entry) {
        return new Pair<>(entry.getKey(), entry.getValue());
    }
    
    /**
     * Create a Pair.
     *
     * @param  first
     *                the first member
     * @param  second
     *                the remaining members
     * @param         <T1>
     *                type of first member
     * @param         <T2>
     *                type of remaining members
     * @return        a new Pair
     */
    @SafeVarargs
    public static <T1, T2> @NotNull Pair<T1, T2[]> ofArray(T1 first, T2... second) {
        return new Pair<>(first, second);
    }

    /**
     * Convert an {@code Array<Pair<K,V>>} to {@code Map<K,V>}.
     * <p>
     * The returned Map can be modified by adding or removing entries.
     * </p>
     * @param <K>
     *                the key type
     * @param <V>
     *                the value type
     *
     * @param    args
     *                the entries to add to the resulting map
     * @return        a new {@code Map<K,V>} whose entries correspond to the pairs
     *                given as arguments
     */
    @SafeVarargs
    public static <K, V> @NotNull Map<K, V> toMap(Pair<K, V>... args) {
        Map<K, V> m = new HashMap<>();
        addToMap(m, args);
        return m;
    }

    /**
     * Convert a {@code Collection<Pair<K,V>>} to {@code Map<K,V>}.
     * <p>
     * The returned Map can be modified by adding or removing entries.
     * </p>
     * @param <K>
     *                the key type
     * @param <V>
     *                the value type
     *
     * @param    args
     *                the entries to add to the resulting map
     * @return        a new {@code Map<K,V>} whose entries correspond to the pairs
     *                given as arguments
     */
    public static <K, V> @NotNull Map<K, V> toMap(@NotNull Collection<Pair<K, V>> args) {
        Map<K, V> m = new HashMap<>();
        addToMap(m, args);
        return m;
    }

    /**
     * Map pair to another pair.
     * @param <U1> the result's first component type
     * @param <U2> the result's second component type
     * @param f1 mapper for first component
     * @param f2 mapper for second component
     * @return Pair consisting of the mapped values of this pair
     */
    public <U1,U2> @NotNull Pair<U1,U2> map(@NotNull Function<? super T1, ? extends U1> f1, @NotNull Function<? super T2, ? extends U2> f2) {
        return Pair.of(f1.apply(first()), f2.apply(second()));
    }

    /**
     * Map pair to another pair.
     * @param <U> the result's component type*
     * @param f mapper for components
     * @return Pair consisting of the mapped values of this pair
     */
    public <U> @NotNull Pair<U,U> map(@NotNull Function<Object,? extends U> f) {
        return Pair.of(f.apply(first()), f.apply(second()));
    }
}
