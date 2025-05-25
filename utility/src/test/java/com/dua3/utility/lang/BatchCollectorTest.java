package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This is a test class for the BatchCollector class.
 * It aims to tests the functionality of the methods within the BatchCollector class.
 */
class BatchCollectorTest {

    /**
     * This test is for testing the functionality of BatchCollector when generating grouping.
     */
    @Test
    public void batchCollector_groupingGenerationTest() {
        BatchCollector<String, Integer> bc = new BatchCollector<>(s -> s != null ? s.length() : null);
        var result = Stream.of("one", "two", "three", "four", "five", "six").collect(bc);

        assertEquals(4, result.size());
        assertEquals(3, result.get(0).first().intValue());
        assertEquals(List.of("one", "two"), result.get(0).second());
        assertEquals(5, result.get(1).first().intValue());
        assertEquals(List.of("three"), result.get(1).second());
        assertEquals(4, result.get(2).first().intValue());
        assertEquals(List.of("four", "five"), result.get(2).second());
        assertEquals(3, result.get(3).first().intValue());
        assertEquals(List.of("six"), result.get(3).second());
    }

    /**
     * This test is for testing the functionality of BatchCollector when generating grouping with default key.
     */
    @Test
    public void batchCollector_defaultKeyGroupingGenerationTest() {
        BatchCollector<String, Integer> bc = new BatchCollector<>(s -> s != null ? s.length() : null, 0);
        var result = Stream.of("one", "two", "three", "four", "five", "six", null).collect(bc);

        assertEquals(5, result.size());
        assertEquals(3, result.get(0).first().intValue());
        assertEquals(List.of("one", "two"), result.get(0).second());
        assertEquals(5, result.get(1).first().intValue());
        assertEquals(List.of("three"), result.get(1).second());
        assertEquals(4, result.get(2).first().intValue());
        assertEquals(List.of("four", "five"), result.get(2).second());
        assertEquals(3, result.get(3).first().intValue());
        assertEquals(List.of("six"), result.get(3).second());
        assertEquals(0, result.get(4).first().intValue());
        assertEquals(Collections.singletonList(null), result.get(4).second());
    }

}