package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Toolkit-agnostic rich-text editor model.
 *
 * <p>Tracks document text, selection/caret and undo/redo history.
 */
public class RichTextEditorModel {

    private static final int DEFAULT_MAX_HISTORY_SIZE = 256;

    private RichText text;
    private int anchor;
    private int caret;
    private final List<HistoryEntry> undoStack = new ArrayList<>();
    private final List<HistoryEntry> redoStack = new ArrayList<>();
    private final int maxHistorySize;

    /**
     * Creates an empty editor model.
     */
    public RichTextEditorModel() {
        this(RichText.emptyText());
    }

    /**
     * Creates an editor model with initial text.
     *
     * @param text initial text
     */
    public RichTextEditorModel(@Nullable CharSequence text) {
        this(text == null ? RichText.emptyText() : RichText.valueOf(text), DEFAULT_MAX_HISTORY_SIZE);
    }

    /**
     * Creates an editor model with initial text and history size.
     *
     * @param text initial text
     * @param maxHistorySize maximum number of history entries
     */
    public RichTextEditorModel(RichText text, int maxHistorySize) {
        this.text = detach(text);
        this.maxHistorySize = Math.max(1, maxHistorySize);
        this.anchor = 0;
        this.caret = 0;
    }

    /**
     * Returns current document text.
     *
     * @return document text
     */
    public RichText getText() {
        return text;
    }

    /**
     * Replaces full document text and clears history.
     *
     * @param value new text
     */
    public void setText(@Nullable CharSequence value) {
        setText(value == null ? RichText.emptyText() : RichText.valueOf(value));
    }

    /**
     * Replaces full document text and clears history.
     *
     * @param value new text
     */
    public void setText(RichText value) {
        text = detach(value);
        clearHistory();
        selectRange(0, 0);
    }

    /**
     * Replaces full document text, optionally preserving history.
     *
     * @param value new text
     * @param preserveHistory true to preserve history stacks
     */
    public void setText(RichText value, boolean preserveHistory) {
        text = detach(value);
        if (!preserveHistory) {
            clearHistory();
        }
        selectRange(anchor, caret);
    }

    /**
     * Returns document length.
     *
     * @return length
     */
    public int length() {
        return text.length();
    }

    /**
     * Returns anchor position.
     *
     * @return anchor
     */
    public int getAnchor() {
        return anchor;
    }

    /**
     * Returns caret position.
     *
     * @return caret
     */
    public int getCaretPosition() {
        return caret;
    }

    /**
     * Sets anchor and caret positions.
     *
     * @param anchorPos anchor position
     * @param caretPos caret position
     */
    public void selectRange(int anchorPos, int caretPos) {
        int max = text.length();
        anchor = Math.clamp(anchorPos, 0, max);
        caret = Math.clamp(caretPos, 0, max);
    }

    /**
     * Returns current selection range.
     *
     * @return selection range
     */
    public SelectionRange getSelection() {
        return new SelectionRange(Math.min(anchor, caret), Math.max(anchor, caret));
    }

    /**
     * Returns selected text.
     *
     * @return selected text
     */
    public RichText getSelectedText() {
        SelectionRange selection = getSelection();
        if (selection.length() == 0) {
            return RichText.emptyText();
        }
        return detach(text.subSequence(selection.start(), selection.end()));
    }

    /**
     * Replaces text in a given range and records the change for undo/redo.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceText(int start, int end, @Nullable CharSequence replacement) {
        RichText inserted = detach(replacement == null ? RichText.emptyText() : RichText.valueOf(replacement));
        return replaceTextInternal(start, end, inserted, true);
    }

    /**
     * Replaces text in a given range and records the change for undo/redo.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceText(int start, int end, RichText replacement) {
        return replaceTextInternal(start, end, detach(replacement), true);
    }

    /**
     * Replaces the current selection.
     *
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceSelection(@Nullable CharSequence replacement) {
        SelectionRange selection = getSelection();
        return replaceText(selection.start(), selection.end(), replacement);
    }

    /**
     * Sets/overrides one text attribute for current selection and stores history.
     *
     * @param attribute attribute name
     * @param value attribute value
     * @return true if text changed
     */
    public boolean applyAttributeToSelection(String attribute, @Nullable Object value) {
        SelectionRange selection = getSelection();
        if (selection.length() == 0) {
            return false;
        }

        RichText updated = text.apply(java.util.Map.of(attribute, value), selection.start(), selection.end());
        return applyFormattingChange(updated);
    }

    /**
     * Applies a style to current selection and stores history.
     *
     * @param style style to apply
     * @return true if text changed
     */
    public boolean applyStyle(Style style) {
        SelectionRange selection = getSelection();
        if (selection.length() == 0) {
            return false;
        }
        return applyFormattingChange(text.apply(style, selection.start(), selection.end()));
    }

    /**
     * Removes a style from current selection and stores history.
     *
     * @param style style to remove
     * @return true if text changed
     */
    public boolean removeStyle(Style style) {
        SelectionRange selection = getSelection();
        if (selection.length() == 0) {
            return false;
        }
        return applyFormattingChange(text.removeStyle(style, selection.start(), selection.end()));
    }

    /**
     * Sets bold state in current selection.
     *
     * @param enabled true for bold, false for normal
     * @return true if text changed
     */
    public boolean markBold(boolean enabled) {
        return enabled ? applyStyle(Style.BOLD) : removeStyle(Style.BOLD);
    }

    /**
     * Sets italic state in current selection.
     *
     * @param enabled true for italic, false for normal
     * @return true if text changed
     */
    public boolean markItalic(boolean enabled) {
        return enabled ? applyStyle(Style.ITALIC) : removeStyle(Style.ITALIC);
    }

    /**
     * Sets underline state in current selection.
     *
     * @param enabled true for underline, false for no underline
     * @return true if text changed
     */
    public boolean markUnderline(boolean enabled) {
        return enabled ? applyStyle(Style.UNDERLINE) : removeStyle(Style.UNDERLINE);
    }

    /**
     * Sets strike-through state in current selection.
     *
     * @param enabled true for strike-through, false for none
     * @return true if text changed
     */
    public boolean markStrikeThrough(boolean enabled) {
        return enabled ? applyStyle(Style.LINE_THROUGH) : removeStyle(Style.LINE_THROUGH);
    }

    /**
     * Returns true if undo is possible.
     *
     * @return true if undo stack is non-empty
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns true if redo is possible.
     *
     * @return true if redo stack is non-empty
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Performs one undo step.
     *
     * @return true if an undo step was applied
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }

        HistoryEntry entry = undoStack.removeLast();
        replaceTextInternal(entry.start(), entry.start() + entry.inserted().length(), entry.removed(), false);
        anchor = entry.beforeAnchor();
        caret = entry.beforeCaret();
        redoStack.add(entry);
        return true;
    }

    /**
     * Performs one redo step.
     *
     * @return true if a redo step was applied
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }

        HistoryEntry entry = redoStack.removeLast();
        replaceTextInternal(entry.start(), entry.start() + entry.removed().length(), entry.inserted(), false);
        anchor = entry.afterAnchor();
        caret = entry.afterCaret();
        undoStack.add(entry);
        return true;
    }

    /**
     * Clears undo/redo history.
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    private boolean applyFormattingChange(RichText updated) {
        RichText current = text;
        if (Objects.equals(current, updated)) {
            return false;
        }

        ChangeRange changed = findChangedRange(current, updated);
        if (changed.isEmpty()) {
            return false;
        }

        int beforeAnchor = anchor;
        int beforeCaret = caret;

        int start = changed.start();
        int endInCurrent = changed.endInCurrent();
        int endInUpdated = changed.endInUpdated();

        RichText removed = detach(current.subSequence(start, endInCurrent));
        RichText inserted = detach(updated.subSequence(start, endInUpdated));

        replaceTextInternal(start, endInCurrent, inserted, false);
        pushHistory(new HistoryEntry(start, removed, inserted, beforeAnchor, beforeCaret, anchor, caret));
        return true;
    }

    private boolean replaceTextInternal(int start, int end, RichText inserted, boolean trackHistory) {
        int max = text.length();
        int s = Math.clamp(Math.min(start, end), 0, max);
        int e = Math.clamp(Math.max(start, end), 0, max);
        RichText removed = detach(text.subSequence(s, e));

        if (removed.equals(inserted)) {
            int newCaret = s + inserted.length();
            selectRange(newCaret, newCaret);
            return false;
        }

        int beforeAnchor = anchor;
        int beforeCaret = caret;

        text = detach(text.replace(s, e, inserted));
        int newCaret = s + inserted.length();
        selectRange(newCaret, newCaret);

        if (trackHistory) {
            pushHistory(new HistoryEntry(s, removed, inserted, beforeAnchor, beforeCaret, anchor, caret));
        }

        return true;
    }

    private void pushHistory(HistoryEntry entry) {
        undoStack.add(entry);
        if (undoStack.size() > maxHistorySize) {
            undoStack.removeFirst();
        }
        redoStack.clear();
    }

    private static RichText detach(RichText text) {
        RichTextBuilder builder = new RichTextBuilder(text.length());
        text.appendTo(builder);
        return builder.toRichText();
    }

    private static ChangeRange findChangedRange(RichText current, RichText updated) {
        int prefix = current.commonPrefixLength(updated);
        int maxSuffix = Math.min(current.length(), updated.length()) - prefix;
        int suffix = Math.min(current.commonSuffixLength(updated), maxSuffix);
        return new ChangeRange(prefix, current.length() - suffix, updated.length() - suffix);
    }

    /**
     * Selection range.
     *
     * @param start selection start (inclusive)
     * @param end selection end (exclusive)
     */
    public record SelectionRange(int start, int end) {
        /**
         * Returns selection length.
         *
         * @return selection length
         */
        public int length() {
            return end - start;
        }
    }

    private record HistoryEntry(
            int start,
            RichText removed,
            RichText inserted,
            int beforeAnchor,
            int beforeCaret,
            int afterAnchor,
            int afterCaret
    ) {}

    private record ChangeRange(int start, int endInCurrent, int endInUpdated) {
        boolean isEmpty() {
            return start == endInCurrent && start == endInUpdated;
        }
    }
}
