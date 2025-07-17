package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link SimpleConverter}.
 */
class SimpleConverterTest {

    @Test
    void testA2b() {
        // Create a converter from String to Integer
        Function<String, Integer> a2b = Integer::valueOf;
        Function<Integer, String> b2a = String::valueOf;
        SimpleConverter<String, Integer> converter = new SimpleConverter<>(a2b, b2a);

        // Test a2b method
        assertEquals(a2b, converter.a2b());
    }

    @Test
    void testB2a() {
        // Create a converter from String to Integer
        Function<String, Integer> a2b = Integer::valueOf;
        Function<Integer, String> b2a = String::valueOf;
        SimpleConverter<String, Integer> converter = new SimpleConverter<>(a2b, b2a);

        // Test b2a method
        assertEquals(b2a, converter.b2a());
    }

    @Test
    void testConvert() {
        // Create a converter from String to Integer
        Function<String, Integer> a2b = Integer::valueOf;
        Function<Integer, String> b2a = String::valueOf;
        SimpleConverter<String, Integer> converter = new SimpleConverter<>(a2b, b2a);

        // Test convert method
        assertEquals(123, converter.convert("123"));
        assertEquals(0, converter.convert("0"));
        assertEquals(-456, converter.convert("-456"));
        
        // Test with invalid input
        assertThrows(ConversionException.class, () -> converter.convert("not a number"));
    }

    @Test
    void testConvertBack() {
        // Create a converter from String to Integer
        Function<String, Integer> a2b = Integer::valueOf;
        Function<Integer, String> b2a = String::valueOf;
        SimpleConverter<String, Integer> converter = new SimpleConverter<>(a2b, b2a);

        // Test convertBack method
        assertEquals("123", converter.convertBack(123));
        assertEquals("0", converter.convertBack(0));
        assertEquals("-456", converter.convertBack(-456));
    }

    @Test
    void testInverse() {
        // Create a converter from String to Integer
        Function<String, Integer> a2b = Integer::valueOf;
        Function<Integer, String> b2a = String::valueOf;
        SimpleConverter<String, Integer> converter = new SimpleConverter<>(a2b, b2a);

        // Get the inverse converter
        Converter<Integer, String> inverse = converter.inverse();

        // Test the inverse converter
        assertEquals("123", inverse.convert(123));
        assertEquals(123, inverse.convertBack("123"));
    }

    @Test
    void testCreateStaticMethod() {
        // Create a converter using the static create method
        Converter<String, Integer> converter = Converter.create(Integer::valueOf, String::valueOf);

        // Test the converter
        assertEquals(123, converter.convert("123"));
        assertEquals("123", converter.convertBack(123));
    }
}