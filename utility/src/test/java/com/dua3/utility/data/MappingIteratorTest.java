package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingIteratorTest {
    @Test
    void testNext() {
        // setup
        Iterator<Integer> integers = Arrays.asList(1, 2, 3).iterator();
        Function<Integer, Integer> doubling = i -> i * 2;
        MappingIterator<Integer, Integer> mappingIterator = new MappingIterator<>(integers, doubling);

        // execute & validate
        assertEquals(2, (int) mappingIterator.next());
        assertEquals(4, (int) mappingIterator.next());
        assertEquals(6, (int) mappingIterator.next());
    }

    @Test
    void testHasNextWithElements() {
        // setup
        Iterator<String> strings = Arrays.asList("A", "B", "C").iterator();
        Function<String, String> repeat = s -> s.repeat(2);
        MappingIterator<String, String> mappingIterator = new MappingIterator<>(strings, repeat);

        // execute & validate
        assertTrue(mappingIterator.hasNext());
    }

    @Test
    void testHasNextWithoutElements() {
        // setup
        Iterator<String> emptyIterator = List.<String>of().iterator();
        Function<String, String> repeat = s -> s.repeat(2);
        MappingIterator<String, String> mappingIterator = new MappingIterator<>(emptyIterator, repeat);

        // execute & validate
        assertFalse(mappingIterator.hasNext());
    }
}