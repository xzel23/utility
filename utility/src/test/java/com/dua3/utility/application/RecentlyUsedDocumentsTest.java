package com.dua3.utility.application;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.IoUtil;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecentlyUsedDocumentsTest {

    @Test
    void testInvalidCapacityThrowsException() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testInvalidCapacity");
        assertThrows(IllegalArgumentException.class, () -> new RecentlyUsedDocuments(prefs, 0));
        assertThrows(IllegalArgumentException.class, () -> new RecentlyUsedDocuments(prefs, -5));
    }

    @Test
    void testDefaultCapacityConstructor() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testDefaultCapacity");
        RecentlyUsedDocuments rud = new RecentlyUsedDocuments(prefs);
        assertTrue(rud.entries().isEmpty());
        assertEquals(IoUtil.getUserHome().toUri(), rud.getLastUri());
    }

    @Test
    void testPut_AddSingleDocument() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_AddSingleDocument");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 5);

        URI uri = URI.create("file:///test/document1.txt");
        String name = "document1.txt";
        recentlyUsedDocuments.put(uri, name);

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.getFirst().first());
        assertEquals(name, entries.getFirst().second());
        assertEquals(uri, recentlyUsedDocuments.getLastUri());
    }

    @Test
    void testPut_AddMultipleDocuments() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_AddMultipleDocuments");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 5);

        URI uri1 = URI.create("file:///test/document1.txt");
        URI uri2 = URI.create("file:///test/document2.txt");
        recentlyUsedDocuments.put(uri1, "document1.txt");
        recentlyUsedDocuments.put(uri2, "document2.txt");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(2, entries.size());
        assertEquals(uri2, entries.get(0).first());
        assertEquals("document2.txt", entries.get(0).second());
        assertEquals(uri1, entries.get(1).first());
        assertEquals("document1.txt", entries.get(1).second());
        assertEquals(uri2, recentlyUsedDocuments.getLastUri());
    }

    @Test
    void testPut_ExceedCapacity() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_ExceedCapacity");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 2);

        URI uri1 = URI.create("file:///test/document1.txt");
        URI uri2 = URI.create("file:///test/document2.txt");
        URI uri3 = URI.create("file:///test/document3.txt");
        recentlyUsedDocuments.put(uri1, "document1.txt");
        recentlyUsedDocuments.put(uri2, "document2.txt");
        recentlyUsedDocuments.put(uri3, "document3.txt");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(2, entries.size());
        assertEquals(uri3, entries.get(0).first());
        assertEquals("document3.txt", entries.get(0).second());
        assertEquals(uri2, entries.get(1).first());
        assertEquals("document2.txt", entries.get(1).second());
    }

    @Test
    void testPut_EmptyNameUsesUriPath() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_EmptyNameUsesUriPath");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document1.txt");
        recentlyUsedDocuments.put(uri, "");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.getFirst().first());
        assertEquals("/test/document1.txt", entries.getFirst().second()); // Uses full path from URI.
    }

    @Test
    void testPut_WithMissingName() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_WithMissingName");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document2.txt");
        recentlyUsedDocuments.put(uri);

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.getFirst().first());
        assertEquals("document2.txt", entries.getFirst().second()); // Extracts file name from path.
    }

    @Test
    void testPut_ReplaceExistingEntry() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testPut_ReplaceExistingEntry");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document1.txt");
        recentlyUsedDocuments.put(uri, "OriginalName.txt");
        recentlyUsedDocuments.put(uri, "UpdatedName.txt");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.getFirst().first());
        assertEquals("UpdatedName.txt", entries.getFirst().second()); // Name is updated.
    }

    @Test
    void testClearAndListeners() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testClearAndListeners");
        RecentlyUsedDocuments rud = new RecentlyUsedDocuments(prefs, 5);

        AtomicInteger updates = new AtomicInteger(0);
        RecentlyUsedDocuments.UpdateListener listener = src -> updates.incrementAndGet();
        RecentlyUsedDocuments.UpdateListener throwingListener = src -> {
            throw new RuntimeException("Simulated listener exception");
        };

        rud.addUpdateListener(listener);
        rud.addUpdateListener(throwingListener);

        rud.put(URI.create("file:///doc1.txt"), "Doc 1");
        assertEquals(1, updates.get());
        assertEquals(1, rud.entries().size());

        rud.clear();
        assertEquals(2, updates.get());
        assertTrue(rud.entries().isEmpty());

        rud.removeUpdateListener(listener);
        rud.removeUpdateListener(throwingListener);
        rud.put(URI.create("file:///doc2.txt"), "Doc 2");
        assertEquals(2, updates.get());
    }

    @Test
    void testReloadFromPreferences() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testReload");
        RecentlyUsedDocuments rud1 = new RecentlyUsedDocuments(prefs, 5);
        rud1.put(URI.create("file:///item1.txt"), "Item 1");
        rud1.put(URI.create("file:///item2.txt"), "Item 2");

        // Create second instance backed by the same preferences
        RecentlyUsedDocuments rud2 = new RecentlyUsedDocuments(prefs, 5);
        List<Pair<URI, String>> entries = rud2.entries();
        assertEquals(2, entries.size());
        assertEquals(URI.create("file:///item2.txt"), entries.get(0).first());
        assertEquals("Item 2", entries.get(0).second());
        assertEquals(URI.create("file:///item1.txt"), entries.get(1).first());
        assertEquals("Item 1", entries.get(1).second());
    }

    @Test
    void testToString() {
        Preferences prefs = EphemeralPreferences.createRoot().node("testToString");
        RecentlyUsedDocuments rud = new RecentlyUsedDocuments(prefs, 5);
        rud.put(URI.create("file:///item1.txt"), "Item 1");
        String str = rud.toString();
        assertNotNull(str);
        assertTrue(str.contains("capacity=5"));
        assertTrue(str.contains("item1.txt"));
    }
}