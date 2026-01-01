package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ControlBuilder;
import com.dua3.utility.text.TextUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builder for {@link ComboBoxEx}.
 *
 * @param <T> the type of the items in the ComboBoxEx
 */
public class ComboBoxExBuilder<T> extends ControlBuilder<ComboBoxEx<T>, ComboBoxExBuilder<T>> {
    private final ObservableList<T> items;
    private final Property<@Nullable T> property;
    private @Nullable Function<T, @Nullable T> edit;
    private @Nullable Supplier<@Nullable T> add;
    private @Nullable BiPredicate<ComboBoxEx<T>, T> remove;
    private Supplier<? extends @Nullable T> dflt = () -> null;
    private Function<@Nullable T, String> format = TextUtil::toLocalizedString;
    private @Nullable Consumer<@Nullable T> onChange;

    /**
     * Constructs a new ComboBoxExBuilder.
     *
     * @param items the collection of items to populate the combo box
     */
    ComboBoxExBuilder(Collection<T> items) {
        super(() -> null);
        this.property = new SimpleObjectProperty<>(null);
        this.items = items instanceof ObservableList<T> ol ? ol : FXCollections.observableList(List.copyOf(items));
    }

    /**
     * Sets an edit function that allows modifying a given item when triggered.
     *
     * @param edit a function that takes an item of type {@code T} and returns the modified item, or
     *             {@code null} if the item should not be modified
     * @return the current instance of the builder
     */
    public ComboBoxExBuilder<T> edit(Function<T, @Nullable T> edit) {
        this.edit = edit;
        return self();
    }

    /**
     * Sets the supplier responsible for adding a new item to the ComboBoxEx.
     *
     * @param add a supplier that provides a new item to be added to the ComboBoxEx, or null if no item is to be added
     * @return this instance of the builder
     */
    public ComboBoxExBuilder<T> add(Supplier<@Nullable T> add) {
        this.add = add;
        return self();
    }

    /**
     * Sets the logic to determine whether an item should be eligible for removal
     * in the ComboBoxEx.
     *
     * @param remove a {@link BiPredicate} that takes the ComboBoxEx instance
     *               and an item of type {@code T}, and returns {@code true} if
     *               the item should be removed, or {@code false} otherwise
     * @return the current instance of {@code ComboBoxExBuilder} for method chaining
     */
    public ComboBoxExBuilder<T> remove(BiPredicate<ComboBoxEx<T>, T> remove) {
        this.remove = remove;
        return self();
    }

    /**
     * Sets the default value supplier for the ComboBoxEx.
     *
     * @param dflt a supplier that provides the default value, which can be null
     * @return this instance of ComboBoxExBuilder for method chaining
     */
    public ComboBoxExBuilder<T> defaultValue(Supplier<? extends @Nullable T> dflt) {
        this.dflt = dflt;
        return self();
    }

    /**
     * Sets the format function to define how items of type {@code T} should be
     * converted to their string representation for display in the combo box.
     *
     * @param format a function that takes an item of type {@code T} (which may
     *               be {@code null}) and returns its string representation
     * @return the current instance of the builder
     */
    public ComboBoxExBuilder<T> format(Function<@Nullable T, String> format) {
        this.format = format;
        return self();
    }

    /**
     * Sets a callback to be invoked when the value of the ComboBoxEx changes.
     *
     * @param onChange a {@link Consumer} that accepts the new value of the ComboBoxEx.
     *                 The parameter may be null, indicating no action is taken on value changes.
     * @return the current instance of {@code ComboBoxExBuilder} for method chaining
     */
    public ComboBoxExBuilder<T> onChange(Consumer<@Nullable T> onChange) {
        this.onChange = onChange;
        return self();
    }

    /**
     * Binds the provided property bidirectionally to the builder's internal property.
     *
     * @param property the property to bind bidirectionally with the builder's internal property
     * @return the current instance of the builder
     */
    public ComboBoxExBuilder<T> bind(Property<@Nullable T> property) {
        this.property.bindBidirectional(property);
        return self();
    }

    @Override
    public ComboBoxEx<T> build() {
        ComboBoxEx<T> comboBoxEx = new ComboBoxEx<>(edit, add, remove, dflt, format, items);

        // ControlBuilder.build() applies tooltip, etc.
        // But it also calls factory.get().
        // Since we want to use the standard build logic but with our instance,
        // we use a temporary factory.
        Supplier<? extends ComboBoxEx<T>> oldFactory = factory;
        factory = () -> comboBoxEx;
        try {
            super.build();
        } finally {
            factory = oldFactory;
        }

        if (onChange != null) {
            comboBoxEx.valueProperty().addListener((obs, oldVal, newVal) -> onChange.accept(newVal));
        }

        Bindings.bindBidirectional(comboBoxEx.valueProperty(), property);

        return comboBoxEx;
    }
}
