package com.dua3.utility.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A helper interface that serves as a demultiplexer for {@link DocumentEvent}, i. e. update events received by
 * either of the three methods {@link #insertUpdate(DocumentEvent)}, {@link #removeUpdate(DocumentEvent)}, and
 * {@link #changedUpdate(DocumentEvent)} are mapped to the single method {@link #update(DocumentEvent)}.
 */
@FunctionalInterface
public interface SwingDocumentListener extends DocumentListener {

    /**
     * Document event handling method.
     * @param e the document event
     */
    void update(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
        update(e);
    }
    @Override
    default void removeUpdate(DocumentEvent e) {
        update(e);
    }
    @Override
    default void changedUpdate(DocumentEvent e) {
        update(e);
    }
}
