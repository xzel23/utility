package com.dua3.utility.lang;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SortedSet implementations.
 * This class tests all methods of the SortedSet interface using parameterized tests.
 */
class SortedSetTest {

    /**
     * Provides a stream of factory methods that create SortedSet instances.
     * Each factory method takes a List of elements and returns a SortedSet.
     *
     * @return a stream of factory methods
     */
    private static Stream<Function<List<Integer>, SortedSet<Integer>>> sortedSetFactories() {
        return Stream.of(
                // Factory method that creates an ImmutableListBackedSortedSet
                list -> {
                    Integer[] array = list.toArray(new Integer[0]);
                    return ImmutableListBackedSortedSet.ofNaturalOrder(array);
                }
                // Add more factory methods for other SortedSet implementations if needed
        );
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testSize(Function<List<Integer>, SortedSet<Integer>> factory) {
        // Empty set
        SortedSet<Integer> emptySet = factory.apply(List.of());
        assertEquals(0, emptySet.size());

        // Set with one element
        SortedSet<Integer> singletonSet = factory.apply(List.of(1));
        assertEquals(1, singletonSet.size());

        // Set with multiple elements
        SortedSet<Integer> multipleSet = factory.apply(List.of(1, 2, 3, 4, 5));
        assertEquals(5, multipleSet.size());

        // Set with duplicate elements (should be removed in a set)
        SortedSet<Integer> duplicateSet = factory.apply(List.of(1, 2, 2, 3, 3, 3));
        assertEquals(3, duplicateSet.size());
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testIsEmpty(Function<List<Integer>, SortedSet<Integer>> factory) {
        // Empty set
        SortedSet<Integer> emptySet = factory.apply(List.of());
        assertTrue(emptySet.isEmpty());

        // Non-empty set
        SortedSet<Integer> nonEmptySet = factory.apply(List.of(1));
        assertFalse(nonEmptySet.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testContains(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3, 4, 5));

        // Test elements that are in the set
        assertTrue(set.contains(1));
        assertTrue(set.contains(3));
        assertTrue(set.contains(5));

        // Test elements that are not in the set
        assertFalse(set.contains(0));
        assertFalse(set.contains(6));
        assertThrows(Throwable.class, () -> set.contains(null));
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testIterator(Function<List<Integer>, SortedSet<Integer>> factory) {
        List<Integer> elements = List.of(1, 2, 3, 4, 5);
        SortedSet<Integer> set = factory.apply(elements);

        // Test iterator returns elements in sorted order
        Iterator<Integer> iterator = set.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            assertEquals(elements.get(index++), iterator.next());
        }
        assertEquals(elements.size(), index);

        // Test iterator of empty set
        SortedSet<Integer> emptySet = factory.apply(List.of());
        Iterator<Integer> emptyIterator = emptySet.iterator();
        assertFalse(emptyIterator.hasNext());
        assertThrows(NoSuchElementException.class, emptyIterator::next);
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testToArray(Function<List<Integer>, SortedSet<Integer>> factory) {
        List<Integer> elements = List.of(1, 2, 3, 4, 5);
        SortedSet<Integer> set = factory.apply(elements);

        // Test toArray()
        Object[] array = set.toArray();
        assertEquals(elements.size(), array.length);
        for (int i = 0; i < elements.size(); i++) {
            assertEquals(elements.get(i), array[i]);
        }

        // Test toArray(T[] a) with array of exact size
        Integer[] typedArray = new Integer[elements.size()];
        Integer[] resultArray = set.toArray(typedArray);
        assertSame(typedArray, resultArray); // Should return the same array if it fits
        for (int i = 0; i < elements.size(); i++) {
            assertEquals(elements.get(i), resultArray[i]);
        }

        // Test toArray(T[] a) with array that's too small
        Integer[] smallArray = new Integer[2];
        Integer[] expandedArray = set.toArray(smallArray);
        assertNotSame(smallArray, expandedArray); // Should return a new array
        assertEquals(elements.size(), expandedArray.length);
        for (int i = 0; i < elements.size(); i++) {
            assertEquals(elements.get(i), expandedArray[i]);
        }
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testContainsAll(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3, 4, 5));

        // Test with collection that is a subset
        assertTrue(set.containsAll(List.of(1, 3, 5)));
        assertTrue(set.containsAll(List.of(1)));
        assertTrue(set.containsAll(List.of()));

        // Test with collection that is not a subset
        assertFalse(set.containsAll(List.of(1, 6)));
        assertFalse(set.containsAll(List.of(0, 1, 2)));
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testComparator(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3));

        // For natural ordering, comparator should be null or return natural order comparator
        Comparator<? super Integer> comparator = LangUtil.orNaturalOrder(set.comparator());
        assertEquals(0, comparator.compare(1, 1));
        assertTrue(comparator.compare(1, 2) < 0);
        assertTrue(comparator.compare(2, 1) > 0);
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testFirst(Function<List<Integer>, SortedSet<Integer>> factory) {
        // Test with non-empty set
        SortedSet<Integer> set = factory.apply(List.of(5, 3, 1, 4, 2));
        assertEquals(1, set.first()); // Should be the smallest element

        // Test with a singleton set
        SortedSet<Integer> singletonSet = factory.apply(List.of(42));
        assertEquals(42, singletonSet.first());

        // Test with an empty set
        SortedSet<Integer> emptySet = factory.apply(List.of());
        assertThrows(NoSuchElementException.class, emptySet::first);
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testLast(Function<List<Integer>, SortedSet<Integer>> factory) {
        // Test with non-empty set
        SortedSet<Integer> set = factory.apply(List.of(5, 3, 1, 4, 2));
        assertEquals(5, set.last()); // Should be the largest element

        // Test with singleton set
        SortedSet<Integer> singletonSet = factory.apply(List.of(42));
        assertEquals(42, singletonSet.last());

        // Test with empty set
        SortedSet<Integer> emptySet = factory.apply(List.of());
        assertThrows(NoSuchElementException.class, emptySet::last);
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testSubSet(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3, 4, 5));

        // Test normal subset
        SortedSet<Integer> subset = set.subSet(2, 5);
        assertEquals(3, subset.size());
        assertTrue(subset.contains(2));
        assertTrue(subset.contains(3));
        assertTrue(subset.contains(4));
        assertFalse(subset.contains(1));
        assertFalse(subset.contains(5));

        // Test empty subset
        SortedSet<Integer> emptySubset = set.subSet(2, 2);
        assertTrue(emptySubset.isEmpty());

        // Test subset with fromElement > toElement
        assertThrows(IllegalArgumentException.class, () -> set.subSet(5, 2));
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testHeadSet(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3, 4, 5));

        // Test normal headSet
        SortedSet<Integer> headSet = set.headSet(3);
        assertEquals(2, headSet.size());
        assertTrue(headSet.contains(1));
        assertTrue(headSet.contains(2));
        assertFalse(headSet.contains(3));
        assertFalse(headSet.contains(4));
        assertFalse(headSet.contains(5));

        // Test empty headSet
        SortedSet<Integer> emptyHeadSet = set.headSet(1);
        assertTrue(emptyHeadSet.isEmpty());

        // Test headSet with toElement greater than any element
        SortedSet<Integer> fullHeadSet = set.headSet(10);
        assertEquals(5, fullHeadSet.size());
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testTailSet(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3, 4, 5));

        // Test normal tailSet
        SortedSet<Integer> tailSet = set.tailSet(3);
        assertEquals(3, tailSet.size());
        assertFalse(tailSet.contains(1));
        assertFalse(tailSet.contains(2));
        assertTrue(tailSet.contains(3));
        assertTrue(tailSet.contains(4));
        assertTrue(tailSet.contains(5));

        // Test empty tailSet
        SortedSet<Integer> emptyTailSet = set.tailSet(10);
        assertTrue(emptyTailSet.isEmpty());

        // Test tailSet with fromElement less than any element
        SortedSet<Integer> fullTailSet = set.tailSet(0);
        assertEquals(5, fullTailSet.size());
    }

    @ParameterizedTest
    @MethodSource("sortedSetFactories")
    void testUnsupportedOperations(Function<List<Integer>, SortedSet<Integer>> factory) {
        SortedSet<Integer> set = factory.apply(List.of(1, 2, 3));

        // Test unsupported add operation
        assertThrows(UnsupportedOperationException.class, () -> set.add(4));

        // Test unsupported remove operation
        assertThrows(UnsupportedOperationException.class, () -> set.remove(1));

        // Test unsupported addAll operation
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(List.of(4, 5)));

        // Test unsupported removeAll operation
        assertThrows(UnsupportedOperationException.class, () -> set.removeAll(List.of(1, 2)));

        // Test unsupported retainAll operation
        assertThrows(UnsupportedOperationException.class, () -> set.retainAll(List.of(1, 2)));

        // Test unsupported clear operation
        assertThrows(UnsupportedOperationException.class, set::clear);
    }
}
