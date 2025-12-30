package com.dua3.utility.fx.controls;

import com.dua3.utility.crypt.PasswordUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder class for creating input controls.
 * <p>
 * The InputBuilder class provides methods to add various types of labeled
 * and unlabeled input controls such as text fields, checkboxes, and combo boxes
 * to a UI form. It allows customization of the control's ID, label text, default
 * value, and validation rules. The builder pattern allows chaining of method calls
 * to create complex input forms with ease.
 *
 * @param <B> the generic type of the {@link InputBuilder}
 */
public interface InputBuilder<B extends InputBuilder<B>> {

    /**
     * Formats the given message using the specified arguments.
     *
     * @param message the message template which can include placeholders for arguments
     * @param args    the arguments to be formatted and substituted into the message template
     * @return the formatted message as a String
     */
    default String format(String message,@Nullable Object... args) {
        return getMessageFormatter().format(message, args);
    }

    /**
     * Retrieves an instance of the MessageFormatter.
     *
     * @return an instance of MessageFormatter that handles the formatting of messages.
     */
    MessageFormatter getMessageFormatter();

    /**
     * Retrieves the style configuration for a specific section level.
     *
     * @param level the level of the section for which the style is to be retrieved
     * @return the SectionStyle associated with the specified section level
     */
    SectionStyle getSectionStyle(int level);

    /**
     * Add a labeled input control.
     *
     * @param <T>     the result type
     * @param id      the control's ID
     * @param label   the label text
     * @param type    the result type
     * @param dflt    supplier of default value
     * @param control the control
     * @param visible flag indicating whether the input is visible to the user
     * @return {@code this}
     */
    default <T> B addInput(String id, String label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean visible) {
        return addInput(id, new MessageFormatter.MessageFormatterArgs(label), type, dflt, control, visible);
    }

    /**
     * Sets the preferred width.
     *
     * @param value the preferred width value
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B prefWidth(double value);

    /**
     * Sets the preferred height.
     *
     * @param value the preferred height value in pixels
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B prefHeight(double value);

    /**
     * Sets the minimum width.
     *
     * @param value the minimum width value to be set
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B minWidth(double value);

    /**
     * Sets the minimum height.
     *
     * @param value the minimum height value to set
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B minHeight(double value);

    /**
     * Sets the maximum width.
     *
     * @param value the maximum width to be set
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B maxWidth(double value);

    /**
     * Sets the maximum height.
     *
     * @param value the maximum height value to set
     * @return the current builder instance of type {@code B}, to allow method chaining
     */
    B maxHeight(double value);

    /**
     * Sets the marker symbols.
     *
     * @param markerSymbols the MarkerSymbols instance representing the symbols to be assigned
     * @return {@code this}
     */
    B markerSymbols(MarkerSymbols markerSymbols);

    /**
     * Add a labeled input control.
     *
     * @param <T>     the result type
     * @param id      the control's ID
     * @param label   the label text
     * @param type    the result type
     * @param dflt    supplier of default value
     * @param control the control
     * @param visible  flag, indicating whether the input us visible from the user (no control is visible on the UI)
     * @return {@code this}
     */
    <T> B addInput(String id, MessageFormatter.MessageFormatterArgs label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean visible);

    /**
     * Add an unlabeled input control.
     *
     * @param <T>     the result type
     * @param id      the control's ID
     * @param type    the result type
     * @param dflt    supplier of default value
     * @param control the control
     * @return {@code this}
     */
    <T> B addInput(String id, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control);

    /**
     * Set the number of columns for layout (default is 1).
     *
     * @param columns the number of columns for laying out the input controls
     * @return {@code this}
     */
    B columns(int columns);

    /**
     * Set the minimum row height.
     *
     * @param height the height
     * @param unit the unit
     * @return {@code this}
     */
    B minRowHeight(double height, LayoutUnit unit);

    /**
     * Set the minimum row height.
     *
     * @param height the height in EM
     * @return {@code this}
     */
    default B minRowHeight(double height) {
        return minRowHeight(height, LayoutUnit.EM);
    }

    /**
     * Set the label placement.
     *
     * @param labelPlacement the label placement
     * @return this builder instance
     */
    B labelPlacement(LabelPlacement labelPlacement);

    /**
     * Add vertical space before the next row.
     *
     * @param height the height of the space
     * @param unit the unit of the height
     * @return this builder instance
     */
    B verticalSpace(double height, LayoutUnit unit);

    /**
     * Add vertical space of 1 em before the next row.
     *
     * @return this builder instance
     */
    default B verticalSpace() {
        return verticalSpace(1.0, LayoutUnit.EM);
    }

    /**
     * Processes the given node and returns a result of type B.
     *
     * @param node the node to be processed
     * @return the result of processing the given node, of type B
     */
    B node(Node node);

    /**
     * Associates a child node with this node using the specified label.
     *
     * @param label the label used to identify the child node
     * @param node  the child node to be associated
     * @return the updated instance of the current object
     */
    default B node(String label, Node node) {
        return node(new MessageFormatter.MessageFormatterArgs(label), node);
    }

    /**
     * Associates a child node with this node using the specified label.
     *
     * @param label the label used to identify the child node
     * @param node  the child node to be associated
     * @return the updated instance of the current object
     */
    B node(MessageFormatter.MessageFormatterArgs label, Node node);

    /**
     * Sets the current section level and formats the section heading
     * using the provided format string and arguments.
     *
     * @param level the level of the section
     * @param fmt   the format string used to create the section heading
     * @param args  the arguments referenced by the format specifiers in the format string
     * @return {@code this}
     */
    B section(int level, String fmt, Object... args);

    /**
     * Add a static text without a label.
     *
     * @param fmt  the formatting pattern
     * @param args the formatting arguments
     * @return {@code this}
     */
    B text(String fmt, Object... args);

    /**
     * Add a static text without a label.
     *
     * <p>
     * Arguments are shared for both patterns
     *
     * @param fmtLabel the label pattern
     * @param fmtText  the text pattern
     * @param args     the formatting arguments
     * @return {@code this}
     */
    B labeledText(String fmtLabel, String fmtText, Object... args);

    /**
     * Creates a disabled input field with the specified configurations.
     * <p>
     * Use this method to include values in the form data that are not editable by the user.
     *
     * @param <T>   The type of the value managed by this input.
     * @param id    The unique identifier for the input field.
     * @param label The label to display for the input field.
     * @param value A supplier that provides the value to be displayed in the input field.
     * @param cls   The class type of the value provided.
     * @return An instance of `B` representing the configured disabled input field.
     */
    default <T> B inputConstant(String id, String label, Supplier<T> value, Class<T> cls) {
        return inputConstant(id, new MessageFormatter.MessageFormatterArgs(label), value, cls);
    }

    /**
     * Creates a disabled input field with the specified configurations.
     * <p>
     * Use this method to include values in the form data that are not editable by the user.
     *
     * @param <T>   The type of the value managed by this input.
     * @param id    The unique identifier for the input field.
     * @param label The label to display for the input field.
     * @param value A supplier that provides the value to be displayed in the input field.
     * @param cls   The class type of the value provided.
     * @return An instance of `B` representing the configured disabled input field.
     */
    <T> B inputConstant(String id, MessageFormatter.MessageFormatterArgs label, Supplier<T> value, Class<T> cls);

    /**
     * Configures a non-editable input field with the specified parameters.
     * <p>
     * Use this method to include values in the form data that are not editable by the user.
     *
     * @param id    the unique identifier for the input field
     * @param label the label associated with the input field
     * @param value the value to be displayed in the disabled input field
     * @param <T>   the type of the value contained in the input field
     * @return an instance of type B representing the configured disabled input field
     */
    default <T> B inputConstant(String id, String label, T value) {
        return inputConstant(id, new MessageFormatter.MessageFormatterArgs(label), value);
    }

    /**
     * Configures a non-editable input field with the specified parameters.
     * <p>
     * Use this method to include values in the form data that are not editable by the user.
     *
     * @param id    the unique identifier for the input field
     * @param label the label associated with the input field
     * @param value the value to be displayed in the disabled input field
     * @param <T>   the type of the value contained in the input field
     * @return an instance of type B representing the configured disabled input field
     */
    <T> B inputConstant(String id, MessageFormatter.MessageFormatterArgs label, T value);

    /**
     * Creates a hidden field with the specified configurations.
     * <p>
     * Use this method to include values in the form data that are not visible to the user.
     *
     * @param <T>   The type of the value managed by this input.
     * @param id    The unique identifier for the input field.
     * @param value A supplier that provides the value for the hidden input field.
     * @param cls   The class type of the value provided.
     * @return An instance of {@code B} representing the configured hidden input field.
     */
    <T> B inputHidden(String id, Supplier<T> value, Class<T> cls);

    /**
     * Creates a hidden field with a constant value.
     * <p>
     * Use this method to include values in the form data that are not visible to the user.
     *
     * @param <T>   the type of the value contained in the input field
     * @param id    the unique identifier for the input field
     * @param value the constant value for the hidden input field
     * @return an instance of type {@code B} representing the configured hidden input field
     */
    <T> B inputHidden(String id, T value);

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a multi-line texts. See {@link #inputString(String, String, Supplier)}
     * for a control that allows inputting single line texts.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputText(String id, String label, Supplier<@Nullable String> dflt) {
        return inputText(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Add a labeled text input.
     * <p>
     * This control is for inputting a multi-line texts. See {@link #inputString(String, String, Supplier, Function)}
     * for a control that allows inputting single line texts.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    default B inputText(String id, String label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return inputText(id, new MessageFormatter.MessageFormatterArgs(label), dflt, validate);
    }

    /**
     * Add a labeled text input.
     * <p>
     * This control is for inputting a multi-line text. See {@link #inputString(String, String, Supplier, Function)}
     * for a control that allows inputting single line texts.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    B inputText(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate);

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a multi-line texts. See {@link #inputString(String, String, Supplier)}
     * for a control that allows inputting single line texts.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputText(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt) {
        return inputText(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a single line of text. See {@link #inputText(String, String, Supplier)}
     * for a control that allows inputting longer texts.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputString(String id, String label, Supplier<@Nullable String> dflt) {
        return inputString(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a single line of text. See {@link #inputText(String, String, Supplier, Function)}
     * for a control that allows inputting longer texts.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    default B inputString(String id, String label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return inputString(id, new MessageFormatter.MessageFormatterArgs(label), dflt, validate);
    }

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a single line of text. See {@link #inputText(String, String, Supplier, Function)}
     * for a control that allows inputting longer texts.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    B inputString(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate);

    /**
     * Add a labeled string input.
     * <p>
     * This control is for inputting a single line of text. See {@link #inputText(String, String, Supplier)}
     * for a control that allows inputting longer texts.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputString(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt) {
        return inputString(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Add a labeled password input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputPassword(String id, String label, Supplier<@Nullable String> dflt) {
        return inputPassword(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Add a labeled string input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    default B inputPassword(String id, String label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return inputPassword(id, new MessageFormatter.MessageFormatterArgs(label), dflt, validate);
    }

    /**
     * Add a labeled string input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    B inputPassword(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate);

    /**
     * Add a labeled password input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputPassword(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt) {
        return inputPassword(id, label, dflt, s -> Optional.empty());
    }

    /**
     * Allows user to input a password along with a verification step where the password
     * is repeated and validated for strength and consistency.
     *
     * @param id          the identifier for the password input field
     * @param label       the label displayed for the initial password input field
     * @param labelRepeat the label displayed for the repeated password input field
     * @return an instance of the builder type B after the password input process
     */
    default B inputPasswordWithVerification(String id, String label, String labelRepeat) {
        return inputPasswordWithVerification(id, new MessageFormatter.MessageFormatterArgs(label), new MessageFormatter.MessageFormatterArgs(labelRepeat));
    }

    /**
     * Allows user to input a password along with a verification step where the password
     * is repeated and validated for strength and consistency.
     *
     * @param id          the identifier for the password input field
     * @param label       the label displayed for the initial password input field
     * @param labelRepeat the label displayed for the repeated password input field
     * @return an instance of the builder type B after the password input process
     */
    default B inputPasswordWithVerification(String id, MessageFormatter.MessageFormatterArgs label, MessageFormatter.MessageFormatterArgs labelRepeat) {
        AtomicReference<@Nullable String> passwordRef = new AtomicReference<>(null);
        return inputPassword(id, label, () -> "", s -> {
            if (s == null) {
                s = "";
            }
            passwordRef.set(s);
            PasswordUtil.PasswordStrength strength = PasswordUtil.evaluatePasswordStrength(s.toCharArray());
            if (strength.strengthLevel().compareTo(PasswordUtil.StrengthLevel.MODERATE) < 0) {
                return Optional.of("Password is too weak: " + strength.strengthLevel());
            } else {
                return Optional.empty();
            }
        }).inputPassword("", labelRepeat, () -> "", s -> Objects.equals(s, passwordRef.get()) ? Optional.empty() : Optional.of("Passwords do not match."));
    }

    /**
     * Add a labeled integer input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputInteger(String id, String label, Supplier<@Nullable Long> dflt) {
        return inputInteger(id, label, dflt, i -> Optional.empty());
    }

    /**
     * Add a labeled integer input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    default B inputInteger(String id, String label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        return inputInteger(id, new MessageFormatter.MessageFormatterArgs(label), dflt, validate);
    }

    /**
     * Add a labeled integer input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    B inputInteger(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate);

    /**
     * Add a labeled integer input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputInteger(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Long> dflt) {
        return inputInteger(id, label, dflt, i -> Optional.empty());
    }

    /**
     * Add a labeled decimal input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputDecimal(String id, String label, Supplier<@Nullable Double> dflt) {
        return inputDecimal(id, label, dflt, d -> Optional.empty());
    }

    /**
     * Add a labeled decimal input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    default B inputDecimal(String id, String label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        return inputDecimal(id, new MessageFormatter.MessageFormatterArgs(label), dflt, validate);
    }

    /**
     * Add a labeled decimal input.
     *
     * @param id       the ID
     * @param label    the label text
     * @param dflt     supplier of default value
     * @param validate validation callback, return error message if invalid, empty optional if valid
     * @return {@code this}
     */
    B inputDecimal(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate);

    /**
     * Add a labeled decimal input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B inputDecimal(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt) {
        return inputDecimal(id, label, dflt, d -> Optional.empty());
    }

    /**
     * Add a labeled checkbox.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param text  the checkbox text
     * @return {@code this}
     */
    default B inputCheckBox(String id, String label, BooleanSupplier dflt, String text) {
        return inputCheckBox(id, label, dflt, text, b -> Optional.empty());
    }

    /**
     * Creates a checkbox with the given parameters.
     *
     * @param id       the ID of the checkbox
     * @param label    the label for the checkbox
     * @param dflt     the default value of the checkbox
     * @param text     the text to display next to the checkbox
     * @param validate a function that takes a Boolean value and returns an optional validation message
     * @return {@code this}
     */
    default B inputCheckBox(String id, String label, BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        return inputCheckBox(id, new MessageFormatter.MessageFormatterArgs(label), dflt, text, validate);
    }

    /**
     * Creates a checkbox with the given parameters.
     *
     * @param id       the ID of the checkbox
     * @param label    the label for the checkbox
     * @param dflt     the default value of the checkbox
     * @param text     the text to display next to the checkbox
     * @param validate a function that takes a Boolean value and returns an optional validation message
     * @return {@code this}
     */
    B inputCheckBox(String id, MessageFormatter.MessageFormatterArgs label, BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate);

    /**
     * Add a labeled checkbox.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param text  the checkbox text
     * @return {@code this}
     */
    default B inputCheckBox(String id, MessageFormatter.MessageFormatterArgs label, BooleanSupplier dflt, String text) {
        return inputCheckBox(id, label, dflt, text, b -> Optional.empty());
    }

    /**
     * Add a labeled combobox for selecting a value from an enum.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the enum class
     * @return {@code this}
     */
    default <T extends Enum<T>> B inputComboBox(String id, String label, Supplier<@Nullable T> dflt, Class<T> cls) {
        return inputComboBox(id, label, dflt, cls, LangUtil.enumValues(cls), t -> Optional.empty());
    }

    /**
     * Creates a comboBox widget with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of the comboBox items
     * @param items    the collection of items to populate the comboBox
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, T[] items, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBox(id, label, dflt, cls, List.of(items), validate);
    }

    /**
     * Creates a comboBox widget with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of the comboBox items
     * @param items    the collection of items to populate the comboBox
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBox(id, new MessageFormatter.MessageFormatterArgs(label), dflt, cls, items, validate);
    }

    /**
     * Creates a comboBox widget with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of the comboBox items
     * @param items    the collection of items to populate the comboBox
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    <T> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate);

    /**
     * Add a labeled combobox for selecting a value from an enum.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the enum class
     * @return {@code this}
     */
    default <T extends Enum<T>> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable T> dflt, Class<T> cls) {
        return inputComboBox(id, label, dflt, cls, LangUtil.enumValues(cls), t -> Optional.empty());
    }

    /**
     * Creates a comboBox widget with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of the comboBox items
     * @param items    the collection of items to populate the comboBox
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, T[] items, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBox(id, label, dflt, cls, List.of(items), validate);
    }

    /**
     * Add a labeled combobox.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, T[] items) {
        return inputComboBox(id, label, dflt, cls, List.of(items));
    }

    /**
     * Add a labeled combobox.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputComboBox(id, label, dflt, cls, items, t -> Optional.empty());
    }

    /**
     * Add a labeled combobox.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, T[] items) {
        return inputComboBox(id, label, dflt, cls, List.of(items));
    }

    /**
     * Add a labeled combobox.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputComboBox(id, label, dflt, cls, items, t -> Optional.empty());
    }

    /**
     * Creates a comboBox widget to select an enum value with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of enum
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    default <T extends Enum<T>> B inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBox(id, label, dflt, cls, LangUtil.enumValues(cls), validate);
    }

    /**
     * Creates a comboBox widget to select an enum value with the given parameters.
     *
     * @param id       the unique identifier for the comboBox
     * @param label    the label to display with the comboBox
     * @param dflt     the supplier function to provide the default value for the comboBox
     * @param cls      the class type of enum
     * @param validate the function to validate the selected item in the comboBox
     * @param <T>      the type of the comboBox items
     * @return {@code this}
     */
    default <T extends Enum<T>> B inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBox(id, label, dflt, cls, LangUtil.enumValues(cls), validate);
    }

    /**
     * Adds a labeled combo box control with extended functionality.
     *
     * @param <T>    the item type
     * @param id     the control's ID
     * @param label  the label text
     * @param edit   the action to be performed when an item is edited (optional)
     * @param add    the action to be performed when a new item is added (optional)
     * @param remove the action to be performed when an item is removed (optional)
     * @param format the function to format the items in the combo box
     * @param dflt   the supplier of the default value
     * @param cls    the result class of the combo box items
     * @param items  the collection of items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBoxEx(String id, String label, @Nullable Function<T, @Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputComboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, t -> Optional.empty());
    }

    /**
     * Returns a custom combo box with the specified parameters.
     *
     * @param <T>      the type of objects in the combo box
     * @param id       the ID of the combo box
     * @param label    the label of the combo box
     * @param edit     a function to modify the selected item in the combo box, or null if editing is not allowed
     * @param add      a supplier to add a new item to the combo box, or null if adding is not allowed
     * @param remove   a predicate to remove an item from the combo box, or null if removing is not allowed
     * @param format   a function to format the items of the combo box as strings
     * @param dflt     a supplier to provide a default item for the combo box
     * @param cls      the class of objects in the combo box
     * @param items    the collection of items to populate the combo box
     * @param validate a function to validate the items in the combo box and return an optional error message
     * @return {@code this}
     */
    default <T> B inputComboBoxEx(String id, String label, @Nullable Function<T, @Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return inputComboBoxEx(id, new MessageFormatter.MessageFormatterArgs(label), edit, add, remove, format, dflt, cls, items, validate);
    }

    /**
     * Returns a custom combo box with the specified parameters.
     *
     * @param <T>      the type of objects in the combo box
     * @param id       the ID of the combo box
     * @param label    the label of the combo box
     * @param edit     a function to modify the selected item in the combo box, or null if editing is not allowed
     * @param add      a supplier to add a new item to the combo box, or null if adding is not allowed
     * @param remove   a predicate to remove an item from the combo box, or null if removing is not allowed
     * @param format   a function to format the items of the combo box as strings
     * @param dflt     a supplier to provide a default item for the combo box
     * @param cls      the class of objects in the combo box
     * @param items    the collection of items to populate the combo box
     * @param validate a function to validate the items in the combo box and return an optional error message
     * @return {@code this}
     */
    <T> B inputComboBoxEx(String id, MessageFormatter.MessageFormatterArgs label, @Nullable Function<T, @Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate);

    /**
     * Adds a labeled combo box control with extended functionality.
     *
     * @param <T>    the item type
     * @param id     the control's ID
     * @param label  the label text
     * @param edit   the action to be performed when an item is edited (optional)
     * @param add    the action to be performed when a new item is added (optional)
     * @param remove the action to be performed when an item is removed (optional)
     * @param format the function to format the items in the combo box
     * @param dflt   the supplier of the default value
     * @param cls    the result class of the combo box items
     * @param items  the collection of items to choose from
     * @return {@code this}
     */
    default <T> B inputComboBoxEx(String id, MessageFormatter.MessageFormatterArgs label, @Nullable Function<T, @Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputComboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, t -> Optional.empty());
    }

    /**
     * Add a labeled list of radiobuttons.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputRadioList(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputRadioList(id, label, dflt, cls, items, (@Nullable T t) -> t != null ? Optional.empty() : Optional.of("No option selected"));
    }

    /**
     * Creates a radio list component.
     *
     * @param id       the ID of the radio list
     * @param label    the label text for the radio list
     * @param dflt     a supplier that provides the default value for the radio list
     * @param cls      the class of the items in the radio list
     * @param items    a collection of items for the radio list
     * @param validate a function to validate the selected item, returning an optional error message
     * @param <T>      the type of items in the radio list
     * @return {@code this}
     */
    default <T> B inputRadioList(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return inputRadioList(id, new MessageFormatter.MessageFormatterArgs(label), dflt, cls, items, validate);
    }

    /**
     * Creates a radio list component.
     *
     * @param id       the ID of the radio list
     * @param label    the label text for the radio list
     * @param dflt     a supplier that provides the default value for the radio list
     * @param cls      the class of the items in the radio list
     * @param items    a collection of items for the radio list
     * @param validate a function to validate the selected item, returning an optional error message
     * @param <T>      the type of items in the radio list
     * @return {@code this}
     */
    <T> B inputRadioList(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate);

    /**
     * Add a labeled list of radiobuttons.
     *
     * @param <T>   the item type
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param cls   the result class
     * @param items the items to choose from
     * @return {@code this}
     */
    default <T> B inputRadioList(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items) {
        return inputRadioList(id, label, dflt, cls, items, (@Nullable T t) -> t != null ? Optional.empty() : Optional.of("No option selected"));
    }

    /**
     * Creates a slider component.
     *
     * @param id    the ID of the radio list
     * @param label the label text for the radio list
     * @param dflt  a supplier that provides the default value for the radio list
     * @param min   the minimum value
     * @param max   the maximum value
     * @return {@code this}
     */
    default B inputSlider(String id, String label, Supplier<@Nullable Double> dflt, double min, double max) {
        return inputSlider(id, new MessageFormatter.MessageFormatterArgs(label), dflt, min, max);
    }

    /**
     * Creates a slider component.
     *
     * @param id    the ID of the radio list
     * @param label the label text for the radio list
     * @param dflt  a supplier that provides the default value for the radio list
     * @param min   the minimum value
     * @param max   the maximum value
     * @return {@code this}
     */
    B inputSlider(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, double min, double max);

    /**
     * Add a labeled pane with options.
     *
     * @param id      the ID
     * @param label   the label text
     * @param dflt    supplier of default values
     * @param options supplier of options
     * @return {@code this}
     */
    default B inputOptions(String id, String label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return inputOptions(id, new MessageFormatter.MessageFormatterArgs(label), dflt, options);
    }

    /**
     * Add a labeled pane with options.
     *
     * @param id      the ID
     * @param label   the label text
     * @param dflt    supplier of default values
     * @param options supplier of options
     * @return {@code this}
     */
    B inputOptions(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options);

    /**
     * Add an unlabeled pane with options.
     * <p>
     * <em>Note to implementers:</em> Labels of the options should be aligned properly with labels of the input dialog.
     * </p>
     *
     * @param id      the ID
     * @param dflt    supplier of default values
     * @param options supplier of options
     * @return {@code this}
     */
    B inputOptions(String id, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options);

    /**
     * Add a file chooser.
     *
     * @param id           the ID
     * @param label        the label text
     * @param dflt         supplier of default value
     * @param mode         the mode, either {@link FileDialogMode#OPEN} or {@link FileDialogMode#SAVE}
     * @param existingOnly only let the user choose existing files; i.e., no new files can be created
     * @param filter       the extension filter to use
     * @return {@code this}
     */
    default B inputFile(String id, String label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter) {
        return inputFile(id, new MessageFormatter.MessageFormatterArgs(label), dflt, mode, existingOnly, filter, FileInput.defaultValidate(mode, existingOnly));
    }

    /**
     * Add a file chooser dialog to allow the user to select a file.
     *
     * @param id           The identifier for the file chooser dialog.
     * @param label        The label to display in the file chooser dialog.
     * @param dflt         A function that provides the default path to preselect in the file chooser dialog.
     * @param mode         The mode of the file dialog, such as OPEN or SAVE.
     * @param existingOnly Whether to only allow selection of existing files.
     * @param filter       The file filters to apply in the file chooser dialog.
     * @param validate     A function to perform additional validation on the selected file path.
     *                     It returns an optional error message if the validation fails.
     * @return {@code this}
     */
    B inputFile(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate);

    /**
     * Add a file chooser.
     *
     * @param id           the ID
     * @param label        the label text
     * @param dflt         supplier of default value
     * @param mode         the mode, either {@link FileDialogMode#OPEN} or {@link FileDialogMode#SAVE}
     * @param existingOnly only let the user choose existing files; i.e., no new files can be created
     * @param filter       the extension filter to use
     * @return {@code this}
     */
    default B inputFile(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter) {
        return inputFile(id, label, dflt, mode, existingOnly, filter, FileInput.defaultValidate(mode, existingOnly));
    }

    /**
     * Add a file chooser dialog to allow the user to select a file.
     *
     * @param id           The identifier for the file chooser dialog.
     * @param label        The label to display in the file chooser dialog.
     * @param dflt         A function that provides the default path to preselect in the file chooser dialog.
     * @param mode         The mode of the file dialog, such as OPEN or SAVE.
     * @param existingOnly Whether to only allow selection of existing files.
     * @param filter       The file filters to apply in the file chooser dialog.
     * @param validate     A function to perform additional validation on the selected file path.
     *                     It returns an optional error message if the validation fails.
     * @return {@code this}
     */
    default B inputFile(String id, String label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate) {
        return inputFile(id, new MessageFormatter.MessageFormatterArgs(label), dflt, mode, existingOnly, filter, validate);
    }

    /**
     * Configures an input field for selecting a folder.
     *
     * @param id           the unique identifier for this input field
     * @param label        the display label for the input field
     * @param dflt         a supplier providing the default selected folder path, which can be null
     * @param existingOnly a boolean indicating if only existing directories should be selectable
     * @return an instance of type B after configuring the folder input field
     */
    default B inputFolder(String id, String label, Supplier<@Nullable Path> dflt, boolean existingOnly) {
        return inputFolder(id, new MessageFormatter.MessageFormatterArgs(label), dflt, existingOnly, FileInput.defaultValidate(FileDialogMode.DIRECTORY, existingOnly));
    }

    /**
     * Configures an input for selecting a folder.
     *
     * @param id           the identifier for the input
     * @param label        the label text for the input
     * @param dflt         a supplier providing the default folder path
     * @param existingOnly a flag indicating whether only existing folders are allowed
     * @param validate     a function to validate the selected folder path
     * @return an instance of the builder configured with the folder input
     */
    default B inputFolder(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, boolean existingOnly, Function<@Nullable Path, Optional<String>> validate) {
        return inputFile(id, label, dflt, FileDialogMode.DIRECTORY, existingOnly, List.of(), validate);
    }

    /**
     * Configures an input field for selecting a folder.
     *
     * @param id           the unique identifier for this input field
     * @param label        the display label for the input field
     * @param dflt         a supplier providing the default selected folder path, which can be null
     * @param existingOnly a boolean indicating if only existing directories should be selectable
     * @return an instance of type B after configuring the folder input field
     */
    default B inputFolder(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, boolean existingOnly) {
        return inputFolder(id, label, dflt, existingOnly, FileInput.defaultValidate(FileDialogMode.DIRECTORY, existingOnly));
    }

    /**
     * Configures an input for selecting a folder.
     *
     * @param id           the identifier for the input
     * @param label        the label text for the input
     * @param dflt         a supplier providing the default folder path
     * @param existingOnly a flag indicating whether only existing folders are allowed
     * @param validate     a function to validate the selected folder path
     * @return an instance of the builder configured with the folder input
     */
    default B inputFolder(String id, String label, Supplier<@Nullable Path> dflt, boolean existingOnly, Function<@Nullable Path, Optional<String>> validate) {
        return inputFile(id, new MessageFormatter.MessageFormatterArgs(label), dflt, FileDialogMode.DIRECTORY, existingOnly, List.of(), validate);
    }

    /**
     * Associates an input control with the specified identifier, type, and default value provider.
     *
     * @param <T>     the generic result type
     * @param id      the unique identifier for the input control
     * @param control the input control to be associated
     * @param type    the class type of the value managed by the input control
     * @param dflt    a supplier for the default value, which may produce a null value
     * @return an instance of type B for method chaining or further configuration
     */
    <T> B inputControl(String id, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt);

    /**
     * Configures an input control with the specified parameters.
     *
     * @param <T>     the generic result type
     * @param id      the unique identifier for the input control
     * @param label   the label to be associated with the input control
     * @param control the input control instance to be configured
     * @param type    the type of the input value
     * @param dflt    a supplier providing the default value for the input control; may be null
     * @return an instance of type B, representing the configured input control
     */
    default <T> B inputControl(String id, String label, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        return inputControl(id, new MessageFormatter.MessageFormatterArgs(label), control, type, dflt);
    }

    /**
     * Configures an input control with the specified parameters.
     *
     * @param <T>     the generic result type
     * @param id      the unique identifier for the input control
     * @param label   the label to be associated with the input control
     * @param control the input control instance to be configured
     * @param type    the type of the input value
     * @param dflt    a supplier providing the default value for the input control; may be null
     * @return an instance of type B, representing the configured input control
     */
    <T> B inputControl(String id, MessageFormatter.MessageFormatterArgs label, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt);

    /**
     * Represents the styling attributes for a section or title label, such as spacing,
     * boldness, and scaling of the font. This style is used to configure the visual
     * presentation of section titles in a UI layout.
     *
     * @param vspaceBefore The vertical space, in units of font height, added before the section.
     * @param vspaceAfter  The vertical space, in units of font height, added after the section.
     * @param makeBold     A flag indicating whether the section title should be bold.
     * @param scale        A scaling factor to adjust the size of the font for the section title.
     */
    record SectionStyle(float vspaceBefore, float vspaceAfter, boolean makeBold, float scale) {}
}
