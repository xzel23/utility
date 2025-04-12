package com.dua3.utility.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A generic class that represents a histogram, which counts occurrences of various items.
 * It supports functionality to add items, retrieve counts, and determine the most frequently
 * occurring item.
 *
 * @param <T> the type of items to be stored in the histogram
 */
public class Histogram<T> {

    /**
     * A constant Counter instance with a value of zero.
     * This is used as a default or fallback value in the {@link Histogram} class
     * when querying counts for items that do not exist in the histogram.
     * <p>
     * This instance must not be mutated or returned directly to user code!
     */
    private static final Counter ZERO = new Counter();

    /**
     * A map that associates items of type {@code T} with their respective counts,
     * represented by instances of the {@link Counter} class.
     * This map serves as the underlying storage for maintaining the histogram's data.
     */
    private final Map<T, Counter> map;

    /**
     * Constructs a {@code Histogram} instance using the provided map for internal storage.
     *
     * @param map the map used to store items and their associated counts;
     *            must map items of type {@code T} to their respective {@code Counter} instances
     */
    private Histogram(Map<T, Counter> map) {
        this.map = map;
    }

    /**
     * Creates a new instance of a {@link Histogram} that backed by an {@link IdentityHashMap}
     * to count occurrences. This ensures that entries are distinguished based
     * on reference equality rather than object equality.
     *
     * @param <T> the type of items to be stored in the histogram
     * @return a new {@code Histogram} instance that uses reference equality for its items
     */
    public static <T> Histogram<T> createIdentityBased() {
        return new Histogram<>(new IdentityHashMap<>());
    }

    /**
     * Creates a new instance of {@code Histogram} backed by a {@code HashMap} as the underlying storage
     * for counting occurrences of items.
     *
     * @param <T> the type of items to be counted in the histogram
     * @return a {@code Histogram} instance utilizing a {@code HashMap<T, Counter>} for storage
     */
    public static <T> Histogram<T> createHashBased() {
        return new Histogram<>(new HashMap<>());
    }

    /**
     * Adds an item to the histogram, incrementing its associated count.
     * If the item does not already exist in the histogram, it is added
     * with an initial count of 1.
     *
     * @param item the item to be added to the histogram
     */
    public void add(T item) {
        map.computeIfAbsent(item, k -> new Counter()).increment();
    }

    /**
     * Returns an optional containing the most frequently occurring item in the histogram,
     * based on the highest count. If the histogram is empty, an empty optional is returned.
     *
     * @return an {@code Optional} containing the item with the highest count, or an empty optional
     *         if the histogram has no items
     */
    public Optional<T> getMax() {
        return getMaxEntry().map(Map.Entry::getKey);
    }

    /**
     * Retrieves the entry with the maximum value in the histogram.
     * The entry returned represents the item with the highest count and its associated count.
     * If the histogram is empty, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the {@code Map.Entry} with the maximum value,
     *         or an empty {@code Optional} if the histogram is empty
     */
    public Optional<Map.Entry<T, Counter>> getMaxEntry() {
        return map.entrySet().stream().max(Map.Entry.comparingByValue());
    }

    /**
     * Retrieves the count of occurrences for a specific item in the histogram.
     * If the item is not found, the count defaults to zero.
     *
     * @param item the item whose count is to be retrieved
     * @return the count of occurrences of the given item; returns zero if the item is not present
     */
    public long getCount(T item) {
        return map.getOrDefault(item, ZERO).get();
    }

    /**
     * Returns an unmodifiable view of the keys in the histogram.
     * Modifications to the returned collection are not allowed.
     *
     * @return a collection of keys stored in the histogram
     */
    public Collection<T> getKeys() {
        return Collections.unmodifiableCollection(map.keySet());
    }

    /**
     * A record that represents an immutable key-value pair consisting of a key
     * and its associated count. Typically used to represent entries in a histogram or similar data structures.
     *
     * @param <T> the type of the key
     * @param key the key associated with this entry
     * @param count the count or frequency associated with the key
     */
    public record Entry<T>(T key, long count) {}

    /**
     * Returns a stream of entries representing the items stored in the histogram
     * along with their respective counts. Each entry includes the item (key) and
     * its associated count.
     *
     * <p>
     * The stream is not ordered.
     *
     * @return a stream of {@code Entry} objects, where each entry contains a key
     *         and its associated count
     */
    public Stream<Entry<T>> entries() {
        return map.entrySet().stream().map(e -> new Entry<>(e.getKey(), e.getValue().get()));
    }
}
