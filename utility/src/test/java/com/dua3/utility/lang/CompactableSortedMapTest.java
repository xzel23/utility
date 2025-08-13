package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CompactableSortedMap} covering typical and corner cases.
 */
class CompactableSortedMapTest {

    private CompactableSortedMap<String, Integer> newFilledMap() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        // start compact and mutate to ensure decompaction works from the start
        map.put("b", 2);
        map.put("d", 4);
        map.put("a", 1);
        map.put("c", 3);
        return map; // currently mutable (TreeMap)
    }

    @Test
    void testEmptyConstructionAndState() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        // Starts compact and empty
        assertTrue(map.isCompact());
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("x"));
        // firstKey/lastKey on empty compact map throws AIOOBE (from ImmutableSortedMap)
        assertThrows(ArrayIndexOutOfBoundsException.class, map::firstKey);
        assertThrows(ArrayIndexOutOfBoundsException.class, map::lastKey);
    }

    @Test
    void testPutTriggersDecompactionAndBasicOps() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        // compact initially
        assertTrue(map.isCompact());
        // mutating should decompact transparently and succeed
        assertNull(map.put("b", 2));
        assertFalse(map.isCompact());
        assertEquals(1, map.size());
        assertEquals(2, map.get("b"));

        // overwrite existing
        Integer old = map.put("b", 20);
        assertEquals(2, old);
        assertEquals(20, map.get("b"));

        // add more
        map.put("a", 1);
        map.put("c", 3);
        assertEquals(3, map.size());
        assertEquals("a", map.firstKey());
        assertEquals("c", map.lastKey());
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(999));
    }

    @Test
    void testNullKeyRejectedAndNullValuesAllowed() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        // null key should throw (TreeMap semantics) – regardless of state
        Throwable t = assertThrows(Throwable.class, () -> map.put(null, 1));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
        // decompact and try again
        map.put("a", 1);
        t = assertThrows(Throwable.class, () -> map.put(null, 2));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
        // null values allowed
        map.put("b", null);
        assertTrue(map.containsKey("b"));
        assertNull(map.get("b"));
        assertTrue(map.containsValue(null));
        // distinguish absent vs present-null
        assertNull(map.get("z"));
        assertFalse(map.containsKey("z"));
    }

    @Test
    void testCompactAndThenMutate() {
        CompactableSortedMap<String, Integer> map = newFilledMap(); // mutable now
        map.compact();
        assertTrue(map.isCompact());
        assertEquals(4, map.size());
        assertEquals(1, map.get("a"));
        // non-mutating ops work in compact state
        assertTrue(map.containsKey("c"));
        assertTrue(map.containsValue(4));
        assertEquals("a", map.firstKey());
        assertEquals("d", map.lastKey());
        assertTrue(LangUtil.orNaturalOrder(map.comparator()).compare("a", "b") < 0);

        // mutating should decompact and still perform the operation
        Integer removed = map.remove("b");
        assertEquals(2, removed);
        assertFalse(map.isCompact());
        assertFalse(map.containsKey("b"));

        map.put("e", 5);
        assertEquals(5, map.get("e"));

        Map<String,Integer> more = Map.of("f", 6, "g", 7);
        map.putAll(more);
        assertEquals(6, map.get("f"));
        assertEquals(7, map.get("g"));
    }

    @Test
    void testClearResetsToEmptyCompact() {
        CompactableSortedMap<String, Integer> map = newFilledMap();
        assertFalse(map.isCompact());
        map.clear();
        // According to implementation, clear() sets to ImmutableSortedMap.emptyMap()
        assertTrue(map.isCompact());
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        // subsequent mutation should decompact
        map.put("x", 1);
        assertFalse(map.isCompact());
        assertEquals(1, map.size());
    }

    @Test
    void testViewsBehaviorHeadTailSubMap() {
        // Build deterministic ordered data
        TreeMap<String,Integer> baseline = new TreeMap<>();
        baseline.put("a", 1);
        baseline.put("c", 3);
        baseline.put("e", 5);
        baseline.put("g", 7);
        baseline.put("i", 9);

        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>(baseline);
        assertFalse(map.isCompact()); // constructed from Map -> TreeMap copy, mutable

        // Compare views with baseline while mutable
        SortedMap<String,Integer> head = map.headMap("f");
        SortedMap<String,Integer> tail = map.tailMap("d");
        SortedMap<String,Integer> sub  = map.subMap("c", "i");

        assertEquals(baseline.headMap("f"), new TreeMap<>(head));
        assertEquals(baseline.tailMap("d"), new TreeMap<>(tail));
        assertEquals(baseline.subMap("c", "i"), new TreeMap<>(sub));

        // Mutate via view while mutable – allowed and reflected in original
        head.put("b", 2);
        assertEquals(2, map.get("b"));
        tail.remove("g");
        assertFalse(map.containsKey("g"));
        sub.put("d", 4);
        assertEquals(4, map.get("d"));

        // Compact and verify views become immutable (from ImmutableSortedMap)
        map.compact();
        assertTrue(map.isCompact());
        SortedMap<String,Integer> head2 = map.headMap("z");
        SortedMap<String,Integer> tail2 = map.tailMap("a");
        SortedMap<String,Integer> sub2  = map.subMap("a", "z");
        assertThrows(UnsupportedOperationException.class, () -> head2.put("x", 24));
        assertThrows(UnsupportedOperationException.class, () -> tail2.remove("a"));
        assertThrows(UnsupportedOperationException.class, sub2::clear);
    }

    @Test
    void testKeySetValuesEntrySetMutabilityDependsOnState() {
        CompactableSortedMap<String, Integer> map = newFilledMap(); // mutable now
        Set<String> keys = map.keySet();
        Collection<Integer> values = map.values();
        Set<Map.Entry<String,Integer>> entries = map.entrySet();

        // Modify through views while mutable
        assertTrue(keys.remove("a"));
        assertFalse(map.containsKey("a"));

        // remove via values iterator (remove unsupported for TreeMap values collection directly, but via iterator is allowed)
        Iterator<Integer> it = values.iterator();
        while (it.hasNext()) {
            Integer v = it.next();
            if (Objects.equals(v, 4)) { // value for key 'd'
                it.remove();
                break;
            }
        }
        assertFalse(map.containsKey("d"));

        // remove via entries iterator
        Iterator<Map.Entry<String,Integer>> eit = entries.iterator();
        if (eit.hasNext()) {
            eit.next();
            eit.remove();
        }
        // size decreased by 3 total removals
        assertEquals(4 - 3, map.size());

        // Compact and ensure views are now immutable
        map.compact();
        assertTrue(map.isCompact());
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().add("zzz"));
        assertThrows(UnsupportedOperationException.class, () -> map.values().clear());
        assertThrows(UnsupportedOperationException.class, () -> {
            Iterator<Map.Entry<String,Integer>> it2 = map.entrySet().iterator();
            it2.next();
            it2.remove();
        });
    }

    @Test
    void testConstructorFromOtherCompactableSortedMap() {
        CompactableSortedMap<String, Integer> original = newFilledMap();
        original.compact();
        assertTrue(original.isCompact());

        CompactableSortedMap<String, Integer> copy1 = new CompactableSortedMap<>(original);
        // If source is compact, new map shares immutable backing and is also compact
        assertTrue(copy1.isCompact());
        assertEquals(original.size(), copy1.size());
        assertEquals(original.get("a"), copy1.get("a"));

        // Mutating copy should not affect original and should decompact
        copy1.put("z", 26);
        assertFalse(copy1.isCompact());
        assertEquals(26, copy1.get("z"));
        assertFalse(original.containsKey("z"));

        // If source is mutable, new map copies into its own TreeMap and remains mutable
        CompactableSortedMap<String, Integer> mutableSrc = newFilledMap();
        assertFalse(mutableSrc.isCompact());
        CompactableSortedMap<String, Integer> copy2 = new CompactableSortedMap<>(mutableSrc);
        assertFalse(copy2.isCompact());
        // Modifying copy should not affect source
        copy2.put("y", 25);
        assertTrue(copy2.containsKey("y"));
        assertFalse(mutableSrc.containsKey("y"));
    }

    @Test
    void testComparatorReflectsState() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        assertTrue(LangUtil.orNaturalOrder(map.comparator()).compare("a", "b") < 0);
        map.put("b", 2);
    }

    @Test
    void testBoundaryRangesAndAbsentKeysInViews() {
        CompactableSortedMap<String, Integer> map = new CompactableSortedMap<>();
        map.put("b", 2);
        map.put("d", 4);
        map.put("f", 6);
        map.put("h", 8);
        map.put("j", 10);
        // mutable state
        assertTrue(map.subMap("b", "b").isEmpty()); // empty range
        assertTrue(map.headMap("a").isEmpty());
        assertTrue(map.tailMap("k").isEmpty());

        // Compare to baseline TreeMap for ranges with non-existent bounds
        TreeMap<String,Integer> baseline = new TreeMap<>();
        baseline.putAll(Map.of("b",2,"d",4,"f",6,"h",8,"j",10));
        assertEquals(new TreeMap<>(baseline.subMap("a","e")), new TreeMap<>(map.subMap("a","e")));
        assertEquals(new TreeMap<>(baseline.subMap("e","k")), new TreeMap<>(map.subMap("e","k")));
        assertEquals(new TreeMap<>(baseline.subMap("c","i")), new TreeMap<>(map.subMap("c","i")));
        assertEquals(new TreeMap<>(baseline.subMap("a","k")), new TreeMap<>(map.subMap("a","k")));

        // Compact and recheck a couple of edge views
        map.compact();
        assertTrue(map.isCompact());
        assertTrue(map.subMap("b", "b").isEmpty());
        assertEquals(0, map.headMap("a").size());
        assertEquals(0, map.tailMap("k").size());
    }

    @Test
    void testEqualsBetweenCompactedAndNonCompactedInstances() {
        // m1 mutable
        CompactableSortedMap<String, Integer> m1 = new CompactableSortedMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        m1.put("c", 3);
        assertFalse(m1.isCompact());

        // m2 compact with same entries
        CompactableSortedMap<String, Integer> m2 = new CompactableSortedMap<>();
        m2.put("a", 1);
        m2.put("b", 2);
        m2.put("c", 3);
        m2.compact();
        assertTrue(m2.isCompact());

        // Equal by content, despite different compact state
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));

        // Change a value in m1 (size unchanged) -> now unequal
        m1.put("b", 20);
        assertFalse(m1.equals(m2));
        assertFalse(m2.equals(m1));
    }

    @Test
    void testEqualsAgainstHashMap() {
        CompactableSortedMap<String, Integer> csm = new CompactableSortedMap<>();
        csm.put("x", 24);
        csm.put("y", 25);
        csm.put("z", 26);
        csm.compact();

        Map<String, Integer> hm = new HashMap<>();
        hm.put("x", 24);
        hm.put("y", 25);
        hm.put("z", 26);

        // Same entries -> equal in both directions
        assertTrue(csm.equals(hm));
        assertTrue(hm.equals(csm));

        // Different entries -> not equal
        hm.put("w", 23); // extra entry
        assertNotEquals(csm, hm);
        assertNotEquals(hm, csm);

        // Change a value -> not equal
        hm.remove("w");
        hm.put("y", 99);
        assertNotEquals(csm, hm);
        assertNotEquals(hm, csm);
    }
}
