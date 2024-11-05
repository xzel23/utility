package com.dua3.utility.data;

import com.dua3.utility.data.Cache.ReferenceType;

import java.util.function.Function;

/**
 * A simple object cache used to avoid holding unnecessary instances of classes with value semantics in memory.
 *
 * <p>The class was initially created for a spreadsheet implementation where sometimes thousands of identical objects
 * with value semantics were stored when a spreadsheet was read from a data source. It helps to reduce memory
 * footprint (at least until Valhalla arrives), but not the cost of creating new instances.
 *
 * <p>
 * Usage:
 * <pre>{@code
 *     ObjectCache cache = new ObjectCache();
 *     T a = cache.get(new A()); // will return the previously cached instance of A if it exists in the cache
 * }</pre>
 */
public class ObjectCache {

    private final Cache<Object, Object> cache;

    /**
     * Initializes a new instance of the ObjectCache class.
     * This constructor initializes the cache using a function identity mapper.
     */
    public ObjectCache() {
        cache = new Cache<>(ReferenceType.WEAK_REFERENCES, Function.identity());
    }

    /**
     * Retrieves an item from the cache.
     *
     * @param <T>  the type of the item to retrieve from the cache
     * @param item the item to retrieve from the cache
     * @return the item retrieved from the cache, or null if the item is not present in the cache
     */
    @SuppressWarnings("unchecked")
    public <T> T get(T item) {
        return (T) cache.get(item);
    }
}
