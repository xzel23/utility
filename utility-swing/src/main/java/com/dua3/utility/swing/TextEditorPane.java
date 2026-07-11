package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.DetachableNode;
import com.dua3.utility.ui.RichTextEditorPane;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VisualLine;
import org.jspecify.annotations.Nullable;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Swing rich-text editor pane backed by a shared {@link com.dua3.utility.ui.RichTextEditorModel}.
 */
public class TextEditorPane extends TextPane implements RichTextEditorPane {

    private static final java.awt.Color SELECTION_COLOR = new java.awt.Color(0.25f, 0.45f, 0.85f, 0.35f);
    private static final int CARET_BLINK_DELAY_MS = 500;
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();
    private static final com.dua3.utility.text.Font DEFAULT_FONT = FONT_UTIL.getDefaultFont();
    private static final Float[] DEFAULT_FONT_SIZES = {8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 14.0f, 16.0f, 18.0f, 20.0f, 24.0f, 28.0f, 32.0f, 36.0f, 40.0f, 48.0f, 56.0f, 64.0f};
    private static final Color[] DEFAULT_TEXT_COLORS = {
            Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
            Color.RED.darker(), Color.RED, Color.RED.brighter(),
            Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
            Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
            Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
            Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
            Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
    };
    private static final Color[] DEFAULT_BACKGROUND_COLORS = {
            Color.TRANSPARENT_WHITE,
            Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
            Color.RED.darker(), Color.RED, Color.RED.brighter(),
            Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
            Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
            Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
            Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
            Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
    };
    private static final Dimension SYMBOL_BUTTON_SIZE = new Dimension(24, 24);

    private volatile boolean editable = true;
    private volatile boolean enterKeyInsertsNewline = true;
    private volatile boolean typingBold;
    private volatile boolean typingItalic;
    private volatile boolean typingUnderline;
    private volatile boolean typingStrikeThrough;
    private transient @Nullable Color typingTextColor;
    private transient @Nullable Color typingBackgroundColor;
    private @Nullable String typingFontFamily;
    private volatile double typingFontSize;
    private int dragAnchor = -1;
    private final AtomicLong documentVersion = new AtomicLong();
    private final AtomicBoolean caretVisible = new AtomicBoolean(true);
    private final Timer caretBlinkTimer;

    private int lastAnchor;
    private int lastCaret;
    private int lastSelectionStart;
    private int lastSelectionEnd;

    private final JToolBar toolbar;
    private final AtomicBoolean synchronizingToolbar = new AtomicBoolean(false);
    private final AtomicBoolean applyingToolbarLocation = new AtomicBoolean(false);
    private final JComboBox<String> fontList;
    private final JComboBox<Float> sizeList;
    private final JComboBox<Color> textColorList;
    private final JComboBox<Color> backgroundColorList;
    private final JButton undoButton;
    private final JButton redoButton;
    private final JToggleButton boldButton;
    private final JToggleButton italicButton;
    private final JToggleButton underlineButton;
    private final JToggleButton strikeButton;
    private @Nullable JDialog toolbarFloatingDialog;
    private @Nullable Container toolbarApplicationParent;
    private DetachableNode.Location toolbarLocation = DetachableNode.Location.EMBEDDED;

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
        caretBlinkTimer = new Timer(CARET_BLINK_DELAY_MS, e -> onCaretBlinkTick());
        caretBlinkTimer.setRepeats(true);
        caretBlinkTimer.setInitialDelay(CARET_BLINK_DELAY_MS);

        toolbar = new JToolBar();
        toolbar.setFloatable(true);
        toolbar.addHierarchyListener(this::onToolbarHierarchyChanged);

        JButton cutButton = editButton("✂", this::cut);
        JButton copyButton = editButton("⧉", this::copy);
        JButton pasteButton = editButton("\uD83D\uDCCB", this::paste);
        undoButton = editButton("⟲", this::undo);
        redoButton = editButton("⟳", this::redo);
        boldButton = fontStyleButton("B", DEFAULT_FONT.withBold(true), this::markBold);
        italicButton = fontStyleButton("I", DEFAULT_FONT.withItalic(true), this::markItalic);
        underlineButton = fontStyleButton("U", DEFAULT_FONT.withUnderline(true), this::markUnderline);
        strikeButton = fontStyleButton("S", DEFAULT_FONT.withStrikeThrough(true), this::markStrikeThrough);
        fontList = new JComboBox<>(FONT_UTIL.getFamilies(FontUtil.FontTypes.ALL).toArray(new String[0]));
        sizeList = new JComboBox<>(DEFAULT_FONT_SIZES);
        textColorList = new JComboBox<>(DEFAULT_TEXT_COLORS);
        backgroundColorList = new JComboBox<>(DEFAULT_BACKGROUND_COLORS);
        textColorList.setRenderer(createColorRenderer());
        backgroundColorList.setRenderer(createColorRenderer());

        toolbar.add(cutButton);
        toolbar.add(copyButton);
        toolbar.add(pasteButton);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(undoButton);
        toolbar.add(redoButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(fontList);
        toolbar.add(sizeList);
        toolbar.add(boldButton);
        toolbar.add(italicButton);
        toolbar.add(underlineButton);
        toolbar.add(strikeButton);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(textColorList);
        toolbar.add(backgroundColorList);
        toolbar.add(Box.createHorizontalGlue());

        installInteractionHandlers();
        installToolbarHandlers();
        syncToolbar();
        applyToolbarLocation();
        syncTypingStylesFromCaret();
    }

    /**
     * Retrieves the current location of the toolbar.
     *
     * @return the location of the toolbar as a DetachableNode.Location object
     */
    public DetachableNode.Location getToolbarLocation() {
        return toolbarLocation;
    }

    /**
     * Sets the location of the toolbar to the specified value. The method updates
     * the toolbar position and triggers necessary actions such as applying the new
     * location and notifying listeners of the change in property.
     *
     * @param value the new location for the toolbar. It must be an instance of
     *              {@code DetachableNode.Location}.
     */
    public void setToolbarLocation(DetachableNode.Location value) {
        if (toolbarLocation == value) {
            return;
        }
        DetachableNode.Location old = toolbarLocation;
        toolbarLocation = value;
        applyToolbarLocation();
        firePropertyChange("toolbarLocation", old, value);
    }

    /**
     * Retrieves the parent container for the toolbar application, if available.
     *
     * @return the parent container for the toolbar application, or null if no parent container is set.
     */
    public @Nullable Container getToolbarApplicationParent() {
        return toolbarApplicationParent;
    }

    /**
     * Sets the parent container for the toolbar application.
     *
     * @param value the container to set as the toolbar application parent.
     *              Can be null to remove the current parent.
     */
    public void setToolbarApplicationParent(@Nullable Container value) {
        if (toolbarApplicationParent == value) {
            return;
        }
        toolbarApplicationParent = value;
        if (toolbarLocation == DetachableNode.Location.APPLICATION) {
            applyToolbarLocation();
        }
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
        return documentVersion.get();
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

    @Override
    public boolean isEnterKeyInsertsNewline() {
        return enterKeyInsertsNewline;
    }

    @Override
    public void setEnterKeyInsertsNewline(boolean value) {
        enterKeyInsertsNewline = value;
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
     * Returns whether bold is active at current caret/selection.
     *
     * @return true if bold is active
     */
    public boolean isBold() {
        return model.getSelection().length() == 0 ? typingBold : model.isSelectionStyled(Style.BOLD);
    }

    /**
     * Returns whether italic is active at current caret/selection.
     *
     * @return true if italic is active
     */
    public boolean isItalic() {
        return model.getSelection().length() == 0 ? typingItalic : model.isSelectionStyled(Style.ITALIC);
    }

    /**
     * Returns whether underline is active at current caret/selection.
     *
     * @return true if underline is active
     */
    public boolean isUnderline() {
        return model.getSelection().length() == 0 ? typingUnderline : model.isSelectionStyled(Style.UNDERLINE);
    }

    /**
     * Returns whether strike-through is active at current caret/selection.
     *
     * @return true if strike-through is active
     */
    public boolean isStrikeThrough() {
        return model.getSelection().length() == 0 ? typingStrikeThrough : model.isSelectionStyled(Style.LINE_THROUGH);
    }

    /**
     * Returns current typing text color.
     *
     * @return text color
     */
    public @Nullable Color getTextColor() {
        if (model.getSelection().length() > 0) {
            RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
            if (properties != null) {
                return properties.textColor();
            }
        }
        return typingTextColor;
    }

    /**
     * Sets text color for selection or typing style.
     *
     * @param value text color
     */
    public void setTextColor(@Nullable Color value) {
        if (value == null) {
            return;
        }
        if (model.getSelection().length() == 0) {
            typingTextColor = value;
            return;
        }
        if (model.applyAttributeToSelection(Style.COLOR, value)) {
            onModelChanged();
        }
    }

    /**
     * Returns current typing background color.
     *
     * @return background color
     */
    public @Nullable Color getBackgroundColor() {
        if (model.getSelection().length() > 0) {
            RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
            if (properties != null) {
                return properties.backgroundColor();
            }
        }
        return typingBackgroundColor;
    }

    /**
     * Sets background color for selection or typing style.
     *
     * @param value background color
     */
    public void setBackgroundColor(@Nullable Color value) {
        if (value == null) {
            return;
        }
        if (model.getSelection().length() == 0) {
            typingBackgroundColor = value;
            return;
        }
        if (model.applyAttributeToSelection(Style.BACKGROUND_COLOR, value)) {
            onModelChanged();
        }
    }

    /**
     * Returns current typing font family.
     *
     * @return font family
     */
    public @Nullable String getFontFamily() {
        if (model.getSelection().length() > 0) {
            RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
            if (properties != null) {
                return properties.fontFamily();
            }
        }
        return typingFontFamily;
    }

    /**
     * Sets font family for selection or typing style.
     *
     * @param value font family
     */
    public void setFontFamily(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (model.getSelection().length() == 0) {
            typingFontFamily = value;
            return;
        }
        if (model.applyAttributeToSelection(Style.FONT_FAMILIES, List.of(value))) {
            onModelChanged();
        }
    }

    /**
     * Returns current typing font size.
     *
     * @return font size in points
     */
    public double getFontSize() {
        if (model.getSelection().length() > 0) {
            RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
            if (properties != null) {
                return properties.fontSize();
            }
        }
        return typingFontSize;
    }

    /**
     * Sets font size for selection or typing style.
     *
     * @param value font size in points
     */
    public void setFontSize(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return;
        }
        if (model.getSelection().length() == 0) {
            typingFontSize = value;
            return;
        }
        if (model.applyAttributeToSelection(Style.FONT_SIZE, (float) value)) {
            onModelChanged();
        }
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
        SwingUtil.copyToClipboard(selected);
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
        SwingUtil.getTextFromClipboard().ifPresent(this::replaceSelection);
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

        long oldVersion = documentVersion.getAndIncrement();
        firePropertyChange("documentVersion", oldVersion, oldVersion + 1L);
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

        if (shouldPaintCaret() && isCaretVisible()) {
            int caret = model.getCaretPosition();
            int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caret);
            if (lineIndex >= 0 && lineIndex < lines.size()) {
                VisualLine line = lines.get(lineIndex);
                int x = (int) Math.round(RichTextVisualLayoutHelper.xForIndex(line, caret));
                int y1 = (int) Math.floor(line.top());
                int y2 = (int) Math.ceil(line.top() + line.height());
                g2.setColor(java.awt.Color.BLACK);
                g2.drawLine(x, y1, x, y2);
            }
        }
    }

    private boolean isCaretVisible() {
        return caretVisible.get();
    }

    private void installInteractionHandlers() {
        getTextComponent().addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                restartCaretBlink();
            }

            @Override
            public void focusLost(FocusEvent e) {
                stopCaretBlink();
            }
        });

        getTextComponent().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                processMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                processMouseReleased(e);
            }
        });

        getTextComponent().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                processMouseDragged(e);
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

    private void processMousePressed(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return;
        }

        getTextComponent().requestFocusInWindow();

        int index = pointToIndex(event.getPoint());
        if (event.getClickCount() >= 3) {
            selectLineAt(index);
            dragAnchor = model.getAnchor();
            onSelectionChanged(true);
            return;
        }

        if (event.isShiftDown()) {
            model.selectRange(model.getAnchor(), index);
        } else {
            model.selectRange(index, index);
        }

        if (event.getClickCount() >= 2) {
            selectWordAt(index);
        }

        dragAnchor = model.getAnchor();
        onSelectionChanged(true);
    }

    private void processMouseReleased(MouseEvent event) {
        if (!SwingUtilities.isLeftMouseButton(event)) {
            return;
        }
        dragAnchor = -1;
    }

    private void processMouseDragged(MouseEvent event) {
        if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) {
            return;
        }
        int anchor = dragAnchor >= 0 ? dragAnchor : model.getAnchor();
        int index = pointToIndex(event.getPoint());
        model.selectRange(anchor, index);
        onSelectionChanged(false);
    }

    private void selectWordAt(int position) {
        var range = model.wordSelectionRangeAt(position);
        model.selectRange(range.start(), range.end());
        model.resetPreferredCaretX();
    }

    private void selectLineAt(int position) {
        var range = model.lineRangeAt(position);
        model.selectRange(range.start(), range.end());
        model.resetPreferredCaretX();
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

        if (c >= 0x20 || c == '\t') {
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
                if (editable && enterKeyInsertsNewline) {
                    replaceSelection("\n");
                    event.consume();
                }
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

        var selection = model.getSelection();
        if (selection.length() == 0) {
            syncTypingStylesFromCaret();
        }

        int anchor = model.getAnchor();
        int caret = model.getCaretPosition();
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

        ensureCaretVisible();
        restartCaretBlink();
        getTextComponent().repaint();
    }

    private void ensureCaretVisible() {
        List<VisualLine> lines = getRenderLayout().visualLines();
        if (lines.isEmpty()) {
            return;
        }

        int caret = model.getCaretPosition();
        int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caret);
        if (lineIndex < 0 || lineIndex >= lines.size()) {
            return;
        }

        VisualLine line = lines.get(lineIndex);
        double scale = getDisplayScale();
        int x = (int) Math.floor(RichTextVisualLayoutHelper.xForIndex(line, caret) * scale);
        int y = (int) Math.floor(line.top() * scale);
        int h = Math.max(1, (int) Math.ceil(line.height() * scale));
        getTextComponent().scrollRectToVisible(new Rectangle(x, y, 2, h));
    }

    private RichText toRichTextWithTypingStyles(@Nullable CharSequence text) {
        return RichTextEditorModel.toRichText(
                text,
                typingBold,
                typingItalic,
                typingUnderline,
                typingStrikeThrough,
                typingTextColor,
                typingBackgroundColor,
                typingFontFamily,
                typingFontSize
        );
    }

    private void syncTypingStylesFromCaret() {
        RichTextEditorModel.CaretProperties properties = model.resolveCaretProperties(getTextFont());
        if (properties == null) {
            typingBold = false;
            typingItalic = false;
            typingUnderline = false;
            typingStrikeThrough = false;
            typingTextColor = getTextFont().getColor();
            typingBackgroundColor = getTextFont().getBackgroundColor();
            typingFontFamily = getTextFont().getFamily();
            typingFontSize = getTextFont().getSizeInPoints();
            return;
        }
        typingBold = properties.bold();
        typingItalic = properties.italic();
        typingUnderline = properties.underline();
        typingStrikeThrough = properties.strikeThrough();
        typingTextColor = properties.textColor();
        typingBackgroundColor = properties.backgroundColor();
        typingFontFamily = properties.fontFamily();
        typingFontSize = properties.fontSize();
    }

    private boolean shouldPaintCaret() {
        return editable && getTextComponent().isFocusOwner();
    }

    private void restartCaretBlink() {
        if (!shouldPaintCaret()) {
            return;
        }
        caretVisible.set(true);
        caretBlinkTimer.restart();
    }

    private void stopCaretBlink() {
        caretBlinkTimer.stop();
        caretVisible.set(false);
        getTextComponent().repaint();
    }

    private void onCaretBlinkTick() {
        if (shouldPaintCaret()) {
            LangUtil.getAndnegate(caretVisible);
            getTextComponent().repaint();
        } else {
            caretBlinkTimer.stop();
            caretVisible.set(false);
        }
    }

    private void installToolbarHandlers() {
        PropertyChangeListener syncOnEditorChange = evt -> syncToolbar();
        addPropertyChangeListener("documentVersion", syncOnEditorChange);
        addPropertyChangeListener("caretPosition", syncOnEditorChange);
        addPropertyChangeListener("selectionStart", syncOnEditorChange);
        addPropertyChangeListener("selectionEnd", syncOnEditorChange);

        fontList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            setFontFamily((String) fontList.getSelectedItem());
            requestFocusInWindow();
            syncToolbar();
        });
        sizeList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            Float size = (Float) sizeList.getSelectedItem();
            if (size != null) {
                setFontSize(size);
            }
            requestFocusInWindow();
            syncToolbar();
        });
        textColorList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            setTextColor((Color) textColorList.getSelectedItem());
            requestFocusInWindow();
            syncToolbar();
        });
        backgroundColorList.addActionListener(e -> {
            if (synchronizingToolbar.get()) {
                return;
            }
            setBackgroundColor((Color) backgroundColorList.getSelectedItem());
            requestFocusInWindow();
            syncToolbar();
        });
    }

    private void syncToolbar() {
        synchronizingToolbar.set(true);
        try {
            boldButton.setSelected(isBold());
            italicButton.setSelected(isItalic());
            underlineButton.setSelected(isUnderline());
            strikeButton.setSelected(isStrikeThrough());
            undoButton.setEnabled(canUndo());
            redoButton.setEnabled(canRedo());
            fontList.setSelectedItem(getFontFamily());
            float editorFontSize = (float) getFontSize();
            ensureSortedFontSizeEntry(sizeList, editorFontSize);
            sizeList.setSelectedItem(editorFontSize);
            textColorList.setSelectedItem(getTextColor());
            backgroundColorList.setSelectedItem(getBackgroundColor());
        } finally {
            synchronizingToolbar.set(false);
        }
    }

    private void onToolbarHierarchyChanged(HierarchyEvent event) {
        long flags = event.getChangeFlags();
        long relevantFlags = HierarchyEvent.PARENT_CHANGED | HierarchyEvent.SHOWING_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED;
        if ((flags & relevantFlags) == 0) {
            return;
        }
        if (applyingToolbarLocation.get()) {
            return;
        }

        refreshToolbarLocationFromUi();
    }

    private void refreshToolbarLocationFromUi() {
        Container parent = toolbar.getParent();
        DetachableNode.Location newLocation = detectToolbarLocation(parent);

        if (toolbarLocation == DetachableNode.Location.APPLICATION && newLocation == DetachableNode.Location.FLOATING) {
            if (toolbarApplicationParent != null && SwingUtilities.isDescendingFrom(toolbar, toolbarApplicationParent)) {
                toolbarApplicationParent.remove(toolbar);
            }
            if (toolbarApplicationParent != null) {
                toolbarApplicationParent.invalidate();
                toolbarApplicationParent.revalidate();
                toolbarApplicationParent.repaint();

                Container applicationParentContainer = toolbarApplicationParent.getParent();
                if (applicationParentContainer != null) {
                    applicationParentContainer.invalidate();
                    applicationParentContainer.revalidate();
                    applicationParentContainer.repaint();
                }
                Window appWindow = SwingUtilities.getWindowAncestor(toolbarApplicationParent);
                if (appWindow != null) {
                    appWindow.invalidate();
                    appWindow.validate();
                    appWindow.repaint();
                }
            }
        }

        if (newLocation != toolbarLocation) {
            DetachableNode.Location old = toolbarLocation;
            toolbarLocation = newLocation;
            firePropertyChange("toolbarLocation", old, newLocation);
        }
    }

    private DetachableNode.Location detectToolbarLocation(@Nullable Container parent) {
        if (parent == null) {
            return DetachableNode.Location.HIDDEN;
        }
        Container embeddedParent = getColumnHeader();
        if (embeddedParent != null && (parent == embeddedParent || SwingUtilities.isDescendingFrom(parent, embeddedParent))) {
            return DetachableNode.Location.EMBEDDED;
        }
        if (toolbarApplicationParent != null && (parent == toolbarApplicationParent || SwingUtilities.isDescendingFrom(parent, toolbarApplicationParent))) {
            return DetachableNode.Location.APPLICATION;
        }
        if (!toolbar.isVisible()) {
            return DetachableNode.Location.HIDDEN;
        }
        return DetachableNode.Location.FLOATING;
    }

    private void applyToolbarLocation() {
        applyingToolbarLocation.set(true);
        try {
            Window oldFloatingHost = SwingUtilities.getWindowAncestor(toolbar);

            if (toolbarLocation == DetachableNode.Location.FLOATING) {
                Container currentParent = toolbar.getParent();
                if (currentParent != null) {
                    currentParent.remove(toolbar);
                }
                setColumnHeaderView(null);
                showToolbarFloatingWindow();
                hideNativeFloatingHost(oldFloatingHost);
                return;
            }

            hideToolbarFloatingWindow();

            Container currentParent = toolbar.getParent();
            if (currentParent != null) {
                currentParent.remove(toolbar);
            }

            if (toolbarLocation == DetachableNode.Location.EMBEDDED) {
                setColumnHeaderView(toolbar);
                toolbar.setVisible(true);
            } else if (toolbarLocation == DetachableNode.Location.APPLICATION && toolbarApplicationParent != null) {
                setColumnHeaderView(null);
                if (toolbarApplicationParent.getLayout() instanceof BorderLayout) {
                    toolbarApplicationParent.add(toolbar, BorderLayout.NORTH);
                } else {
                    toolbarApplicationParent.add(toolbar, 0);
                }
                toolbar.setVisible(true);
            } else {
                setColumnHeaderView(null);
                toolbar.setVisible(false);
            }

            if (toolbarApplicationParent != null) {
                toolbarApplicationParent.revalidate();
                toolbarApplicationParent.repaint();
            }
            hideNativeFloatingHost(oldFloatingHost);
            revalidate();
            repaint();
        } finally {
            applyingToolbarLocation.set(false);
            refreshToolbarLocationFromUi();
        }
    }

    private void showToolbarFloatingWindow() {
        if (toolbarFloatingDialog == null || !toolbarFloatingDialog.isDisplayable()) {
            toolbarFloatingDialog = new JDialog(SwingUtilities.getWindowAncestor(this));
            toolbarFloatingDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            toolbarFloatingDialog.getContentPane().setLayout(new java.awt.BorderLayout());
        }

        if (toolbar.getParent() != toolbarFloatingDialog.getContentPane()) {
            toolbarFloatingDialog.getContentPane().add(toolbar, java.awt.BorderLayout.NORTH);
        }
        toolbar.setVisible(true);
        toolbarFloatingDialog.pack();
        if (!toolbarFloatingDialog.isShowing()) {
            toolbarFloatingDialog.setLocationRelativeTo(this);
        }
        toolbarFloatingDialog.setVisible(true);
    }

    private void hideToolbarFloatingWindow() {
        if (toolbarFloatingDialog != null) {
            toolbarFloatingDialog.setVisible(false);
        }
    }

    private void hideNativeFloatingHost(@Nullable Window oldFloatingHost) {
        if (oldFloatingHost == null || oldFloatingHost == toolbarFloatingDialog) {
            return;
        }

        Window editorWindow = SwingUtilities.getWindowAncestor(this);
        if (oldFloatingHost == editorWindow) {
            return;
        }

        oldFloatingHost.setVisible(false);
        oldFloatingHost.dispose();
    }

    private static JButton editButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setPreferredSize(SYMBOL_BUTTON_SIZE);
        button.setMinimumSize(SYMBOL_BUTTON_SIZE);
        button.setMaximumSize(SYMBOL_BUTTON_SIZE);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(evt -> action.run());
        return button;
    }

    private static JToggleButton fontStyleButton(String text, com.dua3.utility.text.Font font, java.util.function.Consumer<Boolean> action) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(FONT_UTIL.convert(font));
        button.setPreferredSize(SYMBOL_BUTTON_SIZE);
        button.setMinimumSize(SYMBOL_BUTTON_SIZE);
        button.setMaximumSize(SYMBOL_BUTTON_SIZE);
        button.addActionListener(evt -> action.accept(button.isSelected()));
        return button;
    }

    private static void ensureSortedFontSizeEntry(JComboBox<Float> sizeList, float size) {
        if (!Float.isFinite(size) || size <= 0f) {
            return;
        }

        @SuppressWarnings("unchecked")
        DefaultComboBoxModel<Float> model = (DefaultComboBoxModel<Float>) sizeList.getModel();

        int insertAt = model.getSize();
        for (int i = 0; i < model.getSize(); i++) {
            Float existing = model.getElementAt(i);
            if (existing == null) {
                continue;
            }
            int compare = Float.compare(existing, size);
            if (compare == 0) {
                return;
            }
            if (compare > 0) {
                insertAt = i;
                break;
            }
        }
        model.insertElementAt(size, insertAt);
    }

    private static DefaultListCellRenderer createColorRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, @Nullable Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Color c) {
                    setText(c.toArgb());
                    setIcon(new ColorIcon(c));
                } else {
                    setText("");
                    setIcon(null);
                }
                return this;
            }
        };
    }

    private record ColorIcon(Color color) implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            java.awt.Color awt = SwingUtil.convert(color);
            g.setColor(awt);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Objects.equals(awt, java.awt.Color.BLACK) ? java.awt.Color.WHITE : java.awt.Color.BLACK);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }

}
