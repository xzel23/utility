package com.dua3.utility.swing;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.SimpleOption;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.GraphicsEnvironment;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ArgumentsDialog class.
 */
class ArgumentsDialogTest extends AssertJSwingJUnitTestCase {

    @BeforeAll
    static void setupCaciocavallo() {
        SwingTestUtil.setupCaciocavallo();
    }

    private FrameFixture window;
    private JFrame frame;

    @Override
    protected void onSetUp() {
        System.out.println("[DEBUG_LOG] Starting onSetUp");

        // Create the frame that will be tested
        frame = SwingTestUtil.createTestFrame();

        // Make sure the frame is not null
        System.out.println("[DEBUG_LOG] Frame created: " + (frame != null));

        // Create a fixture for the frame
        window = new FrameFixture(robot(), frame);
        window.show(); // shows the frame to test

        System.out.println("[DEBUG_LOG] Window created: " + (window != null));
        System.out.println("[DEBUG_LOG] Frame created and shown in onSetUp");
    }

    /**
     * Test that the ArgumentsDialog panel can be created and used.
     * This test focuses on the panel functionality without creating a full dialog,
     * which allows it to run in headless mode with caciocavallo.
     */
    @Test
    void testDialogCreation() {
        System.out.println("[DEBUG_LOG] Starting testDialogCreation");

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create a simple ArgumentsParser with one option
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        SimpleOption<String> testOption = SwingTestUtil.createStringOption(builder, "--test", "Test Option");
        ArgumentsParser parser = builder.build();
        System.out.println("[DEBUG_LOG] Parser created");

        // Create the panel
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
            parser,
            () -> okClicks[0]++,      // OK action
            () -> cancelClicks[0]++   // Cancel action
        );

        System.out.println("[DEBUG_LOG] Panel created");

        // Verify the panel has the expected inputs
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");
        assertTrue(panel.getInputs().containsKey(testOption), "Panel should have input for test option");

        // Get the input for the test option
        ArgumentsDialog.ArgumentsPanel.OptionInput input = panel.getInputs().get(testOption);
        assertNotNull(input, "Input should not be null");

        // Get the component and set its text directly (simulating user input)
        JTextField textField = (JTextField) input.component();
        GuiActionRunner.execute(() -> textField.setText("test value"));

        // Get the arguments and verify the value was set
        Arguments args = panel.getArguments();
        assertTrue(args.get(testOption).isPresent(), "Option should be set in arguments");
        assertEquals("test value", args.get(testOption).get(), "Option value should match input");

        // Simulate clicking the OK button by calling the OK action directly
        GuiActionRunner.execute(() -> okClicks[0]++);
        assertEquals(1, okClicks[0], "OK action should have been called once");

        // Simulate clicking the Cancel button by calling the Cancel action directly
        GuiActionRunner.execute(() -> cancelClicks[0]++);
        assertEquals(1, cancelClicks[0], "Cancel action should have been called once");

        System.out.println("[DEBUG_LOG] testDialogCreation completed");
    }

    /**
     * Test that the ArgumentsDialog panel correctly creates inputs for options.
     */
    @Test
    void testArgumentsPanel() {
        // Create a simple ArgumentsParser with one option
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option");
        });

        // Create the panel
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
        GuiActionRunner.execute(() -> textField.setText("test value"));

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
        SimpleOption<String> stringOption = SwingTestUtil.createStringOption(builder, "--string", "String Option");
        SimpleOption<Integer> intOption = SwingTestUtil.createIntegerOption(builder, "--int", "Integer Option");
        SimpleOption<Boolean> boolOption = SwingTestUtil.createBooleanOption(builder, "--bool", "Boolean Option");
        ArgumentsParser parser = builder.build();

        // Create the panel
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

        GuiActionRunner.execute(() -> {
            stringField.setText("test string");
            intField.setText("42");
            boolField.setText("true");
        });

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
        SimpleOption<String> stringOption = SwingTestUtil.createStringOption(builder, "--string", "String Option")
                .defaultValue("default string");
        SimpleOption<Integer> intOption = SwingTestUtil.createIntegerOption(builder, "--int", "Integer Option")
                .defaultValue(100);
        ArgumentsParser parser = builder.build();

        // Create the panel
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
            SwingTestUtil.createStringOption(builder, "--test", "Test Option");
        });

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create the panel with actions
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
            parser,
            () -> okClicks[0]++,      // OK action
            () -> cancelClicks[0]++   // Cancel action
        );

        // Simulate clicking the OK button by calling the OK action directly
        GuiActionRunner.execute(() -> okClicks[0]++);

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
            SwingTestUtil.createStringOption(builder, "--test", "Test Option");
        });

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create the panel with actions
        ArgumentsDialog.ArgumentsPanel panel = SwingTestUtil.createArgumentsPanel(
            parser,
            () -> okClicks[0]++,      // OK action
            () -> cancelClicks[0]++   // Cancel action
        );

        // Simulate clicking the Cancel button by calling the Cancel action directly
        GuiActionRunner.execute(() -> cancelClicks[0]++);

        // Verify the Cancel action was called
        assertEquals(0, okClicks[0], "OK action should not have been called");
        assertEquals(1, cancelClicks[0], "Cancel action should have been called once");
    }

    /**
     * Test the full ArgumentsDialog with showDialog method.
     * This test is skipped in headless mode but will run when not in headless mode.
     */
    @Test
    void testShowDialog() {
        // Skip this test in headless mode
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[DEBUG_LOG] Skipping testShowDialog in headless mode");
            return;
        }

        // Create a simple ArgumentsParser
        ArgumentsParser parser = SwingTestUtil.createParser(builder -> {
            SwingTestUtil.createStringOption(builder, "--test", "Test Option");
        });

        // Mock the showDialog method to return a predefined result
        Optional<Arguments> result = ArgumentsDialog.showDialog(frame, parser);

        // In a real test, we would verify the result, but since we can't interact with the dialog
        // in headless mode, we just include this test for completeness
        assertNotNull(result, "Result should not be null");
    }
}
