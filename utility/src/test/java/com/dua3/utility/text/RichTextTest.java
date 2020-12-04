// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Axel Howind
 */
public class RichTextTest {

    public RichTextTest() {
    }

    @Test
    public void testsubSequence() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
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
    public void testSubsequenceRegression() {
        Style style1 = Style.create("style1", Pair.of("attr", "1"));
        Style style2 = Style.create("style2", Pair.of("attr", "2"));
        Style style3 = Style.create("style3", Pair.of("attr", "3"));
        
        RichTextBuilder rtb = new RichTextBuilder();
        rtb.push(style1);
        rtb.append("A Short History of Git");
        rtb.pop(style1);
        rtb.push(style2);
        rtb.append(" ");
        rtb.pop(style2);
        rtb.push(style3);
        rtb.append("\n");
        rtb.pop(style3);
        RichText s = rtb.toRichText();
        assertEquals("A Short History of Git \n", s.toString());

        RichText actual = s.subSequence(19, 22);
        
        RichText expected = RichText.valueOf("Git").apply(style1);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testReplaceAll() {
        String s = "Hello world\n\nThis     is a\ttest!\r\n";
        assertEquals(s.replaceAll("\\s+", " "), RichText.valueOf(s).replaceAll("\\s+", RichText.valueOf(" ")).toString());

        String s1 = "As with many great things in life, Git began with a bit of creative destruction and fiery controversy.";
        RichText r1 = RichText.valueOf(s1);

        assertEquals(s1.replaceAll("\\s+", " "), r1.replaceAll("\\s+", RichText.valueOf(" ")).toString());
        assertEquals(RichText.valueOf(s1.replaceAll("\\s+", " ")), r1.replaceAll("\\s+", RichText.valueOf(" ")));

        RichText r2 = r1.subSequence(13, 33);
        String s2 = r2.toString();

        assertEquals(s2.replaceAll("\\s+", " "), r2.replaceAll("\\s+", RichText.valueOf(" ")).toString());
        assertEquals(RichText.valueOf(s2.replaceAll("\\s+", " ")), r2.replaceAll("\\s+", RichText.valueOf(" ")));
    }
    
    @Test
    public void testLines() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("w\nor\nld");
        builder.pop(Style.FONT_WEIGHT);
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
        builder.push(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);
        builder.append("world");
        builder.pop(Style.FONT_WEIGHT);
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

    // Test that Runs containing same text and attributes but with different offsets to the same base compare ewuals.
    //
    @Test
    public void testRunEquals() {
        RichText txt= RichText.valueOf("1 2 3");
        RichText a = txt.subSequence(1,2);
        RichText b = txt.subSequence(3,4);
        assertEquals(" ", a.toString());
        assertEquals(" ", b.toString());
        assertTrue(a.equals(b));
    }
    
    @Test
    public void tesstJoiner() {
        RichText actual = Stream.of("This","should","be","easy").map(RichText::valueOf).collect(RichText.joiner(" "));
        RichText expected = RichText.valueOf("This should be easy");
        assertEquals(expected, actual);
    }
}
