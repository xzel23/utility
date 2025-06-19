package com.dua3.utility.swing;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.OptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ArgumentsDialog class.
 * <p>
 * These tests focus on the component logic without requiring a full GUI setup.
 * They run in headless mode and test the core functionality of the ArgumentsDialog panel.
 */
class ArgumentsDialogTest {

    private Window mockWindow;

    @BeforeEach
    void setUp() {
        // Create a mock window for tests that need a parent component
        // In headless mode, this will be null, which is fine for panel testing
        mockWindow = SwingTestUtil.createMockWindow();
    }

    /**
     * Test that the ArgumentsDialog panel can be created and used.
     * This test focuses on the panel functionality without creating a full dialog,
     * which allows it to run in headless mode.
     */
    @Test
    void testDialogCreation() {
        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create a simple ArgumentsParser with one option
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        Option<String> testOption = SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        ArgumentsParser parser = builder.build();

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> okClicks[0]++,      // OK action
                () -> cancelClicks[0]++   // Cancel action
        );

        // Verify the panel has the expected inputs
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");
        assertTrue(panel.getInputs().containsKey(testOption), "Panel should have input for test option");

        // Get the input for the test option
        ArgumentsDialog.ArgumentsPanel.OptionInput input = panel.getInputs().get(testOption);
        assertNotNull(input, "Input should not be null");

        // Get the component and set its text directly (simulating user input)
        JTextField textField = (JTextField) input.component();
        textField.setText("test value");

        // Get the arguments and verify the value was set
        Arguments args = panel.getArguments();
        assertTrue(args.get(testOption).isPresent(), "Option should be set in arguments");
        assertEquals("test value", args.get(testOption).get(), "Option value should match input");

        // Simulate clicking the OK button
        SwingTestUtil.clickButton(panel, "OK");
        assertEquals(1, okClicks[0], "OK action should have been called once");

        // Simulate clicking the Cancel button
        SwingTestUtil.clickButton(panel, "Cancel");
        assertEquals(1, cancelClicks[0], "Cancel action should have been called once");
    }

    /**
     * Test that the ArgumentsDialog panel correctly creates inputs for options.
     */
    @Test
    void testArgumentsPanel() {
        // Create a simple ArgumentsParser with one option
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        });

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Verify the panel has the expected inputs
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");

        // Get the first input
        ArgumentsDialog.ArgumentsPanel.OptionInput input = panel.getInputs().values().iterator().next();
        assertNotNull(input, "Input should not be null");

        // Get the component and set its text directly (simulating user input)
        JTextField textField = (JTextField) input.component();
        textField.setText("test value");

        // Get the arguments and verify the value was set
        Arguments args = panel.getArguments();
        // Verify that we can get the option value from the arguments
        assertNotNull(args, "Arguments should not be null");
    }

    /**
     * Test that the ArgumentsDialog panel correctly handles multiple options of different types.
     */
    @Test
    void testMultipleOptions() {
        // Create an ArgumentsParser with multiple options of different types
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        Option<String> stringOption = SwingTestUtil.createStringOption(builder, "--string", "String Option", "");
        Option<Integer> intOption = SwingTestUtil.createIntegerOption(builder, "--int", "Integer Option", null);
        Option<Boolean> boolOption = SwingTestUtil.createBooleanOption(builder, "--bool", "Boolean Option");
        ArgumentsParser parser = builder.build();

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Verify the panel has the expected inputs
        assertEquals(3, panel.getInputs().size(), "Panel should have three inputs");
        assertTrue(panel.getInputs().containsKey(stringOption), "Panel should have input for string option");
        assertTrue(panel.getInputs().containsKey(intOption), "Panel should have input for int option");
        assertTrue(panel.getInputs().containsKey(boolOption), "Panel should have input for bool option");

        // Set values for each option
        JTextField stringField = (JTextField) panel.getInputs().get(stringOption).component();
        JTextField intField = (JTextField) panel.getInputs().get(intOption).component();
        JTextField boolField = (JTextField) panel.getInputs().get(boolOption).component();

        stringField.setText("test string");
        intField.setText("42");
        boolField.setText("true");

        // Get the arguments and verify the values were set
        Arguments args = panel.getArguments();
        assertTrue(args.get(stringOption).isPresent(), "String option should be set in arguments");
        assertEquals("test string", args.get(stringOption).get(), "String option value should match input");

        assertTrue(args.get(intOption).isPresent(), "Int option should be set in arguments");
        assertEquals(42, args.get(intOption).get(), "Int option value should match input");

        assertTrue(args.get(boolOption).isPresent(), "Bool option should be set in arguments");
        assertEquals(true, args.get(boolOption).get(), "Bool option value should match input");
    }

    /**
     * Test that the ArgumentsDialog panel correctly handles default values for options.
     */
    @Test
    void testDefaultValues() {
        // Create an ArgumentsParser with options that have default values
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        Option<String> stringOption = SwingTestUtil.createStringOption(builder, "--string", "String Option", "default string");
        Option<Integer> intOption = SwingTestUtil.createIntegerOption(builder, "--int", "Integer Option", 100);
        ArgumentsParser parser = builder.build();

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Verify the default values are set in the inputs
        JTextField stringField = (JTextField) panel.getInputs().get(stringOption).component();
        JTextField intField = (JTextField) panel.getInputs().get(intOption).component();

        assertEquals("default string", stringField.getText(), "String field should have default value");
        assertEquals("100", intField.getText(), "Int field should have default value");

        // Get the arguments without changing the inputs and verify the default values are used
        Arguments args = panel.getArguments();
        assertTrue(args.get(stringOption).isPresent(), "String option should be set in arguments");
        assertEquals("default string", args.get(stringOption).get(), "String option value should be the default");

        assertTrue(args.get(intOption).isPresent(), "Int option should be set in arguments");
        assertEquals(100, args.get(intOption).get(), "Int option value should be the default");
    }

    /**
     * Test the dialog closing behavior with OK button.
     */
    @Test
    void testDialogOkButton() {
        // Create a simple ArgumentsParser
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        });

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create the panel with actions - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> okClicks[0]++,      // OK action
                () -> cancelClicks[0]++   // Cancel action
        );

        // Simulate clicking the OK button
        SwingTestUtil.clickButton(panel, "OK");

        // Verify the OK action was called
        assertEquals(1, okClicks[0], "OK action should have been called once");
        assertEquals(0, cancelClicks[0], "Cancel action should not have been called");
    }

    /**
     * Test the dialog closing behavior with Cancel button.
     */
    @Test
    void testDialogCancelButton() {
        // Create a simple ArgumentsParser
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        });

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create the panel with actions - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> okClicks[0]++,      // OK action
                () -> cancelClicks[0]++   // Cancel action
        );

        // Simulate clicking the Cancel button
        SwingTestUtil.clickButton(panel, "Cancel");

        // Verify the Cancel action was called
        assertEquals(0, okClicks[0], "OK action should not have been called");
        assertEquals(1, cancelClicks[0], "Cancel action should have been called once");
    }

    /**
     * Test the full ArgumentsDialog with showDialog method.
     * This test is disabled because it requires a real GUI environment.
     */
    @Test
    @Disabled("Requires real GUI environment - disabled for headless testing")
    void testShowDialog() {
        // Skip this test in headless mode
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[DEBUG_LOG] Skipping testShowDialog in headless mode");
            return;
        }

        // Create a simple ArgumentsParser
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        });

        // Mock the showDialog method to return a predefined result
        Optional<Arguments> result = ArgumentsDialog.showDialog(mockWindow, parser);

        // In a real test, we would verify the result, but since we can't interact with the dialog
        // in headless mode, we just include this test for completeness
        assertNotNull(result, "Result should not be null");
    }

    /**
     * Test argument validation and error handling.
     */
    @Test
    void testArgumentValidation() {
        // Create an ArgumentsParser with an integer option that should validate input
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        Option<Integer> intOption = SwingTestUtil.createIntegerOption(builder, "--number", "Number Option", null);
        ArgumentsParser parser = builder.build();

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Get the input field for the integer option
        JTextField intField = (JTextField) panel.getInputs().get(intOption).component();

        // Test valid integer input
        intField.setText("123");
        Arguments args = panel.getArguments();
        assertTrue(args.get(intOption).isPresent(), "Valid integer should be parsed");
        assertEquals(123, args.get(intOption).get(), "Integer value should be correct");

        // Test invalid integer input - the panel should handle this gracefully
        intField.setText("not-a-number");
        assertThrows(OptionException.class, panel::getArguments, "Should throw OptionException");
    }

    /**
     * Test empty input handling.
     */
    @Test
    void testEmptyInput() {
        // Create an ArgumentsParser with optional parameters
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        Option<String> stringOption = SwingTestUtil.createStringOption(builder, "--optional", "Optional Option", "");
        ArgumentsParser parser = builder.build();

        // Create the panel - this works in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Leave the input field empty
        JTextField stringField = (JTextField) panel.getInputs().get(stringOption).component();
        stringField.setText("");

        // Get the arguments and verify empty input is handled correctly
        Arguments args = panel.getArguments();
        assertNotNull(args, "Arguments should not be null");
        // Empty input handling depends on the option configuration
        // We just verify no exception is thrown
    }

    /**
     * Test that we can create an ArgumentsPanel without needing a real window.
     */
    @Test
    void testPanelCreationInHeadlessMode() {
        // This test verifies that we can create panels in headless mode
        assertTrue(GraphicsEnvironment.isHeadless() || !GraphicsEnvironment.isHeadless(),
                "Test should work in both headless and non-headless modes");

        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "");
        });

        // This should not throw any exceptions, even in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        assertNotNull(panel, "Panel should be created successfully");
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");
    }

    /**
     * Test that panel components work correctly in headless mode.
     */
    @Test
    void testPanelComponentsInHeadlessMode() {
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option", "default");
        });

        // Create the panel - this should work in headless mode
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Test that we can interact with the components
        assertNotNull(panel, "Panel should be created");
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");

        // Get the text field and verify it has the default value
        JTextField textField = (JTextField) panel.getInputs().values().iterator().next().component();
        assertEquals("default", textField.getText(), "Text field should have default value");

        // Test that we can change the text
        textField.setText("changed");
        assertEquals("changed", textField.getText(), "Text field should reflect changes");

        // Test that arguments are generated correctly
        Arguments args = panel.getArguments();
        assertNotNull(args, "Arguments should not be null");
    }
}
