package com.dua3.utility.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberParsingUtilTest {

    @ParameterizedTest
    @CsvSource(value = {
            "123.45: 123.45",
            "-42.0: -42.0",
            "7.5: 7.5",
            "50%: 0.50",
            "%50: 0.50",
            "12.5 %: 0.125",
            "500‰: 0.500",
            "‰500: 0.500",
            " 100% : 1.0",
            " 250 ‰ : 0.25"
    }, delimiter = ':')
    void testParseDoubleUS(String input, double expected) {
        double result = NumberParsingUtil.parseDouble(input.trim(), Locale.US);
        assertEquals(expected, result, 1e-6);

        OptionalDouble optResult = NumberParsingUtil.tryParseDouble(input.trim(), Locale.US);
        assertTrue(optResult.isPresent());
        assertEquals(expected, optResult.getAsDouble(), 1e-6);
    }

    @Test
    void testParseDoubleGermanLocale() {
        double result = NumberParsingUtil.parseDouble("1.234,56", Locale.GERMANY);
        assertEquals(1234.56, result, 1e-6);

        double percent = NumberParsingUtil.parseDouble("12,5%", Locale.GERMANY);
        assertEquals(0.125, percent, 1e-6);

        double perMille = NumberParsingUtil.parseDouble("25,5‰", Locale.GERMANY);
        assertEquals(0.0255, perMille, 1e-6);
    }

    @Test
    void testParseDoubleInvalidThrows() {
        assertThrows(NumberFormatException.class, () -> NumberParsingUtil.parseDouble("invalid", Locale.US));
        assertThrows(NumberFormatException.class, () -> NumberParsingUtil.parseDouble("", Locale.US));
        assertThrows(NumberFormatException.class, () -> NumberParsingUtil.parseDouble("foo123", Locale.US));
    }

    @Test
    void testTryParseDoubleInvalidReturnsEmpty() {
        assertFalse(NumberParsingUtil.tryParseDouble("", Locale.US).isPresent());
        assertFalse(NumberParsingUtil.tryParseDouble("invalid", Locale.US).isPresent());
        assertFalse(NumberParsingUtil.tryParseDouble("12.34.56", Locale.US).isPresent());
        assertFalse(NumberParsingUtil.tryParseDouble("123abc", Locale.US).isPresent());
        assertFalse(NumberParsingUtil.tryParseDouble("%", Locale.US).isPresent());
        assertFalse(NumberParsingUtil.tryParseDouble("‰", Locale.US).isPresent());
    }
}
