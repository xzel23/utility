package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Enum class with entries for standard font mappers. This class implements the
 * Function interface to provide a mapping from one font family name to another.
 */
public enum StandardFontMapper implements Function<String, String> {
    /**
     * Do not map the font family name.
     */
    IDENTITY(Function.identity()),
    /**
     * Remove subset prefixes from partially embedded fonts invPDF-files.
     * <p>
     * Example usage:
     * <pre>
     * String input = "ABCDEF+Arial";
     * String result = IGNORE_SUBSETS.apply(input);
     * System.out.println(result); // Output: "Arial"
     * </pre>
     */
    IGNORE_SUBSETS(StandardFontMapper::removeSubsetTag),
    /**
     * Map known font name aliases to the corresponding standard font.
     * <p>
     * Example usage:
     * <pre>
     * String fontAlias = "ArialMT";
     * String fontName = KNOWN_ALIASES.apply(fontAlias);
     * System.out.println(fontName); // Output: Arial
     * </pre>
     */
    KNOWN_ALIASES(StandardFontMapper::replaceKnownAliases),
    /**
     * Remove subset prefixes from font names, then map known font name aliases to the corresponding standard font.
     * <p>
     * Example usage:
     * <pre>
     * String fontAlias = "ABCDEF+ArialMT";
     * String fontName = KNOWN_ALIASES.apply(fontAlias);
     * System.out.println(fontName); // Output: Arial
     * </pre>
     */
    IGNORE_SUBSETS_AND_KNOWN_ALIASES(StandardFontMapper::removeSubsetTagAndReplaceKnownAliases);

    private static String removeSubsetTagAndReplaceKnownAliases(@Nullable String s) {
        return replaceKnownAliases(removeSubsetTag(s));
    }

    private static String replaceKnownAliases(@Nullable String s) {
        if (s == null) {
            return null;
        }
        return switch (s) {
            case "ArialMT" -> "Arial";
            case "TimesNewRomanPSMT", "Times-Roman" -> "Times New Roman";
            case "CourierNewPSMT" -> "Courier New";
            default -> s;
        };
    }

    private static final Pattern PATTERN_SUBSET_TAG = Pattern.compile("^[A-Z]{6}\\+");

    private static String removeSubsetTag(@Nullable String s) {
        return s == null ? s : PATTERN_SUBSET_TAG.matcher(s).replaceFirst("");
    }

    private final Function<String, String> mapper;

    StandardFontMapper(Function<String, String> mapper) {
        this.mapper = mapper;
    }

    @Override
    public String apply(@Nullable String s) {
        return mapper.apply(s);
    }
}