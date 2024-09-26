package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Interface for an input field.
 *
 * @param <R> the input result type
 */
public interface InputControl<R> {
    /**
     * Creates a {@link SimpleInputControl} for a TextField with String input.
     *
     * @param dflt a {@link Supplier} providing the default value for the TextField
     * @param validate a {@link Function} that takes a String and returns an Optional containing a validation error message, if any
     * @return a {@link SimpleInputControl} containing the TextField and associated properties
     */
    static SimpleInputControl<TextField, String> stringInput(Supplier<String> dflt, Function<String, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty value = control.textProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Creates a new {@link SimpleInputControl} for a {@link TextField} with bidirectional binding.
     *
     * @param dflt      The supplier providing the default value.
     * @param validate  A function to validate the value, returning an optional error message.
     * @param converter The StringConverter to convert between the value and its string representation.
     * @param <T>       The type of the value.
     * @return A {@link SimpleInputControl} containing the {@link TextField} and associated properties.
     */
    static <T> SimpleInputControl<TextField, T> stringInput(Supplier<T> dflt, Function<T, Optional<String>> validate, StringConverter<T> converter) {
        TextField control = new TextField();
        ObjectProperty<T> value = new SimpleObjectProperty<>();
        Bindings.bindBidirectional(control.textProperty(), value, converter);
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for integer values.
     *
     * @param dflt the default value {@link Supplier}
     * @param validate the {@link Function} to validate the integer input
     * @return a {@link SimpleInputControl} for integer input
     */
    static SimpleInputControl<TextField, Integer> integerInput(Supplier<Integer> dflt, Function<Integer, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty textProperty = control.textProperty();
        IntegerProperty value = new SimpleIntegerProperty();
        textProperty.bindBidirectional(value, NumberFormat.getIntegerInstance(Locale.getDefault()));
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for decimal input using a {@link TextField}.
     *
     * @param dflt the {@link Supplier} providing the default value for the input
     * @param validate the {@link Function} to validate the input value
     * @return a {@link SimpleInputControl} that manages a TextField for Decimal input
     */
    static SimpleInputControl<TextField, Double> decimalInput(Supplier<Double> dflt, Function<Double, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty textProperty = control.textProperty();
        DoubleProperty value = new SimpleDoubleProperty();
        textProperty.bindBidirectional(value, NumberFormat.getInstance(Locale.getDefault()));
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for a {@link CheckBox} with a default value, text, and validation function.
     *
     * @param dflt a {@link Supplier} providing the default Boolean value
     * @param text the text to be displayed with the {@link CheckBox}
     * @param validate a {@link Function} that takes a Boolean value and returns an {@link Optional} containing an error message if validation fails
     * @return a new instance of {@link SimpleInputControl} configured with a {@link CheckBox} and the provided parameters
     */
    static SimpleInputControl<CheckBox, Boolean> checkBoxInput(Supplier<Boolean> dflt, String text, Function<Boolean, Optional<String>> validate) {
        CheckBox control = new CheckBox(text);
        BooleanProperty value = control.selectedProperty();
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    /**
     * Creates a {@link ComboBox} input control with specified choices, default value {@link Supplier}, and validation {@link Function}.
     *
     * @param <T> the type of the items in the {@link ComboBox}
     * @param choices the collection of available choices for the {@link ComboBox}
     * @param dflt a {@link Supplier} providing the default value
     * @param validate a {@link Function} to validate the selected item which returns an optional error message
     * @return a {@link SimpleInputControl} containing the ComboBox and its value property
     */
    static <T> SimpleInputControl<ComboBox<T>, T> comboBoxInput(Collection<? extends T> choices, Supplier<T> dflt, Function<T, Optional<String>> validate) {
        ComboBox<T> control = new ComboBox<>(FXCollections.observableArrayList(choices));
        Property<T> value = control.valueProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Creates a new {@code SimpleInputControl} for a {@link ComboBoxEx} with the specified parameters.
     *
     * @param <T>      the type of items contained in the {@link ComboBoxEx}
     * @param choices  the collection of choices to populate the {@link ComboBoxEx}
     * @param dflt     a {@link Supplier} for the default value
     * @param edit     a {@link UnaryOperator} to perform editing on the selected item (nullable)
     * @param add      a {@link Supplier} to provide a new item to add (nullable)
     * @param remove   a {@link BiPredicate} to determine if an item should be removed (nullable)
     * @param format   a {@link Function} to format the items as strings
     * @param validate a {@link Function} to validate the current value
     * @return a new instance of {@code SimpleInputControl} configured with a {@link ComboBoxEx} and its value property
     */
    static <T> SimpleInputControl<ComboBoxEx<T>, T> comboBoxExInput(
            Collection<T> choices,
            Supplier<T> dflt,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Function<T, Optional<String>> validate) {
        ComboBoxEx<T> control = new ComboBoxEx<>(edit, add, remove, format, FXCollections.observableArrayList(choices));
        Property<T> value = control.valueProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Provides a file chooser input control.
     *
     * @param dflt          a {@link Supplier} providing the default file path.
     * @param mode          the {@link FileDialogMode} of the dialog (e.g., OPEN, SAVE, DIRECTORY).
     * @param existingOnly  specifies if only existing files can be chosen.
     * @param filters       a {@link Collection} of {@link javafx.stage.FileChooser.ExtensionFilter} to apply.
     * @param validate      a {@link Function} that validates the selected file path.
     * @return An {@code InputControl} instance for file selection.
     */
    static InputControl<Path> chooseFile(Supplier<Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filters,
                                         Function<Path, Optional<String>> validate) {
        return new FileInput(mode, existingOnly, dflt, filters, validate);
    }

    /**
     * Get the {@link Node} for this input element.
     *
     * @return the node
     */
    Node node();

    /**
     * Get value.
     *
     * @return the current value
     */
    default R get() {
        return valueProperty().getValue();
    }

    /**
     * Provides the property representing the value of this input control.
     *
     * @return the property containing the current value
     */
    Property<R> valueProperty();

    /**
     * Set value.
     *
     * @param arg the value to set
     */
    default void set(R arg) {
        valueProperty().setValue(arg);
    }

    /**
     * Test if content is valid.
     * @return true, if content is valid
     */
    default boolean isValid() {
        return validProperty().get();
    }

    /**
     * Set/update control state.
     */
    default void init() {
        // nop
    }

    /**
     * Reset value to default
     */
    void reset();

    /**
     * Provides a read-only property representing the validity of the input.
     *
     * @return a ReadOnlyBooleanProperty that is true if the input is valid and false otherwise
     */
    ReadOnlyBooleanProperty validProperty();

    /**
     * Provides a read-only property representing the error message for this input control.
     *
     * <p>This property contains an error message if the input is invalid, otherwise it is empty.
     *
     * @return a ReadOnlyStringProperty containing the error message if there is a validation error, otherwise empty
     */
    ReadOnlyStringProperty errorProperty();

    class State<R> {
        private final Property<R> value;
        private final BooleanProperty valid = new SimpleBooleanProperty(true);
        private final StringProperty error = new SimpleStringProperty("");

        private Supplier<? extends R> dflt;

        private Function<? super R, Optional<String>> validate;

        public State(Property<R> value) {
            this(value, freeze(value));

        }

        public State(Property<R> value, Supplier<R> dflt) {
            this(value, dflt, s -> Optional.empty());
        }

        private static <R> Supplier<R> freeze(ObservableValue<? extends R> value) {
            final R frozen = value.getValue();
            return () -> frozen;
        }

        public State(Property<R> value, Supplier<? extends R> dflt, Function<? super R, Optional<String>> validate) {
            this.value = value;
            this.value.addListener((v, o, n) -> updateValidState(n));
            this.dflt = dflt;
            this.validate = validate;

            this.value.addListener((v, o, n) -> updateValidState(n));
        }

        private void updateValidState(@Nullable R r) {
            Optional<String> result = validate.apply(r);
            valid.setValue(result.isEmpty());
            error.setValue(result.orElse(""));
        }

        public void setValidate(Function<? super R, Optional<String>> validate) {
            this.validate = validate;
            updateValidState(valueProperty().getValue());
        }

        public Property<R> valueProperty() {
            return value;
        }

        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        public ReadOnlyStringProperty errorProperty() {
            return error;
        }

        public void setDefault(Supplier<? extends R> dflt) {
            this.dflt = dflt;
        }

        public void reset() {
            value.setValue(dflt.get());
        }
    }
}
