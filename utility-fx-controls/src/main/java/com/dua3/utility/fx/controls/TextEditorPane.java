package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.ToRichText;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Text editor control based on {@link TextPane}.
 *
 * <p>This class currently provides a stub editing API surface, modeled after JavaFX {@code TextInputControl},
 * but operating on {@link RichText}. Real editing behavior will be added incrementally.
 */
public class TextEditorPane extends TextPane implements InputControl<RichText> {

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);
    private final BooleanProperty toolbarVisible = new SimpleBooleanProperty(this, "toolbarVisible", false);
    private final ReadOnlyIntegerWrapper length = new ReadOnlyIntegerWrapper(this, "length", 0);
    private final ReadOnlyObjectWrapper<RichText> selectedText = new ReadOnlyObjectWrapper<>(this, "selectedText", RichText.emptyText());
    private final ReadOnlyObjectWrapper<IndexRange> selection = new ReadOnlyObjectWrapper<>(this, "selection", new IndexRange(0, 0));
    private final ReadOnlyIntegerWrapper anchor = new ReadOnlyIntegerWrapper(this, "anchor", 0);
    private final ReadOnlyIntegerWrapper caretPosition = new ReadOnlyIntegerWrapper(this, "caretPosition", 0);
    private final ReadOnlyBooleanWrapper undoable = new ReadOnlyBooleanWrapper(this, "undoable", false);
    private final ReadOnlyBooleanWrapper redoable = new ReadOnlyBooleanWrapper(this, "redoable", false);
    private final InputControlState<RichText> state;
    private final List<EditState> undoStack = new ArrayList<>();
    private final List<EditState> redoStack = new ArrayList<>();
    private final SelectionModel selectionModel = new SelectionModel();
    private @Nullable ScrollPane scrollPane;
    private boolean inHistoryNavigation;
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

        this.state = new ObjectInputControlState<>(textProperty(), this::getText, value -> Optional.empty());
        getStyleClass().add("text-editor-pane");

        RichText initial = getText();
        length.set(initial.length());
        setSelectionState(0, 0);

        textProperty().addListener((obs, oldValue, newValue) -> {
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
                    replaceSelection("\t");
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
            replaceSelection("\n");
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

        replaceSelection(chars);
        evt.consume();
    }

    @Override
    public InputControlState<RichText> state() {
        return state;
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
        replaceSelection(RichText.emptyText());
    }

    public void paste() {
        if (!isEditable()) {
            return;
        }
        FxUtil.getTextFromClipboard().ifPresent(this::replaceSelection);
    }

    public void undo() {
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

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }

        EditState current = snapshot();
        EditState next = redoStack.removeLast();
        inHistoryNavigation = true;
        try {
            undoStack.add(current);
            applyState(next);
        } finally {
            inHistoryNavigation = false;
        }
        updateHistoryState();
    }

    public void cancelEdit() {
        // TODO implement edit cancel behavior
    }

    public void commitValue() {
        // TODO implement value commit behavior
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
        RichText richText = getText();
        String text = richText.toString();
        Font font = getFont();
        FontUtil fontUtil = FontUtil.getInstance();
        double lineHeight = Math.max(1.0, font.getFontData().height());
        boolean wrap = isWrapText() && Double.isFinite(wrapWidth) && wrapWidth > 1.0;

        List<VisualLine> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add(new VisualLine(0, 0, 0.0, lineHeight, new double[]{0.0}));
            return lines;
        }

        int logicalStart = 0;
        double y = 0.0;
        while (logicalStart <= text.length()) {
            int newline = text.indexOf('\n', logicalStart);
            boolean hasNewline = newline >= 0;
            int logicalEnd = hasNewline ? newline : text.length();

            if (wrap && logicalStart < logicalEnd) {
                int start = logicalStart;
                double x = 0.0;
                List<Double> boundaries = new ArrayList<>();
                boundaries.add(0.0);

                for (int i = logicalStart; i < logicalEnd; i++) {
                    double charWidth = charWidthAt(richText, i, font, fontUtil);
                    if (x + charWidth > wrapWidth && i > start) {
                        lines.add(new VisualLine(start, i, y, lineHeight, toArray(boundaries)));
                        y += lineHeight;

                        start = i;
                        x = 0.0;
                        boundaries.clear();
                        boundaries.add(0.0);
                    }
                    x += charWidth;
                    boundaries.add(x);
                }

                lines.add(new VisualLine(start, logicalEnd, y, lineHeight, toArray(boundaries)));
            } else {
                lines.add(new VisualLine(logicalStart, logicalEnd, y, lineHeight, buildBoundaries(richText, logicalStart, logicalEnd, fontUtil, font)));
            }

            y += lineHeight;

            if (!hasNewline) {
                break;
            }
            logicalStart = newline + 1;
        }

        return lines;
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

    private static double[] buildBoundaries(RichText text, int start, int end, FontUtil fontUtil, Font baseFont) {
        int len = Math.max(0, end - start);
        double[] boundaries = new double[len + 1];
        for (int i = 0; i < len; i++) {
            boundaries[i + 1] = boundaries[i] + charWidthAt(text, start + i, baseFont, fontUtil);
        }
        return boundaries;
    }

    private static double charWidthAt(RichText text, int index, Font baseFont, FontUtil fontUtil) {
        Run run = text.runAt(index);
        Font runFont = fontUtil.deriveFont(baseFont, run.getFontDef());
        return fontUtil.getTextWidth(String.valueOf(text.charAt(index)), runFont);
    }

    private static double[] toArray(List<Double> boundaries) {
        double[] result = new double[boundaries.size()];
        for (int i = 0; i < boundaries.size(); i++) {
            result[i] = boundaries.get(i);
        }
        return result;
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
        if (x <= 0.0) {
            return line.start();
        }
        double[] boundaries = line.boundaries();
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

    private void pushUndoState() {
        if (inHistoryNavigation) {
            return;
        }
        undoStack.add(snapshot());
        trimHistory(undoStack);
        redoStack.clear();
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
