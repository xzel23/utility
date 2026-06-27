package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared undo/redo history for text replacement operations.
 */
public final class RichTextEditHistory {
    /**
     * Applies a text replacement with explicit anchor/caret targets.
     */
    @FunctionalInterface
    public interface TextReplaceApplier {
        /**
         * Applies one replacement operation.
         *
         * @param start start offset (inclusive)
         * @param end end offset (exclusive)
         * @param replacement replacement text
         * @param anchorPos anchor position to restore
         * @param caretPos caret position to restore
         */
        void apply(int start, int end, RichText replacement, int anchorPos, int caretPos);
    }

    /**
     * One text replacement history entry.
     *
     * @param start replacement start offset
     * @param removedText removed text
     * @param insertedText inserted text
     * @param beforeAnchor anchor before change
     * @param beforeCaret caret before change
     * @param afterAnchor anchor after change
     * @param afterCaret caret after change
     */
    public record TextReplaceHistoryEntry(
            int start,
            RichText removedText,
            RichText insertedText,
            int beforeAnchor,
            int beforeCaret,
            int afterAnchor,
            int afterCaret
    ) {}

    private final List<TextReplaceHistoryEntry> undoStack = new ArrayList<>();
    private final List<TextReplaceHistoryEntry> redoStack = new ArrayList<>();
    private final int maxHistorySize;

    /**
     * Constructor.
     *
     * @param maxHistorySize maximum undo entries to keep
     */
    public RichTextEditHistory(int maxHistorySize) {
        this.maxHistorySize = Math.max(1, maxHistorySize);
    }

    /**
     * Returns whether undo is possible.
     *
     * @return {@code true} if undo stack is non-empty
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns whether redo is possible.
     *
     * @return {@code true} if redo stack is non-empty
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Clears undo and redo stacks.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * Pushes a new undo entry and clears redo stack.
     *
     * @param entry history entry
     */
    public void push(TextReplaceHistoryEntry entry) {
        undoStack.add(entry);
        while (undoStack.size() > maxHistorySize) {
            undoStack.removeFirst();
        }
        redoStack.clear();
    }

    /**
     * Performs one undo step.
     *
     * @param applier replacement applier
     * @return {@code true} if one step was applied
     */
    public boolean undo(TextReplaceApplier applier) {
        if (undoStack.isEmpty()) {
            return false;
        }

        TextReplaceHistoryEntry entry = undoStack.removeLast();
        applier.apply(
                entry.start(),
                entry.start() + entry.insertedText().length(),
                entry.removedText(),
                entry.beforeAnchor(),
                entry.beforeCaret()
        );
        redoStack.add(entry);
        return true;
    }

    /**
     * Performs one redo step.
     *
     * @param applier replacement applier
     * @return {@code true} if one step was applied
     */
    public boolean redo(TextReplaceApplier applier) {
        if (redoStack.isEmpty()) {
            return false;
        }

        TextReplaceHistoryEntry entry = redoStack.removeLast();
        applier.apply(
                entry.start(),
                entry.start() + entry.removedText().length(),
                entry.insertedText(),
                entry.afterAnchor(),
                entry.afterCaret()
        );
        undoStack.add(entry);
        return true;
    }
}
