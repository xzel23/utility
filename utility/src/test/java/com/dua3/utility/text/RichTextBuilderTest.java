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
 * @author Axel Howind
 */
public class RichTextBuilderTest {

    @Test
    public void testWithAttributes() {
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
    public void testNormalizing() {
        // make sure sebsequent runs possessing the same attributes are joind, but runs with differing attributes are retained
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
    
}
