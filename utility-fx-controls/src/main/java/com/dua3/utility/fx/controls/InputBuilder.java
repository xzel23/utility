package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
     * Add a labeled input control.
     *
     * @param <T>     the result type
     * @param id      the control's ID
     * @param label   the label text
     * @param type    the result type
     * @param dflt    supplier of default value
     * @param control the control
     * @param hidden  flag, indicating whether the input us hidden from the user (no control is visible on the UI)
     * @return {@code this}
     */
    <T> B add(
            String id,
            String label,
            Class<T> type,
            Supplier<T> dflt,
            InputControl<T> control,
            boolean hidden);

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
    <T> B add(
            String id,
            Class<T> type,
            Supplier<T> dflt,
            InputControl<T> control
    );

    /**
     * Add a labeled input control.
     *
     * @param id    the node's ID
     * @param label the label text
     * @param node  the node
     * @return {@code this}
     */
    B addNode(String id, String label, Node node);

    /**
     * Add an unlabeled input control.
     *
     * @param id   the node's ID
     * @param node the node
     * @return {@code this}
     */
    B addNode(String id, Node node);

    /**
     * Set the number of columns for layout (default is 1).
     *
     * @param columns the number of columns for laying out the input controls
     * @return {@code this}
     */
    B columns(int columns);

    /**
     * Add a static text without label.
     *
     * @param text the text to show.
     * @return {@code this}
     */
    B text(String text
    );

    /**
     * Add a static text without label.
     *
     * @param label the label text
     * @param text the text to show.
     * @return {@code this}
     */
    B text(String label,
           String text
    );

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
    <T> B constant(
            String id,
            String label,
            Supplier<T> value,
            Class<T> cls
    );

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
    <T> B constant(
            String id,
            String label,
            T value);

    /**
     * Creates a hidden field with the specified configurations.
     * <p>
     * Use this method to include values in the form data that are not visible to the user.
     *
     * @param <T>   The type of the value managed by this input.
     * @param id    The unique identifier for the input field.
     * @param value A supplier that provides the value to be displayed in the input field.
     * @param cls   The class type of the value provided.
     * @return An instance of `B` representing the configured disabled input field.
     */
    <T> B hidden(
            String id,
            Supplier<T> value,
            Class<T> cls
    );

    /**
     * Configures a non-editable input field with the specified parameters.
     * <p>
     * Use this method to include values in the form data that are not visible to the user.
     *
     * @param <T>   the type of the value contained in the input field
     * @param id    the unique identifier for the input field
     * @param value the value to be displayed in the disabled input field
     * @return an instance of type B representing the configured disabled input field
     */
    <T> B hidden(
            String id,
            T value
    );

    /**
     * Add a labeled string input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B string(
            String id,
            String label,
            Supplier<String> dflt
    ) {
        return string(id, label, dflt, s -> Optional.empty());
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
    B string(
            String id,
            String label,
            Supplier<String> dflt,
            Function<String, Optional<String>> validate
    );

    /**
     * Add a labeled integer input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B integer(
            String id,
            String label,
            Supplier<Long> dflt
    ) {
        return integer(id, label, dflt, i -> Optional.empty());
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
    B integer(
            String id,
            String label,
            Supplier<Long> dflt,
            Function<Long, Optional<String>> validate
    );

    /**
     * Add a labeled decimal input.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @return {@code this}
     */
    default B decimal(
            String id,
            String label,
            Supplier<Double> dflt
    ) {
        return decimal(id, label, dflt, d -> Optional.empty());
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
    B decimal(
            String id,
            String label,
            Supplier<Double> dflt,
            Function<Double, Optional<String>> validate
    );

    /**
     * Add a labeled checkbox.
     *
     * @param id    the ID
     * @param label the label text
     * @param dflt  supplier of default value
     * @param text  the checkbox text
     * @return {@code this}
     */
    default B checkBox(
            String id,
            String label,
            Supplier<Boolean> dflt,
            String text
    ) {
        return checkBox(id, label, dflt, text, b -> Optional.empty());
    }

    /**
     * Creates a checkbox with the given parameters.
     *
     * @param id        the ID of the checkbox
     * @param label     the label for the checkbox
     * @param dflt      the default value of the checkbox
     * @param text      the text to display next to the checkbox
     * @param validate  a function that takes a Boolean value and returns an optional validation message
     * @return {@code this}
     */
    B checkBox(
            String id,
            String label,
            Supplier<Boolean> dflt,
            String text,
            Function<Boolean, Optional<String>> validate
    );

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
    default <T> B comboBox(
            String id,
            String label,
            Supplier<T> dflt,
            Class<T> cls,
            Collection<T> items
    ) {
        return comboBox(id, label, dflt, cls, items, t -> Optional.empty());
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
    <T> B comboBox(
            String id,
            String label,
            Supplier<T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<T, Optional<String>> validate
    );

    /**
     * Adds a labeled combo box control with extended functionality.
     *
     * @param <T> the item type
     * @param id the control's ID
     * @param label the label text
     * @param edit the action to be performed when an item is edited (optional)
     * @param add the action to be performed when a new item is added (optional)
     * @param remove the action to be performed when an item is removed (optional)
     * @param format the function to format the items in the combo box
     * @param dflt the supplier of the default value
     * @param cls the result class of the combo box items
     * @param items the collection of items to choose from
     * @return {@code this}
     */
    default <T> B comboBoxEx(
            String id,
            String label,
            @Nullable UnaryOperator<@Nullable T> edit,
            @Nullable Supplier<@Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Supplier<@Nullable T> dflt,
            Class<T> cls,
            Collection<T> items
    ) {
        return comboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, t -> Optional.empty());
    }

    /**
     * Returns a custom combo box with the specified parameters.
     *
     * @param <T>        the type of objects in the combo box
     * @param id         the ID of the combo box
     * @param label      the label of the combo box
     * @param edit       a function to modify the selected item in the combo box, or null if editing is not allowed
     * @param add        a supplier to add a new item to the combo box, or null if adding is not allowed
     * @param remove     a predicate to remove an item from the combo box, or null if removing is not allowed
     * @param format     a function to format the items of the combo box as strings
     * @param dflt       a supplier to provide a default item for the combo box
     * @param cls        the class of objects in the combo box
     * @param items      the collection of items to populate the combo box
     * @param validate   a function to validate the items in the combo box and return an optional error message
     * @return {@code this}
     */
    <T> B comboBoxEx(
            String id,
            String label,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Supplier<T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<T, Optional<String>> validate
    );

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
    default <T> B radioList(
            String id,
            String label,
            Supplier<@Nullable T> dflt,
            Class<T> cls,
            Collection<T> items
    ) {
        return radioList(id, label, dflt, cls, items, (@Nullable T t) -> t != null ? Optional.empty() : Optional.of("No option selected"));
    }

    /**
     * Creates a radio list component.
     *
     * @param id        the ID of the radio list
     * @param label     the label text for the radio list
     * @param dflt      a supplier that provides the default value for the radio list
     * @param cls       the class of the items in the radio list
     * @param items     a collection of items for the radio list
     * @param validate  a function to validate the selected item, returning an optional error message
     * @param <T>       the type of items in the radio list
     * @return {@code this}
     */
    <T> B radioList(
            String id,
            String label,
            Supplier<T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<T, Optional<String>> validate
    );

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
    B slider(
            String id,
            String label,
            Supplier<Double> dflt,
            double min,
            double max
    );

    /**
     * Add a labeled pane with options.
     *
     * @param id      the ID
     * @param label   the label text
     * @param dflt    supplier of default values
     * @param options supplier of options
     * @return {@code this}
     */
    B options(
            String id,
            String label,
            Supplier<Arguments> dflt,
            Supplier<Collection<Option<?>>> options
    );

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
    B options(
            String id,
            Supplier<Arguments> dflt,
            Supplier<Collection<Option<?>>> options
    );

    /**
     * Add a file chooser.
     *
     * @param id     the ID
     * @param label  the label text
     * @param dflt   supplier of default value
     * @param mode   the mode, either {@link FileDialogMode#OPEN} or {@link FileDialogMode#SAVE}
     * @param existingOnly only let the user choose existing files; i.e., no new files can be created
     * @param filter the extension filter to use
     * @return {@code this}
     */
    default B chooseFile(
            String id,
            String label,
            Supplier<@Nullable Path> dflt,
            FileDialogMode mode,
            boolean existingOnly,
            Collection<FileChooser.ExtensionFilter> filter
    ) {
        return chooseFile(id, label, dflt, mode, existingOnly, filter, FileInput.defaultValidate(mode, existingOnly));
    }

    /**
     * Add a file chooser dialog to allow the user to select a file.
     *
     * @param id            The identifier for the file chooser dialog.
     * @param label         The label to display in the file chooser dialog.
     * @param dflt          A function that provides the default path to preselect in the file chooser dialog.
     * @param mode          The mode of the file dialog, such as OPEN or SAVE.
     * @param existingOnly  Whether to only allow selection of existing files.
     * @param filter        The file filters to apply in the file chooser dialog.
     * @param validate      A function to perform additional validation on the selected file path.
     *                      It returns an optional error message if the validation fails.
     * @return {@code this}
     */
    B chooseFile(
            String id,
            String label,
            Supplier<Path> dflt,
            FileDialogMode mode,
            boolean existingOnly,
            Collection<FileChooser.ExtensionFilter> filter,
            Function<Path, Optional<String>> validate
    );

    /**
     * Add an unlabeled custom node to the grid.
     *
     * @param id the ID of the node
     * @param node the node
     * @return {@code this}
     */
    B node(String id, Node node);

    /**
     * Add a labeled custom node to the grid.
     *
     * @param id the ID of the node
     * @param label the label text
     * @param node the node
     * @return {@code this}
     */
    B node(String id, String label, Node node);
}
