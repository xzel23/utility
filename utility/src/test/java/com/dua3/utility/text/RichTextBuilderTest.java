// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;

import java.util.List;
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
    @Test
    void testComposeDecompose() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello");

        // Compose a new attribute
        builder.compose("color", (name, value) -> Color.RED);
        builder.append(" World");

        // Decompose to revert the attribute
        builder.decompose("color");
        builder.append("!");

        RichText rt = builder.toRichText();
        assertEquals("Hello World!", rt.toString());

        // The middle part should have the color attribute
        assertEquals(3, rt.stream().count());
    }

    @Test
    void testApply() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World");

        Style style = Style.create("test", Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));
        builder.apply(style);

        RichText rt = builder.toRichText();
        assertEquals("Hello World", rt.toString());

        // Check that the style was applied
        Object styleList = rt.stream().findFirst().get().getAttributes().get(RichText.ATTRIBUTE_NAME_STYLE_LIST);
        assertEquals(true, styleList instanceof List);
        assertEquals(1, ((List<?>) styleList).size());
    }

    @Test
    void testEnsureCapacity() {
        RichTextBuilder builder = new RichTextBuilder(5);
        builder.ensureCapacity(20);
        builder.append("This is a test string longer than the initial capacity");

        RichText rt = builder.toRichText();
        assertEquals("This is a test string longer than the initial capacity", rt.toString());
    }

    @Test
    void testCharAt() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World");

        assertEquals('H', builder.charAt(0));
        assertEquals('e', builder.charAt(1));
        assertEquals('l', builder.charAt(2));
        assertEquals('d', builder.charAt(10));
    }

    @Test
    void testSubSequence() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World");

        assertEquals("Hello", builder.subSequence(0, 5).toString());
        assertEquals("World", builder.subSequence(6, 11).toString());
        assertEquals("lo Wo", builder.subSequence(3, 8).toString());
    }

    @Test
    void testAppendRun() {
        RichTextBuilder builder1 = new RichTextBuilder();
        builder1.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder1.append("Bold");
        builder1.pop(Style.FONT_WEIGHT);
        RichText rt1 = builder1.toRichText();

        RichTextBuilder builder2 = new RichTextBuilder();
        builder2.append("Hello ");
        builder2.appendRun(rt1.stream().findFirst().get());
        builder2.append("!");

        RichText rt2 = builder2.toRichText();
        assertEquals("Hello Bold!", rt2.toString());
        assertEquals(3, rt2.stream().count());
    }

    @Test
    void testGetAndGetOrDefault() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);

        // Test get method
        Object fontWeight = builder.get(Style.FONT_WEIGHT);
        assertEquals(Style.FONT_WEIGHT_VALUE_BOLD, fontWeight);

        // Test getOrDefault with existing attribute
        Object fontWeightDefault = builder.getOrDefault(Style.FONT_WEIGHT, "normal");
        assertEquals(Style.FONT_WEIGHT_VALUE_BOLD, fontWeightDefault);

        // Test getOrDefault with non-existing attribute
        Object colorDefault = builder.getOrDefault("color", "black");
        assertEquals("black", colorDefault);
    }
}
