package com.dua3.utility.io;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public enum PredefinedDateFormat {
    LOCALE_DEFAULT("locale dependent", PredefinedDateFormat::formatFromLocale),
    LOCALE_SHORT("short (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.MEDIUM)),
    LOCALE_LONG("long (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.LONG)),
    ISO_DATE("ISO 8601 (2000-12-31)", locale -> DateTimeFormatter.ISO_LOCAL_DATE);

    private static final Pattern PATTERN_YEAR_PATTERN = Pattern.compile("\\byy\\b");

    /**
     * Create a date locale dependent date format.
     *
     * @param locale the locale
     * @param style  the {@link FormatStyle} to use
     * @return the DateFormatter
     */
    private static DateTimeFormatter formatFromLocale(Locale locale, FormatStyle style) {
        return DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
    }

    /**
     * Create a date locale dependent short date format with a four-digit year.
     *
     * @param locale the locale
     * @return the DateFormatter
     */
    private static DateTimeFormatter formatFromLocale(Locale locale) {
        String formatPattern =
                DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        FormatStyle.SHORT,
                        null,
                        IsoChronology.INSTANCE,
                        locale);
        formatPattern = PATTERN_YEAR_PATTERN.matcher(formatPattern).replaceAll("yyyy");
        return DateTimeFormatter.ofPattern(formatPattern, locale);
    }

    private final String name;

    private final Function<? super Locale, DateTimeFormatter> factory;

    PredefinedDateFormat(String name, Function<? super Locale, DateTimeFormatter> factory) {
        this.name = name;
        this.factory = factory;
    }

    public DateTimeFormatter getFormatter(Locale locale) {
        return factory.apply(locale);
    }

    @Override
    public String toString() {
        return name;
    }
}
