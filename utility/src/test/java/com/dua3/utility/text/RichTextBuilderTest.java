// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Axel Howind
 */
public class RichTextBuilderTest {

    public RichTextBuilderTest() {
    }

    @Test
    public void testWithAttributes() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(TextAttributes.FONT_WEIGHT);
        builder.append("!");
        RichText rt = builder.toRichText();

        assertEquals("Hello world!", rt.toString());
        assertEquals("Hello world!", rt.stream().collect(Collectors.joining()));
    }

    @Test
    public void testNormalizing() {
        // make sure sebsequent runs possessing the same attributes are joind, but runs with differing attributes are retained
        Style style = Style.create("bold", "test", Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));
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