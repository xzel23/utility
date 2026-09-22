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

    enum SampleEnum { FOO, BAR }

    static class WithValueOf {
        final String val;
        WithValueOf(String val) { this.val = val; }
        public static WithValueOf valueOf(String s) { return new WithValueOf(s); }
        @Override public String toString() { return val; }
    }

    static class WithConstructor {
        final String val;
        public WithConstructor(String val) { this.val = val; }
        @Override public String toString() { return val; }
    }

    static class UnsupportedType {}

    @Test
    void testStringConverter() {
        Converter<String, SampleEnum> enumConv = Converter.stringConverter(SampleEnum.class);
        assertEquals(SampleEnum.FOO, enumConv.convert("FOO"));
        assertEquals("BAR", enumConv.convertBack(SampleEnum.BAR));

        Converter<String, WithValueOf> valueOfConv = Converter.stringConverter(WithValueOf.class);
        assertEquals("hello", valueOfConv.convert("hello").val);
        assertEquals("hello", valueOfConv.convertBack(new WithValueOf("hello")));

        Converter<String, WithConstructor> constructorConv = Converter.stringConverter(WithConstructor.class);
        assertEquals("world", constructorConv.convert("world").val);
        assertEquals("world", constructorConv.convertBack(new WithConstructor("world")));

        assertThrows(ConversionException.class, () -> Converter.stringConverter(UnsupportedType.class));
    }

    @Test
    void testIdentityAndNullAware() {
        Converter<String, String> identity = Converter.identity();
        assertEquals("test", identity.convert("test"));
        assertEquals("test", identity.convertBack("test"));

        Converter<String, Integer> nullAware = Converter.createNullAware(Integer::valueOf, String::valueOf);
        assertNull(nullAware.convert(null));
        assertNull(nullAware.convertBack(null));
        assertEquals(42, nullAware.convert("42"));
        assertEquals("42", nullAware.convertBack(42));
    }
}