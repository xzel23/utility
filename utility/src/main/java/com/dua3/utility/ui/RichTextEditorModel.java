package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
    private final FontUtil fontUtil;
    private final RichTextEditHistory history;
    private @Nullable VisualLineCache visualLineCache;

    /**
     * Creates an empty editor model.
     */
    public RichTextEditorModel() {
        this(RichText.emptyText(), DEFAULT_MAX_HISTORY_SIZE, FontUtil.getInstance());
    }

    /**
     * Creates an editor model with initial text.
     *
     * @param text initial text
     */
    public RichTextEditorModel(@Nullable CharSequence text) {
        this(text == null ? RichText.emptyText() : RichText.valueOf(text), DEFAULT_MAX_HISTORY_SIZE, FontUtil.getInstance());
    }

    /**
     * Creates an editor model with initial text and explicit font utility.
     *
     * @param text initial text
     * @param fontUtil font utility used for layout-related calculations
     */
    public RichTextEditorModel(@Nullable CharSequence text, FontUtil fontUtil) {
        this(text == null ? RichText.emptyText() : RichText.valueOf(text), DEFAULT_MAX_HISTORY_SIZE, fontUtil);
    }

    /**
     * Creates an editor model with initial text and history size.
     *
     * @param text initial text
     * @param maxHistorySize maximum number of history entries
     */
    public RichTextEditorModel(RichText text, int maxHistorySize) {
        this(text, maxHistorySize, FontUtil.getInstance());
    }

    /**
     * Creates an editor model with initial text, history size and explicit font utility.
     *
     * @param text initial text
     * @param maxHistorySize maximum number of history entries
     * @param fontUtil font utility used for layout-related calculations
     */
    public RichTextEditorModel(RichText text, int maxHistorySize, FontUtil fontUtil) {
        this.text = detach(text);
        this.fontUtil = Objects.requireNonNull(fontUtil, "fontUtil");
        this.history = new RichTextEditHistory(maxHistorySize);
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
     * Returns the font utility used by this model for layout calculations.
     *
     * @return font utility
     */
    public FontUtil getFontUtil() {
        return fontUtil;
    }

    /**
     * Returns a detached text slice between offsets.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @return detached text slice
     */
    public RichText getText(int start, int end) {
        return RichTextEditUtil.detachedSubSequence(text, start, end);
    }

    /**
     * Returns a snapshot of logical lines, preserving trailing empty lines.
     *
     * @return snapshot list of lines
     */
    public List<RichText> snapshotLines() {
        return RichTextVisualLayoutHelper.splitLogicalBlocks(text).stream()
                .map(RichTextVisualLayoutHelper.LogicalBlock::text)
                .toList();
    }

    /**
     * Appends plain-text contents (without split markers) to an appendable.
     *
     * @param appendable append target
     * @throws IOException if writing fails
     */
    public void appendPlainTextTo(Appendable appendable) throws IOException {
        List<RichText> lineSnapshot = snapshotLines();
        for (int i = 0; i < lineSnapshot.size(); i++) {
            RichTextEditUtil.appendPlainText(lineSnapshot.get(i), appendable);
            if (i + 1 < lineSnapshot.size()) {
                appendable.append('\n');
            }
        }
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
        invalidateVisualLineCache();
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
        invalidateVisualLineCache();
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
     * Computes previous-word start offset.
     *
     * @param from starting offset
     * @return previous-word start
     */
    public int previousWordStart(int from) {
        return RichTextEditUtil.previousWordStart(text.toString(), from);
    }

    /**
     * Computes next-word start offset.
     *
     * @param from starting offset
     * @return next-word start
     */
    public int nextWordStart(int from) {
        return RichTextEditUtil.nextWordStart(text.toString(), from);
    }

    /**
     * Computes next-word end offset.
     *
     * @param from starting offset
     * @return next-word end
     */
    public int nextWordEnd(int from) {
        return RichTextEditUtil.nextWordEnd(text.toString(), from);
    }

    /**
     * Computes the word/non-word range around a given offset.
     *
     * @param position probe offset
     * @return range around the probe position
     */
    public RichTextEditUtil.WordRange wordRangeAt(int position) {
        return RichTextEditUtil.wordRangeAt(text.toString(), position);
    }

    /**
     * Builds or reuses cached visual lines for the current text.
     *
     * @param availableWidth layout width
     * @param wrapText whether wrapping is enabled
     * @param baseFont base font
     * @param blockLayoutFactory callback producing block layouts
     * @return visual lines in source coordinates
     */
    public List<VisualLine> buildVisualLines(
            double availableWidth,
            boolean wrapText,
            Font baseFont,
            Function<? super RichText, RichTextVisualLayoutHelper.BlockLayout> blockLayoutFactory
    ) {
        double width = Math.max(1.0, availableWidth);
        double widthKey = wrapText ? width : Double.POSITIVE_INFINITY;
        VisualLineCache cache = visualLineCache;
        if (cache != null
                && Double.compare(cache.widthKey(), widthKey) == 0
                && Objects.equals(cache.font(), baseFont)) {
            return cache.lines();
        }

        double defaultLineHeight = Math.max(1.0, baseFont.getFontData().height());
        List<VisualLine> lines = RichTextVisualLayoutHelper.buildVisualLines(
                RichTextVisualLayoutHelper.splitLogicalBlocks(text),
                defaultLineHeight,
                fontUtil,
                blockLayoutFactory
        );
        visualLineCache = new VisualLineCache(widthKey, baseFont, lines);
        return lines;
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
        return history.canUndo();
    }

    /**
     * Returns true if redo is possible.
     *
     * @return true if redo stack is non-empty
     */
    public boolean canRedo() {
        return history.canRedo();
    }

    /**
     * Performs one undo step.
     *
     * @return true if an undo step was applied
     */
    public boolean undo() {
        return history.undo((start, end, replacement, anchorPos, caretPos) -> {
            replaceTextInternal(start, end, replacement, false);
            anchor = anchorPos;
            caret = caretPos;
        });
    }

    /**
     * Performs one redo step.
     *
     * @return true if a redo step was applied
     */
    public boolean redo() {
        return history.redo((start, end, replacement, anchorPos, caretPos) -> {
            replaceTextInternal(start, end, replacement, false);
            anchor = anchorPos;
            caret = caretPos;
        });
    }

    /**
     * Clears undo/redo history.
     */
    public void clearHistory() {
        history.clear();
    }

    private boolean applyFormattingChange(RichText updated) {
        RichText current = text;
        if (Objects.equals(current, updated)) {
            return false;
        }

        ChangeRange changed = RichTextEditUtil.findChangedRange(current, updated);
        if (changed.isEmpty()) {
            return false;
        }

        int beforeAnchor = anchor;
        int beforeCaret = caret;

        int start = changed.start();
        int endInCurrent = changed.endInCurrent();
        int endInUpdated = changed.endInUpdated();

        RichText removed = RichTextEditUtil.detachedSubSequence(current, start, endInCurrent);
        RichText inserted = RichTextEditUtil.detachedSubSequence(updated, start, endInUpdated);

        replaceTextInternal(start, endInCurrent, inserted, false);
        selectRange(beforeAnchor, beforeCaret);
        pushHistory(new RichTextEditHistory.TextReplaceHistoryEntry(start, removed, inserted, beforeAnchor, beforeCaret, anchor, caret));
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
        invalidateVisualLineCache();
        int newCaret = s + inserted.length();
        selectRange(newCaret, newCaret);

        if (trackHistory) {
            pushHistory(new RichTextEditHistory.TextReplaceHistoryEntry(s, removed, inserted, beforeAnchor, beforeCaret, anchor, caret));
        }

        return true;
    }

    private void pushHistory(RichTextEditHistory.TextReplaceHistoryEntry entry) {
        history.push(entry);
    }

    private static RichText detach(RichText text) {
        RichTextBuilder builder = new RichTextBuilder(text.length());
        text.appendTo(builder);
        return builder.toRichText();
    }

    private void invalidateVisualLineCache() {
        visualLineCache = null;
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

}
