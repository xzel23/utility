package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.ui.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPaneSelectionTest extends FxTestBase {

    @Test
    void textSelectionAndCopyAreOptInAndReadOnly() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextPane pane = new TextPane("read only");

            assertFalse(pane.isSelectable());
            pane.selectAll();
            assertEquals(new IndexRange(0, 0), pane.getSelection());

            pane.setSelectable(true);
            pane.selectAll();
            assertEquals(new IndexRange(0, 9), pane.getSelection());
            pane.copy();
            assertEquals("read only", FxUtil.getTextFromClipboard().orElseThrow().toString());

            KeyEvent cut = keyPressed(KeyCode.X, false, true);
            pane.processSelectableKeyPressed(cut);
            assertTrue(cut.isConsumed());
            assertEquals("read only", pane.getText().toString());

            KeyEvent typed = keyTyped("!");
            pane.processSelectableKeyTyped(typed);
            assertTrue(typed.isConsumed());
            assertEquals("read only", pane.getText().toString());

            pane.setSelectable(false);
            assertEquals(new IndexRange(0, 0), pane.getSelection());
        });
    }

    private static KeyEvent keyTyped(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED, false, false, false, false);
    }

    private static KeyEvent keyPressed(KeyCode code, boolean shift, boolean shortcut) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, shortcut, false, shortcut);
    }
}
