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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.dua3.utility.text.TextUtil.align;
import static com.dua3.utility.text.TextUtil.decodeFontSize;
import static com.dua3.utility.text.TextUtil.escapeHTML;
import static com.dua3.utility.text.TextUtil.getRichTextDimension;
import static com.dua3.utility.text.TextUtil.getTextDimension;
import static com.dua3.utility.text.TextUtil.nonEmptyOr;
import static com.dua3.utility.text.TextUtil.transform;
import static com.dua3.utility.text.TextUtil.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextUtilTest {

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
    public void testTransformWithMapArgument() {
        String template = "Hello ${NAME}.";

        String expected = "Hello Axel.";
        String actual = transform(template, Map.of("NAME", "Axel"));

        assertEquals(expected, actual);
    }

    record TestDataAlign(String text, String expected, int width, Alignment align, Character fill) {}

    @ParameterizedTest
    @MethodSource("generateTestDataAlign")
    public void testAlign(TestDataAlign data) {
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
    public void testGetTextDimension(CharSequence text, Font font, Object expected) {
        if (expected instanceof Dimension2f)
            assertEquals(expected, getTextDimension(text, font));
        else if (expected instanceof Class<?> && Exception.class.isAssignableFrom((Class<?>) expected))
            assertThrows((Class<? extends Throwable>) expected, () -> getTextDimension(text, font));
    }

    @Test
    public void testGetTextDimensionRichtext() {
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
}
