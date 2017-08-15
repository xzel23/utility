package com.dua3.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A pair helper class.
 *
 * @param <T1> type of first member
 * @param <T2> type of second member
 */
public class Pair<T1, T2> {

	/** first member */
    public final T1 first;
	/** second member */
    public final T2 second;

    /**
     * Construct a new Pair.
     * @param first the first member
     * @param second the seond member
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Create a Pair
     * @param first the first member
     * @param second the second member
     * @param <T1> type of first member
     * @param <T2> type of second member
     * @return a new Pair
     */
    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    @Override
    public String toString() {
    		return "[" + first + "," + second +"]";
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

    @SafeVarargs
    public static <K extends Object, V extends Object> Map<K,V> toMap(Pair<K,V>... args) {
        Map<K,V> m = new HashMap<>();
        for (Pair<K,V> arg: args ) {
            m.put(arg.first, arg.second);
        }
        return m;
    }
}
