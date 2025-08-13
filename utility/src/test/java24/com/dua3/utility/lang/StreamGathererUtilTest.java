package com.dua3.utility.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Test cases for StreamGathererUtil.
 */
class StreamGathererUtilTest {

    /**
     * Tests for the groupConsecutive method in StreamGathererUtil.
     * This method groups consecutive elements from a stream based on a given predicate.
     */

    @Test
    void testGroupConsecutive_AllMatching() {
        // Initialize test data
        List<Integer> input = List.of(1, 2, 3, 4);
        Predicate<Integer> predicate = n -> n > 0; // All numbers positive
        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = (group, element) -> {
            group.add(element);
            return group;
        };

        // Use groupConsecutive
        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.groupConsecutive(predicate, groupSupplier, accumulator))
                .toList();

        // Verify results
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(List.of(1, 2, 3, 4), result.getFirst());
    }

    @Test
    void testGroupConsecutive_NoneMatching() {
        // Initialize test data
        List<Integer> input = List.of(1, 2, 3, 4);
        Predicate<Integer> predicate = n -> n % 2 == 0; // No numbers positive
        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = (group, element) -> {
            group.add(element);
            return group;
        };

        // Use groupConsecutive
        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.groupConsecutive(predicate, groupSupplier, accumulator))
                .toList();

        // Verify results
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals(List.of(1), result.get(0));
        Assertions.assertEquals(List.of(2), result.get(1));
        Assertions.assertEquals(List.of(3), result.get(2));
        Assertions.assertEquals(List.of(4), result.get(3));
    }

    @Test
    void testGroupConsecutive_SplitGroups() {
        // Initialize test data
        List<Integer> input = List.of(1, 2, -1, -2, 3, 4);
        Predicate<Integer> predicate = n -> n > 0; // Positive and negative grouping
        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = (group, element) -> {
            group.add(element);
            return group;
        };

        // Use groupConsecutive
        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.groupConsecutive(predicate, groupSupplier, accumulator))
                .toList();

        // Verify results
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals(List.of(1, 2), result.get(0));
        Assertions.assertEquals(List.of(-1, -2), result.get(1));
        Assertions.assertEquals(List.of(3, 4), result.get(2));
    }

    @Test
    void testGroupConsecutive_SingleElementGroups() {
        // Initialize test data
        List<Integer> input = List.of(1, -1, 2, -2, 3, -3);
        Predicate<Integer> predicate = n -> n > 0; // Positive vs negative alternate
        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = (group, element) -> {
            group.add(element);
            return group;
        };

        // Use groupConsecutive
        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.groupConsecutive(predicate, groupSupplier, accumulator))
                .toList();

        // Verify results
        Assertions.assertEquals(6, result.size());
        Assertions.assertEquals(List.of(1), result.get(0));
        Assertions.assertEquals(List.of(-1), result.get(1));
        Assertions.assertEquals(List.of(2), result.get(2));
        Assertions.assertEquals(List.of(-2), result.get(3));
        Assertions.assertEquals(List.of(3), result.get(4));
        Assertions.assertEquals(List.of(-3), result.get(5));
    }

    @Test
    void testGroupConsecutive_EmptyInput() {
        // Initialize test data
        Predicate<Integer> predicate = n -> n > 0;
        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = (group, element) -> {
            group.add(element);
            return group;
        };

        // Use groupConsecutive
        List<List<Integer>> result = Stream.<Integer>empty()
                .gather(StreamGathererUtil.groupConsecutive(predicate, groupSupplier, accumulator))
                .toList();

        // Verify results
        Assertions.assertEquals(0, result.size());
    }
    
    // Helper accumulator that creates a new group when currentGroup is null
    private static BiFunction<List<Integer>, Integer, List<Integer>> listAccumulator(Supplier<List<Integer>> supplier) {
        return (group, element) -> {
            if (group == null) {
                group = supplier.get();
            }
            group.add(element);
            return group;
        };
    }

    // Tests for filterAndGroupConsecutive

    @Test
    void testFilterAndGroupConsecutive_BasicWithIgnore() {
        List<Integer> input = List.of(1, 2, 2, 3, 0, 4, 4);

        // IGNORE zeros, otherwise group equal consecutive numbers
        BiFunction<Integer, Integer, StreamGathererUtil.GroupingGathererAction> actionSelector = (prev, curr) -> {
            if (curr == 0) {
                return StreamGathererUtil.GroupingGathererAction.IGNORE;
            }
            if (prev == null) {
                return StreamGathererUtil.GroupingGathererAction.START_GROUP;
            }
            return curr.equals(prev)
                    ? StreamGathererUtil.GroupingGathererAction.JOIN_GROUP
                    : StreamGathererUtil.GroupingGathererAction.START_GROUP;
        };

        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = listAccumulator(groupSupplier);

        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.filterAndGroupConsecutive(actionSelector, groupSupplier, accumulator))
                .toList();

        // Expected: [1], [2,2], [3], [4,4] (0 is ignored)
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals(List.of(1), result.get(0));
        Assertions.assertEquals(List.of(2, 2), result.get(1));
        Assertions.assertEquals(List.of(3), result.get(2));
        Assertions.assertEquals(List.of(4, 4), result.get(3));
    }

    @Test
    void testFilterAndGroupConsecutive_DropResetsPrev() {
        List<Integer> input = List.of(1, 1, -1, 1);

        // DROP negatives; group equal consecutive positives
        BiFunction<Integer, Integer, StreamGathererUtil.GroupingGathererAction> actionSelector = (prev, curr) -> {
            if (curr < 0) {
                return StreamGathererUtil.GroupingGathererAction.DROP;
            }
            if (prev == null) {
                return StreamGathererUtil.GroupingGathererAction.START_GROUP;
            }
            return curr.equals(prev)
                    ? StreamGathererUtil.GroupingGathererAction.JOIN_GROUP
                    : StreamGathererUtil.GroupingGathererAction.START_GROUP;
        };

        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = listAccumulator(groupSupplier);

        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.filterAndGroupConsecutive(actionSelector, groupSupplier, accumulator))
                .toList();

        // Expected: [1,1], [1] (the -1 is dropped, resets prev)
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(List.of(1, 1), result.get(0));
        Assertions.assertEquals(List.of(1), result.get(1));
    }

    @Test
    void testFilterAndGroupConsecutive_AllDroppedOrIgnored() {
        List<Integer> input = List.of(0, -1, 0, -2, 0);

        BiFunction<Integer, Integer, StreamGathererUtil.GroupingGathererAction> actionSelector = (prev, curr) -> {
            if (curr == 0) {
                return StreamGathererUtil.GroupingGathererAction.IGNORE;
            }
            if (curr < 0) {
                return StreamGathererUtil.GroupingGathererAction.DROP;
            }
            return StreamGathererUtil.GroupingGathererAction.START_GROUP; // not reached by given input
        };

        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = listAccumulator(groupSupplier);

        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.filterAndGroupConsecutive(actionSelector, groupSupplier, accumulator))
                .toList();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testFilterAndGroupConsecutive_EmptyInput() {
        BiFunction<Integer, Integer, StreamGathererUtil.GroupingGathererAction> actionSelector = (prev, curr) ->
                StreamGathererUtil.GroupingGathererAction.START_GROUP;

        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = listAccumulator(groupSupplier);

        List<List<Integer>> result = Stream.<Integer>empty()
                .gather(StreamGathererUtil.filterAndGroupConsecutive(actionSelector, groupSupplier, accumulator))
                .toList();

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testFilterAndGroupConsecutive_FinalizeLastGroup() {
        List<Integer> input = List.of(5, 5);

        BiFunction<Integer, Integer, StreamGathererUtil.GroupingGathererAction> actionSelector = (prev, curr) -> {
            if (prev == null) {
                return StreamGathererUtil.GroupingGathererAction.START_GROUP;
            }
            return curr.equals(prev)
                    ? StreamGathererUtil.GroupingGathererAction.JOIN_GROUP
                    : StreamGathererUtil.GroupingGathererAction.START_GROUP;
        };

        Supplier<List<Integer>> groupSupplier = ArrayList::new;
        BiFunction<List<Integer>, Integer, List<Integer>> accumulator = listAccumulator(groupSupplier);

        List<List<Integer>> result = input.stream()
                .gather(StreamGathererUtil.filterAndGroupConsecutive(actionSelector, groupSupplier, accumulator))
                .toList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(List.of(5, 5), result.getFirst());
    }
}
