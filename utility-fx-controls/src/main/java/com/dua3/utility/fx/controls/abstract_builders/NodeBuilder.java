package com.dua3.utility.fx.controls.abstract_builders;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An abstract base class for building nodes, providing a fluent API for configuring and creating instances
 * of the node type specified by the generic parameter {@code N}.
 *
 * @param <N> the type of node to be built
 * @param <B> the type of the concrete builder
 */
public abstract class NodeBuilder<N extends Node, B extends NodeBuilder<N, B>> {
    private final Supplier<? extends N> factory;
    private @Nullable ObservableValue<Boolean> disabled = null;
    private @Nullable Double width = null;
    private @Nullable Double height = null;

    /**
     * Constructs a new instance of the NodeBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected NodeBuilder(Supplier<? extends N> factory) {
        this.factory = factory;
    }

    /**
     * Returns the current instance of the builder with the proper type.
     *
     * @return this instance of the builder
     */
    protected final B self() {
        //noinspection unchecked
        return (B) this;
    }

    /**
     * Applies a value to the provided consumer if the value is not null.
     *
     * @param <T>   the type of the value to apply
     * @param value the value to be applied; can be null
     * @param setter the consumer to which the value is applied if not null
     */
    protected static <T> void apply(@Nullable T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Binds the given {@link ObservableValue} to the specified {@link Property}.
     *
     * <p>This method ensures that the property is dynamically updated to reflect the value
     * of the {@link ObservableValue}, if it is not null.
     *
     * @param <T>      the type of the value to be bound
     * @param value    the {@link ObservableValue} to be bound to the property; may be null
     * @param property the {@link Property} to bind the observable value to
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected static <T> void apply(@Nullable ObservableValue<? extends T> value, Property<? super T> property) {
        if (value != null) {
            if (value instanceof Property valueProperty) {
                property.bindBidirectional(valueProperty);
            } else {
                property.bind(value);
            }
        }
    }

    /**
     * Builds and returns a new instance of the node type specified by the builder.
     *
     * <p>Derived classes should always call {@code super.build()} to get an instance and then apply
     * the added configuration.
     *
     * @return a new instance of the node created by the factory associated with this builder
     */
    public N build() {
        N node = factory.get();
        apply(disabled, node.disableProperty());
        apply(width, node::prefWidth);
        apply(height, node::prefHeight);
        return node;
    }

    /**
     * Set the node's disabled state to the supplied value.
     *
     * <p><strong>NOTE: </strong>Do not use together with {@link #bindEnabled(ObservableValue)} and
     * {@link #bindDisabled(ObservableValue)}.
     *
     * @param disabled the value to bind the node's disableProperty to
     * @return this NodeBuilder instance
     */
    public B disabled(boolean disabled) {
        this.disabled = new SimpleBooleanProperty(disabled);
        return self();
    }

    /**
     * Bind the node's disabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #bindEnabled(ObservableValue)}, not both.
     *
     * @param disabled the value to bind the node's disableProperty to
     * @return this NodeBuilder instance
     */
    public B bindDisabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return self();
    }

    /**
     * Bind the button's enabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #bindDisabled(ObservableValue)}, not both.
     *
     * @param enabled the value to bind the button's disableProperty to
     * @return this NodeBuilder instance
     */
    public B bindEnabled(ObservableValue<Boolean> enabled) {
        this.disabled = Bindings.createBooleanBinding(() -> !enabled.getValue(), enabled);
        return self();
    }

    /**
     * Sets the preferred width for the node being built.
     *
     * @param width the preferred width to set for the node
     * @return this instance of the builder
     */
    public B prefWidth(double width) {
        this.width = width;
        return self();
    }

    /**
     * Sets the preferred height of the node being built.
     *
     * @param height the preferred height to set
     * @return this instance of the builder
     */
    public B prefHeight(double height) {
        this.height = height;
        return self();
    }

    /**
     * Sets the preferred width and height for the node being built.
     *
     * @param width  the preferred width to set for the node
     * @param height the preferred height to set for the node
     * @return this instance of the builder
     */
    public B prefSize(double width, double height) {
        this.width = width;
        this.height = height;
        return self();
    }

}
