package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Converter;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PropertyConverter;
import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import com.dua3.utility.text.TextUtil;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A builder for menus with a choice of items.
 *
 * @param <T> the type of the items
 */
public class ChoiceMenuBuilder<T> extends MenuItemBuilder<Menu, ChoiceMenuBuilder<T>> {
    private final ObservableList<T> items;
    private final Property<@Nullable T> property;
    private @Nullable Consumer<@Nullable T> onChange;

    /**
     * Constructs a new instance of the {@code ChoiceMenuBuilder} with the specified collection of items.
     *
     * @param items the collection of items to populate the choice menu;
     *               if the provided collection is not an instance of {@code ObservableList},
     *               a new observable list will be created from a copy of the provided collection
     */
    ChoiceMenuBuilder(Collection<T> items) {
        super(Menu::new);
        this.property = new SimpleObjectProperty<>(null);
        this.items = items instanceof ObservableList<T> ol ? ol : FXCollections.observableList(List.copyOf(items));
    }

    /**
     * Sets the initial value for the choice menu.
     *
     * @param initialValue the initial value to set
     * @return this instance of the {@code ChoiceMenuBuilder} for method chaining
     */
    public ChoiceMenuBuilder<T> initialValue(T initialValue) {
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
    public ChoiceMenuBuilder<T> onChange(Consumer<@Nullable T> onChange) {
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
    public ChoiceMenuBuilder<T> bind(Property<@Nullable T> property) {
        this.property.bindBidirectional(property);
        return self();
    }

    @Override
    public Menu build() {
        Menu menu = super.build();

        T current = property.getValue();

        if (onChange != null) {
            property.addListener((obs, oldVal, newVal) -> onChange.accept(newVal));
        }

        ToggleGroup group = new ToggleGroup();
        for (T value : items) {
            Property<@Nullable Boolean> selected = new SimpleBooleanProperty(Objects.equals(current, value));
            Converter<@Nullable T, @Nullable Boolean> converter = Converter.create(
                    v -> Objects.equals(v, value),
                    b -> b != null && b ? value : property.getValue()
            );
            selected.bindBidirectional(PropertyConverter.convert(property, converter));
            RadioMenuItem mi = new RadioMenuItem(TextUtil.toLocalizedString(value));
            mi.setToggleGroup(group);
            mi.selectedProperty().bindBidirectional(selected);
            mi.setSelected(value.equals(property.getValue()));
            mi.setOnAction(evt -> property.setValue(value));

            // make sure selected is not GC'ed
            FxUtil.addStrongReference(mi.selectedProperty(), selected);

            menu.getItems().add(mi);
        }
        return menu;
    }
}
