package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheTest {

    @Test
    void testCacheSoftReferences() {
        AtomicInteger computeCount = new AtomicInteger(0);
        Cache<String, String> cache = new Cache<>(Cache.ReferenceType.SOFT_REFERENCES, key -> {
            computeCount.incrementAndGet();
            return "value_for_" + key;
        });

        assertEquals("value_for_a", cache.get("a"));
        assertEquals(1, computeCount.get());

        // Repeated get returns cached value without recomputing
        assertEquals("value_for_a", cache.get("a"));
        assertEquals(1, computeCount.get());

        assertEquals("value_for_b", cache.get("b"));
        assertEquals(2, computeCount.get());

        assertTrue(cache.toString().contains("Cache backed by"));
    }

    @Test
    void testCacheWeakReferences() {
        AtomicInteger computeCount = new AtomicInteger(0);
        Cache<Integer, String> cache = new Cache<>(Cache.ReferenceType.WEAK_REFERENCES, key -> {
            computeCount.incrementAndGet();
            return "num_" + key;
        });

        assertEquals("num_1", cache.get(1));
        assertEquals("num_1", cache.get(1));
        assertEquals(1, computeCount.get());
        assertNotNull(cache.toString());
    }

    @Test
    void clearRemovesEntriesAndRecomputesValues() {
        AtomicInteger computeCount = new AtomicInteger(0);
        Cache<String, Object> cache = new Cache<>(Cache.ReferenceType.SOFT_REFERENCES, key -> {
            computeCount.incrementAndGet();
            return new Object();
        });

        Object first = cache.get("key");
        assertEquals(1, computeCount.get());
        assertTrue(cache.toString().contains("[1 entries]"));

        cache.clear();

        assertTrue(cache.toString().contains("[0 entries]"));
        Object second = cache.get("key");
        assertEquals(2, computeCount.get());
        assertTrue(first != second);
    }
}
