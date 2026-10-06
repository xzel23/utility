package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.math.MathUtil;
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
    void testCustomFontSizeEntry() throws Exception {
        TextEditorPane editor = new TextEditorPane("sample text");
        addToScene(editor);

        runOnFxThreadAndWait(() -> {
            var comboBoxes = editor.lookupAll(".comboboxex .combo-box");
            assertEquals(2, comboBoxes.size(), "Should have font and size combo boxes");

            @SuppressWarnings("unchecked")
            ComboBox<Float> sizeComboBox = (ComboBox<Float>) comboBoxes.stream()
                    .filter(node -> node instanceof ComboBox<?> cb && cb.isEditable())
                    .findFirst()
                    .orElse(null);

            assertNotNull(sizeComboBox, "Editable size ComboBox should be found");
            assertEquals(3, sizeComboBox.getEditor().getPrefColumnCount());
            assertEquals(Integer.toString(MathUtil.roundToInt(editor.getFontSize())), sizeComboBox.getEditor().getText());

            // Enter a custom font size not originally in the default sizes list
            sizeComboBox.getEditor().setText("17");
            sizeComboBox.commitValue();

            assertEquals(17.0f, editor.getFontSize(), 0.01f);
            assertEquals(17.0f, sizeComboBox.getValue(), 0.01f);
            org.junit.jupiter.api.Assertions.assertTrue(sizeComboBox.getItems().contains(17.0f));
        });
    }

    @Test
    void testFontSizeSelectionAndTyping() throws Exception {
        TextEditorPane editor = new TextEditorPane("sample text");
        addToScene(editor);

        runOnFxThreadAndWait(() -> {
            var comboBoxes = editor.lookupAll(".comboboxex .combo-box");
            @SuppressWarnings("unchecked")
            ComboBox<Float> sizeComboBox = (ComboBox<Float>) comboBoxes.stream()
                    .filter(node -> node instanceof ComboBox<?> cb && cb.isEditable())
                    .findFirst()
                    .orElse(null);
            assertNotNull(sizeComboBox, "Editable size ComboBox should be found");

            // 1. Focus the editor first (user had clicked the combo box or opened dropdown)
            sizeComboBox.getEditor().requestFocus();
            // User selects a value from the dropdown
            sizeComboBox.getSelectionModel().select(24.0f);
            assertEquals(24.0f, editor.getFontSize(), 0.01f);
            assertEquals(24.0f, sizeComboBox.getValue(), 0.01f);
            assertEquals("24", sizeComboBox.getEditor().getText());

            // 2. Type into the sizeComboBox editor
            sizeComboBox.getEditor().requestFocus();
            javafx.scene.input.KeyEvent keyEvent = new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_TYPED,
                    "x", "x", javafx.scene.input.KeyCode.UNDEFINED,
                    false, false, false, false
            );
            javafx.event.Event.fireEvent(sizeComboBox.getEditor(), keyEvent);
            // Verify characters were NOT intercepted and inserted into editor document text
            assertEquals("sample text", editor.getText().toString());
        });
    }

    @Test
    void testItalicButtonToggle() throws Exception {
        TextEditorPane editor = new TextEditorPane("sample text");
        addToScene(editor);

        runOnFxThreadAndWait(() -> {
            var toggleButtons = editor.lookupAll(".toggle-button");
            javafx.scene.control.ToggleButton boldBtn = null;
            javafx.scene.control.ToggleButton italicBtn = null;
            for (var node : toggleButtons) {
                if (node instanceof javafx.scene.control.ToggleButton tb) {
                    if (tb.getTooltip() != null && "Bold".equals(tb.getTooltip().getText())) {
                        boldBtn = tb;
                    } else if (tb.getTooltip() != null && "Italic".equals(tb.getTooltip().getText())) {
                        italicBtn = tb;
                    }
                }
            }
            assertNotNull(boldBtn, "Bold button should be found");
            assertNotNull(italicBtn, "Italic button should be found");

            org.junit.jupiter.api.Assertions.assertFalse(boldBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(italicBtn.isSelected());

            // Case 1: No selection, plain text
            editor.selectRange(10, 10);
            org.junit.jupiter.api.Assertions.assertFalse(boldBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(italicBtn.isSelected());
            italicBtn.fire();
            org.junit.jupiter.api.Assertions.assertTrue(italicBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(boldBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertTrue(editor.isItalic());
            org.junit.jupiter.api.Assertions.assertFalse(editor.isBold());

            // Toggle italic back off
            italicBtn.fire();
            org.junit.jupiter.api.Assertions.assertFalse(italicBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(boldBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(editor.isItalic());

            // Case 2: Selection in plain text
            editor.selectRange(0, 4);
            italicBtn.fire();
            org.junit.jupiter.api.Assertions.assertTrue(italicBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(boldBtn.isSelected());
            org.junit.jupiter.api.Assertions.assertTrue(editor.isItalic());
            org.junit.jupiter.api.Assertions.assertFalse(editor.isBold());
            org.junit.jupiter.api.Assertions.assertTrue(editor.getText().stylesAt(1).contains(com.dua3.utility.text.Style.ITALIC));
            org.junit.jupiter.api.Assertions.assertFalse(editor.getText().stylesAt(1).contains(com.dua3.utility.text.Style.BOLD));

            // Case 3: Selection in bold text
            com.dua3.utility.text.RichText rt = com.dua3.utility.text.RichText.valueOf("Bold text\nPlain text");
            rt = rt.apply(com.dua3.utility.text.Style.BOLD, 0, 9);
            TextEditorPane editor2 = new TextEditorPane(rt);
            addToScene(editor2);
            var tb2 = editor2.lookupAll(".toggle-button");
            javafx.scene.control.ToggleButton b2 = null, i2 = null;
            for (var node : tb2) {
                if (node instanceof javafx.scene.control.ToggleButton tb) {
                    if (tb.getTooltip() != null && "Bold".equals(tb.getTooltip().getText())) b2 = tb;
                    if (tb.getTooltip() != null && "Italic".equals(tb.getTooltip().getText())) i2 = tb;
                }
            }
            assertNotNull(b2);
            assertNotNull(i2);
            editor2.selectRange(0, 4);
            org.junit.jupiter.api.Assertions.assertTrue(b2.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(i2.isSelected());

            // Apply italic to bold text
            i2.fire();
            org.junit.jupiter.api.Assertions.assertTrue(b2.isSelected());
            org.junit.jupiter.api.Assertions.assertTrue(i2.isSelected());
            org.junit.jupiter.api.Assertions.assertTrue(editor2.isBold());
            org.junit.jupiter.api.Assertions.assertTrue(editor2.isItalic());

            // Remove bold
            b2.fire();
            org.junit.jupiter.api.Assertions.assertFalse(b2.isSelected());
            org.junit.jupiter.api.Assertions.assertTrue(i2.isSelected());
            org.junit.jupiter.api.Assertions.assertFalse(editor2.isBold());
            org.junit.jupiter.api.Assertions.assertTrue(editor2.isItalic());
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
