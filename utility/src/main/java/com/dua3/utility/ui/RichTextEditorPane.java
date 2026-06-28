package com.dua3.utility.ui;

/**
 * Toolkit-agnostic API for editable rich-text panes.
 */
public interface RichTextEditorPane extends RichTextPane {

    /**
     * Returns whether pressing the ENTER key inserts a newline.
     *
     * @return true if ENTER inserts a newline
     */
    boolean isEnterKeyInsertsNewline();

    /**
     * Configures ENTER key behavior.
     *
     * @param value true if ENTER should insert a newline, false to leave ENTER handling to outer controls
     */
    void setEnterKeyInsertsNewline(boolean value);
}