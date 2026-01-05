package com.dua3.utility.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.Objects;

/**
 * Functional interface for an Internationalization (I18N) provider.
 * This interface defines a single method that returns an instance of the I18N class.
 * Implementing classes should provide an implementation for the i18n() method, which can be used to retrieve the
 * localized text resources.
 * @deprecated use {@link I18N.Provider} instead
 */
@FunctionalInterface
@Deprecated
public interface I18NProvider extends I18N.Provider {
    /**
     * Retrieves an instance of the Internationalization (I18N) class.
     *
     * @return An instance of the I18N class that provides localized text resources.
     */
    I18N i18n();

    /**
     * Retrieves an instance of the Internationalization (I18N) class for the given locale.
     * <p>
     * Implementation note:
     * This method temporarily changes the default locale to the specified locale
     * during the execution of the I18N initialization and then restores it.
     * Implementers should ensure that the i18n(Locale) method is properly
     * supported to provide localized text resources.
     *
     * @param locale the locale for which the I18N instance should be initialized
     * @return an instance of the I18N class that provides localized text resources for the specified locale
     */
    default I18N i18n(Locale locale) {
        Locale defaultLocale = Locale.getDefault();
        if (!Objects.equals(locale, defaultLocale)) {
            Logger logger = LogManager.getLogger(I18NProvider.class);
            logger.warn("temporarily changing default locale for I18N initialization");
            logger.warn("Change your code to implement I18N.Provider instead!");
            logger.warn("I18NProvider will be removed in the next major release!");
            Locale.setDefault(locale);
            try {
                return i18n();
            } finally {
                Locale.setDefault(defaultLocale);
            }
        }
        return i18n();
    }
}
