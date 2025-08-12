package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A {@code CompactableSortedMap} is a specialized implementation of the {@link SortedMap} interface
 * that supports an optional "compact" mode. It internally manages two states: a mutable state backed
 * by a {@link TreeMap} and an immutable "compact" state backed by an {@code ImmutableSortedMap}.
 * This enables a transition from a mutable to a compact, memory-efficient structure when the data is finalized
 * and further modifications are not required.
 * <p>
 * If any mutating operation is done on a compacted map, it will transparently go back to mutable mode.
 *
 * @param <K> the type of keys maintained by this map; must implement {@link Comparable}
 * @param <V> the type of mapped values; can be nullable
 */
public final class CompactableSortedMap<K extends Comparable<K>, V extends @Nullable Object> implements SortedMap<K, V> {
    private SortedMap<K,V> map;

    /**
     * Constructs an instance of CompactableSortedMap with an initially empty TreeMap as its backing map.
     * The compact representation is initialized to null, indicating that the map is in its mutable state.
     */
    public CompactableSortedMap() {
        this.map = ImmutableSortedMap.emptyMap();
    }

    /**
     * Constructs a new instance of CompactableSortedMap and fills it with the content of the provided map.
     *
     * @param map the map to initialize the CompactableSortedMap
     */
    public CompactableSortedMap(Map<K, V> map) {
        if (map instanceof CompactableSortedMap<K, V> csm) {
            this.map = csm.isCompact() ? csm.map : new TreeMap<>(csm.map);
        } else {
            this.map = new TreeMap<>(map);
        }
    }

    /**
     * Checks if the map is in a compact or immutable state.
     *
     * @return {@code true} if the map is an instance of CompactableSortedMap in its compacted form,
     *         {@code false} otherwise.
     */
    public boolean isCompact() {
        return map instanceof ImmutableSortedMap;
    }

    private SortedMap<K,V> getBackingMap(boolean mutable) {
        if (mutable && isCompact()) {
            map = new TreeMap<>(map);
        }
        return map;
    }

    /**
     * Transitions the current state of the map from mutable to immutable.
     * This method creates an immutable, sorted representation of the map's current
     * content and assigns it to the compactMap field. It also clears the mutable map
     * by setting it to null, indicating that no further modifications can be made.
     * <p>
     * If the map is already compacted, no operation is performed.
     */
    public void compact() {
        if (!isCompact()) {
            map = new ImmutableSortedMap<>(map);
        }
    }

    // Non-mutating methods (mutable=false)

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Comparator<? super K> comparator() {
        return map.comparator();
    }

    @Override
    public K firstKey() {
        return map.firstKey();
    }

    @Override
    public K lastKey() {
        return map.lastKey();
    }

    @Override
    public SortedMap<K, V> headMap(K toKey) {
        return map.headMap(toKey);
    }

    @Override
    public SortedMap<K, V> tailMap(K fromKey) {
        return map.tailMap(fromKey);
    }

    @Override
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return map.subMap(fromKey, toKey);
    }

    // Mutating methods (mutable=true)

    @Override
    public V put(K key, V value) {
        return getBackingMap(true).put(key, value);
    }

    @Override
    public V remove(Object key) {
        return getBackingMap(true).remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        getBackingMap(true).putAll(m);
    }

    @Override
    public void clear() {
        map = ImmutableSortedMap.emptyMap();
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        // the hash has to be calculated manually because implementations may use different algorithms
        // the entry set is sorted, so this should yield the same result for both implementations
        int h = 1;
        for (var entry: map.entrySet()) {
            h = 31 * h + entry.getKey().hashCode() + 37 * Objects.hashCode(entry.getValue());
        }
        return 0;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Map m) || m.size() != size()) {
            return false;
        }

        if (obj instanceof CompactableSortedMap<?, ?> other && other.isCompact() == isCompact()) {
            return map.equals(other.map);
        }

        for (Map.Entry<K, V> e : entrySet()) {
            if (!Objects.equals(m.get(e.getKey()), e.getValue())) {
                return false;
            }
        }
        return true;
    }
}
