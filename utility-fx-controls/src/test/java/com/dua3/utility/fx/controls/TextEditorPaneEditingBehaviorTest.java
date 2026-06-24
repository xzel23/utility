package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditorPaneEditingBehaviorTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testReplaceTextClampsIndicesAndUpdatesCaret() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcdef");
            editor.replaceText(-10, 100, "X");

            assertEquals("X", editor.getText().toString());
            assertEquals(1, editor.getCaretPosition());
            assertEquals(1, editor.getAnchor());
            assertEquals(0, editor.getSelection().getLength());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testReplaceSelectionUsesCurrentSelection() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcdef");
            editor.selectRange(2, 5);
            editor.replaceSelection("X");

            assertEquals("abXf", editor.getText().toString());
            assertEquals(3, editor.getCaretPosition());
            assertEquals(3, editor.getAnchor());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testInsertTextWithFontOverloadInsertsText() throws Exception {
        runOnFxThreadAndWait(() -> {
            Font font = FontUtil.getInstance().getFont("Helvetica-18-bold");
            TextEditorPane editor = new TextEditorPane("ab");
            editor.insertText(1, "X", font);

            assertEquals("aXb", editor.getText().toString());
            assertEquals(2, editor.getCaretPosition());
            assertEquals(2, editor.getAnchor());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testDeletePreviousAndNextCharBehavior() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.positionCaret(2);
            assertTrue(editor.deletePreviousChar());
            assertEquals("acd", editor.getText().toString());
            assertEquals(1, editor.getCaretPosition());

            assertTrue(editor.deleteNextChar());
            assertEquals("ad", editor.getText().toString());
            assertEquals(1, editor.getCaretPosition());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testDeletePreviousCharDeletesSelectionIfPresent() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcdef");
            editor.selectRange(1, 4);
            assertTrue(editor.deletePreviousChar());

            assertEquals("aef", editor.getText().toString());
            assertEquals(1, editor.getCaretPosition());
            assertEquals(1, editor.getAnchor());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testUndoRedoRoundTripAndRedoClearedAfterNewEdit() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");

            editor.appendText("d");
            editor.appendText("e");
            assertEquals("abcde", editor.getText().toString());
            assertTrue(editor.isUndoable());
            assertFalse(editor.isRedoable());

            editor.undo();
            assertEquals("abcd", editor.getText().toString());
            assertTrue(editor.isUndoable());
            assertTrue(editor.isRedoable());

            editor.undo();
            assertEquals("abc", editor.getText().toString());

            editor.redo();
            assertEquals("abcd", editor.getText().toString());
            assertTrue(editor.isRedoable());

            editor.insertText(editor.getLength(), "Z");
            assertEquals("abcdZ", editor.getText().toString());
            assertFalse(editor.isRedoable());

            editor.redo();
            assertEquals("abcdZ", editor.getText().toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testUndoRestoresOriginalSelectionAndRedoRestoresCaret() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcdef");
            editor.selectRange(1, 4);

            editor.replaceSelection("X");
            assertEquals("aXef", editor.getText().toString());
            assertEquals(2, editor.getAnchor());
            assertEquals(2, editor.getCaretPosition());

            editor.undo();
            assertEquals("abcdef", editor.getText().toString());
            assertEquals(1, editor.getAnchor());
            assertEquals(4, editor.getCaretPosition());
            assertEquals(3, editor.getSelection().getLength());

            editor.redo();
            assertEquals("aXef", editor.getText().toString());
            assertEquals(2, editor.getAnchor());
            assertEquals(2, editor.getCaretPosition());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testUndoRedoFormattingChange() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.selectRange(1, 3);
            editor.apply(Style.BOLD);

            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertTrue(editor.getText().stylesAt(2).contains(Style.BOLD));

            editor.undo();
            assertFalse(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertFalse(editor.getText().stylesAt(2).contains(Style.BOLD));

            editor.redo();
            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertTrue(editor.getText().stylesAt(2).contains(Style.BOLD));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testUndoRedoFormattingChangeWithPartiallyFormattedSelection() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");

            editor.selectRange(1, 3);
            editor.apply(Style.BOLD);
            editor.selectRange(0, 4);
            editor.apply(Style.BOLD);

            for (int i = 0; i < editor.getLength(); i++) {
                assertTrue(editor.getText().stylesAt(i).contains(Style.BOLD));
            }

            editor.undo();
            assertFalse(editor.getText().stylesAt(0).contains(Style.BOLD));
            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertTrue(editor.getText().stylesAt(2).contains(Style.BOLD));
            assertFalse(editor.getText().stylesAt(3).contains(Style.BOLD));

            editor.redo();
            for (int i = 0; i < editor.getLength(); i++) {
                assertTrue(editor.getText().stylesAt(i).contains(Style.BOLD));
            }
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testUndoRedoKeepsInsertedRichTextStyle() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("ab");
            RichText boldX = RichText.valueOf("X", Style.BOLD);

            editor.replaceText(1, 1, boldX);
            assertEquals("aXb", editor.getText().toString());
            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));

            editor.undo();
            assertEquals("ab", editor.getText().toString());

            editor.redo();
            assertEquals("aXb", editor.getText().toString());
            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testCopyCutPasteRoundTrip() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("hello world");
            editor.selectRange(0, 5);
            editor.copy();
            assertEquals("hello", FxUtil.getTextFromClipboard().orElseThrow().toString());

            editor.positionCaret(editor.getLength());
            editor.paste();
            assertEquals("hello worldhello", editor.getText().toString());

            editor.selectRange(0, 5);
            editor.cut();
            assertEquals(" worldhello", editor.getText().toString());
            assertEquals("hello", FxUtil.getTextFromClipboard().orElseThrow().toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testCutAndPasteAreNoOpsWhenNotEditable() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");
            editor.selectRange(0, 1);
            editor.copy();

            editor.setEditable(false);
            editor.selectRange(0, 1);
            editor.cut();
            assertEquals("abc", editor.getText().toString());

            editor.positionCaret(editor.getLength());
            editor.paste();
            assertEquals("abc", editor.getText().toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testApplyAndRemoveStyleOnSelection() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.selectRange(1, 3);
            editor.apply(Style.BOLD);

            assertTrue(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertTrue(editor.getText().stylesAt(2).contains(Style.BOLD));
            assertFalse(editor.getText().stylesAt(0).contains(Style.BOLD));
            assertFalse(editor.getText().stylesAt(3).contains(Style.BOLD));

            editor.remove(Style.BOLD);
            assertFalse(editor.getText().stylesAt(1).contains(Style.BOLD));
            assertFalse(editor.getText().stylesAt(2).contains(Style.BOLD));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testMarkFormattingUpdatesExpectedAttributes() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.selectRange(0, 4);

            editor.markBold(true);
            editor.markItalic(true);
            editor.markUnderline(true);
            editor.markStrikeThrough(true);

            assertEquals(Style.FONT_WEIGHT_VALUE_BOLD, editor.getText().attributesAt(1).get(Style.FONT_WEIGHT));
            assertEquals(Style.FONT_STYLE_VALUE_ITALIC, editor.getText().attributesAt(1).get(Style.FONT_STYLE));
            assertEquals(Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE, editor.getText().attributesAt(1).get(Style.TEXT_DECORATION_UNDERLINE));
            assertEquals(Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE, editor.getText().attributesAt(1).get(Style.TEXT_DECORATION_LINE_THROUGH));

            editor.markBold(false);
            editor.markItalic(false);
            editor.markUnderline(false);
            editor.markStrikeThrough(false);

            assertEquals(Style.FONT_WEIGHT_VALUE_NORMAL, editor.getText().attributesAt(1).get(Style.FONT_WEIGHT));
            assertEquals(Style.FONT_STYLE_VALUE_NORMAL, editor.getText().attributesAt(1).get(Style.FONT_STYLE));
            assertEquals(Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE, editor.getText().attributesAt(1).get(Style.TEXT_DECORATION_UNDERLINE));
            assertEquals(Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE, editor.getText().attributesAt(1).get(Style.TEXT_DECORATION_LINE_THROUGH));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testStylePropertiesApplyFormattingToCurrentSelection() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.selectRange(0, 4);
            editor.setTextColor(Color.BLUE);
            editor.setBackgroundColor(Color.YELLOW);
            editor.setFontFamily("Courier New");
            editor.setFontSize(17);

            assertEquals(Color.BLUE, editor.getText().attributesAt(2).get(Style.COLOR));
            assertEquals(Color.YELLOW, editor.getText().attributesAt(2).get(Style.BACKGROUND_COLOR));

            Object families = editor.getText().attributesAt(2).get(Style.FONT_FAMILIES);
            assertNotNull(families);
            assertTrue(families instanceof List<?>);
            assertEquals("Courier New", ((List<?>) families).getFirst());

            Number size = (Number) editor.getText().attributesAt(2).get(Style.FONT_SIZE);
            assertNotNull(size);
            assertEquals(17.0, size.doubleValue(), 0.001);
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testCommitAndCancelEditRoundTrip() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("one");
            editor.replaceText(0, editor.getLength(), "two");
            editor.commitValue();
            assertEquals("two", editor.get().toString());

            editor.replaceText(0, editor.getLength(), "temp");
            assertTrue(editor.isUndoable());

            editor.cancelEdit();
            assertEquals("two", editor.getText().toString());
            assertEquals("two", editor.get().toString());
            assertFalse(editor.isUndoable());
            assertFalse(editor.isRedoable());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testSetCommittedValueAppliesTextAndClearsHistory() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");
            editor.appendText("x");
            assertTrue(editor.isUndoable());

            editor.set(RichText.valueOf("committed"));
            assertEquals("committed", editor.getText().toString());
            assertEquals("committed", editor.get().toString());
            assertFalse(editor.isUndoable());
            assertFalse(editor.isRedoable());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testDocumentObservationApiTracksInternalEdits() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");
            long initialVersion = editor.getDocumentVersion();

            ObjectBinding<RichText> observedDocument = Bindings.createObjectBinding(
                    editor::getDocumentText,
                    editor.documentVersionProperty()
            );
            observedDocument.get();

            AtomicInteger documentBindingChanges = new AtomicInteger();
            AtomicInteger documentVersionChanges = new AtomicInteger();

            observedDocument.addListener((obs, oldVal, newVal) -> documentBindingChanges.incrementAndGet());
            editor.documentVersionProperty().addListener((obs, oldVal, newVal) -> documentVersionChanges.incrementAndGet());

            editor.replaceText(1, 2, "X");

            assertEquals("aXc", observedDocument.get().toString());
            assertEquals("aXc", editor.getDocumentText().toString());
            assertEquals("aXc", editor.getText().toString());
            assertEquals("aXc", editor.textProperty().get().toRichText().toString());
            assertEquals(initialVersion + 1L, editor.getDocumentVersion());
            assertEquals(1, documentBindingChanges.get());
            assertEquals(1, documentVersionChanges.get());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testDocumentObservationApiTracksExternalSetText() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");
            long initialVersion = editor.getDocumentVersion();

            ObjectBinding<RichText> observedDocument = Bindings.createObjectBinding(
                    editor::getDocumentText,
                    editor.documentVersionProperty()
            );
            observedDocument.get();
            AtomicInteger documentBindingChanges = new AtomicInteger();
            observedDocument.addListener((obs, oldVal, newVal) -> documentBindingChanges.incrementAndGet());

            editor.setText("xyz");

            assertEquals("xyz", editor.textProperty().get().toRichText().toString());
            assertEquals("xyz", observedDocument.get().toString());
            assertEquals("xyz", editor.getDocumentText().toString());
            assertEquals("xyz", editor.getText().toString());
            assertEquals(initialVersion + 1L, editor.getDocumentVersion());
            assertEquals(1, documentBindingChanges.get());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testLinesReturnsSnapshotAtInvocationTime() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("one\ntwo");

            List<String> snapshotBeforeEdit = editor.lines().map(RichText::toString).toList();
            editor.replaceText(0, 3, "ONE");
            List<String> afterEdit = editor.lines().map(RichText::toString).toList();

            assertEquals(List.of("one", "two"), snapshotBeforeEdit);
            assertEquals(List.of("ONE", "two"), afterEdit);
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testAppendToWritesPlainTextAcrossLogicalLines() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("ab\n");

            StringBuilder sb = new StringBuilder();
            try {
                editor.appendTo(sb);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            assertEquals("ab\n", sb.toString());

            editor.replaceText(0, 2, "xy");
            StringBuilder sb2 = new StringBuilder();
            try {
                editor.appendTo(sb2);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            assertEquals("xy\n", sb2.toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testWordNavigationMethods() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc  def_1  ghi");

            editor.positionCaret(0);
            editor.nextWord();
            assertEquals(5, editor.getCaretPosition());

            editor.endOfNextWord();
            assertEquals(10, editor.getCaretPosition());

            editor.previousWord();
            assertEquals(5, editor.getCaretPosition());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testSelectionAndCaretHelpers() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcdef");
            editor.selectRange(5, 2);
            assertEquals(5, editor.getAnchor());
            assertEquals(2, editor.getCaretPosition());

            IndexRange range = editor.getSelection();
            assertEquals(2, range.getStart());
            assertEquals(5, range.getEnd());

            editor.home();
            assertEquals(0, editor.getCaretPosition());
            editor.end();
            assertEquals(editor.getLength(), editor.getCaretPosition());

            editor.selectAll();
            assertEquals(0, editor.getSelection().getStart());
            assertEquals(editor.getLength(), editor.getSelection().getEnd());

            editor.deselect();
            assertEquals(0, editor.getSelection().getLength());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testProcessKeyTypedInsertsPrintableAndIgnoresControlChars() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("ab");
            editor.positionCaret(editor.getLength());

            editor.processKeyTyped(keyTyped("\b"));
            editor.processKeyTyped(keyTyped("\u0001"));
            assertEquals("ab", editor.getText().toString());

            KeyEvent printable = keyTyped("X");
            editor.processKeyTyped(printable);
            assertEquals("abX", editor.getText().toString());
            assertTrue(printable.isConsumed());

            KeyEvent enter = keyTyped("\r");
            editor.processKeyTyped(enter);
            assertEquals("abX\n", editor.getText().toString());
            assertTrue(enter.isConsumed());

            editor.setEditable(false);
            editor.processKeyTyped(keyTyped("Y"));
            assertEquals("abX\n", editor.getText().toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testProcessKeyPressedHandlesDeleteAndShortcuts() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.positionCaret(2);

            KeyEvent backspace = keyPressed(KeyCode.BACK_SPACE, false, false);
            editor.processKeyPressed(backspace);
            assertTrue(backspace.isConsumed());
            assertEquals("acd", editor.getText().toString());

            editor.selectAll();
            KeyEvent copy = keyPressed(KeyCode.C, false, true);
            editor.processKeyPressed(copy);
            assertTrue(copy.isConsumed());
            assertEquals("acd", FxUtil.getTextFromClipboard().orElseThrow().toString());

            editor.positionCaret(editor.getLength());
            KeyEvent tab = keyPressed(KeyCode.TAB, false, false);
            editor.processKeyPressed(tab);
            assertTrue(tab.isConsumed());
            assertEquals("acd\t", editor.getText().toString());

            KeyEvent undo = keyPressed(KeyCode.Z, false, true);
            editor.processKeyPressed(undo);
            assertTrue(undo.isConsumed());
            assertEquals("acd", editor.getText().toString());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testAttributeRemovalWithNullIgnored() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abcd");
            editor.selectRange(0, 4);
            editor.setTextColor(Color.GREEN);
            assertEquals(Color.GREEN, editor.getText().attributesAt(1).get(Style.COLOR));

            editor.setTextColor(null);
            assertEquals(Color.GREEN, editor.getText().attributesAt(1).get(Style.COLOR));
        });
    }

    private static KeyEvent keyTyped(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED, false, false, false, false);
    }

    private static KeyEvent keyPressed(KeyCode code, boolean shift, boolean shortcut) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, shortcut, false, shortcut);
    }
}
