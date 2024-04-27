// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT
package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    @Test
    void addToMap_varargs() {
        Map<String, Integer> map = new HashMap<>();
        Pair<String, Integer> pair1 = Pair.of("one", 1);
        Pair<String, Integer> pair2 = Pair.of("two", 2);

        Pair.addToMap(map, pair1, pair2);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
    }

    @Test
    void addToMap_iterable() {
        Map<String, Integer> map = new HashMap<>();
        Iterable<Pair<String, Integer>> pairs = List.of(Pair.of("one", 1), Pair.of("two", 2));

        Pair.addToMap(map, pairs);

        assertEquals(1, map.get("one"));
        assertEquals(2, map.get("two"));
    }

    @Test
    void of() {
        Pair<String, Integer> pair = Pair.of("one", 1);

        assertEquals("one", pair.first());
        assertEquals(1, pair.second());
    }

    @Test
    void ofEntry() {
        Map.Entry<String, Integer> entry = Map.entry("one", 1);
        Pair<String, Integer> pair = Pair.of(entry);

        assertEquals("one", pair.first());
        assertEquals(1, pair.second());
    }

    @Test
    void ofArray() {
        String[] second = {"two", "three"};
        Pair<String,String[]> pair = Pair.ofArray("one",second);

        assertEquals("one", pair.first());
        assertArrayEquals(second,pair.second());
    }

    @Test
    void map() {
        Pair<String, String> pair1 = Pair.of("one", "two");

        Function<Object, Integer> mappings = str -> str.toString().length();

        Pair<Integer,Integer> pair2 = pair1.map(mappings);

        assertEquals(3, pair2.first());
        assertEquals(3, pair2.second());

    }

    @Test
    void mapFirst() {
        Pair<String, String> pair1 = Pair.of("one", "two");

        Function<Object, Integer> mappings = str -> str.toString().length();

        Pair<Integer,String> pair2 = pair1.mapFirst(mappings);

        assertEquals(3, pair2.first());
        assertEquals("two",pair2.second());
    }

    @Test
    void mapSecond() {
        Pair<String, String> pair1 = Pair.of("one", "two");

        Function<Object, Integer> mappings = str -> str.toString().length();

        Pair<String,Integer> pair2 = pair1.mapSecond(mappings);

        assertEquals("one",pair2.first());
        assertEquals(3, pair2.second());
    }

    @Test
    void getKey() {

        Pair<String, Integer> pair = Pair.of("one", 1);

        assertEquals("one", pair.getKey());
    }

    @Test
    void getValue() {
        Pair<String, Integer> pair = Pair.of("one", 1);

        assertEquals(1, pair.getValue());
    }
    @Test
    void setValue_unsupportedOperationException() {
        Pair<String, Integer> pair = Pair.of("one", 1);

        assertThrows(UnsupportedOperationException.class, () -> pair.setValue(2));
    }
}