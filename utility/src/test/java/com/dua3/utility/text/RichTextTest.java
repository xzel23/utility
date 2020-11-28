// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testsubSequence() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_NORMAL);
        builder.append("!");
        RichText rt = builder.toRichText();

        assertEquals("Hello", rt.subSequence(0,5).toString());
        assertEquals("Hello ", rt.subSequence(0,6).toString());
        assertEquals("ello", rt.subSequence(1,5).toString());
        assertEquals("ello ", rt.subSequence(1,6).toString());
        assertEquals("ello w", rt.subSequence(1,7).toString());
        assertEquals("Hello world", rt.subSequence(0,11).toString());
        assertEquals("Hello world!", rt.subSequence(0,12).toString());
        assertEquals("", rt.subSequence(0,0).toString());
        
        RichText sub = rt.subSequence(5,10);
        assertEquals(" worl", sub.toString());
        assertEquals("wo", sub.subSequence(1,3).toString());
    }

    @Test
    public void testReplaceAll() {
        String s = "Hello world\n\nThis     is a\ttest!\r\n";
        assertEquals(s.replaceAll("\\s+", " "), RichText.valueOf(s).replaceAll("\\s+", RichText.valueOf(" ")).toString());    
    }
    
    @Test
    public void testLines() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD);
        builder.append("w\nor\nld");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_NORMAL);
        builder.append("!");
        RichText rt = builder.toRichText();
        
        StringBuilder sb = new StringBuilder();
        rt.lines().forEach(s -> sb.append(s.toString()).append(";"));
        assertEquals("Hello w;or;ld!;", sb.toString());
    }

    @Test
    public void testAttributedChars() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.put(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_NORMAL);
        builder.append("!");
        RichText rt = builder.toRichText();

        // test extracting the characters using attributedCharAt()
        String s = rt.toString();
        assertEquals("Hello world!", s);
        for (int i=0; i<rt.length(); i++) {
            assertEquals(s.charAt(i), rt.charAt(i));
            assertEquals(s.charAt(i), rt.attributedCharAt(i).character());
        }
        
        // test the attributed character iterator
        StringBuilder sb = new StringBuilder();
        rt.attributedChars().map(AttributedCharacter::character).forEach(sb::append);
        assertEquals("Hello world!", sb.toString());
    }
}
