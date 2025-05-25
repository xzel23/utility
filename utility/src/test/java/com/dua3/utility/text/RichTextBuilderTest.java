// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RichTextBuilder} unit test.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
class RichTextBuilderTest {

    @Test
    void testDeleteCharAtMiddle() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World");
        builder.deleteCharAt(5);
        RichText rt = builder.toRichText();

        assertEquals(RichText.valueOf("HelloWorld"), rt);
    }

    @Test
    void testDeleteCharAtStart() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello");
        builder.deleteCharAt(0);
        RichText rt = builder.toRichText();

        assertEquals(RichText.valueOf("ello"), rt);
    }

    @Test
    void testDeleteCharAtEnd() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("World!");
        builder.deleteCharAt(builder.length() - 1);
        RichText rt = builder.toRichText();

        assertEquals(RichText.valueOf("World"), rt);
    }

    @Test
    void testDeleteCharAtWithAttributes() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder.append("A");
        builder.pop(Style.FONT_STYLE);
        builder.append("B");
        builder.deleteCharAt(0);
        RichText rt = builder.toRichText();

        assertEquals(RichText.valueOf("B"), rt);
    }

    @Test
    void testDeleteCharAtWithAttributes2() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder.append("A");
        builder.pop(Style.FONT_STYLE);

        builder.push(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);
        builder.append("B");
        builder.pop(Style.TEXT_DECORATION_UNDERLINE);

        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("C");
        builder.pop(Style.FONT_WEIGHT);

        builder.deleteCharAt(1);
        RichText actual = builder.toRichText();

        RichTextBuilder builder2 = new RichTextBuilder();
        builder2.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder2.append("A");
        builder2.pop(Style.FONT_STYLE);

        builder2.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder2.append("C");
        builder2.pop(Style.FONT_WEIGHT);

        RichText expected = builder2.toRichText();

        assertEquals(expected, actual);
    }

    @Test
    void testDeleteCharAtWithAttributes3() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder.append("A");
        builder.pop(Style.FONT_STYLE);

        builder.push(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);
        builder.append("BC");
        builder.pop(Style.TEXT_DECORATION_UNDERLINE);

        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("D");
        builder.pop(Style.FONT_WEIGHT);

        builder.deleteCharAt(1);
        RichText actual = builder.toRichText();

        RichTextBuilder builder2 = new RichTextBuilder();
        builder2.push(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC);
        builder2.append("A");
        builder2.pop(Style.FONT_STYLE);

        builder2.push(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);
        builder2.append("C");
        builder2.pop(Style.TEXT_DECORATION_UNDERLINE);

        builder2.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder2.append("D");
        builder2.pop(Style.FONT_WEIGHT);

        RichText expected = builder2.toRichText();

        assertEquals(expected, actual);
    }

    @Test
    void testWithAttributes() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
        builder.append("!");
        RichText rt = builder.toRichText();

        assertEquals("Hello world!", rt.toString());
        assertEquals("Hello world!", rt.stream().collect(Collectors.joining()));
    }

    @Test
    void testNormalizing() {
        // make sure subsequent runs possessing the same attributes are joined, but runs with differing attributes are retained
        Style style = Style.create("bold", Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style);
        builder.append("Hello ");
        builder.pop(style);
        builder.push(style);
        builder.append("world");
        builder.pop(style);
        builder.append("!");
        RichText rt = builder.toRichText();

        assertEquals("Hello world!", rt.toString());
        assertEquals("Hello world!", rt.stream().collect(Collectors.joining()));
        assertEquals(2, rt.stream().count());
        assertEquals("Hello world", rt.stream().findFirst().get().toString());
    }

    @Test
    void testEmpty() {
        RichTextBuilder builder = new RichTextBuilder();
        RichText rt = builder.toRichText();
        assertEquals(rt, RichText.emptyText());
    }
}
