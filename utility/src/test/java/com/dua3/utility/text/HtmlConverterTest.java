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
        Style bold = Style.create("bold", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));
        
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(bold);
        builder.append("world");
        builder.append("!");
        builder.pop(bold);

        RichText rt = builder.toRichText();

        String expected = "Hello <b>world!</b>";
        String actual = new HtmlConverter().toHtml(rt);
        
        assertEquals(expected, actual);
    }

}
