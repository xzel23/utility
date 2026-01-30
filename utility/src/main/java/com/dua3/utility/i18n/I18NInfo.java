package com.dua3.utility.i18n;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents internationalization (i18n) metadata for an application, including the
 * resource bundle name and supported languageTags.
 * <p>
 * This record holds essential information for managing localization via
 * resource bundles in combination with specified languageTags.
 *
 * @param bundle the name of the resource bundle used for localization.
 * @param languageTags collection of contained languageTags (language tags).
 */
public record I18NInfo(String bundle, Collection<String> languageTags) {
    private static final Logger LOG = LogManager.getLogger(I18NInfo.class);

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+");

    /**
     * Loads internationalization metadata from a resource file associated with the specified class.
     * The method reads the "i18n.properties" file, extracts the required properties, and constructs
     * an {@link I18NInfo} instance representing the resource bundle name and supported languageTags.
     *
     * @param clazz the class whose associated "i18n.properties" file should be loaded.
     * @return an {@link Optional} containing the parsed {@link I18NInfo} if the resource file
     *         is found and valid; otherwise, an empty {@link Optional}.
     * @throws IOException if an error occurs while reading the "i18n.properties" file.
     */
    public static Optional<I18NInfo> load(Class<?> clazz) throws IOException {
        return LangUtil.loadProperties(clazz, "i18n.properties").map(p -> {
            String bundle = p.getProperty("bundle");
            String locales = p.getProperty("languages");

            LangUtil.check(bundle != null, () -> new IllegalStateException("Missing 'bundle' property in i18n.properties"));
            LangUtil.check(locales != null, () -> new IllegalStateException("Missing 'languageTags' property in i18n.properties"));

            String[] languageArray = SPLIT_PATTERN.split(locales);
            Set<String> validLocales = HashSet.newHashSet(languageArray.length);
            Arrays.stream(languageArray).forEach(lang -> {
                if (!Locale.forLanguageTag(lang).toLanguageTag().equals(lang)) {
                    LOG.warn("Invalid language tag found in i18n.properties for {}: {}", clazz, lang);
                } else {
                    validLocales.add(lang);
                }
            });

            return new I18NInfo(bundle, Set.copyOf(validLocales));
        });
    }
}
