package com.dua3.utility.i18n;

import com.dua3.utility.lang.StreamUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The I18N class provides internationalization support for applications.
 * It retrieves localized strings from one or more resource bundles based on the requested locale.
 * <p>
 * This class is intended to control resource management in internationalized environments.
 * The Application class
 * should instantiate a single instance of this class with its main {@link ResourceBundle}.
 * The {@link #getLocale()}
 * method will return the locale of the main bundle, and it should be passed down to used libraries so that those
 * can select the appropriate resource bundle for that locale.
 * This prevents the main application falling back
 * to the default locale while the libraries using another locale, for example, if the application runs on a system
 * configured to use a French locale but not providing a bundle matching that locale, this prevents mixed output
 * using english texts from the application bundle and French texts from the library bundle.
 * <p>
 * If the library is built using the I18N class, it should not directly use the library bundle but instead use
 * {@link #mergeBundle(ResourceBundle)} to add its own bundle to the instance.
 * Only keys not yet present will be added to the I18N instance.
 * This makes it possible for the application to customize library resources
 * by adding a mapping for the library resource key to be customized.
 * <p>
 * The application should implement the {@link I18NProvider} interface to announce the I18N instance to libraries.
 * Once initialised, libraries can get the instance using {@link I18N#getInstance()} and merge their own bundles.
 * <p>
 * If multiple providers are found, class initialization fails with an {@link IllegalStateException}.
 */
public final class I18N {
    private static final Logger LOG = LogManager.getLogger(I18N.class);

    @FunctionalInterface
    public interface Provider {
        I18N i18n(Locale locale);
    }

    private final ResourceBundle mainBundle;
    private final Map<String, ResourceBundle> bundleMap = new HashMap<>();

    private static AtomicReference<I18N> INSTANCE = new AtomicReference<>(loadAndInitInstance(Locale.getDefault()));

    private static I18N loadAndInitInstance(Locale locale) {
        return Stream.concat(
                        ServiceLoader.load(I18N.Provider.class).stream(),
                        ServiceLoader.load(I18NProvider.class).stream() // TODO remove in version 21
                )
                .findFirst()
                .map(provider -> {
                    Provider i18nProvider = provider.get();
                    LOG.debug("I18N.Provider: {}", i18nProvider.getClass().getName());
                    return i18nProvider.i18n(locale);
                })
                .orElseGet(() -> {
                    LOG.debug("no I18N.Provider found, creating empty instance");
                    return createEmptyInstance(locale);
                });
    }

    private static I18N createEmptyInstance(Locale locale) {
        ResourceBundle bundle = new ListResourceBundle() {
            private static final Object[][] EMPTY_CONTENT = {};

            @Override
            protected Object[][] getContents() {
                return EMPTY_CONTENT;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }
        };
        return create(bundle);
    }

    /**
     * Get the global instance.
     * @return the singleton instance.
     */
    public static I18N getInstance() {
        return INSTANCE.get();
    }

    public static void resetInstance(Locale locale) {
        INSTANCE.set(loadAndInitInstance(locale));
    }

    /**
     * Creates an instance of the I18N class.
     *
     * @param baseName The base name of the resource bundle.
     * @param locale   The locale to be used for localization.
     * @return An instance of the I18N class.
     */
    public static I18N create(String baseName, Locale locale) {
        LOG.trace("creating an instance for {} with requested locale {}", baseName, locale);
        ResourceBundle bundle = getResourceBundle(baseName, locale);
        return new I18N(bundle);
    }

    /**
     * Retrieves a ResourceBundle for the given base name and locale. If the requested locale is not available,
     * logs a message and falls back to the closest matching locale or the default bundle.
     *
     * @param baseName The base name of the resource bundle.
     * @param locale   The requested locale for which the resource bundle is sought.
     * @return The ResourceBundle corresponding to the given base name and locale.
     */
    public static ResourceBundle getResourceBundle(String baseName, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        Locale bundleLocale = bundle.getLocale();
        if (!Objects.equals(locale, bundleLocale)) {
            String bundleLocaleName = String.valueOf(bundleLocale);
            if (!bundleLocaleName.isEmpty()) {
                LOG.debug("requested locale {} not available, falling back to {}", locale, bundleLocaleName);
            } else {
                LOG.debug("requested locale {} not available, falling back to the default bundle", locale);
            }
        }
        return bundle;
    }

    /**
     * Creates an instance of the I18N class with the provided resource bundle.
     *
     * @param bundle The resource bundle to use.
     * @return An instance of the I18N class.
     */
    public static I18N create(ResourceBundle bundle) {
        return new I18N(bundle);
    }

    /**
     * Creates an instance of the I18N class with the provided resource bundle.
     *
     * @param bundle The resource bundle to use.
     */
    private I18N(ResourceBundle bundle) {
        this.mainBundle = bundle;
        mergeBundle(bundle);
    }

    /**
     * Merges a resource bundle identified by its base name and locale into the internal bundleMap.
     *
     * @param baseName The base name of the resource bundle.
     * @param locale   The locale associated with the resource bundle.
     */
    public void mergeBundle(String baseName, Locale locale) {
        mergeBundle(getResourceBundle(baseName, locale));
    }

    /**
     * Merges the given resource bundle into the internal bundleMap.
     *
     * @param bundle The resource bundle to merge.
     */
    public void mergeBundle(ResourceBundle bundle) {
        LOG.trace("merging resource bundle {}_{}", bundle::getBaseBundleName, bundle::getLocale);
        bundle.getKeys().asIterator()
                .forEachRemaining(key -> bundleMap.putIfAbsent(key, bundle));
    }

    /**
     * Retrieves the ResourceBundle associated with the given key from the bundleMap.
     * If the key is not found, the mainBundle is returned.
     *
     * @param key The key to lookup in the bundleMap.
     * @return The retrieved ResourceBundle or the mainBundle if the key is not found.
     */
    public ResourceBundle lookupBundle(String key) {
        return bundleMap.getOrDefault(key, mainBundle);
    }

    /**
     * Retrieves the ResourceBundle associated with the given key from the bundleMap.
     * If the key is not found, an empty Optional is returned.
     *
     * @param key The key to lookup in the bundleMap.
     * @return Optional containing the bundle the key is mapped to
     */
    public Optional<ResourceBundle> getBundle(String key) {
        return Optional.ofNullable(bundleMap.get(key));
    }

    /**
     * Retrieves the ResourceBundle associated with the given key from the bundleMap.
     * If the key is not found, a new bundle is loaded using the supplied loader, and if
     * the key is contained in the new bundle, the new bundle is merged and returned.
     *
     * @param key The key to lookup in the bundleMap.
     * @param bundleLoader The function used to load the ResourceBundle for the given locale.
     * @return An Optional containing the ResourceBundle for the key, or an empty Optional if the key is not found.
     */
    public Optional<ResourceBundle> lookupBundle(String key, Function<Locale, ResourceBundle> bundleLoader) {
        return Optional.ofNullable(bundleMap.computeIfAbsent(key, k -> {
            ResourceBundle newBundle = bundleLoader.apply(mainBundle.getLocale());
            if (!newBundle.containsKey(k)) {
                return null;
            }
            mergeBundle(newBundle);
            return newBundle;
        }));
    }

    /**
     * Retrieves the locale used for localization.
     *
     * @return The locale used for localization.
     */
    public Locale getLocale() {
        return mainBundle.getLocale();
    }

    /**
     * Retrieves the object for the given key from the resource bundle.
     *
     * @param key the key that represents the string to be retrieved.
     * @return the object bound to the given key.
     * @see ResourceBundle#getObject(String)
     */
    public Object getObject(String key) {
        return lookupBundle(key).getObject(key);
    }

    /**
     * Retrieves the localized string for the given key from the resource bundle.
     *
     * @param key The key that represents the string to be retrieved.
     * @return The localized string for the given key.
     * @throws MissingResourceException – if no object for the given key can be found
     * @throws ClassCastException – if the object found for the given key is not a string
     * @see ResourceBundle#getString(String)
     */
    public String get(String key) {
        return lookupBundle(key).getString(key);
    }

    /**
     * Retrieves the localized string for the given key from the resource bundle
     * or computes a result if the key is not mapped.
     * <p>
     * The computed value is not added to this instance.
     *
     * @param key The key that represents the string to be retrieved.
     * @param compute the function that computes values for unmapped keys
     * @return The localized string for the given key.
     * @see ResourceBundle#getString(String)
     */
    public String getOrCompute(String key, Function<? super String, String> compute) {
        return getBundle(key)
                .map(bundle -> bundle.getString(key))
                .orElseGet(() -> compute.apply(key));
    }

    /**
     * Formats a string using either a pattern or a localization key with optional arguments.
     * If {@code keyOrPattern} contains at least one '{', it is considered a pattern, otherwise
     * a localization key to retrieve the pattern
     *
     * @param keyOrPattern the localization key or the pattern to format
     * @param args optional arguments to be formatted into the pattern
     * @return the formatted string or an empty string if {@code keyOrPattern} is empty
     */
    public String format(String keyOrPattern, @Nullable Object... args) {
        if (keyOrPattern.isEmpty()) {
            return "";
        }
        String pattern = keyOrPattern.contains("{") ? keyOrPattern : lookupBundle(keyOrPattern).getString(keyOrPattern);
        return MessageFormat.format(pattern, args);
    }

    /**
     * Checks if the given key is present in the bundleMap.
     *
     * @param key The key to check.
     * @return {@code true} if the key is present, {@code false} otherwise.
     */
    public boolean isMapped(String key) {
        return bundleMap.containsKey(key);
    }

    @Override
    public String toString() {
        return "I18N{" +
                "mainBundle=" + mainBundle +
                '}';
    }
}
