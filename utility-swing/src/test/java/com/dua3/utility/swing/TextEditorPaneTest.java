package com.dua3.utility.swing;

import com.dua3.utility.lang.Platform;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
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

    @Test
    void testUpAtFirstLineMovesCaretToLineStart() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("abcd\nefgh"));

        onEdtRun(() -> {
            editor.setCaretPosition(2);
            invokeKeyPressed(editor, KeyEvent.VK_UP);
        });

        assertEquals(0, onEdtGet(editor::getCaretPosition));
    }

    @Test
    void testDownAtLastLineMovesCaretToLineEnd() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("abcd\nefgh"));

        onEdtRun(() -> {
            editor.setCaretPosition(7);
            invokeKeyPressed(editor, KeyEvent.VK_DOWN);
        });

        assertEquals(9, onEdtGet(editor::getCaretPosition));
    }

    @Test
    void testNavigationBindingsMatchPlatformTextAreaConventions() {
        String text = "alpha beta\ngamma";

        TextEditorPane macEditor = onEdtGet(() -> new TextEditorPane(text));
        onEdtRun(() -> {
            macEditor.setCaretPosition(8);
            invokeKeyPressed(macEditor, KeyEvent.VK_HOME, 0, Platform.MACOS);
            assertEquals(0, macEditor.getCaretPosition(), "macOS Home must move to the current visual line start");
            invokeKeyPressed(macEditor, KeyEvent.VK_END, 0, Platform.MACOS);
            assertEquals(10, macEditor.getCaretPosition(), "macOS End must move to the current visual line end");

            macEditor.setCaretPosition(8);
            invokeKeyPressed(macEditor, KeyEvent.VK_LEFT, KeyEvent.META_DOWN_MASK, Platform.MACOS);
            assertEquals(0, macEditor.getCaretPosition(), "Command-Left must move to line start");
            macEditor.setCaretPosition(8);
            invokeKeyPressed(macEditor, KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK, Platform.MACOS);
            assertEquals(10, macEditor.getCaretPosition(), "Option-Right must move to word end");
            macEditor.setCaretPosition(8);
            invokeKeyPressed(macEditor, KeyEvent.VK_DOWN, KeyEvent.META_DOWN_MASK, Platform.MACOS);
            assertEquals(text.length(), macEditor.getCaretPosition(), "Command-Down must move to document end");
        });

        TextEditorPane windowsEditor = onEdtGet(() -> new TextEditorPane(text));
        onEdtRun(() -> {
            windowsEditor.setCaretPosition(6);
            invokeKeyPressed(windowsEditor, KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK, Platform.WINDOWS);
            assertEquals(11, windowsEditor.getCaretPosition(), "Windows Control-Right must move to the next word start");
            invokeKeyPressed(windowsEditor, KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK, Platform.WINDOWS);
            assertEquals(0, windowsEditor.getCaretPosition(), "Windows Control-Home must move to document start");
        });

        TextEditorPane linuxEditor = onEdtGet(() -> new TextEditorPane(text));
        onEdtRun(() -> {
            linuxEditor.setCaretPosition(6);
            invokeKeyPressed(linuxEditor, KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK, Platform.LINUX);
            assertEquals(10, linuxEditor.getCaretPosition(), "Linux Control-Right must move to word end");
            linuxEditor.setCaretPosition(7);
            invokeKeyPressed(linuxEditor, KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK, Platform.LINUX);
            assertEquals(10, linuxEditor.getCaretPosition(), "Linux Control-Down must move to the paragraph end");
        });
    }

    @Test
    void testEnterIsInsertedOnlyOnceWhenPressedAndTypedEventsFire() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("a"));

        onEdtRun(() -> {
            editor.setCaretPosition(editor.getText().length());
            invokeKeyPressed(editor, KeyEvent.VK_ENTER);
            invokeKeyTyped(editor, '\n');
        });

        assertEquals("a\n", onEdtGet(() -> editor.getText().toString()));
    }

    @Test
    void testDoubleClickSelectsWord() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("hello world"));

        onEdtRun(() -> invokeMousePressed(editor, 2, 0, 0));

        assertEquals(0, onEdtGet(editor::getSelectionStart));
        assertEquals(5, onEdtGet(editor::getSelectionEnd));
    }

    @Test
    void testTripleClickSelectsLine() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("hello world\nnext"));

        onEdtRun(() -> invokeMousePressed(editor, 3, 0, 0));

        assertEquals(0, onEdtGet(editor::getSelectionStart));
        assertEquals(11, onEdtGet(editor::getSelectionEnd));
    }

    @Test
    void testPreferredHeightIncludesTrailingEmptyLine() {
        TextEditorPane singleLine = onEdtGet(() -> new TextEditorPane("abc"));
        TextEditorPane trailingEmptyLine = onEdtGet(() -> new TextEditorPane("abc\n"));

        Dimension h1 = onEdtGet(() -> singleLine.getTextComponent().getPreferredSize());
        Dimension h2 = onEdtGet(() -> trailingEmptyLine.getTextComponent().getPreferredSize());

        assertTrue(h2.height > h1.height, "Trailing empty line must increase preferred height");
    }

    @Test
    void testCaretPositionListenerSeesUpdatedTypingStyle() {
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("a");
        builder.push(Style.BOLD).append("b").pop(Style.BOLD);
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane(builder.toRichText()));

        final boolean[] boldAtCaretEvent = {false};
        onEdtRun(() -> editor.addPropertyChangeListener("caretPosition", evt -> boldAtCaretEvent[0] = editor.isBold()));

        onEdtRun(() -> editor.setCaretPosition(1));

        assertTrue(boldAtCaretEvent[0], "Caret listener should observe style at new caret position");

        onEdtRun(() -> editor.setCaretPosition(0));
        assertFalse(onEdtGet(editor::isBold));
    }

    @Test
    void testFontFamilyGetterReflectsSelectionFormatting() {
        TextEditorPane editor = onEdtGet(() -> new TextEditorPane("abcd"));

        onEdtRun(() -> {
            editor.selectRange(1, 3);
            editor.setFontFamily("UnitTestFamily");
        });

        assertEquals("UnitTestFamily", onEdtGet(editor::getFontFamily));
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

    private static void invokeKeyPressed(TextEditorPane editor, int keyCode) {
        KeyEvent event = new KeyEvent(
                editor.getTextComponent(),
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                keyCode,
                KeyEvent.CHAR_UNDEFINED
        );
        invokePrivate(editor, "handleKeyPressed", event);
    }

    private static void invokeKeyPressed(TextEditorPane editor, int keyCode, int modifiers, Platform platform) {
        KeyEvent event = new KeyEvent(
                editor.getTextComponent(),
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                keyCode,
                KeyEvent.CHAR_UNDEFINED
        );
        editor.handleKeyPressed(event, platform);
    }

    private static void invokeKeyTyped(TextEditorPane editor, char ch) {
        KeyEvent event = new KeyEvent(
                editor.getTextComponent(),
                KeyEvent.KEY_TYPED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_UNDEFINED,
                ch
        );
        invokePrivate(editor, "handleKeyTyped", event);
    }

    private static void invokePrivate(TextEditorPane editor, String methodName, KeyEvent event) {
        try {
            Method method = TextEditorPane.class.getDeclaredMethod(methodName, KeyEvent.class);
            method.setAccessible(true);
            method.invoke(editor, event);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void invokeMousePressed(TextEditorPane editor, int clickCount, int x, int y) {
        MouseEvent event = new MouseEvent(
                editor.getTextComponent(),
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                MouseEvent.BUTTON1_DOWN_MASK,
                x,
                y,
                clickCount,
                false,
                MouseEvent.BUTTON1
        );
        try {
            Method method = TextEditorPane.class.getDeclaredMethod("processMousePressed", MouseEvent.class);
            method.setAccessible(true);
            method.invoke(editor, event);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
