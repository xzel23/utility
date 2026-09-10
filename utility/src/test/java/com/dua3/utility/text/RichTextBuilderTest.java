// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.VAnchor;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RichTextBuilder} unit test.
 */
@SuppressWarnings({"OptionalGetWithoutIsPresent", "unchecked"})
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
        assertInstanceOf(List.class, styleList);
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

    @Test
    void testAppendChar() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append('H');
        builder.append('e');
        builder.append('l');
        builder.append('l');
        builder.append('o');

        RichText rt = builder.toRichText();
        assertEquals("Hello", rt.toString());
    }

    @Test
    void testAppendCharSequence() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello");
        builder.append(" ");
        builder.append("World");

        RichText rt = builder.toRichText();
        assertEquals("Hello World", rt.toString());

        // Test with another RichTextBuilder as input
        RichTextBuilder builder2 = new RichTextBuilder();
        builder2.append("!");
        builder.append(builder2);

        rt = builder.toRichText();
        assertEquals("Hello World!", rt.toString());
    }

    @Test
    void testAppendCharSequenceWithRange() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World", 0, 5); // "Hello"
        builder.append(" ");
        builder.append("Hello World", 6, 11); // "World"

        RichText rt = builder.toRichText();
        assertEquals("Hello World", rt.toString());

        // Test with another RichText as input
        RichTextBuilder sourceBuilder = new RichTextBuilder();
        sourceBuilder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        sourceBuilder.append("Bold Text");
        sourceBuilder.pop(Style.FONT_WEIGHT);
        RichText sourceRt = sourceBuilder.toRichText();

        builder.append(" in ");
        builder.append(sourceRt, 0, 4); // "Bold"

        rt = builder.toRichText();
        assertEquals("Hello World in Bold", rt.toString());
    }

    @Test
    void testToString() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello World");

        assertEquals("Hello World", builder.toString());

        // Test with attributes
        builder = new RichTextBuilder();
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("Bold");
        builder.pop(Style.FONT_WEIGHT);
        builder.append(" Normal");

        assertEquals("Bold Normal", builder.toString());
    }

    @Test
    void testAppendTo() {
        RichTextBuilder source = new RichTextBuilder();
        source.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        source.append("Bold");
        source.pop(Style.FONT_WEIGHT);

        RichTextBuilder target = new RichTextBuilder();
        target.append("Normal ");

        source.appendTo(target);

        RichText rt = target.toRichText();
        assertEquals("Normal Bold", rt.toString());
        assertEquals(2, rt.stream().count()); // Should have two runs with different attributes
    }

    @Test
    void testAppendToWithRange() {
        RichTextBuilder source = new RichTextBuilder();
        source.append("ab");
        source.push(Style.BOLD);
        source.append("CD");
        source.pop(Style.BOLD);
        source.push(Style.ITALIC);
        source.append("ef");
        source.pop(Style.ITALIC);

        RichTextBuilder target = new RichTextBuilder();
        target.append(">");
        source.appendTo(target, 1, 5);

        RichText rt = target.toRichText();
        assertEquals(">bCDe", rt.toString());
        assertTrue(rt.stylesAt(1).isEmpty());
        assertTrue(rt.stylesAt(2).contains(Style.BOLD));
        assertTrue(rt.stylesAt(3).contains(Style.BOLD));
        assertTrue(rt.stylesAt(4).contains(Style.ITALIC));
    }

    @Test
    void testAppendToWithRangeFullAndEmpty() {
        RichTextBuilder source = new RichTextBuilder();
        source.append("text");

        RichTextBuilder fullRangeTarget = new RichTextBuilder();
        fullRangeTarget.append("#");
        source.appendTo(fullRangeTarget, 0, source.length());
        assertEquals("#text", fullRangeTarget.toRichText().toString());

        RichTextBuilder emptyRangeTarget = new RichTextBuilder();
        emptyRangeTarget.append("#");
        source.appendTo(emptyRangeTarget, 2, 2);
        assertEquals("#", emptyRangeTarget.toRichText().toString());
    }

    @Test
    @SuppressWarnings("java:S5778") // accepted for test code
    void testAppendToWithRangeErrors() {
        RichTextBuilder source = new RichTextBuilder();
        source.append("abc");

        assertThrows(IndexOutOfBoundsException.class, () -> source.appendTo(new RichTextBuilder(), -1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> source.appendTo(new RichTextBuilder(), 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> source.appendTo(new RichTextBuilder(), 0, 4));
        Throwable throwable = assertThrows(Throwable.class, () -> source.appendTo(null, 0, 1));
        assertTrue(throwable instanceof NullPointerException || throwable instanceof AssertionError);
    }

    @Test
    void testLength() {
        RichTextBuilder builder = new RichTextBuilder();
        assertEquals(0, builder.length());

        builder.append("Hello");
        assertEquals(5, builder.length());

        builder.append(" World");
        assertEquals(11, builder.length());

        builder.deleteCharAt(5); // Delete the space
        assertEquals(10, builder.length());
    }

    // Note: split() is a private method and cannot be directly tested

    // Note: compactParts() is a private method and cannot be directly tested
    // However, we can test the normalization behavior which uses compactParts() internally
    @Test
    void testNormalizationWithSameAttributes() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("Bold");
        builder.pop(Style.FONT_WEIGHT);
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append(" Still Bold");

        // toRichText() calls normalize() which should compact parts with the same attributes
        RichText rt = builder.toRichText();
        assertEquals("Bold Still Bold", rt.toString());
        assertEquals(1, rt.stream().count()); // After normalization, should be just one run
    }

    @Test
    void testPushPopStyle() {
        Style boldStyle = Style.create("bold", Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));
        Style italicStyle = Style.create("italic", Map.entry(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC));

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(boldStyle);
        builder.append("Bold");
        builder.push(italicStyle);
        builder.append(" and Italic");
        builder.pop(italicStyle);
        builder.append(" just Bold");
        builder.pop(boldStyle);

        RichText rt = builder.toRichText();
        assertEquals("Bold and Italic just Bold", rt.toString());
        assertEquals(3, rt.stream().count()); // Should have three runs with different attributes
    }

    @Test
    void testAppendInlineNodeCreatesLazyInlineNodeFactory() {
        TestRichTextBuilder builder = new TestRichTextBuilder();
        AtomicInteger supplierCalls = new AtomicInteger();

        assertSame(builder, builder.appendInlineNode(() -> {
            supplierCalls.incrementAndGet();
            return "node";
        }));

        Style style = inlineStyle(builder);
        Function<String, ?> factory = inlineNodeFactory(style);
        assertEquals(0, supplierCalls.get());

        InlineNode<String> inlineNode = assertInstanceOf(InlineNode.class, factory.apply("ignored"));
        assertEquals("node", inlineNode.getWrapped());
        assertEquals("application/octet-stream", inlineNode.getMimeType());
        assertArrayEquals(new byte[0], inlineNode.getData());
        assertEquals(1, supplierCalls.get());
    }

    @Test
    void testExtensionCapacityConstructor() {
        TestRichTextBuilder builder = new TestRichTextBuilder(1);
        builder.append("text that exceeds the initial capacity");

        assertEquals("text that exceeds the initial capacity", builder.toString());
    }

    @Test
    void testAppendHyperlinkStoresTargetAndLabelInInlinePayload() {
        TestRichTextBuilder builder = new TestRichTextBuilder();
        URI target = URI.create("https://example.test/path?q=1");

        assertSame(builder, builder.appendHyperlink(new StringBuilder("read ✓"), target));

        Style style = inlineStyle(builder);
        Function<String, ?> factory = inlineNodeFactory(style);
        InlineNode<String> inlineNode = assertInstanceOf(InlineNode.class, factory.apply("ignored"));

        assertEquals("hyperlink:read ✓:" + target, inlineNode.getWrapped());
        assertEquals(RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_HYPERLINK, inlineNode.getMimeType());
        assertEquals(new RichTextBuilderExtBase.HyperlinkData(target.toString(), "read ✓"),
                RichTextBuilderExtBase.decodeInlineHyperlinkData(inlineNode.getData()));
    }

    @Test
    void testAppendButtonCreatesButtonAndPreservesAction() {
        TestRichTextBuilder builder = new TestRichTextBuilder();
        AtomicBoolean actionCalled = new AtomicBoolean();

        assertSame(builder, builder.appendButton("Click me", () -> actionCalled.set(true)));

        Style style = inlineStyle(builder);
        Function<String, ?> factory = inlineNodeFactory(style);
        InlineNode<String> inlineNode = assertInstanceOf(InlineNode.class, factory.apply("ignored"));

        assertEquals("button:Click me", inlineNode.getWrapped());
        assertEquals(RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_BUTTON, inlineNode.getMimeType());
        RichTextBuilderExtBase.ButtonData data = RichTextBuilderExtBase.decodeInlineButtonData(inlineNode.getData());
        assertEquals(RichTextBuilderExtBase.createInlineButtonFallbackUri("Click me").toString(), data.target());
        assertEquals("Click me", data.text());

        assertEquals(1, builder.createdActions.size());
        builder.createdActions.getFirst().run();
        assertTrue(actionCalled.get());
    }

    @Test
    void testAppendImageAddsAnchorAndArgbPayload() {
        Image image = testImage();
        TestRichTextBuilder builder = new TestRichTextBuilder();

        assertSame(builder, builder.appendImage(image, VAnchor.TOP));

        Style style = inlineStyle(builder);
        assertEquals(VAnchor.TOP, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR));
        assertEquals(0.0, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT));

        Function<String, ?> factory = inlineNodeFactory(style);
        InlineNode<String> inlineNode = assertInstanceOf(InlineNode.class, factory.apply("ignored"));
        assertEquals("image", inlineNode.getWrapped());
        assertEquals("image/test", inlineNode.getMimeType());
        Image decodedImage = InlineNode.decodeArgbImageData(inlineNode.getData());
        assertEquals(2, decodedImage.width());
        assertEquals(1, decodedImage.height());
        assertArrayEquals(new int[]{0xff102030, 0xff405060}, decodedImage.getArgb());

        TestRichTextBuilder defaultAnchorBuilder = new TestRichTextBuilder();
        defaultAnchorBuilder.appendImage(image);
        assertEquals(VAnchor.BOTTOM, inlineStyle(defaultAnchorBuilder)
                .get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR));
    }

    @Test
    void testAppendScaledImageStoresLimitsAndUsesScaledImageFactory() {
        Image image = testImage();
        TestRichTextBuilder builder = new TestRichTextBuilder();

        builder.appendImage(image, 40.0f, 20.0f, VAnchor.MIDDLE);

        Style style = inlineStyle(builder);
        assertEquals(VAnchor.MIDDLE, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR));
        assertEquals(0.0, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT));
        assertEquals(40.0f, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH));
        assertEquals(20.0f, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT));

        Function<String, ?> factory = inlineNodeFactory(style);
        assertEquals("scaled-image:40.0:20.0",
                assertInstanceOf(InlineNode.class, factory.apply("ignored")).getWrapped());

        TestRichTextBuilder clampingBuilder = new TestRichTextBuilder();
        clampingBuilder.appendImage(image, 0.0f, -2.0f);
        Style clampedStyle = inlineStyle(clampingBuilder);
        assertEquals(1.0f, clampedStyle.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH));
        assertEquals(1.0f, clampedStyle.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT));
        assertEquals(VAnchor.BASELINE, clampedStyle.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR));
    }

    @Test
    void testInlineHyperlinkPayloadSupportsEmptyUtf8AndLegacyData() {
        assertEquals(new RichTextBuilderExtBase.HyperlinkData("target ✓", "label ✓"),
                RichTextBuilderExtBase.decodeInlineHyperlinkData(
                        RichTextBuilderExtBase.encodeInlineHyperlinkData("target ✓", "label ✓")));
        assertEquals(new RichTextBuilderExtBase.HyperlinkData("", ""),
                RichTextBuilderExtBase.decodeInlineHyperlinkData(
                        RichTextBuilderExtBase.encodeInlineHyperlinkData("", "")));
        assertEquals(new RichTextBuilderExtBase.HyperlinkData("legacy", "legacy"),
                RichTextBuilderExtBase.decodeInlineHyperlinkData("legacy".getBytes(StandardCharsets.UTF_8)));

        byte[] malformed = new byte[Integer.BYTES * 2];
        malformed[3] = 1;
        String malformedLegacyValue = new String(malformed, StandardCharsets.UTF_8);
        assertEquals(new RichTextBuilderExtBase.HyperlinkData(malformedLegacyValue, malformedLegacyValue),
                RichTextBuilderExtBase.decodeInlineHyperlinkData(malformed));
    }

    @Test
    void testInlineButtonPayloadUsesFallbackForBlankTargetAndEncodesSpaces() {
        String label = "Save / ✓";
        URI fallback = RichTextBuilderExtBase.createInlineButtonFallbackUri(label);
        assertEquals("dua3button://action?text=Save%20%2F%20%E2%9C%93", fallback.toString());

        assertEquals(new RichTextBuilderExtBase.ButtonData(fallback.toString(), label),
                RichTextBuilderExtBase.decodeInlineButtonData(
                        RichTextBuilderExtBase.encodeInlineButtonData("", label)));
        assertEquals(new RichTextBuilderExtBase.ButtonData("custom:action", label),
                RichTextBuilderExtBase.decodeInlineButtonData(
                        RichTextBuilderExtBase.encodeInlineButtonData("custom:action", label)));
        assertEquals(new RichTextBuilderExtBase.ButtonData(label, label),
                RichTextBuilderExtBase.decodeInlineButtonData(
                        RichTextBuilderExtBase.encodeInlineButtonData(label, label)));
        assertEquals(new RichTextBuilderExtBase.ButtonData(fallback.toString(), label),
                RichTextBuilderExtBase.decodeInlineButtonData(label.getBytes(StandardCharsets.UTF_8)));
    }

    private static Style inlineStyle(RichTextBuilder builder) {
        return builder.toRichText().runs().stream()
                .filter(run -> run.toString().equals(String.valueOf(RichTextBuilderExtBase.INLINE_NODE_MARKER)))
                .findFirst()
                .orElseThrow()
                .getStyles().getFirst();
    }

    @SuppressWarnings("unchecked")
    private static Function<String, ?> inlineNodeFactory(Style style) {
        return (Function<String, ?>) assertInstanceOf(Function.class,
                style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY));
    }

    private static Image testImage() {
        return new Image() {
            @Override
            public int width() {
                return 2;
            }

            @Override
            public int height() {
                return 1;
            }

            @Override
            public int[] getArgb() {
                return new int[]{0xff102030, 0xff405060};
            }

            @Override
            public String mimeType() {
                return "image/test";
            }
        };
    }

    private static final class TestRichTextBuilder extends RichTextBuilderExtBase<String, TestRichTextBuilder> {
        private final List<Runnable> createdActions = new ArrayList<>();

        private TestRichTextBuilder() {
            super();
        }

        private TestRichTextBuilder(int capacity) {
            super(capacity);
        }

        @Override
        protected String createHyperlink(CharSequence text, URI uri) {
            return "hyperlink:" + text + ":" + uri;
        }

        @Override
        protected String createButton(CharSequence text, Runnable action) {
            createdActions.add(action);
            return "button:" + text;
        }

        @Override
        protected String createImage(Image image) {
            return "image";
        }

        @Override
        protected String createImage(Image image, float maxWidth, float maxHeight) {
            return "scaled-image:" + maxWidth + ":" + maxHeight;
        }
    }
}
