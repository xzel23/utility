// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlConverterTest {

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

}
