package com.dua3.utility.swing;

import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.SimpleOption;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class for Swing tests.
 */
public final class SwingTestUtil {

    private SwingTestUtil() {
        // Utility class, no instances
    }

    /**
     * Sets up the Caciocavallo toolkit for headless testing.
     */
    public static void setupCaciocavallo() {
        // Ensure we're using caciocavallo for headless testing
        System.setProperty("java.awt.headless", "true");
        System.setProperty("awt.toolkit", "org.caciocavallo.CaciocavalloToolkit");
        System.setProperty("cacio.managed.screensize", "1024x768");

        // Print debug information
        System.out.println("[DEBUG_LOG] Headless mode: " + GraphicsEnvironment.isHeadless());
        System.out.println("[DEBUG_LOG] AWT Toolkit: " + System.getProperty("awt.toolkit"));
    }

    /**
     * Creates a test frame for use in Swing tests.
     *
     * @return a new JFrame for testing
     */
    public static JFrame createTestFrame() {
        return GuiActionRunner.execute(() -> {
            JFrame f = new JFrame("Test Frame");
            f.setSize(400, 200);
            // Add a dummy component to make sure the frame is properly initialized
            f.getContentPane().add(new JTextField("Test"));
            return f;
        });
    }

    /**
     * Creates a simple ArgumentsParser with the specified options.
     *
     * @param optionSetup a consumer that sets up options on the builder
     * @return the built ArgumentsParser
     */
    public static ArgumentsParser createParser(Consumer<ArgumentsParserBuilder> optionSetup) {
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        optionSetup.accept(builder);
        return builder.build();
    }

    /**
     * Creates a simple String option.
     *
     * @param builder the ArgumentsParserBuilder to use
     * @param name the option name
     * @param displayName the display name for the option
     * @return the created SimpleOption
     */
    public static SimpleOption<String> createStringOption(ArgumentsParserBuilder builder, String name, String displayName) {
        return builder.simpleOption(String.class, name)
                .displayName(displayName);
    }

    /**
     * Creates a simple Integer option.
     *
     * @param builder the ArgumentsParserBuilder to use
     * @param name the option name
     * @param displayName the display name for the option
     * @return the created SimpleOption
     */
    public static SimpleOption<Integer> createIntegerOption(ArgumentsParserBuilder builder, String name, String displayName) {
        return builder.simpleOption(Integer.class, name)
                .displayName(displayName);
    }

    /**
     * Creates a simple Boolean option.
     *
     * @param builder the ArgumentsParserBuilder to use
     * @param name the option name
     * @param displayName the display name for the option
     * @return the created SimpleOption
     */
    public static SimpleOption<Boolean> createBooleanOption(ArgumentsParserBuilder builder, String name, String displayName) {
        return builder.simpleOption(Boolean.class, name)
                .displayName(displayName);
    }

    /**
     * Creates an ArgumentsPanel for testing.
     *
     * @param parser the ArgumentsParser to use
     * @param onOk the action to perform when OK is clicked
     * @param onCancel the action to perform when Cancel is clicked
     * @return the created ArgumentsPanel
     */
    public static ArgumentsDialog.ArgumentsPanel createArgumentsPanel(
            ArgumentsParser parser, Runnable onOk, Runnable onCancel) {
        return GuiActionRunner.execute(() -> 
            new ArgumentsDialog.ArgumentsPanel(parser, onOk, onCancel)
        );
    }

    /**
     * Finds all components of a specific type in a container.
     *
     * @param container the container to search in
     * @param type the type of components to find
     * @param <T> the component type
     * @return a list of components of the specified type
     */
    public static <T extends Component> List<T> findComponentsOfType(Container container, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                result.add(type.cast(component));
            }
            if (component instanceof Container) {
                result.addAll(findComponentsOfType((Container) component, type));
            }
        }
        return result;
    }

    /**
     * Finds a button with the specified text in a container.
     *
     * @param container the container to search in
     * @param text the button text to search for
     * @return the button with the specified text, or null if not found
     */
    public static JButton findButtonByText(Container container, String text) {
        List<JButton> buttons = findComponentsOfType(container, JButton.class);
        for (JButton button : buttons) {
            if (text.equals(button.getText())) {
                return button;
            }
        }
        return null;
    }

    /**
     * Clicks a button in an ArgumentsPanel.
     *
     * @param panel the ArgumentsPanel containing the button
     * @param buttonText the text of the button to click
     */
    public static void clickButton(ArgumentsDialog.ArgumentsPanel panel, String buttonText) {
        GuiActionRunner.execute(() -> {
            JButton button = findButtonByText(panel, buttonText);
            if (button != null) {
                button.doClick();
            } else {
                throw new IllegalArgumentException("Button with text '" + buttonText + "' not found");
            }
        });
    }
}
