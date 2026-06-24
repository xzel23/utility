package com.dua3.utility.swing;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPaneTest {

    @Test
    void testSetAndGetText() {
        TextPane pane = onEdtGet(TextPane::new);

        RichText text = new RichTextBuilder()
                .append("Hello ")
                .push(com.dua3.utility.text.Style.BOLD)
                .append("world")
                .pop(com.dua3.utility.text.Style.BOLD)
                .toRichText();

        onEdtRun(() -> pane.setText(text));

        RichText current = onEdtGet(pane::getText);
        assertEquals(text, current);
        assertEquals("Hello world", onEdtGet(() -> documentText(pane)));
    }

    @Test
    void testWrapModeUpdatesScrollbarPolicy() {
        TextPane pane = onEdtGet(TextPane::new);

        assertFalse(onEdtGet(pane::isWrapText));
        assertEquals(TextPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, onEdtGet(pane::getHorizontalScrollBarPolicy));

        onEdtRun(() -> pane.setWrapText(true));
        assertTrue(onEdtGet(pane::isWrapText));
        assertEquals(TextPane.HORIZONTAL_SCROLLBAR_NEVER, onEdtGet(pane::getHorizontalScrollBarPolicy));
    }

    @Test
    void testSetTextFont() {
        TextPane pane = onEdtGet(TextPane::new);
        Font font = FontUtil.getInstance().getFont("dialog-18-bold");

        onEdtRun(() -> pane.setTextFont(font));

        assertSame(font, onEdtGet(pane::getTextFont));
        assertEquals(18, onEdtGet(() -> pane.getTextComponent().getFont().getSize()));
        assertTrue(onEdtGet(() -> pane.getTextComponent().getFont().isBold()));
    }

    @Test
    void testDefaultReadOnlyBehavior() {
        TextPane pane = onEdtGet(TextPane::new);
        assertFalse(onEdtGet(() -> pane.getTextComponent().isEditable()));
    }

    private static String documentText(TextPane pane) {
        try {
            return pane.getTextComponent().getDocument().getText(0, pane.getTextComponent().getDocument().getLength());
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void onEdtRun(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private static <T> T onEdtGet(Supplier<T> action) {
        if (SwingUtilities.isEventDispatchThread()) {
            return action.get();
        }

        final Object[] result = new Object[1];
        onEdtRun(() -> result[0] = action.get());
        @SuppressWarnings("unchecked")
        T value = (T) result[0];
        return value;
    }
}
