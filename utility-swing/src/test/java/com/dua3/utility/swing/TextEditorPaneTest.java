package com.dua3.utility.swing;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditorPaneTest {

    @Test
    void testEditorIsEditableByDefault() {
        TextEditorPane editor = onEdtGet(TextEditorPane::new);
        assertTrue(onEdtGet(() -> editor.getTextComponent().isEditable()));
    }

    @Test
    void testEditsUpdateModelAndUndoRedo() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("ab"));

        onEdtRun(() -> {
            editor.setCaretPosition(2);
            editor.replaceSelection("c");
        });

        assertEquals("abc", onEdtGet(() -> editor.getText().toString()));
        assertTrue(onEdtGet(editor::canUndo));

        onEdtRun(editor::undo);
        assertEquals("ab", onEdtGet(() -> editor.getText().toString()));

        assertTrue(onEdtGet(editor::canRedo));
        onEdtRun(editor::redo);
        assertEquals("abc", onEdtGet(() -> editor.getText().toString()));
    }

    @Test
    void testBoldFormattingOnSelection() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("abcd"));

        onEdtRun(() -> {
            editor.selectRange(1, 3);
            editor.markBold(true);
        });

        AttributeSet attrsBold = onEdtGet(() -> characterAttributes(editor, 1));
        assertTrue(StyleConstants.isBold(attrsBold));
        assertTrue(onEdtGet(() -> isBoldAt(editor.getText(), 1)));

        onEdtRun(() -> {
            editor.selectRange(1, 3);
            editor.markBold(false);
        });

        AttributeSet attrsPlain = onEdtGet(() -> characterAttributes(editor, 1));
        assertFalse(StyleConstants.isBold(attrsPlain));
        assertFalse(onEdtGet(() -> isBoldAt(editor.getText(), 1)));
    }

    @Test
    void testSelectedText() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("one two"));
        onEdtRun(() -> editor.selectRange(4, 7));

        RichText selected = onEdtGet(editor::getSelectedText);
        assertEquals("two", selected.toString());
    }

    @Test
    void testTypingAttributesArePersistedInModel() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("a"));

        onEdtRun(() -> {
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setBold(attributes, true);
            try {
                editor.getTextComponent().getStyledDocument().insertString(1, "b", attributes);
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        });

        RichText textAfterInsert = onEdtGet(editor::getText);
        assertEquals("ab", textAfterInsert.toString());
        assertTrue(isBoldAt(textAfterInsert, 1));
        assertTrue(StyleConstants.isBold(onEdtGet(() -> characterAttributes(editor, 1))));

        onEdtRun(editor::undo);
        assertEquals("a", onEdtGet(() -> editor.getText().toString()));

        onEdtRun(editor::redo);
        RichText redone = onEdtGet(editor::getText);
        assertEquals("ab", redone.toString());
        assertTrue(isBoldAt(redone, 1));
    }

    private static AttributeSet characterAttributes(TextEditorPane editor, int offset) {
        StyledDocument document = editor.getTextComponent().getStyledDocument();
        return document.getCharacterElement(offset).getAttributes();
    }

    private static boolean isBoldAt(RichText text, int index) {
        for (Run run : text) {
            if (run.getStart() <= index && index < run.getEnd()) {
                return run.getFontDef().getBold() == Boolean.TRUE;
            }
        }
        return false;
    }

    private static void onEdtRun(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static <T> T onEdtGet(Supplier<T> action) {
        if (SwingUtilities.isEventDispatchThread()) {
            return action.get();
        }

        final Object[] result = new Object[1];
        onEdtRun(() -> result[0] = action.get());
        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }
}
