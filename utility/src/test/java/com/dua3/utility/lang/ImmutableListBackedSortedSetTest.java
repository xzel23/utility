package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link ImmutableListBackedSortedSet} and its reversed view obtained via {@link ImmutableSortedListSet#reversed()}.
 */
class ImmutableListBackedSortedSetTest {

    @Test
    void testOf() {
        // Test with no elements
        ImmutableListBackedSortedSet<Integer> emptySet = ImmutableListBackedSortedSet.of();
        assertTrue(emptySet.isEmpty());
        assertEquals(0, emptySet.size());

        // Test with one element
        ImmutableListBackedSortedSet<Integer> singletonSet = ImmutableListBackedSortedSet.of(42);
        assertEquals(1, singletonSet.size());
        assertEquals(42, singletonSet.get(0));

        // Test with multiple elements (unsorted)
        ImmutableListBackedSortedSet<Integer> multiSet = ImmutableListBackedSortedSet.of(5, 3, 1, 4, 2);
        assertEquals(5, multiSet.size());
        assertEquals(1, multiSet.get(0)); // Should be sorted
        assertEquals(2, multiSet.get(1));
        assertEquals(3, multiSet.get(2));
        assertEquals(4, multiSet.get(3));
        assertEquals(5, multiSet.get(4));

        // Test with duplicate elements
        ImmutableListBackedSortedSet<Integer> duplicateSet = ImmutableListBackedSortedSet.of(1, 2, 2, 3, 3, 3);
        assertEquals(3, duplicateSet.size()); // Duplicates should be removed
        assertEquals(1, duplicateSet.get(0));
        assertEquals(2, duplicateSet.get(1));
        assertEquals(3, duplicateSet.get(2));
    }

    @Test
    void testReversed() {
        // Create a set and its reversed view
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test size and isEmpty
        assertEquals(originalSet.size(), reversedSet.size());
        assertEquals(originalSet.isEmpty(), reversedSet.isEmpty());

        // Test order is reversed
        assertEquals(5, reversedSet.get(0));
        assertEquals(4, reversedSet.get(1));
        assertEquals(3, reversedSet.get(2));
        assertEquals(2, reversedSet.get(3));
        assertEquals(1, reversedSet.get(4));

        // Test first and last are swapped
        assertEquals(originalSet.first(), reversedSet.last());
        assertEquals(originalSet.last(), reversedSet.first());
        assertEquals(originalSet.getFirst(), reversedSet.getLast());
        assertEquals(originalSet.getLast(), reversedSet.getFirst());

        // Test contains
        assertTrue(reversedSet.contains(1));
        assertTrue(reversedSet.contains(3));
        assertTrue(reversedSet.contains(5));
        assertFalse(reversedSet.contains(0));
        assertFalse(reversedSet.contains(6));

        // Test containsAll
        assertTrue(reversedSet.containsAll(Arrays.asList(1, 3, 5)));
        assertFalse(reversedSet.containsAll(Arrays.asList(1, 6)));

        // Test iterator
        Iterator<Integer> iterator = reversedSet.iterator();
        assertEquals(5, iterator.next());
        assertEquals(4, iterator.next());
        assertEquals(3, iterator.next());
        assertEquals(2, iterator.next());
        assertEquals(1, iterator.next());
        assertFalse(iterator.hasNext());

        // Test toArray
        Object[] array = reversedSet.toArray();
        assertEquals(5, array.length);
        assertEquals(5, array[0]);
        assertEquals(4, array[1]);
        assertEquals(3, array[2]);
        assertEquals(2, array[3]);
        assertEquals(1, array[4]);

        // Test toArray with provided array
        Integer[] typedArray = new Integer[5];
        Integer[] resultArray = reversedSet.toArray(typedArray);
        assertSame(typedArray, resultArray);
        assertEquals(5, resultArray[0]);
        assertEquals(4, resultArray[1]);
        assertEquals(3, resultArray[2]);
        assertEquals(2, resultArray[3]);
        assertEquals(1, resultArray[4]);

        // Test indexOf and lastIndexOf
        assertEquals(0, reversedSet.indexOf(5));
        assertEquals(2, reversedSet.indexOf(3));
        assertEquals(4, reversedSet.indexOf(1));
        assertEquals(-1, reversedSet.indexOf(0));
        assertEquals(0, reversedSet.lastIndexOf(5)); // Same as indexOf for unique elements
    }

    @Test
    void testReversedListIterators() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test listIterator()
        ListIterator<Integer> listIterator = reversedSet.listIterator();
        assertTrue(listIterator.hasNext());
        assertFalse(listIterator.hasPrevious());
        assertEquals(0, listIterator.nextIndex());
        assertEquals(-1, listIterator.previousIndex());

        assertEquals(5, listIterator.next());
        assertTrue(listIterator.hasNext());
        assertTrue(listIterator.hasPrevious());
        assertEquals(1, listIterator.nextIndex());
        assertEquals(0, listIterator.previousIndex());

        assertEquals(5, listIterator.previous());
        assertTrue(listIterator.hasNext());
        assertFalse(listIterator.hasPrevious());

        // Test listIterator(int)
        ListIterator<Integer> midIterator = reversedSet.listIterator(2);
        assertEquals(2, midIterator.nextIndex());
        assertEquals(1, midIterator.previousIndex());
        assertEquals(3, midIterator.next());
    }

    @Test
    void testReversedSubList() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test subList
        List<Integer> subList = reversedSet.subList(1, 4);
        assertEquals(3, subList.size());
        assertEquals(4, subList.get(0));
        assertEquals(3, subList.get(1));
        assertEquals(2, subList.get(2));
    }

    @Test
    void testReversedComparator() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test comparator is reversed
        assertTrue(reversedSet.comparator().compare(1, 2) > 0); // Reversed order
        assertTrue(reversedSet.comparator().compare(2, 1) < 0); // Reversed order
        assertEquals(0, reversedSet.comparator().compare(3, 3));
    }

    @Test
    void testReversedSubSet() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test subSet
        SortedSet<Integer> subSet = reversedSet.subSet(4, 2);
        assertEquals(2, subSet.size());
        assertTrue(subSet.contains(4));
        assertTrue(subSet.contains(3));
        assertFalse(subSet.contains(1));
        assertFalse(subSet.contains(2));
        assertFalse(subSet.contains(5));

        // Test empty subSet
        SortedSet<Integer> emptySubSet = reversedSet.subSet(3, 3);
        assertTrue(emptySubSet.isEmpty());

        // Test invalid subSet
        assertThrows(IllegalArgumentException.class, () -> reversedSet.subSet(2, 4));
    }

    @Test
    void testReversedHeadSet() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test headSet
        SortedSet<Integer> headSet = reversedSet.headSet(3);
        assertEquals(2, headSet.size());
        assertTrue(headSet.contains(5));
        assertTrue(headSet.contains(4));
        assertFalse(headSet.contains(3));
        assertFalse(headSet.contains(2));
        assertFalse(headSet.contains(1));

        // Test empty headSet
        SortedSet<Integer> emptyHeadSet = reversedSet.headSet(5);
        assertTrue(emptyHeadSet.isEmpty());
    }

    @Test
    void testReversedTailSet() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test tailSet
        SortedSet<Integer> tailSet = reversedSet.tailSet(3);
        assertEquals(3, tailSet.size());
        assertTrue(tailSet.contains(3));
        assertTrue(tailSet.contains(2));
        assertTrue(tailSet.contains(1));
        assertFalse(tailSet.contains(4));
        assertFalse(tailSet.contains(5));

        // Test empty tailSet
        SortedSet<Integer> emptyTailSet = reversedSet.tailSet(0);
        assertTrue(emptyTailSet.isEmpty());
    }

    @Test
    void testDoubleReversed() {
        // Test that reversing a reversed set returns the original set
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3, 4, 5);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();
        ImmutableSortedListSet<Integer> doubleReversedSet = reversedSet.reversed();

        assertSame(originalSet, doubleReversedSet);
        assertEquals(originalSet, doubleReversedSet);
    }

    @Test
    void testReversedWithEmptySet() {
        // Test reversed view of an empty set
        ImmutableListBackedSortedSet<Integer> emptySet = ImmutableListBackedSortedSet.of();
        ImmutableSortedListSet<Integer> reversedEmptySet = emptySet.reversed();

        assertTrue(reversedEmptySet.isEmpty());
        assertEquals(0, reversedEmptySet.size());
        assertThrows(NoSuchElementException.class, reversedEmptySet::first);
        assertThrows(NoSuchElementException.class, reversedEmptySet::last);
        assertThrows(NoSuchElementException.class, reversedEmptySet::getFirst);
        assertThrows(NoSuchElementException.class, reversedEmptySet::getLast);
    }

    @Test
    void testReversedUnsupportedOperations() {
        ImmutableListBackedSortedSet<Integer> originalSet = ImmutableListBackedSortedSet.of(1, 2, 3);
        ImmutableSortedListSet<Integer> reversedSet = originalSet.reversed();

        // Test unsupported operations
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.add(4));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.remove(1));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.addAll(Collections.singletonList(4)));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.removeAll(Collections.singletonList(1)));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.retainAll(Collections.singletonList(1)));
        assertThrows(UnsupportedOperationException.class, reversedSet::clear);
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.set(0, 10));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.add(0, 10));
        assertThrows(UnsupportedOperationException.class, () -> reversedSet.remove(0));
    }

    @Test
    void testReversedEqualsAndHashCode() {
        ImmutableListBackedSortedSet<Integer> set1 = ImmutableListBackedSortedSet.of(1, 2, 3);
        ImmutableSortedListSet<Integer> reversed1 = set1.reversed();

        ImmutableListBackedSortedSet<Integer> set2 = ImmutableListBackedSortedSet.of(1, 2, 3);
        ImmutableSortedListSet<Integer> reversed2 = set2.reversed();

        ImmutableListBackedSortedSet<Integer> set3 = ImmutableListBackedSortedSet.of(1, 2);
        ImmutableSortedListSet<Integer> reversed3 = set3.reversed();

        // Test that same content produces same hashCode
        assertEquals(reversed1.hashCode(), reversed2.hashCode());
        assertNotEquals(reversed1.hashCode(), reversed3.hashCode());

        // Test that original and reversed have different hashCodes
        assertNotEquals(set1.hashCode(), reversed1.hashCode());

        // Test content equality
        assertEquals(3, reversed1.size());
        assertEquals(3, reversed2.size());
        assertEquals(3, reversed1.get(0).intValue());
        assertEquals(3, reversed2.get(0).intValue());
        assertEquals(1, reversed1.get(2).intValue());
        assertEquals(1, reversed2.get(2).intValue());
    }

    @Test
    void testCornerCasesSubSet() {
        // Create a set with some elements
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);

        // Create a TreeSet with the same elements for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));

        // Test subSet with keys not in the set

        // Case 1: fromKey before all elements, toKey between elements
        SortedSet<Integer> subSet1 = set.subSet(5, 25);
        SortedSet<Integer> expectedSubSet1 = treeSet.subSet(5, 25);
        assertEquals(expectedSubSet1.size(), subSet1.size());
        assertEquals(new TreeSet<>(expectedSubSet1), new TreeSet<>(subSet1));

        // Case 2: fromKey between elements, toKey after all elements
        SortedSet<Integer> subSet2 = set.subSet(25, 55);
        SortedSet<Integer> expectedSubSet2 = treeSet.subSet(25, 55);
        assertEquals(expectedSubSet2.size(), subSet2.size());
        assertEquals(new TreeSet<>(expectedSubSet2), new TreeSet<>(subSet2));

        // Case 3: both fromKey and toKey between different elements
        SortedSet<Integer> subSet3 = set.subSet(15, 45);
        SortedSet<Integer> expectedSubSet3 = treeSet.subSet(15, 45);
        assertEquals(expectedSubSet3.size(), subSet3.size());
        assertEquals(new TreeSet<>(expectedSubSet3), new TreeSet<>(subSet3));

        // Case 4: fromKey before all elements, toKey after all elements
        SortedSet<Integer> subSet4 = set.subSet(5, 55);
        SortedSet<Integer> expectedSubSet4 = treeSet.subSet(5, 55);
        assertEquals(expectedSubSet4.size(), subSet4.size());
        assertEquals(new TreeSet<>(expectedSubSet4), new TreeSet<>(subSet4));
    }

    @Test
    void testCornerCasesHeadSet() {
        // Create a set with some elements
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);

        // Create a TreeSet with the same elements for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));

        // Test headSet with keys not in the set

        // Case 1: toKey before all elements
        SortedSet<Integer> headSet1 = set.headSet(5);
        SortedSet<Integer> expectedHeadSet1 = treeSet.headSet(5);
        assertEquals(expectedHeadSet1.size(), headSet1.size());
        assertEquals(new TreeSet<>(expectedHeadSet1), new TreeSet<>(headSet1));

        // Case 2: toKey between elements
        SortedSet<Integer> headSet2 = set.headSet(25);
        SortedSet<Integer> expectedHeadSet2 = treeSet.headSet(25);
        assertEquals(expectedHeadSet2.size(), headSet2.size());
        assertEquals(new TreeSet<>(expectedHeadSet2), new TreeSet<>(headSet2));

        // Case 3: toKey after all elements
        SortedSet<Integer> headSet3 = set.headSet(55);
        SortedSet<Integer> expectedHeadSet3 = treeSet.headSet(55);
        assertEquals(expectedHeadSet3.size(), headSet3.size());
        assertEquals(new TreeSet<>(expectedHeadSet3), new TreeSet<>(headSet3));
    }

    @Test
    void testCornerCasesTailSet() {
        // Create a set with some elements
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);

        // Create a TreeSet with the same elements for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));

        // Test tailSet with keys not in the set

        // Case 1: fromKey before all elements
        SortedSet<Integer> tailSet1 = set.tailSet(5);
        SortedSet<Integer> expectedTailSet1 = treeSet.tailSet(5);
        assertEquals(expectedTailSet1.size(), tailSet1.size());
        assertEquals(new TreeSet<>(expectedTailSet1), new TreeSet<>(tailSet1));

        // Case 2: fromKey between elements
        SortedSet<Integer> tailSet2 = set.tailSet(25);
        SortedSet<Integer> expectedTailSet2 = treeSet.tailSet(25);
        assertEquals(expectedTailSet2.size(), tailSet2.size());
        assertEquals(new TreeSet<>(expectedTailSet2), new TreeSet<>(tailSet2));

        // Case 3: fromKey after all elements
        SortedSet<Integer> tailSet3 = set.tailSet(55);
        SortedSet<Integer> expectedTailSet3 = treeSet.tailSet(55);
        assertEquals(expectedTailSet3.size(), tailSet3.size());
        assertEquals(new TreeSet<>(expectedTailSet3), new TreeSet<>(tailSet3));
    }

    @Test
    void testReversedCornerCasesSubSet() {
        // Create a set with some elements and its reversed view
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);
        ImmutableSortedListSet<Integer> reversedSet = set.reversed();

        // Create a TreeSet with the same elements and its reversed view for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));
        TreeSet<Integer> reversedTreeSet = new TreeSet<>(treeSet.descendingSet());

        // Test subSet with keys not in the set

        // Case 1: fromKey (higher value) before all elements, toKey (lower value) between elements
        SortedSet<Integer> expectedSubSet1 = reversedTreeSet.subSet(55, 25);
        SortedSet<Integer> subSet1 = reversedSet.subSet(55, 25);
        assertEquals(expectedSubSet1.size(), subSet1.size());
        assertEquals(new TreeSet<>(expectedSubSet1), new TreeSet<>(subSet1));

        // Case 2: fromKey (higher value) between elements, toKey (lower value) after all elements
        SortedSet<Integer> expectedSubSet2 = reversedTreeSet.subSet(45, 5);
        SortedSet<Integer> subSet2 = reversedSet.subSet(45, 5);
        assertEquals(expectedSubSet2.size(), subSet2.size());
        assertEquals(new TreeSet<>(expectedSubSet2), new TreeSet<>(subSet2));

        // Case 3: both fromKey and toKey between different elements
        SortedSet<Integer> expectedSubSet3 = reversedTreeSet.subSet(45, 15);
        SortedSet<Integer> subSet3 = reversedSet.subSet(45, 15);
        assertEquals(expectedSubSet3.size(), subSet3.size());
        assertEquals(new TreeSet<>(expectedSubSet3), new TreeSet<>(subSet3));

        // Case 4: fromKey (higher value) before all elements, toKey (lower value) after all elements
        SortedSet<Integer> expectedSubSet4 = reversedTreeSet.subSet(55, 5);
        SortedSet<Integer> subSet4 = reversedSet.subSet(55, 5);
        assertEquals(expectedSubSet4.size(), subSet4.size());
        assertEquals(new TreeSet<>(expectedSubSet4), new TreeSet<>(subSet4));
    }

    @Test
    void testReversedCornerCasesHeadSet() {
        // Create a set with some elements and its reversed view
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);
        ImmutableSortedListSet<Integer> reversedSet = set.reversed();

        // Create a TreeSet with the same elements and its reversed view for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));
        TreeSet<Integer> reversedTreeSet = new TreeSet<>(treeSet.descendingSet());

        // Test headSet with keys not in the set

        // Case 1: toKey (lower value) before all elements
        SortedSet<Integer> expectedHeadSet1 = reversedTreeSet.headSet(5);
        SortedSet<Integer> headSet1 = reversedSet.headSet(5);
        assertEquals(expectedHeadSet1.size(), headSet1.size());
        assertEquals(new TreeSet<>(expectedHeadSet1), new TreeSet<>(headSet1));

        // Case 2: toKey (lower value) between elements
        SortedSet<Integer> expectedHeadSet2 = reversedTreeSet.headSet(25);
        SortedSet<Integer> headSet2 = reversedSet.headSet(25);
        assertEquals(expectedHeadSet2.size(), headSet2.size());
        assertEquals(new TreeSet<>(expectedHeadSet2), new TreeSet<>(headSet2));

        // Case 3: toKey (lower value) after all elements
        SortedSet<Integer> expectedHeadSet3 = reversedTreeSet.headSet(55);
        SortedSet<Integer> headSet3 = reversedSet.headSet(55);
        assertEquals(expectedHeadSet3.size(), headSet3.size());
        assertEquals(new TreeSet<>(expectedHeadSet3), new TreeSet<>(headSet3));
    }

    @Test
    void testReversedCornerCasesTailSet() {
        // Create a set with some elements and its reversed view
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);
        ImmutableSortedListSet<Integer> reversedSet = set.reversed();

        // Create a TreeSet with the same elements and its reversed view for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));
        TreeSet<Integer> reversedTreeSet = new TreeSet<>(treeSet.descendingSet());

        // Test tailSet with keys not in the set

        // Case 1: fromKey (higher value) before all elements
        SortedSet<Integer> expectedTailSet1 = reversedTreeSet.tailSet(55);
        SortedSet<Integer> tailSet1 = reversedSet.tailSet(55);
        assertEquals(expectedTailSet1.size(), tailSet1.size());
        assertEquals(new TreeSet<>(expectedTailSet1), new TreeSet<>(tailSet1));

        // Case 2: fromKey (higher value) between elements
        SortedSet<Integer> expectedTailSet2 = reversedTreeSet.tailSet(25);
        SortedSet<Integer> tailSet2 = reversedSet.tailSet(25);
        assertEquals(expectedTailSet2.size(), tailSet2.size());
        assertEquals(new TreeSet<>(expectedTailSet2), new TreeSet<>(tailSet2));

        // Case 3: fromKey (higher value) after all elements
        SortedSet<Integer> expectedTailSet3 = reversedTreeSet.tailSet(5);
        SortedSet<Integer> tailSet3 = reversedSet.tailSet(5);
        assertEquals(expectedTailSet3.size(), tailSet3.size());
        assertEquals(new TreeSet<>(expectedTailSet3), new TreeSet<>(tailSet3));
    }

    @Test
    void testBoundaryElementsSubHeadTailSet() {
        // Create a set with some elements
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);

        // Create a TreeSet with the same elements for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));

        // Test subSet with fromElement equal to the first element
        SortedSet<Integer> subSet1 = set.subSet(10, 30);
        SortedSet<Integer> expectedSubSet1 = treeSet.subSet(10, 30);
        assertEquals(expectedSubSet1.size(), subSet1.size());
        assertEquals(new TreeSet<>(expectedSubSet1), new TreeSet<>(subSet1));

        // Test subSet with toElement equal to last element
        SortedSet<Integer> subSet2 = set.subSet(30, 50);
        SortedSet<Integer> expectedSubSet2 = treeSet.subSet(30, 50);
        assertEquals(expectedSubSet2.size(), subSet2.size());
        assertEquals(new TreeSet<>(expectedSubSet2), new TreeSet<>(subSet2));

        // Test subSet with fromElement equal to first element and toElement equal to last element
        SortedSet<Integer> subSet3 = set.subSet(10, 50);
        SortedSet<Integer> expectedSubSet3 = treeSet.subSet(10, 50);
        assertEquals(expectedSubSet3.size(), subSet3.size());
        assertEquals(new TreeSet<>(expectedSubSet3), new TreeSet<>(subSet3));

        // Test headSet with toElement equal to first element
        SortedSet<Integer> headSet1 = set.headSet(10);
        SortedSet<Integer> expectedHeadSet1 = treeSet.headSet(10);
        assertEquals(expectedHeadSet1.size(), headSet1.size());
        assertEquals(new TreeSet<>(expectedHeadSet1), new TreeSet<>(headSet1));

        // Test headSet with toElement equal to last element
        SortedSet<Integer> headSet2 = set.headSet(50);
        SortedSet<Integer> expectedHeadSet2 = treeSet.headSet(50);
        assertEquals(expectedHeadSet2.size(), headSet2.size());
        assertEquals(new TreeSet<>(expectedHeadSet2), new TreeSet<>(headSet2));

        // Test tailSet with fromElement equal to first element
        SortedSet<Integer> tailSet1 = set.tailSet(10);
        SortedSet<Integer> expectedTailSet1 = treeSet.tailSet(10);
        assertEquals(expectedTailSet1.size(), tailSet1.size());
        assertEquals(new TreeSet<>(expectedTailSet1), new TreeSet<>(tailSet1));

        // Test tailSet with fromElement equal to last element
        SortedSet<Integer> tailSet2 = set.tailSet(50);
        SortedSet<Integer> expectedTailSet2 = treeSet.tailSet(50);
        assertEquals(expectedTailSet2.size(), tailSet2.size());
        assertEquals(new TreeSet<>(expectedTailSet2), new TreeSet<>(tailSet2));
    }

    @Test
    void testReversedBoundaryElementsSubHeadTailSet() {
        // Create a set with some elements and its reversed view
        ImmutableListBackedSortedSet<Integer> set = ImmutableListBackedSortedSet.of(10, 20, 30, 40, 50);
        ImmutableSortedListSet<Integer> reversedSet = set.reversed();

        // Create a TreeSet with the same elements and its reversed view for comparison
        TreeSet<Integer> treeSet = new TreeSet<>(Arrays.asList(10, 20, 30, 40, 50));
        TreeSet<Integer> reversedTreeSet = new TreeSet<>(treeSet.descendingSet());

        // Test subSet with fromElement equal to first element (50 in reversed set)
        SortedSet<Integer> subSet1 = reversedSet.subSet(50, 30);
        SortedSet<Integer> expectedSubSet1 = reversedTreeSet.subSet(50, 30);
        assertEquals(expectedSubSet1.size(), subSet1.size());
        assertEquals(new TreeSet<>(expectedSubSet1), new TreeSet<>(subSet1));

        // Test subSet with toElement equal to last element (10 in reversed set)
        SortedSet<Integer> subSet2 = reversedSet.subSet(30, 10);
        SortedSet<Integer> expectedSubSet2 = reversedTreeSet.subSet(30, 10);
        assertEquals(expectedSubSet2.size(), subSet2.size());
        assertEquals(new TreeSet<>(expectedSubSet2), new TreeSet<>(subSet2));

        // Test subSet with fromElement equal to first element and toElement equal to last element
        SortedSet<Integer> subSet3 = reversedSet.subSet(50, 10);
        SortedSet<Integer> expectedSubSet3 = reversedTreeSet.subSet(50, 10);
        assertEquals(expectedSubSet3.size(), subSet3.size());
        assertEquals(new TreeSet<>(expectedSubSet3), new TreeSet<>(subSet3));

        // Test headSet with toElement equal to first element (50 in reversed set)
        SortedSet<Integer> headSet1 = reversedSet.headSet(50);
        SortedSet<Integer> expectedHeadSet1 = reversedTreeSet.headSet(50);
        assertEquals(expectedHeadSet1.size(), headSet1.size());
        assertEquals(new TreeSet<>(expectedHeadSet1), new TreeSet<>(headSet1));

        // Test headSet with toElement equal to last element (10 in reversed set)
        SortedSet<Integer> headSet2 = reversedSet.headSet(10);
        SortedSet<Integer> expectedHeadSet2 = reversedTreeSet.headSet(10);
        assertEquals(expectedHeadSet2.size(), headSet2.size());
        assertEquals(new TreeSet<>(expectedHeadSet2), new TreeSet<>(headSet2));

        // Test tailSet with fromElement equal to first element (50 in reversed set)
        SortedSet<Integer> tailSet1 = reversedSet.tailSet(50);
        SortedSet<Integer> expectedTailSet1 = reversedTreeSet.tailSet(50);
        assertEquals(expectedTailSet1.size(), tailSet1.size());
        assertEquals(new TreeSet<>(expectedTailSet1), new TreeSet<>(tailSet1));

        // Test tailSet with fromElement equal to last element (10 in reversed set)
        SortedSet<Integer> tailSet2 = reversedSet.tailSet(10);
        SortedSet<Integer> expectedTailSet2 = reversedTreeSet.tailSet(10);
        assertEquals(expectedTailSet2.size(), tailSet2.size());
        assertEquals(new TreeSet<>(expectedTailSet2), new TreeSet<>(tailSet2));
    }
}
