package com.dua3.utility.fx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.control.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The ToggleButtonBuilder class is a utility class for constructing ToggleButton instances
 * with properties configured using a fluent API.
 * It extends the ButtonBuilderBase by supporting to bind the button's selected state to a boolean property.
 */
public class ToggleButtonBuilder extends ButtonBuilderBase<ToggleButton, ToggleButtonBuilder> {
    private final List<Property<Boolean>> selectedList = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param factory the factory method for ToggleButton instances
     */
    ToggleButtonBuilder(Supplier<? extends ToggleButton> factory) {
        super(factory);
    }

    /**
     * Bind the button's selected state to an {@link Property<Boolean>}.
     * @param selected the property to bind this button's selected state to
     * @return this ToggleButtonBuilder instance
     */
    public ToggleButtonBuilder bindSelected(Property<Boolean> selected) {
        this.selectedList.add(selected);
        return self();
    }

    @Override
    public ToggleButton build() {
        ToggleButton button = super.build();
        selectedList.forEach(selected -> Bindings.bindBidirectional(selected, button.selectedProperty()));
        return button;
    }
}
