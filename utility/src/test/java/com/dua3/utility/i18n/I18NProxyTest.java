package com.dua3.utility.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class I18NProxyTest {

    @Test
    void testProxyGetAndSyncWithGlobalInstance() {
        I18N.init("", Locale.ENGLISH);

        I18NProxy proxy = new I18NProxy("com.dua3.utility.i18n.testbundle.messages");

        I18N i18n = proxy.get();
        assertNotNull(i18n);
        assertSame(I18N.getInstance(), i18n);
        assertEquals("Hello", i18n.get("greeting"));

        // Second call should return the cached instance reference
        I18N i18nSecond = proxy.get();
        assertSame(i18n, i18nSecond);

        // Re-initializing global I18N causes proxy to update and merge again
        I18N newGlobal = I18N.init("", Locale.GERMAN);
        I18N i18nThird = proxy.get();
        assertSame(newGlobal, i18nThird);
        assertEquals("Hallo", i18nThird.get("greeting"));
    }
}
