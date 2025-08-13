package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

/**
 * A Counter class that maintains a numerical value and provides methods to
 * manipulate and retrieve that value. It implements the {@link Comparable}
 * interface to allow comparison between different Counter instances based
 * on their values.
 * <p>
 * This class is not thread-safe!
 * <p>
 * Motivation for this class is implementing histograms or related structures
 * where storing a wrapper type would lead to lots of temporary objects being
 * created. It is intended to be used where the thread-safety of
 * {@link java.util.concurrent.atomic.AtomicLong} is not required.
 */
public final class Counter implements Comparable<Counter> {
    private long value;

    /**
     * Constructs a new {@code Counter} instance with an initial value of 0.
     */
    public Counter() {
        this.value = 0;
    }

    /**
     * Retrieves the current value of the Counter.
     *
     * @return the current numerical value stored in this Counter instance
     */
    long get() {
        return value;
    }

    /**
     * Increments the internal counter value by one.
     */
    void increment() {
        this.value++;
    }

    /**
     * Decreases the current value of the counter by 1.
     */
    void decrement() {
        this.value--;
    }

    /**
     * Adds the specified value to the current value of the Counter.
     *
     * @param n the value to add to the Counter
     */
    void add(long n) {
        value += n;
    }

    /**
     * Subtracts the specified value from the current value of this counter.
     *
     * @param n the value to subtract from the counter
     */
    void subtract(long n) {
        value -= n;
    }

    /**
     * Compares this Counter instance with the specified Counter instance for order.
     * The comparison is based on the numerical value of the counters.
     *
     * @param o the Counter instance to be compared with this instance
     * @return a negative integer, zero, or a positive integer as the value of this
     *         Counter is less than, equal to, or greater than the value
     *         of the specified Counter, respectively
     */
    @SuppressWarnings("CompareToUsesNonFinalVariable")
    @Override
    public int compareTo(Counter o) {
        return Long.compare(value, o.value);
    }

    /**
     * Compares the specified object with this Counter for equality. Returns true
     * if the specified object is also a Counter and both instances have the same value.
     *
     * @param obj the object to be compared for equality with this Counter
     * @return true if the specified object is equal to this Counter, false otherwise
     */
    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof Counter c) && (value == c.value);
    }

    /**
     * Computes the hash code for this {@code Counter} object based on its current value.
     *
     * @return the hash code computed from the value of this counter
     */
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
