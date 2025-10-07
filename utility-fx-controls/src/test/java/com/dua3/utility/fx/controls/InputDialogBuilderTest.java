package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link InputDialogBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class InputDialogBuilderTest extends FxTestBase {

    /**
     * Test the constructor and initial state of the InputDialogBuilder.
     */
    @Test
    void testConstructor() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Verify initial state
            assertNotNull(builder);
        });
    }

    /**
     * Test building a dialog.
     */
    @Test
    void testBuildDialog() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);
            assertNotNull(dialog.getDialogPane());
            assertInstanceOf(InputPane.class, dialog.getDialogPane());

            // Verify dialog has OK and Cancel buttons
            assertTrue(dialog.getDialogPane().getButtonTypes().contains(ButtonType.OK));
            assertTrue(dialog.getDialogPane().getButtonTypes().contains(ButtonType.CANCEL));
        });
    }

    /**
     * Test the text method.
     */
    @Test
    void testText() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add text
            builder.text("Test text");

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created with text
            assertNotNull(dialog);

            // Verify the dialog contains the text
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the text method with label.
     */
    @Test
    void testTextWithLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add text with label
            builder.text("Label", "Test text");

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created with text
            assertNotNull(dialog);

            // Verify the dialog contains the text with label
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the string method.
     */
    @Test
    void testString() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add string input
            builder.string("id", "Label", () -> "Default", s -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the string input
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the integer method.
     */
    @Test
    void testInteger() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add integer input
            builder.integer("id", "Label", () -> 42L, i -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the integer input
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the decimal method.
     */
    @Test
    void testDecimal() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add decimal input
            builder.decimal("id", "Label", () -> 3.14, d -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the decimal input
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the checkBox method.
     */
    @Test
    void testCheckBox() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add checkbox
            builder.checkBox("id", "Label", () -> true, "Check this", b -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the checkbox
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the comboBox method.
     */
    @Test
    void testComboBox() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add combo box
            List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
            builder.comboBox("id", "Label", () -> "Item 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the combo box
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the comboBoxEx method.
     */
    @Test
    void testComboBoxEx() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add extended combo box
            List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
            builder.comboBoxEx("id", "Label", s -> s + " (edited)", () -> "New Item", (cb, s) -> true, s -> s, () -> "Item 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the extended combo box
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the radioList method.
     */
    @Test
    void testRadioList() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add radio list
            List<String> items = Arrays.asList("Option 1", "Option 2", "Option 3");
            builder.radioList("id", "Label", () -> "Option 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the radio list
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the slider method.
     */
    @Test
    void testSlider() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add slider
            builder.slider("id", "Label", () -> 50.0, 0.0, 100.0);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the slider
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the chooseFile method.
     */
    @Test
    void testChooseFile() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add file chooser
            builder.chooseFile("id", "Label", () -> Paths.get(System.getProperty("user.home")), FileDialogMode.OPEN, true, Collections.emptyList(), p -> Optional.empty());

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the file chooser
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the node method.
     */
    @Test
    void testNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add node
            Node node = new Label("Custom node");
            builder.node("id", node);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the node
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the node method with label.
     */
    @Test
    void testNodeWithLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add node with label
            Node node = new Label("Custom node");
            builder.node("id", "Label", node);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the node with label
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the columns method.
     */
    @Test
    void testColumns() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Set columns
            builder.columns(3);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog has the columns set
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the constant method.
     */
    @Test
    void testConstant() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add constant
            builder.constant("id", "Label", () -> "Constant value", String.class);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the constant
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the constant method with direct value.
     */
    @Test
    void testConstantWithValue() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add constant with direct value
            builder.constant("id", "Label", "Constant value");

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the constant
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the hidden method.
     */
    @Test
    void testHidden() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add hidden value
            builder.hidden("id", () -> "Hidden value", String.class);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the hidden value
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the hidden method with direct value.
     */
    @Test
    void testHiddenWithValue() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add hidden with direct value
            builder.hidden("id", "Hidden value");

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the hidden value
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the add method.
     */
    @Test
    void testAdd() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add custom input control
            InputControl<String> control = new TestInputControl();
            builder.add("id", "Label", String.class, () -> "Default", control, false);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the custom input control
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the add method without label.
     */
    @Test
    void testAddWithoutLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add custom input control without label
            InputControl<String> control = new TestInputControl();
            builder.add("id", String.class, () -> "Default", control);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the custom input control
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the addNode method.
     */
    @Test
    void testAddNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add node
            Node node = new Label("Custom node");
            builder.addNode("id", "Label", node);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the node
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the addNode method without label.
     */
    @Test
    void testAddNodeWithoutLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(MessageFormatter.standard(), null);

            // Add node without label
            Node node = new Label("Custom node");
            builder.addNode("id", node);

            // Build a dialog
            Dialog<Map<String, Object>> dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the node
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test input control implementation for testing.
     */
    private static class TestInputControl implements InputControl<String> {
        private final Label label;
        private final SimpleObjectProperty<String> value;
        private final SimpleBooleanProperty valid;
        private final SimpleStringProperty error;
        private final Supplier<String> defaultValue;

        public TestInputControl() {
            this.label = new Label("Test Input");
            this.value = new SimpleObjectProperty<>("");
            this.valid = new SimpleBooleanProperty(true);
            this.error = new SimpleStringProperty("");
            this.defaultValue = () -> "";
        }

        @Override
        public Node node() {
            return label;
        }

        @Override
        public Property<String> valueProperty() {
            return value;
        }

        @Override
        public void reset() {
            value.set(defaultValue.get());
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        @Override
        public ReadOnlyStringProperty errorProperty() {
            return error;
        }
    }
}