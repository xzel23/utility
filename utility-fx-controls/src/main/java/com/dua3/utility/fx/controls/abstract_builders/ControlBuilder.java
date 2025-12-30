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

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected ControlBuilder(Supplier<? extends C> factory) {
        super(factory);
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
     * @deprecated use {@link #tooltip(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindTooltip(ObservableValue<String> tooltip) {
        return tooltip(tooltip);
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
