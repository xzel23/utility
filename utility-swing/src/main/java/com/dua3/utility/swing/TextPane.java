package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextPane;
import org.jspecify.annotations.Nullable;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Swing rich-text viewer.
 *
 * <p>The Swing implementation stores and edits text using a {@link StyledDocument}.
 * {@link #getText()} returns the current document text as {@link RichText}.
 */
public class TextPane extends JScrollPane implements RichTextPane {

    private final RichTextTextPane textComponent = new RichTextTextPane();
    protected final RichTextEditorModel model;
    private boolean wrapText;
    private Font textFont = FontUtil.getInstance().getDefaultFont();
    private Consumer<URI> hyperlinkHandler = TextPane::openUriUsingDesktop;
    private boolean callbacksEnabled;

    /**
     * Creates an empty text pane.
     */
    public TextPane() {
        this(null);
    }

    /**
     * Creates a text pane with initial text.
     *
     * @param text initial text
     */
    public TextPane(@Nullable CharSequence text) {
        model = new RichTextEditorModel(text);
        setViewportView(textComponent);
        setWrapText(false);
        textComponent.setEditable(false);
        textComponent.setFocusable(false);
        textComponent.setFont(AwtFontUtil.getInstance().convert(textFont));
        applyTextModelToDocument();
        callbacksEnabled = true;
    }

    /**
     * Returns the underlying Swing text component.
     *
     * @return underlying text component
     */
    public final JTextPane getTextComponent() {
        return textComponent;
    }

    @Override
    public RichText getText() {
        return model.getText();
    }

    @Override
    public final void setText(@Nullable CharSequence value) {
        setTextInternal(value == null ? RichText.emptyText() : RichText.valueOf(value), true);
    }

    @Override
    public boolean isWrapText() {
        return wrapText;
    }

    @Override
    public final void setWrapText(boolean value) {
        wrapText = value;
        textComponent.setWrapText(value);
        setHorizontalScrollBarPolicy(value ? HORIZONTAL_SCROLLBAR_NEVER : HORIZONTAL_SCROLLBAR_AS_NEEDED);
        revalidate();
        repaint();
    }

    @Override
    public Font getTextFont() {
        return textFont;
    }

    @Override
    public final void setTextFont(Font value) {
        textFont = Objects.requireNonNull(value);
        textComponent.setFont(AwtFontUtil.getInstance().convert(textFont));
        applyTextModelToDocument();
        if (callbacksEnabled) {
            onDocumentReplaced(textComponent.getStyledDocument());
        }
    }

    @Override
    public Consumer<URI> getHyperlinkHandler() {
        return hyperlinkHandler;
    }

    @Override
    public void setHyperlinkHandler(Consumer<URI> handler) {
        hyperlinkHandler = Objects.requireNonNull(handler);
    }

    /**
     * Called after the document backing the text component has been replaced.
     *
     * @param document new document
     */
    protected void onDocumentReplaced(StyledDocument document) {
        // default no-op
    }

    /**
     * Replaces the text model.
     *
     * @param value text value
     * @param preserveHistory true to preserve history in the model
     */
    protected final void setTextModel(RichText value, boolean preserveHistory) {
        model.setText(value, preserveHistory);
    }

    private void setTextInternal(RichText value, boolean notifyDocumentReplaced) {
        model.setText(value);
        applyTextModelToDocument();
        if (notifyDocumentReplaced && callbacksEnabled) {
            onDocumentReplaced(textComponent.getStyledDocument());
        }
    }

    protected final void applyTextModelToDocument() {
        StyledDocument document = createStyledDocument(model.getText());
        textComponent.setStyledDocument(document);
    }

    /**
     * Creates a Swing styled document for the given rich text.
     *
     * @param value source text
     * @return converted styled document
     */
    protected StyledDocument createStyledDocument(RichText value) {
        try {
            return StyledDocumentConverter
                    .create(StyledDocumentConverter.defaultFont(textFont))
                    .convert(value);
        } catch (RuntimeException ex) {
            // keep control usable even if conversion fails for unsupported attributes
            DefaultStyledDocument fallback = new DefaultStyledDocument();
            try {
                fallback.insertString(0, value.toString(), null);
            } catch (BadLocationException e) {
                throw new IllegalStateException("failed to create fallback document", e);
            }
            return fallback;
        }
    }

    private static void openUriUsingDesktop(URI uri) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        try {
            String scheme = uri.getScheme();
            if ("mailto".equalsIgnoreCase(scheme) && desktop.isSupported(Desktop.Action.MAIL)) {
                desktop.mail(uri);
            } else if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // ignore failures from user-supplied or unsupported URI schemes
        }
    }

    private static final class RichTextTextPane extends JTextPane {
        private boolean wrapText;

        void setWrapText(boolean value) {
            wrapText = value;
            revalidate();
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return wrapText;
        }
    }
}
