package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.ToRichText;
import com.dua3.utility.ui.DetachableNode;
import com.dua3.utility.ui.IndexRange;
import com.dua3.utility.ui.RichTextEditorPane;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextPaneLayoutHelper;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VisualLine;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Text editor control based on {@link TextPane}.
 *
 * <p>This class currently provides a stub editing API surface, modeled after JavaFX {@code TextInputControl},
 * but operating on {@link RichText}. Real editing behavior will be added incrementally.
 *
 * <p>For observing live editor mutations, use {@link #documentVersionProperty()} and fetch current text via
 * {@link #getDocumentText()} (for example with
 * {@code Bindings.createObjectBinding(editor::getDocumentText, editor.documentVersionProperty())}).
 * {@link #textProperty()} exposes {@link ToRichText} snapshots and is updated on
 * each document change.
 */
public class TextEditorPane extends TextPane implements InputControl<RichText>, RichTextEditorPane {
    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);
    private final BooleanProperty enterKeyInsertsNewline = new SimpleBooleanProperty(this, "enterKeyInsertsNewline", true);
    private final ObjectProperty<DetachableNode.Location> toolbarLocation = new SimpleObjectProperty<>(this, "toolbarLocation", DetachableNode.Location.EMBEDDED);
    private final ObjectProperty<@Nullable Parent> toolbarApplicationParent = new SimpleObjectProperty<>(this, "toolbarApplicationParent", null);
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
    private final ObjectProperty<@Nullable Color> backgroundColor = new SimpleObjectProperty<>(this, "backgroundColor");
    private final StringProperty fontFamily = new SimpleStringProperty(this, "fontFamily");
    private final DoubleProperty fontSize = new SimpleDoubleProperty(this, "fontSize", 0.0);
    private final ObjectProperty<RichText> committedValue;
    private final RichText defaultValue;
    private final InputControlState<RichText> state;
    private final SelectionModel selectionModel = new SelectionModel();
    private @Nullable ScrollPane scrollPane;
    private boolean updatingPropertiesFromText;
    private boolean syncingTextPropertyFromDocument;
    private final ReadOnlyObjectWrapper<RichText> document = new ReadOnlyObjectWrapper<>(this, "documentText", RichText.emptyText());
    private final ReadOnlyLongWrapper documentVersion = new ReadOnlyLongWrapper(this, "documentVersion", 0L);
    private final RichTextEditorModel sharedModel;
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

        RichText initial = normalizeIncomingText(textProperty().get());
        document.set(initial);
        this.sharedModel = new RichTextEditorModel(initial, MAX_HISTORY_SIZE, FxFontUtil.getInstance());
        sharedModel.setPageWidthProvider(model -> {
            double fallback = getWidth() - snappedLeftInset() - snappedRightInset();
            return Double.isFinite(fallback) && fallback > 1.0 ? fallback : 1.0;
        });
        sharedModel.setPageHeightProvider(model -> resolvePageHeightFromView());
        sharedModel.setWrapWidthProvider(model -> resolveCurrentWrapWidthFromView());
        this.defaultValue = initial;
        this.committedValue = new SimpleObjectProperty<>(this, "value", initial);
        this.state = new ObjectInputControlState<>(committedValue, () -> defaultValue, value -> Optional.empty());

        committedValue.addListener((obs, oldValue, newValue) -> applyCommittedValue(newValue));

        length.set(initial.length());
        setSelectionState(0, 0);
        initFormatPropertyListeners();

        textProperty().addListener((obs, oldValue, newValue) -> {
            if (syncingTextPropertyFromDocument) {
                return;
            }

            RichText oldText = normalizeIncomingText(oldValue);
            RichText currentText = normalizeIncomingText(newValue);
            if (!Objects.equals(oldText, currentText)) {
                sharedModel.setText(currentText, true);
                onModelTextMutated(false);
                return;
            }
            length.set(sharedModel.length());
            syncSelectionFromModel();
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

    /**
     * Returns the current rich-text document.
     *
     * <p>This returns the materialized document model.
     * {@link #textProperty()} publishes lazy {@link ToRichText} snapshots.
     *
     * @return current document text
     */
    public final RichText getDocumentText() {
        return sharedModel.getText();
    }

    /**
     * Read-only property exposing the materialized editor document snapshot.
     *
     * <p>Use {@link #documentVersionProperty()} as the edit-change signal and
     * {@link #getDocumentText()} to retrieve the current document text.
     * This snapshot property is not intended as the primary edit notification channel.
     *
     * @return read-only document text property
     */
    public final ReadOnlyObjectProperty<RichText> documentTextProperty() {
        return document.getReadOnlyProperty();
    }

    /**
     * Returns the current document version.
     *
     * <p>The value increments whenever the editor document changes.
     *
     * @return document version
     */
    public final long getDocumentVersion() {
        return documentVersion.get();
    }

    /**
     * Read-only property exposing the document version.
     *
     * <p>Use this property as a lightweight invalidation signal for document changes.
     *
     * @return read-only document version property
     */
    public final ReadOnlyLongProperty documentVersionProperty() {
        return documentVersion.getReadOnlyProperty();
    }

    private void markDocumentChanged() {
        documentVersion.set(documentVersion.get() + 1L);
    }

    private RichText materializeDocumentText() {
        return sharedModel.getText();
    }

    private RichText readDocumentRange(int start, int end) {
        return sharedModel.getText(start, end);
    }

    private void onModelTextMutated(boolean syncTextProperty) {
        document.set(sharedModel.getText());
        length.set(sharedModel.length());
        markDocumentChanged();
        if (syncTextProperty) {
            syncTextPropertyWithDocumentSnapshot();
        }
        updateHistoryState();
        syncSelectionFromModel();
    }

    private void syncSelectionFromModel() {
        selectionModel.selectRange(sharedModel.getAnchor(), sharedModel.getCaretPosition());
        updatePropertiesFromCaretPosition();
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
                sharedModel.resetPreferredCaretX();
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
                sharedModel.resetPreferredCaretX();
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
                sharedModel.resetPreferredCaretX();
                if (shift) {
                    selectHome();
                } else {
                    home();
                }
                evt.consume();
            }
            case END -> {
                sharedModel.resetPreferredCaretX();
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
            if (isEnterKeyInsertsNewline()) {
                replaceSelection(toRichTextWithCurrentProperties("\n"));
                evt.consume();
            }
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
        backgroundColor.addListener((obs, oldValue, newValue) -> onAttributePropertyChanged(Style.BACKGROUND_COLOR, newValue));
        fontFamily.addListener((obs, oldValue, newValue) -> onFontFamilyChanged(newValue));
        fontSize.addListener((obs, oldValue, newValue) -> onFontSizeChanged(newValue.doubleValue()));
    }

    private void onAttributePropertyChanged(String name, @Nullable Object value) {
        if (updatingPropertiesFromText || getSelection().getLength() == 0 || value == null) {
            return;
        }
        if (sharedModel.applyAttributeToSelection(name, value)) {
            onModelTextMutated(true);
        }
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

    @Override
    public final boolean isEnterKeyInsertsNewline() {
        return enterKeyInsertsNewline.get();
    }

    @Override
    public final void setEnterKeyInsertsNewline(boolean value) {
        enterKeyInsertsNewline.set(value);
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
    public final ObjectProperty<DetachableNode.Location> toolbarLocationProperty() {
        return toolbarLocation;
    }

    /**
     * Indicates whether the toolbar should be visible.
     *
     * @return {@code true} if toolbar is visible
     */
    public final DetachableNode.Location getToolbarLocation() {
        return toolbarLocation.getValue();
    }

    /**
     * Shows or hides the toolbar.
     *
     * @param value {@code true} to show the toolbar
     */
    public final void setToolbarLocation(DetachableNode.Location value) {
        toolbarLocation.setValue(value);
    }

    /**
     * Toolbar application parent property.
     * <p>
     * This property controls what parent node to set for the toolbar when the toolbar location is set to
     * {@link com.dua3.utility.ui.DetachableNode.Location#APPLICATION}.
     *
     * @return the toolbar application parent property
     */
    public final ObjectProperty<@Nullable Parent> toolbarApplicationParentProperty() {
        return toolbarApplicationParent;
    }

    /**
     * Retrieves the parent component to use as the toolbar's parent when the toolbar location is set to
     * {@link com.dua3.utility.ui.DetachableNode.Location#APPLICATION}.
     *
     * @return the Parent object to be set as the toolbar's parent when the location is set to
     *         {@link com.dua3.utility.ui.DetachableNode.Location#APPLICATION}.
     */
    public final @Nullable Parent getToolbarApplicationParent() {
        return toolbarApplicationParent.getValue();
    }

    /**
     * Sets the parent to show the toolbar in when the location is set to
     * {@link com.dua3.utility.ui.DetachableNode.Location#APPLICATION}.
     *
     * @param value the Parent object to be set as the toolbar's parent when the location is set to
     *              {@link com.dua3.utility.ui.DetachableNode.Location#APPLICATION}.
     */
    public final void setToolbarApplicationParent(@Nullable Parent value) {
        toolbarApplicationParent.setValue(value);
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
     * Background-color property for current formatting state.
     *
     * @return background-color property
     */
    public final ObjectProperty<@Nullable Color> backgroundColorProperty() {
        return backgroundColor;
    }

    /**
     * Returns current text background color setting.
     *
     * @return current background color or {@code null}
     */
    public final @Nullable Color getBackgroundColor() {
        return backgroundColor.get();
    }

    /**
     * Sets text background color for subsequent input or current selection formatting.
     *
     * @param value background color, or {@code null}
     */
    public final void setBackgroundColor(@Nullable Color value) {
        backgroundColor.set(value);
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

    @Override
    public RichText getText() {
        return materializeDocumentText();
    }

    /**
     * Returns a text slice between two offsets.
     *
     * @param start start offset (inclusive)
     * @param end end offset (exclusive)
     * @return selected text slice
     */
    public RichText getText(int start, int end) {
        return readDocumentRange(start, end);
    }

    /**
     * Returns a snapshot stream over the current logical lines.
     *
     * <p>The returned stream reflects the document state at invocation time.
     * Subsequent edits do not affect the stream contents.
     *
     * @return stream of rich-text lines
     */
    public Stream<RichText> lines() {
        return sharedModel.snapshotLines().stream();
    }

    /**
     * Appends the current plain-text document contents to the given appendable.
     *
     * <p>This method writes line by line from the editor's logical document
     * representation and avoids building a full-document {@link RichText} instance.
     *
     * @param appendable target appendable
     * @throws IOException if writing fails
     */
    public void appendTo(Appendable appendable) throws IOException {
        sharedModel.appendPlainTextTo(appendable);
    }

    private void syncTextPropertyWithDocumentSnapshot() {
        ToRichText snapshot = sharedModel.createLazySnapshot();
        syncingTextPropertyFromDocument = true;
        try {
            textProperty().set(snapshot);
        } finally {
            syncingTextPropertyFromDocument = false;
        }
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
        if (sharedModel.deletePreviousChar()) {
            onModelTextMutated(true);
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
        if (sharedModel.deleteNextChar()) {
            onModelTextMutated(true);
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
     * Deletes text inside the given JavaFX range.
     *
     * @param range JavaFX range to delete
     */
    public void deleteText(javafx.scene.control.IndexRange range) {
        deleteText(toIndexRange(range));
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
     * Replaces the given JavaFX range with plain text.
     *
     * @param range JavaFX range
     * @param text replacement text
     */
    public void replaceText(javafx.scene.control.IndexRange range, @Nullable CharSequence text) {
        replaceText(toIndexRange(range), text);
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
     * Replaces the given JavaFX range with text rendered using a font.
     *
     * @param range JavaFX range
     * @param text replacement text
     * @param font font to apply
     */
    public void replaceText(javafx.scene.control.IndexRange range, @Nullable CharSequence text, Font font) {
        replaceText(toIndexRange(range), text, font);
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
        if (sharedModel.replaceText(start, end, replacement)) {
            onModelTextMutated(true);
        } else {
            syncSelectionFromModel();
        }
    }

    /**
     * Replaces the current selection.
     *
     * @param replacement replacement text
     */
    public void replaceSelection(@Nullable CharSequence replacement) {
        if (sharedModel.replaceSelection(Objects.requireNonNullElse(replacement, ""))) {
            onModelTextMutated(true);
        } else {
            syncSelectionFromModel();
        }
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
     * Selects the given JavaFX range.
     *
     * @param range JavaFX range
     */
    public void selectRange(javafx.scene.control.IndexRange range) {
        setSelectionState(range.getStart(), range.getEnd());
    }

    /**
     * Selects the full document.
     */
    public void selectAll() {
        sharedModel.selectAll();
        syncSelectionFromModel();
    }

    /**
     * Clears the current selection.
     */
    public void deselect() {
        sharedModel.deselect();
        syncSelectionFromModel();
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
        sharedModel.positionCaret(pos);
        syncSelectionFromModel();
    }

    /**
     * Moves caret while keeping current anchor.
     *
     * @param pos new caret position
     */
    public void selectPositionCaret(int pos) {
        sharedModel.selectPositionCaret(pos);
        syncSelectionFromModel();
    }

    /**
     * Moves caret to document start.
     */
    public void home() {
        sharedModel.home();
        syncSelectionFromModel();
    }

    /**
     * Moves caret to document end.
     */
    public void end() {
        sharedModel.end();
        syncSelectionFromModel();
    }

    /**
     * Moves caret one character forward.
     */
    public void forward() {
        sharedModel.forward();
        syncSelectionFromModel();
    }

    /**
     * Moves caret one character backward.
     */
    public void backward() {
        sharedModel.backward();
        syncSelectionFromModel();
    }

    /**
     * Moves caret to start of previous word.
     */
    public void previousWord() {
        sharedModel.previousWord();
        syncSelectionFromModel();
    }

    /**
     * Moves caret to start of next word.
     */
    public void nextWord() {
        sharedModel.nextWord();
        syncSelectionFromModel();
    }

    /**
     * Moves caret to end of next word.
     */
    public void endOfNextWord() {
        sharedModel.endOfNextWord();
        syncSelectionFromModel();
    }

    /**
     * Extends selection one character backward.
     */
    public void selectBackward() {
        sharedModel.selectBackward();
        syncSelectionFromModel();
    }

    /**
     * Extends selection one character forward.
     */
    public void selectForward() {
        sharedModel.selectForward();
        syncSelectionFromModel();
    }

    /**
     * Extends selection to start of previous word.
     */
    public void selectPreviousWord() {
        sharedModel.selectPreviousWord();
        syncSelectionFromModel();
    }

    /**
     * Extends selection to start of next word.
     */
    public void selectNextWord() {
        sharedModel.selectNextWord();
        syncSelectionFromModel();
    }

    /**
     * Extends selection to end of next word.
     */
    public void selectEndOfNextWord() {
        sharedModel.selectEndOfNextWord();
        syncSelectionFromModel();
    }

    /**
     * Extends selection to document start.
     */
    public void selectHome() {
        sharedModel.selectHome();
        syncSelectionFromModel();
    }

    /**
     * Extends selection to document end.
     */
    public void selectEnd() {
        sharedModel.selectEnd();
        syncSelectionFromModel();
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
        if (sharedModel.undo()) {
            onModelTextMutated(true);
        }
    }

    /**
     * Performs one redo step.
     */
    public void redo() {
        if (sharedModel.redo()) {
            onModelTextMutated(true);
        }
    }

    /**
     * Applies a style to the current selection.
     *
     * @param style style to apply
     */
    public void apply(Style style) {
        if (sharedModel.applyStyle(style)) {
            onModelTextMutated(true);
        }
    }

    /**
     * Enables or disables a style on the current selection.
     *
     * @param style style to toggle
     * @param enabled {@code true} to apply, {@code false} to remove
     */
    public void setStyle(Style style, boolean enabled) {
        if (sharedModel.setStyle(style, enabled)) {
            onModelTextMutated(true);
        }
    }

    /**
     * Removes a style from the current selection.
     *
     * @param style style to remove
     */
    public void remove(Style style) {
        if (sharedModel.removeStyle(style)) {
            onModelTextMutated(true);
        }
    }

    /**
     * Sets bold formatting on current selection.
     *
     * @param enabled {@code true} for bold, {@code false} for normal weight
     */
    public void markBold(boolean enabled) {
        if (sharedModel.markAttribute(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD, Style.FONT_WEIGHT_VALUE_NORMAL, enabled)) {
            onModelTextMutated(true);
        }
    }

    /**
     * Sets italic formatting on current selection.
     *
     * @param enabled {@code true} for italic, {@code false} for normal style
     */
    public void markItalic(boolean enabled) {
        if (sharedModel.markAttribute(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC, Style.FONT_STYLE_VALUE_NORMAL, enabled)) {
            onModelTextMutated(true);
        }
    }

    /**
     * Sets underline formatting on current selection.
     *
     * @param enabled {@code true} to underline, {@code false} to remove underline
     */
    public void markUnderline(boolean enabled) {
        if (sharedModel.markAttribute(
                Style.TEXT_DECORATION_UNDERLINE,
                Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE,
                Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE,
                enabled
        )) {
            onModelTextMutated(true);
        }
    }

    /**
     * Sets strike-through formatting on current selection.
     *
     * @param enabled {@code true} to strike through, {@code false} to remove
     */
    public void markStrikeThrough(boolean enabled) {
        if (sharedModel.markAttribute(
                Style.TEXT_DECORATION_LINE_THROUGH,
                Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE,
                Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE,
                enabled
        )) {
            onModelTextMutated(true);
        }
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
        RichText committed = normalizeIncomingText(materializeDocumentText());
        if (!Objects.equals(get(), committed)) {
            set(committed);
        } else {
            state.validate();
        }
    }

    private int hitTest(MouseEvent evt) {
        Point2D p = toContentPoint(evt);
        List<VisualLine> lines = buildVisualLines(currentWrapWidth());
        return RichTextVisualLayoutHelper.indexForPoint(lines, p.getX(), p.getY());
    }

    private void selectWordAt(int pos) {
        IndexRange range = sharedModel.wordSelectionRangeAt(pos);
        setSelectionState(range.start(), range.end());
        sharedModel.resetPreferredCaretX();
    }

    private void selectLineAt(int pos) {
        IndexRange range = sharedModel.lineRangeAt(pos);
        setSelectionState(range.start(), range.end());
        sharedModel.resetPreferredCaretX();
    }

    private void moveLine(int delta, boolean extendSelection) {
        List<VisualLine> lines = buildVisualLines(currentWrapWidth());
        if (sharedModel.moveLine(lines, delta, extendSelection)) {
            syncSelectionFromModel();
        }
    }

    private void movePage(int delta, boolean extendSelection) {
        List<VisualLine> lines = buildVisualLines(currentWrapWidth());
        if (sharedModel.movePage(lines, delta, extendSelection)) {
            syncSelectionFromModel();
        }
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
        double availableWidth = sharedModel.resolveAvailableWidth(wrapWidth);
        Font baseFont = getFont();
        return sharedModel.buildVisualLines(
                availableWidth,
                isWrapText(),
                baseFont,
                blockText -> {
                    RichTextPaneLayoutHelper.Layout<?> layout = createLayout(blockText, availableWidth);
                    return new RichTextVisualLayoutHelper.BlockLayout(
                            layout.renderLines(),
                            layout.height(),
                            layout.layoutTextData()::layoutToSourcePosition
                    );
                }
        );
    }

    private double currentWrapWidth() {
        return sharedModel.currentWrapWidth();
    }

    private void setSelectionState(int anchorPos, int caretPos) {
        sharedModel.selectRange(anchorPos, caretPos);
        syncSelectionFromModel();
    }

    private void updatePropertiesFromCaretPosition() {
        RichTextEditorModel.CaretProperties properties = sharedModel.resolveCaretProperties(getFont());
        if (properties == null) {
            return;
        }

        updatingPropertiesFromText = true;
        try {
            setBold(properties.bold());
            setItalic(properties.italic());
            setUnderline(properties.underline());
            setStrikeThrough(properties.strikeThrough());
            setTextColor(properties.textColor());
            setBackgroundColor(properties.backgroundColor());
            setFontFamily(properties.fontFamily());
            setFontSize(properties.fontSize());
        } finally {
            updatingPropertiesFromText = false;
        }
    }

    private static RichText toRichText(@Nullable CharSequence text, Font font) {
        return RichTextEditorModel.toRichText(text, font);
    }

    private RichText toRichTextWithCurrentProperties(@Nullable CharSequence text) {
        return RichTextEditorModel.toRichText(
                text,
                isBold(),
                isItalic(),
                isUnderline(),
                isStrikeThrough(),
                getTextColor(),
                getBackgroundColor(),
                getFontFamily(),
                getFontSize()
        );
    }

    private RichText normalizeIncomingText(@Nullable ToRichText text) {
        if (text == null) {
            return RichText.emptyText();
        }

        return text.toRichText();
    }

    private double resolveCurrentWrapWidthFromView() {
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

    private double resolvePageHeightFromView() {
        ScrollPane sp = getScrollPane();
        if (sp != null) {
            double viewportHeight = sp.getViewportBounds().getHeight();
            if (Double.isFinite(viewportHeight) && viewportHeight > 1.0) {
                return viewportHeight;
            }
        }

        double fallback = getHeight() - snappedTopInset() - snappedBottomInset();
        return Double.isFinite(fallback) && fallback > 1.0 ? fallback : 0.0;
    }

    private static IndexRange toIndexRange(javafx.scene.control.IndexRange range) {
        return new IndexRange(range.getStart(), range.getEnd());
    }

    private void clearHistory() {
        sharedModel.clearHistory();
        updateHistoryState();
    }

    private void applyCommittedValue(@Nullable RichText value) {
        RichText committed = normalizeIncomingText(value);
        if (Objects.equals(sharedModel.getText(), committed)) {
            return;
        }

        sharedModel.setText(committed);
        sharedModel.selectRange(0, 0);
        onModelTextMutated(true);
        clearHistory();
    }

    private void updateHistoryState() {
        undoable.set(sharedModel.canUndo());
        redoable.set(sharedModel.canRedo());
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

}
