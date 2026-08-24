package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RichTextEditorModelIndentationTest {

    @Test
    void movesIndentationToNewFirstCharacter() {
        RichTextEditorModel model = indentedModel("text");

        model.replaceText(0, 0, "X");

        assertEquals(40.0f, indentAt(model.getText(), 0));
        assertNull(model.getText().attributesAt(1).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void adjustsCurrentParagraphAndClampsAtZero() {
        RichTextEditorModel model = new RichTextEditorModel(RichText.valueOf("first\nsecond"), 10, com.dua3.utility.text.FontUtil.getInstance());
        model.positionCaret(7);

        model.adjustIndentation(40);
        model.adjustIndentation(-80);

        assertNull(model.getText().attributesAt(0).get(Style.TEXT_INDENT_LEFT));
        assertNull(model.getText().attributesAt(6).get(Style.TEXT_INDENT_LEFT));
        assertNull(model.getText().attributesAt(7).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void adjustsEveryParagraphTouchedBySelection() {
        RichTextEditorModel model = new RichTextEditorModel(RichText.valueOf("first\nsecond"), 10, com.dua3.utility.text.FontUtil.getInstance());
        model.selectRange(1, 9);

        model.adjustIndentation(40);

        assertEquals(40.0f, indentAt(model.getText(), 0));
        assertEquals(40.0f, indentAt(model.getText(), 6));
        assertNull(model.getText().attributesAt(1).get(Style.TEXT_INDENT_LEFT));
        assertNull(model.getText().attributesAt(7).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void splitAtEndKeepsIndentationForNextInput() {
        RichTextEditorModel model = indentedModel("text");
        model.replaceText(4, 4, "\n");
        model.replaceText(5, 5, "X");

        assertEquals(40.0f, indentAt(model.getText(), 5));
    }

    @Test
    void splitInMiddleIndentsBothParagraphsWithoutLeakingIndentation() {
        RichTextEditorModel model = indentedModel("text");

        model.replaceText(2, 2, "\n");

        RichText text = model.getText();
        assertEquals("te\nxt", text.toString());
        assertEquals(40.0f, indentAt(text, 0));
        assertEquals(40.0f, indentAt(text, 3));
        assertNull(text.attributesAt(1).get(Style.TEXT_INDENT_LEFT));
        assertNull(text.attributesAt(4).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void splitBeforeLeadingSpaceIndentsNewParagraph() {
        RichTextEditorModel model = indentedModel("text more");

        model.replaceText(4, 4, "\n");

        RichText text = model.getText();
        assertEquals("text\n more", text.toString());
        assertEquals(40.0f, indentAt(text, 5));
        assertNull(text.attributesAt(6).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void joiningParagraphsUsesFirstParagraphIndentation() {
        RichTextEditorModel model = new RichTextEditorModel(RichText.valueOf("first\nsecond"), 10, com.dua3.utility.text.FontUtil.getInstance());
        model.selectRange(0, 1);
        model.adjustIndentation(40);
        model.selectRange(6, 7);
        model.adjustIndentation(40);

        model.deleteText(5, 6);

        assertEquals(40.0f, indentAt(model.getText(), 0));
        assertNull(model.getText().attributesAt(5).get(Style.TEXT_INDENT_LEFT));
    }

    @Test
    void joiningUsesUnindentedFirstParagraph() {
        RichTextEditorModel model = new RichTextEditorModel(RichText.valueOf("first\nsecond"), 10, com.dua3.utility.text.FontUtil.getInstance());
        model.selectRange(6, 7);
        model.adjustIndentation(40);

        model.deleteText(5, 6);

        assertNull(model.getText().attributesAt(0).get(Style.TEXT_INDENT_LEFT));
        assertNull(model.getText().attributesAt(5).get(Style.TEXT_INDENT_LEFT));
    }

    private static RichTextEditorModel indentedModel(String value) {
        RichTextEditorModel model = new RichTextEditorModel(RichText.valueOf(value), 10, com.dua3.utility.text.FontUtil.getInstance());
        model.selectRange(0, 1);
        model.adjustIndentation(40);
        return model;
    }

    private static Float indentAt(RichText text, int index) {
        return (Float) text.attributesAt(index).get(Style.TEXT_INDENT_LEFT);
    }
}
