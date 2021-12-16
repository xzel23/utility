package com.dua3.utility.io;

import com.dua3.cabe.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;

public enum PredefinedDateTimeFormat {
    LOCALE_SHORT("short (locale dependent)", FormatStyle.SHORT),
    LOCALE_MEDIUM("short (locale dependent)", FormatStyle.MEDIUM),
    LOCALE_LONG("long (locale dependent)", FormatStyle.LONG),
    ISO_DATE_TIME("ISO 8601 (2000-12-31T10:15:30)", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private static DateTimeFormatter formatFromPattern(@NotNull String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    private final String name;

    private final Function<? super Locale, DateTimeFormatter> dateTimeFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> dateFormatterFactory;
    private final Function<? super Locale, DateTimeFormatter> timeFormatterFactory;

    PredefinedDateTimeFormat(@NotNull String name, @NotNull DateTimeFormatter formatter) {
        this.name = name;
        this.dateTimeFormatterFactory = locale -> formatter;
        this.dateFormatterFactory = locale -> formatter;
        this.timeFormatterFactory = locale -> formatter;
    }

    PredefinedDateTimeFormat(@NotNull String name, @NotNull FormatStyle style) {
        this.name = name;
        this.dateTimeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
        this.dateFormatterFactory = locale -> DateTimeFormatter.ofLocalizedDate(style).withLocale(locale);
        this.timeFormatterFactory = locale -> DateTimeFormatter.ofLocalizedTime(style).withLocale(locale);
    }

    public DateTimeFormatter getDateTimeFormatter(@NotNull Locale locale) {
        return dateTimeFormatterFactory.apply(locale);
    }
    
    public DateTimeFormatter getDateFormatter(@NotNull Locale locale) {
        return dateFormatterFactory.apply(locale);
    }

    public DateTimeFormatter getTimeFormatter(@NotNull Locale locale) {
        return timeFormatterFactory.apply(locale);
    }

    @Override
    public String toString() {
        return name;
    }
}
