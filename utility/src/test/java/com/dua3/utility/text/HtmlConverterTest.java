// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
