package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.FontType;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.RichTextConverter;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.ToRichText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Text editor control based on {@link TextPane}.
 *
 * <p>This class currently provides a stub editing API surface, modeled after JavaFX {@code TextInputControl},
 * but operating on {@link RichText}. Real editing behavior will be added incrementally.
 */
public class TextEditorPane extends TextPane implements InputControl<RichText> {
    private static final String STYLE_LIST_ATTRIBUTE = "__styles";

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);
    private final BooleanProperty toolbarVisible = new SimpleBooleanProperty(this, "toolbarVisible", false);
    private final ReadOnlyIntegerWrapper length = new ReadOnlyIntegerWrapper(this, "length", 0);
    private final ReadOnlyObjectWrapper<RichText> selectedText = new ReadOnlyObjectWrapper<>(this, "selectedText", RichText.emptyText());
    private final ReadOnlyObjectWrapper<IndexRange> selection = new ReadOnlyObjectWrapper<>(this, "selection", new IndexRange(0, 0));
    private final ReadOnlyIntegerWrapper anchor = new ReadOnlyIntegerWrapper(this, "anchor", 0);
    private final ReadOnlyIntegerWrapper caretPosition = new ReadOnlyIntegerWrapper(this, "caretPosition", 0);
    private final ReadOnlyBooleanWrapper undoable = new ReadOnlyBooleanWrapper(this, "undoable", false);
    private final ReadOnlyBooleanWrapper redoable = new ReadOnlyBooleanWrapper(this, "redoable", false);
    private final BooleanProperty bold = new SimpleBooleanProperty(this, "bold", false);
    private final BooleanProperty italic = new SimpleBooleanProperty(this, "italic", false);
    private final BooleanProperty underline = new SimpleBooleanProperty(this, "underline", false);
    private final BooleanProperty strikeThrough = new SimpleBooleanProperty(this, "strikeThrough", false);
    private final ObjectProperty<@Nullable Color> textColor = new SimpleObjectProperty<>(this, "textColor");
    private final StringProperty fontFamily = new SimpleStringProperty(this, "fontFamily");
    private final DoubleProperty fontSize = new SimpleDoubleProperty(this, "fontSize", 0.0);
    private final ObjectProperty<RichText> committedValue;
    private final RichText defaultValue;
    private final InputControlState<RichText> state;
    private final List<EditState> undoStack = new ArrayList<>();
    private final List<EditState> redoStack = new ArrayList<>();
    private final SelectionModel selectionModel = new SelectionModel();
    private @Nullable ScrollPane scrollPane;
    private boolean inHistoryNavigation;
    private boolean updatingPropertiesFromText;
    private boolean normalizingIncomingText;
    private double preferredCaretX = Double.NaN;
    private static final int MAX_HISTORY_SIZE = 256;

    /**
     * Creates an empty {@code TextEditorPane}.
     */
    public TextEditorPane() {
        this(null);
    }

    /**
     * Creates a {@code TextEditorPane} with initial text.
     *
     * @param text initial text
     */
    public TextEditorPane(@Nullable CharSequence text) {
        super(text);

        getStyleClass().add("text-editor-pane");

        RichText initial = normalizeIncomingText(getText());
        if (!initial.equals(getText())) {
            normalizingIncomingText = true;
            try {
                setText(initial);
            } finally {
                normalizingIncomingText = false;
            }
        }

        initial = getText();
        this.defaultValue = initial;
        this.committedValue = new SimpleObjectProperty<>(this, "value", initial);
        this.state = new ObjectInputControlState<>(committedValue, () -> defaultValue, value -> Optional.empty());

        committedValue.addListener((obs, oldValue, newValue) -> applyCommittedValue(newValue));

        length.set(initial.length());
        setSelectionState(0, 0);
        initFormatPropertyListeners();

        textProperty().addListener((obs, oldValue, newValue) -> {
            if (!normalizingIncomingText) {
                RichText normalized = normalizeIncomingText(newValue);
                if (!normalized.equals(newValue)) {
                    normalizingIncomingText = true;
                    try {
                        setText(normalized);
                    } finally {
                        normalizingIncomingText = false;
                    }
                    return;
                }
            }

            length.set(newValue == null ? 0 : newValue.length());
            setSelectionState(getAnchor(), getCaretPosition());
        });

        state.requiredProperty().addListener((v, o, n) -> {
            if (n) {
                if (!getStyleClass().contains(CSS_REQUIRED_INPUT)) {
                    getStyleClass().add(CSS_REQUIRED_INPUT);
                }
            } else {
                getStyleClass().remove(CSS_REQUIRED_INPUT);
            }
        });

        // perform a validation when the control receives or loses focus
        focusedProperty().addListener((v, o, n) -> state.validate());

        initSelectionInteraction();
    }

    private void initSelectionInteraction() {
        setFocusTraversable(true);
        addEventFilter(KeyEvent.KEY_PRESSED, this::processKeyPressed);
        addEventFilter(KeyEvent.KEY_TYPED, this::processKeyTyped);
    }

    void processMousePressed(MouseEvent evt) {
        if (!isTextInteractionEvent(evt)) {
            return;
        }
        requestFocus();

        int pos = hitTest(evt);
        if (evt.getClickCount() >= 3) {
            selectLineAt(pos);
            evt.consume();
            return;
        }

        if (evt.isShiftDown()) {
            selectPositionCaret(pos);
        } else {
            positionCaret(pos);
        }

        if (evt.getClickCount() >= 2) {
            selectWordAt(pos);
        }

        evt.consume();
    }

    void processMouseDragged(MouseEvent evt) {
        if (!isTextInteractionEvent(evt)) {
            return;
        }
        selectPositionCaret(hitTest(evt));
        evt.consume();
    }

    void processKeyPressed(KeyEvent evt) {
        boolean shift = evt.isShiftDown();
        boolean shortcut = evt.isShortcutDown();

        if (shortcut) {
            switch (evt.getCode()) {
                case C -> {
                    copy();
                    evt.consume();
                    return;
                }
                case X -> {
                    cut();
                    evt.consume();
                    return;
                }
                case V -> {
                    paste();
                    evt.consume();
                    return;
                }
                case Z -> {
                    if (shift) {
                        redo();
                    } else {
                        undo();
                    }
                    evt.consume();
                    return;
                }
                case Y -> {
                    redo();
                    evt.consume();
                    return;
                }
                case A -> {
                    selectAll();
                    evt.consume();
                    return;
                }
                default -> {
                    // continue with non-shortcut handling
                }
            }
        }

        switch (evt.getCode()) {
            case BACK_SPACE -> {
                if (isEditable()) {
                    deletePreviousChar();
                    evt.consume();
                }
            }
            case DELETE -> {
                if (isEditable()) {
                    deleteNextChar();
                    evt.consume();
                }
            }
            case TAB -> {
                if (isEditable()) {
                    replaceSelection(toRichTextWithCurrentProperties("\t"));
                }
                evt.consume();
            }
            case LEFT -> {
                preferredCaretX = Double.NaN;
                if (shortcut) {
                    if (shift) {
                        selectPreviousWord();
                    } else {
                        previousWord();
                    }
                } else if (shift) {
                    selectBackward();
                } else {
                    backward();
                }
                evt.consume();
            }
            case RIGHT -> {
                preferredCaretX = Double.NaN;
                if (shortcut) {
                    if (shift) {
                        selectNextWord();
                    } else {
                        nextWord();
                    }
                } else if (shift) {
                    selectForward();
                } else {
                    forward();
                }
                evt.consume();
            }
            case UP -> {
                moveLine(-1, shift);
                evt.consume();
            }
            case DOWN -> {
                moveLine(1, shift);
                evt.consume();
            }
            case HOME -> {
                preferredCaretX = Double.NaN;
                if (shift) {
                    selectHome();
                } else {
                    home();
                }
                evt.consume();
            }
            case END -> {
                preferredCaretX = Double.NaN;
                if (shift) {
                    selectEnd();
                } else {
                    end();
                }
                evt.consume();
            }
            default -> {
                // no-op
            }
        }
    }

    void processKeyTyped(KeyEvent evt) {
        if (!isEditable()) {
            return;
        }
        if (evt.isShortcutDown()) {
            return;
        }

        String chars = evt.getCharacter();
        if (chars == null || chars.isEmpty()) {
            return;
        }

        if ("\r".equals(chars)) {
            replaceSelection(toRichTextWithCurrentProperties("\n"));
            evt.consume();
            return;
        }

        if ("\u007F".equals(chars) || "\b".equals(chars)) {
            return;
        }
        if ("\t".equals(chars)) {
            return;
        }

        boolean hasPrintable = chars.chars().anyMatch(c -> c >= 0x20 || c == '\n' || c == '\t');
        if (!hasPrintable) {
            return;
        }

        replaceSelection(toRichTextWithCurrentProperties(chars));
        evt.consume();
    }

    private void initFormatPropertyListeners() {
        textColor.addListener((obs, oldValue, newValue) -> onAttributePropertyChanged(Style.COLOR, newValue));
        fontFamily.addListener((obs, oldValue, newValue) -> onFontFamilyChanged(newValue));
        fontSize.addListener((obs, oldValue, newValue) -> onFontSizeChanged(newValue.doubleValue()));
    }

    private void onAttributePropertyChanged(String name, @Nullable Object value) {
        if (updatingPropertiesFromText || getSelection().getLength() == 0 || value == null) {
            return;
        }

        IndexRange range = getSelection();
        applyFormattingChange(getText().apply(Map.of(name, value), range.getStart(), range.getEnd()));
    }

    private void onFontFamilyChanged(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        onAttributePropertyChanged(Style.FONT_FAMILIES, List.of(value));
    }

    private void onFontSizeChanged(double value) {
        if (value <= 0.0 || !Double.isFinite(value)) {
            return;
        }

        onAttributePropertyChanged(Style.FONT_SIZE, (float) value);
    }

    @Override
    public InputControlState<RichText> state() {
        return state;
    }

    @Override
    public void set(@Nullable RichText arg) {
        state.setValue(normalizeIncomingText(arg));
    }

    @Override
    public Node node() {
        return this;
    }

    public final BooleanProperty editableProperty() {
        return editable;
    }

    public final boolean isEditable() {
        return editable.get();
    }

    public final void setEditable(boolean value) {
        editable.set(value);
    }

    public final BooleanProperty toolbarVisibleProperty() {
        return toolbarVisible;
    }

    public final boolean isToolbarVisible() {
        return toolbarVisible.get();
    }

    public final void setToolbarVisible(boolean value) {
        toolbarVisible.set(value);
    }

    public final int getLength() {
        return length.get();
    }

    public final ReadOnlyIntegerProperty lengthProperty() {
        return length.getReadOnlyProperty();
    }

    public final RichText getSelectedText() {
        return selectedText.get();
    }

    public final ReadOnlyObjectProperty<RichText> selectedTextProperty() {
        return selectedText.getReadOnlyProperty();
    }

    public final IndexRange getSelection() {
        return selection.get();
    }

    public final ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return selection.getReadOnlyProperty();
    }

    public final int getAnchor() {
        return anchor.get();
    }

    public final ReadOnlyIntegerProperty anchorProperty() {
        return anchor.getReadOnlyProperty();
    }

    public final int getCaretPosition() {
        return caretPosition.get();
    }

    public final ReadOnlyIntegerProperty caretPositionProperty() {
        return caretPosition.getReadOnlyProperty();
    }

    public final BooleanProperty boldProperty() {
        return bold;
    }

    public final boolean isBold() {
        return bold.get();
    }

    public final void setBold(boolean value) {
        bold.set(value);
    }

    public final BooleanProperty italicProperty() {
        return italic;
    }

    public final boolean isItalic() {
        return italic.get();
    }

    public final void setItalic(boolean value) {
        italic.set(value);
    }

    public final BooleanProperty underlineProperty() {
        return underline;
    }

    public final boolean isUnderline() {
        return underline.get();
    }

    public final void setUnderline(boolean value) {
        underline.set(value);
    }

    public final BooleanProperty strikeThroughProperty() {
        return strikeThrough;
    }

    public final boolean isStrikeThrough() {
        return strikeThrough.get();
    }

    public final void setStrikeThrough(boolean value) {
        strikeThrough.set(value);
    }

    public final ObjectProperty<@Nullable Color> textColorProperty() {
        return textColor;
    }

    public final @Nullable Color getTextColor() {
        return textColor.get();
    }

    public final void setTextColor(@Nullable Color value) {
        textColor.set(value);
    }

    public final StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public final @Nullable String getFontFamily() {
        return fontFamily.get();
    }

    public final void setFontFamily(@Nullable String value) {
        fontFamily.set(value);
    }

    public final DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public final double getFontSize() {
        return fontSize.get();
    }

    public final void setFontSize(double value) {
        fontSize.set(value);
    }

    public final boolean isUndoable() {
        return undoable.get();
    }

    public final ReadOnlyBooleanProperty undoableProperty() {
        return undoable.getReadOnlyProperty();
    }

    public final boolean isRedoable() {
        return redoable.get();
    }

    public final ReadOnlyBooleanProperty redoableProperty() {
        return redoable.getReadOnlyProperty();
    }

    public RichText getText(int start, int end) {
        RichText text = getText();
        int s = Math.clamp(Math.min(start, end), 0, text.length());
        int e = Math.clamp(Math.max(start, end), 0, text.length());
        return text.subSequence(s, e);
    }

    public void appendText(@Nullable CharSequence text) {
        replaceText(getLength(), getLength(), text);
    }

    public void appendText(@Nullable ToRichText text) {
        replaceText(getLength(), getLength(), text == null ? RichText.emptyText() : text.toRichText());
    }

    public void appendText(@Nullable CharSequence text, Font font) {
        replaceText(getLength(), getLength(), toRichText(text, font));
    }

    public void insertText(int index, @Nullable CharSequence text) {
        replaceText(index, index, text);
    }

    public void insertText(int index, @Nullable CharSequence text, Font font) {
        replaceText(index, index, toRichText(text, font));
    }

    public boolean deletePreviousChar() {
        IndexRange range = getSelection();
        if (range.getLength() > 0) {
            deleteText(range);
            return true;
        }
        int caret = getCaretPosition();
        if (caret > 0) {
            deleteText(caret - 1, caret);
            return true;
        }
        return false;
    }

    public boolean deleteNextChar() {
        IndexRange range = getSelection();
        if (range.getLength() > 0) {
            deleteText(range);
            return true;
        }
        int caret = getCaretPosition();
        if (caret < getLength()) {
            deleteText(caret, caret + 1);
            return true;
        }
        return false;
    }

    public void deleteText(IndexRange range) {
        replaceText(range.getStart(), range.getEnd(), "");
    }

    public void deleteText(int start, int end) {
        replaceText(start, end, "");
    }

    public void replaceText(IndexRange range, @Nullable CharSequence text) {
        replaceText(range.getStart(), range.getEnd(), text);
    }

    public void replaceText(IndexRange range, @Nullable CharSequence text, Font font) {
        replaceText(range.getStart(), range.getEnd(), toRichText(text, font));
    }

    public void replaceText(int start, int end, @Nullable CharSequence text, Font font) {
        replaceText(start, end, toRichText(text, font));
    }

    public void replaceText(int start, int end, @Nullable CharSequence replacement) {
        CharSequence text = Objects.requireNonNullElse(replacement, "");

        int max = getLength();
        int s = Math.clamp(Math.min(start, end), 0, max);
        int e = Math.clamp(Math.max(start, end), 0, max);

        RichText current = getText();
        RichText prefix = current.subSequence(0, s);
        RichText suffix = current.subSequence(e, current.length());

        RichTextBuilder rtb = new RichTextBuilder(current.length() - (e - s) + text.length());
        rtb.append(prefix);
        rtb.append(text);
        rtb.append(suffix);
        RichText updated = rtb.toRichText();

        if (current.equals(updated)) {
            int newCaret = s + text.length();
            setSelectionState(newCaret, newCaret);
            return;
        }

        pushUndoState();
        setText(updated);

        int newCaret = s + text.length();
        setSelectionState(newCaret, newCaret);
        updateHistoryState();
    }

    public void replaceSelection(@Nullable CharSequence replacement) {
        IndexRange r = getSelection();
        replaceText(r.getStart(), r.getEnd(), Objects.requireNonNullElse(replacement, ""));
    }

    public void selectRange(int anchor, int caretPosition) {
        setSelectionState(anchor, caretPosition);
    }

    public void selectAll() {
        setSelectionState(0, getLength());
    }

    public void deselect() {
        int caret = getCaretPosition();
        setSelectionState(caret, caret);
    }

    public void clear() {
        replaceText(0, getLength(), RichText.emptyText());
    }

    public void positionCaret(int pos) {
        int p = Math.clamp(pos, 0, getLength());
        setSelectionState(p, p);
        preferredCaretX = Double.NaN;
    }

    public void selectPositionCaret(int pos) {
        setSelectionState(getAnchor(), pos);
    }

    public void home() {
        positionCaret(0);
    }

    public void end() {
        positionCaret(getLength());
    }

    public void forward() {
        positionCaret(getCaretPosition() + 1);
    }

    public void backward() {
        positionCaret(getCaretPosition() - 1);
    }

    public void previousWord() {
        positionCaret(previousWordStart(getCaretPosition()));
    }

    public void nextWord() {
        positionCaret(nextWordStart(getCaretPosition()));
    }

    public void endOfNextWord() {
        positionCaret(nextWordEnd(getCaretPosition()));
    }

    public void selectBackward() {
        selectPositionCaret(getCaretPosition() - 1);
    }

    public void selectForward() {
        selectPositionCaret(getCaretPosition() + 1);
    }

    public void selectPreviousWord() {
        selectPositionCaret(previousWordStart(getCaretPosition()));
    }

    public void selectNextWord() {
        selectPositionCaret(nextWordStart(getCaretPosition()));
    }

    public void selectEndOfNextWord() {
        selectPositionCaret(nextWordEnd(getCaretPosition()));
    }

    public void selectHome() {
        selectPositionCaret(0);
    }

    public void selectEnd() {
        selectPositionCaret(getLength());
    }

    public void copy() {
        FxUtil.copyToClipboard(getSelectedText());
    }

    public void cut() {
        if (!isEditable()) {
            return;
        }
        if (getSelection().getLength() <= 0) {
            return;
        }
        copy();
        replaceSelection("");
    }

    public void paste() {
        if (!isEditable()) {
            return;
        }
        FxUtil.getTextFromClipboard().ifPresent(this::replaceSelection);
    }

    public void undo() {
        undoOrRedo(undoStack, redoStack);
    }

    public void redo() {
        undoOrRedo(redoStack, undoStack);
    }

    /**
     * Performs an undo or redo operation by managing the provided undo and redo stacks.
     * The method restores the previous state from the undo stack and saves the current
     * state into the redo stack. It ensures application of the restored state and updates
     * the history navigation state.
     *
     * @param undoStack the stack containing the states for undo operations.
     * @param redoStack the stack containing the states for redo operations.
     */
    private void undoOrRedo(List<EditState> undoStack, List<EditState> redoStack) {
        if (undoStack.isEmpty()) {
            return;
        }

        EditState current = snapshot();
        EditState previous = undoStack.removeLast();
        inHistoryNavigation = true;
        try {
            redoStack.add(current);
            applyState(previous);
        } finally {
            inHistoryNavigation = false;
        }
        updateHistoryState();
    }

    public void apply(Style style) {
        IndexRange range = getSelection();
        applyFormattingChange(getText().apply(style, range.getStart(), range.getEnd()));
    }

    public void setStyle(Style style, boolean enabled) {
        if (enabled) {
            apply(style);
        } else {
            remove(style);
        }
    }

    public void remove(Style style) {
        IndexRange range = getSelection();
        applyFormattingChange(getText().removeStyle(style, range.getStart(), range.getEnd()));
    }

    private void applyFormattingChange(RichText updated) {
        RichText current = getText();
        if (current.equals(updated)) {
            return;
        }

        pushUndoState();
        setText(updated);
        updateHistoryState();
    }

    public void markBold(boolean enabled) {
        applyAttributeToSelection(Style.FONT_WEIGHT, enabled ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
    }

    public void markItalic(boolean enabled) {
        applyAttributeToSelection(Style.FONT_STYLE, enabled ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
    }

    public void markUnderline(boolean enabled) {
        applyAttributeToSelection(Style.TEXT_DECORATION_UNDERLINE,
                enabled ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
    }

    public void markStrikeThrough(boolean enabled) {
        applyAttributeToSelection(Style.TEXT_DECORATION_LINE_THROUGH,
                enabled ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
    }

    public void cancelEdit() {
        applyCommittedValue(get());
        state.validate();
    }

    public void commitValue() {
        RichText committed = normalizeIncomingText(getText());
        if (!Objects.equals(get(), committed)) {
            set(committed);
        } else {
            state.validate();
        }
    }

    private int hitTest(MouseEvent evt) {
        Point2D p = toContentPoint(evt);
        return indexForPoint(buildVisualLines(currentWrapWidth()), p.getX(), p.getY());
    }

    private void selectWordAt(int pos) {
        String text = getText().toString();
        if (text.isEmpty()) {
            setSelectionState(0, 0);
            return;
        }

        int p = Math.clamp(pos, 0, text.length());
        if (p == text.length() && p > 0) {
            p--;
        }

        boolean word = isWordChar(text.charAt(p));
        int start = p;
        int end = p;
        while (start > 0 && isWordChar(text.charAt(start - 1)) == word) {
            start--;
        }
        while (end < text.length() && isWordChar(text.charAt(end)) == word) {
            end++;
        }
        setSelectionState(start, end);
        preferredCaretX = Double.NaN;
    }

    private void selectLineAt(int pos) {
        String text = getText().toString();
        if (text.isEmpty()) {
            setSelectionState(0, 0);
            return;
        }

        int p = Math.clamp(pos, 0, text.length());
        if (p == text.length() && p > 0) {
            p--;
        }

        int start = p <= 0 ? 0 : text.lastIndexOf('\n', p - 1) + 1;
        int end = text.indexOf('\n', p);
        if (end < 0) {
            end = text.length();
        }

        setSelectionState(start, end);
        preferredCaretX = Double.NaN;
    }

    private void moveLine(int delta, boolean extendSelection) {
        List<VisualLine> lines = buildVisualLines(currentWrapWidth());
        if (lines.isEmpty()) {
            return;
        }

        int caret = getCaretPosition();
        int currentLineIndex = lineIndexForCaret(lines, caret);
        if (currentLineIndex < 0) {
            return;
        }

        VisualLine currentLine = lines.get(currentLineIndex);
        double x = Double.isNaN(preferredCaretX) ? xForIndex(currentLine, caret) : preferredCaretX;

        int targetLineIndex = Math.clamp(currentLineIndex + delta, 0, lines.size() - 1);
        int targetCaret = indexForX(lines.get(targetLineIndex), x);

        if (extendSelection) {
            selectPositionCaret(targetCaret);
        } else {
            positionCaret(targetCaret);
        }

        preferredCaretX = x;
    }

    private Point2D toContentPoint(MouseEvent evt) {
        ScrollPane sp = getScrollPane();
        if (sp != null && sp.getContent() != null) {
            Point2D scenePoint = new Point2D(evt.getSceneX(), evt.getSceneY());
            return sp.getContent().sceneToLocal(scenePoint);
        }
        return new Point2D(evt.getX() - snappedLeftInset(), evt.getY() - snappedTopInset());
    }

    private boolean isTextInteractionEvent(MouseEvent evt) {
        if (evt.getEventType() == MouseEvent.MOUSE_PRESSED && evt.getButton() != MouseButton.PRIMARY) {
            return false;
        }

        Object rawTarget = evt.getTarget();
        if (!(rawTarget instanceof Node target)) {
            return false;
        }

        ScrollPane sp = getScrollPane();
        if (sp == null) {
            return true;
        }

        return isDescendantOf(target, sp) && !hasStyleClassInAncestry(target, "scroll-bar");
    }

    private static boolean isDescendantOf(Node node, Node ancestor) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStyleClassInAncestry(Node node, String styleClass) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current.getStyleClass().contains(styleClass)) {
                return true;
            }
        }
        return false;
    }

    private @Nullable ScrollPane getScrollPane() {
        if (scrollPane != null) {
            return scrollPane;
        }

        Node direct = lookup(".scroll-pane");
        if (direct instanceof ScrollPane sp) {
            scrollPane = sp;
            return sp;
        }

        for (Node n : lookupAll(".scroll-pane")) {
            if (n instanceof ScrollPane sp) {
                scrollPane = sp;
                return sp;
            }
        }

        return null;
    }

    List<VisualLine> buildVisualLines(double wrapWidth) {
        double availableWidth = wrapWidth;
        if (!Double.isFinite(availableWidth) || availableWidth <= 1.0) {
            double fallback = getWidth() - snappedLeftInset() - snappedRightInset();
            availableWidth = Double.isFinite(fallback) && fallback > 1.0 ? fallback : 1.0;
        }

        TextPane.Layout layout = createLayout(availableWidth);
        Font baseFont = getFont();
        double defaultLineHeight = Math.max(1.0, baseFont.getFontData().height());
        FontUtil fontUtil = FontUtil.getInstance();

        List<VisualLine> lines = new ArrayList<>();
        for (List<FragmentedText.Fragment> fragmentLine : layout.renderLines()) {
            VisualLine line = toVisualLine(fragmentLine, layout.layoutTextData(), fontUtil, defaultLineHeight);
            if (line == null) {
                continue;
            }
            if (line.length() == 0 && !isLogicalEmptyLine(line.start())) {
                continue;
            }
            if (!lines.isEmpty()) {
                VisualLine previous = lines.getLast();
                if (line.length() == 0 && previous.length() == 0
                        && line.start() == previous.start() && line.end() == previous.end()) {
                    continue;
                }
            }
            lines.add(line);
        }

        if (lines.isEmpty()) {
            lines.add(new VisualLine(0, 0, 0.0, defaultLineHeight, new double[]{0.0}));
        }

        return lines;
    }

    private boolean isLogicalEmptyLine(int position) {
        String text = getText().toString();
        int p = Math.clamp(position, 0, text.length());
        boolean atLineStart = p == 0 || text.charAt(p - 1) == '\n';
        boolean atLineEnd = p == text.length() || text.charAt(p) == '\n';
        return atLineStart && atLineEnd;
    }

    private double currentWrapWidth() {
        if (!isWrapText()) {
            return Double.POSITIVE_INFINITY;
        }

        ScrollPane sp = getScrollPane();
        if (sp != null) {
            double w = sp.getViewportBounds().getWidth();
            if (Double.isFinite(w) && w > 1.0) {
                return w;
            }
        }

        double fallback = getWidth() - snappedLeftInset() - snappedRightInset();
        return Double.isFinite(fallback) && fallback > 1.0 ? fallback : 1.0;
    }

    private static @Nullable VisualLine toVisualLine(
            List<FragmentedText.Fragment> fragmentLine,
            TextPane.LayoutTextData mapping,
            FontUtil fontUtil,
            double defaultLineHeight
    ) {
        if (fragmentLine.isEmpty()) {
            return null;
        }

        double lineTop = fragmentLine.getFirst().y();
        double lineHeight = fragmentLine.stream().mapToDouble(FragmentedText.Fragment::h).max().orElse(defaultLineHeight);
        Map<Integer, Double> sourceBoundaries = new LinkedHashMap<>();

        int lineStart = Integer.MAX_VALUE;
        int lineEnd = Integer.MIN_VALUE;

        for (FragmentedText.Fragment fragment : fragmentLine) {
            if (!(fragment.text() instanceof Run run)) {
                continue;
            }

            int fragmentStart = run.getStart();
            int fragmentEnd = run.getEnd();
            for (int layoutPos = fragmentStart; layoutPos <= fragmentEnd; layoutPos++) {
                int rel = layoutPos - fragmentStart;
                double x = fragment.x() + textWidth(fontUtil, run, rel, fragment.font());
                int sourcePos = mapping.layoutToSourcePosition(layoutPos);
                lineStart = Math.min(lineStart, sourcePos);
                lineEnd = Math.max(lineEnd, sourcePos);
                sourceBoundaries.merge(sourcePos, x, Math::min);
            }
        }

        if (lineStart == Integer.MAX_VALUE || lineEnd < lineStart) {
            return new VisualLine(0, 0, lineTop, Math.max(1.0, lineHeight), new double[]{0.0});
        }

        double[] boundaries = new double[lineEnd - lineStart + 1];
        double x = sourceBoundaries.getOrDefault(lineStart, 0.0);
        for (int sourcePos = lineStart; sourcePos <= lineEnd; sourcePos++) {
            Double mappedX = sourceBoundaries.get(sourcePos);
            if (mappedX != null) {
                x = mappedX;
            }
            boundaries[sourcePos - lineStart] = x;
        }

        return new VisualLine(lineStart, lineEnd, lineTop, Math.max(1.0, lineHeight), boundaries);
    }

    private static double textWidth(FontUtil fontUtil, Run run, int length, Font font) {
        if (length <= 0) {
            return 0.0;
        }
        if (length >= run.length()) {
            return fontUtil.getTextWidth(run, font);
        }
        return fontUtil.getTextWidth(run.subSequence(0, length), font);
    }

    static int indexForPoint(List<VisualLine> lines, double x, double y) {
        if (lines.isEmpty()) {
            return 0;
        }

        if (y <= lines.getFirst().top()) {
            return indexForX(lines.getFirst(), x);
        }

        for (VisualLine line : lines) {
            if (y < line.top() + line.height()) {
                return indexForX(line, x);
            }
        }

        return indexForX(lines.getLast(), x);
    }

    static int lineIndexForCaret(List<VisualLine> lines, int caret) {
        for (int i = 0; i < lines.size(); i++) {
            VisualLine line = lines.get(i);
            if (caret <= line.end()) {
                return i;
            }
            if (i + 1 < lines.size() && caret < lines.get(i + 1).start()) {
                return i;
            }
        }
        return lines.size() - 1;
    }

    static double xForIndex(VisualLine line, int index) {
        int offset = Math.clamp(index - line.start(), 0, line.length());
        return line.boundaries()[offset];
    }

    static int indexForX(VisualLine line, double x) {
        double[] boundaries = line.boundaries();
        double min = boundaries[0];
        if (x <= min) {
            return line.start();
        }
        double max = boundaries[boundaries.length - 1];
        if (x >= max) {
            return line.end();
        }
        for (int i = 0; i < line.length(); i++) {
            double midpoint = (boundaries[i] + boundaries[i + 1]) * 0.5;
            if (x < midpoint) {
                return line.start() + i;
            }
        }
        return line.end();
    }

    private int previousWordStart(int from) {
        String text = getText().toString();
        int i = Math.clamp(from, 0, text.length());
        if (i == 0) {
            return 0;
        }
        i--;
        while (i > 0 && !isWordChar(text.charAt(i))) {
            i--;
        }
        while (i > 0 && isWordChar(text.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    private int nextWordStart(int from) {
        String text = getText().toString();
        int i = Math.clamp(from, 0, text.length());
        while (i < text.length() && isWordChar(text.charAt(i))) {
            i++;
        }
        while (i < text.length() && !isWordChar(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private int nextWordEnd(int from) {
        String text = getText().toString();
        int i = Math.clamp(from, 0, text.length());
        while (i < text.length() && !isWordChar(text.charAt(i))) {
            i++;
        }
        while (i < text.length() && isWordChar(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private void setSelectionState(int anchorPos, int caretPos) {
        selectionModel.selectRange(anchorPos, caretPos);
        updatePropertiesFromCaretPosition();
    }

    private void updatePropertiesFromCaretPosition() {
        RichText text = getText();
        if (text.isEmpty()) {
            return;
        }

        int idx = getPropertyProbeIndex(text.length());
        TextAttributes attributes = text.attributesAt(idx);
        List<Style> styles = text.stylesAt(idx);

        boolean boldAtCaret = styles.contains(Style.BOLD)
                || Objects.equals(resolveAttribute(attributes, styles, Style.FONT_WEIGHT), Style.FONT_WEIGHT_VALUE_BOLD)
                || resolveFont(attributes, styles).map(Font::isBold).orElse(false);

        Object fontStyle = resolveAttribute(attributes, styles, Style.FONT_STYLE);
        boolean italicAtCaret = styles.contains(Style.ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_OBLIQUE)
                || resolveFont(attributes, styles).map(Font::isItalic).orElse(false);

        boolean underlineAtCaret = styles.contains(Style.UNDERLINE)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_UNDERLINE), Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)
                || resolveFont(attributes, styles).map(Font::isUnderline).orElse(false);

        boolean strikeThroughAtCaret = styles.contains(Style.LINE_THROUGH)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_LINE_THROUGH), Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)
                || resolveFont(attributes, styles).map(Font::isStrikeThrough).orElse(false);

        Font fallbackFont = getFont();
        @Nullable Color colorAtCaret = Optional.ofNullable(resolveColor(attributes, styles))
                .orElseGet(fallbackFont::getColor);
        @Nullable String familyAtCaret = Optional.ofNullable(resolveFontFamily(attributes, styles))
                .orElseGet(fallbackFont::getFamily);
        double sizeAtCaret = resolveFontSize(attributes, styles);
        if (!Double.isFinite(sizeAtCaret) || sizeAtCaret <= 0.0) {
            sizeAtCaret = fallbackFont.getSizeInPoints();
        }

        updatingPropertiesFromText = true;
        try {
            setBold(boldAtCaret);
            setItalic(italicAtCaret);
            setUnderline(underlineAtCaret);
            setStrikeThrough(strikeThroughAtCaret);
            setTextColor(colorAtCaret);
            setFontFamily(familyAtCaret);
            setFontSize(sizeAtCaret);
        } finally {
            updatingPropertiesFromText = false;
        }
    }

    private int getPropertyProbeIndex(int textLength) {
        IndexRange selectionRange = getSelection();
        if (selectionRange.getLength() > 0) {
            int start = selectionRange.getStart();
            int end = selectionRange.getEnd();
            int caret = getCaretPosition();
            int idx = caret <= start ? start : caret - 1;
            return Math.clamp(idx, start, end - 1);
        }
        return Math.clamp(getCaretPosition(), 0, textLength - 1);
    }

    private static @Nullable Object resolveAttribute(TextAttributes attributes, List<Style> styles, String name) {
        @Nullable Object value = attributes.get(name);
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

    private static Optional<Font> resolveFont(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT);
        return value instanceof Font font ? Optional.of(font) : Optional.empty();
    }

    private static @Nullable Color resolveColor(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.COLOR);
        if (value instanceof Color color) {
            return color;
        }
        return resolveFont(attributes, styles).map(Font::getColor).orElse(null);
    }

    private static @Nullable String resolveFontFamily(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_FAMILIES);
        if (value instanceof List<?> families && !families.isEmpty()) {
            Object family = families.getFirst();
            if (family != null) {
                return family.toString();
            }
        }
        return resolveFont(attributes, styles).map(Font::getFamily).orElse(null);
    }

    private static double resolveFontSize(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_SIZE);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return resolveFont(attributes, styles).map(Font::getSizeInPoints).orElse(0.0f);
    }

    private static RichText toRichText(@Nullable CharSequence text, Font font) {
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

    private void applyAttributeToSelection(String name, @Nullable Object value) {
        if (value == null) {
            return;
        }

        IndexRange range = getSelection();
        if (range.getLength() == 0) {
            return;
        }

        applyFormattingChange(getText().apply(Map.of(name, value), range.getStart(), range.getEnd()));
    }

    private RichText toRichTextWithCurrentProperties(@Nullable CharSequence text) {
        if (text == null || text.isEmpty()) {
            return RichText.emptyText();
        }

        RichTextBuilder rtb = new RichTextBuilder(text.length());
        if (isBold()) {
            rtb.push(Style.BOLD);
        }
        if (isItalic()) {
            rtb.push(Style.ITALIC);
        }
        if (isUnderline()) {
            rtb.push(Style.UNDERLINE);
        }
        if (isStrikeThrough()) {
            rtb.push(Style.LINE_THROUGH);
        }

        @Nullable Color color = getTextColor();
        if (color != null) {
            rtb.push(Style.COLOR, color);
        }

        @Nullable String family = getFontFamily();
        if (family != null && !family.isBlank()) {
            rtb.push(Style.FONT_FAMILIES, List.of(family));
        }

        double size = getFontSize();
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
        if (color != null) {
            rtb.pop(Style.COLOR);
        }
        if (isStrikeThrough()) {
            rtb.pop(Style.LINE_THROUGH);
        }
        if (isUnderline()) {
            rtb.pop(Style.UNDERLINE);
        }
        if (isItalic()) {
            rtb.pop(Style.ITALIC);
        }
        if (isBold()) {
            rtb.pop(Style.BOLD);
        }

        return rtb.toRichText();
    }

    private RichText normalizeIncomingText(@Nullable RichText text) {
        if (text == null || text.isEmpty()) {
            return text == null ? RichText.emptyText() : text;
        }

        boolean needsNormalization = text.runStream()
                .flatMap(run -> run.getStyles().stream())
                .anyMatch(TextEditorPane::needsFontNormalization);
        if (!needsNormalization) {
            return text;
        }

        RichTextBuilder rtb = new RichTextBuilder(text.length());
        for (Run run : text) {
            List<Style> normalizedStyles = new ArrayList<>();
            for (Style style : run.getStyles()) {
                normalizedStyles.addAll(normalizeStyle(style));
            }

            List<String> pushedAttributes = new ArrayList<>();
            for (Map.Entry<String, @Nullable Object> entry : run.attributes().entrySet()) {
                if (STYLE_LIST_ATTRIBUTE.equals(entry.getKey()) || entry.getValue() == null) {
                    continue;
                }
                rtb.push(entry.getKey(), entry.getValue());
                pushedAttributes.add(entry.getKey());
            }

            for (Style normalizedStyle : normalizedStyles) {
                rtb.push(normalizedStyle);
            }

            rtb.append(run.toString());

            for (int i = normalizedStyles.size() - 1; i >= 0; i--) {
                rtb.pop(normalizedStyles.get(i));
            }
            for (int i = pushedAttributes.size() - 1; i >= 0; i--) {
                rtb.pop(pushedAttributes.get(i));
            }
        }

        return rtb.toRichText();
    }

    private static boolean needsFontNormalization(Style style) {
        if (!style.containsKey(Style.FONT)) {
            return false;
        }

        Object value = style.get(Style.FONT);
        return value instanceof Font || value instanceof FontDef;
    }

    private static List<Style> normalizeStyle(Style style) {
        if (!needsFontNormalization(style)) {
            return List.of(style);
        }

        Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
        for (Map.Entry<String, @Nullable Object> entry : style.entrySet()) {
            String key = entry.getKey();
            @Nullable Object value = entry.getValue();

            if (Style.FONT.equals(key)) {
                if (value instanceof Font font) {
                    RichTextConverter.putFontProperties(attributes, font);
                } else if (value instanceof FontDef fontDef) {
                    putFontDefProperties(attributes, fontDef);
                }
                continue;
            }

            attributes.put(key, value);
        }

        List<Style> result = new ArrayList<>(attributes.size());
        for (Map.Entry<String, @Nullable Object> entry : attributes.entrySet()) {
            if (entry.getValue() != null) {
                result.add(Style.create(style.name() + "-" + entry.getKey(), Map.entry(entry.getKey(), entry.getValue())));
            }
        }
        return result;
    }

    private static void putFontDefProperties(Map<? super String, @Nullable Object> attributes, FontDef fd) {
        @Nullable List<String> families = fd.getFamilies();
        if (families != null && !families.isEmpty()) {
            attributes.put(Style.FONT_FAMILIES, families);
        }

        @Nullable FontType type = fd.getType();
        if (type == FontType.MONOSPACED) {
            attributes.put(Style.FONT_CLASS, Style.FONT_CLASS_VALUE_MONOSPACE);
        }

        @Nullable Float size = fd.getSize();
        if (size != null) {
            attributes.put(Style.FONT_SIZE, size);
        }

        @Nullable Color color = fd.getColor();
        if (color != null) {
            attributes.put(Style.COLOR, color);
        }

        @Nullable Boolean italic = fd.getItalic();
        if (italic != null) {
            attributes.put(Style.FONT_STYLE, italic ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
        }

        @Nullable Boolean bold = fd.getBold();
        if (bold != null) {
            attributes.put(Style.FONT_WEIGHT, bold ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
        }

        @Nullable Boolean underline = fd.getUnderline();
        if (underline != null) {
            attributes.put(Style.TEXT_DECORATION_UNDERLINE,
                    underline ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
        }

        @Nullable Boolean strikeThrough = fd.getStrikeThrough();
        if (strikeThrough != null) {
            attributes.put(Style.TEXT_DECORATION_LINE_THROUGH,
                    strikeThrough ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
        }
    }

    private void pushUndoState() {
        if (inHistoryNavigation) {
            return;
        }
        undoStack.add(snapshot());
        trimHistory(undoStack);
        redoStack.clear();
    }

    private void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        updateHistoryState();
    }

    private void trimHistory(List<EditState> stack) {
        while (stack.size() > MAX_HISTORY_SIZE) {
            stack.removeFirst();
        }
    }

    private EditState snapshot() {
        return new EditState(getText(), getAnchor(), getCaretPosition());
    }

    private void applyState(EditState state) {
        setText(state.text());
        setSelectionState(state.anchor(), state.caret());
    }

    private void applyCommittedValue(@Nullable RichText value) {
        RichText committed = normalizeIncomingText(value);
        if (Objects.equals(getText(), committed)) {
            return;
        }

        normalizingIncomingText = true;
        try {
            setText(committed);
        } finally {
            normalizingIncomingText = false;
        }

        int caret = Math.clamp(getCaretPosition(), 0, committed.length());
        setSelectionState(caret, caret);
        clearHistory();
    }

    private void updateHistoryState() {
        undoable.set(!undoStack.isEmpty());
        redoable.set(!redoStack.isEmpty());
    }

    record VisualLine(int start, int end, double top, double height, double[] boundaries) {
        int length() {
            return Math.max(0, end - start);
        }
    }

    private final class SelectionModel {
        void selectRange(int anchorPos, int caretPos) {
            int a = Math.clamp(anchorPos, 0, getLength());
            int c = Math.clamp(caretPos, 0, getLength());
            anchor.set(a);
            caretPosition.set(c);

            int start = Math.min(a, c);
            int end = Math.max(a, c);
            selection.set(new IndexRange(start, end));
            selectedText.set(getText(start, end));
        }
    }

    private record EditState(RichText text, int anchor, int caret) {}
}
