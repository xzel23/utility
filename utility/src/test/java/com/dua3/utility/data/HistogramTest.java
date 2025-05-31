package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Histogram}.
 */
class HistogramTest {

    @Test
    void testCreateHashBased() {
        // Create a hash-based histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Add some items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");

        // Test counts
        assertEquals(2, histogram.getCount("apple"));
        assertEquals(1, histogram.getCount("banana"));
        assertEquals(0, histogram.getCount("orange")); // Not in histogram
    }

    @Test
    void testCreateIdentityBased() {
        // Create an identity-based histogram
        Histogram<String> histogram = Histogram.createIdentityBased();

        // Add some items
        String apple1 = new StringBuilder("apple").toString();
        String apple2 = new StringBuilder("apple").toString();
        String banana = "banana";

        histogram.add(apple1);
        histogram.add(apple2);
        histogram.add(banana);

        // Test counts - apple1 and apple2 are different objects
        assertEquals(1, histogram.getCount(apple1));
        assertEquals(1, histogram.getCount(apple2));
        assertEquals(1, histogram.getCount(banana));
        assertEquals(0, histogram.getCount("orange")); // Not in histogram
    }

    @Test
    void testAdd() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Add items
        histogram.add("apple");
        assertEquals(1, histogram.getCount("apple"));

        // Add the same item again
        histogram.add("apple");
        assertEquals(2, histogram.getCount("apple"));

        // Add a different item
        histogram.add("banana");
        assertEquals(1, histogram.getCount("banana"));
    }

    @Test
    void testGetMax() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Test empty histogram
        Optional<String> max = histogram.getMax();
        assertTrue(max.isEmpty());

        // Add items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");
        histogram.add("orange");
        histogram.add("apple");

        // Test max
        max = histogram.getMax();
        assertTrue(max.isPresent());
        assertEquals("apple", max.get());
    }

    @Test
    void testGetMaxEntry() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Test empty histogram
        Optional<Map.Entry<String, Counter>> maxEntry = histogram.getMaxEntry();
        assertTrue(maxEntry.isEmpty());

        // Add items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");
        histogram.add("orange");
        histogram.add("apple");

        // Test max entry
        maxEntry = histogram.getMaxEntry();
        assertTrue(maxEntry.isPresent());
        assertEquals("apple", maxEntry.get().getKey());
        assertEquals(3, maxEntry.get().getValue().get());
    }

    @Test
    void testGetCount() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Test count for non-existent item
        assertEquals(0, histogram.getCount("apple"));

        // Add items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");

        // Test counts
        assertEquals(2, histogram.getCount("apple"));
        assertEquals(1, histogram.getCount("banana"));
        assertEquals(0, histogram.getCount("orange")); // Not in histogram
    }

    @Test
    void testGetKeys() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Test keys for empty histogram
        Collection<String> keys = histogram.getKeys();
        assertTrue(keys.isEmpty());

        // Add items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");

        // Test keys
        keys = histogram.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("apple"));
        assertTrue(keys.contains("banana"));

        // Verify keys collection is unmodifiable
        Collection<String> finalKeys = keys;
        assertThrows(UnsupportedOperationException.class, () -> finalKeys.add("orange"));
    }

    @Test
    void testEntries() {
        // Create a histogram
        Histogram<String> histogram = Histogram.createHashBased();

        // Add items
        histogram.add("apple");
        histogram.add("banana");
        histogram.add("apple");

        // Test entries
        var entries = histogram.entries().toList();
        assertEquals(2, entries.size());

        // Check entries
        for (Histogram.Entry<String> entry : entries) {
            if ("apple".equals(entry.key())) {
                assertEquals(2, entry.count());
            } else if ("banana".equals(entry.key())) {
                assertEquals(1, entry.count());
            } else {
                fail("Unexpected entry: " + entry.key());
            }
        }
    }
}
