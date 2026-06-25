package com.dua3.utility.swing;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VisualLine;
import org.jspecify.annotations.Nullable;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Swing rich-text editor pane backed by a shared {@link com.dua3.utility.ui.RichTextEditorModel}.
 */
public class TextEditorPane extends TextPane {

    private static final Color SELECTION_COLOR = new Color(0.25f, 0.45f, 0.85f, 0.35f);

    private boolean editable = true;
    private boolean typingBold;
    private boolean typingItalic;
    private boolean typingUnderline;
    private boolean typingStrikeThrough;
    private int dragAnchor = -1;
    private long documentVersion;

    private int lastAnchor;
    private int lastCaret;
    private int lastSelectionStart;
    private int lastSelectionEnd;

    /**
     * Creates an empty editor pane.
     */
    public TextEditorPane() {
        this(null);
    }

    /**
     * Creates an editor pane with initial text.
     *
     * @param text initial text
     */
    public TextEditorPane(@Nullable CharSequence text) {
        super(text);

        getTextComponent().setFocusable(true);
        getTextComponent().setFocusTraversalKeysEnabled(false);
        installInteractionHandlers();
        syncTypingStylesFromCaret();
    }

    /**
     * Returns the current document text.
     *
     * @return current document text
     */
    public RichText getDocumentText() {
        return model.getText();
    }

    /**
     * Returns current document version.
     *
     * @return document version, incremented on each document mutation
     */
    public long getDocumentVersion() {
        return documentVersion;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    /**
     * Enables or disables editing.
     *
     * @param value true to enable editing
     */
    public void setEditable(boolean value) {
        if (editable == value) {
            return;
        }

        boolean old = editable;
        editable = value;
        getTextComponent().setFocusable(value);
        firePropertyChange("editable", old, value);
        repaint();
    }

    /**
     * Returns current selection start.
     *
     * @return selection start
     */
    public int getSelectionStart() {
        return model.getSelection().start();
    }

    /**
     * Returns current selection end.
     *
     * @return selection end
     */
    public int getSelectionEnd() {
        return model.getSelection().end();
    }

    /**
     * Returns current anchor position.
     *
     * @return anchor position
     */
    public int getAnchorPosition() {
        return model.getAnchor();
    }

    /**
     * Returns whether edits can be undone.
     *
     * @return true if undo is possible
     */
    public boolean canUndo() {
        return model.canUndo();
    }

    /**
     * Returns whether edits can be redone.
     *
     * @return true if redo is possible
     */
    public boolean canRedo() {
        return model.canRedo();
    }

    /**
     * Undo last edit.
     */
    public void undo() {
        if (model.undo()) {
            onModelChanged();
        }
    }

    /**
     * Redo last undone edit.
     */
    public void redo() {
        if (model.redo()) {
            onModelChanged();
        }
    }

    /**
     * Replaces text in a range.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @param replacement replacement text
     */
    public void replaceText(int start, int end, @Nullable CharSequence replacement) {
        if (model.replaceText(start, end, replacement)) {
            onModelChanged();
        } else {
            onSelectionChanged(false);
        }
    }

    /**
     * Replaces the current selection with text.
     *
     * @param value replacement text or {@code null}
     */
    public void replaceSelection(@Nullable CharSequence value) {
        RichText replacement = toRichTextWithTypingStyles(value);
        if (model.replaceSelection(replacement)) {
            onModelChanged();
        } else {
            onSelectionChanged(false);
        }
    }

    /**
     * Marks the current selection as bold.
     *
     * @param value true to apply bold, false to remove
     */
    public void markBold(boolean value) {
        if (model.markBold(value)) {
            onModelChanged();
        }
        if (model.getSelection().length() == 0) {
            typingBold = value;
        }
    }

    /**
     * Toggles bold style.
     */
    public void markBold() {
        markBold(model.getSelection().length() == 0 ? !typingBold : !model.isSelectionStyled(Style.BOLD));
    }

    /**
     * Marks the current selection as italic.
     *
     * @param value true to apply italic, false to remove
     */
    public void markItalic(boolean value) {
        if (model.markItalic(value)) {
            onModelChanged();
        }
        if (model.getSelection().length() == 0) {
            typingItalic = value;
        }
    }

    /**
     * Toggles italic style.
     */
    public void markItalic() {
        markItalic(model.getSelection().length() == 0 ? !typingItalic : !model.isSelectionStyled(Style.ITALIC));
    }

    /**
     * Marks the current selection as underlined.
     *
     * @param value true to apply underline, false to remove
     */
    public void markUnderline(boolean value) {
        if (model.markUnderline(value)) {
            onModelChanged();
        }
        if (model.getSelection().length() == 0) {
            typingUnderline = value;
        }
    }

    /**
     * Toggles underline style.
     */
    public void markUnderline() {
        markUnderline(model.getSelection().length() == 0 ? !typingUnderline : !model.isSelectionStyled(Style.UNDERLINE));
    }

    /**
     * Marks the current selection as strike-through.
     *
     * @param value true to apply strike-through, false to remove
     */
    public void markStrikeThrough(boolean value) {
        if (model.markStrikeThrough(value)) {
            onModelChanged();
        }
        if (model.getSelection().length() == 0) {
            typingStrikeThrough = value;
        }
    }

    /**
     * Toggles strike-through style.
     */
    public void markStrikeThrough() {
        markStrikeThrough(model.getSelection().length() == 0 ? !typingStrikeThrough : !model.isSelectionStyled(Style.LINE_THROUGH));
    }

    /**
     * Returns caret position.
     *
     * @return caret position
     */
    public int getCaretPosition() {
        return model.getCaretPosition();
    }

    /**
     * Sets caret position.
     *
     * @param position caret position
     */
    public void setCaretPosition(int position) {
        model.positionCaret(position);
        onSelectionChanged(false);
    }

    /**
     * Selects the text range {@code [start, end)}.
     *
     * @param start selection start
     * @param end selection end
     */
    public void selectRange(int start, int end) {
        model.selectRange(start, end);
        onSelectionChanged(true);
    }

    /**
     * Selects all text.
     */
    public void selectAll() {
        model.selectAll();
        onSelectionChanged(true);
    }

    /**
     * Returns selected text as {@link RichText}.
     *
     * @return selected text
     */
    public RichText getSelectedText() {
        return model.getSelectedText();
    }

    /**
     * Copies selected plain text to the clipboard.
     */
    public void copy() {
        RichText selected = model.getSelectedText();
        if (selected.isEmpty()) {
            return;
        }
        SwingUtil.setClipboardText(selected.toString());
    }

    /**
     * Cuts selected text to clipboard.
     */
    public void cut() {
        if (!editable) {
            return;
        }
        if (model.getSelection().length() == 0) {
            return;
        }
        copy();
        replaceSelection("");
    }

    /**
     * Pastes plain text from clipboard.
     */
    public void paste() {
        if (!editable) {
            return;
        }
        SwingUtil.getStringFromClipboard().ifPresent(this::replaceSelection);
    }

    /**
     * Deletes selection or previous character.
     *
     * @return true if text was deleted
     */
    public boolean deletePreviousChar() {
        if (model.deletePreviousChar()) {
            onModelChanged();
            return true;
        }
        return false;
    }

    /**
     * Deletes selection or next character.
     *
     * @return true if text was deleted
     */
    public boolean deleteNextChar() {
        if (model.deleteNextChar()) {
            onModelChanged();
            return true;
        }
        return false;
    }

    @Override
    protected void onModelChanged() {
        super.onModelChanged();

        long oldVersion = documentVersion;
        documentVersion++;
        firePropertyChange("documentVersion", oldVersion, documentVersion);
        firePropertyChange("text", null, model.getText());
        onSelectionChanged(true);
    }

    @Override
    protected void paintOverlay(Graphics2D g2, RenderLayout layout) {
        List<VisualLine> lines = layout.visualLines();
        if (lines.isEmpty()) {
            return;
        }

        var selection = model.getSelection();
        if (selection.length() > 0) {
            g2.setColor(SELECTION_COLOR);
            for (VisualLine line : lines) {
                int from = Math.max(selection.start(), line.start());
                int to = Math.min(selection.end(), line.end());
                if (from >= to) {
                    continue;
                }

                double x1 = RichTextVisualLayoutHelper.xForIndex(line, from);
                double x2 = RichTextVisualLayoutHelper.xForIndex(line, to);
                double left = Math.min(x1, x2);
                double width = Math.max(1.0, Math.abs(x2 - x1));
                int y = (int) Math.floor(line.top());
                int h = Math.max(1, (int) Math.ceil(line.height()));
                g2.fillRect((int) Math.floor(left), y, (int) Math.ceil(width), h);
            }
        }

        if (editable && getTextComponent().isFocusOwner()) {
            int caret = model.getCaretPosition();
            int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caret);
            if (lineIndex >= 0 && lineIndex < lines.size()) {
                VisualLine line = lines.get(lineIndex);
                int x = (int) Math.round(RichTextVisualLayoutHelper.xForIndex(line, caret));
                int y1 = (int) Math.floor(line.top());
                int y2 = (int) Math.ceil(line.top() + line.height());
                g2.setColor(Color.BLACK);
                g2.drawLine(x, y1, x, y2);
            }
        }
    }

    private void installInteractionHandlers() {
        getTextComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }

                getTextComponent().requestFocusInWindow();
                int index = pointToIndex(e.getPoint());
                if (e.isShiftDown()) {
                    dragAnchor = model.getAnchor();
                    model.selectRange(model.getAnchor(), index);
                } else {
                    dragAnchor = index;
                    model.selectRange(index, index);
                }
                onSelectionChanged(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragAnchor = -1;
            }
        });

        getTextComponent().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                int anchor = dragAnchor >= 0 ? dragAnchor : model.getAnchor();
                int index = pointToIndex(e.getPoint());
                model.selectRange(anchor, index);
                onSelectionChanged(false);
            }
        });

        getTextComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                handleKeyTyped(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e);
            }
        });
    }

    private int pointToIndex(Point point) {
        return indexForPoint(point);
    }

    private void handleKeyTyped(KeyEvent event) {
        if (event.isConsumed()) {
            return;
        }
        if (!editable) {
            return;
        }
        if (isShortcutDown(event) || event.isAltDown()) {
            return;
        }

        char c = event.getKeyChar();
        if (c == KeyEvent.CHAR_UNDEFINED || c == '\b' || c == 0x7f) {
            return;
        }

        if (c >= 0x20 || c == '\n' || c == '\t') {
            replaceSelection(String.valueOf(c));
            event.consume();
        }
    }

    private void handleKeyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean shift = event.isShiftDown();

        if (isShortcutDown(event)) {
            switch (keyCode) {
                case KeyEvent.VK_Z -> {
                    if (shift) {
                        redo();
                    } else {
                        undo();
                    }
                    event.consume();
                }
                case KeyEvent.VK_Y -> {
                    redo();
                    event.consume();
                }
                case KeyEvent.VK_C -> {
                    copy();
                    event.consume();
                }
                case KeyEvent.VK_X -> {
                    cut();
                    event.consume();
                }
                case KeyEvent.VK_V -> {
                    paste();
                    event.consume();
                }
                case KeyEvent.VK_A -> {
                    selectAll();
                    event.consume();
                }
                case KeyEvent.VK_B -> {
                    markBold();
                    event.consume();
                }
                case KeyEvent.VK_I -> {
                    markItalic();
                    event.consume();
                }
                case KeyEvent.VK_U -> {
                    markUnderline();
                    event.consume();
                }
                default -> {
                    // no-op
                }
            }
            return;
        }

        boolean wordNavigation = event.isControlDown() || event.isAltDown();
        switch (keyCode) {
            case KeyEvent.VK_LEFT -> {
                moveCaretHorizontal(-1, shift, wordNavigation);
                event.consume();
            }
            case KeyEvent.VK_RIGHT -> {
                moveCaretHorizontal(1, shift, wordNavigation);
                event.consume();
            }
            case KeyEvent.VK_UP -> {
                moveCaretVertical(-1, shift);
                event.consume();
            }
            case KeyEvent.VK_DOWN -> {
                moveCaretVertical(1, shift);
                event.consume();
            }
            case KeyEvent.VK_PAGE_UP -> {
                moveCaretPage(-1, shift);
                event.consume();
            }
            case KeyEvent.VK_PAGE_DOWN -> {
                moveCaretPage(1, shift);
                event.consume();
            }
            case KeyEvent.VK_HOME -> {
                moveCaretLineBoundary(false, shift);
                event.consume();
            }
            case KeyEvent.VK_END -> {
                moveCaretLineBoundary(true, shift);
                event.consume();
            }
            case KeyEvent.VK_BACK_SPACE -> {
                if (editable) {
                    deletePreviousChar();
                }
                event.consume();
            }
            case KeyEvent.VK_DELETE -> {
                if (editable) {
                    deleteNextChar();
                }
                event.consume();
            }
            case KeyEvent.VK_ENTER -> {
                if (editable) {
                    replaceSelection("\n");
                }
                event.consume();
            }
            default -> {
                // no-op
            }
        }
    }

    private static boolean isShortcutDown(KeyEvent event) {
        return event.isControlDown() || event.isMetaDown();
    }

    private void moveCaretHorizontal(int direction, boolean extendSelection, boolean wordNavigation) {
        if (model.moveHorizontal(direction, extendSelection, wordNavigation)) {
            onSelectionChanged(false);
        }
    }

    private void moveCaretVertical(int deltaLines, boolean extendSelection) {
        List<VisualLine> lines = getRenderLayout().visualLines();
        if (model.moveLine(lines, deltaLines, extendSelection)) {
            onSelectionChanged(false);
        }
    }

    private void moveCaretPage(int deltaPages, boolean extendSelection) {
        List<VisualLine> lines = getRenderLayout().visualLines();
        if (model.movePage(lines, deltaPages, extendSelection)) {
            onSelectionChanged(false);
        }
    }

    private void moveCaretLineBoundary(boolean toEnd, boolean extendSelection) {
        List<VisualLine> lines = getRenderLayout().visualLines();
        if (model.moveLineBoundary(lines, toEnd, extendSelection)) {
            onSelectionChanged(false);
        }
    }

    private void onSelectionChanged(boolean resetPreferredX) {
        if (resetPreferredX) {
            model.resetPreferredCaretX();
        }

        int anchor = model.getAnchor();
        int caret = model.getCaretPosition();
        var selection = model.getSelection();
        int selectionStart = selection.start();
        int selectionEnd = selection.end();

        if (anchor != lastAnchor) {
            firePropertyChange("anchor", lastAnchor, anchor);
            lastAnchor = anchor;
        }
        if (caret != lastCaret) {
            firePropertyChange("caretPosition", lastCaret, caret);
            lastCaret = caret;
        }
        if (selectionStart != lastSelectionStart) {
            firePropertyChange("selectionStart", lastSelectionStart, selectionStart);
            lastSelectionStart = selectionStart;
        }
        if (selectionEnd != lastSelectionEnd) {
            firePropertyChange("selectionEnd", lastSelectionEnd, selectionEnd);
            lastSelectionEnd = selectionEnd;
        }

        if (selection.length() == 0) {
            syncTypingStylesFromCaret();
        }

        getTextComponent().repaint();
    }

    private RichText toRichTextWithTypingStyles(@Nullable CharSequence text) {
        return RichTextEditorModel.toRichText(
                text,
                typingBold,
                typingItalic,
                typingUnderline,
                typingStrikeThrough,
                null,
                null,
                null,
                0.0
        );
    }

    private void syncTypingStylesFromCaret() {
        RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
        if (properties == null) {
            typingBold = false;
            typingItalic = false;
            typingUnderline = false;
            typingStrikeThrough = false;
            return;
        }
        typingBold = properties.bold();
        typingItalic = properties.italic();
        typingUnderline = properties.underline();
        typingStrikeThrough = properties.strikeThrough();
    }

}
