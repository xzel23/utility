package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextEditHistoryTest {

    @Test
    void testInitialStateAndEmptyUndoRedo() {
        RichTextEditHistory history = new RichTextEditHistory(10);
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());

        assertFalse(history.undo((start, end, replacement, anchorPos, caretPos) -> {}));
        assertFalse(history.redo((start, end, replacement, anchorPos, caretPos) -> {}));
    }

    @Test
    void testPushUndoAndRedo() {
        RichTextEditHistory history = new RichTextEditHistory(5);

        RichText removed = RichText.valueOf("old");
        RichText inserted = RichText.valueOf("new text");
        RichTextEditHistory.TextReplaceHistoryEntry entry = new RichTextEditHistory.TextReplaceHistoryEntry(
                2, removed, inserted, 2, 5, 2, 10
        );
        org.junit.jupiter.api.Assertions.assertNotNull(entry.toString());

        history.push(entry);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());

        // Perform undo
        AtomicInteger appliedStart = new AtomicInteger();
        AtomicInteger appliedEnd = new AtomicInteger();
        AtomicReference<RichText> appliedReplacement = new AtomicReference<>();
        AtomicInteger appliedAnchor = new AtomicInteger();
        AtomicInteger appliedCaret = new AtomicInteger();

        boolean undoResult = history.undo((start, end, replacement, anchorPos, caretPos) -> {
            appliedStart.set(start);
            appliedEnd.set(end);
            appliedReplacement.set(replacement);
            appliedAnchor.set(anchorPos);
            appliedCaret.set(caretPos);
        });

        assertTrue(undoResult);
        assertEquals(2, appliedStart.get());
        assertEquals(2 + inserted.length(), appliedEnd.get());
        assertEquals(removed, appliedReplacement.get());
        assertEquals(2, appliedAnchor.get());
        assertEquals(5, appliedCaret.get());

        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        // Perform redo
        boolean redoResult = history.redo((start, end, replacement, anchorPos, caretPos) -> {
            appliedStart.set(start);
            appliedEnd.set(end);
            appliedReplacement.set(replacement);
            appliedAnchor.set(anchorPos);
            appliedCaret.set(caretPos);
        });

        assertTrue(redoResult);
        assertEquals(2, appliedStart.get());
        assertEquals(2 + removed.length(), appliedEnd.get());
        assertEquals(inserted, appliedReplacement.get());
        assertEquals(2, appliedAnchor.get());
        assertEquals(10, appliedCaret.get());

        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void testMaxHistoryEvictionAndClear() {
        RichTextEditHistory history = new RichTextEditHistory(2);

        RichText r1 = RichText.valueOf("1");
        RichText r2 = RichText.valueOf("2");
        RichText r3 = RichText.valueOf("3");

        history.push(new RichTextEditHistory.TextReplaceHistoryEntry(0, r1, r1, 0, 1, 0, 1));
        history.push(new RichTextEditHistory.TextReplaceHistoryEntry(1, r2, r2, 1, 2, 1, 2));
        history.push(new RichTextEditHistory.TextReplaceHistoryEntry(2, r3, r3, 2, 3, 2, 3));

        // Only r2 and r3 should be on stack (capacity 2)
        AtomicReference<RichText> lastUndone = new AtomicReference<>();
        history.undo((s, e, rep, a, c) -> lastUndone.set(rep));
        assertEquals(r3, lastUndone.get());

        history.undo((s, e, rep, a, c) -> lastUndone.set(rep));
        assertEquals(r2, lastUndone.get());

        // Now empty
        assertFalse(history.canUndo());

        // Clear resets both
        history.clear();
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());
    }
}
