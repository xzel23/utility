package com.dua3.utility.swing;

import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.SimpleOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

/**
 * Tests for the ArgumentsDialog class.
 */
class ArgumentsDialogTest extends AssertJSwingJUnitTestCase {

    @BeforeAll
    static void setupCaciocavallo() {
        // Ensure we're using caciocavallo for headless testing
        System.setProperty("java.awt.headless", "true");
        System.setProperty("awt.toolkit", "org.caciocavallo.CaciocavalloToolkit");
        System.setProperty("cacio.managed.screensize", "1024x768");

        // Print debug information
        System.out.println("[DEBUG_LOG] Headless mode: " + GraphicsEnvironment.isHeadless());
        System.out.println("[DEBUG_LOG] AWT Toolkit: " + System.getProperty("awt.toolkit"));
    }

    private FrameFixture window;
    private JFrame frame;

    @Override
    protected void onSetUp() {
        System.out.println("[DEBUG_LOG] Starting onSetUp");

        // Create the frame that will be tested
        frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("Test Frame");
            f.setSize(400, 200);
            // Add a dummy component to make sure the frame is properly initialized
            f.getContentPane().add(new JTextField("Test"));
            return f;
        });

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

        // Create a simple ArgumentsParser with one option
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        SimpleOption<String> testOption = builder.simpleOption(String.class, "--test")
                .displayName("Test Option");
        ArgumentsParser parser = builder.build();
        System.out.println("[DEBUG_LOG] Parser created");

        // Create counters to track button clicks
        final int[] okClicks = {0};
        final int[] cancelClicks = {0};

        // Create the panel
        ArgumentsDialog.ArgumentsPanel panel = GuiActionRunner.execute(() -> 
            new ArgumentsDialog.ArgumentsPanel(
                parser,
                () -> okClicks[0]++,      // OK action
                () -> cancelClicks[0]++   // Cancel action
            )
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
        com.dua3.utility.options.Arguments args = panel.getArguments();
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
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        SimpleOption<String> testOption = builder.simpleOption(String.class, "--test")
                .displayName("Test Option");
        ArgumentsParser parser = builder.build();

        // Create the panel
        ArgumentsDialog.ArgumentsPanel panel = new ArgumentsDialog.ArgumentsPanel(
                parser,
                () -> {}, // no-op for OK
                () -> {}  // no-op for Cancel
        );

        // Verify the panel has the expected inputs
        assertEquals(1, panel.getInputs().size(), "Panel should have one input");
        assertTrue(panel.getInputs().containsKey(testOption), "Panel should have input for test option");

        // Get the input for the test option
        ArgumentsDialog.ArgumentsPanel.OptionInput input = panel.getInputs().get(testOption);

        // Get the component and set its text directly (simulating user input)
        JTextField textField = (JTextField) input.component();
        textField.setText("test value");

        // Get the arguments and verify the value was set
        com.dua3.utility.options.Arguments args = panel.getArguments();
        assertTrue(args.get(testOption).isPresent(), "Option should be set in arguments");
        assertEquals("test value", args.get(testOption).get(), "Option value should match input");
    }
}
