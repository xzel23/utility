package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ControlBuilder;
import com.dua3.utility.text.TextUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for {@link ComboBox}.
 *
 * @param <T> the type of the items in the ComboBox
 */
public class ComboBoxBuilder<T> extends ControlBuilder<ComboBox<T>, ComboBoxBuilder<T>> {
    private final ObservableList<T> items;
    private final Property<@Nullable T> property;
    private @Nullable Consumer<@Nullable T> onChange;
    private boolean localized = true;

    /**
     * Constructs a new ComboBoxBuilder.
     *
     * @param items the collection of items to populate the combo box;
     *               if the provided collection is not an instance of {@code ObservableList},
     *               a new observable list will be created from a copy of the provided collection
     */
    ComboBoxBuilder(Collection<T> items) {
        super(ComboBox::new);
        this.property = new SimpleObjectProperty<>(null);
        this.items = items instanceof ObservableList<T> ol ? ol : FXCollections.observableList(List.copyOf(items));
    }

    /**
     * Sets the initial value for the choice menu.
     *
     * @param initialValue the initial value to set
     * @return this instance of the {@code ChoiceMenuBuilder} for method chaining
     */
    public ComboBoxBuilder<T> initialValue(T initialValue) {
        property.setValue(initialValue);
        return self();
    }

    /**
     * Sets a callback to be invoked whenever the selected value in the choice menu changes.
     *
     * @param onChange a {@link Consumer} that accepts the new value of type {@code T}, or {@code null} if
     *                 no value is selected or the value is explicitly set to {@code null}
     * @return this instance of the {@code ChoiceMenuBuilder} for method chaining
     */
    public ComboBoxBuilder<T> onChange(Consumer<@Nullable T> onChange) {
        this.onChange = onChange;
        return self();
    }

    /**
     * Binds the menu's internal property to the specified external property, enabling bidirectional
     * synchronization of items between the two properties.
     *
     * @param property the external {@link Property} to bind bidirectionally with the builder's internal property;
     *                 can hold {@code null} items
     * @return this instance of the {@code ChoiceMenuBuilder} for method chaining
     */
    public ComboBoxBuilder<T> bind(Property<@Nullable T> property) {
        this.property.bindBidirectional(property);
        return self();
    }

    /**
     * Set the localization state.
     *
     * @param localized true, if items should be localized
     * @return this builder
     */
    public ComboBoxBuilder<T> localized(boolean localized) {
        this.localized = localized;
        return self();
    }

    @Override
    public ComboBox<T> build() {
        ComboBox<T> comboBox = super.build();
        comboBox.setItems(items);

        if (localized) {
            comboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(@Nullable T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : TextUtil.toLocalizedString(item));
                }
            });
            comboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(@Nullable T item) {
                    return item == null ? "" : TextUtil.toLocalizedString(item);
                }

                @Override
                public T fromString(String string) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        if (onChange != null) {
            comboBox.valueProperty().addListener((obs, oldVal, newVal) -> onChange.accept(newVal));
        }

        Bindings.bindBidirectional(comboBox.valueProperty(), property);

        return comboBox;
    }
}
