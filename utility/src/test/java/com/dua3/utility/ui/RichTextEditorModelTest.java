package com.dua3.utility.ui;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextEditorModelTest {

    @Test
    void testConstructorsAndInitialState() {
        RichTextEditorModel m1 = new RichTextEditorModel();
        assertEquals("", m1.getText().toString());
        assertEquals(0, m1.length());
        assertEquals(0, m1.getCaretPosition());
        assertEquals(0, m1.getAnchor());

        RichTextEditorModel m2 = new RichTextEditorModel("Hello");
        assertEquals("Hello", m2.getText().toString());
        assertEquals(5, m2.length());

        RichTextEditorModel m3 = new RichTextEditorModel("Hello", FontUtil.getInstance());
        assertEquals("Hello", m3.getText().toString());

        RichTextEditorModel m4 = new RichTextEditorModel(RichText.valueOf("Hello"), 50);
        assertEquals("Hello", m4.getText().toString());

        RichTextEditorModel m5 = new RichTextEditorModel(RichText.valueOf("Hello"), 50, FontUtil.getInstance());
        assertEquals("Hello", m5.getText().toString());
        assertEquals(FontUtil.getInstance(), m5.getFontUtil());
    }

    @Test
    void testTextMutations() {
        RichTextEditorModel model = new RichTextEditorModel("Hello world");

        // slice
        assertEquals("Hello", model.getText(0, 5).toString());

        // replaceText
        boolean changed = model.replaceText(6, 11, "there");
        assertTrue(changed);
        assertEquals("Hello there", model.getText().toString());

        // insertText
        model.insertText(5, ",");
        assertEquals("Hello, there", model.getText().toString());

        // deleteText
        model.deleteText(5, 6);
        assertEquals("Hello there", model.getText().toString());

        // appendText
        model.appendText("!");
        assertEquals("Hello there!", model.getText().toString());

        // setText
        model.setText("New text");
        assertEquals("New text", model.getText().toString());
        assertEquals(8, model.length());

        model.setText((CharSequence) null);
        assertEquals("", model.getText().toString());
    }

    @Test
    void testSelectionAndCaret() {
        RichTextEditorModel model = new RichTextEditorModel("abcdef");

        model.positionCaret(3);
        assertEquals(3, model.getCaretPosition());
        assertEquals(3, model.getAnchor());
        assertEquals(0, model.getSelection().length());

        model.selectRange(1, 4);
        assertEquals(1, model.getAnchor());
        assertEquals(4, model.getCaretPosition());
        assertEquals(new IndexRange(1, 4), model.getSelection());
        assertEquals("bcd", model.getSelectedText().toString());

        model.deselect();
        assertEquals(0, model.getSelection().length());
        assertEquals(4, model.getCaretPosition());

        model.selectAll();
        assertEquals(0, model.getAnchor());
        assertEquals(6, model.getCaretPosition());
        assertEquals("abcdef", model.getSelectedText().toString());

        // replaceSelection
        model.selectRange(2, 4);
        model.replaceSelection("X");
        assertEquals("abXef", model.getText().toString());
    }

    @Test
    void testDeletePreviousAndNextChar() {
        RichTextEditorModel model = new RichTextEditorModel("abcd");

        model.positionCaret(2);
        model.deletePreviousChar();
        assertEquals("acd", model.getText().toString());
        assertEquals(1, model.getCaretPosition());

        model.deleteNextChar();
        assertEquals("ad", model.getText().toString());
        assertEquals(1, model.getCaretPosition());
    }

    @Test
    void testNavigation() {
        RichTextEditorModel model = new RichTextEditorModel("line 1 word\nline 2 word");

        model.positionCaret(0);

        // forward / backward
        model.forward();
        assertEquals(1, model.getCaretPosition());
        model.backward();
        assertEquals(0, model.getCaretPosition());

        // nextWord / previousWord / endOfNextWord
        model.nextWord();
        assertEquals(5, model.getCaretPosition());
        model.previousWord();
        assertEquals(0, model.getCaretPosition());
        model.endOfNextWord();
        assertEquals(4, model.getCaretPosition());

        // home / end
        model.end();
        assertEquals(model.length(), model.getCaretPosition());
        model.home();
        assertEquals(0, model.getCaretPosition());

        // selection navigation
        model.selectForward();
        assertEquals(0, model.getAnchor());
        assertEquals(1, model.getCaretPosition());

        model.selectNextWord();
        assertEquals(0, model.getAnchor());
        assertEquals(5, model.getCaretPosition());

        model.selectPreviousWord();
        assertEquals(0, model.getAnchor());
        assertEquals(0, model.getCaretPosition());

        model.selectEndOfNextWord();
        assertEquals(0, model.getAnchor());
        assertEquals(4, model.getCaretPosition());

        model.selectEnd();
        assertEquals(0, model.getAnchor());
        assertEquals(model.length(), model.getCaretPosition());

        model.selectHome();
        assertEquals(0, model.getAnchor());
        assertEquals(0, model.getCaretPosition());
    }

    @Test
    void testUndoRedo() {
        RichTextEditorModel model = new RichTextEditorModel("initial");

        model.replaceText(0, 7, "changed");
        assertEquals("changed", model.getText().toString());
        assertTrue(model.canUndo());

        model.undo();
        assertEquals("initial", model.getText().toString());
        assertTrue(model.canRedo());

        model.redo();
        assertEquals("changed", model.getText().toString());

        model.clearHistory();
        assertFalse(model.canUndo());
        assertFalse(model.canRedo());
    }

    @Test
    void testStylingAndAttributes() {
        RichTextEditorModel model = new RichTextEditorModel("sample text");

        model.selectRange(0, 6);
        model.markBold(true);
        assertTrue(model.isSelectionStyled(Style.BOLD));
        assertTrue(model.hasStyleAt(0, Style.BOLD));

        model.markBold(false);
        assertFalse(model.isSelectionStyled(Style.BOLD));

        model.markItalic(true);
        assertTrue(model.isSelectionStyled(Style.ITALIC));
        model.markItalic(false);

        model.markUnderline(true);
        assertTrue(model.isSelectionStyled(Style.UNDERLINE));
        model.markUnderline(false);

        model.markStrikeThrough(true);
        assertTrue(model.isSelectionStyled(Style.LINE_THROUGH));
        model.markStrikeThrough(false);
    }

    @Test
    void testVisualLinesAndProviders() throws IOException {
        RichTextEditorModel model = new RichTextEditorModel("line 1\nline 2\nline 3");

        model.setPageWidthProvider(m -> 500.0);
        model.setPageHeightProvider(m -> 800.0);
        model.setWrapWidthProvider(m -> 400.0);

        assertEquals(500.0, model.getPageWidth());
        assertEquals(800.0, model.getPageHeight());
        assertEquals(400.0, model.currentWrapWidth());
        assertEquals(400.0, model.resolveAvailableWidth(400.0));
        assertEquals(500.0, model.resolveAvailableWidth(0.0));

        Font font = FontUtil.getInstance().getDefaultFont();
        List<VisualLine> lines = model.buildVisualLines(
                400.0,
                true,
                font,
                block -> {
                    Run run = block.runs().getFirst();
                    float w = (float) FontUtil.getInstance().getTextWidth(run, font);
                    float h = (float) FontUtil.getInstance().getTextHeight(run, font);
                    FragmentedText.Fragment f = new FragmentedText.Fragment(0f, 0f, w, h, h, font, run);
                    return new RichTextVisualLayoutHelper.BlockLayout(List.of(List.of(f)), h, pos -> pos);
                }
        );

        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        // snapshots and plain text
        List<RichText> snapshots = model.snapshotLines();
        assertEquals(3, snapshots.size());

        StringBuilder sb = new StringBuilder();
        model.appendPlainTextTo(sb);
        assertEquals("line 1\nline 2\nline 3", sb.toString());

        assertNotNull(model.createLazySnapshot());
    }
}
