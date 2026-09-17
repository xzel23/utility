package com.dua3.utility.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlTagTest {

    @Test
    void testEmptyTag() {
        HtmlTag empty = HtmlTag.emptyTag();
        assertSame(empty, HtmlTag.emptyTag());
        assertEquals("", empty.open());
        assertEquals("", empty.close());
        assertEquals("", empty.toString());
        assertEquals(-1, empty.headerChange());
        assertEquals(HtmlTag.FormattingHint.NO_LINE_BREAK, empty.formattingHint());
        assertEquals("", empty.getTag(HtmlTag.TagType.OPEN_TAG));
        assertEquals("", empty.getTag(HtmlTag.TagType.CLOSE_TAG));
    }

    @Test
    void testSimpleTag() {
        HtmlTag tag = HtmlTag.tag("<b>", "</b>");
        assertEquals("<b>", tag.open());
        assertEquals("</b>", tag.close());
        assertEquals("<b></b>", tag.toString());
        assertEquals(-1, tag.headerChange());
        assertEquals(HtmlTag.FormattingHint.NO_LINE_BREAK, tag.formattingHint());
        assertEquals("<b>", tag.getTag(HtmlTag.TagType.OPEN_TAG));
        assertEquals("</b>", tag.getTag(HtmlTag.TagType.CLOSE_TAG));
    }

    @Test
    void testSimpleTagWithFormattingHint() {
        HtmlTag tag = HtmlTag.tag("<p>", "</p>", HtmlTag.FormattingHint.LINE_BREAK_BEFORE_AND_AFTER_TAG);
        assertEquals("<p>", tag.open());
        assertEquals("</p>", tag.close());
        assertEquals(HtmlTag.FormattingHint.LINE_BREAK_BEFORE_AND_AFTER_TAG, tag.formattingHint());
        assertEquals(-1, tag.headerChange());
    }

    @Test
    void testHeaderTag() {
        HtmlTag h1 = HtmlTag.headerTag("<h1>", "</h1>", 1);
        assertEquals("<h1>", h1.open());
        assertEquals("</h1>", h1.close());
        assertEquals(1, h1.headerChange());
        assertEquals(HtmlTag.FormattingHint.LINE_BREAK_BEFORE_TAG, h1.formattingHint());
        assertEquals("<h1></h1>", h1.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, NO_LINE_BREAK",
            "false, true, LINE_BREAK_AFTER_TAG",
            "true, false, LINE_BREAK_BEFORE_TAG",
            "true, true, LINE_BREAK_BEFORE_AND_AFTER_TAG"
    })
    void testFormattingHintFrom(boolean before, boolean after, HtmlTag.FormattingHint expected) {
        HtmlTag.FormattingHint hint = HtmlTag.FormattingHint.from(before, after);
        assertEquals(expected, hint);
        assertEquals(before, hint.linebreakBeforeTag());
        assertEquals(after, hint.linebreakAfterTag());
    }

    @ParameterizedTest
    @EnumSource(HtmlTag.FormattingHint.class)
    void testFormattingHintProperties(HtmlTag.FormattingHint hint) {
        switch (hint) {
            case NO_LINE_BREAK -> {
                assertFalse(hint.linebreakBeforeTag());
                assertFalse(hint.linebreakAfterTag());
            }
            case LINE_BREAK_AFTER_TAG -> {
                assertFalse(hint.linebreakBeforeTag());
                assertTrue(hint.linebreakAfterTag());
            }
            case LINE_BREAK_BEFORE_TAG -> {
                assertTrue(hint.linebreakBeforeTag());
                assertFalse(hint.linebreakAfterTag());
            }
            case LINE_BREAK_BEFORE_AND_AFTER_TAG -> {
                assertTrue(hint.linebreakBeforeTag());
                assertTrue(hint.linebreakAfterTag());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(HtmlTag.TagType.class)
    void testTagType(HtmlTag.TagType type) {
        HtmlTag tag = HtmlTag.tag("<i>", "</i>");
        if (type == HtmlTag.TagType.OPEN_TAG) {
            assertEquals("<i>", tag.getTag(type));
        } else {
            assertEquals("</i>", tag.getTag(type));
        }
    }

    @Test
    void testCombineTagsEmpty() {
        HtmlTag combined = HtmlTag.combineTags();
        assertSame(HtmlTag.emptyTag(), combined);
    }

    @Test
    void testCombineTagsSingle() {
        HtmlTag tag = HtmlTag.tag("<em>", "</em>");
        HtmlTag combined = HtmlTag.combineTags(tag);
        assertSame(tag, combined);
    }

    @Test
    void testCombineTagsMultiple() {
        HtmlTag bold = HtmlTag.tag("<b>", "</b>");
        HtmlTag italic = HtmlTag.tag("<i>", "</i>");
        HtmlTag underline = HtmlTag.tag("<u>", "</u>");

        HtmlTag combined = HtmlTag.combineTags(bold, italic, underline);
        assertEquals("<b><i><u>", combined.open());
        assertEquals("</u></i></b>", combined.close());
        assertEquals("<b><i><u></u></i></b>", combined.toString());
        assertEquals(0, combined.headerChange());
        assertEquals(HtmlTag.FormattingHint.NO_LINE_BREAK, combined.formattingHint());
    }

    @Test
    void testCombineTagsWithFormattingAndHeaderChange() {
        HtmlTag h2 = HtmlTag.headerTag("<h2>", "</h2>", 2);
        HtmlTag span = HtmlTag.tag("<span>", "</span>", HtmlTag.FormattingHint.LINE_BREAK_AFTER_TAG);

        HtmlTag combined = HtmlTag.combineTags(h2, span);
        assertEquals("<h2><span>", combined.open());
        assertEquals("</span></h2>", combined.close());
        assertEquals(2, combined.headerChange());
        assertEquals(HtmlTag.FormattingHint.LINE_BREAK_BEFORE_AND_AFTER_TAG, combined.formattingHint());
    }

    @Test
    void testCompoundHtmlTagEmptyArray() {
        CompoundHtmlTag compound = new CompoundHtmlTag();
        assertEquals("", compound.open());
        assertEquals("", compound.close());
        assertEquals(0, compound.headerChange());
        assertEquals(HtmlTag.FormattingHint.NO_LINE_BREAK, compound.formattingHint());
        assertEquals("", compound.toString());
    }
}
