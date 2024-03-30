package com.dua3.utility.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PredefinedDateTimeFormatTest {

    private static Stream<Arguments> provideParametersForDateTimeFormatTesting() {
        return Stream.of(
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.ROOT, LocalDateTime.of(2001, 1, 1, 2, 30), "2001-01-01 02:30:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.US, LocalDateTime.of(2001, 1, 1, 3, 45), "1/1/2001, 3:45:00 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.GERMANY, LocalDateTime.of(2001, 1, 1, 12, 15), "01.01.2001, 12:15:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.FRANCE, LocalDateTime.of(2001, 1, 1, 23, 59), "01/01/2001 23:59:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.CHINA, LocalDateTime.of(2001, 1, 1, 18, 22), "2001/1/1 下午6:22:00"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.US, LocalDateTime.of(2021, 12, 31, 10, 10), "12/31/21, 10:10 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.GERMANY, LocalDateTime.of(2021, 12, 31, 6, 6), "31.12.21, 06:06"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.FRANCE, LocalDateTime.of(2021, 12, 31, 19, 35), "31/12/2021 19:35"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.CHINA, LocalDateTime.of(2021, 12, 31, 9, 30), "2021/12/31 上午9:30"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.US, LocalDateTime.of(2021, 12, 31, 10, 10), "Dec 31, 2021, 10:10:00 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.GERMANY, LocalDateTime.of(2021, 12, 31, 6, 6), "31.12.2021, 06:06:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.FRANCE, LocalDateTime.of(2021, 12, 31, 19, 35), "31 déc. 2021, 19:35:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.CHINA, LocalDateTime.of(2021, 12, 31, 9, 30), "2021年12月31日 上午9:30:00"),

                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.US, LocalDateTime.of(2021, 12, 31, 10, 43), "2021-12-31T10:43:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.GERMANY, LocalDateTime.of(2021, 12, 31, 13, 58), "2021-12-31T13:58:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.FRANCE, LocalDateTime.of(2021, 12, 31, 4, 21), "2021-12-31T04:21:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.CHINA, LocalDateTime.of(2021, 12, 31, 22, 0), "2021-12-31T22:00:00")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForDateTimeFormatTesting")
    void testDateTimeFormats(PredefinedDateTimeFormat format, Locale locale, LocalDateTime date, String expectedFormat) {
        // Prepare expected DateTimeFormatter
        DateTimeFormatter formatter = format.getDateTimeFormatter(locale);

        // Validate the date formatting
        assertEquals(expectedFormat, formatter.format(date), format.getClass().getSimpleName() + "." + format.name() + "@" + locale);
    }

    private static Stream<Arguments> provideParametersForDateFormatTesting() {
        return Stream.of(
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.ROOT, LocalDate.of(2001, 1, 1), "2001-01-01"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.US, LocalDate.of(2001, 1, 1), "1/1/2001"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.GERMANY, LocalDate.of(2001, 1, 1), "01.01.2001"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.FRANCE, LocalDate.of(2001, 1, 1), "01/01/2001"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.CHINA, LocalDate.of(2001, 1, 1), "2001/1/1"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.US, LocalDate.of(2021, 12, 31), "12/31/21"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.GERMANY, LocalDate.of(2021, 12, 31), "31.12.21"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.FRANCE, LocalDate.of(2021, 12, 31), "31/12/2021"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021/12/31"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.US, LocalDate.of(2021, 12, 31), "Dec 31, 2021"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.GERMANY, LocalDate.of(2021, 12, 31), "31.12.2021"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.FRANCE, LocalDate.of(2021, 12, 31), "31 déc. 2021"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021年12月31日"),

                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.US, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.GERMANY, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.FRANCE, LocalDate.of(2021, 12, 31), "2021-12-31"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.CHINA, LocalDate.of(2021, 12, 31), "2021-12-31")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForDateFormatTesting")
    void testDateFormats(PredefinedDateTimeFormat format, Locale locale, LocalDate date, String expectedFormat) {
        // Prepare expected DateTimeFormatter
        DateTimeFormatter formatter = format.getDateFormatter(locale);

        // Validate the date formatting
        assertEquals(expectedFormat, formatter.format(date), format.name());
    }

    private static Stream<Arguments> provideParametersForTimeFormatTesting() {
        return Stream.of(
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.ROOT, LocalTime.of(2, 30), "02:30"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.US, LocalTime.of(3, 45), "3:45 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.GERMANY, LocalTime.of(12, 15), "12:15"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.FRANCE, LocalTime.of(23, 59), "23:59"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_DEFAULT, Locale.CHINA, LocalTime.of(18, 22), "下午6:22"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.US, LocalTime.of(10, 10), "10:10 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.GERMANY, LocalTime.of(6, 6), "06:06"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.FRANCE, LocalTime.of(19, 35), "19:35"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_SHORT, Locale.CHINA, LocalTime.of(9, 30), "上午9:30"),

                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.US, LocalTime.of(10, 10), "10:10:00 AM"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.GERMANY, LocalTime.of(6, 6), "06:06:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.FRANCE, LocalTime.of(19, 35), "19:35:00"),
                Arguments.of(PredefinedDateTimeFormat.LOCALE_MEDIUM, Locale.CHINA, LocalTime.of(9, 30), "上午9:30:00"),

                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.US, LocalTime.of(10, 43), "10:43:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.GERMANY, LocalTime.of(13, 58), "13:58:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.FRANCE, LocalTime.of(4, 21), "04:21:00"),
                Arguments.of(PredefinedDateTimeFormat.ISO_DATE_TIME, Locale.CHINA, LocalTime.of(22, 0), "22:00:00")
        );
    }

    @ParameterizedTest
    @MethodSource("provideParametersForTimeFormatTesting")
    void testTimeFormats(PredefinedDateTimeFormat format, Locale locale, LocalTime time, String expectedFormat) {
        // Prepare expected DateTimeFormatter
        DateTimeFormatter formatter = format.getTimeFormatter(locale);

        // Validate the date formatting
        assertEquals(expectedFormat, formatter.format(time), format.getClass().getSimpleName() + "." + format.name() + "@" + locale);
    }

}