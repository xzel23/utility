// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.test.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.TextAttributes;

/**
 * @author Axel Howind
 */
public class RichTextTest {

    public RichTextTest() {
    }

    @Test
    public void testRichTextBuilding() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_NORMAL);
        builder.append("!");

        RichText rt = builder.toRichText();
        assertEquals("Hello world!", rt.toString());
        assertEquals("Hello world!", rt.stream().collect(Collectors.joining()));
    }

}
