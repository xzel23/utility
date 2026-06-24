package com.dua3.utility.swing;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.jspecify.annotations.Nullable;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Swing rich-text editor pane backed by a shared {@code RichTextEditorModel}.
 */
public class TextEditorPane extends TextPane {

    private final DocumentFilter modelUpdatingFilter = new ModelUpdatingFilter();
    private boolean syncingFromModel;

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
        getTextComponent().setEditable(true);
        getTextComponent().setFocusable(true);
        getTextComponent().addCaretListener(evt -> syncModelSelectionFromComponent());
        onDocumentReplaced(getTextComponent().getStyledDocument());
        syncSelectionFromModel();
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
            refreshDocumentFromModel();
        }
    }

    /**
     * Redo last undone edit.
     */
    public void redo() {
        if (model.redo()) {
            refreshDocumentFromModel();
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
            refreshDocumentFromModel();
        } else {
            syncSelectionFromModel();
        }
    }

    /**
     * Replaces the current selection with text.
     *
     * @param value replacement text or {@code null}
     */
    public void replaceSelection(@Nullable CharSequence value) {
        syncModelSelectionFromComponent();
        if (model.replaceSelection(value)) {
            refreshDocumentFromModel();
        } else {
            syncSelectionFromModel();
        }
    }

    /**
     * Marks the current selection as bold.
     *
     * @param value true to apply bold, false to remove
     */
    public void markBold(boolean value) {
        syncModelSelectionFromComponent();
        if (model.markBold(value)) {
            refreshDocumentFromModel();
        }
        applyTypingAttributes(attributes -> StyleConstants.setBold(attributes, value));
    }

    /**
     * Toggles bold style on the current selection.
     */
    public void markBold() {
        markBold(!StyleConstants.isBold(getTextComponent().getCharacterAttributes()));
    }

    /**
     * Marks the current selection as italic.
     *
     * @param value true to apply italic, false to remove
     */
    public void markItalic(boolean value) {
        syncModelSelectionFromComponent();
        if (model.markItalic(value)) {
            refreshDocumentFromModel();
        }
        applyTypingAttributes(attributes -> StyleConstants.setItalic(attributes, value));
    }

    /**
     * Toggles italic style on the current selection.
     */
    public void markItalic() {
        markItalic(!StyleConstants.isItalic(getTextComponent().getCharacterAttributes()));
    }

    /**
     * Marks the current selection as underlined.
     *
     * @param value true to apply underline, false to remove
     */
    public void markUnderline(boolean value) {
        syncModelSelectionFromComponent();
        if (model.markUnderline(value)) {
            refreshDocumentFromModel();
        }
        applyTypingAttributes(attributes -> StyleConstants.setUnderline(attributes, value));
    }

    /**
     * Toggles underline style on the current selection.
     */
    public void markUnderline() {
        markUnderline(!StyleConstants.isUnderline(getTextComponent().getCharacterAttributes()));
    }

    /**
     * Marks the current selection with strike-through.
     *
     * @param value true to apply strike-through, false to remove
     */
    public void markStrikeThrough(boolean value) {
        syncModelSelectionFromComponent();
        if (model.markStrikeThrough(value)) {
            refreshDocumentFromModel();
        }
        applyTypingAttributes(attributes -> StyleConstants.setStrikeThrough(attributes, value));
    }

    /**
     * Toggles strike-through style on the current selection.
     */
    public void markStrikeThrough() {
        markStrikeThrough(!StyleConstants.isStrikeThrough(getTextComponent().getCharacterAttributes()));
    }

    /**
     * Returns current caret position.
     *
     * @return caret position
     */
    public int getCaretPosition() {
        return getTextComponent().getCaretPosition();
    }

    /**
     * Sets caret position.
     *
     * @param position caret position
     */
    public void setCaretPosition(int position) {
        getTextComponent().setCaretPosition(Math.clamp(position, 0, getText().length()));
        syncModelSelectionFromComponent();
    }

    /**
     * Selects the text range {@code [start, end)}.
     *
     * @param start selection start
     * @param end selection end
     */
    public void selectRange(int start, int end) {
        getTextComponent().select(start, end);
        syncModelSelectionFromComponent();
    }

    /**
     * Returns selected text as {@link RichText}.
     *
     * @return selected text
     */
    public RichText getSelectedText() {
        syncModelSelectionFromComponent();
        return model.getSelectedText();
    }

    @Override
    protected void onDocumentReplaced(StyledDocument document) {
        if (document instanceof AbstractDocument abstractDocument) {
            abstractDocument.setDocumentFilter(modelUpdatingFilter);
        }
    }

    private void refreshDocumentFromModel() {
        syncingFromModel = true;
        try {
            applyTextModelToDocument();
            onDocumentReplaced(getTextComponent().getStyledDocument());
            syncSelectionFromModel();
        } finally {
            syncingFromModel = false;
        }
    }

    private void syncSelectionFromModel() {
        syncingFromModel = true;
        try {
            var selection = model.getSelection();
            getTextComponent().select(selection.start(), selection.end());
        } finally {
            syncingFromModel = false;
        }
    }

    private void syncModelSelectionFromComponent() {
        if (syncingFromModel) {
            return;
        }
        model.selectRange(getTextComponent().getSelectionStart(), getTextComponent().getSelectionEnd());
    }

    private void applyTypingAttributes(Consumer<MutableAttributeSet> update) {
        MutableAttributeSet attributes = new SimpleAttributeSet(getTextComponent().getCharacterAttributes());
        update.accept(attributes);
        getTextComponent().setCharacterAttributes(attributes, false);
    }

    private RichText extractInsertedRichText(DocumentFilter.FilterBypass fb, int start, int end) throws BadLocationException {
        if (start >= end) {
            return RichText.emptyText();
        }

        if (!(fb.getDocument() instanceof StyledDocument styledDocument)) {
            return RichText.valueOf(fb.getDocument().getText(start, end - start));
        }

        RichTextBuilder builder = new RichTextBuilder(end - start);
        int position = start;
        while (position < end) {
            var element = styledDocument.getCharacterElement(position);
            int runEnd = Math.min(end, element.getEndOffset());
            String chunk = styledDocument.getText(position, runEnd - position);
            builder.append(applySwingStyles(RichText.valueOf(chunk), element.getAttributes()));
            position = runEnd;
        }

        return builder.toRichText();
    }

    private static RichText applySwingStyles(RichText text, AttributeSet attributes) {
        RichText styled = text;
        int length = styled.length();
        if (StyleConstants.isBold(attributes)) {
            styled = styled.apply(Style.BOLD, 0, length);
        }
        if (StyleConstants.isItalic(attributes)) {
            styled = styled.apply(Style.ITALIC, 0, length);
        }
        if (StyleConstants.isUnderline(attributes)) {
            styled = styled.apply(Style.UNDERLINE, 0, length);
        }
        if (StyleConstants.isStrikeThrough(attributes)) {
            styled = styled.apply(Style.LINE_THROUGH, 0, length);
        }

        return styled;
    }

    private final class ModelUpdatingFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws javax.swing.text.BadLocationException {
            replace(fb, offset, length, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                throws javax.swing.text.BadLocationException {
            if (syncingFromModel) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            String replacement = Objects.requireNonNullElse(text, "");
            super.replace(fb, offset, length, replacement, attrs);
            model.replaceText(offset, offset + length, extractInsertedRichText(fb, offset, offset + replacement.length()));
            syncModelSelectionFromComponent();
        }
    }
}
