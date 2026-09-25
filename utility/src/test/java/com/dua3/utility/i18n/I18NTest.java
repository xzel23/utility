package com.dua3.utility.i18n;

import com.dua3.utility.text.MessageFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("uses global I18N.init")
class I18NTest {

    @SuppressWarnings("RedundantFieldInitialization")
    private static Locale defaultLocale = null;

    @BeforeAll
    static void setLocale() {
        defaultLocale = Locale.getDefault();
    }

    @AfterAll
    static void restoreLocale() {
        Locale.setDefault(defaultLocale);
        I18N.init("", Locale.getDefault());
    }

    private static final String TEST_KEY = "test.key";
    private static final String TEST_VALUE = "Test Value";
    private static final String TEST_PATTERN = "Hello, {0}!";
    private static final String TEST_ARG = "World";
    private static final String TEST_FORMATTED = "Hello, World!";
    private static final String OBJECT_KEY = "test.object";
    private static final List<String> OBJECT_VALUE = List.of("A", "B", "C");

    private ResourceBundle testBundle;

    @BeforeEach
    void setUp() {
        testBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                        {TEST_KEY, TEST_VALUE},
                        {"test.pattern", TEST_PATTERN},
                        {OBJECT_KEY, OBJECT_VALUE}
                };
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    void testCreateFromResourceBundle() {
        I18N i18n = I18N.create(testBundle);
        assertNotNull(i18n);
        assertEquals(Locale.ENGLISH, i18n.getLocale());
    }

    @Test
    void testCreateWithEmptyBaseName() {
        I18N i18n = I18N.create("", Locale.GERMAN);
        assertNotNull(i18n);
        assertEquals(Locale.GERMAN, i18n.getLocale());
        assertFalse(i18n.isMapped("any.key"));
    }

    @Test
    void testCreateWithBaseName() {
        I18N i18n = I18N.create("com.dua3.utility.i18n.testbundle.messages", Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, i18n.getLocale());
        assertEquals("Hello", i18n.get("greeting"));

        // Fallback when requested locale is not explicitly provided (e.g., French falling back to default/root bundle)
        I18N i18nFallback = I18N.create("com.dua3.utility.i18n.testbundle.messages", Locale.FRENCH);
        assertNotNull(i18nFallback);
        assertEquals("Hello", i18nFallback.get("greeting"));
    }

    @Test
    void testInitAndGetInstance() {
        I18N initialized = I18N.init("com.dua3.utility.i18n.testbundle.messages", Locale.GERMAN);
        assertSame(initialized, I18N.getInstance());
        assertEquals(Locale.GERMAN, I18N.getInstance().getLocale());

        I18N initEnglish = I18N.init("com.dua3.utility.i18n.testbundle.messages", Locale.ENGLISH);
        assertSame(initEnglish, I18N.getInstance());
        assertDoesNotThrow(I18N::getInstance);
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
    void testGetObject() {
        I18N i18n = I18N.create(testBundle);
        assertEquals(OBJECT_VALUE, i18n.getObject(OBJECT_KEY));
        assertEquals(TEST_VALUE, i18n.getObject(TEST_KEY));
        assertThrows(MissingResourceException.class, () -> i18n.getObject("missing.key"));
    }

    @Test
    void testLiteral() {
        String literal = I18N.literal("Raw Text");
        assertEquals("\0Raw Text", literal);

        I18N i18n = I18N.create(testBundle);
        assertTrue(i18n.isLiteral(literal));
        assertFalse(i18n.isLiteral(TEST_KEY));
        assertFalse(i18n.isLiteral(""));
        //noinspection DataFlowIssue
        assertThrows(Throwable.class, () -> i18n.isLiteral(null));

        assertEquals("Raw Text", i18n.get(literal));
    }

    @Test
    void testGetOrCompute() {
        I18N i18n = I18N.create(testBundle);
        // Mapped key returns bundle value
        assertEquals(TEST_VALUE, i18n.getOrCompute(TEST_KEY, key -> "Computed"));
        // Unmapped key computes value
        assertEquals("Computed", i18n.getOrCompute("missing.key", key -> "Computed"));
        // Literal returns literal without computation
        assertEquals("Literal Text", i18n.getOrCompute(I18N.literal("Literal Text"), key -> "Computed"));
    }

    @Test
    void testFormat() {
        I18N i18n = I18N.create(testBundle);

        // Pattern in bundle
        assertEquals(TEST_FORMATTED, i18n.format("test.pattern", TEST_ARG));

        // Inline format string with placeholder
        assertEquals("Count is 5", i18n.format("Count is {0}", 5));

        // Literal format
        assertEquals("Literal format with {0}", i18n.format(I18N.literal("Literal format with {0}"), "ignored"));

        // Empty string
        assertEquals("", i18n.format(""));

        // Missing key without placeholder throws exception
        assertThrows(MissingResourceException.class, () -> i18n.format("missing.key.without.placeholder", "arg"));

        // MessageFormatterArgs overload
        MessageFormatter.MessageFormatterArgs args = MessageFormatter.args("test.pattern", TEST_ARG);
        assertEquals(TEST_FORMATTED, i18n.format(args));
    }

    @Test
    void testMergeBundle() {
        I18N i18n = I18N.create(new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"original.key", "Original Value"}, {TEST_KEY, "Existing"}};
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        });

        // Test before merge
        assertEquals("Original Value", i18n.get("original.key"));
        assertEquals("Existing", i18n.get(TEST_KEY));

        // Merge bundle (putIfAbsent keeps existing keys)
        i18n.mergeBundle(testBundle);

        // Test after merge
        assertEquals("Original Value", i18n.get("original.key"));
        assertEquals("Existing", i18n.get(TEST_KEY));
        assertEquals(TEST_PATTERN, i18n.get("test.pattern"));
    }

    @Test
    void testMergeBundleByBaseNameAndLocale() {
        I18N i18n = I18N.create("", Locale.GERMAN);
        assertFalse(i18n.isMapped("greeting"));

        i18n.mergeBundle("com.dua3.utility.i18n.testbundle.messages", Locale.GERMAN);
        assertTrue(i18n.isMapped("greeting"));
        assertEquals("Hallo", i18n.get("greeting"));
    }

    @Test
    void testContainsKey() {
        I18N i18n = I18N.create(testBundle);
        assertTrue(i18n.isMapped(TEST_KEY));
        assertFalse(i18n.isMapped("missing.key"));
    }

    @Test
    void testLookupBundle() {
        I18N i18n = I18N.create(testBundle);
        ResourceBundle bundle = i18n.lookupBundle(TEST_KEY);
        assertNotNull(bundle);
        assertEquals(TEST_VALUE, bundle.getString(TEST_KEY));

        // Unmapped key returns mainBundle
        ResourceBundle fallbackBundle = i18n.lookupBundle("non.existent.key");
        assertNotNull(fallbackBundle);
        assertSame(testBundle, fallbackBundle);
    }

    @Test
    void testGetBundle() {
        I18N i18n = I18N.create(testBundle);
        Optional<ResourceBundle> bundleOpt = i18n.getBundle(TEST_KEY);
        assertTrue(bundleOpt.isPresent());
        assertSame(testBundle, bundleOpt.get());

        Optional<ResourceBundle> missingOpt = i18n.getBundle("missing.key");
        assertFalse(missingOpt.isPresent());
    }

    @Test
    void testLookupBundleWithLoader() {
        I18N i18n = I18N.create(testBundle);

        // 1. Key already mapped -> loader should not be invoked
        AtomicBoolean loaderCalled = new AtomicBoolean(false);
        Optional<ResourceBundle> result1 = i18n.lookupBundle(TEST_KEY, locale -> {
            loaderCalled.set(true);
            return testBundle;
        });
        assertTrue(result1.isPresent());
        assertSame(testBundle, result1.get());
        assertFalse(loaderCalled.get());

        // 2. Key unmapped, loader returns bundle containing key -> loaded bundle merged and returned
        ResourceBundle dynamicBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][]{{"dynamic.key", "Dynamic Value"}};
            }
        };
        Optional<ResourceBundle> result2 = i18n.lookupBundle("dynamic.key", locale -> dynamicBundle);
        assertTrue(result2.isPresent());
        assertSame(dynamicBundle, result2.get());
        assertTrue(i18n.isMapped("dynamic.key"));
        assertEquals("Dynamic Value", i18n.get("dynamic.key"));

        // 3. Key unmapped, loader returns bundle NOT containing key -> returns Optional.empty()
        Optional<ResourceBundle> result3 = i18n.lookupBundle("unmatched.key", locale -> dynamicBundle);
        assertFalse(result3.isPresent());

        // 4. Key unmapped, loader returns null -> returns Optional.empty()
        Optional<ResourceBundle> result4 = i18n.lookupBundle("null.key", locale -> null);
        assertFalse(result4.isPresent());
    }

    @Test
    void testToString() {
        I18N i18n = I18N.create(testBundle);
        String str = i18n.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("I18N{mainBundle="));
        assertTrue(str.contains(testBundle.toString()));
    }
}
