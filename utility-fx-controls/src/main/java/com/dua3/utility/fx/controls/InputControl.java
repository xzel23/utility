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
    static SimpleInputControl<TextField, String> stringInput(Supplier<String> dflt, Function<String, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty value = control.textProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    static <T> SimpleInputControl<TextField, T> stringInput(Supplier<T> dflt, Function<T, Optional<String>> validate, StringConverter<T> converter) {
        TextField control = new TextField();
        ObjectProperty<T> value = new SimpleObjectProperty<>();
        Bindings.bindBidirectional(control.textProperty(), value, converter);
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

    static SimpleInputControl<TextField, Integer> integerInput(Supplier<Integer> dflt, Function<Integer, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty textProperty = control.textProperty();
        IntegerProperty value = new SimpleIntegerProperty();
        textProperty.bindBidirectional(value, NumberFormat.getIntegerInstance(Locale.getDefault()));
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    static SimpleInputControl<TextField, Double> decimalInput(Supplier<Double> dflt, Function<Double, Optional<String>> validate) {
        TextField control = new TextField();
        StringProperty textProperty = control.textProperty();
        DoubleProperty value = new SimpleDoubleProperty();
        textProperty.bindBidirectional(value, NumberFormat.getInstance(Locale.getDefault()));
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    static SimpleInputControl<CheckBox, Boolean> checkBoxInput(Supplier<Boolean> dflt, String text, Function<Boolean, Optional<String>> validate) {
        CheckBox control = new CheckBox(text);
        BooleanProperty value = control.selectedProperty();
        return new SimpleInputControl<>(control, value.asObject(), dflt, validate);
    }

    static <T> SimpleInputControl<ComboBox<T>, T> comboBoxInput(Collection<? extends T> choices, Supplier<T> dflt, Function<T, Optional<String>> validate) {
        ComboBox<T> control = new ComboBox<>(FXCollections.observableArrayList(choices));
        Property<T> value = control.valueProperty();
        return new SimpleInputControl<>(control, value, dflt, validate);
    }

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

    static InputControl<Path> chooseFile(Supplier<Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filters,
                                         Function<Path, Optional<String>> validate) {
        return new FileInput(mode, existingOnly, dflt, filters, validate);
    }

    /**
     * Get the Node for this input element.
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

    ReadOnlyBooleanProperty validProperty();

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
