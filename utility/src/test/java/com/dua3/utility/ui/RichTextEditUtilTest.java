package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S5961")
class RichTextEditUtilTest {

    @Test
    void testDetachedRichText() {
        RichText empty = RichTextEditUtil.detachedRichText("");
        assertEquals(0, empty.length());

        RichText text = RichTextEditUtil.detachedRichText("Hello world");
        assertEquals("Hello world", text.toString());
    }

    @Test
    void testDetachedSubSequence() {
        RichText original = RichText.valueOf("Hello world");

        RichText sub = RichTextEditUtil.detachedSubSequence(original, 0, 5);
        assertEquals("Hello", sub.toString());

        RichText inverted = RichTextEditUtil.detachedSubSequence(original, 5, 0);
        assertEquals("Hello", inverted.toString());

        RichText empty = RichTextEditUtil.detachedSubSequence(original, 3, 3);
        assertEquals("", empty.toString());

        RichText clamped = RichTextEditUtil.detachedSubSequence(original, -5, 100);
        assertEquals("Hello world", clamped.toString());
    }

    @Test
    void testAppendPlainText() throws IOException {
        RichText withSplitMarker = RichText.valueOf("abc" + (char) RichText.SPLIT_MARKER + "def");
        StringBuilder sb = new StringBuilder();
        RichTextEditUtil.appendPlainText(withSplitMarker, sb);

        assertEquals("abcdef", sb.toString());
    }

    @Test
    void testFindChangedRange() {
        RichText r1 = RichText.valueOf("Hello world");
        RichText r2 = RichText.valueOf("Hello wonderful world");
        ChangeRange change = RichTextEditUtil.findChangedRange(r1, r2);
        assertEquals(8, change.start());
        assertEquals(8, change.endInCurrent());
        assertEquals(18, change.endInUpdated());

        // Identical texts
        ChangeRange identical = RichTextEditUtil.findChangedRange(r1, r1);
        assertTrue(identical.isEmpty());

        // Replacement
        RichText replaced = RichText.valueOf("Hello earth");
        ChangeRange repRange = RichTextEditUtil.findChangedRange(r1, replaced);
        assertEquals(6, repRange.start());
        assertEquals(11, repRange.endInCurrent());
        assertEquals(11, repRange.endInUpdated());
    }

    @Test
    void testWordNavigation() {
        String text = "hello  world_123 ! test";

        // isWordChar
        assertTrue(RichTextEditUtil.isWordChar('a'));
        assertTrue(RichTextEditUtil.isWordChar('Z'));
        assertTrue(RichTextEditUtil.isWordChar('5'));
        assertTrue(RichTextEditUtil.isWordChar('_'));
        assertFalse(RichTextEditUtil.isWordChar(' '));
        assertFalse(RichTextEditUtil.isWordChar('!'));
        assertFalse(RichTextEditUtil.isWordChar('-'));

        // previousWordStart
        assertEquals(0, RichTextEditUtil.previousWordStart(text, 0));
        assertEquals(0, RichTextEditUtil.previousWordStart(text, 3));
        assertEquals(0, RichTextEditUtil.previousWordStart(text, 6));
        assertEquals(7, RichTextEditUtil.previousWordStart(text, 10));
        assertEquals(7, RichTextEditUtil.previousWordStart(text, 17));
        assertEquals(19, RichTextEditUtil.previousWordStart(text, text.length()));

        // nextWordStart
        assertEquals(7, RichTextEditUtil.nextWordStart(text, 0));
        assertEquals(7, RichTextEditUtil.nextWordStart(text, 3));
        assertEquals(19, RichTextEditUtil.nextWordStart(text, 7));
        assertEquals(19, RichTextEditUtil.nextWordStart(text, 17));
        assertEquals(text.length(), RichTextEditUtil.nextWordStart(text, 19));
        assertEquals(text.length(), RichTextEditUtil.nextWordStart(text, text.length()));

        // nextWordEnd
        assertEquals(5, RichTextEditUtil.nextWordEnd(text, 0));
        assertEquals(16, RichTextEditUtil.nextWordEnd(text, 5));
        assertEquals(16, RichTextEditUtil.nextWordEnd(text, 7));
        assertEquals(23, RichTextEditUtil.nextWordEnd(text, 17));
        assertEquals(text.length(), RichTextEditUtil.nextWordEnd(text, text.length()));

        // wordRangeAt
        RichTextEditUtil.WordRange empty = RichTextEditUtil.wordRangeAt("", 0);
        assertEquals(0, empty.start());
        assertEquals(0, empty.end());

        RichTextEditUtil.WordRange word1 = RichTextEditUtil.wordRangeAt(text, 2);
        assertEquals(0, word1.start());
        assertEquals(5, word1.end());

        RichTextEditUtil.WordRange spaces = RichTextEditUtil.wordRangeAt(text, 5);
        assertEquals(5, spaces.start());
        assertEquals(7, spaces.end());

        RichTextEditUtil.WordRange word2 = RichTextEditUtil.wordRangeAt(text, 10);
        assertEquals(7, word2.start());
        assertEquals(16, word2.end());

        RichTextEditUtil.WordRange endRange = RichTextEditUtil.wordRangeAt(text, text.length());
        assertEquals(19, endRange.start());
        assertEquals(23, endRange.end());
    }
}
