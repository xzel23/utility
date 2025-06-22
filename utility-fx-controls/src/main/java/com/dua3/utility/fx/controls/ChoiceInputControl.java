package com.dua3.utility.fx.controls;

import com.dua3.utility.options.Param;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Interface for an input field.
 *
 * @param <T> the input result type
 */
public class ChoiceInputControl<T> implements InputControl<T> {

    private final ComboBox<Choice<@Nullable T>> control;
    private final Supplier<? extends @Nullable T> dfltValue;
    private final Property<@Nullable T> valueProperty;

    private record Choice<T>(@Nullable T value, String text) {}

    /**
     * Constructs a ChoiceInputControl, which represents an input control that allows for selecting one
     * option from a predefined list of choices. The control is initialized with a list of allowed values
     * and supports a default value that can be set upon creation.
     *
     * @param param     the parameter defining the allowed values and associated display text for
     *                  each choice
     * @param dfltValue a supplier that provides the default value to be selected when the control
     *                  is initialized
     */
    public ChoiceInputControl(Param<T> param, Supplier<? extends @Nullable T> dfltValue) {
        this.dfltValue = dfltValue;
        this.control = new ComboBox<>();
        this.valueProperty = new SimpleObjectProperty<>();

        LinkedHashMap<T, Choice<T>> choices = new LinkedHashMap<>();
        param.allowedValues().forEach(v -> choices.put(v, new Choice<>(v, param.getText(v))));
        control.getItems().setAll(choices.values());

        control.valueProperty().addListener(
                (ObservableValue<? extends Choice<@Nullable T>> v, @Nullable Choice<T> o, @Nullable Choice<T> n)
                        -> valueProperty.setValue(n == null ? null : n.value())
        );

        valueProperty.addListener((ObservableValue<? extends @Nullable T> v, @Nullable T o, @Nullable T n) -> {
            if (n == null) {
                control.getSelectionModel().clearSelection();
            } else {
                control.getSelectionModel().select(choices.get(n));
            }
        });

        Optional.ofNullable(dfltValue.get()).ifPresent(dflt -> control.getSelectionModel().select(choices.get(dflt)));
    }

    @Override
    public Node node() {
        return control;
    }

    @Override
    public Property<@Nullable T> valueProperty() {
        return valueProperty;
    }

    @Override
    public void reset() {
        valueProperty.setValue(dfltValue.get());
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return new SimpleStringProperty("");
    }
}
