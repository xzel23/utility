package com.dua3.utility.samples;

import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.Repetitions;
import com.dua3.utility.swing.ArgumentsDialog;
import com.dua3.utility.swing.ComboBoxEx;
import com.dua3.utility.swing.SwingUtil;
import net.miginfocom.swing.MigLayout;
import org.slb4j.SLB4J;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The SwingComboBoxExSample class demonstrates a sample Swing application
 * with an enhanced ComboBox component using {@code ComboBoxEx}. This application
 * allows for adding, editing, and removing {@code Person} objects in the ComboBox,
 * with user-friendly interaction dialogs.
 * <p>
 * This class extends {@code JFrame} to create the Swing GUI and uses the
 * {@code MigLayout} layout manager for flexible component positioning.
 */
public class SwingComboBoxExSample extends JFrame {

    static {
        SLB4J.init();
    }

    /**
     * The entry point to execute the `SwingComboBoxExSample` application.
     * This method sets the native look and feel of the UI, and then initializes
     * and displays the application's main frame using the Swing event dispatch thread.
     *
     * @param args Command-line arguments passed to the program. These arguments
     *             are not used in this application.
     */
    public static void main(String[] args) {
        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComboBoxExSample instance = new SwingComboBoxExSample();
            instance.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    /**
     * Constructs a new instance of SwingComponentsSampleLogBase.
     * <p>
     * This constructor initializes the frame layout using the MigLayout manager,
     * defines the minimum size of the frame, and sets its initial size. Additionally,
     * it calls the {@code init()} method to set up the frame's components and
     * functionalities.
     */
    protected SwingComboBoxExSample() {
        setLayout(new MigLayout("fill", "[grow,fill]", "[][][grow,fill]"));
        setMinimumSize(new Dimension(400, 400));
        setSize(800, 600);

        init();
    }

    static class Person {
        String firstName;
        String lastName;

        Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return Stream.of(lastName, firstName).filter(Objects::nonNull).collect(Collectors.joining(", "));
        }
    }

    private Person showPersonDialog() {
        return showPersonDialog(null);
    }

    private Person showPersonDialog(Person initialPerson) {
        Optional<Person> op = Optional.ofNullable(initialPerson);
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        var optFirstName = builder.addStringOption(
                "First name",
                "The person's first name.",
                Repetitions.EXACTLY_ONE,
                "first-name",
                () -> op.map(pp -> pp.firstName).orElse(""),
                "--first-name"
        );
        var optLastName = builder.addStringOption(
                "Last name",
                "The person's last or family name.",
                Repetitions.EXACTLY_ONE,
                "last-name",
                () -> op.map(pp -> pp.lastName).orElse(""),
                "--last-name"
        );

        ArgumentsParser parser = builder.build();

        return ArgumentsDialog.showDialog(this, parser)
                .map(args -> {
                    Person p = new Person("", "");
                    p.firstName = args.get(optFirstName).orElse("");
                    p.lastName = args.get(optLastName).orElse("");
                    return p;
                })
                .orElse(null);
    }

    private void init() {

        // -- ComboboxEx
        ComboBoxEx<Person> comboBoxEx = new ComboBoxEx<>(
                this::showPersonDialog,
                this::showPersonDialog,
                ComboBoxEx::askBeforeRemoveSelectedItem,
                Object::toString,
                new Person("John", "Doe"),
                new Person("Jane", "Doe"),
                new Person("Baby", "Doe"),
                new Person("Richard", "Roe"),
                new Person("Jeanny", "Roe")
        );
        comboBoxEx.setComparator(Comparator.comparing(Person::toString));
        // add components
        add(comboBoxEx, "wrap");
    }
}
