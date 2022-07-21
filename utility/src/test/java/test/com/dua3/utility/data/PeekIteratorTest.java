package test.com.dua3.utility.data;

import com.dua3.utility.data.PeekIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

class PeekIteratorTest {

    @Test
    void peekIterator() {
        List<Integer>  items = List.of(1,2,3);
        
        PeekIterator<Integer> iter = new PeekIterator<>(items.iterator());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(1, (int) iter.peek());
        Assertions.assertEquals(1, (int) iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(2, (int) iter.peek());
        Assertions.assertEquals(2, (int) iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(3, (int) iter.peek());
        Assertions.assertEquals(3, (int) iter.next());
        Assertions.assertFalse(iter.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iter::peek);
        Assertions.assertThrows(NoSuchElementException.class, iter::next);
    }

    @Test
    void emptyIterator() {
        PeekIterator<Integer> iter = new PeekIterator<>(Collections.emptyIterator());
        Assertions.assertFalse(iter.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, iter::peek);
        Assertions.assertThrows(NoSuchElementException.class, iter::next);
    }

}
