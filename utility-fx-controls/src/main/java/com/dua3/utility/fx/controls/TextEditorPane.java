package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
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
import javafx.scene.control.SelectionModel;
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
            if (n == Boolean.TRUE) {
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
            case PAGE_UP -> {
                movePage(-1, shift);
                evt.consume();
            }
            case PAGE_DOWN -> {
                movePage(1, shift);
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

    /**
     * Returns the input-control state facade.
     *
     * @return state facade used for validation and commit handling
     */
    @Override
    public InputControlState<RichText> state() {
        return state;
    }

    /**
     * Sets the committed value.
     *
     * @param arg new committed value
     */
    @Override
    public void set(@Nullable RichText arg) {
        state.setValue(normalizeIncomingText(arg));
    }

    /**
     * Returns this control as a JavaFX node.
     *
     * @return this instance
     */
    @Override
    public Node node() {
        return this;
    }

    /**
     * Editable property.
     *
     * @return editable property
     */
    public final BooleanProperty editableProperty() {
        return editable;
    }

    /**
     * Indicates whether editing is enabled.
     *
     * @return {@code true} if editable
     */
    public final boolean isEditable() {
        return editable.get();
    }

    /**
     * Enables or disables editing.
     *
     * @param value {@code true} to enable editing
     */
    public final void setEditable(boolean value) {
        editable.set(value);
    }

    /**
     * Toolbar visibility property.
     *
     * @return toolbar visibility property
     */
    public final BooleanProperty toolbarVisibleProperty() {
        return toolbarVisible;
    }

    /**
     * Indicates whether the toolbar should be visible.
     *
     * @return {@code true} if toolbar is visible
     */
    public final boolean isToolbarVisible() {
        return toolbarVisible.get();
    }

    /**
     * Shows or hides the toolbar.
     *
     * @param value {@code true} to show the toolbar
     */
    public final void setToolbarVisible(boolean value) {
        toolbarVisible.set(value);
    }

    /**
     * Returns current document length.
     *
     * @return number of characters
     */
    public final int getLength() {
        return length.get();
    }

    /**
     * Read-only document length property.
     *
     * @return read-only length property
     */
    public final ReadOnlyIntegerProperty lengthProperty() {
        return length.getReadOnlyProperty();
    }

    /**
     * Returns the currently selected text.
     *
     * @return current selection text
     */
    public final RichText getSelectedText() {
        return selectedText.get();
    }

    /**
     * Read-only selected-text property.
     *
     * @return selected-text property
     */
    public final ReadOnlyObjectProperty<RichText> selectedTextProperty() {
        return selectedText.getReadOnlyProperty();
    }

    /**
     * Returns the current selection range.
     *
     * @return current selection range
     */
    public final IndexRange getSelection() {
        return selection.get();
    }

    /**
     * Read-only selection range property.
     *
     * @return selection property
     */
    public final ReadOnlyObjectProperty<IndexRange> selectionProperty() {
        return selection.getReadOnlyProperty();
    }

    /**
     * Returns the selection anchor position.
     *
     * @return anchor position
     */
    public final int getAnchor() {
        return anchor.get();
    }

    /**
     * Read-only anchor property.
     *
     * @return anchor property
     */
    public final ReadOnlyIntegerProperty anchorProperty() {
        return anchor.getReadOnlyProperty();
    }

    /**
     * Returns caret position.
     *
     * @return caret position
     */
    public final int getCaretPosition() {
        return caretPosition.get();
    }

    /**
     * Read-only caret-position property.
     *
     * @return caret-position property
     */
    public final ReadOnlyIntegerProperty caretPositionProperty() {
        return caretPosition.getReadOnlyProperty();
    }

    /**
     * Bold toggle property for current formatting state.
     *
     * @return bold property
     */
    public final BooleanProperty boldProperty() {
        return bold;
    }

    /**
     * Indicates whether bold formatting is active.
     *
     * @return {@code true} if bold is active
     */
    public final boolean isBold() {
        return bold.get();
    }

    /**
     * Sets bold formatting state.
     *
     * @param value {@code true} to enable bold
     */
    public final void setBold(boolean value) {
        bold.set(value);
    }

    /**
     * Italic toggle property for current formatting state.
     *
     * @return italic property
     */
    public final BooleanProperty italicProperty() {
        return italic;
    }

    /**
     * Indicates whether italic formatting is active.
     *
     * @return {@code true} if italic is active
     */
    public final boolean isItalic() {
        return italic.get();
    }

    /**
     * Sets italic formatting state.
     *
     * @param value {@code true} to enable italic
     */
    public final void setItalic(boolean value) {
        italic.set(value);
    }

    /**
     * Underline toggle property for current formatting state.
     *
     * @return underline property
     */
    public final BooleanProperty underlineProperty() {
        return underline;
    }

    /**
     * Indicates whether underline formatting is active.
     *
     * @return {@code true} if underline is active
     */
    public final boolean isUnderline() {
        return underline.get();
    }

    /**
     * Sets underline formatting state.
     *
     * @param value {@code true} to enable underline
     */
    public final void setUnderline(boolean value) {
        underline.set(value);
    }

    /**
     * Strike-through toggle property for current formatting state.
     *
     * @return strike-through property
     */
    public final BooleanProperty strikeThroughProperty() {
        return strikeThrough;
    }

    /**
     * Indicates whether strike-through formatting is active.
     *
     * @return {@code true} if strike-through is active
     */
    public final boolean isStrikeThrough() {
        return strikeThrough.get();
    }

    /**
     * Sets strike-through formatting state.
     *
     * @param value {@code true} to enable strike-through
     */
    public final void setStrikeThrough(boolean value) {
        strikeThrough.set(value);
    }

    /**
     * Text-color property for current formatting state.
     *
     * @return text-color property
     */
    public final ObjectProperty<@Nullable Color> textColorProperty() {
        return textColor;
    }

    /**
     * Returns current text color setting.
     *
     * @return current text color or {@code null}
     */
    public final @Nullable Color getTextColor() {
        return textColor.get();
    }

    /**
     * Sets text color for subsequent input or current selection formatting.
     *
     * @param value text color, or {@code null}
     */
    public final void setTextColor(@Nullable Color value) {
        textColor.set(value);
    }

    /**
     * Font-family property for current formatting state.
     *
     * @return font-family property
     */
    public final StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    /**
     * Returns current font-family setting.
     *
     * @return font family or {@code null}
     */
    public final @Nullable String getFontFamily() {
        return fontFamily.get();
    }

    /**
     * Sets font family for subsequent input or current selection formatting.
     *
     * @param value font family or {@code null}
     */
    public final void setFontFamily(@Nullable String value) {
        fontFamily.set(value);
    }

    /**
     * Font-size property for current formatting state.
     *
     * @return font-size property
     */
    public final DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    /**
     * Returns current font-size setting.
     *
     * @return font size in points
     */
    public final double getFontSize() {
        return fontSize.get();
    }

    /**
     * Sets font size for subsequent input or current selection formatting.
     *
     * @param value font size in points
     */
    public final void setFontSize(double value) {
        fontSize.set(value);
    }

    /**
     * Indicates whether an undo step is available.
     *
     * @return {@code true} if undo is possible
     */
    public final boolean isUndoable() {
        return undoable.get();
    }

    /**
     * Read-only undo-availability property.
     *
     * @return undoable property
     */
    public final ReadOnlyBooleanProperty undoableProperty() {
        return undoable.getReadOnlyProperty();
    }

    /**
     * Indicates whether a redo step is available.
     *
     * @return {@code true} if redo is possible
     */
    public final boolean isRedoable() {
        return redoable.get();
    }

    /**
     * Read-only redo-availability property.
     *
     * @return redoable property
     */
    public final ReadOnlyBooleanProperty redoableProperty() {
        return redoable.getReadOnlyProperty();
    }

    /**
     * Returns a text slice between two offsets.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @return selected text slice
     */
    public RichText getText(int start, int end) {
        RichText text = getText();
        int s = Math.clamp(Math.min(start, end), 0, text.length());
        int e = Math.clamp(Math.max(start, end), 0, text.length());
        return text.subSequence(s, e);
    }

    /**
     * Appends plain text.
     *
     * @param text text to append
     */
    public void appendText(@Nullable CharSequence text) {
        replaceText(getLength(), getLength(), text);
    }

    /**
     * Appends rich text.
     *
     * @param text rich text to append
     */
    public void appendText(@Nullable ToRichText text) {
        replaceText(getLength(), getLength(), text == null ? RichText.emptyText() : text.toRichText());
    }

    /**
     * Appends text using the provided font.
     *
     * @param text text to append
     * @param font font to apply
     */
    public void appendText(@Nullable CharSequence text, Font font) {
        replaceText(getLength(), getLength(), toRichText(text, font));
    }

    /**
     * Inserts plain text at the given index.
     *
     * @param index insertion position
     * @param text text to insert
     */
    public void insertText(int index, @Nullable CharSequence text) {
        replaceText(index, index, text);
    }

    /**
     * Inserts text with a specific font at the given index.
     *
     * @param index insertion position
     * @param text text to insert
     * @param font font to apply
     */
    public void insertText(int index, @Nullable CharSequence text, Font font) {
        replaceText(index, index, toRichText(text, font));
    }

    /**
     * Deletes selection or previous character.
     *
     * @return {@code true} if text was deleted
     */
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

    /**
     * Deletes selection or next character.
     *
     * @return {@code true} if text was deleted
     */
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

    /**
     * Deletes text inside the given range.
     *
     * @param range range to delete
     */
    public void deleteText(IndexRange range) {
        replaceText(range.getStart(), range.getEnd(), "");
    }

    /**
     * Deletes text between offsets.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     */
    public void deleteText(int start, int end) {
        replaceText(start, end, "");
    }

    /**
     * Replaces the given range with plain text.
     *
     * @param range range to replace
     * @param text replacement text
     */
    public void replaceText(IndexRange range, @Nullable CharSequence text) {
        replaceText(range.getStart(), range.getEnd(), text);
    }

    /**
     * Replaces the given range with text rendered using a font.
     *
     * @param range range to replace
     * @param text replacement text
     * @param font font to apply
     */
    public void replaceText(IndexRange range, @Nullable CharSequence text, Font font) {
        replaceText(range.getStart(), range.getEnd(), toRichText(text, font));
    }

    /**
     * Replaces text between offsets with text rendered using a font.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @param text replacement text
     * @param font font to apply
     */
    public void replaceText(int start, int end, @Nullable CharSequence text, Font font) {
        replaceText(start, end, toRichText(text, font));
    }

    /**
     * Replaces text between offsets with plain text.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @param replacement replacement text
     */
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

    /**
     * Replaces the current selection.
     *
     * @param replacement replacement text
     */
    public void replaceSelection(@Nullable CharSequence replacement) {
        IndexRange r = getSelection();
        replaceText(r.getStart(), r.getEnd(), Objects.requireNonNullElse(replacement, ""));
    }

    /**
     * Sets selection anchor and caret.
     *
     * @param anchor anchor position
     * @param caretPosition caret position
     */
    public void selectRange(int anchor, int caretPosition) {
        setSelectionState(anchor, caretPosition);
    }

    /**
     * Selects the full document.
     */
    public void selectAll() {
        setSelectionState(0, getLength());
    }

    /**
     * Clears the current selection.
     */
    public void deselect() {
        int caret = getCaretPosition();
        setSelectionState(caret, caret);
    }

    /**
     * Removes all text.
     */
    public void clear() {
        replaceText(0, getLength(), RichText.emptyText());
    }

    /**
     * Moves caret to a position and clears selection.
     *
     * @param pos target caret position
     */
    public void positionCaret(int pos) {
        int p = Math.clamp(pos, 0, getLength());
        setSelectionState(p, p);
        preferredCaretX = Double.NaN;
    }

    /**
     * Moves caret while keeping current anchor.
     *
     * @param pos new caret position
     */
    public void selectPositionCaret(int pos) {
        setSelectionState(getAnchor(), pos);
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
        positionCaret(getLength());
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
     * Extends selection to start of previous word.
     */
    public void selectPreviousWord() {
        selectPositionCaret(previousWordStart(getCaretPosition()));
    }

    /**
     * Extends selection to start of next word.
     */
    public void selectNextWord() {
        selectPositionCaret(nextWordStart(getCaretPosition()));
    }

    /**
     * Extends selection to end of next word.
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
        selectPositionCaret(getLength());
    }

    /**
     * Copies selection to clipboard.
     */
    public void copy() {
        FxUtil.copyToClipboard(getSelectedText());
    }

    /**
     * Cuts selection to clipboard.
     */
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

    /**
     * Pastes clipboard text at selection.
     */
    public void paste() {
        if (!isEditable()) {
            return;
        }
        FxUtil.getTextFromClipboard().ifPresent(this::replaceSelection);
    }

    /**
     * Performs one undo step.
     */
    public void undo() {
        undoOrRedo(undoStack, redoStack);
    }

    /**
     * Performs one redo step.
     */
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

    /**
     * Applies a style to the current selection.
     *
     * @param style style to apply
     */
    public void apply(Style style) {
        IndexRange range = getSelection();
        applyFormattingChange(getText().apply(style, range.getStart(), range.getEnd()));
    }

    /**
     * Enables or disables a style on the current selection.
     *
     * @param style style to toggle
     * @param enabled {@code true} to apply, {@code false} to remove
     */
    public void setStyle(Style style, boolean enabled) {
        if (enabled) {
            apply(style);
        } else {
            remove(style);
        }
    }

    /**
     * Removes a style from the current selection.
     *
     * @param style style to remove
     */
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

    /**
     * Sets bold formatting on current selection.
     *
     * @param enabled {@code true} for bold, {@code false} for normal weight
     */
    public void markBold(boolean enabled) {
        applyAttributeToSelection(Style.FONT_WEIGHT, enabled ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
    }

    /**
     * Sets italic formatting on current selection.
     *
     * @param enabled {@code true} for italic, {@code false} for normal style
     */
    public void markItalic(boolean enabled) {
        applyAttributeToSelection(Style.FONT_STYLE, enabled ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
    }

    /**
     * Sets underline formatting on current selection.
     *
     * @param enabled {@code true} to underline, {@code false} to remove underline
     */
    public void markUnderline(boolean enabled) {
        applyAttributeToSelection(Style.TEXT_DECORATION_UNDERLINE,
                enabled ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
    }

    /**
     * Sets strike-through formatting on current selection.
     *
     * @param enabled {@code true} to strike through, {@code false} to remove
     */
    public void markStrikeThrough(boolean enabled) {
        applyAttributeToSelection(Style.TEXT_DECORATION_LINE_THROUGH,
                enabled ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
    }

    /**
     * Restores the last committed value and validates the control state.
     */
    public void cancelEdit() {
        applyCommittedValue(get());
        state.validate();
    }

    /**
     * Commits the current text value and validates the control state.
     */
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

        int targetLineIndex = currentLineIndex + delta;
        int targetCaret;
        if (targetLineIndex < 0) {
            targetCaret = 0;
        } else if (targetLineIndex >= lines.size()) {
            VisualLine line = lines.getLast();
            targetCaret = line.end();
            x = xForIndex(line, targetCaret);
        } else {
            targetCaret = indexForX(lines.get(targetLineIndex), x);
        }

        if (extendSelection) {
            selectPositionCaret(targetCaret);
        } else {
            positionCaret(targetCaret);
        }

        preferredCaretX = x;
    }

    private void movePage(int delta, boolean extendSelection) {
        if (delta == 0) {
            return;
        }

        List<VisualLine> lines = buildVisualLines(currentWrapWidth());
        if (lines.isEmpty()) {
            return;
        }

        int caret = getCaretPosition();
        int currentLineIndex = lineIndexForCaret(lines, caret);
        if (currentLineIndex < 0 || currentLineIndex >= lines.size()) {
            return;
        }

        VisualLine currentLine = lines.get(currentLineIndex);
        double x = Double.isNaN(preferredCaretX) ? xForIndex(currentLine, caret) : preferredCaretX;

        double pageHeight = 0.0;
        ScrollPane sp = getScrollPane();
        if (sp != null) {
            double viewportHeight = sp.getViewportBounds().getHeight();
            if (Double.isFinite(viewportHeight) && viewportHeight > 1.0) {
                pageHeight = viewportHeight;
            }
        }

        if (pageHeight <= 1.0) {
            double fallback = getHeight() - snappedTopInset() - snappedBottomInset();
            if (Double.isFinite(fallback) && fallback > 1.0) {
                pageHeight = fallback;
            } else {
                pageHeight = Math.max(1.0, currentLine.height());
            }
        }

        double targetY = currentLine.top() + delta * pageHeight;
        int targetCaret = indexForPoint(lines, x, targetY);

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

        String text = getText().toString();
        lines = addMissingLogicalEmptyLines(lines, text, defaultLineHeight);

        return lines;
    }

    private boolean isLogicalEmptyLine(int position) {
        return isLogicalEmptyLine(getText().toString(), position);
    }

    private static boolean isLogicalEmptyLine(String text, int position) {
        int p = Math.clamp(position, 0, text.length());
        boolean atLineStart = p == 0 || text.charAt(p - 1) == '\n';
        boolean atLineEnd = p == text.length() || text.charAt(p) == '\n';
        return atLineStart && atLineEnd;
    }

    private static List<VisualLine> addMissingLogicalEmptyLines(List<VisualLine> sourceLines, String text, double defaultLineHeight) {
        if (text.isEmpty()) {
            return sourceLines;
        }

        List<VisualLine> lines = sourceLines;
        for (int p = 0; p <= text.length(); p++) {
            if (!isLogicalEmptyLine(text, p) || containsExactLine(lines, p)) {
                continue;
            }
            lines = insertEmptyLine(lines, p, defaultLineHeight);
        }
        return lines;
    }

    private static boolean containsExactLine(List<VisualLine> lines, int position) {
        return lines.stream().anyMatch(line -> line.start() == position && line.end() == position);
    }

    private static List<VisualLine> insertEmptyLine(List<VisualLine> sourceLines, int position, double defaultLineHeight) {
        List<VisualLine> result = new ArrayList<>(sourceLines.size() + 1);
        int insertIndex = 0;
        while (insertIndex < sourceLines.size() && sourceLines.get(insertIndex).end() <= position) {
            insertIndex++;
        }

        VisualLine previous = insertIndex > 0 ? sourceLines.get(insertIndex - 1) : null;
        VisualLine next = insertIndex < sourceLines.size() ? sourceLines.get(insertIndex) : null;

        double height = Math.max(1.0, defaultLineHeight);
        if (previous != null && next != null) {
            height = Math.clamp(previous.height(), 1.0, next.height());
        } else if (previous != null) {
            height = Math.max(1.0, previous.height());
        } else if (next != null) {
            height = Math.max(1.0, next.height());
        }

        boolean shiftFollowing = false;
        double top;
        if (previous != null && next != null) {
            top = previous.top() + previous.height();
            double gap = next.top() - top;
            if (gap > 0.5) {
                height = Math.clamp(height, 1.0, gap);
            } else {
                shiftFollowing = true;
            }
        } else if (previous != null) {
            top = previous.top() + previous.height();
        } else if (next != null) {
            top = Math.max(0.0, next.top() - height);
            if (top + height > next.top() + 0.5) {
                shiftFollowing = true;
            }
        } else {
            top = 0.0;
        }

        for (int i = 0; i < insertIndex; i++) {
            result.add(sourceLines.get(i));
        }

        result.add(new VisualLine(position, position, top, height, new double[]{0.0}));

        for (int i = insertIndex; i < sourceLines.size(); i++) {
            VisualLine line = sourceLines.get(i);
            double adjustedTop = shiftFollowing ? line.top() + height : line.top();
            result.add(new VisualLine(line.start(), line.end(), adjustedTop, line.height(), line.boundaries()));
        }

        return result;
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
            return indexForX(lines.getFirst(), 0);
        }

        for (VisualLine line : lines) {
            if (y < line.top() + line.height()) {
                return indexForX(line, x);
            }
        }

        return indexForX(lines.getLast(), Double.MAX_VALUE);
    }

    static int lineIndexForCaret(List<VisualLine> lines, int caret) {
        int fallback = lines.size() - 1;
        for (int i = 0; i < lines.size(); i++) {
            VisualLine line = lines.get(i);
            if (caret < line.start()) {
                return Math.max(0, i - 1);
            }

            if (caret > line.end()) {
                fallback = i;
                continue;
            }

            // At shared boundaries (especially empty lines), prefer the latest matching line
            // so caret positions can resolve to visually empty lines.
            int candidate = i;
            while (candidate + 1 < lines.size()) {
                VisualLine next = lines.get(candidate + 1);
                if (caret < next.start() || caret > next.end()) {
                    break;
                }
                candidate++;
            }
            return candidate;
        }
        return fallback;
    }

    static double xForIndex(VisualLine line, int index) {
        int offset = Math.clamp(index - line.start(), 0, line.length());
        return line.boundaries()[offset];
    }

    static int indexForX(VisualLine line, double x) {
        if (x <= line.minX()) {
            return line.start();
        }
        if (x >= line.maxX()) {
            return line.end();
        }
        double[] boundaries = line.boundaries();
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
                || Optional.<Font>empty().map(Font::isBold).orElse(false);

        Object fontStyle = resolveAttribute(attributes, styles, Style.FONT_STYLE);
        boolean italicAtCaret = styles.contains(Style.ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_ITALIC)
                || Objects.equals(fontStyle, Style.FONT_STYLE_VALUE_OBLIQUE)
                || Optional.<Font>empty().map(Font::isItalic).orElse(false);

        boolean underlineAtCaret = styles.contains(Style.UNDERLINE)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_UNDERLINE), Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)
                || Optional.<Font>empty().map(Font::isUnderline).orElse(false);

        boolean strikeThroughAtCaret = styles.contains(Style.LINE_THROUGH)
                || Objects.equals(resolveAttribute(attributes, styles, Style.TEXT_DECORATION_LINE_THROUGH), Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)
                || Optional.<Font>empty().map(Font::isStrikeThrough).orElse(false);

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

    private static @Nullable Color resolveColor(TextAttributes attributes, List<Style> styles) {
        return resolveAttribute(attributes, styles, Style.COLOR) instanceof Color color ? color : null;
    }

    private static @Nullable String resolveFontFamily(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_FAMILIES);
        if (value instanceof List<?> families && !families.isEmpty()) {
            Object family = families.getFirst();
            if (family != null) {
                return family.toString();
            }
        }
        return null;
    }

    private static double resolveFontSize(TextAttributes attributes, List<Style> styles) {
        Object value = resolveAttribute(attributes, styles, Style.FONT_SIZE);
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return Optional.<Font>empty().map(Font::getSizeInPoints).orElse(0.0f);
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
        return text == null ? RichText.emptyText() : text;
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

        double minX() {
            return boundaries.length == 0 ? 0 : boundaries[0];
        }

        double maxX() {
            return boundaries.length == 0 ? 0 : boundaries[boundaries.length - 1];
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
