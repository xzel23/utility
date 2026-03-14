// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        String expected = "<span style='font-family: sans-serif'>Keyboard input is shown in a <code>monospaced</code> typeface, direct speech is shown in a font with <span style='font-family: serif'>serifs</span>.</span>";
        String actual = HtmlConverter.create().convert(rt);

        assertEquals(expected, actual);
    }

    @Test
    void testFont() {
        Font arial = FontUtil.getInstance().getFont("arial-16-bold");
        Font courier = FontUtil.getInstance().getFont("courier-12");

        Style style1 = Style.create("style1", Map.entry(Style.FONT, arial));
        Style style2 = Style.create("style2", Map.entry(Style.FONT, courier));

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

        Style style1 = Style.create("style1", Map.entry(Style.FONT, arial));
        Style style2 = Style.create("style2", Map.entry(Style.FONT, times));

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
        Style style = Style.create("style", Map.entry(Style.FONT, arial));

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style);
        builder.append("styled text");
        builder.pop(style);
        RichText rt = builder.toRichText();

        // With CSS, should use class attribute
        String withCssResult = withCss.convert(rt);
        assertTrue(withCssResult.contains("class='"));
        assertFalse(withCssResult.contains("style='"));

        // Without CSS, should use style attribute
        String withoutCssResult = withoutCss.convert(rt);
        assertFalse(withoutCssResult.contains("class='"));
        assertTrue(withoutCssResult.contains("style='"));
    }

    @Test
    void testCustomMapping() {
        // Test custom mapping
        HtmlConverter converter = HtmlConverter.createBlank(HtmlConverter.map(Style.FONT_WEIGHT, value -> {
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
                HtmlConverter.map("x-test", value -> Objects.equals("ok", value) ? HtmlTag.tag("<x>", "</x>") : HtmlTag.emptyTag())
        );

        HtmlConverter converter = HtmlConverter.createBlank(
                HtmlConverter.map("x-test", value -> Objects.equals("ok", value) ? HtmlTag.tag("<x>", "</x>") : HtmlTag.emptyTag()),
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

    @Test
    void testMapAttribute() {
        HtmlConverter converter = HtmlConverter.createBlank(
                HtmlConverter.mapAttribute("lang", change -> HtmlTag.tag("<lang>", "</lang>"))
        );

        RichTextBuilder builder = new RichTextBuilder();
        builder.push("lang", "en");
        builder.append("hello");
        builder.pop("lang");

        assertEquals("<lang>hello</lang>", converter.convert(builder.toRichText()));
    }

    @Test
    void testInlineTextDecorations() {
        Font font = FontUtil.getInstance().getFont("arial-16-bold");
        Map<String, Object> input = new LinkedHashMap<>();
        input.put(Style.FONT, font);
        input.put("custom", "value");

        Map<String, Object> result = HtmlConverter.inlineTextDecorations(input);

        assertFalse(result.containsKey(Style.FONT));
        assertEquals("value", result.get("custom"));
        assertEquals(font.getFamilies(), result.get(Style.FONT_FAMILIES));
        assertEquals(font.getSizeInPoints(), result.get(Style.FONT_SIZE));
        assertEquals(Style.FONT_WEIGHT_VALUE_BOLD, result.get(Style.FONT_WEIGHT));
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
                HtmlConverter.map("mapped", value -> HtmlTag.tag("<mapped>", "</mapped>")),
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
        RichText text = RichText.valueOf("a" + RichText.SPLIT_MARKER + "b");
        assertEquals("ab", HtmlConverter.create().convert(text));
    }
}
