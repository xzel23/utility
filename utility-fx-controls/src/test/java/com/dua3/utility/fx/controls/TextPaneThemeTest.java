package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TextPaneThemeTest extends FxTestBase {

    private static final String LIGHT_THEME = "-fx-text-fill: rgb(31, 41, 55); -fx-control-inner-background: rgb(248, 250, 252);";
    private static final String DARK_THEME = "-fx-text-fill: rgb(226, 232, 240); -fx-control-inner-background: rgb(30, 41, 59);";

    @Test
    void testFontSelectorTextColor() throws Exception {
        TextEditorPane editor = new TextEditorPane("sample text");
        editor.setFontFamily("Verdana");
        addToScene(editor);

        runOnFxThreadAndWait(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<String> fontComboBox = (ComboBox<String>) editor.lookup(".comboboxex .combo-box");
            assertNotNull(fontComboBox, "fontComboBox should be found");
            javafx.scene.control.ListCell<String> buttonCell = fontComboBox.getButtonCell();
            assertNotNull(buttonCell, "buttonCell should be found");
            assertEquals("Verdana", buttonCell.getText());
            assertNull(buttonCell.getGraphic(), "Graphic should be null as text is set on buttonCell directly");
            // Button cell keeps the standard font to avoid changing toolbar height
            assertEquals(new javafx.scene.control.ListCell<String>().getFont(), buttonCell.getFont());
            // Text color is styled by buttonCell directly (not white on light background)
            javafx.scene.paint.Paint textFill = buttonCell.getTextFill();
            assertNotEquals(javafx.scene.paint.Color.WHITE, textFill);
        });
    }

    @Test
    void usesTextAreaColorsAndRefreshesThemWhenStylesChange() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextArea reference = new TextArea("text");
            TextPane pane = new TextPane("text");
            TextEditorPane editor = new TextEditorPane("text");
            VBox root = new VBox(reference, pane, editor);
            new Scene(root);

            applyTheme(LIGHT_THEME, root, reference, pane, editor);
            assertUsesTextAreaColors(reference, pane);
            assertUsesTextAreaColors(reference, editor);

            applyTheme(DARK_THEME, root, reference, pane, editor);
            assertUsesTextAreaColors(reference, pane);
            assertUsesTextAreaColors(reference, editor);
        });
    }

    private static void applyTheme(String stylesheet, VBox root, TextArea reference, TextPane pane, TextEditorPane editor) {
        reference.setStyle(stylesheet);
        pane.setStyle(stylesheet);
        editor.setStyle(stylesheet);
        root.applyCss();
        root.layout();
    }

    private static void assertUsesTextAreaColors(TextArea reference, TextPane pane) {
        Text referenceText = assertInstanceOf(Text.class, reference.lookup(".text"));
        javafx.scene.paint.Color referenceTextFill = assertInstanceOf(javafx.scene.paint.Color.class, referenceText.getFill());
        assertEquals(FxUtil.convert(referenceTextFill), pane.getTextFont().getColor());

        Region referenceContent = assertInstanceOf(Region.class, reference.lookup(".content"));
        Region paneContent = assertInstanceOf(Region.class, pane.lookup(".content"));
        assertEquals(backgroundFill(referenceContent), backgroundFill(paneContent));
    }

    private static Paint backgroundFill(Region pane) {
        return pane.getBackground().getFills().getFirst().getFill();
    }
}
