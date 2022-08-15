package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterIteratorTest {

    @Test
    public void testEmpty() {
        List<Integer> items = List.of();

        @SuppressWarnings("RedundantOperationOnEmptyContainer") 
        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->true);
        assertFalse(fi.hasNext());
    }

    @Test
    public void testAllMatching() {
        List<Integer> items = List.of(1,2,3);

        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->true);
        assertTrue(fi.hasNext());
        assertEquals(1, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(2, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(3, fi.next());
        assertFalse(fi.hasNext());
    }

    @Test
    public void testNonMatching() {
        List<Integer> items = List.of(1,2,3);

        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->false);
        assertFalse(fi.hasNext());
    }


    @Test
    public void testSomeMatchingFirstNonMatching() {
        List<Integer> items = List.of(1,2,3,4,5,6,7,8,9,10);

        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->i%3==0);
        assertTrue(fi.hasNext());
        assertEquals(3, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(6, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(9, fi.next());
        assertFalse(fi.hasNext());
    }

    @Test
    public void testSomeMatchingFirstMatching() {
        List<Integer> items = List.of(1,2,3,4,5,6,7,8,9,10);

        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->(i&1)==1);
        assertTrue(fi.hasNext());
        assertEquals(1, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(3, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(5, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(7, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(9, fi.next());
        assertFalse(fi.hasNext());
    }

    @Test
    public void testSomeMatchingLastMatching() {
        List<Integer> items = List.of(1,2,3,4,5,6,7,8,9,10);

        Iterator<Integer> fi = new FilterIterator<>(items.iterator(), i->i%2==0);
        assertTrue(fi.hasNext());
        assertEquals(2, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(4, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(6, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(8, fi.next());
        assertTrue(fi.hasNext());
        assertEquals(10, fi.next());
        assertFalse(fi.hasNext());
    }
}
