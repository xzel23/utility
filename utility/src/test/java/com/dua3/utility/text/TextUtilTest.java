// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
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

import static com.dua3.utility.text.TextUtil.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextUtilTest {

    @ParameterizedTest
    @MethodSource("generateTestData_transform")
    void testTransform(String template, Function<String, String> env, String expected) {
        String actual = transform(template, env);
        assertEquals(expected, actual);
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
        RichText actual = transform(template, env);
        assertEquals(expected, actual);
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
        String actual = transform(template, Map.of("NAME", "Axel"));

        assertEquals(expected, actual);
    }

    record TestDataAlign(String text, String expected, int width, Alignment align, Character fill) {}

    @ParameterizedTest
    @MethodSource("generateTestDataAlign")
    void testAlign(TestDataAlign data) {
        if (data.fill() == null) {
            assertEquals(data.expected(), align(data.text(), data.width(), data.align()));
        } else {
            assertEquals(data.expected(), align(data.text(), data.width(), data.align(), data.fill()));
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
                wrap(LangUtil.getResourceAsString(getClass(), data.fileIn()), data.width, data.align, data.hardWrap),
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
        assertEquals("Test Input", nonEmptyOr(input, defaultInput));

        // Test with empty CharSequence - Expected to return defaultString
        input = "";
        assertEquals("Default", nonEmptyOr(input, defaultInput));

        // Test with null CharSequence - Expected to return defaultString
        assertEquals("Default", nonEmptyOr(null, defaultInput));
    }

    @Test
    void testNonEmptyOr_RichText() {
        // Test with non-null, non-empty CharSequence - Expected to return inputString itself
        RichText input = RichText.valueOf("Test Input");
        RichText defaultInput = RichText.valueOf("Default");
        assertEquals(input, nonEmptyOr(input, defaultInput));

        // Test with empty CharSequence - Expected to return defaultString
        input = RichText.valueOf("");
        assertEquals(defaultInput, nonEmptyOr(input, defaultInput));

        // Test with null CharSequence - Expected to return defaultString
        assertEquals(defaultInput, nonEmptyOr(null, defaultInput));
    }

    @Test
    void testEscapeHTML() {
        String normalString = "<div>Test Content</div>";
        String escapedString = escapeHTML(normalString);
        assertEquals("&lt;div&gt;Test Content&lt;/div&gt;", escapedString);

        String stringWithAmpersand = "Tom & Jerry";
        escapedString = escapeHTML(stringWithAmpersand);
        assertEquals("Tom &amp; Jerry", escapedString);

        String specialCharactersString = "< > & \" ' /";
        escapedString = escapeHTML(specialCharactersString);
        assertEquals("&lt; &gt; &amp; &quot; &apos; /", escapedString);
    }

    @Test
    void testDecodeFontSize() {
        // Test with "pt"
        assertEquals(10.0f, decodeFontSize("10pt"), 0.001);

        // Test with "em"
        assertEquals(120.0f, decodeFontSize("10em"), 0.001);

        // Test with "px"
        assertEquals(7.5f, decodeFontSize("10px"), 0.001);

        // Test with "%"
        assertEquals(1.2f, decodeFontSize("10%"), 0.001);

        // Test with unknown unit
        assertThrows(IllegalArgumentException.class, () -> decodeFontSize("10abc"));

        // Test with "vw"
        assertEquals(120.0f, decodeFontSize("10vw"), 0.001);

        // Test with empty string
        assertThrows(IllegalArgumentException.class, () -> decodeFontSize(""));
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
            assertEquals(expected, getTextDimension(text, font));
        else if (expected instanceof Class<?> && Exception.class.isAssignableFrom((Class<?>) expected))
            assertThrows((Class<? extends Throwable>) expected, () -> getTextDimension(text, font));
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

        Rectangle2f textDimensionHiJohn = getTextDimension(textHiJohn, timesRoman12);
        Rectangle2f richTextDimensionHiJohn = getRichTextDimension(textHiJohn, timesRoman12);
        assertEquals(textDimensionHiJohn, richTextDimensionHiJohn);

        // the bold font should use more space
        RichText textHiBoldJohn = new RichTextBuilder()
                .append("Hi ")
                .push(Style.BOLD)
                .append("John")
                .pop(Style.BOLD)
                .append("!")
                .toRichText();

        Rectangle2f richTextDimensionHiBoldJohn = getRichTextDimension(textHiBoldJohn, timesRoman12);
        assertTrue(textDimensionHiJohn.height() <= richTextDimensionHiBoldJohn.height());
        assertTrue(textDimensionHiJohn.width() < richTextDimensionHiBoldJohn.width());
    }

    @Test
    void testToStringWithNonNullObject() {
        String result = TextUtil.toString(123, "default");
        assertEquals("123", result, "Expected toString() of 123 to be '123'");
    }

    @Test
    void testToStringWithNullObject() {
        String result = TextUtil.toString(null, "default");
        assertEquals("default", result, "Expected toString() of null to be 'default'");
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
        assertEquals("CustomObject", result, "Expected toString() of custom object to be 'CustomObject'");
    }

    @Test
    void testToStringWithEmptyValueIfNull() {
        String result = TextUtil.toString(null, "");
        assertEquals("", result, "Expected toString() of null to be an empty string");
    }

    @Test
    void testLexicographicComparatorWithDefaultLocale() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.getDefault());
        assertTrue(comparator.compare("apple", "banana") < 0, "Expected 'apple' to come before 'banana'");
        assertTrue(comparator.compare("banana", "apple") > 0, "Expected 'banana' to come after 'apple'");
        assertEquals(0, comparator.compare("apple", "apple"), "Expected 'apple' to be equal to 'apple'");
    }

    @Test
    void testLexicographicComparatorWithCustomLocale() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.FRENCH);
        assertTrue(comparator.compare("éclair", "ete") < 0, "Expected 'éclair' to come before 'ete' in French locale");
        assertTrue(comparator.compare("été", "éclair") > 0, "Expected 'été' to come after 'éclair' in French locale");
        assertEquals(0, comparator.compare("été", "été"), "Expected 'été' to be equal to 'été'");
    }

    @Test
    void testLexicographicComparatorWithNullValues() {
        Comparator<String> comparator = TextUtil.lexicographicComparator(Locale.getDefault());
        assertTrue(comparator.compare(null, "apple") < 0, "Expected null to come before 'apple'");
        assertTrue(comparator.compare("apple", null) > 0, "Expected 'apple' to come after null");
        assertEquals(0, comparator.compare(null, null), "Expected null to be equal to null");
    }

    @Test
    void testAppendHtmlEscapedCharacters() {
        StringBuilder sb = new StringBuilder();
        appendHtmlEscapedCharacters(sb, "<div>Test & 'Quote' \"DoubleQuote\"</div>");
        assertEquals("&lt;div&gt;Test &amp; &apos;Quote&apos; &quot;DoubleQuote&quot;&lt;/div&gt;", sb.toString());

        // Test with empty string
        sb = new StringBuilder();
        appendHtmlEscapedCharacters(sb, "");
        assertEquals("", sb.toString());

        // Test with non-ASCII characters
        sb = new StringBuilder();
        appendHtmlEscapedCharacters(sb, "Café");
        assertEquals("Caf&#233;", sb.toString());
    }

    @Test
    void testEscape() {
        // Test with special characters
        assertEquals("\\\"Hello\\\"", escape("\"Hello\""));
        assertEquals("\\\\backslash", escape("\\backslash"));
        assertEquals("Tab\\tNewline\\n", escape("Tab\tNewline\n"));
        assertEquals("Carriage\\rReturn", escape("Carriage\rReturn"));
        assertEquals("Form\\fFeed", escape("Form\fFeed"));
        assertEquals("Backspace\\b", escape("Backspace\b"));
        assertEquals("\\'Single Quote\\'", escape("'Single Quote'"));

        // Test with null character
        assertEquals("Null\\u0000Character", escape("Null\0Character"));

        // Test with non-ASCII characters
        assertEquals("Café", escape("Café"));
        assertEquals("你好!", escape("你好!"));

        // Test with character not belonging to any of the above classes
        assertEquals("\\u2603", escapeASCII("☃"));

        // Test with empty string
        assertEquals("", escape(""));
    }

    @Test
    void testEscapeASCII() {
        // Test with special characters
        assertEquals("\\\"Hello\\\"", escapeASCII("\"Hello\""));
        assertEquals("\\\\backslash", escapeASCII("\\backslash"));
        assertEquals("Tab\\tNewline\\n", escapeASCII("Tab\tNewline\n"));
        assertEquals("Carriage\\rReturn", escapeASCII("Carriage\rReturn"));
        assertEquals("Form\\fFeed", escapeASCII("Form\fFeed"));
        assertEquals("Backspace\\b", escapeASCII("Backspace\b"));
        assertEquals("\\'Single Quote\\'", escapeASCII("'Single Quote'"));

        // Test with null character
        assertEquals("Null\\u0000Character", escapeASCII("Null\0Character"));

        // Test with non-ASCII characters
        assertEquals("Caf\\u00E9", escapeASCII("Café"));
        assertEquals("\\u4F60\\u597D!", escapeASCII("你好!"));

        // Test with empty string
        assertEquals("", escapeASCII(""));
    }

    @Test
    void testContentEquals() {
        // Test with equal content
        assertTrue(contentEquals("test", "test"));
        assertTrue(contentEquals(new StringBuilder("test"), "test"));

        // Test with different content
        assertTrue(!contentEquals("test", "Test"));
        assertTrue(!contentEquals("test", "test1"));

        // Test with empty strings
        assertTrue(contentEquals("", ""));
    }

    @Test
    void testContains() {
        // Test with substring present
        assertTrue(contains("Hello World", "World"));
        assertTrue(contains("Hello World", "Hello"));
        assertTrue(contains("Hello World", "o W"));

        // Test with substring not present
        assertTrue(!contains("Hello World", "world"));
        assertTrue(!contains("Hello World", "Hello  World"));

        // Test with empty strings
        assertTrue(contains("Hello", ""));
        assertTrue(!contains("", "Hello"));
    }

    @Test
    void testContainsNoneOf() {
        // Test with no matching characters
        assertTrue(containsNoneOf("Hello", "xyz"));
        assertTrue(containsNoneOf("12345", "abcde"));

        // Test with matching characters
        assertTrue(!containsNoneOf("Hello", "lo"));
        assertTrue(!containsNoneOf("12345", "56"));

        // Test with empty strings
        assertTrue(containsNoneOf("", "xyz"));
        assertTrue(containsNoneOf("Hello", ""));
    }

    @Test
    void testContainsAnyOf() {
        // Test with matching characters
        assertTrue(containsAnyOf("Hello", "lo"));
        assertTrue(containsAnyOf("12345", "56"));

        // Test with no matching characters
        assertTrue(!containsAnyOf("Hello", "xyz"));
        assertTrue(!containsAnyOf("12345", "abcde"));

        // Test with empty strings
        assertTrue(!containsAnyOf("", "xyz"));
        assertTrue(!containsAnyOf("Hello", ""));
    }

    @Test
    void testIndexOf() {
        // Test indexOf(CharSequence, int)
        assertEquals(1, indexOf("Hello", 'e'));
        assertEquals(-1, indexOf("Hello", 'x'));

        // Test indexOf(CharSequence, CharSequence)
        assertEquals(0, indexOf("Hello", "He"));
        assertEquals(3, indexOf("Hello", "lo"));
        assertEquals(-1, indexOf("Hello", "hi"));

        // Test indexOf(CharSequence, int, int)
        assertEquals(2, indexOf("Hello", 'l', 0));
        assertEquals(3, indexOf("Hello", 'l', 3));
        assertEquals(4, indexOf("Hello", 'o', 0));
        assertEquals(-1, indexOf("Hello", 'e', 2));

        // Test indexOf(CharSequence, CharSequence, int)
        assertEquals(0, indexOf("Hello", "He", 0));
        assertEquals(3, indexOf("Hello", "lo", 0));
        assertEquals(-1, indexOf("Hello", "He", 1));
        assertEquals(-1, indexOf("Hello", "hi", 0));
    }

    @Test
    void testStartsWith() {
        // Test with matching prefix
        assertTrue(startsWith("Hello", "He"));
        assertTrue(startsWith("Hello", "Hello"));

        // Test with non-matching prefix
        assertTrue(!startsWith("Hello", "he"));
        assertTrue(!startsWith("Hello", "Hello World"));

        // Test with empty strings
        assertTrue(startsWith("Hello", ""));
        assertTrue(!startsWith("", "Hello"));
    }

    @Test
    void testGroupMatch() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)");
        String input = "key:value";
        Matcher matcher = pattern.matcher(input);
        assertTrue(matcher.matches());

        Optional<CharSequence> prefix = group(matcher, input, "prefix");
        Optional<CharSequence> value = group(matcher, input, "value");

        assertTrue(prefix.isPresent());
        assertEquals("key", prefix.get().toString());

        assertTrue(value.isPresent());
        assertEquals("value", value.get().toString());
    }

    @Test
    void testGroupNoMatch() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)?");
        String input = "key:";
        Matcher matcher = pattern.matcher(input);
        assertTrue(matcher.matches());

        Optional<CharSequence> prefix = group(matcher, input, "prefix");
        Optional<CharSequence> value = group(matcher, input, "value");

        assertTrue(prefix.isPresent());
        assertEquals("key", prefix.get().toString());

        assertTrue(value.isEmpty());
    }

    @Test
    void testGroupNonExisting() {
        // Test with matching group
        Pattern pattern = Pattern.compile("(?<prefix>\\w+):(?<value>\\w+)");
        String input = "key:value";
        Matcher matcher = pattern.matcher(input);
        assertTrue(matcher.matches());

        // Test with non-existing group
        assertThrows(
                IllegalArgumentException.class,
                () -> group(matcher, input, "nonexistent"),
                "should throw IllegalStateException for non-existing group"
        );
    }

    @Test
    void testGetMD5String() throws IOException {
        // Test with string input
        String input = "Hello, World!";
        String md5 = getMD5String(input);
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5);

        // Test with byte array input
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        String md5Bytes = getMD5String(bytes);
        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5Bytes);

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String md5Stream = getMD5String(is);
            assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5Stream);
        }
    }

    @Test
    void testGetMD5() throws IOException {
        // Test with string input
        String input = "Hello, World!";
        byte[] md5 = getMD5(input);

        // Test with byte array input
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = getMD5(bytes);
        assertArrayEquals(md5, md5Bytes);

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            byte[] md5Stream = getMD5(is);
            assertArrayEquals(md5, md5Stream);
        }
    }

    @Test
    void testGetDigest() throws NoSuchAlgorithmException, IOException {
        // Test with byte array input
        String input = "Hello, World!";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        byte[] sha256 = getDigest("SHA-256", bytes);
        assertEquals(32, sha256.length); // SHA-256 produces 32 bytes

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            byte[] sha256Stream = getDigest("SHA-256", is);
            assertArrayEquals(sha256, sha256Stream);
        }
    }

    @Test
    void testGetDigestString() throws NoSuchAlgorithmException, IOException {
        // Test with byte array input
        String input = "Hello, World!";
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        String sha256 = getDigestString("SHA-256", bytes);
        assertEquals(64, sha256.length()); // SHA-256 hex string is 64 characters

        // Test with InputStream input
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String sha256Stream = getDigestString("SHA-256", is);
            assertEquals(sha256, sha256Stream);
        }
    }

    @Test
    void testGenerateMailToLink() {
        // Test with simple email and subject
        String link = generateMailToLink("test@example.com", "Test Subject");
        assertEquals("mailto:test@example.com?subject=Test%20Subject", link);

        // Test with email and subject containing special characters
        link = generateMailToLink("test@example.com", "Test & Subject");
        assertEquals("mailto:test@example.com?subject=Test%20%26%20Subject", link);
    }

    @Test
    void testLineEndConversions() {
        // Test text with mixed line endings
        String mixedText = "Line1\r\nLine2\nLine3\rLine4";

        // Test toUnixLineEnds
        String unixText = toUnixLineEnds(mixedText);
        assertEquals("Line1\nLine2\nLine3\nLine4", unixText);

        // Test toWindowsLineEnds
        String windowsText = toWindowsLineEnds(mixedText);
        assertEquals("Line1\r\nLine2\r\nLine3\r\nLine4", windowsText);

        // Test setLineEnds with custom line ending
        String customText = setLineEnds(mixedText, "|");
        assertEquals("Line1|Line2|Line3|Line4", customText);
    }

    @Test
    void testQuote() {
        // Test with simple string
        assertEquals("\"Hello\"", quote("Hello"));

        // Test with string containing quotes
        assertEquals("\"Hello \\\"World\\\"\"", quote("Hello \"World\""));

        // Test with string containing special characters
        assertEquals("\"Tab\\tNewline\\n\"", quote("Tab\tNewline\n"));

        // Test with empty string
        assertEquals("\"\"", quote(""));
    }

    @Test
    void testQuoteIfNeeded() {
        // Test with string that needs quoting
        assertEquals("\"Hello World\"", quoteIfNeeded("Hello World"));

        // Test with string that doesn't need quoting
        assertEquals("Hello", quoteIfNeeded("Hello"));

        // Test with empty string
        assertEquals("", quoteIfNeeded(""));
    }

    @Test
    void testJoinQuotedIfNeeded() {
        // Test with list of strings
        List<String> list = List.of("Hello", "World", "Test");
        assertEquals("Hello, World, Test", joinQuotedIfNeeded(list));

        // Test with list containing strings that need quoting
        list = List.of("Hello", "World Test", "End");
        assertEquals("Hello, \"World Test\", End", joinQuotedIfNeeded(list));

        // Test with custom delimiter
        assertEquals("Hello|\"World Test\"|End", joinQuotedIfNeeded(list, "|"));

        // Test with empty list
        assertEquals("", joinQuotedIfNeeded(List.of()));
    }

    @Test
    void testJoinQuoted() {
        // Test with list of strings
        List<String> list = List.of("Hello", "World", "Test");
        assertEquals("\"Hello\", \"World\", \"Test\"", joinQuoted(list));

        // Test with custom delimiter
        assertEquals("\"Hello\"|\"World\"|\"Test\"", joinQuoted(list, "|"));

        // Test with empty list
        assertEquals("", joinQuoted(List.of()));
    }

    @Test
    void testIsBlank() {
        // Test with blank strings
        assertTrue(isBlank(""));
        assertTrue(isBlank(" "));
        assertTrue(isBlank("\t"));
        assertTrue(isBlank("\n"));
        assertTrue(isBlank(" \t\n\r"));

        // Test with non-blank strings
        assertTrue(!isBlank("a"));
        assertTrue(!isBlank(" a "));
        assertTrue(!isBlank("\ta\n"));
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
        appendHtmlEscapedCharacters(app, "<div>Test & 'Quote' \"DoubleQuote\"</div>");
        assertEquals("&lt;div&gt;Test &amp; &apos;Quote&apos; &quot;DoubleQuote&quot;&lt;/div&gt;", app.toString());

        // Test with empty string
        app = new TestAppendable();
        appendHtmlEscapedCharacters(app, "");
        assertEquals("", app.toString());

        // Test with non-ASCII characters
        app = new TestAppendable();
        appendHtmlEscapedCharacters(app, "Café");
        assertEquals("Caf&#233;", app.toString());

        // Test with null character
        app = new TestAppendable();
        appendHtmlEscapedCharacters(app, "\0");
        assertEquals("&#0;", app.toString());
    }

    @Test
    void testTransformWithMapEntries() {
        // Test with varargs Map.Entry
        String template = "Hello ${NAME}, welcome to ${PLACE}!";

        Map.Entry<String, String> entry1 = Map.entry("NAME", "John");
        Map.Entry<String, String> entry2 = Map.entry("PLACE", "Wonderland");

        String result = transform(template, entry1, entry2);
        assertEquals("Hello John, welcome to Wonderland!", result);

        // Test with single entry
        template = "Hello ${NAME}!";
        result = transform(template, Map.entry("NAME", "Alice"));
        assertEquals("Hello Alice!", result);

        // Test with non-existent placeholder
        template = "Hello ${NAME}, how are you ${MOOD}?";
        result = transform(template, Map.entry("NAME", "Bob"));
        assertEquals("Hello Bob, how are you MOOD?", result);

        // Test with empty template
        template = "";
        result = transform(template, Map.entry("NAME", "Charlie"));
        assertEquals("", result);

        // Test with null value using AbstractMap.SimpleEntry which allows null values
        template = "Hello ${NAME}!";
        result = transform(template, new AbstractMap.SimpleEntry<>("NAME", null));
        assertEquals("Hello null!", result);
    }

    @Test
    void testContainsNoneOfWithCharArray() {
        // Test with no matching characters
        assertTrue(containsNoneOf("Hello", 'x', 'y', 'z'));
        assertTrue(containsNoneOf("12345", 'a', 'b', 'c'));

        // Test with matching characters
        assertTrue(!containsNoneOf("Hello", 'l', 'o', 'z'));
        assertTrue(!containsNoneOf("12345", '3', '6', '9'));

        // Test with empty string
        assertTrue(containsNoneOf("", 'a', 'b', 'c'));

        // Test with empty char array
        assertTrue(containsNoneOf("Hello"));

        // Test with single character
        assertTrue(containsNoneOf("a", 'b'));
        assertTrue(!containsNoneOf("a", 'a'));
    }

    @Test
    void testContainsAnyOfWithCharArray() {
        // Test with matching characters
        assertTrue(containsAnyOf("Hello", 'l', 'o', 'z'));
        assertTrue(containsAnyOf("12345", '3', '6', '9'));

        // Test with no matching characters
        assertTrue(!containsAnyOf("Hello", 'x', 'y', 'z'));
        assertTrue(!containsAnyOf("12345", 'a', 'b', 'c'));

        // Test with empty string
        assertTrue(!containsAnyOf("", 'a', 'b', 'c'));

        // Test with empty char array
        assertTrue(!containsAnyOf("Hello"));

        // Test with single character
        assertTrue(!containsAnyOf("a", 'b'));
        assertTrue(containsAnyOf("a", 'a'));
    }
}
