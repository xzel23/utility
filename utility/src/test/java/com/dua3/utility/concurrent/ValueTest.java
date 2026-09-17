package com.dua3.utility.concurrent;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueTest {

    @Test
    void testCreateAndGet() {
        Value<String> val = Value.create("initial");
        assertEquals("initial", val.get());

        val.set("updated");
        assertEquals("updated", val.get());
    }

    @Test
    void testCreateWithNull() {
        Value<String> val = Value.create(null);
        assertNull(val.get());

        val.set("non-null");
        assertEquals("non-null", val.get());

        val.set(null);
        assertNull(val.get());
    }

    @Test
    void testCreateReadOnly() {
        ReadOnlyValue<Integer> roVal = Value.createReadOnly(100);
        assertEquals(100, roVal.get());
    }

    @Test
    void testChangeListener() {
        Value<String> val = Value.create("first");
        List<String> events = new ArrayList<>();

        BiConsumer<String, String> listener = (oldVal, newVal) -> events.add(oldVal + "->" + newVal);
        val.addChangeListener(listener);

        Collection<BiConsumer<? super String, ? super String>> listeners = val.getChangeListeners();
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(listener));

        // Setting same value should NOT trigger listener
        val.set("first");
        assertTrue(events.isEmpty());

        // Setting different value should trigger listener
        val.set("second");
        assertEquals(List.of("first->second"), events);

        // Setting to null should trigger listener
        val.set(null);
        assertEquals(List.of("first->second", "second->null"), events);

        // Setting from null to another value
        val.set("third");
        assertEquals(List.of("first->second", "second->null", "null->third"), events);

        // Remove listener
        val.removeChangeListener(listener);
        assertEquals(0, val.getChangeListeners().size());

        val.set("fourth");
        assertEquals(List.of("first->second", "second->null", "null->third"), events);
    }

    @Test
    void testMultipleChangeListeners() {
        Value<Integer> val = Value.create(1);
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();

        val.addChangeListener((oldV, newV) -> list1.add(newV));
        val.addChangeListener((oldV, newV) -> list2.add(newV));

        assertEquals(2, val.getChangeListeners().size());

        val.set(2);
        assertEquals(List.of(2), list1);
        assertEquals(List.of(2), list2);
    }

    @Test
    void testGetChangeListenersUnmodifiable() {
        Value<String> val = Value.create("a");
        BiConsumer<String, String> dummyListener = (o, n) -> {};
        Collection<BiConsumer<? super String, ? super String>> listeners = val.getChangeListeners();

        assertThrows(UnsupportedOperationException.class, () -> listeners.add(dummyListener));
    }

    @Test
    void testEqualsAndHashCode() {
        Value<String> v1 = Value.create("hello");
        Value<String> v2 = Value.create("hello");
        Value<String> v3 = Value.create("world");
        Value<String> vNull1 = Value.create(null);
        Value<String> vNull2 = Value.create(null);

        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());

        assertNotEquals(v1, v3);
        assertNotEquals(null, v1);
        assertNotEquals("hello", v1);

        assertEquals(vNull1, vNull2);
        assertEquals(vNull1.hashCode(), vNull2.hashCode());
        assertNotEquals(v1, vNull1);
    }

    @Test
    void testToString() {
        Value<String> val = Value.create("test string");
        assertEquals("test string", val.toString());

        Value<String> nullVal = Value.create(null);
        assertEquals("null", nullVal.toString());
    }
}
