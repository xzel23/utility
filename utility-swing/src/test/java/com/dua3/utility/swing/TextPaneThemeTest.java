package com.dua3.utility.swing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TextPaneThemeTest {

    @Test
    void usesJTextAreaColorsAndRefreshesThemAfterALookAndFeelUpdate() {
        onEdt(() -> {
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            Object originalForeground = defaults.get("TextArea.foreground");
            Object originalBackground = defaults.get("TextArea.background");
            try {
                Color lightForeground = new Color(31, 41, 55);
                Color lightBackground = new Color(248, 250, 252);
                defaults.put("TextArea.foreground", lightForeground);
                defaults.put("TextArea.background", lightBackground);

                TextPane pane = new TextPane("text");
                TextEditorPane editor = new TextEditorPane("text");
                assertUsesTextAreaColors(new JTextArea(), pane);
                assertUsesTextAreaColors(new JTextArea(), editor);

                Color darkForeground = new Color(226, 232, 240);
                Color darkBackground = new Color(30, 41, 59);
                defaults.put("TextArea.foreground", darkForeground);
                defaults.put("TextArea.background", darkBackground);
                SwingUtilities.updateComponentTreeUI(pane);
                SwingUtilities.updateComponentTreeUI(editor);

                assertUsesTextAreaColors(new JTextArea(), pane);
                assertUsesTextAreaColors(new JTextArea(), editor);
            } finally {
                restore(defaults, "TextArea.foreground", originalForeground);
                restore(defaults, "TextArea.background", originalBackground);
            }
        });
    }

    private static void assertUsesTextAreaColors(JTextArea reference, TextPane pane) {
        assertEquals(reference.getForeground(), pane.getForeground());
        assertEquals(reference.getBackground(), pane.getBackground());
        assertEquals(reference.getForeground(), pane.getTextComponent().getForeground());
        assertEquals(reference.getBackground(), pane.getTextComponent().getBackground());
        assertEquals(SwingUtil.convert(reference.getForeground()), pane.getTextFont().getColor());
    }

    private static void restore(UIDefaults defaults, String key, Object value) {
        if (value == null) {
            defaults.remove(key);
        } else {
            defaults.put(key, value);
        }
    }

    private static void onEdt(Runnable action) {
        try {
            SwingUtilities.invokeAndWait(action);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
