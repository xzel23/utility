/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A simple cache implementation.
 * <p>
 * NOTE: This class is not intended as a replacement for {@code JCache} (JSR 107).
 *
 * @param <K> key class
 * @param <V> value class
 */
public class Cache<K, V> {

    private static final Cleaner CLEANER = Cleaner.create();

    private final Function<V, Reference<V>> newReference;
    private final Function<? super K, ? extends V> compute;
    private final Map<K, Reference<V>> items = new ConcurrentHashMap<>();

    /**
     * Constructs a new Cache object with the given type and compute function.
     *
     * @param type    the type of the cache, either SOFT_REFERENCES or WEAK_REFERENCES
     * @param compute a function that computes the value for the given key if it is not already present in the cache
     */
    public Cache(ReferenceType type, Function<? super K, ? extends V> compute) {
        this.compute = compute;
        this.newReference = switch (type) {
            case SOFT_REFERENCES -> SoftReference::new;
            case WEAK_REFERENCES -> WeakReference::new;
        };
    }

    /**
     * Gets the value associated with the specified key.
     *
     * @param key the key whose associated value is to be retrieved
     * @return the value to which the specified key is mapped
     */
    public V get(K key) {
        // Fast path: optimistic read
        Reference<V> ref = items.get(key);
        V item = ref == null ? null : ref.get();
        if (item != null) {
            return item;
        }

        // Atomic path: use compute to lock only this key's bucket, not the whole cache
        AtomicReference<@Nullable V> holder = new AtomicReference<>();

        items.compute(key, (k, currentRef) -> {
            // 1. Check if another thread already computed it while we were waiting
            V val = currentRef == null ? null : currentRef.get();
            if (val != null) {
                holder.set(val);
                return currentRef; // Keep existing reference
            }

            // 2. Compute new value
            val = compute.apply(k);
            holder.set(val);

            // 3. Create new reference
            Reference<V> newRef = newReference.apply(val);

            // 4. Register cleaner with SAFE removal
            // We capture 'newRef' to ensure we only remove the entry if it still holds THIS reference
            CLEANER.register(val, () -> items.remove(k, newRef));

            return newRef;
        });

        V v = holder.get();
        assert v != null; // safe, because compute().apply(k) returns non null
        return v;
    }

    @Override
    public String toString() {
        return String.format("Cache backed by %s [%d entries]", items.getClass().getSimpleName(), items.size());
    }

    /**
     * Enum representing the different types of reference to be used in the Cache class.
     */
    public enum ReferenceType {

        /**
         * Use {@link SoftReference}.
         */
        SOFT_REFERENCES,

        /**
         * Use {@link WeakReference}.
         */
        WEAK_REFERENCES
    }
}
