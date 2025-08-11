package com.dua3.utility.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18NTest {

    private static final String TEST_KEY = "test.key";
    private static final String TEST_VALUE = "Test Value";
    private static final String TEST_PATTERN = "Hello, {0}!";
    private static final String TEST_ARG = "World";
    private static final String TEST_FORMATTED = "Hello, World!";

    private ResourceBundle testBundle;

    @BeforeEach
    void setUp() {
        testBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{TEST_KEY, TEST_VALUE}, {"test.pattern", TEST_PATTERN}};
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    void testCreate() {
        I18N i18n = I18N.create(testBundle);
        assertNotNull(i18n);
        assertEquals(Locale.ENGLISH, i18n.getLocale());
    }

    @Test
    void testGetString() {
        I18N i18n = I18N.create(testBundle);
        assertEquals(TEST_VALUE, i18n.get(TEST_KEY));
    }

    @Test
    void testGetMissingString() {
        I18N i18n = I18N.create(testBundle);
        assertThrows(MissingResourceException.class, () -> i18n.get("missing.key"));
    }

    @Test
    void testGetOrCompute() {
        I18N i18n = I18N.create(testBundle);
        assertEquals(TEST_VALUE, i18n.getOrCompute(TEST_KEY, key -> "Computed"));
        assertEquals("Computed", i18n.getOrCompute("missing.key", key -> "Computed"));
    }

    @Test
    void testFormat() {
        I18N i18n = I18N.create(testBundle);
        assertEquals(TEST_FORMATTED, i18n.format("test.pattern", TEST_ARG));
    }

    @Test
    void testMergeBundle() {
        I18N i18n = I18N.create(new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"original.key", "Original Value"}};
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        });

        // Test before merge
        assertEquals("Original Value", i18n.get("original.key"));
        assertThrows(MissingResourceException.class, () -> i18n.get(TEST_KEY));

        // Merge bundle
        i18n.mergeBundle(testBundle);

        // Test after merge
        assertEquals("Original Value", i18n.get("original.key"));
        assertEquals(TEST_VALUE, i18n.get(TEST_KEY));
    }

    @Test
    void testContainsKey() {
        I18N i18n = I18N.create(testBundle);
        assertTrue(i18n.isMapped(TEST_KEY));
        assertFalse(i18n.isMapped("missing.key"));
    }

    @Test
    void testGetInstance() {
        assertDoesNotThrow(I18N::getInstance);
    }

    @Test
    void testLookupBundle() {
        I18N i18n = I18N.create(testBundle);
        ResourceBundle bundle = i18n.lookupBundle(TEST_KEY);
        assertNotNull(bundle);
        assertEquals(TEST_VALUE, bundle.getString(TEST_KEY));
    }
}