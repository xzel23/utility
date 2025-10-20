package com.dua3.utility.application;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecentlyUsedDocumentsTest {

    @Test
    void testPut_AddSingleDocument() {
        Preferences prefs = Preferences.userRoot().node("testPut_AddSingleDocument");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 5);

        URI uri = URI.create("file:///test/document1.txt");
        String name = "document1.txt";
        recentlyUsedDocuments.put(uri, name);

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.get(0).first());
        assertEquals(name, entries.get(0).second());
    }

     void testPut_AddMultipleDocuments() {
        Preferences prefs = Preferences.userRoot().node("testPut_AddMultipleDocuments");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 5);

        URI uri1 = URI.create("file:///test/document1.txt");
        URI uri2 = URI.create("file:///test/document2.txt");
        recentlyUsedDocuments.put(uri1, "document1.txt");
        recentlyUsedDocuments.put(uri2, "document2.txt");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(2, entries.size());
        assertEquals(uri1, entries.get(0).first());
        assertEquals("document1.txt", entries.get(0).second());
        assertEquals(uri2, entries.get(1).first());
        assertEquals("document2.txt", entries.get(1).second());
    }

    @Test
    void testPut_ExceedCapacity() {
        Preferences prefs = Preferences.userRoot().node("testPut_ExceedCapacity");
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
        Preferences prefs = Preferences.userRoot().node("testPut_EmptyNameUsesUriPath");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document1.txt");
        recentlyUsedDocuments.put(uri, "");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.get(0).first());
        assertEquals("/test/document1.txt", entries.get(0).second()); // Uses full path from URI.
    }

    @Test
    void testPut_WithMissingName() {
        Preferences prefs = Preferences.userRoot().node("testPut_WithMissingName");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document2.txt");
        recentlyUsedDocuments.put(uri);

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.get(0).first());
        assertEquals("document2.txt", entries.get(0).second()); // Extracts file name from path.
    }

    @Test
    void testPut_ReplaceExistingEntry() {
        Preferences prefs = Preferences.userRoot().node("testPut_ReplaceExistingEntry");
        RecentlyUsedDocuments recentlyUsedDocuments = new RecentlyUsedDocuments(prefs, 3);

        URI uri = URI.create("file:///test/document1.txt");
        recentlyUsedDocuments.put(uri, "OriginalName.txt");
        recentlyUsedDocuments.put(uri, "UpdatedName.txt");

        List<Pair<URI, String>> entries = recentlyUsedDocuments.entries();
        assertEquals(1, entries.size());
        assertEquals(uri, entries.get(0).first());
        assertEquals("UpdatedName.txt", entries.get(0).second()); // Name is updated.
    }
}