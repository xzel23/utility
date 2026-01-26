// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@NullUnmarked
class TextUtilTest {

    @Test
    void testLines() {
        String input = "Line1\nLine2\rLine3\r\nLine4";
        String[] expected = {"Line1", "Line2", "Line3", "Line4"};
        String[] result = TextUtil.lines(input);
        Assertions.assertArrayEquals(expected, result, "Expected lines do not match the result.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\n", "\n\n", "   \r\n  ", "\r\n\r\n"})
    void testLinesWithEmptyAndWhitespaceStrings(String input) {
        String[] expected = input.split("\\R");
        String[] result = TextUtil.lines(input);
        Assertions.assertArrayEquals(expected, result, "Expected lines for input with empty/whitespace strings do not match.");
    }

    @Test
    void testBytesToCharsWithValidInput() {
        byte[] input = "Hello".getBytes(StandardCharsets.UTF_8);
        char[] expected = {'H', 'e', 'l', 'l', 'o'};
        char[] result = TextUtil.toCharArray(input);
        Assertions.assertArrayEquals(expected, result, "Expected char array does not match the result.");
    }

    @Test
    void testBytesToCharsWithEmptyInput() {
        byte[] input = {};
        char[] expected = new char[0];
        char[] result = TextUtil.toCharArray(input);
        Assertions.assertArrayEquals(expected, result, "Expected an empty char array.");
    }

    @Test
    void testBytesToCharsWithSpecialCharacters() {
        String input = "Ω漢字";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        char[] expected = {'Ω', '漢', '字'};
        char[] result = TextUtil.toCharArray(bytes);
        Assertions.assertArrayEquals(expected, result, "Expected char array for special characters does not match the result.");
    }

    @ParameterizedTest
    @MethodSource("generateTestData_transform")
    void testTransform(String template, Function<String, String> env, String expected) {
        String actual = TextUtil.transform(template, env);
        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> generateTestData_transform() {
        UnaryOperator<String> env = v -> switch (v) {
            case "greeting" -> "Hi";
            case "name" -> "John";
            default -> "";
        };

        return Stream.of(
                Arguments.of("Hello, ${name}!", env, "Hello, John!"),
                Arguments.of("${greeting}, ${name}!", env, "Hi, John!"),
                Arguments.of("Hello, ${name}! How are you, ${name}?", env, "Hello, John! How are you, John?"),
                Arguments.of("Hello, name}!", env, "Hello, name}!"),
                Arguments.of("Hello, John!", env, "Hello, John!"),
                Arguments.of("", env, "")
                // Add more test data if needed
        );
    }

    @ParameterizedTest
    @MethodSource("generateTestData_transform_RichText")
    void testTransform_RichText(RichText template, Function<String, RichText> env, RichText expected) {
        RichText actual = TextUtil.transform(template, env);
        Assertions.assertEquals(expected, actual);
    }

    private static Stream<Arguments> generateTestData_transform_RichText() {
        Function<String, RichText> env = v -> switch (v) {
            case "greeting" -> RichText.valueOf("Hi");
            case "name" -> RichText.valueOf("John");
            default -> RichText.emptyText();
        };

        return Stream.of(
                Arguments.of(RichText.valueOf("Hello, ${name}!"), env, RichText.valueOf("Hello, John!")),
                Arguments.of(RichText.valueOf("${greeting}, ${name}!"), env, RichText.valueOf("Hi, John!")),
                Arguments.of(RichText.valueOf("Hello, ${name}! How are you, ${name}?"), env, RichText.valueOf("Hello, John! How are you, John?")),
                Arguments.of(RichText.valueOf("Hello, name}!"), env, RichText.valueOf("Hello, name}!")),
                Arguments.of(RichText.valueOf("Hello, John!"), env, RichText.valueOf("Hello, John!")),
                Arguments.of(RichText.valueOf(""), env, RichText.valueOf(""))
                // Add more test data if needed
        );
    }

    @Test
    void testTransformWithMapArgument() {
        String template = "Hello ${NAME}.";

        String expected = "Hello Axel.";
        String actual = TextUtil.transform(template, Map.of("NAME", "Axel"));

        Assertions.assertEquals(expected, actual);
    }

    @NullUnmarked
    record TestDataAlign(String text, String expected, int width, Alignment align, Character fill) {}

    @ParameterizedTest
    @MethodSource("generateTestDataAlign")
    void testAlign(TestDataAlign data) {
        if (data.fill() == null) {
            Assertions.assertEquals(data.expected(), TextUtil.align(data.text(), data.width(), data.align()));
        } else {
            Assertions.assertEquals(data.expected(), TextUtil.align(data.text(), data.width(), data.align(), data.fill()));
        }
    }

    static List<TestDataAlign> generateTestDataAlign() {
        return List.of(
                new TestDataAlign("", "", 0, Alignment.LEFT, null),
                new TestDataAlign("", "", 0, Alignment.CENTER, null),
                new TestDataAlign("", "", 0, Alignment.RIGHT, null),
                new TestDataAlign("", "   ", 3, Alignment.LEFT, null),
                new TestDataAlign("", "   ", 3, Alignment.CENTER, null),
                new TestDataAlign("", "   ", 3, Alignment.RIGHT, null),
                new TestDataAlign("abc", "abc", 1, Alignment.LEFT, null),
                new TestDataAlign("abc", "abc", 1, Alignment.CENTER, null),
                new TestDataAlign("abc", "abc", 1, Alignment.RIGHT, null),
                new TestDataAlign("abc", "abc  ", 5, Alignment.LEFT, null),
                new TestDataAlign("abc", " abc ", 5, Alignment.CENTER, null),
                new TestDataAlign("abc", "  abc", 5, Alignment.RIGHT, null),
                new TestDataAlign("abcd", "abcd ", 5, Alignment.LEFT, null),
                new TestDataAlign("abcd", "abcd ", 5, Alignment.CENTER, null),
                new TestDataAlign("abcd", " abcd", 5, Alignment.RIGHT, null),
                new TestDataAlign("", "", 0, Alignment.LEFT, '_'),
                new TestDataAlign("", "", 0, Alignment.CENTER, '_'),
                new TestDataAlign("", "", 0, Alignment.RIGHT, '_'),
                new TestDataAlign("", "___", 3, Alignment.LEFT, '_'),
                new TestDataAlign("", "___", 3, Alignment.CENTER, '_'),
                new TestDataAlign("", "___", 3, Alignment.RIGHT, '_'),
                new TestDataAlign("abc", "abc", 1, Alignment.LEFT, '_'),
                new TestDataAlign("abc", "abc", 1, Alignment.CENTER, '_'),
                new TestDataAlign("abc", "abc", 1, Alignment.RIGHT, '_'),
                new TestDataAlign("abc", "abc__", 5, Alignment.LEFT, '_'),
                new TestDataAlign("abc", "_abc_", 5, Alignment.CENTER, '_'),
                new TestDataAlign("abc", "__abc", 5, Alignment.RIGHT, '_'),
                new TestDataAlign("abcd", "abcd_", 5, Alignment.LEFT, '_'),
                new TestDataAlign("abcd", "abcd_", 5, Alignment.CENTER, '_'),
                new TestDataAlign("abcd", "_abcd", 5, Alignment.RIGHT, '_'),
                new TestDataAlign("This is a test", "This is a test", 5, Alignment.JUSTIFY, ' '),
                new TestDataAlign("This is a test", "This  is a  test", 16, Alignment.JUSTIFY, ' '),
                new TestDataAlign("This is a test", "This  is  a  test", 17, Alignment.JUSTIFY, ' '),
                new TestDataAlign("This is a test", "This   is   a   test", 20, Alignment.JUSTIFY, ' '),
                new TestDataAlign("This  is a   test", "This      is    a         test", 30, Alignment.JUSTIFY, ' ')
        );
    }

    record TestDataWrap(String fileIn, String fileRef, int width, Alignment align, boolean hardWrap) {}

    @ParameterizedTest
    @MethodSource("generateTestDataWrap")
    void testWrap(TestDataWrap data) throws IOException {
        TextAssertions.assertEqualsWithEscapedOutput(
                LangUtil.getResourceAsString(getClass(), data.fileRef()),
                TextUtil.wrap(LangUtil.getResourceAsString(getClass(), data.fileIn()), data.width, data.align, data.hardWrap),
                data.fileIn()
        );
    }

    static List<TestDataWrap> generateTestDataWrap() {
        return List.of(
                new TestDataWrap("mobydick.txt", "mobydick-leftaligned-120_chars.txt", 120, Alignment.LEFT, false),
                new TestDataWrap("mobydick.txt", "mobydick-rightaligned-120_chars.txt", 120, Alignment.RIGHT, false),
                new TestDataWrap("mobydick.txt", "mobydick-centeraligned-120_chars.txt", 120, Alignment.CENTER, false),
                new TestDataWrap("mobydick.txt", "mobydick-justifyaligned-120_chars.txt", 120, Alignment.JUSTIFY, false),
                new TestDataWrap("longwords.txt", "longwords-leftaligned-12_chars-softwrap.txt", 12, Alignment.LEFT, false),
                new TestDataWrap("longwords.txt", "longwords-leftaligned-12_chars-hardwrap.txt", 12, Alignment.LEFT, true)
        );
    }

    @Test
    void testNonEmptyOr() {
        // Test with non-null, non-empty CharSequence - Expected to return inputString itself
        String input = "Test Input";
        String defaultInput = "Default";
        Assertions.assertEquals("Test Input", TextUtil.nonEmptyOr(input, defaultInput));

        // Test with empty CharSequence - Expected to return defaultString
        input = "";
        Assertions.assertEquals("Default", TextUtil.nonEmptyOr(input, defaultInput));

        // Test with null CharSequence - Expected to return defaultString
        Assertions.assertEquals("Default", TextUtil.nonEmptyOr(null, defaultInput));
    }

    @Test
    void testNonEmptyOr_RichText() {
        // Test with non-null, non-empty CharSequence - Expected to return inputString itself
        RichText input = RichText.valueOf("Test Input");
        RichText defaultInput = RichText.valueOf("Default");
        Assertions.assertEquals(input, TextUtil.nonEmptyOr(input, defaultInput));

        // Test with empty CharSequence - Expected to return defaultString
        input = RichText.valueOf("");
        Assertions.assertEquals(defaultInput, TextUtil.nonEmptyOr(input, defaultInput));

        // Test with null CharSequence - Expected to return defaultString
        Assertions.assertEquals(defaultInput, TextUtil.nonEmptyOr(null, defaultInput));
    }

    @Test
    void testEscapeHTML() {
        String normalString = "<div>Test Content</div>";
        String escapedString = TextUtil.escapeHtml(normalString);
        Assertions.assertEquals("&lt;div&gt;Test Content&lt;/div&gt;", escapedString);

        String stringWithAmpersand = "Tom & Jerry";
        escapedString = TextUtil.escapeHtml(stringWithAmpersand);
        Assertions.assertEquals("Tom &amp; Jerry", escapedString);

        String specialCharactersString = "< > & \" ' /";
        escapedString = TextUtil.escapeHtml(specialCharactersString);
        Assertions.assertEquals("&lt; &gt; &amp; &quot; &apos; /", escapedString);
    }

    @Test
    void testDecodeFontSize() {
        // Test with "pt"
        Assertions.assertEquals(10.0f, TextUtil.decodeFontSize("10pt"), 0.001);

        // Test with "em"
        Assertions.assertEquals(120.0f, TextUtil.decodeFontSize("10em"), 0.001);

        // Test with "px"
        Assertions.assertEquals(7.5f, TextUtil.decodeFontSize("10px"), 0.001);

        // Test with "%"
        Assertions.assertEquals(1.2f, TextUtil.decodeFontSize("10%"), 0.001);

        // Test with unknown unit
        Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtil.decodeFontSize("10abc"));

        // Test with "vw"
        Assertions.assertEquals(120.0f, TextUtil.decodeFontSize("10vw"), 0.001);

        // Test with empty string
        Assertions.assertThrows(IllegalArgumentException.class, () -> TextUtil.decodeFontSize(""));
    }

    private static Object[][] textDimensionProvider() {
        Font timesRomanBold12 = FontUtil.getInstance().getFont("TimesRoman-bold-12");
        Font timesRomanItalic18 = FontUtil.getInstance().getFont("TimesRoman-italic-18");

        return new Object[][]{
                // Testing for empty string
                {"", timesRomanBold12, AwtFontUtil.getInstance().getTextDimension("", timesRomanBold12)},
                // Testing for normal use case
                {"Test text", timesRomanItalic18, AwtFontUtil.getInstance().getTextDimension("Test text", timesRomanItalic18)}
        };
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("textDimensionProvider")
    void testGetTextDimension(CharSequence text, Font font, Object expected) {
        if (expected instanceof Dimension2f)
            Assertions.assertEquals(expected, TextUtil.getTextDimension(text, font));
        else if (expected instanceof Class<?> && Exception.class.isAssignableFrom((Class<?>) expected))
            Assertions.assertThrows((Class<? extends Throwable>) expected, () -> TextUtil.getTextDimension(text, font));
    }

    @Test
    void testGetTextDimensionRichtext() {
        Font timesRoman12 = FontUtil.getInstance().getFont("TimesRoman-12");

        // if the text does not change the font, the result should match the result for the plain string
        RichText textHiJohn = new RichTextBuilder()
                .append("Hi ")
                .append("John")
                .append("!")
                .toRichText();

        Rectangle2f textDimensionHiJohn = TextUtil.getTextDimension(textHiJohn, timesRoman12);
        Rectangle2f richTextDimensionHiJohn = TextUtil.getRichTextDimension(textHiJohn, timesRoman12);
        Assertions.assertEquals(textDimensionHiJohn, richTextDimensionHiJohn);

        // the bold font should use more space
        RichText textHiBoldJohn = new RichTextBuilder()
                .append("Hi ")
                .push(Style.BOLD)
                .append("John")
                .pop(Style.BOLD)
                .append("!")
                .toRichText();

        Rectangle2f richTextDimensionHiBoldJohn = TextUtil.getRichTextDimension(textHiBoldJohn, timesRoman12);
        Assertions.assertTrue(textDimensionHiJohn.height() <= richTextDimensionHiBoldJohn.height());
        Assertions.assertTrue(textDimensionHiJohn.width() < richTextDimensionHiBoldJohn.width());
    }

    @Test
    void testToStringWithNonNullObject() {
        String result = TextUtil.toString(123, "default");
        Assertions.assertEquals("123", result, "Expected toString() of 123 to be '123'");
    }

    @Test
    void testToStringWithNullObject() {
        String result = TextUtil.toString(null, "default");
        Assertions.assertEquals("default", result, "Expected toString() of null to be 'default'");
    }

    @Test
    void testToStringWithCustomObject() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        String result = TextUtil.toString(obj, "default");
        Assertions.assertEquals("CustomObject", result, "Expected toString() of custom object to be 'CustomObject'");
    }

    @Test
    void testToStringWithEmptyValueIfNull() {
        String result = TextUtil.toString(null, "");
        Assertions.assertEquals("", result, "Expected toString() of null to be an empty string");
    }

    @Test
    void testLexicographicComparatorWithDefaultLocale() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.getDefault());
        Assertions.assertTrue(comparator.compare("apple", "banana") < 0, "Expected 'apple' to come before 'banana'");
        Assertions.assertTrue(comparator.compare("banana", "apple") > 0, "Expected 'banana' to come after 'apple'");
        Assertions.assertEquals(0, comparator.compare("apple", "apple"), "Expected 'apple' to be equal to 'apple'");
    }

    @Test
    void testLexicographicComparatorWithCustomLocale() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.FRENCH);
        Assertions.assertTrue(comparator.compare("éclair", "ete") < 0, "Expected 'éclair' to come before 'ete' in French locale");
        Assertions.assertTrue(comparator.compare("été", "éclair") > 0, "Expected 'été' to come after 'éclair' in French locale");
        Assertions.assertEquals(0, comparator.compare("été", "été"), "Expected 'été' to be equal to 'été'");
    }

    @Test
    void testLexicographicComparatorWithNullValues() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.getDefault());
        Assertions.assertTrue(comparator.compare(null, "apple") < 0, "Expected null to come before 'apple'");
        Assertions.assertTrue(comparator.compare("apple", null) > 0, "Expected 'apple' to come after null");
        Assertions.assertEquals(0, comparator.compare(null, null), "Expected null to be equal to null");
    }

    @Test
    void testAppendHtmlEscapedCharacters() {
        StringBuilder sb = new StringBuilder();
        TextUtil.appendHtmlEscapedCharacters(sb, "<div>Test & 'Quote' \"DoubleQuote\"</div>");
        Assertions.assertEquals("&lt;div&gt;Test &amp; &apos;Quote&apos; &quot;DoubleQuote&quot;&lt;/div&gt;", sb.toString());

        // Test with empty string
        sb = new StringBuilder();
        TextUtil.appendHtmlEscapedCharacters(sb, "");
        Assertions.assertEquals("", sb.toString());

        // Test with non-ASCII characters
        sb = new StringBuilder();
        TextUtil.appendHtmlEscapedCharacters(sb, "Café");
        Assertions.assertEquals("Caf&#233;", sb.toString());
    }

    @Test
    void testEscape() {
        // Test with special characters
        Assertions.assertEquals("\\\"Hello\\\"", TextUtil.escape("\"Hello\""));
        Assertions.assertEquals("\\\\backslash", TextUtil.escape("\\backslash"));
        Assertions.assertEquals("Tab\\tNewline\\n", TextUtil.escape("Tab\tNewline\n"));
        Assertions.assertEquals("Carriage\\rReturn", TextUtil.escape("Carriage\rReturn"));
        Assertions.assertEquals("Form\\fFeed", TextUtil.escape("Form\fFeed"));
        Assertions.assertEquals("Backspace\\b", TextUtil.escape("Backspace\b"));
        Assertions.assertEquals("\\'Single Quote\\'", TextUtil.escape("'Single Quote'"));

        // Test with null character
        Assertions.assertEquals("Null\\u0000Character", TextUtil.escape("Null\0Character"));

        // Test with non-ASCII characters
        Assertions.assertEquals("Café", TextUtil.escape("Café"));
        Assertions.assertEquals("你好!", TextUtil.escape("你好!"));

        // Test with character not belonging to any of the above classes
        Assertions.assertEquals("\\u2603", TextUtil.escapeASCII("☃"));

        // Test with empty string
        Assertions.assertEquals("", TextUtil.escape(""));
    }

    @Test
    void testEscapeASCII() {
        // Test with special characters
        Assertions.assertEquals("\\\"Hello\\\"", TextUtil.escapeASCII("\"Hello\""));
        Assertions.assertEquals("\\\\backslash", TextUtil.escapeASCII("\\backslash"));
        Assertions.assertEquals("Tab\\tNewline\\n", TextUtil.escapeASCII("Tab\tNewline\n"));
        Assertions.assertEquals("Carriage\\rReturn", TextUtil.escapeASCII("Carriage\rReturn"));
        Assertions.assertEquals("Form\\fFeed", TextUtil.escapeASCII("Form\fFeed"));
        Assertions.assertEquals("Backspace\\b", TextUtil.escapeASCII("Backspace\b"));
        Assertions.assertEquals("\\'Single Quote\\'", TextUtil.escapeASCII("'Single Quote'"));

        // Test with null character
        Assertions.assertEquals("Null\\u0000Character", TextUtil.escapeASCII("Null\0Character"));

        // Test with non-ASCII characters
        Assertions.assertEquals("Caf\\u00E9", TextUtil.escapeASCII("Café"));
        Assertions.assertEquals("\\u4F60\\u597D!", TextUtil.escapeASCII("你好!"));

        // Test with empty string
        Assertions.assertEquals("", TextUtil.escapeASCII(""));
    }

    @Test
    void testContentEquals() {
        // Test with equal content
        Assertions.assertTrue(TextUtil.contentEquals("test", "test"));
        Assertions.assertTrue(TextUtil.contentEquals(new StringBuilder("test"), "test"));

        // Test with different content
        Assertions.assertFalse(TextUtil.contentEquals("test", "Test"));
        Assertions.assertFalse(TextUtil.contentEquals("test", "test1"));

        // Test with empty strings
        Assertions.assertTrue(TextUtil.contentEquals("", ""));
    }

    @Test
    void testContains() {
        // Test with substring present
        Assertions.assertTrue(TextUtil.contains("Hello World", "World"));
        Assertions.assertTrue(TextUtil.contains("Hello World", "Hello"));
        Assertions.assertTrue(TextUtil.contains("Hello World", "o W"));

        // Test with substring not present
        Assertions.assertFalse(TextUtil.contains("Hello World", "world"));
        Assertions.assertFalse(TextUtil.contains("Hello World", "Hello  World"));

        // Test with empty strings
        Assertions.assertTrue(TextUtil.contains("Hello", ""));
        Assertions.assertFalse(TextUtil.contains("", "Hello"));
    }

    @Test
    void testContainsNoneOf() {
        // Test with no matching characters
        Assertions.assertTrue(TextUtil.containsNoneOf("Hello", "xyz"));
        Assertions.assertTrue(TextUtil.containsNoneOf("12345", "abcde"));

        // Test with matching characters
        Assertions.assertFalse(TextUtil.containsNoneOf("Hello", "lo"));
        Assertions.assertFalse(TextUtil.containsNoneOf("12345", "56"));

        // Test with empty strings
        Assertions.assertTrue(TextUtil.containsNoneOf("", "xyz"));
        Assertions.assertTrue(TextUtil.containsNoneOf("Hello", ""));
    }

    @Test
    void testContainsAnyOf() {
        // Test with matching characters
        Assertions.assertTrue(TextUtil.containsAnyOf("Hello", "lo"));
        Assertions.assertTrue(TextUtil.containsAnyOf("12345", "56"));

        // Test with no matching characters
        Assertions.assertFalse(TextUtil.containsAnyOf("Hello", "xyz"));
        Assertions.assertFalse(TextUtil.containsAnyOf("12345", "abcde"));

        // Test with empty strings
        Assertions.assertFalse(TextUtil.containsAnyOf("", "xyz"));
        Assertions.assertFalse(TextUtil.containsAnyOf("Hello", ""));
    }

    @Test
    void testIndexOf() {
        // Test indexOf(CharSequence, int)
        Assertions.assertEquals(1, TextUtil.indexOf("Hello", 'e'));
        Assertions.assertEquals(-1, TextUtil.indexOf("Hello", 'x'));

        // Test indexOf(CharSequence, CharSequence)
        Assertions.assertEquals(0, TextUtil.indexOf("Hello", "He"));
        Assertions.assertEquals(3, TextUtil.indexOf("Hello", "lo"));
        Assertions.assertEquals(-1, TextUtil.indexOf("Hello", "hi"));

        // Test indexOf(CharSequence, int, int)
        Assertions.assertEquals(2, TextUtil.indexOf("Hello", 'l', 0));
        Assertions.assertEquals(3, TextUtil.indexOf("Hello", 'l', 3));
        Assertions.assertEquals(4, TextUtil.indexOf("Hello", 'o', 0));
        Assertions.assertEquals(-1, TextUtil.indexOf("Hello", 'e', 2));

        // Test indexOf(CharSequence, CharSequence, int)
        Assertions.assertEquals(0, TextUtil.indexOf("Hello", "He", 0));
        Assertions.assertEquals(3, TextUtil.indexOf("Hello", "lo", 0));
        Assertions.assertEquals(-1, TextUtil.indexOf("Hello", "He", 1));
        Assertions.assertEquals(-1, TextUtil.indexOf("Hello", "hi", 0));
    }

    @Test
    void testStartsWith() {
        // Test with matching prefix
        Assertions.assertTrue(TextUtil.startsWith("Hello", "He"));
        Assertions.assertTrue(TextUtil.startsWith("Hello", "Hello"));

        // Test with non-matching prefix
        Assertions.assertFalse(TextUtil.startsWith("Hello", "he"));
        Assertions.assertFalse(TextUtil.startsWith("Hello", "Hello World"));

        // Test with empty strings
        Assertions.assertTrue(TextUtil.startsWith("Hello", ""));
        Assertions.assertFalse(TextUtil.startsWith("", "Hello"));
    }

    @Test
    void testGroupMatch() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)");
        String input = "key:value";
        Matcher matcher = pattern.matcher(input);
        Assertions.assertTrue(matcher.matches());

        Optional<CharSequence> prefix = TextUtil.group(matcher, input, "prefix");
        Optional<CharSequence> value = TextUtil.group(matcher, input, "value");

        Assertions.assertTrue(prefix.isPresent());
        Assertions.assertEquals("key", prefix.get().toString());

        Assertions.assertTrue(value.isPresent());
        Assertions.assertEquals("value", value.get().toString());
    }

    @Test
    void testGroupNoMatch() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)?");
        String input = "key:";
        Matcher matcher = pattern.matcher(input);
        Assertions.assertTrue(matcher.matches());

        Optional<CharSequence> prefix = TextUtil.group(matcher, input, "prefix");
        Optional<CharSequence> value = TextUtil.group(matcher, input, "value");

        Assertions.assertTrue(prefix.isPresent());
        Assertions.assertEquals("key", prefix.get().toString());

        Assertions.assertTrue(value.isEmpty());
    }

    @Test
    void testGroupNonExisting() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)");
        String input = "key:value";
        Matcher matcher = pattern.matcher(input);
        Assertions.assertTrue(matcher.matches());

        // Test with non-existing group
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> TextUtil.group(matcher, input, "nonexistent"),
                "should throw IllegalStateException for non-existing group"
        );
    }

    @Test
    void testGetDigest() throws Exception {
        // Test with byte array input
        String input = "Hello, World!";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        byte[] sha256 = TextUtil.getDigest("SHA-256", bytes);
        Assertions.assertEquals(32, sha256.length); // SHA-256 produces 32 bytes

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            byte[] sha256Stream = TextUtil.getDigest("SHA-256", is);
            Assertions.assertArrayEquals(sha256, sha256Stream);
        }
    }

    @Test
    void testGetDigestString() throws Exception {
        // Test with byte array input
        String input = "Hello, World!";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        String sha256 = TextUtil.getDigestString("SHA-256", bytes);
        Assertions.assertEquals(64, sha256.length()); // SHA-256 hex string is 64 characters

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String sha256Stream = TextUtil.getDigestString("SHA-256", is);
            Assertions.assertEquals(sha256, sha256Stream);
        }
    }

    @Test
    void testGenerateMailToLink() {
        // Test with simple email and subject
        String link = TextUtil.generateMailToLink("test@example.com", "Test Subject");
        Assertions.assertEquals("mailto:test@example.com?subject=Test%20Subject", link);

        // Test with email and subject containing special characters
        link = TextUtil.generateMailToLink("test@example.com", "Test & Subject");
        Assertions.assertEquals("mailto:test@example.com?subject=Test%20%26%20Subject", link);
    }

    @Test
    void testLineEndConversions() {
        // Test text with mixed line endings
        String mixedText = "Line1\r\nLine2\nLine3\rLine4";

        // Test toUnixLineEnds
        String unixText = TextUtil.toUnixLineEnds(mixedText);
        Assertions.assertEquals("Line1\nLine2\nLine3\nLine4", unixText);

        // Test toWindowsLineEnds
        String windowsText = TextUtil.toWindowsLineEnds(mixedText);
        Assertions.assertEquals("Line1\r\nLine2\r\nLine3\r\nLine4", windowsText);

        // Test setLineEnds with custom line ending
        String customText = TextUtil.setLineEnds(mixedText, "|");
        Assertions.assertEquals("Line1|Line2|Line3|Line4", customText);
    }

    @Test
    void testQuote() {
        // Test with simple string
        Assertions.assertEquals("\"Hello\"", TextUtil.quote("Hello"));

        // Test with string containing quotes
        Assertions.assertEquals("\"Hello \\\"World\\\"\"", TextUtil.quote("Hello \"World\""));

        // Test with string containing special characters
        Assertions.assertEquals("\"Tab\\tNewline\\n\"", TextUtil.quote("Tab\tNewline\n"));

        // Test with empty string
        Assertions.assertEquals("\"\"", TextUtil.quote(""));
    }

    @Test
    void testQuoteIfNeeded() {
        // Test with string that needs quoting
        Assertions.assertEquals("\"Hello World\"", TextUtil.quoteIfNeeded("Hello World"));

        // Test with string that doesn't need quoting
        Assertions.assertEquals("Hello", TextUtil.quoteIfNeeded("Hello"));

        // Test with empty string
        Assertions.assertEquals("", TextUtil.quoteIfNeeded(""));
    }

    @Test
    void testJoinQuotedIfNeeded() {
        // Test with list of strings
        List<String> list = List.of("Hello", "World", "Test");
        Assertions.assertEquals("Hello, World, Test", TextUtil.joinQuotedIfNeeded(list));

        // Test with list containing strings that need quoting
        list = List.of("Hello", "World Test", "End");
        Assertions.assertEquals("Hello, \"World Test\", End", TextUtil.joinQuotedIfNeeded(list));

        // Test with custom delimiter
        Assertions.assertEquals("Hello|\"World Test\"|End", TextUtil.joinQuotedIfNeeded(list, "|"));

        // Test with empty list
        Assertions.assertEquals("", TextUtil.joinQuotedIfNeeded(List.of()));
    }

    @Test
    void testJoinQuoted() {
        // Test with list of strings
        List<String> list = List.of("Hello", "World", "Test");
        Assertions.assertEquals("\"Hello\", \"World\", \"Test\"", TextUtil.joinQuoted(list));

        // Test with custom delimiter
        Assertions.assertEquals("\"Hello\"|\"World\"|\"Test\"", TextUtil.joinQuoted(list, "|"));

        // Test with empty list
        Assertions.assertEquals("", TextUtil.joinQuoted(List.of()));
    }

    @Test
    void testIsBlank() {
        // Test with blank strings
        Assertions.assertTrue(TextUtil.isBlank(""));
        Assertions.assertTrue(TextUtil.isBlank(" "));
        Assertions.assertTrue(TextUtil.isBlank("\t"));
        Assertions.assertTrue(TextUtil.isBlank("\n"));
        Assertions.assertTrue(TextUtil.isBlank(" \t\n\r"));

        // Test with non-blank strings
        Assertions.assertFalse(TextUtil.isBlank("a"));
        Assertions.assertFalse(TextUtil.isBlank(" a "));
        Assertions.assertFalse(TextUtil.isBlank("\ta\n"));
    }

    @Test
    void testAppendHtmlEscapedCharactersGeneric() throws IOException {
        // Test with a custom Appendable implementation
        class TestAppendable implements Appendable {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public Appendable append(CharSequence csq) throws IOException {
                sb.append(csq);
                return this;
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                sb.append(csq, start, end);
                return this;
            }

            @Override
            public Appendable append(char c) throws IOException {
                sb.append(c);
                return this;
            }

            @Override
            public String toString() {
                return sb.toString();
            }
        }

        // Test with HTML special characters
        TestAppendable app = new TestAppendable();
        TextUtil.appendHtmlEscapedCharacters(app, "<div>Test & 'Quote' \"DoubleQuote\"</div>");
        Assertions.assertEquals("&lt;div&gt;Test &amp; &apos;Quote&apos; &quot;DoubleQuote&quot;&lt;/div&gt;", app.toString());

        // Test with empty string
        app = new TestAppendable();
        TextUtil.appendHtmlEscapedCharacters(app, "");
        Assertions.assertEquals("", app.toString());

        // Test with non-ASCII characters
        app = new TestAppendable();
        TextUtil.appendHtmlEscapedCharacters(app, "Café");
        Assertions.assertEquals("Caf&#233;", app.toString());

        // Test with null character
        app = new TestAppendable();
        TextUtil.appendHtmlEscapedCharacters(app, "\0");
        Assertions.assertEquals("&#0;", app.toString());
    }

    @Test
    void testTransformWithMapEntries() {
        // Test with varargs Map.Entry
        String template = "Hello ${NAME}, welcome to ${PLACE}!";

        Map.Entry<String, String> entry1 = Map.entry("NAME", "John");
        Map.Entry<String, String> entry2 = Map.entry("PLACE", "Wonderland");

        String result = TextUtil.transform(template, entry1, entry2);
        Assertions.assertEquals("Hello John, welcome to Wonderland!", result);

        // Test with single entry
        template = "Hello ${NAME}!";
        result = TextUtil.transform(template, Map.entry("NAME", "Alice"));
        Assertions.assertEquals("Hello Alice!", result);

        // Test with non-existent placeholder
        template = "Hello ${NAME}, how are you ${MOOD}?";
        result = TextUtil.transform(template, Map.entry("NAME", "Bob"));
        Assertions.assertEquals("Hello Bob, how are you MOOD?", result);

        // Test with empty template
        template = "";
        result = TextUtil.transform(template, Map.entry("NAME", "Charlie"));
        Assertions.assertEquals("", result);

        // Test with null value using AbstractMap.SimpleEntry which allows null values
        template = "Hello ${NAME}!";
        result = TextUtil.transform(template, new AbstractMap.SimpleEntry<>("NAME", null));
        Assertions.assertEquals("Hello null!", result);
    }

    @Test
    void testContainsNoneOfWithCharArray() {
        // Test with no matching characters
        Assertions.assertTrue(TextUtil.containsNoneOf("Hello", 'x', 'y', 'z'));
        Assertions.assertTrue(TextUtil.containsNoneOf("12345", 'a', 'b', 'c'));

        // Test with matching characters
        Assertions.assertFalse(TextUtil.containsNoneOf("Hello", 'l', 'o', 'z'));
        Assertions.assertFalse(TextUtil.containsNoneOf("12345", '3', '6', '9'));

        // Test with empty string
        Assertions.assertTrue(TextUtil.containsNoneOf("", 'a', 'b', 'c'));

        // Test with empty char array
        Assertions.assertTrue(TextUtil.containsNoneOf("Hello"));

        // Test with single character
        Assertions.assertTrue(TextUtil.containsNoneOf("a", 'b'));
        Assertions.assertFalse(TextUtil.containsNoneOf("a", 'a'));
    }

    @Test
    void testContainsAnyOfWithCharArray() {
        // Test with matching characters
        Assertions.assertTrue(TextUtil.containsAnyOf("Hello", 'l', 'o', 'z'));
        Assertions.assertTrue(TextUtil.containsAnyOf("12345", '3', '6', '9'));

        // Test with no matching characters
        Assertions.assertFalse(TextUtil.containsAnyOf("Hello", 'x', 'y', 'z'));
        Assertions.assertFalse(TextUtil.containsAnyOf("12345", 'a', 'b', 'c'));

        // Test with empty string
        Assertions.assertFalse(TextUtil.containsAnyOf("", 'a', 'b', 'c'));

        // Test with empty char array
        Assertions.assertFalse(TextUtil.containsAnyOf("Hello"));

        // Test with single character
        Assertions.assertFalse(TextUtil.containsAnyOf("a", 'b'));
        Assertions.assertTrue(TextUtil.containsAnyOf("a", 'a'));
    }

    @Test
    void testToCharArray() {
        // Test with a regular string
        String input = "Hello World";
        char[] expected = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd'};
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(input));

        // Test with an empty string
        input = "";
        expected = new char[0];
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(input));

        // Test with a single character string
        input = "A";
        expected = new char[]{'A'};
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(input));

        // Test with a string containing special characters
        input = "Text!$&*(){}[]";
        expected = new char[]{'T', 'e', 'x', 't', '!', '$', '&', '*', '(', ')', '{', '}', '[', ']'};
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(input));

        // Test with a StringBuilder
        StringBuilder builder = new StringBuilder("BuilderTest");
        expected = new char[]{'B', 'u', 'i', 'l', 'd', 'e', 'r', 'T', 'e', 's', 't'};
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(builder));

        // Test with a large string
        input = "A".repeat(1000);
        expected = input.toCharArray();
        Assertions.assertArrayEquals(expected, TextUtil.toCharArray(input));
    }

    @Test
    void testCharsToBytesWithValidInput() {
        char[] input = {'H', 'e', 'l', 'l', 'o'};
        byte[] expected = "Hello".getBytes(StandardCharsets.UTF_8);
        byte[] result = TextUtil.toByteArray(input);
        Assertions.assertArrayEquals(expected, result, "Expected byte array does not match the result.");
    }

    @Test
    void testCharsToBytesWithEmptyInput() {
        char[] input = {};
        byte[] expected = new byte[0];
        byte[] result = TextUtil.toByteArray(input);
        Assertions.assertArrayEquals(expected, result, "Expected an empty byte array.");
    }

    @Test
    void testCharsToBytesWithSpecialCharacters() {
        char[] input = {'Ω', '漢', '字'};
        String expectedString = new String(input);
        byte[] expected = expectedString.getBytes(StandardCharsets.UTF_8);
        byte[] result = TextUtil.toByteArray(input);
        Assertions.assertArrayEquals(expected, result, "Expected byte array for special characters does not match the result.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abcdef"})
    void testAsCharSequence(String s) {
        CharSequence cs = TextUtil.asCharSequence(s.toCharArray());

        Assertions.assertTrue(TextUtil.contentEquals(s, cs));
        Assertions.assertEquals(s, cs.toString());
        Assertions.assertEquals(s.length(), cs.length());

        for (int i = -1; i <= s.length() + 1; i++) {
            for (int j = -1; j <= s.length() + 1; j++) {
                if (i < 0 || j < 0 || j < i || i > s.length() || j > s.length()) {
                    int ii = i;
                    int jj = j;
                    Assertions.assertThrows(IllegalArgumentException.class, () -> cs.subSequence(ii, jj));
                } else {
                    Assertions.assertEquals(s.substring(i, j), cs.subSequence(i, j).toString());
                }
            }
        }
    }

    @Test
    void testStripTrailingWithWhitespace() {
        String input = "Test String   ";
        CharSequence result = TextUtil.stripTrailing(input);
        Assertions.assertEquals("Test String", result.toString(), "Expected to remove trailing whitespaces.");
    }

    @Test
    void testStripTrailingWithoutWhitespace() {
        String input = "TestString";
        CharSequence result = TextUtil.stripTrailing(input);
        Assertions.assertEquals("TestString", result.toString(), "Expected no change for strings without trailing whitespaces.");
    }

    @Test
    void testStripTrailingWithEmptyString() {
        String input = "";
        CharSequence result = TextUtil.stripTrailing(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string as result for empty input.");
    }

    @Test
    void testStripTrailingWithOnlyWhitespace() {
        String input = "   ";
        CharSequence result = TextUtil.stripTrailing(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string after removing all whitespaces.");
    }

    @Test
    void testStripLeadingWithOnlyWhitespace() {
        String input = "   ";
        CharSequence result = TextUtil.stripLeading(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string after removing leading whitespaces.");
    }

    @Test
    void testStripLeadingWithLeadingWhitespace() {
        String input = "   Test String";
        CharSequence result = TextUtil.stripLeading(input);
        Assertions.assertEquals("Test String", result.toString(), "Expected to remove leading whitespaces only.");
    }

    @Test
    void testStripLeadingWithoutWhitespace() {
        String input = "TestString";
        CharSequence result = TextUtil.stripLeading(input);
        Assertions.assertEquals("TestString", result.toString(), "Expected no change for strings without leading whitespaces.");
    }

    @Test
    void testStripLeadingWithEmptyString() {
        String input = "";
        CharSequence result = TextUtil.stripLeading(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string as result for empty input.");
    }

    @Test
    void testStripWithWhitespace() {
        // Test with leading and trailing whitespaces
        String input = "   Test String   ";
        CharSequence result = TextUtil.strip(input);
        Assertions.assertEquals("Test String", result.toString(), "Expected to remove both leading and trailing whitespaces.");

        // Test without any whitespaces
        input = "TestString";
        result = TextUtil.strip(input);
        Assertions.assertEquals("TestString", result.toString(), "Expected no change for strings without whitespaces.");

        // Test with only whitespaces
        input = "   ";
        result = TextUtil.strip(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string after removing all whitespaces.");

        // Test with an empty string
        input = "";
        result = TextUtil.strip(input);
        Assertions.assertEquals("", result.toString(), "Expected empty string as result for empty input.");
    }

    @Test
    @DisplayName("Test HTML escaping/unescaping roundtrip")
    void testHtmlRoundtrip() {
        List<String> testCases = List.of(
                // Basic test cases
                "",
                "Hello World",
                "<div>Test</div>",
                "Quote: \"Test\"",
                "Apostrophe: 'Test'",
                "Ampersand: &",
                // Numeric entities
                "Decimal: &#169;",
                "Hex: &#x00A9;",
                // Invalid entities
                "Invalid: &invalid;",
                "Incomplete: &quot",
                // Mixed content
                "Mixed: <div>&quot;Test&quot;</div>&#169; &amp; &#x00A9;",
                // Multiple entities
                "&lt;&gt;&amp;&quot;&apos;",
                // Corner case with multiple ampersands
                "&&amp;&&&amp;&&",
                // Entities at start/end
                "&quot;test&quot;",
                // Unicode characters
                "Unicode: §±®©™"
        );

        for (String test : testCases) {
            String unescaped = TextUtil.unescapeHtml(test);
            String escaped = TextUtil.escapeHtml(unescaped);
            String roundtrip = TextUtil.unescapeHtml(escaped);

            Assertions.assertEquals(unescaped, roundtrip,
                    "Roundtrip failed for: " + test);
        }

        // Test null input
        Throwable t = Assertions.assertThrows(Throwable.class, () -> TextUtil.unescapeHtml(null));
        Assertions.assertTrue(t instanceof NullPointerException || t instanceof AssertionError, "unexpected exception thrown: " + t.getClass());
    }

    @ParameterizedTest
    @MethodSource("normalizeTestCases")
    void testNormalize(String input, String expected, String message) {
        String result = TextUtil.normalize(input);
        Assertions.assertEquals(expected, result, message);
    }

    private static Stream<Arguments> normalizeTestCases() {
        return Stream.of(
                Arguments.of("Test Normalization", "Test Normalization", "Expected normalized text to match the input."),
                Arguments.of("e\u0301", "\u00E9", "Expected normalized text to combine characters."),
                Arguments.of("\u212B", "Å", "Expected normalized text to replace Unicode equivalent."),
                Arguments.of("", "", "Expected normalized result to be an empty string.")
        );
    }

    @ParameterizedTest
    @MethodSource("normalizeEmailTestCases")
    void testNormalizeEmail(String input, String expected, Class<? extends Throwable> expectedException) {
        if (expectedException != null) {
            Assertions.assertThrows(expectedException, () -> TextUtil.normalizeEmail(input));
        } else {
            Assertions.assertEquals(expected, TextUtil.normalizeEmail(input));
        }
    }

    private static Stream<Arguments> normalizeEmailTestCases() {
        return Stream.of(
                // Valid emails - should normalize successfully
                Arguments.of("user@example.com", "user@example.com", null),
                Arguments.of(" USER@EXAMPLE.COM ", "user@example.com", null),
                Arguments.of("user.name+alias@Example.Com", "user.name+alias@example.com", null),
                Arguments.of("\nuser@example.com ", "user@example.com", null),
                Arguments.of("user+filter@example.com", "user+filter@example.com", null),
                Arguments.of("User.Name-With.Dash@EXAMPLE.CO.UK", "user.name-with.dash@example.co.uk", null),
                Arguments.of("user_with_underscore@Example.Com", "user_with_underscore@example.com", null),

                // Invalid emails - should throw IllegalArgumentException
                Arguments.of("userexample.com", null, IllegalArgumentException.class),
                Arguments.of("", null, IllegalArgumentException.class),
                Arguments.of("   ", null, IllegalArgumentException.class),
                Arguments.of("my\nuser@example.com", null, IllegalArgumentException.class),
                Arguments.of("@example.com", null, IllegalArgumentException.class),
                Arguments.of("user@", null, IllegalArgumentException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("normalizeEmailWithInvalidEmailTestCases")
    void testNormalizeEmailWithInvalidEmails(String input, String expected, Class<? extends Throwable> expectedException) {
        if (expectedException != null) {
            Assertions.assertThrows(expectedException, () -> TextUtil.normalizeEmail(input));
        } else {
            Assertions.assertEquals(expected, TextUtil.normalizeEmail(input));
        }
    }

    private static Stream<Arguments> normalizeEmailWithInvalidEmailTestCases() {
        return Stream.of(
                // Test missing "@" symbol
                Arguments.of("userexample.com", null, IllegalArgumentException.class),

                // Test with empty string
                Arguments.of("", null, IllegalArgumentException.class),

                // Test with only whitespaces
                Arguments.of("   ", null, IllegalArgumentException.class),

                // Test with newline character
                Arguments.of("my\nuser@example.com", null, IllegalArgumentException.class),

                // Test with "@" symbol at invalid positions
                Arguments.of("@example.com", null, IllegalArgumentException.class),
                Arguments.of("user@", null, IllegalArgumentException.class)
        );
    }
}
