package com.dua3.utility.concurrent;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class SimpleValueTest {

    @Test
    void testConstructorAndGet() {
        // Test with non-null value
        SimpleValue<String> value = new SimpleValue<>("test");
        assertEquals("test", value.get());

        // Test with null value
        SimpleValue<@Nullable String> nullValue = new SimpleValue<>(null);
        assertNull(nullValue.get());
    }

    @Test
    void testSet() {
        // Test setting a new value
        SimpleValue<Integer> value = new SimpleValue<>(10);
        assertEquals(10, value.get());

        value.set(20);
        assertEquals(20, value.get());

        // Test setting to null
        value.set(null);
        assertNull(value.get());
    }

    @Test
    void testChangeListeners() {
        // Create a value with initial value
        SimpleValue<String> value = new SimpleValue<>("initial");

        // Create a list to track old and new values
        List<String> oldValues = new ArrayList<>();
        List<String> newValues = new ArrayList<>();

        // Add a change listener
        BiConsumer<String, String> listener = (oldVal, newVal) -> {
            oldValues.add(oldVal);
            newValues.add(newVal);
        };

        value.addChangeListener(listener);

        // Change the value and verify listener was called
        value.set("updated");
        assertEquals(1, oldValues.size());
        assertEquals(1, newValues.size());
        assertEquals("initial", oldValues.getFirst());
        assertEquals("updated", newValues.getFirst());

        // Change the value again
        value.set("final");
        assertEquals(2, oldValues.size());
        assertEquals(2, newValues.size());
        assertEquals("updated", oldValues.get(1));
        assertEquals("final", newValues.get(1));

        // Remove the listener
        value.removeChangeListener(listener);

        // Change the value and verify listener was not called
        value.set("after-removal");
        assertEquals(2, oldValues.size());
        assertEquals(2, newValues.size());
    }

    @Test
    void testGetChangeListeners() {
        SimpleValue<String> value = new SimpleValue<>("test");

        // Initially, there should be no listeners
        Collection<BiConsumer<? super String, ? super String>> listeners = value.getChangeListeners();
        assertTrue(listeners.isEmpty());

        // Add a listener
        BiConsumer<String, String> listener1 = (oldVal, newVal) -> {};
        value.addChangeListener(listener1);

        // Now there should be one listener
        listeners = value.getChangeListeners();
        assertEquals(1, listeners.size());

        // Add another listener
        BiConsumer<String, String> listener2 = (oldVal, newVal) -> {};
        value.addChangeListener(listener2);

        // Now there should be two listeners
        listeners = value.getChangeListeners();
        assertEquals(2, listeners.size());

        // Remove a listener
        value.removeChangeListener(listener1);

        // Now there should be one listener again
        listeners = value.getChangeListeners();
        assertEquals(1, listeners.size());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create values with the same content
        SimpleValue<String> value1 = new SimpleValue<>("test");
        SimpleValue<String> value2 = new SimpleValue<>("test");

        // They should be equal and have the same hash code
        assertEquals(value1, value2);
        assertEquals(value1.hashCode(), value2.hashCode());

        // Change one value
        value2.set("different");

        // Now they should not be equal
        assertNotEquals(value1, value2);

        // Test with null values
        SimpleValue<String> nullValue1 = new SimpleValue<>(null);
        SimpleValue<String> nullValue2 = new SimpleValue<>(null);

        // Null values should be equal to each other
        assertEquals(nullValue1, nullValue2);
        assertEquals(nullValue1.hashCode(), nullValue2.hashCode());
    }

    @Test
    void testToString() {
        // Test with non-null value
        SimpleValue<String> value = new SimpleValue<>("test");
        assertEquals("test", value.toString());

        // Test with null value
        SimpleValue<String> nullValue = new SimpleValue<>(null);
        assertEquals("null", nullValue.toString());
    }

    @Test
    void testStaticFactoryMethods() {
        // Test Value.create
        Value<Integer> value = Value.create(42);
        assertInstanceOf(SimpleValue.class, value);
        assertEquals(42, value.get());

        // Test Value.createReadOnly
        ReadOnlyValue<String> readOnlyValue = Value.createReadOnly("readonly");
        assertEquals("readonly", readOnlyValue.get());

        // Verify that the readOnlyValue is actually a Value instance
        // (which means it's mutable, but the interface restricts access)
        assertInstanceOf(Value.class, readOnlyValue);
    }
}