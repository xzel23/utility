// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Axel Howind
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "StringBufferWithoutInitialCapacity", "SpellCheckingInspection", "EqualsWithItself"})
public class RichTextTest {

    @Test
    public void testValueOf() {
        String s = "hello world!";
        RichText text = RichText.valueOf(s);

        String expected = s;
        String actual = text.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testEquals() {
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
    public void testEqualsWithHashCollision() {
        RichText a = RichText.valueOf("FB");
        RichText b = RichText.valueOf("Ea");

        Assumptions.assumeTrue(a.hashCode() == b.hashCode(), "hashes do not matxh");
        assertNotEquals(a, b);
    }

    @Test
    public void testisEmpty() {
        assertTrue(RichText.emptyText().isEmpty());
        assertFalse(RichText.valueOf(".").isEmpty());
    }

    @Test
    public void testEqualsText() {
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
    public void testEqualizer() {
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
    public void testSubSequence() {
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
                System.out.println(expected);
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
    public void testSubsequenceRegression() {
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
    public void testReplaceAll(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceAll());
        RichText actual = RichText.valueOf(testData.input()).replaceAll(testData.regex(), RichText.valueOf(testData.replacement()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    public void testReplaceAllStringArgument(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceAll());
        RichText actual = RichText.valueOf(testData.input()).replaceAll(testData.regex(), testData.replacement());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    public void testReplaceFirst(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceFirst());
        RichText actual = RichText.valueOf(testData.input()).replaceFirst(testData.regex(), RichText.valueOf(testData.replacement()));
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("provideTestDataReplaceAll")
    public void testReplaceFirstStringArgument(TestDataReplace testData) {
        RichText expected = RichText.valueOf(testData.replaceFirst());
        RichText actual = RichText.valueOf(testData.input()).replaceFirst(testData.regex(), testData.replacement());
        assertEquals(expected, actual);
    }

    @Test // not a parameterized test because it expects an exception
    public void testReplaceAll_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceAll("[ol", RichText.valueOf("!")));
    }

    @Test // not a parameterized test because it expects an exception
    public void testReplaceAllStringArgument_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceAll("[ol", "!"));
    }

    @Test // not a parameterized test because it expects an exception
    public void testReplaceFirst_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceFirst("[ol", RichText.valueOf("!")));
    }

    @Test // not a parameterized test because it expects an exception
    public void testReplaceFirstStringArgument_MalformedRegex() {
        RichText input = RichText.valueOf("Hello, world!");
        Assertions.assertThrows(PatternSyntaxException.class, () -> input.replaceFirst("[ol", "!"));
    }

    @Test
    public void testLines() {
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
    public void testAttributedChars() {
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
    public void testAttributedCharsOfSubString() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("I said: Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText rt_ = builder.toRichText();

        RichText rt = rt_.subSequence(8);

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
    public void testRunEquals() {
        RichText txt = RichText.valueOf("1 2 3");
        RichText a = txt.subSequence(1, 2);
        RichText b = txt.subSequence(3, 4);
        assertEquals(" ", a.toString());
        assertEquals(" ", b.toString());
        assertEquals(b, a);
    }

    @Test
    public void testJoiner() {
        RichText actual = Stream.of("This", "should", "be", "easy").map(RichText::valueOf).collect(RichText.joiner(" "));
        RichText expected = RichText.valueOf("This should be easy");
        assertEquals(expected, actual);
    }

    @Test
    void testSplitWithoutLimit() {
        RichText txt = RichText.valueOf("apple,banana,carrot");
        RichText[] result = txt.split(",", 0);
        assertEquals(3, result.length);
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana"), result[1]);
        assertEquals(RichText.valueOf("carrot"), result[2]);
    }

    @Test
    void testSplitWithPositiveLimit() {
        RichText txt = RichText.valueOf("apple,banana,carrot");
        RichText[] result = txt.split(",", 2);
        assertEquals(2, result.length);
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana,carrot"), result[1]);
    }

    @Test
    void testSplitWithLimitGreaterThanArraySize() {
        RichText txt = RichText.valueOf("apple,banana,carrot");
        RichText[] result = txt.split(",", 10);
        assertEquals(3, result.length);
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana"), result[1]);
        assertEquals(RichText.valueOf("carrot"), result[2]);
    }

    @Test
    void testSplitWithLimitOne() {
        RichText txt = RichText.valueOf("apple,banana,carrot");
        RichText[] result = txt.split(",", 1);
        assertEquals(1, result.length);
        assertEquals(RichText.valueOf("apple,banana,carrot"), result[0]);
    }

    @Test
    void testSplitWithNegativeLimit() {
        RichText txt = RichText.valueOf("apple,banana,carrot,,,");
        RichText[] result = txt.split(",", -1);
        assertEquals(6, result.length);
    }

    @Test
    void testSplitWithComplexExpression() {
        RichText txt = RichText.valueOf("apple, banana,carrot\nlettuce,,,");
        RichText[] result = txt.split("[,\\.\\n] *");
        assertEquals(4, result.length);
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana"), result[1]);
        assertEquals(RichText.valueOf("carrot"), result[2]);
        assertEquals(RichText.valueOf("lettuce"), result[3]);
    }

    @Test
    void testSplitWithComplexExpressionAndNegativeLimit() {
        RichText txt = RichText.valueOf("apple, banana,carrot\nlettuce,,,");
        RichText[] result = txt.split("[,\\.\\n] *", -1);
        assertEquals(7, result.length);
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana"), result[1]);
        assertEquals(RichText.valueOf("carrot"), result[2]);
        assertEquals(RichText.valueOf("lettuce"), result[3]);
        assertTrue(result[4].isEmpty());
        assertTrue(result[5].isEmpty());
        assertTrue(result[6].isEmpty());
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
}
