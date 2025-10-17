package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import javafx.scene.control.PasswordField;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Interface for an input field.
 *
 * @param <T> the input result type
 */
public interface InputControl<T> {

    /**
     * The constant string "Invalid value".
     */
    String INVALID_VALUE = "Invalid value";

    /**
     * Creates a {@link SimpleInputControl} for a TextField with String input.
     *
     * @param dflt a {@link Supplier} providing the default value for the TextField
     * @param validate a {@link Function} that takes a String and returns an Optional containing a validation error message, if any
     * @return a {@link SimpleInputControl} containing the TextField and associated properties
     */
    static SimpleInputControl<TextField, String> stringInput(Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
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
    static <T> SimpleInputControl<TextField, T> stringInput(Supplier<? extends @Nullable T> dflt, Function<@Nullable T, Optional<String>> validate, StringConverter<@Nullable T> converter) {
        TextField control = new TextField();
        ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>();
        Bindings.bindBidirectional(control.textProperty(), value, converter);
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for a TextField with String input.
     *
     * @param dflt a {@link Supplier} providing the default value for the TextField
     * @param validate a {@link Function} that takes a String and returns an Optional containing a validation error message, if any
     * @return a {@link SimpleInputControl} containing the TextField and associated properties
     */
    static SimpleInputControl<TextField, String> passwordInput(Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        PasswordField control = new PasswordField();
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
    static <T> SimpleInputControl<TextField, T> passwordInput(Supplier<? extends @Nullable T> dflt, Function<@Nullable T, Optional<String>> validate, StringConverter<@Nullable T> converter) {
        PasswordField control = new PasswordField();
        ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>();
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
    static SimpleInputControl<TextField, Long> integerInput(Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        Format format = new FormatWithDefaultValue(NumberFormat.getIntegerInstance(Locale.getDefault()), dflt);
        return formattableInput(Long.class, format, dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for decimal input using a {@link TextField}.
     *
     * @param dflt the {@link Supplier} providing the default value for the input
     * @param validate the {@link Function} to validate the input value
     * @return a {@link SimpleInputControl} that manages a TextField for Decimal input
     */
    static SimpleInputControl<TextField, Double> decimalInput(Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        Format format = new FormatWithDefaultValue(NumberFormat.getInstance(Locale.getDefault()), dflt);
        return formattableInput(Double.class, format, dflt, validate);
    }

    /**
     * Creates a {@link SimpleInputControl} for a {@link TextField} with a formattable input,
     * allowing bidirectional binding and validation of the input using a specified {@link Format}.
     *
     * @param <T>     The type of the value for the input field.
     * @param cls     The class of the value type to be formatted and validated.
     * @param format  The {@link Format} used for converting the value to and from its string representation.
     * @param dflt    A {@link Supplier} providing the default value for the input field.
     * @param validate A {@link Function} that validates the input value and returns an {@link Optional} containing
     *                 an error message if validation fails; otherwise, an empty {@link Optional}.
     * @return A {@link SimpleInputControl} containing a {@link TextField} for the formattable input and
     *         associated properties, including validation.
     */
    static <T> SimpleInputControl<TextField, T> formattableInput(Class<T> cls, Format format, Supplier<? extends @Nullable T> dflt, Function<@Nullable T, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty textProperty = control.textProperty();
        Property<T> value = new SimpleObjectProperty<>();
        AtomicReference<@Nullable String> err = new AtomicReference<>(null);
        textProperty.bindBidirectional(value, createStrictStringConverter(cls, format, err::set));

        Function<@Nullable T, Optional<String>> strictValidate = d ->
                Optional.ofNullable(err.get()).or(() -> validate.apply(d));

        return new SimpleInputControl<>(control, value, dflt, strictValidate);
    }

    private static <T> StringConverter<T> createStrictStringConverter(Class<T> cls, Format format, Consumer<@Nullable String> setErrorMessage) {
        return new StringConverter<>() {
            @Override
            public String toString(@Nullable T object) {
                return object == null ? "" : format.format(object);
            }

            @Override
            public @Nullable T fromString(String s) {
                setErrorMessage.accept(null);

                if (s.isEmpty()) {
                    return null;
                }

                try {
                    ParsePosition pos = new ParsePosition(0);
                    Object result = format.parseObject(s, pos);

                    if (result == null || pos.getIndex() != s.length()) {
                        setErrorMessage.accept(INVALID_VALUE);
                        return null;
                    }

                    if (result instanceof Number n && !cls.isAssignableFrom(result.getClass())) {
                        result = convertNumber(n, cls);
                    }

                    return cls.cast(result);
                } catch (RuntimeException e) {
                    setErrorMessage.accept(INVALID_VALUE);
                    return null;
                }
            }
        };
    }

    /**
     * Converts a given number to a specified type, if possible.
     * The method supports conversions to Integer, Long, Double, Float, and BigDecimal.
     * If the target type is not supported, the original number is returned.
     *
     * @param <T> the target type to which the number is to be converted
     * @param x the number to be converted
     * @param cls the class of the target type
     * @return the number converted to the specified type, or the original number if conversion is not supported
     */
    private static <T> @Nullable Number convertNumber(@Nullable Number x, Class<T> cls) {
        Number result;
        if (x == null) {
            result = null;
        } else if (cls.isAssignableFrom(Integer.class)) {
            result = x.intValue();
        } else if (cls.isAssignableFrom(Long.class)) {
            result = x.longValue();
        } else if (cls.isAssignableFrom(Double.class)) {
            result = x.doubleValue();
        } else if (cls.isAssignableFrom(Float.class)) {
            result = x.floatValue();
        } else if (cls.isAssignableFrom(BigDecimal.class)) {
            result = x instanceof BigDecimal ? x : new BigDecimal(x.toString());
        } else {
            result = x;
        }
        return result;
    }

    /**
     * Creates a {@link SimpleInputControl} for a {@link CheckBox} with a default value, text, and validation function.
     *
     * @param dflt a {@link Supplier} providing the default Boolean value
     * @param text the text to be displayed with the {@link CheckBox}
     * @param validate a {@link Function} that takes a Boolean value and returns an {@link Optional} containing an error message if validation fails
     * @return a new instance of {@link SimpleInputControl} configured with a {@link CheckBox} and the provided parameters
     */
    static SimpleInputControl<CheckBox, Boolean> checkBoxInput(BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        CheckBox control = new CheckBox(text);
        BooleanProperty value = control.selectedProperty();
        return new SimpleInputControl<>(control, value.asObject(), dflt::getAsBoolean, validate);
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
    static <T> SimpleInputControl<ComboBox<T>, T> comboBoxInput(Collection<? extends T> choices, Supplier<? extends @Nullable T> dflt, Function<@Nullable T, Optional<String>> validate) {
        ComboBox<T> control = new ComboBox<>(FxUtil.makeObservable(choices));
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
            Supplier<? extends @Nullable T> dflt,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<@Nullable T, String> format,
            Function<@Nullable T, Optional<String>> validate) {
        ComboBoxEx<T> control = new ComboBoxEx<>(edit, add, remove, dflt, format, FxUtil.makeObservable(choices));
        Property<T> value = control.valueProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    /**
     * Provides a file chooser input control.
     *
     * @param parentWindow  the parent window
     * @param dflt          a {@link Supplier} providing the default file path.
     * @param mode          the {@link FileDialogMode} of the dialog (e.g., OPEN, SAVE, DIRECTORY).
     * @param existingOnly  specifies if only existing files can be chosen.
     * @param filters       a {@link Collection} of {@link javafx.stage.FileChooser.ExtensionFilter} to apply.
     * @param validate      a {@link Function} that validates the selected file path.
     * @return An {@code InputControl} instance for file selection.
     */
    static InputControl<Path> chooseFile(@Nullable Window parentWindow, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filters,
                                         Function<Path, Optional<String>> validate) {
        return new FileInput(parentWindow, mode, existingOnly, dflt, filters, validate);
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
    default @Nullable T get() {
        return valueProperty().getValue();
    }

    /**
     * Provides the property representing the value of this input control.
     *
     * @return the property containing the current value
     */
    Property<@Nullable T> valueProperty();

    /**
     * Set value.
     *
     * @param arg the value to set
     */
    default void set(@Nullable T arg) {
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

    /**
     * State class encapsulates a value, validation logic, error message, and validity state.
     *
     * @param <R> the type of the value being managed
     */
    class State<R> {
        private final Property<@Nullable R> value;
        private final BooleanProperty valid = new SimpleBooleanProperty(true);
        private final StringProperty error = new SimpleStringProperty("");

        private Supplier<? extends @Nullable R> dflt;

        private Function<? super @Nullable R, Optional<String>> validate;

        /**
         * Constructs a State object with the given value.
         *
         * @param value the property representing the value managed by this State
         */
        public State(Property<@Nullable R> value) {
            this(value, freeze(value));
        }

        /**
         * Constructs a State object with the given value and default value supplier.
         *
         * @param value the property representing the value managed by this State
         * @param dflt a supplier that provides the default value for the property
         */
        public State(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt) {
            this(value, dflt, s -> Optional.empty());
        }

        /**
         * Creates a supplier that always returns the current value of the given ObservableValue,
         * capturing its value at the moment this method is called.
         *
         * @param value the ObservableValue whose current value is to be captured
         * @return a Supplier that returns the captured value
         */
        private static <R> Supplier<R> freeze(ObservableValue<? extends R> value) {
            final R frozen = value.getValue();
            return () -> frozen;
        }

        /**
         * Constructs a State object with the given value, default value supplier, and validation function.
         *
         * @param value the property representing the value managed by this State
         * @param dflt a supplier that provides the default value for the property
         * @param validate a function that validates the value and returns an optional error message
         */
        public State(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate) {
            this.value = value;
            this.value.addListener((ObservableValue<? extends @Nullable R> v, @Nullable R o, @Nullable R n) -> updateValidState(n));
            this.dflt = dflt;
            this.validate = validate;

            this.valid.set(validate.apply(value.getValue()).isEmpty());
            this.error.setValue("");

            this.value.addListener((v, o, n) -> updateValidState(n));
        }

        private void updateValidState(@Nullable R r) {
            Optional<String> result = validate.apply(r);
            valid.setValue(result.isEmpty());
            error.setValue(result.orElse(""));
        }

        /**
         * Sets the validation function for the State.
         *
         * @param validate a function that validates the value and returns an optional error message
         */
        public void setValidate(Function<? super @Nullable R, Optional<String>> validate) {
            this.validate = validate;
            updateValidState(valueProperty().getValue());
        }

        /**
         * Returns the property representing the value managed by this State.
         *
         * @return the property representing the value
         */
        public Property<@Nullable R> valueProperty() {
            return value;
        }

        /**
         * Provides a read-only boolean property indicating the validity state.
         *
         * @return a {@link ReadOnlyBooleanProperty} representing whether the current state is valid
         */
        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        /**
         * Returns a read-only string property representing the current error message.
         * If the value is valid, the error message will be an empty string.
         *
         * @return ReadOnlyStringProperty representing the error message.
         */
        public ReadOnlyStringProperty errorProperty() {
            return error;
        }

        /**
         * Sets the default value supplier for this State.
         *
         * @param dflt a supplier that provides the default value for the property
         */
        public void setDefault(Supplier<? extends @Nullable R> dflt) {
            this.dflt = dflt;
        }

        /**
         * Resets the state to its default value.
         *
         * <p>This method sets the current value of the property managed by this
         * state to the default value supplied during the creation of the state.
         */
        public void reset() {
            value.setValue(dflt.get());
        }

        /**
         * Validates the current state based on the value and validation function provided during
         * the creation of the State object or set later and updates the valid state of the control.
         *
         * @return true if the current value of the property is valid, otherwise false
         */
        boolean validate() {
            Optional<String> result;
            try {
                result = validate.apply(valueProperty().getValue());
            } catch (Exception e) {
                result = Optional.of(INVALID_VALUE);
            }
            valid.setValue(result.isEmpty());
            error.setValue(result.orElse(""));
            return result.isEmpty();
        }

    }
}

final class FormatWithDefaultValue extends Format {
    final Format baseFormat;
    final Supplier<?> defaultValue;

    FormatWithDefaultValue(Format baseFormat, Supplier<? extends @Nullable Object> defaultValue) {
        this.baseFormat = baseFormat;
        this.defaultValue = defaultValue;
    }

    @Override
    public StringBuffer format(@Nullable Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return obj == null ? toAppendTo : baseFormat.format(obj, toAppendTo, pos);
    }

    @Override
    public @Nullable Object parseObject(String source, ParsePosition pos) {
        return source.isEmpty() ? defaultValue.get() : baseFormat.parseObject(source, pos);
    }

    @Override
    public @Nullable Object parseObject(String source) throws ParseException {
        return source.isEmpty() ? defaultValue.get() : super.parseObject(source);
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.fx.controls.FormatWithDefaultValue");}

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {throw new java.io.NotSerializableException("com.dua3.utility.fx.controls.FormatWithDefaultValue");}
}
