package com.dua3.utility.fx.controls.abstract_builders;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An abstract base class for building nodes, providing a fluent API for configuring and creating instances
 * of the node type specified by the generic parameter {@code N}.
 *
 * @param <C>  the type of control to be built
 * @param <B> the type of the concrete builder
 */
public abstract class ControlBuilder<C extends Control, B extends ControlBuilder<C, B>> extends RegionBuilder<C, B> {
    private @Nullable ObservableValue<String> tooltip;
    private boolean focusTraversable = true;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected ControlBuilder(Supplier<? extends C> factory) {
        super(factory);
    }

    /**
     * Sets the focus-traversable property of the control being built.
     *
     * @param focusTraversable a boolean indicating whether the control should be focus traversable
     * @return this builder instance for method chaining
     */
    public B focusTraversable(boolean focusTraversable) {
        this.focusTraversable = focusTraversable;
        return self();
    }

    /**
     * Build the Control.
     *
     * @return new Control instance
     */
    @Override
    public C build() {
        C node = super.build();
        apply(tooltip, t -> {
            Tooltip tt = new Tooltip();
            tt.textProperty().bind(t);
            node.setTooltip(tt);
        });
        node.setFocusTraversable(focusTraversable);
        return node;
    }

    /**
     * Set the tooltip for the control.
     *
     * @param tooltip the tooltip text
     * @return this ControlBuilder instance
     */
    public B tooltip(String tooltip) {
        this.tooltip = new SimpleStringProperty(tooltip);
        return self();
    }

    /**
     * Set the tooltip for the control.
     *
     * @param tooltip the tooltip text
     * @return this ControlBuilder instance
     */
    public B tooltip(ObservableValue<String> tooltip) {
        this.tooltip = tooltip;
        return self();
    }

}
