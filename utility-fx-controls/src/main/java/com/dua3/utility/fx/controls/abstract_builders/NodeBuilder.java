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
    @SuppressWarnings("unchecked")
    protected final B self() {
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
        return node;
    }

    /**
     * Set the node's disabled state to the supplied value.
     *
     * <p><strong>NOTE: </strong>Do not use together with {@link #enabled(ObservableValue)} and
     * {@link #disabled(ObservableValue)}.
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
     * <p><strong>NOTE: </strong>Use either this method or {@link #enabled(ObservableValue)}, not both.
     *
     * @param disabled the value to bind the node's disableProperty to
     * @return this NodeBuilder instance
     * @deprecated use {@link #disabled(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindDisabled(ObservableValue<Boolean> disabled) {
        return disabled(disabled);
    }

    /**
     * Bind the node's disabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #enabled(ObservableValue)}, not both.
     *
     * @param disabled the value to bind the node's disableProperty to
     * @return this NodeBuilder instance
     */
    public B disabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return self();
    }

    /**
     * Bind the button's enabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #disabled(ObservableValue)}, not both.
     *
     * @param enabled the value to bind the button's disableProperty to
     * @return this NodeBuilder instance
     * @deprecated use {@link #enabled(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindEnabled(ObservableValue<Boolean> enabled) {
        return enabled(enabled);
    }

    /**
     * Bind the button's enabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #disabled(ObservableValue)}, not both.
     *
     * @param enabled the value to bind the button's disableProperty to
     * @return this NodeBuilder instance
     */
    public B enabled(ObservableValue<Boolean> enabled) {
        this.disabled = Bindings.createBooleanBinding(() -> !enabled.getValue(), enabled);
        return self();
    }


}
