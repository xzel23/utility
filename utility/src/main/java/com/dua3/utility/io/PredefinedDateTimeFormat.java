package com.dua3.utility.io;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;

public enum PredefinedDateTimeFormat {
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
    LOCALE_MEDIUM("short (locale dependent)", FormatStyle.MEDIUM),
    /**
     * Represents a locale-dependent long format style.
     * <p>
     * The LOCALE_LONG constant can be used to format dates and times in a long format, using the
     * locale-specific conventions.
     * <p>
     * The format style used is determined based on the locale set in the system. The format style
     * defines the format pattern used to represent the long date and time format.
     * <p>
     * Example usage:
     * <pre>
     *     LocalDate date = LocalDate.now();
     *     String formattedDate = date.format(LOCALE_LONG.getDateTimeFormatter());
     *     System.out.println(formattedDate);
     * </pre>
     *
     * @see FormatStyle#LONG
     * @see java.time.LocalDate#format(DateTimeFormatter)
     */
    LOCALE_LONG("long (locale dependent)", FormatStyle.LONG),
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
    ISO_DATE_TIME("ISO 8601 (2000-12-31T10:15:30)", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private final String name;

    private final Function<? super Locale, DateTimeFormatter> dateTimeFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> dateFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> timeFormatterFactory;

    PredefinedDateTimeFormat(String name, DateTimeFormatter formatter) {
        this.name = name;
        this.dateTimeFormatterFactory = locale -> formatter;
        this.dateFormatterFactory = locale -> formatter;
        this.timeFormatterFactory = locale -> formatter;
    }

    PredefinedDateTimeFormat(String name, FormatStyle style) {
        this.name = name;
        this.dateTimeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
        this.dateFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
        this.timeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedTime(style).withLocale(locale);
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
