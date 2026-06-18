// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class HtmlConverterTest {

    @Test
    void testEmbeddedStyle() {
        Style bold = Style.create("bold", Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD));

        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(bold);
        builder.append("world");
        builder.pop(bold);
        builder.append("!");

        RichText rt = builder.toRichText();

        String expected = "Hello <b>world</b>!";
        String actual = HtmlConverter.create().convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testTrailingStyle() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.append("!");
        builder.pop(Style.BOLD);

        RichText rt = builder.toRichText();

        String expected = "Hello <b>world!</b>";
        String actual = HtmlConverter.create().convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testSettingFontFamily() {
        Style sans = Style.create("sans", Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_SANS_SERIF));
        Style serif = Style.create("serif", Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_SERIF));
        Style mono = Style.create("mono", Map.entry(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE));

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(sans);
        builder.append("Keyboard input is shown in a ");
        builder.push(mono);
        builder.append("monospaced");
        builder.pop(mono);
        builder.append(" typeface, direct speech is shown in a font with ");
        builder.push(serif);
        builder.append("serifs");
        builder.pop(serif);
        builder.append(".");
        builder.pop(sans);
        RichText rt = builder.toRichText();
        String expected = "<span style='font-family: sans-serif'>Keyboard input is shown in a <span style='font-family: monospace'>monospaced</span> typeface, direct speech is shown in a font with <span style='font-family: serif'>serifs</span>.</span>";
        String actual = HtmlConverter.create().convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testFont() {
        Font arial = FontUtil.getInstance().getFont("arial-16-bold");
        Font courier = FontUtil.getInstance().getFont("courier-12");

        Style style1 = Style.create("style1", arial.toFontDef());
        Style style2 = Style.create("style2", courier.toFontDef());

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style1);
        builder.append("Don't ");
        builder.push(style2);
        builder.append("mix");
        builder.pop(style2);
        builder.append(" too many fonts!");
        builder.pop(style1);
        RichText rt = builder.toRichText();
        String expected = "<span style='font-family: arial; font-size: 16.0pt; font-weight: bold; font-style: normal; color: #000000;'>Don&apos;t <span style='font-family: courier; font-size: 12.0pt; font-weight: normal; font-style: normal; color: #000000;'>mix</span> too many fonts!</span>";
        String actual = HtmlConverter.create().convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testFontCss() {
        Font arial = FontUtil.getInstance().getFont("arial-16-bold");
        Font times = FontUtil.getInstance().getFont("courier-12");

        Style style1 = Style.create("style1", arial.toFontDef());
        Style style2 = Style.create("style2", times.toFontDef());

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style1);
        builder.append("Don't ");
        builder.push(style2);
        builder.append("mix");
        builder.pop(style2);
        builder.append(" too many fonts!");
        builder.pop(style1);
        RichText rt = builder.toRichText();
        String expected = "<span class='arial-bold-normal-none-no_line-16.0-#000000'>Don&apos;t <span class='courier-regular-normal-none-no_line-12.0-#000000'>mix</span> too many fonts!</span>";
        String actual = HtmlConverter.create(HtmlConverter.useCss(true)).convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testCreateBlank() {
        // Test createBlank without options
        HtmlConverter converter = HtmlConverter.createBlank();

        // Create a simple RichText with bold style
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);
        RichText rt = builder.toRichText();

        // Since we're using createBlank, no default mappings should be applied
        // So the bold style should not be converted to <b> tags
        String result = converter.convert(rt);
        assertEquals("bold text", result);
    }

    @Test
    void testAddDefaultMappings() {
        // Test createBlank with addDefaultMappings option
        HtmlConverter converter = HtmlConverter.createBlank(HtmlConverter.addDefaultMappings());

        // Create a simple RichText with bold style
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);
        RichText rt = builder.toRichText();

        // Now the bold style should be converted to <b> tags
        String result = converter.convert(rt);
        assertEquals("<b>bold text</b>", result);
    }

    @Test
    void testUseCss() {
        // Test useCss option
        HtmlConverter withCss = HtmlConverter.create(HtmlConverter.useCss(true));
        HtmlConverter withoutCss = HtmlConverter.create(HtmlConverter.useCss(false));

        // Create a simple RichText with a font
        Font arial = FontUtil.getInstance().getFont("arial-12-bold");
        Style style = Style.create("style", arial.toFontDef());

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style);
        builder.append("styled text");
        builder.pop(style);
        RichText rt = builder.toRichText();

        // Without CSS, should use style attribute
        String withoutCssResult = withoutCss.convert(rt);
        assertFalse(withoutCssResult.contains("class='"));
        assertTrue(withoutCssResult.contains("style='"));

        // With CSS, should use class attribute
        String withCssResult = withCss.convert(rt);
        assertTrue(withCssResult.contains("class='"));
        assertFalse(withCssResult.contains("style='"));
    }

    @Test
    void testCustomMapping() {
        // Test custom mapping
        HtmlConverter converter = HtmlConverter.createBlank(HtmlConverter.mapStyle(Style.FONT_WEIGHT, value -> {
            if (Style.FONT_WEIGHT_VALUE_BOLD.equals(value)) {
                return HtmlTag.tag("<strong>", "</strong>");
            }
            return HtmlTag.emptyTag();
        }));

        // Create a simple RichText with bold style
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);
        RichText rt = builder.toRichText();

        // Bold should be converted to <strong> instead of <b>
        String result = converter.convert(rt);
        assertEquals("<strong>bold text</strong>", result);
    }

    @Test
    void testHeaderStyleMapper() {
        // Create a custom header style mapper
        IntFunction<HtmlConverter.HeaderStyle> headerStyleMapper = level -> new HtmlConverter.HeaderStyle(level, Style.EMPTY, Style.EMPTY);

        // Create converter with custom header style mapper
        HtmlConverter converter = HtmlConverter.create(HtmlConverter.headerStyleMapper(headerStyleMapper));

        // Test that the converter uses our custom header style mapper
        // This is hard to test directly, so we'll just verify the converter was created
        assertFalse(converter.isUseCss());
    }

    @Test
    void testReplaceMapping() {
        // Create a converter with a replaced mapping
        HtmlConverter converter = HtmlConverter.create(HtmlConverter.replaceMapping(Style.FONT_WEIGHT, value -> {
            if (Style.FONT_WEIGHT_VALUE_BOLD.equals(value)) {
                return HtmlTag.tag("<em class=\"bold\">", "</em>");
            }
            return HtmlTag.emptyTag();
        }));

        // Create a simple RichText with bold style
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);
        RichText rt = builder.toRichText();

        // Bold should be converted to <em class="bold"> instead of <b>
        String result = converter.convert(rt);
        assertEquals("<em class=\"bold\">bold text</em>", result);
    }

    @Test
    void testDefaultMapper() {
        // Create a converter with a default mapper
        HtmlConverter converter = HtmlConverter.createBlank(HtmlConverter.defaultMapper((attribute, value) -> {
            if (Style.FONT_WEIGHT.equals(attribute) && Style.FONT_WEIGHT_VALUE_BOLD.equals(value)) {
                return HtmlTag.tag("<custom-bold>", "</custom-bold>");
            }
            return HtmlTag.emptyTag();
        }));

        // Create a simple RichText with bold style
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);
        RichText rt = builder.toRichText();

        // Bold should be converted to <custom-bold> using our default mapper
        String result = converter.convert(rt);
        assertEquals("<custom-bold>bold text</custom-bold>", result);
    }

    @Test
    void testRefineStyleProperties() {
        HtmlConverter noRefineConverter = HtmlConverter.createBlank(
                HtmlConverter.mapStyle("x-test", value -> Objects.equals("ok", value) ? HtmlTag.tag("<x>", "</x>") : HtmlTag.emptyTag())
        );

        HtmlConverter converter = HtmlConverter.createBlank(
                HtmlConverter.mapStyle("x-test", value -> Objects.equals("ok", value) ? HtmlTag.tag("<x>", "</x>") : HtmlTag.emptyTag()),
                HtmlConverter.refineStyleProperties(props -> {
                    Map<String, Object> map = new LinkedHashMap<>(props);
                    map.put("x-test", "ok");
                    return map;
                })
        );

        assertEquals("plain", noRefineConverter.convert(RichText.valueOf("plain")));
        String result = converter.convert(RichText.valueOf("plain"));
        assertNotEquals("plain", result);
        assertTrue(result.contains("<x>"));
        assertTrue(result.contains("</x>"));
        assertTrue(result.contains("plain"));
    }

    @ParameterizedTest
    @MethodSource("mapAttributeArguments")
    void testMapAttribute(HtmlConverter converter, RichText text, String expected) {
        assertEquals(expected, converter.convert(text));
    }

    static Stream<Arguments> mapAttributeArguments() {
        HtmlConverter langConverter = HtmlConverter.createBlank(
                HtmlConverter.mapAttribute("lang", change -> HtmlTag.tag("<lang:" + change.newValue() + ">", "</lang>"))
        );

        HtmlConverter multiConverter = HtmlConverter.createBlank(
                HtmlConverter.mapAttribute("attr1", change -> HtmlTag.tag("<attr1>", "</attr1>")),
                HtmlConverter.mapAttribute("attr2", change -> HtmlTag.tag("<attr2>", "</attr2>"))
        );

        HtmlConverter multiValueConverter = HtmlConverter.createBlank(
                HtmlConverter.mapAttribute("attr1", change -> HtmlTag.tag("<attr1:" + change.newValue() + ">", "</attr1>")),
                HtmlConverter.mapAttribute("attr2", change -> HtmlTag.tag("<attr2:" + change.newValue() + ">", "</attr2>"))
        );

        String interleavedText = "abc" + "cde" + "fgh";
        String interleavedValueText = "abcde";
        String interleavedCloseText = "abc";

        return Stream.of(
                // Basic test
                arguments(
                        langConverter,
                        new RichTextBuilder().push("lang", "en").append("hello").pop("lang").toRichText(),
                        "<lang:en>hello</lang>"
                ),
                // Attributes not starting/ending at text start/end
                arguments(
                        langConverter,
                        new RichTextBuilder().append("prefix ").push("lang", "en").append("hello").pop("lang").append(" suffix").toRichText(),
                        "prefix <lang:en>hello</lang> suffix"
                ),
                // Multiple attributes, interleaved: <attr1>abc<attr2>cde</attr1>fgh</attr2>
                arguments(
                        multiConverter,
                        new RichText(
                                new Run(interleavedText, 0, 3, TextAttributes.of(Map.of("attr1", "v1"))),
                                new Run(interleavedText, 3, 3, TextAttributes.of(Map.of("attr1", "v1", "attr2", "v2"))),
                                new Run(interleavedText, 6, 3, TextAttributes.of(Map.of("attr2", "v2")))
                        ),
                        "<attr1>abc<attr2>cde</attr2></attr1><attr2>fgh</attr2>"
                ),
                // Attribute value changes: lang "en" changing to "de", and back again
                arguments(
                        langConverter,
                        new RichTextBuilder().push("lang", "en").append("english").push("lang", "de").append("deutsch").pop("lang").append("english again").pop("lang").toRichText(),
                        "<lang:en>english</lang><lang:de>deutsch</lang><lang:en>english again</lang>"
                ),
                // Attributes not starting at text start, and value changes
                arguments(
                        langConverter,
                        new RichTextBuilder().append("a").push("lang", "en").append("b").push("lang", "de").append("c").pop("lang").append("d").pop("lang").append("e").toRichText(),
                        "a<lang:en>b</lang><lang:de>c</lang><lang:en>d</lang>e"
                ),
                // Interleaved with value changes
                arguments(
                        multiValueConverter,
                        new RichText(
                                new Run(interleavedValueText, 0, 1, TextAttributes.of(Map.of("attr1", "v1"))),
                                new Run(interleavedValueText, 1, 1, TextAttributes.of(Map.of("attr1", "v1", "attr2", "v2"))),
                                new Run(interleavedValueText, 2, 1, TextAttributes.of(Map.of("attr1", "v3", "attr2", "v2"))),
                                new Run(interleavedValueText, 3, 1, TextAttributes.of(Map.of("attr2", "v2"))),
                                new Run(interleavedValueText, 4, 1, TextAttributes.of(Map.of("attr1", "v1")))
                        ),
                        "<attr1:v1>a<attr2:v2>b</attr2></attr1><attr2:v2><attr1:v3>c</attr1>d</attr2><attr1:v1>e</attr1>"
                ),
                // Interleaved where it closes attr1
                arguments(
                        multiValueConverter,
                        new RichText(
                                new Run(interleavedCloseText, 0, 1, TextAttributes.of(Map.of("attr1", "v1"))),
                                new Run(interleavedCloseText, 1, 1, TextAttributes.of(Map.of("attr1", "v1", "attr2", "v2"))),
                                new Run(interleavedCloseText, 2, 1, TextAttributes.of(Map.of("attr2", "v2")))
                        ),
                        "<attr1:v1>a<attr2:v2>b</attr2></attr1><attr2:v2>c</attr2>"
                )
        );
    }

    @Test
    void testCreateWithCollectionOptions() {
        HtmlConverter converter = HtmlConverter.create(List.of(HtmlConverter.useCss(true)));
        assertTrue(converter.isUseCss());
    }

    @Test
    void testCreateBlankWithCollectionOptions() {
        HtmlConverter converter = HtmlConverter.createBlank(List.of(HtmlConverter.addDefaultMappings()));

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("bold text");
        builder.pop(Style.BOLD);

        assertEquals("<b>bold text</b>", converter.convert(builder.toRichText()));
    }

    @Test
    void testGet() {
        HtmlConverter converter = HtmlConverter.createBlank(
                HtmlConverter.mapStyle("mapped", value -> HtmlTag.tag("<mapped>", "</mapped>")),
                HtmlConverter.defaultMapper((attribute, value) -> HtmlTag.tag("<default>", "</default>"))
        );

        HtmlTag mapped = converter.get("mapped", "x");
        HtmlTag fallback = converter.get("unknown", "x");

        assertEquals("<mapped>", mapped.open());
        assertEquals("</mapped>", mapped.close());
        assertEquals("<default>", fallback.open());
        assertEquals("</default>", fallback.close());
    }

    @Test
    void testGetTagForAttributeChange() {
        HtmlConverter converter = HtmlConverter.createBlank(
                HtmlConverter.mapAttribute("lang", change -> HtmlTag.tag("<lang>", "</lang>"))
        );

        TagBasedConverter.AttributeChange mappedChange = new TagBasedConverter.AttributeChange("lang", null, "en");
        TagBasedConverter.AttributeChange unmappedChange = new TagBasedConverter.AttributeChange("x", null, "y");

        HtmlTag mapped = converter.getTagForAttributeChange(mappedChange);
        HtmlTag unmapped = converter.getTagForAttributeChange(unmappedChange);

        assertEquals("<lang>", mapped.open());
        assertEquals("</lang>", mapped.close());
        assertEquals("", unmapped.open());
        assertEquals("", unmapped.close());
    }

    @Test
    void testConvertIgnoresSplitMarker() {
        RichText text = new RichTextBuilder().append("a").appendSplitMarker().append("b").toRichText();
        assertEquals("ab", HtmlConverter.create().convert(text));
    }

    /**
     * Regression test derived from PDF conversion:
     * font-derived styles should still map to inline tags ({@code <b>, <i>, <u>, <strike>})
     * and line breaks should honor convertLineBreaksTo.
     * <p>
     * This currently fails and documents the desired output for the pending converter fix.
     */
    @Test
    void testPdfDerivedFontStylesShouldUseInlineTags() {
        Style regular = Style.create("regular", FontUtil.getInstance().getFont("Helvetica-12").toFontDef());
        Style bold = Style.create("bold", FontUtil.getInstance().getFont("Helvetica-12-bold").toFontDef());
        Style italic = Style.create("italic", FontUtil.getInstance().getFont("Helvetica-12-italic").toFontDef());
        Style underline = Style.create("underline", FontUtil.getInstance().getFont("Helvetica-12-underline").toFontDef());
        Style strike = Style.create("strike", FontUtil.getInstance().getFont("Helvetica-12-strikethrough").toFontDef());

        String text = "a          b          c          d          e\nX";
        RichText rt = new RichText(
                new Run(text, 0, 1, styleAttributes(regular)),
                new Run(text, 1, 11, styleAttributes(bold)),
                new Run(text, 12, 11, styleAttributes(italic)),
                new Run(text, 23, 11, styleAttributes(underline)),
                new Run(text, 34, 11, styleAttributes(strike)),
                new Run(text, 45, 1, TextAttributes.none()),
                new Run(text, 46, 1, styleAttributes(bold))
        );

        HtmlConverter converter = HtmlConverter.create(
                HtmlConverter.convertLineBreaksTo("<br>\n")
        );

        String expected = """
                a<b>          b</b><i>          c</i><u>          d</u><strike>          e</strike><br>
                <b>X</b>""";

        String actual = converter.convert(rt);
        assertEquals(expected, actual);
    }

    /**
     * Regression test for line break conversion when a split marker directly follows a newline in the same run.
     * <p>
     * This occurs in some transformed RichText pipelines and used to produce raw '\n' instead of mapped '<br>'.
     */
    @Test
    void testConvertLineBreaksToWhenSplitMarkerFollowsNewline() {
        RichText text = new RichTextBuilder()
                .push(Style.BOLD)
                .append("a\n")
                .appendSplitMarker()
                .append("b")
                .pop(Style.BOLD)
                .toRichText();

        String actual = HtmlConverter.create(
                HtmlConverter.convertLineBreaksTo("<br>\n")
        ).convert(text);

        assertEquals("<b>a<br>\nb</b>", actual);
    }

    private static HtmlTag getBlockTag(String value) {
        return switch (value) {
            case "h1" -> HtmlTag.tag("<h1>", "</h1>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_TAG);
            case "h2" -> HtmlTag.tag("<h2>", "</h2>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_TAG);
            case "h3" -> HtmlTag.tag("<h3>", "</h3>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_TAG);
            case "paragraph" -> HtmlTag.tag("<div class=\"paragraph\">", "</div>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_TAG);
            default -> HtmlTag.tag("<div>", "</div>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_AND_AFTER_TAG);
        };
    }

    // Regression test case derived from bug where incorrect HTML was generated generated when converting a PDF document.
    @Test
    void testPdfDerived() {
        // Mappings derived from StructuredDocument
        HtmlConverter converter = HtmlConverter.create(
                HtmlConverter.mapAttribute("block-stack", change -> {
                    String[] newStack = (String[]) change.newValue();
                    if (newStack == null) {
                        return HtmlTag.emptyTag();
                    }

                    String open = Arrays.stream(newStack).map(v -> getBlockTag(v).open()).collect(Collectors.joining());
                    List<String> closeList = Arrays.stream(newStack).map(v -> getBlockTag(v).close()).collect(Collectors.toCollection(ArrayList::new));
                    Collections.reverse(closeList);
                    String close = String.join("", closeList);

                    return HtmlTag.tag(open, close);
                }),
                HtmlConverter.convertLineBreaksTo("")
        );

        Style regular19 = Style.create("regular19",
                Map.entry(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_NORMAL),
                Map.entry(Style.FONT_SIZE, 19.0f));

        RichTextBuilder rtb = new RichTextBuilder();

        // h1
        rtb.push("block-stack", new String[]{"h1"});
        rtb.append("Header 1");
        rtb.pop("block-stack");

        // p1
        rtb.push("block-stack", new String[]{"paragraph"});
        rtb.push(regular19);
        rtb.append("Paragraph 1");
        rtb.pop(regular19);
        rtb.pop("block-stack");
        rtb.appendSplitMarker();

        // h2
        rtb.push("block-stack", new String[]{"h2"});
        rtb.append("Header 2");
        rtb.pop("block-stack");

        RichText rt = rtb.toRichText();
        String result = converter.convert(rt);

        String expected = "<h1>Header 1</h1>" +
                "<div class=\"paragraph\"><span style='font-size: 19.0pt; font-weight: normal;'>Paragraph 1</span></div>" +
                "<h2>Header 2</h2>";

        assertEquals(expected, result);
    }

    private static TextAttributes styleAttributes(Style style) {
        return TextAttributes.of(Map.of(
                RichText.ATTRIBUTE_NAME_STYLE_LIST, List.of(style)
        ));
    }
}
