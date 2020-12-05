// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A pair helper class.
 *
 * @param <T1>
 *        type of first member
 * @param <T2>
 *        type of second member
 */
public class Pair<T1, T2> {

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
    public static <K, V> void addToMap(Map<K, V> m, Pair<K, V>... args) {
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
    public static <K, V> void addToMap(Map<K, V> m, Iterable<Pair<K, V>> args) {
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
    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
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
    public static <T1, T2> Pair<T1, T2> of(Map.Entry<T1,T2> entry) {
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
    public static <T1, T2> Pair<T1, T2[]> ofArray(T1 first, T2... second) {
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
    public static <K, V> Map<K, V> toMap(Pair<K, V>... args) {
        Map<K, V> m = new HashMap<>();
        addToMap(m, args);
        return m;
    }

    /** first member */
    public final T1 first;

    /** second member */
    public final T2 second;

    /**
     * Construct a new Pair.
     *
     * @param first
     *               the first member
     * @param second
     *               the second member
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "[" + first + ',' + second + ']';
    }
}
