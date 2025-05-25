package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.PropertyConverter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An abstract base builder class for creating instances of ButtonBase subclasses.
 * Provides a fluent API for configuring properties such as actions and bindings for the button.
 *
 * @param <B>  the type of ButtonBase subclass to build
 * @param <BB> the type of the builder subclass
 */
public abstract class ButtonBaseBuilder<B extends ButtonBase, BB extends ButtonBaseBuilder<B, BB>> extends LabeledBuilder<B, BB> {
    private @Nullable ObservableValue<EventHandler<ActionEvent>> action;

    /**
     * Constructor.
     *
     * @param factory the factory method for Button instances
     */
    protected ButtonBaseBuilder(Supplier<? extends B> factory) {
        super(factory);
    }

    /**
     * Build the button.
     *
     * @return new button instance
     */
    @Override
    public B build() {
        B node = super.build();
        apply(action, node.onActionProperty());
        return node;
    }

    /**
     * Set the event handler for the button.
     *
     * @param action the {@link EventHandler}
     * @return this ButtonBaseBuilder instance
     */
    public BB action(EventHandler<ActionEvent> action) {
        this.action = new SimpleObjectProperty<>(action);
        return self();
    }

    /**
     * Set the action for the button.
     *
     * @param action the action to perform when pressed
     * @return this ButtonBaseBuilder instance
     */
    public BB action(Runnable action) {
        return action(evt -> action.run());
    }

    /**
     * Set action for the button.
     *
     * @param action the action to perform when pressed
     * @return this ButtonBaseBuilder instance
     */
    public BB bindAction(ObservableValue<? extends Runnable> action) {
        this.action = PropertyConverter.convertReadOnly(action, r -> event -> r.run());
        return self();
    }
}
