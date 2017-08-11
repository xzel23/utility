package com.dua3.utility;

/**
 * A pair helper class.
 *
 * @author axel@dua3.com
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
     * @return a new Pair
     */
    public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
        return new Pair<>(first, second);
    }
    
    @Override
    public String toString() {
    		return "[" + first + ", " + second +"]";
    }
}
