package com.dua3.utility.swing;

import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ArgumentsDialog extends JDialog {

    private static final Logger LOG = LoggerFactory.getLogger(ArgumentsDialog.class);
    private final ArgumentsPanel panel;

    public static class ArgumentsPanel extends JPanel {
        public record OptionInput(Option<?> option, JComponent component, Supplier<List<String>> getParameter, Consumer<Collection<String>> setParameter) {
        }

        private ArgumentsParser parser;
        private Map<Option<?>, OptionInput> inputs = new IdentityHashMap<>();

        public ArgumentsPanel(ArgumentsParser parser, Runnable onOk, Runnable onCancel) {
            super(new GridBagLayout());
            this.parser = parser;

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridy = 0;
            Insets insetsLeft = new Insets(0,8,0, 4);
            Insets insetsRight = new Insets(0,0,0, 8);
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

        public Map<Option<?>, OptionInput> getInputs() {
            return Collections.unmodifiableMap(inputs);
        }

        public Arguments getArguments() {
            List<? extends Arguments.Entry<?>> parsedOptions = inputs.values().stream()
                    .map( oi -> {
                        var option = oi.option();
                        var entry = Arguments.createEntry(oi.option);
                        oi.getParameter().get().forEach(entry::addParameter);
                        return entry;
                    })
                    .toList();
            return new Arguments(parsedOptions, Collections.emptyList());
        }
    }

    public ArgumentsDialog(Window owner, ArgumentsParser parser, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.panel = new ArgumentsPanel(parser, this::dialogClosed, this::dialogCancelled);

        setContentPane(panel);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    public static Optional<Arguments> showDialog(Window owner, ArgumentsParser parser) {
        ArgumentsDialog dlg = new ArgumentsDialog(owner, parser, "");
        return Optional.ofNullable(dlg.arguments);
    }

    private Arguments arguments = null;

    private void dialogClosed() {
        arguments = panel.getArguments();
        dispose();
    }

    private void dialogCancelled() {
        arguments = null;
        dispose();
    }
}