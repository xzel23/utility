package com.dua3.utility.io;

import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The PredefinedDateTimeFormat class defines a set of predefined date and time formats.
 * Each format represents a specific style and pattern for formatting dates and times.
 */
public enum PredefinedDateTimeFormat {
    /**
     * The default format to use for the current locale.
     */
    LOCALE_DEFAULT("locale dependent", PredefinedDateTimeFormat::formatDateTimeFromLocale, PredefinedDateTimeFormat::formatDateFromLocale, PredefinedDateTimeFormat::formatTimeFromLocale),

    /**
     * Represents a locale-dependent short format style.
     * <p>
     * The LOCALE_SHORT constant can be used to format dates and times in a short format, using the
     * locale-specific conventions.
     * <p>
     * The format style used is determined based on the locale set in the system. The format style
     * defines the format pattern used to represent the short date and time format.
     * <p>
     * Example usage:
     * <pre>
     *     LocalDate date = LocalDate.now();
     *     String formattedDate = date.format(LOCALE_SHORT.getDateTimeFormatter());
     *     System.out.println(formattedDate);
     * </pre>
     *
     * @see FormatStyle#SHORT
     * @see java.time.LocalDate#format(DateTimeFormatter)
     */
    LOCALE_SHORT("short (locale dependent)", FormatStyle.SHORT),
    /**
     * Represents a locale-dependent medium format style.
     * <p>
     * The LOCALE_MEDIUM constant can be used to format dates and times in a medium format, using the
     * locale-specific conventions.
     * <p>
     * The format style used is determined based on the locale set in the system. The format style
     * defines the format pattern used to represent the medium date and time format.
     * <p>
     * Example usage:
     * <pre>
     *     LocalDate date = LocalDate.now();
     *     String formattedDate = date.format(LOCALE_MEDIUM.getDateTimeFormatter());
     *     System.out.println(formattedDate);
     * </pre>
     *
     * @see FormatStyle#MEDIUM
     * @see java.time.LocalDate#format(DateTimeFormatter)
     */
    LOCALE_MEDIUM("medium (locale dependent)", FormatStyle.MEDIUM),
    /**
     * Represents the format for ISO 8601 formatted date and time.
     * <p>
     * The ISO_DATE_TIME constant can be used to format dates and times in standard ISO format, independent of the
     * locale setting.
     * <p>
     * Example usage:
     * <pre>
     *     LocalDateTime now = LocalDateTime.now();
     *     String formattedDateTime = now.format(ISO_DATE_TIME.getFormatter());
     *     System.out.println(formattedDateTime); // e.g. 2022-04-28T12:35:20
     * </pre>
     *
     * @see java.time.LocalDate#format(DateTimeFormatter)
     */
    ISO_DATE_TIME("ISO 8601 (2000-12-31T10:15:30)", locale -> DateTimeFormatter.ISO_LOCAL_DATE_TIME, locale -> DateTimeFormatter.ISO_LOCAL_DATE, locale -> DateTimeFormatter.ISO_LOCAL_TIME);

    static final Pattern PATTERN_YEAR_PATTERN = Pattern.compile("\\byy\\b");

    private final String name;

    private final Function<? super Locale, DateTimeFormatter> dateTimeFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> dateFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> timeFormatterFactory;

    PredefinedDateTimeFormat(String name, Function<? super Locale, DateTimeFormatter> dateTimeFormatterFactory, Function<? super Locale, DateTimeFormatter> dateFormatterFactory, Function<? super Locale, DateTimeFormatter> timeFormatterFactory) {
        this.name = name;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.dateFormatterFactory = dateFormatterFactory;
        this.timeFormatterFactory = timeFormatterFactory;
    }

    PredefinedDateTimeFormat(String name, FormatStyle style) {
        this.name = name;
        this.dateTimeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
        this.dateFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
        this.timeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedTime(style).withLocale(locale);
    }

    /**
     * Create a locale dependent DateTimeFormatter for a short date time format with a four-digit year.
     *
     * @param locale the locale
     * @return the DateTimeFormatter
     */
    private static DateTimeFormatter formatDateTimeFromLocale(Locale locale) {
        String formatPattern =
                DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        FormatStyle.SHORT,
                        FormatStyle.MEDIUM,
                        IsoChronology.INSTANCE,
                        locale);
        formatPattern = PATTERN_YEAR_PATTERN.matcher(formatPattern).replaceAll("yyyy");
        return DateTimeFormatter.ofPattern(formatPattern, locale);
    }

    /**
     * Create a locale dependent DateTimeFormatter for a short date format with a four-digit year.
     *
     * @param locale the locale
     * @return the DateFormatter
     */
    private static DateTimeFormatter formatDateFromLocale(Locale locale) {
        String formatPattern =
                DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        FormatStyle.SHORT,
                        null,
                        IsoChronology.INSTANCE,
                        locale);
        formatPattern = PATTERN_YEAR_PATTERN.matcher(formatPattern).replaceAll("yyyy");
        return DateTimeFormatter.ofPattern(formatPattern, locale);
    }

    /**
     * Create a locale dependent DateTimeFormatter for a time format.
     *
     * @param locale the locale
     * @return the DateFormatter
     */
    private static DateTimeFormatter formatTimeFromLocale(Locale locale) {
        String formatPattern =
                DateTimeFormatterBuilder.getLocalizedDateTimePattern(
                        null,
                        FormatStyle.SHORT,
                        IsoChronology.INSTANCE,
                        locale);
        formatPattern = PATTERN_YEAR_PATTERN.matcher(formatPattern).replaceAll("yyyy");
        return DateTimeFormatter.ofPattern(formatPattern, locale);
    }

    /**
     * Retrieves a DateTimeFormatter based on the provided Locale.
     *
     * @param locale The Locale to be used for formatting the date and time.
     * @return A DateTimeFormatter that supports formatting based on the specified Locale.
     */
    public DateTimeFormatter getDateTimeFormatter(Locale locale) {
        return dateTimeFormatterFactory.apply(locale);
    }

    /**
     * Retrieves a DateTimeFormatter based on the provided Locale.
     *
     * @param locale The Locale to be used for formatting the date.
     * @return A DateTimeFormatter that supports formatting based on the specified Locale.
     */
    public DateTimeFormatter getDateFormatter(Locale locale) {
        return dateFormatterFactory.apply(locale);
    }

    /**
     * Retrieves a DateTimeFormatter based on the provided Locale.
     *
     * @param locale The Locale to be used for formatting the time.
     * @return A DateTimeFormatter that supports formatting based on the specified Locale.
     */
    public DateTimeFormatter getTimeFormatter(Locale locale) {
        return timeFormatterFactory.apply(locale);
    }

    @Override
    public String toString() {
        return name;
    }
}
