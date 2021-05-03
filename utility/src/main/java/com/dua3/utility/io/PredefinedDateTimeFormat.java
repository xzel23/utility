package com.dua3.utility.io;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Function;

public enum PredefinedDateTimeFormat {
    LOCALE_SHORT("short (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.SHORT)),
    LOCALE_LONG("long (locale dependent)", locale -> formatFromLocale(locale, FormatStyle.LONG)),
    ISO_DATE_TIME("ISO 8601 (2000-12-31T10:15:30)", locale -> DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    private static DateTimeFormatter formatFromLocale(Locale locale, FormatStyle style) {
        return DateTimeFormatter.ofLocalizedDateTime(style).withLocale(locale);
    }

    private static DateTimeFormatter formatFromPattern(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    private final String name;

    private final Function<? super Locale, DateTimeFormatter> factory;

    PredefinedDateTimeFormat(String name, Function<? super Locale, DateTimeFormatter> factory) {
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
