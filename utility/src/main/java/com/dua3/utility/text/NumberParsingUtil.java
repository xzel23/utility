package com.dua3.utility.text;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Utility for parsing localized numeric text into fractional double values.
 * Supports plain numbers, localized percent format, and explicit percent/per-mille suffixes.
 */
final class NumberParsingUtil {

    /**
     * Prevent instantiation of utility class.
     */
    private NumberParsingUtil() {
        // utility class
    }

    /**
     * Parse text as a localized number, percent, or per-mille value.
     *
     * @param text the input text
     * @param locale the locale used for parsing
     * @return the parsed value
     * @throws NumberFormatException if parsing fails
     */
    static double parseDouble(String text, Locale locale) throws NumberFormatException {
        OptionalDouble v = tryParseDouble(text, locale);

        if (v.isPresent()) {
            return v.getAsDouble();
        }

        // only executed in case of parsing failure to reproduce the original exception
        try {
            return NumberFormat.getNumberInstance(locale).parse(text).doubleValue();
        } catch (ParseException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }

    /**
     * Try to parse text as a localized number, percent, or per-mille value.
     *
     * @param text the input text
     * @param locale the locale used for parsing
     * @return parsed value, or empty if parsing fails
     */
    static OptionalDouble tryParseDouble(String text, Locale locale) {
        if (text.isEmpty()) {
            return OptionalDouble.empty();
        }

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);

        // Keep parsing order deterministic: plain numbers first, then localized percent,
        // then explicit symbol handling (% / ‰).
        OptionalDouble plain = tryParseWithFormat(text, NumberFormat.getNumberInstance(locale));
        if (plain.isPresent()) {
            return plain;
        }

        OptionalDouble percent = tryParseWithFormat(text, NumberFormat.getPercentInstance(locale));
        if (percent.isPresent()) {
            return percent;
        }

        OptionalDouble explicitPercent = tryParseWithMultiplier(text, locale, 100d, String.valueOf(symbols.getPercent()), "%");
        if (explicitPercent.isPresent()) {
            return explicitPercent;
        }

        return tryParseWithMultiplier(text, locale, 1000d, String.valueOf(symbols.getPerMill()), "‰");
    }

    /**
     * Try parsing a value with a scaling symbol (% or ‰).
     */
    private static OptionalDouble tryParseWithMultiplier(String text, Locale locale, double divisor, String localeSymbol, String fallbackSymbol) {
        Optional<String> numberText = removeSymbol(text, localeSymbol).or(() -> removeSymbol(text, fallbackSymbol));
        if (numberText.isEmpty()) {
            return OptionalDouble.empty();
        }

        OptionalDouble parsed = tryParseWithFormat(numberText.get(), NumberFormat.getNumberInstance(locale));
        if (parsed.isEmpty()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(parsed.getAsDouble() / divisor);
    }

    /**
     * Remove a symbol from the start or end of text.
     */
    private static Optional<String> removeSymbol(String text, String symbol) {
        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        if (text.startsWith(symbol)) {
            return Optional.of(text.substring(symbol.length()).strip());
        }

        if (text.endsWith(symbol)) {
            return Optional.of(text.substring(0, text.length() - symbol.length()).strip());
        }

        return Optional.empty();
    }

    /**
     * Parse text with strict full-input consumption.
     */
    private static OptionalDouble tryParseWithFormat(String text, NumberFormat format) {
        ParsePosition pos = new ParsePosition(0);
        Number parsed = format.parse(text, pos);
        // Require full match so trailing garbage does not silently pass.
        if (parsed != null && pos.getErrorIndex() < 0 && pos.getIndex() == text.length()) {
            return OptionalDouble.of(parsed.doubleValue());
        }
        return OptionalDouble.empty();
    }
}
