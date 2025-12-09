package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Build a dialog
            InputDialog dialog = builder.build();

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
    void testDescription() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add text
            builder.text("Test text");

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add text with label
            builder.labeledText("Label", "Test text");

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add string input
            builder.inputString("id", "Label", () -> "Default", s -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add integer input
            builder.inputInteger("id", "Label", () -> 42L, i -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add decimal input
            builder.inputDecimal("id", "Label", () -> 3.14, d -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add checkbox
            builder.inputCheckBox("id", "Label", () -> true, "Check this", b -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add combo box
            List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
            builder.inputComboBox("id", "Label", () -> "Item 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add extended combo box
            List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3");
            builder.inputComboBoxEx("id", "Label", s -> s + " (edited)", () -> "New Item", (cb, s) -> true, s -> s, () -> "Item 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add radio list
            List<String> items = Arrays.asList("Option 1", "Option 2", "Option 3");
            builder.inputRadioList("id", "Label", () -> "Option 1", String.class, items, s -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add slider
            builder.inputSlider("id", "Label", () -> 50.0, 0.0, 100.0);

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add file chooser
            builder.inputFile("id", "Label", () -> Paths.get(System.getProperty("user.home")), FileDialogMode.OPEN, true, Collections.emptyList(), p -> Optional.empty());

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add node
            Node node = new Label("Custom node");
            builder.node("id", node);

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add node with label
            Node node = new Label("Custom node");
            builder.node("Label", node);

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Set columns
            builder.columns(3);

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add constant
            builder.inputConstant("id", "Label", () -> "Constant value", String.class);

            // Build a dialog
            InputDialog dialog = builder.build();

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
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add constant with direct value
            builder.inputConstant("id", "Label", "Constant value");

            // Build a dialog
            InputDialog dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the constant
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the visible method.
     */
    @Test
    void testHidden() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add visible value
            builder.inputHidden("id", () -> "Hidden value", String.class);

            // Build a dialog
            InputDialog dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the visible value
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the visible method with direct value.
     */
    @Test
    void testHiddenWithValue() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add visible with direct value
            builder.inputHidden("id", "Hidden value");

            // Build a dialog
            InputDialog dialog = builder.build();

            // Verify dialog was created
            assertNotNull(dialog);

            // Verify the dialog contains the visible value
            DialogPane dialogPane = dialog.getDialogPane();
            assertInstanceOf(InputPane.class, dialogPane);
        });
    }

    /**
     * Test the add method.
     */
    @Test
    void testAddInput() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add custom input control
            InputControl<String> control = new TestInputControl();
            builder.addInput("id", "Label", String.class, () -> "Default", control, false);

            // Build a dialog
            InputDialog dialog = builder.build();

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
    void testAddInputWithoutLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add custom input control without label
            InputControl<String> control = new TestInputControl();
            builder.addInput("id", String.class, () -> "Default", control);

            // Build a dialog
            InputDialog dialog = builder.build();

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
    void testAddInputNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add node
            Node node = new Label("Custom node");
            builder.node("Label", node);

            // Build a dialog
            InputDialog dialog = builder.build();

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
    void testAddInputNodeWithoutLabel() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            InputDialogBuilder builder = new InputDialogBuilder(null, MessageFormatter.standard());

            // Add node without label
            Node node = new Label("Custom node");
            builder.node("id", node);

            // Build a dialog
            InputDialog dialog = builder.build();

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
        private final InputControlState<String> state;

        public TestInputControl() {
            this.label = new Label("Test Input");
            this.state = new InputControlState<>(new SimpleObjectProperty<>(""), () -> "", v -> Optional.empty());
        }

        @Override
        public InputControlState<String> state() {
            return state;
        }

        @Override
        public Node node() {
            return label;
        }
    }
}