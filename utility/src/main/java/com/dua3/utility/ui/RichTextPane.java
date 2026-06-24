package com.dua3.utility.ui;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Toolkit-agnostic API for rich-text panes.
 *
 * <p>Implemented by UI toolkit specific controls (for example JavaFX and Swing).
 */
public interface RichTextPane {

    /**
     * Returns the current rich text.
     *
     * @return current text
     */
    RichText getText();

    /**
     * Sets text content.
     *
     * @param value text or {@code null} for empty text
     */
    void setText(@Nullable CharSequence value);

    /**
     * Returns whether line wrapping is enabled.
     *
     * @return true if wrapping is enabled
     */
    boolean isWrapText();

    /**
     * Enables or disables line wrapping.
     *
     * @param value true to enable wrapping
     */
    void setWrapText(boolean value);

    /**
     * Returns the rendering font.
     *
     * @return current font
     */
    Font getFont();

    /**
     * Sets the rendering font.
     *
     * @param value font
     */
    void setFont(Font value);

    /**
     * Returns the hyperlink handler used for inline hyperlinks.
     *
     * @return hyperlink handler
     */
    Consumer<URI> getHyperlinkHandler();

    /**
     * Sets the hyperlink handler used for inline hyperlinks.
     *
     * @param handler hyperlink handler
     */
    void setHyperlinkHandler(Consumer<URI> handler);
}
