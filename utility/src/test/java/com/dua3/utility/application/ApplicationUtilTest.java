package com.dua3.utility.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ApplicationUtilTest {

    @Test
    void testPreferencesShouldNotBeNull() {
        // Initialize with a valid Preferences instance
        assertNotNull(ApplicationUtil.preferences());
    }

    @Test
    void testRecentlyUsedDocumentsShouldReturnSameInstance() {
        var first = ApplicationUtil.recentlyUsedDocuments();
        var second = ApplicationUtil.recentlyUsedDocuments();
        assertSame( first, second);
    }
}