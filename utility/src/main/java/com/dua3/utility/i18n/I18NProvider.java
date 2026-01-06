package com.dua3.utility.i18n;

import java.util.Locale;

/**
 * Functional interface for an Internationalization (I18N) provider.
 * This interface defines a single method that returns an instance of the I18N class.
 * Implementing classes should provide an implementation for the i18n() method, which can be used to retrieve the
 * localized text resources.
 * @deprecated call {@link I18N#init(String, Locale)} instead
 */
@FunctionalInterface
@Deprecated(forRemoval = true)
public interface I18NProvider {
    /**
     * Retrieves an instance of the Internationalization (I18N) class.
     *
     * @return An instance of the I18N class that provides localized text resources.
     */
    I18N i18n();
}
