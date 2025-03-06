package com.dua3.utility.i18n;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.function.Function;

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
 */
public class I18N {
    private static final Logger LOG = LogManager.getLogger(I18N.class);

    private final ResourceBundle mainBundle;
    private final Map<String, ResourceBundle> bundleMap = new HashMap<>();

    private static final I18N INSTANCE;

    static {
        Iterator<I18NProvider> serviceIterator = ServiceLoader
                .load(I18NProvider.class)
                .iterator();

        I18N i18n;
        if (!serviceIterator.hasNext()) {
            ResourceBundle bundle = new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[0][];
                }
            };
            i18n = create(bundle);
            LOG.warn("No I18N provider found. Creating empty I18N instance.");
        } else {
            i18n = serviceIterator.next().i18n();
        }

        if (serviceIterator.hasNext()) {
            throw new IllegalStateException(
                    "multiple I18N providers found: " + i18n.getClass().getName()
                            + ", " + serviceIterator.next().i18n().getClass().getName()
            );
        }

        LOG.debug("I18N provider: {}", i18n.getClass().getName());

        INSTANCE = i18n;
    }

    /**
     * Get the global instance.
     * @return the singleton instance.
     */
    public static I18N getInstance() {
        return INSTANCE;
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
     * Merges the given resource bundle into the internal bundleMap.
     *
     * @param bundle The resource bundle to merge.
     */
    public void mergeBundle(ResourceBundle bundle) {
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            bundleMap.putIfAbsent(key, bundle);
        }
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
     * @return The ResourceBundle containing the key, or {@code null} if the key is not found.
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
     * Creates an instance of the I18N class.
     *
     * @param baseName The base name of the resource bundle.
     * @param locale   The locale to be used for localization.
     * @return An instance of the I18N class.
     */
    public static I18N create(String baseName, Locale locale) {
        LOG.debug("creating instance for {} with requested locale {}", baseName, locale);
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
        Locale bundleLocale = bundle.getLocale();
        if (!Objects.equals(locale, bundleLocale)) {
            String bundleLocaleName = String.valueOf(bundleLocale);
            if (!bundleLocaleName.isEmpty()) {
                LOG.info("requested locale {} not available, falling back to {}", locale, bundleLocaleName);
            } else {
                LOG.warn("requested locale {} not available, falling back to the default bundle", locale);
            }
        }
        return new I18N(bundle);
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
     * @see ResourceBundle#getString(String)
     */
    public String get(String key) {
        return lookupBundle(key).getString(key);
    }

    /**
     * Retrieves the localized string for the given key from the resource bundle.
     *
     * @param key The key that represents the string to be retrieved.
     * @param defaultValue the value to use when no localized string can be found
     * @return The localized string for the given key.
     * @see ResourceBundle#getString(String)
     */
    public String getOrDefault(String key, String defaultValue) {
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
        return getBundle(key).map(bundle -> bundle.getString(key)).orElse(compute.apply(key));
    }

    /**
     * Retrieves the formatted localized string for the given key from the resource bundle,
     * using the provided arguments.
     *
     * @param key  The key that represents the string pattern to be retrieved.
     * @param args The arguments to be formatted into the string pattern.
     * @return The formatted string for the given key and arguments.
     * @see ResourceBundle#getString(String)
     * @see MessageFormat#format(String, Object...)
     */
    public String format(String key, Object... args) {
        String pattern = lookupBundle(key).getString(key);
        return MessageFormat.format(pattern, args);
    }

    /**
     * Checks if the given key is present in the bundleMap.
     *
     * @param key The key to check.
     * @return {@code true} if the key is present, {@code false} otherwise.
     */
    public boolean containsKey(String key) {
        return bundleMap.containsKey(key);
    }
}
