package com.dua3.utility.swing;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditorPaneTest {

    @Test
    void testEditorIsEditableByDefault() {
        TextEditorPane editor = onEdtGet(TextEditorPane::new);
        assertTrue(onEdtGet(editor::isEditable));
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

        assertTrue(onEdtGet(() -> isBoldAt(editor.getText(), 1)));

        onEdtRun(() -> {
            editor.selectRange(1, 3);
            editor.markBold(false);
        });

        assertTrue(onEdtGet(() -> !isBoldAt(editor.getText(), 1)));
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
            editor.setCaretPosition(1);
            editor.markBold(true);
            editor.replaceSelection("b");
        });

        RichText textAfterInsert = onEdtGet(editor::getText);
        assertEquals("ab", textAfterInsert.toString());
        assertTrue(isBoldAt(textAfterInsert, 1));

        onEdtRun(editor::undo);
        assertEquals("a", onEdtGet(() -> editor.getText().toString()));

        onEdtRun(editor::redo);
        RichText redone = onEdtGet(editor::getText);
        assertEquals("ab", redone.toString());
        assertTrue(isBoldAt(redone, 1));
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
