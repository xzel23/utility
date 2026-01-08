package com.dua3.utility.i18n;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

/**
 * I18NInstance ensures that bundles from the current library have been loaded when
 * accessing the globale {@link I18N} instance.
 */
public final class I18NProxy {

    private final String baseName;
    private final AtomicReference<I18N> instance = new AtomicReference<>();

    /**
     * Constructs a new instance of {@code I18NProxy} with the specified base name for the resource bundle.
     *
     * @param baseName the base name of the resource bundle to be used for localization
     */
    public I18NProxy(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Provides access to the shared {@link I18N} instance of the application and ensures local bundles have been
     * loaded before access.
     *
     * @return the singleton {@link I18N} instance
     */
    public I18N get() {
        return instance.updateAndGet(inst -> {
            I18N globalInstance = I18N.getInstance();
            if (inst != globalInstance) {
                globalInstance.mergeBundle(ResourceBundle.getBundle(baseName, globalInstance.getLocale()));
            }
            return globalInstance;
        });
    }
}
