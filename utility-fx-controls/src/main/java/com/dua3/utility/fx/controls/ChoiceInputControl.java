package com.dua3.utility.fx.controls;

import com.dua3.utility.options.ChoiceOption;
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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Interface for an input field.
 *
 * @param <T> the input result type
 */
public class ChoiceInputControl<T> implements InputControl<T> {

    private final ComboBox<ChoiceOption.Choice<@Nullable T>> control;
    private final Supplier<? extends @Nullable T> dfltValue;
    private final Property<@Nullable T> valueProperty;

    /**
     * Constructs a ChoiceInputControl with the given options and default value supplier.
     *
     * @param option the ChoiceOption containing possible values for the input control
     * @param dfltValue a Supplier that provides the default value for the input control
     */
    public ChoiceInputControl(ChoiceOption<T> option, Supplier<? extends @Nullable T> dfltValue) {
        this.dfltValue = dfltValue;
        this.control = new ComboBox<>();
        this.valueProperty = new SimpleObjectProperty<>();

        //noinspection NullableProblems - false positive
        control.valueProperty().addListener((ObservableValue<? extends ChoiceOption.Choice<T>> v, ChoiceOption.@Nullable Choice<T> o, ChoiceOption.@Nullable Choice<T> n) -> valueProperty.setValue(n == null ? null : n.value()));
        valueProperty.addListener((ObservableValue<? extends @Nullable T> v, @Nullable T o, @Nullable T n) -> {
            if (n == null) {
                control.getSelectionModel().clearSelection();
            } else {
                control.getSelectionModel().select(option.choice(n));
            }
        });

        control.getItems().setAll(option.choices());
        Optional.ofNullable(dfltValue.get()).ifPresent(dflt -> control.getSelectionModel().select(option.choice(dflt)));
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
