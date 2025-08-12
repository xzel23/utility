package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * A memory efficient immutable and sorted map implementation that maintains the natural
 * ordering of keys. This class does not allow modifications after initialization,
 * ensuring thread safety and predictability in concurrent environments.
 * <p>
 * The ImmutableSortedMap provides a consistent order based on the natural comparison
 * of keys, as defined by their {@code Comparable} implementation.
 *
 * @param <K> the type of keys in the map, which must implement {@link Comparable}
 * @param <V> the type of values in the map
 */
public final class ImmutableSortedMap<K extends Comparable<K>, V extends @Nullable Object> implements SortedMap<K, V> {

    /**
     * A record that represents an immutable key-value pair entry. This record associates a key
     * with a value and enforces immutability, meaning the value cannot be changed once the
     * entry is created. The key must be of a type that implements {@link Comparable}, allowing
     * for comparisons between entries based on their keys.
     * <p>
     * Implements the {@link Map.Entry} interface to provide behavior similar to standard map
     * entries and the {@link Comparable} interface to enable comparison between instances of
     * this record.
     *
     * @param <K> The type of the key, which must implement {@link Comparable}.
     * @param <V> The type of the value, which can be any nullable object.
     */
    record Entry<K extends Comparable<K>, V extends @Nullable Object>(K getKey, V getValue)
            implements Map.Entry<K, V>, Comparable<Entry<K, V>> {
        @SuppressWarnings("unchecked")
        @Override
        public @Nullable Object setValue(@Nullable Object value) {
            throw new UnsupportedOperationException("the collection is immutable");
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(Entry o) {
            return getKey().compareTo((K) o.getKey());
        }
    }

    private static final ImmutableSortedMap<?, ?> EMPTY_MAP = new ImmutableSortedMap<>(new Comparable[0], new Object[0]);

    private final K[] keys;
    private final V[] values;
    private int hash;

    /**
     * Returns an empty immutable sorted map. This map is guaranteed to have no entries.
     *
     * @param <K> the type of the keys, which must be comparable
     * @param <V> the type of the values, which can be nullable
     * @return an empty {@code ImmutableSortedMap} instance
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>, V extends @Nullable Object> ImmutableSortedMap<K,V> emptyMap() {
        return (ImmutableSortedMap<K, V>) EMPTY_MAP;
    }

    /**
     * Construct a new immutable sorted map from the given map.
     *
     * @param map the map to copy
     */
    public ImmutableSortedMap(Map<K, V> map) {
        this(getArrayOfEntries(map), map instanceof SortedMap<?,?> sm && sm.comparator() == Comparator.naturalOrder());
    }

    private ImmutableSortedMap(Map.Entry<K, V>[] entries, boolean isSorted) {
        if (!isSorted) {
            entries = entries.clone();
            Arrays.sort(entries, Map.Entry.comparingByKey());
        }
        this.keys = (K[]) new Comparable[entries.length];
        this.values = (V[]) new Object[entries.length];
        for (int i = 0; i < entries.length; i++) {
            this.keys[i] = entries[i].getKey();
            this.values[i] = entries[i].getValue();
        }
        assert keyAreSortedAndUnique() : "keys are not sorted or not unique";
    }

    private <T extends Comparable<T>> ImmutableSortedMap(
            K[] keys,
            V[] values) {
        this.keys = keys;
        this.values = values;
        assert keyAreSortedAndUnique() : "keys are not sorted or not unique";
    }

    private boolean keyAreSortedAndUnique() {
        for (int i=1; i<keys.length; i++) {
            if ((keys[i-1]).compareTo(keys[i]) >= 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <K extends Comparable<K>, V extends @Nullable Object> Entry<K, V>[] getArrayOfEntries(Map<K, V> map) {
        List<Entry<K,V>> entries = new ArrayList<>(map.size());
        map.forEach((k, v) -> entries.add(new Entry<>(k, v)));
        return entries.toArray(Entry[]::new);
    }

    @Override
    public Comparator<? super K> comparator() {
        return Comparator.naturalOrder();
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        int start = keyIndex(fromKey);
        if (start < 0) {
            start = -start - 1;
        }
        int end = keyIndex(toKey);
        if (end < 0) {
            end = -end - 1;
        }

        return subMapHelper(start, end);
    }

    private ImmutableSortedMap<K, V> subMapHelper(int start, int end) {
        if (start == end) {
            return emptyMap();
        }
        if (end - start == keys.length) {
            return this;
        }
        return new ImmutableSortedMap<>(
                Arrays.copyOfRange(this.keys, start, end),
                Arrays.copyOfRange(this.values, start, end)
        );
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        int end = keyIndex(toKey);
        if (end < 0) {
            end = -end - 1;
        }
        return subMapHelper(0, end);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        int start = keyIndex(fromKey);
        if (start < 0) {
            start = -start - 1;
        }
        return subMapHelper(start, keys.length);
    }

    @SuppressWarnings("unchecked")
    @Override
    public K firstKey() {
        return (K) keys[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public K lastKey() {
        return (K) keys[keys.length - 1];
    }

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public boolean isEmpty() {
        return keys.length == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return keyIndex(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Object v: values()) {
            if (Objects.equals(v, value)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        int idx = keyIndex(key);
        return idx < 0 ? null : (V) values[idx];
    }

    private int keyIndex(Object key) {
        return Arrays.binarySearch(keys, key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        Object[] kKeys = keys;
        return Set.of((K[]) kKeys);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> values() {
        return LangUtil.asUnmodifiableList((V[]) values);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SortedSet<Map.Entry<K, V>> entrySet() {
        Entry[] entries = new Entry[keys.length];
        for (int i = 0; i < keys.length; i++) {
            entries[i] = new Entry<>(keys[i], values[i]);
        }
        return new ImmutableListBackedSortedSet(entries);
    }

    @Override
    public boolean equals(Object o) {
        return switch (o) {
            case null -> false;
            case ImmutableSortedMap<?, ?> ism ->
                    Objects.deepEquals(keys, ism.keys) && Objects.deepEquals(values, ism.values);
            case SortedMap<?,?> sm -> Objects.deepEquals(keys, sm.keySet().toArray()) && Objects.deepEquals(values, sm.values().toArray());
            case Map<?,?> m -> {
                if (m.size() != keys.length) {
                    yield false;
                }
                for (int i = 0; i < keys.length; i++) {
                    if (!Objects.equals(m.get(keys[i]), values[i])) {
                        yield false;
                    }
                }
                yield true;
            }
            default -> false;
        };
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (int i = 0; i < size(); i++) {
                h += Objects.hashCode(keys[i]) ^ Objects.hashCode(values[i]);
            }
            hash = h;
        }
        return h;
    }
}
