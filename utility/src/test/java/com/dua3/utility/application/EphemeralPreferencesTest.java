package com.dua3.utility.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testTypeOperations() {
        root.putInt("intVal", 42);
        assertEquals(42, root.getInt("intVal", 0));

        root.putLong("longVal", 123456789L);
        assertEquals(123456789L, root.getLong("longVal", 0L));

        root.putBoolean("boolVal", true);
        assertTrue(root.getBoolean("boolVal", false));

        root.putFloat("floatVal", 3.14f);
        assertEquals(3.14f, root.getFloat("floatVal", 0.0f), 0.001f);

        root.putDouble("doubleVal", 2.71828);
        assertEquals(2.71828, root.getDouble("doubleVal", 0.0), 0.00001);

        byte[] bytes = new byte[]{1, 2, 3, 4};
        root.putByteArray("bytes", bytes);
        assertArrayEquals(bytes, root.getByteArray("bytes", null));
    }

    @Test
    void testChildNodesAndHierarchy() throws BackingStoreException {
        assertEquals("", root.name());
        assertNull(root.parent());

        Preferences parentNode = root.node("parent");
        assertEquals("parent", parentNode.name());
        assertEquals(root, parentNode.parent());

        Preferences childNode = parentNode.node("child");
        assertEquals("child", childNode.name());
        assertEquals(parentNode, childNode.parent());

        childNode.put("childKey", "childValue");
        assertEquals("childValue", childNode.get("childKey", null));

        assertTrue(root.nodeExists("parent"));
        assertTrue(parentNode.nodeExists("child"));

        childNode.removeNode();
        assertFalse(parentNode.nodeExists("child"));
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
        assertTrue(java.util.Set.of(keys).containsAll(java.util.List.of("key1", "key2")));
        assertTrue(java.util.Set.of(children).containsAll(java.util.List.of("child1", "child2")));
    }

    @Test
    void testClear() throws BackingStoreException {
        root.put("k1", "v1");
        root.put("k2", "v2");
        assertEquals(2, root.keys().length);

        root.clear();
        assertEquals(0, root.keys().length);
    }

    @Test
    void testRecursiveRemoveNodeSubtree() throws BackingStoreException {
        Preferences parent = root.node("treeParent");
        Preferences child = parent.node("treeChild");
        Preferences grandchild = child.node("treeGrandchild");

        parent.put("pKey", "pVal");
        child.put("cKey", "cVal");
        grandchild.put("gKey", "gVal");

        parent.removeNode();
        assertFalse(root.nodeExists("treeParent"));
    }

    @Test
    void testSyncAndFlush() {
        root.put("key", "value");
        assertDoesNotThrow(root::flush);
        assertDoesNotThrow(root::sync);
    }
}
