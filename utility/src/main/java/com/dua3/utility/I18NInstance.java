package com.dua3.utility;

import com.dua3.utility.i18n.I18N;
import com.dua3.utility.i18n.I18NProxy;

/**
 * I18NInstance ensures that bundles from the current library have been loaded when
 * accessing the globale {@link I18N} instance.
 */
public final class I18NInstance {

    private static final I18NProxy proxy = new I18NProxy("com.dua3.utility.messages");

    private I18NInstance() {
    }

    /**
     * Provides access to the shared {@link I18N} instance of the application and ensures local bundles have been
     * loaded before access.
     *
     * @return the singleton {@link I18N} instance
     */
    public static I18N get() {
        return proxy.get();
    }
}
