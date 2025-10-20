package com.dua3.utility.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions .*;

class EphemeralPreferencesTest {
    private Preferences root;

    @BeforeEach
    void setUp() {
        root = EphemeralPreferences.createRoot();
    }

    @Test
    void testBasicOperations() {
        root.put("key1", "value1");
        assertEquals("value1", root.get("key1", null));
        assertEquals("default", root.get("nonexistent", "default"));

        root.remove("key1");
        assertNull(root.get("key1", null));
    }

    @Test
    void testChildNodes() throws BackingStoreException {
        Preferences child = root.node("child");
        assertNotNull(child);
        assertTrue(root.nodeExists("child"));

        child.removeNode();
        assertFalse(root.nodeExists("child"));
    }

    @Test
    void testKeysAndChildren() throws BackingStoreException {
        root.put("key1", "value1");
        root.put("key2", "value2");
        root.node("child1");
        root.node("child2");

        String[] keys = root.keys();
        String[] children = root.childrenNames();

        assertEquals(2, keys.length);
        assertEquals(2, children.length);
        assertArrayEquals(new String[]{"key1", "key2"}, keys);
        assertArrayEquals(new String[]{"child1", "child2"}, children);
    }

    @Test
    void testSyncAndFlush() {
        // These operations should not throw exceptions
        root.put("key", "value");
        assertDoesNotThrow(root::flush);
        assertDoesNotThrow(root::sync);
    }
}
