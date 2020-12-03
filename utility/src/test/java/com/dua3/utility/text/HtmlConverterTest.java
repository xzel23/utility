// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static com.dua3.utility.text.TextAttributes.STYLE_CLASS_DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Axel Howind
 */
public class HtmlConverterTest {

    public HtmlConverterTest() {
    }

    @Test
    public void testEmbeddedStyle() {
        Style bold = Style.create("bold", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));
        
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(bold);
        builder.append("world");
        builder.pop(bold);
        builder.append("!");

        RichText rt = builder.toRichText();

        String expected = "Hello <b>world</b>!";
        String actual = new HtmlConverter().toHtml(rt);
        
        assertEquals(expected, actual);
    }

    @Test
    public void testTrailingStyle() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(TextAttributes.BOLD);
        builder.append("world");
        builder.append("!");
        builder.pop(TextAttributes.BOLD);

        RichText rt = builder.toRichText();

        String expected = "Hello <b>world!</b>";
        String actual = new HtmlConverter().toHtml(rt);
        
        assertEquals(expected, actual);
    }

    @Test
    public void testSettingFontFamily() {
        Style sans = Style.create("sans", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_SANS_SERIF));
        Style serif = Style.create("serif", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_SERIF));
        Style mono = Style.create("mono", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_MONOSPACE));
        
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
        String expected = "<span style=\"font-family: sans-serif\">Keyboard input is shown in a <code>monospaced</code> typeface, direct speech is shown in a font with <span style=\"font-family: serif\">serifs</span>.</span>";
        String actual = new HtmlConverter().toHtml(rt);
        
        assertEquals(expected, actual);
    }

    @Test
    public void testFont() {
        Font arial = new Font("arial-16-bold");
        Font times = new Font("courier-12");
        
        Style style1 = Style.create("style1", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT, arial));
        Style style2 = Style.create("style2", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT, times));
        
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style1);
        builder.append("Don't ");
        builder.push(style2);
        builder.append("mix");
        builder.pop(style2);
        builder.append(" too many fonts!");
        builder.pop(style1);
        RichText rt = builder.toRichText();
        String expected = "<span style=\"color: #000000; font-size: 16.0pt; font-family: arial; font-weight: bold; font-style: normal;\">Don't <span style=\"color: #000000; font-size: 12.0pt; font-family: courier; font-weight: normal; font-style: normal;\">mix</span> too many fonts!</span>";
        String actual = new HtmlConverter().toHtml(rt);
        
        assertEquals(expected, actual);
    }

    @Test
    public void testFontCss() {
        Font arial = new Font("arial-16-bold");
        Font times = new Font("courier-12");
        
        Style style1 = Style.create("style1", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT, arial));
        Style style2 = Style.create("style2", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT, times));
        
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(style1);
        builder.append("Don't ");
        builder.push(style2);
        builder.append("mix");
        builder.pop(style2);
        builder.append(" too many fonts!");
        builder.pop(style1);
        RichText rt = builder.toRichText();
        String expected = "<span class=\"arial-bold-16.0-#000000\">Don't <span class=\"courier-12.0-#000000\">mix</span> too many fonts!</span>";
        String actual = new HtmlConverter(true).toHtml(rt);
        
        assertEquals(expected, actual);
    }

}
