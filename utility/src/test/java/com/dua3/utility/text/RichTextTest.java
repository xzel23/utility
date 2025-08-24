// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Axel Howind
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "StringBufferWithoutInitialCapacity", "SpellCheckingInspection", "EqualsWithItself"})
class RichTextTest {
    private static final Logger LOG = LogManager.getLogger(RichTextTest.class);

    @Test
    void testValueOf() {
        String s = "hello world!";
        RichText text = RichText.valueOf(s);

        String expected = s;
        String actual = text.toString();

        assertEquals(expected, actual);
    }

    @Test
    void testEquals() {
        // tests all sorts if equals comparisons
        String s = "hello world!";
        RichText a = RichText.valueOf(s);
        RichText b = RichText.valueOf(s);

        RichText c = RichText.valueOf(s);
        RichText d = RichText.valueOf(s);

        RichText e = RichText.valueOf("Hello World!");
        RichText f = RichText.valueOf("Hello World!");

        assertEquals(s, a.toString());
        assertEquals(a, a);
        assertEquals(b, b);
        assertEquals(c, c);
        assertEquals(d, c);

        assertEquals(a, b);
        assertEquals(a, c);
        assertEquals(a, d);
        assertEquals(b, c);
        assertEquals(b, d);
        assertEquals(c, d);
        assertEquals(b, a);
        assertEquals(c, a);
        assertEquals(d, a);
        assertEquals(c, b);
        assertEquals(d, b);
        assertEquals(d, c);

        assertEquals(e, e);
        assertEquals(f, f);
        assertEquals(e, f);
        assertEquals(f, e);

        assertNotEquals(a, e);
        assertNotEquals(b, e);
        assertNotEquals(c, e);
        assertNotEquals(d, e);
        assertNotEquals(a, f);
        assertNotEquals(b, f);
        assertNotEquals(c, f);
        assertNotEquals(d, f);

        assertNotEquals(e, a);
        assertNotEquals(e, b);
        assertNotEquals(e, c);
        assertNotEquals(e, d);
        assertNotEquals(f, a);
        assertNotEquals(f, b);
        assertNotEquals(f, c);
        assertNotEquals(f, d);
    }

    @Test
    void testEqualsWithHashCollision() {
        RichText a = RichText.valueOf("FB");
        RichText b = RichText.valueOf("Ea");

        Assumptions.assumeTrue(a.hashCode() == b.hashCode(), "hashes do not matxh");
        assertNotEquals(a, b);
    }

    @Test
    void testisEmpty() {
        assertTrue(RichText.emptyText().isEmpty());
        assertFalse(RichText.valueOf(".").isEmpty());
    }

    @Test
    void testEqualsText() {
        // tests all sorts if equals comparisons
        RichText text = RichText.valueOf("text");

        RichText blue = text.wrap(Style.BLUE);
        RichText red = text.wrap(Style.RED);
        RichText upper = RichText.valueOf("TEXT");

        RichText texts = RichText.valueOf("texts");
        RichText blues = texts.wrap(Style.BLUE);
        RichText blue2 = blues.subSequence(0, 4);

        assertFalse(text.equalsText(upper));
        assertTrue(text.equalsText(text));
        assertTrue(text.equalsText(blue));
        assertTrue(text.equalsText(red));
        assertFalse(text.equalsText(texts));

        assertTrue(RichText.textAndFontEquals(blue, blue2));
        assertFalse(RichText.textAndFontEquals(blue, blues));

        assertTrue(blue.equalsTextAndFont(blue2));
        assertTrue(blue.equalsTextAndFont(blue2));
        assertFalse(blue.equalsTextAndFont(red));

        assertTrue(text.equalsTextIgnoreCase(upper));
        assertTrue(text.equalsTextIgnoreCase(text));
        assertTrue(text.equalsTextIgnoreCase(blue));
        assertTrue(text.equalsTextIgnoreCase(red));
        assertFalse(text.equalsTextIgnoreCase(texts));
    }

    @Test
    void testEqualizer() {
        // tests all sorts if equals comparisons
        RichText text = RichText.valueOf("text");
        RichText upper = RichText.valueOf("TEXT");
        RichText bigger = text.wrap(Style.create("bigger", Map.entry(Style.FONT_SIZE, 20f)));
        RichText smaller = text.wrap(Style.create("smaller", Map.entry(Style.FONT_SIZE, 8f)));
        RichText serif = text.wrap(Style.SERIF);
        RichText sans = text.wrap(Style.SANS_SERIF);
        RichText texts = RichText.valueOf("texts");
        RichText arial = text.wrap(Style.create("arial", Map.entry(Style.FONT, FontUtil.getInstance().getFont("Arial-12"))));
        RichText arialSubset = text.wrap(Style.create("AAAAAA+arial", Map.entry(Style.FONT, FontUtil.getInstance().getFont("AAAAAA+Arial-12"))));
        RichText helvetica = text.wrap(Style.create("arial", Map.entry(Style.FONT, FontUtil.getInstance().getFont("Helvetica-12"))));

        BiPredicate<RichText, RichText> ignoreColor = RichText.equalizer(ComparisonSettings.builder().setIgnoreTextColor(true).build());
        assertTrue(ignoreColor.test(text, text.wrap(Style.RED)));
        assertTrue(ignoreColor.test(text.wrap(Style.BLUE), text.wrap(Style.RED)));
        assertTrue(ignoreColor.test(text.wrap(Style.BLUE), text));
        assertFalse(ignoreColor.test(texts, text));
        assertFalse(ignoreColor.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreColor.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreBold = RichText.equalizer(ComparisonSettings.builder().setIgnoreBold(true).build());
        assertTrue(ignoreBold.test(text, text.wrap(Style.BOLD)));
        assertTrue(ignoreBold.test(text.wrap(Style.BOLD), text.wrap(Style.BOLD)));
        assertTrue(ignoreBold.test(text.wrap(Style.BOLD), text));
        assertFalse(ignoreBold.test(texts, text));
        assertFalse(ignoreBold.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreBold.test(text, text.wrap(Style.UNDERLINE)));

        BiPredicate<RichText, RichText> ignoreUnderline = RichText.equalizer(ComparisonSettings.builder().setIgnoreUnderline(true).build());
        assertTrue(ignoreUnderline.test(text, text.wrap(Style.UNDERLINE)));
        assertTrue(ignoreUnderline.test(text.wrap(Style.UNDERLINE), text.wrap(Style.UNDERLINE)));
        assertTrue(ignoreUnderline.test(text.wrap(Style.UNDERLINE), text));
        assertFalse(ignoreUnderline.test(texts, text));
        assertFalse(ignoreUnderline.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreUnderline.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreStrikeThrough = RichText.equalizer(ComparisonSettings.builder().setIgnoreStrikeThrough(true).build());
        assertTrue(ignoreStrikeThrough.test(text, text.wrap(Style.LINE_THROUGH)));
        assertTrue(ignoreStrikeThrough.test(text.wrap(Style.LINE_THROUGH), text.wrap(Style.LINE_THROUGH)));
        assertTrue(ignoreStrikeThrough.test(text.wrap(Style.LINE_THROUGH), text));
        assertFalse(ignoreStrikeThrough.test(texts, text));
        assertFalse(ignoreStrikeThrough.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreStrikeThrough.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreItalic = RichText.equalizer(ComparisonSettings.builder().setIgnoreItalic(true).build());
        assertTrue(ignoreItalic.test(text, text.wrap(Style.ITALIC)));
        assertTrue(ignoreItalic.test(text.wrap(Style.ITALIC), text.wrap(Style.ITALIC)));
        assertTrue(ignoreItalic.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreItalic.test(texts, text));
        assertFalse(ignoreItalic.test(text.wrap(Style.UNDERLINE), text));
        assertFalse(ignoreItalic.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreCase = RichText.equalizer(ComparisonSettings.builder().setIgnoreCase(true).build());
        assertTrue(ignoreCase.test(text, upper));
        assertTrue(ignoreCase.test(upper, text));
        assertFalse(ignoreCase.test(text, texts));
        assertFalse(ignoreCase.test(texts, text));
        assertFalse(ignoreCase.test(text, text.wrap(Style.ITALIC)));
        assertFalse(ignoreCase.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreCase.test(texts, text));
        assertFalse(ignoreCase.test(text.wrap(Style.UNDERLINE), text));
        assertFalse(ignoreCase.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreFontSize = RichText.equalizer(ComparisonSettings.builder().setIgnoreFontSize(true).build());
        assertTrue(ignoreFontSize.test(text, text));
        assertTrue(ignoreFontSize.test(bigger, bigger));
        assertTrue(ignoreFontSize.test(text, bigger));
        assertTrue(ignoreFontSize.test(bigger, text));
        assertTrue(ignoreFontSize.test(smaller, bigger));
        assertTrue(ignoreFontSize.test(bigger, smaller));
        assertFalse(ignoreFontSize.test(text, texts));
        assertFalse(ignoreFontSize.test(texts, text));
        assertFalse(ignoreFontSize.test(text, text.wrap(Style.ITALIC)));
        assertFalse(ignoreFontSize.test(text.wrap(Style.ITALIC), text));
        assertFalse(ignoreFontSize.test(text.wrap(Style.UNDERLINE), text));
        assertFalse(ignoreFontSize.test(text, text.wrap(Style.BOLD)));

        BiPredicate<RichText, RichText> ignoreFontFamily = RichText.equalizer(ComparisonSettings.builder().setIgnoreFontFamily(true).build());
        assertTrue(ignoreFontFamily.test(text, text));
        assertTrue(ignoreFontFamily.test(bigger, bigger));
        assertTrue(ignoreFontFamily.test(serif, serif));
        assertTrue(ignoreFontFamily.test(text, serif));
        assertTrue(ignoreFontFamily.test(sans, text));
        assertTrue(ignoreFontFamily.test(sans, serif));
        assertTrue(ignoreFontFamily.test(serif, sans));
        assertFalse(ignoreFontFamily.test(text, texts));
        assertFalse(ignoreFontFamily.test(texts, text));
        assertFalse(ignoreFontFamily.test(text, text.wrap(Style.ITALIC)));
        assertFalse(ignoreFontFamily.test(sans, sans.wrap(Style.ITALIC)));

        Function<String, String> fontMapper = s -> s.replaceFirst("[A-Z]{6}\\+", "");
        BiPredicate<RichText, RichText> mappedFonts = RichText.equalizer(ComparisonSettings.builder().setFontMapper(fontMapper).build());
        assertFalse(ignoreCase.test(arial, arialSubset));
        assertTrue(mappedFonts.test(text, text));
        assertTrue(mappedFonts.test(arial, arial));
        assertTrue(mappedFonts.test(arialSubset, arialSubset));
        assertTrue(mappedFonts.test(helvetica, helvetica));
        assertTrue(mappedFonts.test(arial, arialSubset));
        assertTrue(mappedFonts.test(arialSubset, arial));
        assertFalse(mappedFonts.test(arial, helvetica));
        assertFalse(mappedFonts.test(helvetica, arial));
        assertFalse(mappedFonts.test(arialSubset, helvetica));
        assertFalse(mappedFonts.test(helvetica, arialSubset));
    }

    @Test
    void testSubSequence() {
        String s = "Hello world! We need a longer text to reach all case labels in runIndex()!";

        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("! ");
        builder.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder.append("We need a ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("longer");
        builder.pop(Style.FONT_WEIGHT);
        builder.append(" text to reach ");
        builder.push(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);
        builder.append("all");
        builder.pop(Style.TEXT_DECORATION_UNDERLINE);
        builder.append(" case labels in runIndex()!");
        builder.pop(Style.FONT_STYLE);

        RichText r = builder.toRichText();

        for (int i = 0; i < s.length() - 1; i++) {
            for (int j = i; j < s.length(); j++) {
                CharSequence expected = s.subSequence(i, j);
                LOG.debug("Subsequence: {}", expected);
                RichText subSequence = r.subSequence(i, j);
                String actual = subSequence.toString();
                // test the current subsequence
                assertEquals(expected, actual);
                // Also test all possible subsequences to make sure all case labels in runIndex() are reached.
                // The case labels correspond to the number of runs and by using all possible subsequences we are
                // sure to hit all case labels.
                for (int h = 0; h < subSequence.length() - 1; h++) {
                    for (int k = h; k < subSequence.length(); k++) {
                        CharSequence expected2 = expected.subSequence(h, k);
                        RichText subSequence2 = subSequence.subSequence(h, k);
                        String actual2 = subSequence2.toString();
                        assertEquals(expected2, actual2);
                    }
                }
            }
        }
    }

    @Test
    void testSubsequenceRegression() {
        Style style1 = Style.create("style1", Map.entry("attr", "1"));
        Style style2 = Style.create("style2", Map.entry("attr", "2"));
        Style style3 = Style.create("style3", Map.entry("attr", "3"));

        RichTextBuilder rtb = new RichTextBuilder();
        rtb.push(style1);
        rtb.append("A Short History of Git");
        rtb.pop(style1);
        rtb.push(style2);
        rtb.append(" ");
        rtb.pop(style2);
        rtb.push(style3);
        rtb.append("\n");
        rtb.pop(style3);
        RichText s = rtb.toRichText();
        assertEquals("A Short History of Git \n", s.toString());

        RichText actual = s.subSequence(19, 22);

        RichText expected = RichText.valueOf("Git").apply(style1);
        assertEquals(expected, actual);
    }

    record TestDataReplace(String input, String regex, String replacement, String replaceAll, String replaceFirst) {
        static TestDataReplace of(String input, String regex, String replacement) {
            return new TestDataReplace(input, regex, replacement, input.replaceAll(regex, replacement), input.replaceFirst(regex, replacement));
        }
    }

    private static Stream<TestDataReplace> provideTestDataReplaceAll() {
        return Stream.of(
                TestDataReplace.of("Hello world\n\nThis     is a\ttest!\r\n", "\\s+", " "),
                TestDataReplace.of("As with many great things in life, Git began with a bit of creative destruction and fiery controversy.", "\\s+", " "),
                TestDataReplace.of("", "a", "b"),
                TestDataReplace.of("Hello, world!", "a", "b"),
                TestDataReplace.of("Hello, world!", "o", "0"),
                TestDataReplace.of("Hello, world!", "[ol]", "*"),
                TestDataReplace.of("Hello, world!", "[ol]", ""),
                TestDataReplace.of("a.b.c", "\\.", "-")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    void testReplaceAll(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceAll());
        RichText actual = RichText.valueOf(testData.input()).replaceAll(testData.regex(), RichText.valueOf(testData.replacement()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    void testReplaceAllStringArgument(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceAll());
        RichText actual = RichText.valueOf(testData.input()).replaceAll(testData.regex(), testData.replacement());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    void testReplaceFirst(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceFirst());
        RichText actual = RichText.valueOf(testData.input()).replaceFirst(testData.regex(), RichText.valueOf(testData.replacement()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    void testReplaceFirstStringArgument(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceFirst());
        RichText actual = RichText.valueOf(testData.input()).replaceFirst(testData.regex(), testData.replacement());
        assertEquals(expected, actual);
    }

    @Test
        // not a parameterized test because it expects an exception
    void testReplaceAll_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceAll("[ol", RichText.valueOf("!")));
    }

    @Test
        // not a parameterized test because it expects an exception
    void testReplaceAllStringArgument_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceAll("[ol", "!"));
    }

    @Test
        // not a parameterized test because it expects an exception
    void testReplaceFirst_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceFirst("[ol", RichText.valueOf("!")));
    }

    @Test
        // not a parameterized test because it expects an exception
    void testReplaceFirstStringArgument_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceFirst("[ol", "!"));
    }

    @Test
    void testLines() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("w\nor\nld");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText rt = builder.toRichText();

        StringBuilder sb = new StringBuilder();
        rt.lines().forEach(s -> sb.append(s).append(";"));
        assertEquals("Hello w;or;ld!;", sb.toString());
    }

    @Test
    void testAttributedChars() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText rt = builder.toRichText();

        // test extracting the characters using attributedCharAt()
        String s = rt.toString();
        assertEquals("Hello world!", s);
        for (int i = 0; i < rt.length(); i++) {
            assertEquals(s.charAt(i), rt.charAt(i));
            assertEquals(s.charAt(i), rt.attributedCharAt(i).character());
        }

        // test the attributed character iterator
        StringBuilder sb = new StringBuilder();
        rt.attributedChars().map(AttributedCharacter::character).forEach(sb::append);
        assertEquals("Hello world!", sb.toString());
    }

    // regression: java.lang.IndexOutOfBoundsException was thrown when iterating over attributed chars of a subSequence
    @Test
    void testAttributedCharsOfSubString() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("I said: Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText text = builder.toRichText();

        RichText rt = text.subSequence(8);

        // test extracting the characters using attributedCharAt()
        String s = rt.toString();
        assertEquals("Hello world!", s);
        for (int i = 0; i < rt.length(); i++) {
            assertEquals(s.charAt(i), rt.charAt(i));
            assertEquals(s.charAt(i), rt.attributedCharAt(i).character());
        }

        // test the attributed character iterator
        StringBuilder sb = new StringBuilder();
        rt.attributedChars().map(AttributedCharacter::character).forEach(sb::append);
        assertEquals("Hello world!", sb.toString());
    }

    // Test that Runs containing same text and attributes but with different offsets to the same base compare equal.
    @Test
    void testRunEquals() {
        RichText txt = RichText.valueOf("1 2 3");
        RichText a = txt.subSequence(1, 2);
        RichText b = txt.subSequence(3, 4);
        assertEquals(" ", a.toString());
        assertEquals(" ", b.toString());
        assertEquals(b, a);
    }

    @Test
    void testJoiner() {
        RichText actual = Stream.of("This", "should", "be", "easy").map(RichText::valueOf).collect(RichText.joiner(" "));
        RichText expected = RichText.valueOf("This should be easy");
        assertEquals(expected, actual);
    }

    record TestCaseSplit(String text, String regex, int limit) {
        @Override
        public String toString() {
            return "\"" + text + "\", \"" + regex + "\", " + limit;
        }
    }

    private static Stream<TestCaseSplit> provideSplitTestCases() {
        return Stream.of(
                // Basic functionality with different limits
                new TestCaseSplit("a,b,c,d", ",", 0),
                new TestCaseSplit("a,b,c,d", ",", 1),
                new TestCaseSplit("a,b,c,d", ",", 2),
                new TestCaseSplit("a,b,c,d", ",", 3),
                new TestCaseSplit("a,b,c,d", ",", 5),
                new TestCaseSplit("a,b,c,d", ",", -1),
                new TestCaseSplit("a,b,c,d", ",", -5),

                // Edge cases
                new TestCaseSplit("", ",", 0),
                new TestCaseSplit("", ",", 1),
                new TestCaseSplit("", ",", -1),
                new TestCaseSplit("abcd", ",", 0),
                new TestCaseSplit("abcd", ",", 1),
                new TestCaseSplit("abcd", ",", -1),

                // Leading delimiters
                new TestCaseSplit(",a,b", ",", 0),
                new TestCaseSplit(",a,b", ",", 1),
                new TestCaseSplit(",a,b", ",", 2),
                new TestCaseSplit(",a,b", ",", 3),
                new TestCaseSplit(",a,b", ",", -1),

                // Trailing delimiters
                new TestCaseSplit("a,b,", ",", 0),
                new TestCaseSplit("a,b,", ",", 1),
                new TestCaseSplit("a,b,", ",", 2),
                new TestCaseSplit("a,b,", ",", 3),
                new TestCaseSplit("a,b,", ",", -1),

                // Consecutive delimiters
                new TestCaseSplit("a,,b", ",", 0),
                new TestCaseSplit("a,,b", ",", 1),
                new TestCaseSplit("a,,b", ",", 2),
                new TestCaseSplit("a,,b", ",", 3),
                new TestCaseSplit("a,,b", ",", -1),

                // Only delimiters
                new TestCaseSplit(",,,", ",", 0),
                new TestCaseSplit(",,,", ",", 1),
                new TestCaseSplit(",,,", ",", 2),
                new TestCaseSplit(",,,", ",", -1),

                // Different regex patterns
                new TestCaseSplit("a.b.c", "\\.", 0),
                new TestCaseSplit("a.b.c", "\\.", 2),
                new TestCaseSplit("a.b.c", "\\.", -1),
                new TestCaseSplit("one two  three", "\\s+", 0),
                new TestCaseSplit("one two  three", "\\s+", 2),
                new TestCaseSplit("one two  three", "\\s+", -1),

                // Complex regex patterns
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", -1),
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", 0),
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", 1),
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", 2),
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", 5),
                new TestCaseSplit("apple, banana,carrot\nlettuce,,,", "[,\\.\\n] *", 10),

                // Multiple consecutive trailing empty segments
                new TestCaseSplit("a,b,,,", ",", 0),
                new TestCaseSplit("a,b,,,", ",", -1),
                new TestCaseSplit("a,b,,,", ",", 3),

                // Mixed empty and non-empty segments
                new TestCaseSplit(",a,,b,", ",", 0),
                new TestCaseSplit(",a,,b,", ",", -1),
                new TestCaseSplit(",a,,b,", ",", 3),

                // Single character strings
                new TestCaseSplit(",", ",", 0),
                new TestCaseSplit(",", ",", 1),
                new TestCaseSplit(",", ",", -1),
                new TestCaseSplit("a", ",", 0),
                new TestCaseSplit("a", ",", 1),
                new TestCaseSplit("a", ",", -1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideSplitTestCases")
    void testSplitUsingCompiledPattern(TestCaseSplit tc) {
        // test using compiled Pattern
        Pattern pattern = Pattern.compile(tc.regex());

        RichText[] expected = Arrays.stream(pattern.split(tc.text(), tc.limit()))
                .map(RichText::valueOf)
                .toArray(RichText[]::new);

        RichText[] actual = RichText.valueOf(tc.text()).split(pattern, tc.limit());

        assertArrayEquals(expected, actual,
                String.format("Failed for input='%s', regex='%s', limit=%d%nexpected: %s%nactual: %s%n",
                        tc.text(), tc.regex(), tc.limit(), Arrays.toString(expected), Arrays.toString(actual)));

        // test without limit parameter
        if (tc.limit() == 0) {
            expected = Arrays.stream(pattern.split(tc.text()))
                    .map(RichText::valueOf)
                    .toArray(RichText[]::new);

            actual = RichText.valueOf(tc.text()).split(pattern);

            assertArrayEquals(expected, actual,
                    String.format("Failed for input='%s', regex='%s'", tc.text(), tc.regex()));
        }
    }

    @ParameterizedTest
    @MethodSource("provideSplitTestCases")
    void testSplitUsingStringPattern(TestCaseSplit tc) {
        RichText[] expected = Arrays.stream(tc.text().split(tc.regex(), tc.limit()))
                .map(RichText::valueOf)
                .toArray(RichText[]::new);

        RichText[] actual = RichText.valueOf(tc.text()).split(tc.regex(), tc.limit());

        assertArrayEquals(expected, actual,
                String.format("Failed for input='%s', regex='%s', limit=%d%nexpected: %s%nactual: %s%n",
                        tc.text(), tc.regex(), tc.limit(), Arrays.toString(expected), Arrays.toString(actual)));

        // test without limit parameter
        if (tc.limit() == 0) {
            expected = Arrays.stream(tc.text().split(tc.regex()))
                    .map(RichText::valueOf)
                    .toArray(RichText[]::new);

            actual = RichText.valueOf(tc.text()).split(tc.regex());

            assertArrayEquals(expected, actual,
                    String.format("Failed for input='%s', regex='%s'", tc.text(), tc.regex()));
        }
    }

    @ParameterizedTest
    @MethodSource("joinArguments")
    void testJoin(String... args) {
        RichText expected = RichText.valueOf(String.join(":", args));
        RichText actual = RichText.join(RichText.valueOf(":"), Arrays.stream(args).map(RichText::valueOf).toArray(RichText[]::new));
        assertEquals(expected, actual);
    }

    static Stream<Arguments> joinArguments() {
        return Stream.of(
                Arguments.of((Object) new String[]{}),
                Arguments.of((Object) new String[]{"1", "2"}),
                Arguments.of((Object) new String[]{"1", "2", "3"})
        );
    }

    private static String[] stringArgsForTrimAndStrip() {
        return new String[]{
                // normal whitespace, i. e. character codes ' ' and lower
                "", "  ", "Hello, world!", "Hello, world! ", "     Hello, world!", "\t ABC\tDEF GHI \n",
                // other whitespace
                "\u00A0hello\u00A0",
                "\u2000hello\u2000",
                "\u2001hello\u2001",
                "\u2002hello\u2002",
                "\u2003hello\u2003",
                "\u2004hello\u2004",
                "\u2005hello\u2005",
                "\u2006hello\u2006",
                "\u2007hello\u2007",
                "\u2008hello\u2008",
                "\u2009hello\u2009",
                "\u200Ahello\u200A",
                "\u200Bhello\u200B",
                "\u202Fhello\u202F",
                "\u205Fhello\u205F",
                "\u3000hello\u3000",
                "\thello\t",
                "\nhello\n",
                "\rhello\r",
                "\u000Bhello\u000B",
                "\u000Chello\u000C"
        };
    }

    @ParameterizedTest
    @MethodSource("stringArgsForTrimAndStrip")
    void testTrim(String input) {
        RichText expected = RichText.valueOf(input.trim());
        RichText actual = RichText.valueOf(input).trim();
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("stringArgsForTrimAndStrip")
    void testStrip(String input) {
        RichText expected = RichText.valueOf(input.strip());
        RichText actual = RichText.valueOf(input).strip();
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("stringArgsForTrimAndStrip")
    void testStripLeading(String input) {
        RichText expected = RichText.valueOf(input.stripLeading());
        RichText actual = RichText.valueOf(input).stripLeading();
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("stringArgsForTrimAndStrip")
    void testStripTrailing(String input) {
        RichText expected = RichText.valueOf(input.stripTrailing());
        RichText actual = RichText.valueOf(input).stripTrailing();
        assertEquals(expected, actual);
    }

    @Test
    void testValueOfWithStyles() {
        Style[] styles = {
                Style.BOLD,
                Style.ITALIC,
                Style.UNDERLINE
        };

        Object obj = new Object() {
            @Override
            public String toString() {
                return "Hello world!";
            }
        };

        // when styles are set in the correct order, equals must return true
        RichText expected = RichText.valueOf("Hello world!").wrap(Style.UNDERLINE).wrap(Style.ITALIC).wrap(Style.BOLD);
        RichText actual = RichText.valueOf(obj, styles);
        assertEquals(expected, actual);
        assertTrue(actual.equalsTextAndFont(expected), "error in textAndFontEquals()");

        // when styles are set in a different order, equals() must return false, but equalsTextAndFont() must return true
        RichText a = RichText.valueOf("Hello world!").wrap(Style.BOLD).wrap(Style.ITALIC).wrap(Style.UNDERLINE);
        RichText b = RichText.valueOf(obj, styles);
        assertNotEquals(a, b, "error in equals()");
        assertTrue(a.equalsTextAndFont(b), "error in textAndFontEquals()");
    }

    @Test
    void testIsBlank() {
        // Test with empty text
        assertTrue(RichText.emptyText().isBlank());

        // Test with whitespace only
        assertTrue(RichText.valueOf("   ").isBlank());
        assertTrue(RichText.valueOf("\t\n\r").isBlank());

        // Test with non-blank text
        assertFalse(RichText.valueOf("Hello").isBlank());
        assertFalse(RichText.valueOf("   Hello   ").isBlank());
    }

    @Test
    void testIndexOfChar() {
        RichText text = RichText.valueOf("Hello world");

        // Test indexOf(int ch)
        assertEquals(0, text.indexOf('H'));
        assertEquals(1, text.indexOf('e'));
        assertEquals(4, text.indexOf('o'));
        assertEquals(-1, text.indexOf('z'));

        // Test indexOf(char ch, int off)
        assertEquals(7, text.indexOf('o', 5));
        assertEquals(-1, text.indexOf('H', 1));
        assertEquals(4, text.indexOf('o', 0));
        assertEquals(4, text.indexOf('o', 4));
        assertEquals(-1, text.indexOf('o', 11));
    }

    @Test
    void testIndexOfCharSequence() {
        RichText text = RichText.valueOf("Hello world, Hello universe");

        // Test indexOf(CharSequence s)
        assertEquals(0, text.indexOf("Hello"));
        assertEquals(6, text.indexOf("world"));
        assertEquals(-1, text.indexOf("goodbye"));

        // Test indexOf(CharSequence s, int fromIndex)
        assertEquals(13, text.indexOf("Hello", 1));
        assertEquals(-1, text.indexOf("world", 8));
        assertEquals(6, text.indexOf("world", 6));
        assertEquals(-1, text.indexOf("Hello", 14));
    }

    @Test
    void testStartsWith() {
        RichText text = RichText.valueOf("Hello world");

        assertTrue(text.startsWith("Hello"));
        assertTrue(text.startsWith("H"));
        assertTrue(text.startsWith(""));
        assertFalse(text.startsWith("hello")); // case sensitive
        assertFalse(text.startsWith("world"));
    }

    @Test
    void testContains() {
        RichText text = RichText.valueOf("Hello world");

        assertTrue(text.contains("Hello"));
        assertTrue(text.contains("world"));
        assertTrue(text.contains("lo wo"));
        assertTrue(text.contains(""));
        assertFalse(text.contains("goodbye"));
        assertFalse(text.contains("World")); // case sensitive
    }

    @Test
    void testApplyStyle() {
        RichText original = RichText.valueOf("Hello");
        RichText styled = original.apply(Style.BOLD);

        // Check that the text is the same
        assertEquals("Hello", styled.toString());

        // Check that the style was applied
        List<Style> styles = styled.stylesAt(0);
        assertTrue(styles.contains(Style.BOLD));

        // Apply another style
        RichText doubleStyled = styled.apply(Style.ITALIC);

        // Check that both styles are applied
        styles = doubleStyled.stylesAt(0);
        assertTrue(styles.contains(Style.BOLD));
        assertTrue(styles.contains(Style.ITALIC));
    }

    @Test
    void testStylesAt() {
        // Create text with different styles
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.pop(Style.BOLD);
        builder.append("!");
        RichText text = builder.toRichText();

        // Check styles at different positions
        List<Style> stylesAtStart = text.stylesAt(0);
        List<Style> stylesInMiddle = text.stylesAt(7);
        List<Style> stylesAtEnd = text.stylesAt(11);

        assertTrue(stylesAtStart.isEmpty() || !stylesAtStart.contains(Style.BOLD));
        assertTrue(stylesInMiddle.contains(Style.BOLD));
        assertTrue(stylesAtEnd.isEmpty() || !stylesAtEnd.contains(Style.BOLD));
    }

    @Test
    void testRunAt() {
        // Create text with different styles
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.pop(Style.BOLD);
        builder.append("!");
        RichText text = builder.toRichText();

        // Check runs at different positions
        Run runAtStart = text.runAt(0);
        Run runInMiddle = text.runAt(7);
        Run runAtEnd = text.runAt(11);

        assertEquals("Hello ", runAtStart.toString());
        assertEquals("world", runInMiddle.toString());
        assertEquals("!", runAtEnd.toString());

        assertTrue(runInMiddle.getStyles().contains(Style.BOLD));
        assertFalse(runAtStart.getStyles().contains(Style.BOLD));
        assertFalse(runAtEnd.getStyles().contains(Style.BOLD));
    }

    @Test
    void testRuns() {
        // Create text with different styles
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.pop(Style.BOLD);
        builder.append("!");
        RichText text = builder.toRichText();

        // Get all runs
        List<Run> runs = text.runs();

        // Check number of runs
        assertEquals(3, runs.size());

        // Check content of runs
        assertEquals("Hello ", runs.get(0).toString());
        assertEquals("world", runs.get(1).toString());
        assertEquals("!", runs.get(2).toString());

        // Check styles of runs
        assertFalse(runs.get(0).getStyles().contains(Style.BOLD));
        assertTrue(runs.get(1).getStyles().contains(Style.BOLD));
        assertFalse(runs.get(2).getStyles().contains(Style.BOLD));
    }

    @Test
    void testRunStream() {
        // Create text with different styles
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.pop(Style.BOLD);
        builder.append("!");
        RichText text = builder.toRichText();

        // Count runs using stream
        long count = text.runStream().count();
        assertEquals(3, count);

        // Check if any run has BOLD style
        boolean hasBold = text.runStream()
                .anyMatch(run -> run.getStyles().contains(Style.BOLD));
        assertTrue(hasBold);

        // Check if all runs have BOLD style
        boolean allBold = text.runStream()
                .allMatch(run -> run.getStyles().contains(Style.BOLD));
        assertFalse(allBold);
    }
}
