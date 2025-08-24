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
public final class ImmutableSortedMap<K, V extends @Nullable Object> implements SortedMap<K, V> {

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
    @SuppressWarnings("unchecked")
    record Entry<K, V>(K getKey, @Nullable V getValue, @Nullable Comparator<? super K> comparator)
            implements Map.Entry<K, V> {
        @Override
        public @Nullable Object setValue(@Nullable Object value) {
            throw new UnsupportedOperationException("the collection is immutable");
        }
    }

    @SuppressWarnings("unchecked")
    private static final ImmutableSortedMap<?, ?> EMPTY_MAP = new ImmutableSortedMap<>(new Comparable[0], new Object[0], null);

    private final K[] keys;
    private final V[] values;
    private final @Nullable Comparator<? super K> comparator;
    private int hash;

    /**
     * Returns an empty immutable sorted map. This map is guaranteed to have no entries.
     *
     * @param <K> the type of the keys, which must be comparable
     * @param <V> the type of the values, which can be nullable
     * @return an empty {@code ImmutableSortedMap} instance
     */
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>, V extends @Nullable Object> ImmutableSortedMap<K, V> emptyMap() {
        return (ImmutableSortedMap<K, V>) EMPTY_MAP;
    }

    /**
     * Construct a new immutable sorted map from the given map.
     *
     * @param map the map to copy
     */
    @SuppressWarnings("unchecked")
    public ImmutableSortedMap(Map<K, V> map) {
        this(
                getArrayOfEntries(map, map instanceof SortedMap<?, ?> sm ? (Comparator<? super K>) sm.comparator() : null),
                map instanceof SortedMap<?, ?> sm ? (Comparator<? super K>) sm.comparator() : null,
                map instanceof SortedMap<?, ?>
        );
    }

    @SuppressWarnings("unchecked")
    private ImmutableSortedMap(
            Map.Entry<K, V>[] entries,
            @Nullable Comparator<? super K> comparator,
            boolean isSorted
    ) {
        if (!isSorted) {
            entries = entries.clone();
            Arrays.sort(entries, Map.Entry.comparingByKey(LangUtil.orNaturalOrder(comparator)));
        }
        this.keys = (K[]) new Comparable[entries.length];
        this.values = (V[]) new Object[entries.length];
        this.comparator = comparator;
        for (int i = 0; i < entries.length; i++) {
            keys[i] = entries[i].getKey();
            values[i] = entries[i].getValue();
        }
        assert keysAreSortedAndUnique() : "keys are not sorted or not unique";
    }

    private <T extends Comparable<T>> ImmutableSortedMap(
            K[] keys,
            V[] values,
            @Nullable Comparator<? super K> comparator
    ) {
        this.keys = keys;
        this.values = values;
        this.comparator = comparator;
        assert keysAreSortedAndUnique() : "keys are not sorted or not unique";
    }

    private boolean keysAreSortedAndUnique() {
        Comparator<? super K> cmp = LangUtil.orNaturalOrder(comparator);
        for (int i = 1; i < keys.length; i++) {
            if (cmp.compare(keys[i - 1], keys[i]) >= 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <K, V extends @Nullable Object> Entry<K, V>[] getArrayOfEntries(
            Map<K, V> map,
            @Nullable Comparator<? super K> comparator
    ) {
        List<Entry<K, V>> entries = new ArrayList<>(map.size());
        map.forEach((k, v) -> entries.add(new Entry<>(k, v, comparator)));
        return entries.toArray(Entry[]::new);
    }

    @Override
    public @Nullable Comparator<? super K> comparator() {
        return comparator;
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

    @SuppressWarnings("unchecked")
    private ImmutableSortedMap<K, V> subMapHelper(int start, int end) {
        if (start == end) {
            return (ImmutableSortedMap<K, V>) emptyMap();
        }
        if (end - start == keys.length) {
            return this;
        }
        return new ImmutableSortedMap<>(
                Arrays.copyOfRange(keys, start, end),
                Arrays.copyOfRange(values, start, end),
                comparator
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

    @Override
    public K firstKey() {
        return keys[0];
    }

    @Override
    public K lastKey() {
        return keys[keys.length - 1];
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
        for (Object v : values()) {
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
        Comparator<? super K> cmp = LangUtil.orNaturalOrder(comparator);
        @SuppressWarnings("unchecked")
        K k = (K) key;
        return Arrays.binarySearch(keys, k, cmp);
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
        return Set.of(keys);
    }

    @Override
    public Collection<V> values() {
        return LangUtil.asUnmodifiableList(values);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SortedSet<Map.Entry<K, V>> entrySet() {
        Entry[] entries = new Entry[keys.length];
        for (int i = 0; i < keys.length; i++) {
            entries[i] = new Entry<>(keys[i], values[i], comparator);
        }
        Comparator<? super Object> cmp = (Comparator<? super Object>) LangUtil.orNaturalOrder(comparator);
        return new ImmutableListBackedSortedSet(entries, Map.Entry.comparingByKey(cmp));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return switch (o) {
            case null -> false;
            case ImmutableSortedMap<?, ?> ism ->
                    Objects.deepEquals(keys, ism.keys) && Objects.deepEquals(values, ism.values);
            case SortedMap<?, ?> sm ->
                    Objects.deepEquals(keys, sm.keySet().toArray()) && Objects.deepEquals(values, sm.values().toArray());
            case Map<?, ?> m -> {
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
