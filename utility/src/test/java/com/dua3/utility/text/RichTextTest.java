// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
        RichText arial = text.wrap(Style.create("arial", Map.entry(Style.FONT, new Font("Arial-12"))));
        RichText arialSubset = text.wrap(Style.create("AAAAAA+arial", Map.entry(Style.FONT, new Font("AAAAAA+Arial-12"))));
        RichText helvetica = text.wrap(Style.create("arial", Map.entry(Style.FONT, new Font("Helvetica-12"))));

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

        Function<String,String> fontMapper = s -> s.replaceFirst("[A-Z]{6}\\+", "");
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
    public void testsubSequence() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText rt = builder.toRichText();

        assertEquals("Hello", rt.subSequence(0, 5).toString());
        assertEquals("Hello ", rt.subSequence(0, 6).toString());
        assertEquals("ello", rt.subSequence(1, 5).toString());
        assertEquals("ello ", rt.subSequence(1, 6).toString());
        assertEquals("ello w", rt.subSequence(1, 7).toString());
        assertEquals("Hello world", rt.subSequence(0, 11).toString());
        assertEquals("Hello world!", rt.subSequence(0, 12).toString());
        assertEquals("", rt.subSequence(0, 0).toString());

        RichText sub = rt.subSequence(5, 10);
        assertEquals(" worl", sub.toString());
        assertEquals("wo", sub.subSequence(1, 3).toString());
    }

    @Test
    public void testsingleCharSubSequence() {
        String s = "Hello world!";

        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText r = builder.toRichText();

        for (int i = 0; i < s.length() - 1; i++) {
            assertEquals(s.subSequence(i, i + 1), r.subSequence(i, i + 1).toString());
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

    @Test
    public void testReplaceAll() {
        String s = "Hello world\n\nThis     is a\ttest!\r\n";
        assertEquals(s.replaceAll("\\s+", " "), RichText.valueOf(s).replaceAll("\\s+", RichText.valueOf(" ")).toString());

        String s1 = "As with many great things in life, Git began with a bit of creative destruction and fiery controversy.";
        RichText r1 = RichText.valueOf(s1);

        assertEquals(s1.replaceAll("\\s+", " "), r1.replaceAll("\\s+", RichText.valueOf(" ")).toString());
        assertEquals(RichText.valueOf(s1.replaceAll("\\s+", " ")), r1.replaceAll("\\s+", RichText.valueOf(" ")));

        RichText r2 = r1.subSequence(13, 33);
        String s2 = r2.toString();

        assertEquals(s2.replaceAll("\\s+", " "), r2.replaceAll("\\s+", RichText.valueOf(" ")).toString());
        assertEquals(RichText.valueOf(s2.replaceAll("\\s+", " ")), r2.replaceAll("\\s+", RichText.valueOf(" ")));
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
        rt.lines().forEach(s -> sb.append(s.toString()).append(";"));
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
        assertEquals(RichText.valueOf("apple"), result[0]);
        assertEquals(RichText.valueOf("banana"), result[1]);
        assertEquals(RichText.valueOf("carrot"), result[2]);
    }

}
