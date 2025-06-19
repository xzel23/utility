package com.dua3.utility.swing;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.Option;
import org.jspecify.annotations.Nullable;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a dialog that allows users to input arguments for a command-line program.
 * The dialog displays a list of options, each with an input field for the user to provide the corresponding value.
 * The dialog provides an "OK" button to accept the input and a "Cancel" button to close the dialog without saving the input.
 * <p>
 * Usage:
 * {@code
 * ArgumentsParser parser = new ArgumentsParser();
 * // Add options to the parser
 * ArgumentsDialog.showDialog(window, parser);
 * Optional<Arguments> arguments = ArgumentsDialog.showDialog(window, parser);
 * }
 */
public class ArgumentsDialog extends JDialog {

    private final ArgumentsPanel panel;

    /**
     * A custom extension of JPanel that displays input fields for options defined by an ArgumentsParser.
     * <p>
     * The ArgumentsPanel class provides a way to create a panel with input fields for each option defined by an ArgumentsParser.
     * It also handles the retrieval and storage of user inputs for each option.
     * <p>
     * Usage:
     * ArgumentsPanel panel = new ArgumentsPanel(parser, onOk, onCancel);
     * <p>
     * The constructor takes an ArgumentsParser, which defines the options to be displayed, and two Runnables,
     * onOk and onCancel, which are executed when the corresponding buttons are clicked.
     * <p>
     * The panel is constructed with a GridBagLayout and adds JLabels and JTextFields for each option defined
     * by the parser. The user inputs are stored in a {@code Map<Option<?>, OptionInput>} for easy retrieval.
     * <p>
     * Example usage:
     * ArgumentsPanel panel = new ArgumentsPanel(parser, () -> {
     *   // Handle OK button click
     * }, () -> {
     *   // Handle Cancel button click
     * });
     * <p>
     * {@code
     * Map<Option<?>, OptionInput> inputs = panel.getInputs();
     * Arguments arguments = panel.getArguments();
     * }
     */
    public static class ArgumentsPanel extends JPanel {
        /**
         * Represents an input component that consists of an {@link Option}, a UI {@link JComponent},
         * and getter and setter functionality for parameter management.
         * <p>
         * This record is used to encapsulate the association between an option and its corresponding
         * input component, along with the necessary operations to retrieve and update its parameters.
         *
         * @param option       The {@link Option} object to be associated with the input component.
         * @param component    The {@link JComponent} that serves as the UI component for the input.
         * @param getParameter A {@link Supplier} that provides a list of parameter values for the option.
         * @param setParameter A {@link Consumer} that allows setting a collection of parameter values
         *                     for the option.
         */
        public record OptionInput(Option<?> option, JComponent component, Supplier<List<String>> getParameter,
                                  Consumer<Collection<String>> setParameter) {
        }

        private final Map<Option<?>, OptionInput> inputs = new IdentityHashMap<>();

        /**
         * Constructs a new ArgumentsPanel with the specified arguments.
         *
         * @param parser  the ArgumentsParser object used to retrieve the options
         * @param onOk  the Runnable object to be executed when the OK button is clicked
         * @param onCancel  the Runnable object to be executed when the Cancel button is clicked
         */
        public ArgumentsPanel(ArgumentsParser parser, Runnable onOk, Runnable onCancel) {
            super(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy = 0;
            Insets insetsLeft = new Insets(0, 8, 0, 4);
            Insets insetsRight = new Insets(0, 0, 0, 8);
            for (var option : parser.options()) {
                constraints.gridx = 0;
                constraints.fill = GridBagConstraints.NONE;
                constraints.weightx = 0;
                constraints.insets = insetsLeft;
                add(new JLabel(option.displayName() + ":"), constraints);

                constraints.gridx = 1;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1;
                constraints.insets = insetsRight;
                JComponent input = createInput(option);
                add(input, constraints);

                constraints.gridy++;
            }

            constraints.gridx = 0;
            constraints.gridwidth = 2;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createGlue());
            buttonBox.add(new JButton(SwingUtil.createAction("OK", onOk)));
            buttonBox.add(new JButton(SwingUtil.createAction("Cancel", onCancel)));
            buttonBox.add(Box.createGlue());
            add(buttonBox, constraints);
        }

        private JComponent createInput(Option<?> option) {
            JTextField input = new JTextField(20);
            option.getDefaultString().ifPresent(input::setText);
            OptionInput optionInput = new OptionInput(
                    option,
                    input,
                    () -> List.of(input.getText()),
                    strings -> input.setText(String.join(" ", strings))
            );
            inputs.put(option, optionInput);
            return input;
        }

        /**
         * Returns an unmodifiable map containing the input options and their corresponding OptionInput components.
         *
         * @return an unmodifiable map of Option objects to OptionInput objects
         */
        public Map<Option<?>, OptionInput> getInputs() {
            return Collections.unmodifiableMap(inputs);
        }

        /**
         * Returns the arguments generated based on the input options and their corresponding OptionInput components.
         *
         * @return the generated Arguments object
         */
        public Arguments getArguments() {
            List<Arguments.Entry<?>> parsedOptions = inputs.values().stream()
                    .map(oi -> {
                        List<String> strings = oi.getParameter().get();
                        return oi.option.isFlag() && strings.size() == 1
                                ? Arguments.Entry.create((Option<Boolean>) oi.option, Boolean.parseBoolean(strings.getFirst()))
                                : oi.option.map(strings);
                    })
                    .collect(Collectors.toUnmodifiableList());
            return new Arguments(parsedOptions, Collections.emptyList());
        }
    }

    /**
     * Creates a new ArgumentsDialog with the specified owner, parser, and title.
     * The dialog is displayed as a modal dialog and blocks input from other windows.
     * The dialog contains an ArgumentsPanel with the specified parser and callback methods for when the dialog is closed or canceled.
     *
     * @param owner the owner Window of the dialog
     * @param parser the ArgumentsParser used by the dialog to parse arguments
     * @param title the title of the dialog
     */
    public ArgumentsDialog(Window owner, ArgumentsParser parser, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.panel = new ArgumentsPanel(parser, this::dialogClosed, this::dialogCancelled);

        setContentPane(panel);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    /**
     * Displays a dialog for getting arguments from the user.
     *
     * @param owner   the Window object that owns the dialog
     * @param parser  the ArgumentsParser object used to parse the arguments
     * @return an Optional containing the Arguments obtained from the user, or an empty Optional if the user cancels the dialog
     */
    public static Optional<Arguments> showDialog(Window owner, ArgumentsParser parser) {
        ArgumentsDialog dlg = new ArgumentsDialog(owner, parser, "");
        return Optional.ofNullable(dlg.arguments);
    }

    private transient @Nullable Arguments arguments;

    private void dialogClosed() {
        arguments = panel.getArguments();
        dispose();
    }

    private void dialogCancelled() {
        arguments = null;
        dispose();
    }
}