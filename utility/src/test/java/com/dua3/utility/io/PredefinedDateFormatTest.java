package com.dua3.utility.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredefinedDateFormatTest {

    private static Stream<Arguments> provideParametersForTesting() {
        return Stream.of(
                Arguments.of(PredefinedDateFormat.LOCALE_DEFAULT, Locale.ROOT, LocalDate.of(2001, 1, 1), "2001-01-01"),
                Arguments.of(PredefinedDateFormat.LOCALE_DEFAULT, Locale.US, LocalDate.of(2001, 1, 1), "1/1/2001"),
                Arguments.of(PredefinedDateFormat.LOCALE_DEFAULT, Locale.GERMANY, LocalDate.of(2001, 1, 1), "01.01.2001"),
                Arguments.of(PredefinedDateFormat.LOCALE_DEFAULT, Locale.FRANCE, LocalDate.of(2001, 1, 1), "01/01/2001"),
                Arguments.of(PredefinedDateFormat.LOCALE_DEFAULT, Locale.CHINA, LocalDate.of(2001, 1, 1), "2001/1/1"),

                Arguments.of(PredefinedDateFormat.LOCALE_SHORT, Locale.US, LocalDate.of(2021, 12, 31), "Dec 31, 2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_SHORT, Locale.GERMANY, LocalDate.of(2021, 12, 31), "31.12.2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_SHORT, Locale.FRANCE, LocalDate.of(2021, 12, 31), "31 déc. 2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_SHORT, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021年12月31日"),

                Arguments.of(PredefinedDateFormat.LOCALE_LONG, Locale.US, LocalDate.of(2021, 12, 31), "December 31, 2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_LONG, Locale.GERMANY, LocalDate.of(2021, 12, 31), "31. Dezember 2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_LONG, Locale.FRANCE, LocalDate.of(2021, 12, 31), "31 décembre 2021"),
                Arguments.of(PredefinedDateFormat.LOCALE_LONG, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021年12月31日"),

                Arguments.of(PredefinedDateFormat.ISO_DATE, Locale.US, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateFormat.ISO_DATE, Locale.GERMANY, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateFormat.ISO_DATE, Locale.FRANCE, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateFormat.ISO_DATE, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021-12-31")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForTesting")
    void testGetFormatter(PredefinedDateFormat format, Locale locale, LocalDate date, String expectedFormat) {
        // Prepare expected DateTimeFormatter
        DateTimeFormatter formatter = format.getFormatter(locale);

        // Validate the date formatting
        assertEquals(expectedFormat, formatter.format(date));
    }

}