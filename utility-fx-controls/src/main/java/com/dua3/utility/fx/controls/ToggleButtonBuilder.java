package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ButtonBaseBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.control.ToggleButton;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The ToggleButtonBuilder class is a utility class for constructing ToggleButton instances
 * with properties configured using a fluent API.
 * It extends the ButtonBuilderBase by supporting to bind the button's selected state to a boolean property.
 */
public class ToggleButtonBuilder extends ButtonBaseBuilder<ToggleButton, ToggleButtonBuilder> {
    private final List<Property<Boolean>> selectedList = new ArrayList<>();
    private @Nullable Boolean selected = null;

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

    /**
     * Sets the selected state of the {@link ToggleButton}.
     *
     * @param selected the boolean value to set the selected state of the {@link ToggleButton}
     * @return this ToggleButtonBuilder instance
     */
    public ToggleButtonBuilder selected(boolean selected) {
        this.selected = selected;
        return self();
    }

    @Override
    public ToggleButton build() {
        ToggleButton button = super.build();

        selectedList.forEach(selected -> Bindings.bindBidirectional(selected, button.selectedProperty()));

        if (selected != null) {
            button.setSelected(selected);
        }

        return button;
    }
}
