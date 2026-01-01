package com.dua3.utility;

import com.dua3.utility.i18n.I18N;

import java.util.ResourceBundle;

/**
 * I18NInstance ensures that bundles from the current library have been loaded when
 * accessing the globale {@link I18N} instance.
 */
public final class I18NInstance {

    private static final class Holder {
        private static final I18N INSTANCE = initInstance();

        private static I18N initInstance() {
            I18N i18N = I18N.getInstance();

            i18N.mergeBundle(ResourceBundle.getBundle("com.dua3.messages", i18N.getLocale()));

            return i18N;
        }
    }

    private I18NInstance() {
        // Private constructor to prevent instantiation
    }

    /**
     * Provides access to the shared {@link I18N} instance of the application and ensures local bundles have been
     * loaded before access.
     *
     * @return the singleton {@link I18N} instance
     */
    public static I18N get() {
        return Holder.INSTANCE;
    }
}
