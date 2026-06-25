package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.ToRichText;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Toolkit-agnostic rich-text editor model.
 *
 * <p>Tracks document text, selection/caret and undo/redo history.
 */
public class RichTextEditorModel {

    private static final int DEFAULT_MAX_HISTORY_SIZE = 256;
    private static final String PROVIDER = "provider";

    private RichText text;
    private int anchor;
    private int caret;
    private final FontUtil fontUtil;
    private final RichTextEditHistory history;
    private @Nullable VisualLineCache visualLineCache;
    private double preferredCaretX = Double.NaN;

    private ToDoubleFunction<RichTextEditorModel> pageWidthProvider = model -> Double.POSITIVE_INFINITY;
    private ToDoubleFunction<RichTextEditorModel> pageHeightProvider = model -> 0.0;
    private ToDoubleFunction<RichTextEditorModel> wrapWidthProvider = model -> Double.POSITIVE_INFINITY;

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
     * Returns the font utility used by this model for layout calculations.
     *
     * @return font utility
     */
    public FontUtil getFontUtil() {
        return fontUtil;
    }

    /**
     * Sets page-width provider used by shared movement/layout helpers.
     *
     * @param provider width provider
     */
    public void setPageWidthProvider(ToDoubleFunction<RichTextEditorModel> provider) {
        pageWidthProvider = Objects.requireNonNull(provider, PROVIDER);
    }

    /**
     * Sets page-height provider used by shared movement/layout helpers.
     *
     * @param provider height provider
     */
    public void setPageHeightProvider(ToDoubleFunction<RichTextEditorModel> provider) {
        pageHeightProvider = Objects.requireNonNull(provider, PROVIDER);
    }

    /**
     * Sets wrap-width provider used by shared layout helpers.
     *
     * @param provider wrap-width provider
     */
    public void setWrapWidthProvider(ToDoubleFunction<RichTextEditorModel> provider) {
        wrapWidthProvider = Objects.requireNonNull(provider, PROVIDER);
    }

    /**
     * Returns page width from the configured provider.
     *
     * @return page width
     */
    public double getPageWidth() {
        return pageWidthProvider.applyAsDouble(this);
    }

    /**
     * Returns page height from the configured provider.
     *
     * @return page height
     */
    public double getPageHeight() {
        return pageHeightProvider.applyAsDouble(this);
    }

    /**
     * Returns current wrap width from the configured provider.
     *
     * @return wrap width
     */
    public double currentWrapWidth() {
        return wrapWidthProvider.applyAsDouble(this);
    }

    /**
     * Resolves an effective available width.
     *
     * @param wrapWidth requested wrap width
     * @return effective positive width
     */
    public double resolveAvailableWidth(double wrapWidth) {
        double availableWidth = wrapWidth;
        if (!Double.isFinite(availableWidth) || availableWidth <= 1.0) {
            double fallback = getPageWidth();
            availableWidth = Double.isFinite(fallback) && fallback > 1.0 ? fallback : 1.0;
        }
        return availableWidth;
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
     * Creates a lazy {@link ToRichText} snapshot of current document lines.
     *
     * @return lazy snapshot
     */
    public ToRichText createLazySnapshot() {
        return createLazySnapshot(snapshotLines());
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
        resetPreferredCaretX();
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
        resetPreferredCaretX();
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
     * Resets the preferred caret x coordinate used by vertical/page movement.
     */
    public void resetPreferredCaretX() {
        preferredCaretX = Double.NaN;
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
    public IndexRange getSelection() {
        return new IndexRange(Math.min(anchor, caret), Math.max(anchor, caret));
    }

    /**
     * Returns selected text.
     *
     * @return selected text
     */
    public RichText getSelectedText() {
        IndexRange selection = getSelection();
        if (selection.length() == 0) {
            return RichText.emptyText();
        }
        return detach(text.subSequence(selection.start(), selection.end()));
    }

    /**
     * Checks whether a style is active at a document position.
     *
     * @param position probe position
     * @param style style to test
     * @return true if style is active
     */
    public boolean hasStyleAt(int position, Style style) {
        Objects.requireNonNull(style, "style");
        if (length() == 0) {
            return false;
        }

        int probe = Math.clamp(position, 0, length() - 1);
        for (Run run : text) {
            if (run.getStart() <= probe && probe < run.getEnd()) {
                return run.getStyles().contains(style);
            }
        }
        return false;
    }

    /**
     * Checks whether a style is active for the current selection.
     *
     * <p>For a collapsed selection this checks the style at the caret probe index.
     *
     * @param style style to test
     * @return true if the style is active everywhere in selection or at caret
     */
    public boolean isSelectionStyled(Style style) {
        Objects.requireNonNull(style, "style");
        IndexRange selection = getSelection();
        if (selection.length() == 0) {
            return hasStyleAt(getPropertyProbeIndex(), style);
        }

        for (Run run : text) {
            int overlapStart = Math.max(selection.start(), run.getStart());
            int overlapEnd = Math.min(selection.end(), run.getEnd());
            if (overlapStart >= overlapEnd) {
                continue;
            }
            if (!run.getStyles().contains(style)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves caret to a position and clears selection.
     *
     * @param position target caret position
     */
    public void positionCaret(int position) {
        int p = Math.clamp(position, 0, length());
        selectRange(p, p);
        resetPreferredCaretX();
    }

    /**
     * Moves caret while keeping current anchor.
     *
     * @param position new caret position
     */
    public void selectPositionCaret(int position) {
        selectRange(anchor, position);
    }

    /**
     * Selects all text.
     */
    public void selectAll() {
        selectRange(0, length());
    }

    /**
     * Clears selection while keeping caret.
     */
    public void deselect() {
        positionCaret(getCaretPosition());
    }

    /**
     * Moves caret to document start.
     */
    public void home() {
        positionCaret(0);
    }

    /**
     * Moves caret to document end.
     */
    public void end() {
        positionCaret(length());
    }

    /**
     * Moves caret one character forward.
     */
    public void forward() {
        positionCaret(getCaretPosition() + 1);
    }

    /**
     * Moves caret one character backward.
     */
    public void backward() {
        positionCaret(getCaretPosition() - 1);
    }

    /**
     * Moves caret to start of previous word.
     */
    public void previousWord() {
        positionCaret(previousWordStart(getCaretPosition()));
    }

    /**
     * Moves caret to start of next word.
     */
    public void nextWord() {
        positionCaret(nextWordStart(getCaretPosition()));
    }

    /**
     * Moves caret to end of next word.
     */
    public void endOfNextWord() {
        positionCaret(nextWordEnd(getCaretPosition()));
    }

    /**
     * Extends selection one character backward.
     */
    public void selectBackward() {
        selectPositionCaret(getCaretPosition() - 1);
    }

    /**
     * Extends selection one character forward.
     */
    public void selectForward() {
        selectPositionCaret(getCaretPosition() + 1);
    }

    /**
     * Extends selection to previous word start.
     */
    public void selectPreviousWord() {
        selectPositionCaret(previousWordStart(getCaretPosition()));
    }

    /**
     * Extends selection to next word start.
     */
    public void selectNextWord() {
        selectPositionCaret(nextWordStart(getCaretPosition()));
    }

    /**
     * Extends selection to next word end.
     */
    public void selectEndOfNextWord() {
        selectPositionCaret(nextWordEnd(getCaretPosition()));
    }

    /**
     * Extends selection to document start.
     */
    public void selectHome() {
        selectPositionCaret(0);
    }

    /**
     * Extends selection to document end.
     */
    public void selectEnd() {
        selectPositionCaret(length());
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
     * @param range replacement range
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceText(IndexRange range, @Nullable CharSequence replacement) {
        return replaceText(range.getStart(), range.getEnd(), replacement);
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
     * Replaces text in a given range and records the change for undo/redo.
     *
     * @param range replacement range
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceText(IndexRange range, RichText replacement) {
        return replaceText(range.getStart(), range.getEnd(), replacement);
    }

    /**
     * Replaces the current selection.
     *
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceSelection(@Nullable CharSequence replacement) {
        return replaceText(getSelection(), replacement);
    }

    /**
     * Replaces the current selection.
     *
     * @param replacement replacement text
     * @return true if a change was applied
     */
    public boolean replaceSelection(RichText replacement) {
        return replaceText(getSelection(), replacement);
    }

    /**
     * Deletes text in a given range.
     *
     * @param range range to delete
     * @return true if a change was applied
     */
    public boolean deleteText(IndexRange range) {
        return replaceText(range.getStart(), range.getEnd(), RichText.emptyText());
    }

    /**
     * Deletes text in a given range.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @return true if a change was applied
     */
    public boolean deleteText(int start, int end) {
        return replaceText(start, end, RichText.emptyText());
    }

    /**
     * Deletes current selection or previous character.
     *
     * @return true if a change was applied
     */
    public boolean deletePreviousChar() {
        IndexRange selection = getSelection();
        if (selection.length() > 0) {
            return deleteText(selection);
        }
        int caretPos = getCaretPosition();
        if (caretPos <= 0) {
            return false;
        }
        return deleteText(caretPos - 1, caretPos);
    }

    /**
     * Deletes current selection or next character.
     *
     * @return true if a change was applied
     */
    public boolean deleteNextChar() {
        IndexRange selection = getSelection();
        if (selection.length() > 0) {
            return deleteText(selection);
        }
        int caretPos = getCaretPosition();
        if (caretPos >= length()) {
            return false;
        }
        return deleteText(caretPos, caretPos + 1);
    }

    /**
     * Appends plain text.
     *
     * @param value text to append
     * @return true if a change was applied
     */
    public boolean appendText(@Nullable CharSequence value) {
        return replaceText(length(), length(), value);
    }

    /**
     * Inserts plain text.
     *
     * @param index insertion position
     * @param value text to insert
     * @return true if a change was applied
     */
    public boolean insertText(int index, @Nullable CharSequence value) {
        return replaceText(index, index, value);
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
     * Computes the word/non-word range around a given offset.
     *
     * @param position probe offset
     * @return range around the probe position
     */
    public IndexRange wordSelectionRangeAt(int position) {
        RichTextEditUtil.WordRange range = wordRangeAt(position);
        return new IndexRange(range.start(), range.end());
    }

    /**
     * Computes logical line range around a given offset.
     *
     * @param position probe offset
     * @return line range
     */
    public IndexRange lineRangeAt(int position) {
        String plain = text.toString();
        if (plain.isEmpty()) {
            return new IndexRange(0, 0);
        }

        int p = Math.clamp(position, 0, plain.length());
        if (p == plain.length()) {
            p--;
        }
        int start = p <= 0 ? 0 : plain.lastIndexOf('\n', p - 1) + 1;
        int end = plain.indexOf('\n', p);
        if (end < 0) {
            end = plain.length();
        }
        return new IndexRange(start, end);
    }

    /**
     * Moves caret horizontally by character or word.
     *
     * @param direction -1 for left, +1 for right
     * @param extendSelection true to keep anchor
     * @param wordNavigation true for word jumps
     * @return true if selection/caret changed
     */
    public boolean moveHorizontal(int direction, boolean extendSelection, boolean wordNavigation) {
        int caretPos = getCaretPosition();
        int target = switch (direction) {
            case -1 -> wordNavigation ? previousWordStart(caretPos) : Math.max(0, caretPos - 1);
            case 1 -> wordNavigation ? nextWordEnd(caretPos) : Math.min(length(), caretPos + 1);
            default -> caretPos;
        };
        return moveCaretTo(target, extendSelection, true);
    }

    /**
     * Moves caret to line start or end in current visual line.
     *
     * @param lines visual lines
     * @param toEnd true for end, false for start
     * @param extendSelection true to keep anchor
     * @return true if selection/caret changed
     */
    public boolean moveLineBoundary(List<VisualLine> lines, boolean toEnd, boolean extendSelection) {
        if (lines.isEmpty()) {
            return false;
        }
        int caretPos = getCaretPosition();
        int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caretPos);
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return false;
        }

        VisualLine line = lines.get(lineIndex);
        int target = toEnd ? line.end() : line.start();
        return moveCaretTo(target, extendSelection, true);
    }

    /**
     * Moves caret to a neighboring visual line while preserving x column.
     *
     * @param lines visual lines
     * @param deltaLines line delta
     * @param extendSelection true to keep anchor
     * @return true if selection/caret changed
     */
    public boolean moveLine(List<VisualLine> lines, int deltaLines, boolean extendSelection) {
        if (lines.isEmpty()) {
            return false;
        }

        int caretPos = getCaretPosition();
        int currentLineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caretPos);
        if (currentLineIndex < 0 || currentLineIndex >= lines.size()) {
            return false;
        }

        VisualLine currentLine = lines.get(currentLineIndex);
        double x = Double.isNaN(preferredCaretX)
                ? RichTextVisualLayoutHelper.xForIndex(currentLine, caretPos)
                : preferredCaretX;

        int targetLineIndex = Math.clamp((long) currentLineIndex + deltaLines, 0, lines.size() - 1);
        int targetCaret = RichTextVisualLayoutHelper.indexForX(lines.get(targetLineIndex), x);
        preferredCaretX = x;
        return moveCaretTo(targetCaret, extendSelection, false);
    }

    /**
     * Moves caret by one page height in visual coordinates.
     *
     * @param lines visual lines
     * @param deltaPages page delta
     * @param extendSelection true to keep anchor
     * @return true if selection/caret changed
     */
    public boolean movePage(List<VisualLine> lines, int deltaPages, boolean extendSelection) {
        if (deltaPages == 0 || lines.isEmpty()) {
            return false;
        }

        int caretPos = getCaretPosition();
        int currentLineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caretPos);
        if (currentLineIndex < 0 || currentLineIndex >= lines.size()) {
            return false;
        }

        VisualLine currentLine = lines.get(currentLineIndex);
        double x = Double.isNaN(preferredCaretX)
                ? RichTextVisualLayoutHelper.xForIndex(currentLine, caretPos)
                : preferredCaretX;

        double pageHeight = getPageHeight();
        if (!Double.isFinite(pageHeight) || pageHeight <= 1.0) {
            pageHeight = Math.max(1.0, currentLine.height());
        }

        double targetY = currentLine.top() + deltaPages * pageHeight;
        int targetCaret = RichTextVisualLayoutHelper.indexForPoint(lines, x, targetY);
        preferredCaretX = x;
        return moveCaretTo(targetCaret, extendSelection, false);
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
     * Builds visual lines using wrap-width provider and resolved available width.
     *
     * @param wrapText whether wrapping is enabled
     * @param baseFont base font
     * @param blockLayoutFactory callback producing block layouts
     * @return visual lines in source coordinates
     */
    public List<VisualLine> buildVisualLines(
            boolean wrapText,
            Font baseFont,
            Function<? super RichText, RichTextVisualLayoutHelper.BlockLayout> blockLayoutFactory
    ) {
        double width = resolveAvailableWidth(currentWrapWidth());
        return buildVisualLines(width, wrapText, baseFont, blockLayoutFactory);
    }

    /**
     * Sets/overrides one text attribute for current selection and stores history.
     *
     * @param attribute attribute name
     * @param value attribute value
     * @return true if text changed
     */
    public boolean applyAttributeToSelection(String attribute, @Nullable Object value) {
        if (value == null) {
            return false;
        }

        IndexRange selection = getSelection();
        if (selection.length() == 0) {
            return false;
        }

        RichText updated = text.apply(Map.of(attribute, value), selection.start(), selection.end());
        return applyFormattingChange(updated);
    }

    /**
     * Applies a style to current selection and stores history.
     *
     * @param style style to apply
     * @return true if text changed
     */
    public boolean applyStyle(Style style) {
        IndexRange selection = getSelection();
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
        IndexRange selection = getSelection();
        if (selection.length() == 0) {
            return false;
        }
        return applyFormattingChange(text.removeStyle(style, selection.start(), selection.end()));
    }

    /**
     * Sets style state in current selection.
     *
     * @param style style to set
     * @param enabled true to apply, false to remove
     * @return true if text changed
     */
    public boolean setStyle(Style style, boolean enabled) {
        return enabled ? applyStyle(style) : removeStyle(style);
    }

    /**
     * Sets an attribute state in current selection.
     *
     * @param attribute attribute name
     * @param enabledValue value for enabled state
     * @param disabledValue value for disabled state
     * @param enabled true for enabled state
     * @return true if text changed
     */
    public boolean markAttribute(String attribute, Object enabledValue, Object disabledValue, boolean enabled) {
        return applyAttributeToSelection(attribute, enabled ? enabledValue : disabledValue);
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
            resetPreferredCaretX();
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
            resetPreferredCaretX();
        });
    }

    /**
     * Clears undo/redo history.
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     * Returns preferred probe index used for caret-style resolution.
     *
     * @return probe index
     */
    public int getPropertyProbeIndex() {
        return getPropertyProbeIndex(length());
    }

    /**
     * Returns preferred probe index used for caret-style resolution.
     *
     * @param textLength effective text length
     * @return probe index
     */
    public int getPropertyProbeIndex(int textLength) {
        if (textLength <= 0) {
            return 0;
        }
        IndexRange selectionRange = getSelection();
        if (selectionRange.length() > 0) {
            int start = selectionRange.getStart();
            int end = selectionRange.getEnd();
            int caretPos = getCaretPosition();
            int idx = caretPos <= start ? start : caretPos - 1;
            return Math.clamp(idx, start, end - 1);
        }
        return Math.clamp(getCaretPosition(), 0, textLength - 1);
    }

    /**
     * Returns document character at index.
     *
     * @param index index
     * @return character or {@code '\0'} for empty document
     */
    public char charAtDocument(int index) {
        int textLength = length();
        if (textLength == 0) {
            return '\0';
        }
        int p = Math.clamp(index, 0, textLength - 1);
        RichText probe = getText(p, p + 1);
        return probe.isEmpty() ? '\0' : probe.charAt(0);
    }

    /**
     * Computes effective caret-format properties at current probe position.
     *
     * @param fallbackFont fallback font for missing attributes
     * @return caret properties or {@code null} when document is empty
     */
    public @Nullable CaretProperties resolveCaretProperties(Font fallbackFont) {
        int textLength = length();
        if (textLength == 0) {
            return null;
        }

        int idx = getPropertyProbeIndex(textLength);
        if (charAtDocument(idx) == '\n') {
            idx = idx > 0 ? idx - 1 : Math.min(idx + 1, textLength - 1);
        }

        RichText probe = getText(idx, idx + 1);
        if (probe.isEmpty()) {
            return null;
        }

        TextAttributes attributes = probe.attributesAt(0);
        List<Style> styles = probe.stylesAt(0);

        boolean boldAtCaret = styles.contains(Style.BOLD)
                || Objects.equals(resolveAttribute(attributes, styles, Style.FONT_WEIGHT), Style.FONT_WEIGHT_VALUE_BOLD);

        Object fontStyle = resolveAttribute(attributes, styles, Style.FONT_STYLE);
        boolean italicAtCaret = styles.contains(Style.ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_OBLIQUE);

        boolean underlineAtCaret = styles.contains(Style.UNDERLINE)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_UNDERLINE), Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE);

        boolean strikeThroughAtCaret = styles.contains(Style.LINE_THROUGH)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_LINE_THROUGH), Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE);

        Color colorAtCaret = Optional.ofNullable(resolveColor(attributes, styles))
                .orElseGet(fallbackFont::getColor);
        Color backgroundColorAtCaret = Optional.ofNullable(resolveBackgroundColor(attributes, styles))
                .orElseGet(fallbackFont::getBackgroundColor);
        String familyAtCaret = Optional.ofNullable(resolveFontFamily(attributes, styles))
                .orElseGet(fallbackFont::getFamily);
        double sizeAtCaret = resolveFontSize(attributes, styles);
        if (!Double.isFinite(sizeAtCaret) || sizeAtCaret <= 0.0) {
            sizeAtCaret = fallbackFont.getSizeInPoints();
        }

        return new CaretProperties(
                boldAtCaret,
                italicAtCaret,
                underlineAtCaret,
                strikeThroughAtCaret,
                colorAtCaret,
                backgroundColorAtCaret,
                familyAtCaret,
                sizeAtCaret
        );
    }

    /**
     * Resolves one attribute from explicit attributes and style stack.
     *
     * @param attributes explicit attributes
     * @param styles style stack
     * @param name attribute name
     * @return resolved value or {@code null}
     */
    public static @Nullable Object resolveAttribute(TextAttributes attributes, List<Style> styles, String name) {
        Object value = attributes.get(name);
        if (value != null) {
            return value;
        }

        for (int i = styles.size() - 1; i >= 0; i--) {
            value = styles.get(i).get(name);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    /**
     * Resolves text color from explicit attributes and style stack.
     *
     * @param attributes explicit attributes
     * @param styles style stack
     * @return resolved color or {@code null}
     */
    public static @Nullable Color resolveColor(TextAttributes attributes, List<Style> styles) {
        return resolveAttribute(attributes, styles, Style.COLOR) instanceof Color color ? color : null;
    }

    /**
     * Resolves background color from explicit attributes and style stack.
     *
     * @param attributes explicit attributes
     * @param styles style stack
     * @return resolved color or {@code null}
     */
    public static @Nullable Color resolveBackgroundColor(TextAttributes attributes, List<Style> styles) {
        return resolveAttribute(attributes, styles, Style.BACKGROUND_COLOR) instanceof Color color ? color : null;
    }

    /**
     * Resolves font family from explicit attributes and style stack.
     *
     * @param attributes explicit attributes
     * @param styles style stack
     * @return resolved family or {@code null}
     */
    public static @Nullable String resolveFontFamily(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_FAMILIES);
        if (value instanceof List<?> families && !families.isEmpty()) {
            Object family = families.getFirst();
            if (family != null) {
                return family.toString();
            }
        }
        return null;
    }

    /**
     * Resolves font size from explicit attributes and style stack.
     *
     * @param attributes explicit attributes
     * @param styles style stack
     * @return resolved size or 0.0
     */
    public static double resolveFontSize(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_SIZE);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return 0.0;
    }

    /**
     * Creates a rich-text value from plain text and a font style.
     *
     * @param text plain text
     * @param font font
     * @return styled rich text
     */
    public static RichText toRichText(@Nullable CharSequence text, Font font) {
        if (text == null || text.isEmpty()) {
            return RichText.emptyText();
        }

        Style style = Style.create(font);
        RichTextBuilder rtb = new RichTextBuilder(text.length());
        rtb.push(style);
        rtb.append(text);
        rtb.pop(style);
        return rtb.toRichText();
    }

    /**
     * Creates a rich-text value from plain text and current formatting flags.
     *
     * @param text text
     * @param bold bold flag
     * @param italic italic flag
     * @param underline underline flag
     * @param strikeThrough strike-through flag
     * @param color text color
     * @param background background color
     * @param family font family
     * @param size font size
     * @return styled rich text
     */
    public static RichText toRichText(
            @Nullable CharSequence text,
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikeThrough,
            @Nullable Color color,
            @Nullable Color background,
            @Nullable String family,
            double size
    ) {
        if (text == null || text.isEmpty()) {
            return RichText.emptyText();
        }

        RichTextBuilder rtb = new RichTextBuilder(text.length());
        if (bold) {
            rtb.push(Style.BOLD);
        }
        if (italic) {
            rtb.push(Style.ITALIC);
        }
        if (underline) {
            rtb.push(Style.UNDERLINE);
        }
        if (strikeThrough) {
            rtb.push(Style.LINE_THROUGH);
        }
        if (color != null) {
            rtb.push(Style.COLOR, color);
        }
        if (background != null) {
            rtb.push(Style.BACKGROUND_COLOR, background);
        }
        if (family != null && !family.isBlank()) {
            rtb.push(Style.FONT_FAMILIES, List.of(family));
        }
        if (size > 0.0 && Double.isFinite(size)) {
            rtb.push(Style.FONT_SIZE, (float) size);
        }

        rtb.append(text);

        if (size > 0.0 && Double.isFinite(size)) {
            rtb.pop(Style.FONT_SIZE);
        }
        if (family != null && !family.isBlank()) {
            rtb.pop(Style.FONT_FAMILIES);
        }
        if (background != null) {
            rtb.pop(Style.BACKGROUND_COLOR);
        }
        if (color != null) {
            rtb.pop(Style.COLOR);
        }
        if (strikeThrough) {
            rtb.pop(Style.LINE_THROUGH);
        }
        if (underline) {
            rtb.pop(Style.UNDERLINE);
        }
        if (italic) {
            rtb.pop(Style.ITALIC);
        }
        if (bold) {
            rtb.pop(Style.BOLD);
        }

        return rtb.toRichText();
    }

    private boolean moveCaretTo(int target, boolean extendSelection, boolean resetPreferredX) {
        int oldAnchor = anchor;
        int oldCaret = caret;
        int clamped = Math.clamp(target, 0, length());
        if (extendSelection) {
            selectRange(anchor, clamped);
        } else {
            selectRange(clamped, clamped);
        }
        if (resetPreferredX) {
            resetPreferredCaretX();
        }
        return oldAnchor != anchor || oldCaret != caret;
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

    private static RichText detach(RichText value) {
        RichTextBuilder builder = new RichTextBuilder(value.length());
        value.appendTo(builder);
        return builder.toRichText();
    }

    private void invalidateVisualLineCache() {
        visualLineCache = null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ToRichText createLazySnapshot(List<RichText> lines) {
        List[] snapshot = {lines};
        LangUtil.StrongCachingSupplier<RichText> materialized = LangUtil.cache(() -> {
            RichTextBuilder rtb = new RichTextBuilder();
            appendSnapshotToBuilder(snapshot[0], rtb);
            snapshot[0] = null;
            return rtb.toRichText();
        });

        return new ToRichText() {
            @Override
            public RichText toRichText() {
                return materialized.get();
            }

            @Override
            public void appendTo(RichTextBuilder builder) {
                List<RichText> list = snapshot[0];
                if (list == null) {
                    materialized.get().appendTo(builder);
                } else {
                    appendSnapshotToBuilder(list, builder);
                }
            }
        };
    }

    private static void appendSnapshotToBuilder(List<RichText> lineSnapshot, RichTextBuilder builder) {
        for (int i = 0; i < lineSnapshot.size(); i++) {
            lineSnapshot.get(i).appendTo(builder);
            if (i + 1 < lineSnapshot.size()) {
                builder.append('\n');
            }
        }
    }

    /**
     * Caret style snapshot resolved at current probe position.
     *
     * @param bold bold flag
     * @param italic italic flag
     * @param underline underline flag
     * @param strikeThrough strike-through flag
     * @param textColor text color
     * @param backgroundColor background color
     * @param fontFamily font family
     * @param fontSize font size
     */
    public record CaretProperties(
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikeThrough,
            @Nullable Color textColor,
            @Nullable Color backgroundColor,
            @Nullable String fontFamily,
            double fontSize
    ) {
    }
}
