package com.dua3.utility.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationUtilTest {

    @Test
    void testPreferencesShouldNotBeNull() {
        // Initialize with a valid Preferences instance
        assertNotNull(ApplicationUtil.preferences());
    }

}