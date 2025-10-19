package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An abstract base class for building input controls with customizable properties and behavior.
 * This generic builder class supports method chaining and facilitates the creation of
 * input controls with a variety of configurations.
 *
 * @param <B> the specific subtype of the builder (to enable method chaining).
 * @param <V> the type of value handled by the input control being built.
 */
public abstract class InputControlBuilder<B extends InputControlBuilder<B, V>, V> {
    private Supplier<@Nullable V> dflt;
    private final List<Consumer<@Nullable V>> onChangeListeners = new ArrayList<>();

    /**
     * Constructor for the InputControlBuilder class.
     */
    protected InputControlBuilder() {
    }

    /**
     * Returns the current instance of the class.
     * This method is used to facilitate method chaining in subclasses by returning
     * the current instance cast to the appropriate type parameter.
     *
     * @return the current instance of type {@code B}.
     */
    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * Sets a callback to be invoked when the value of the slider changes.
     *
     * @param onChange a DoubleConsumer that will be invoked with the new slider value whenever it changes
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public B onChange(Consumer<@Nullable V> onChange) {
        onChangeListeners.add(onChange);
        return self();
    }

    /**
     * Sets the default value of the slider.
     *
     * @param dflt the default value to set
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public B setDefault(@Nullable V dflt) {
        this.dflt = () -> dflt;
        return self();
    }

    /**
     * Sets the default value supplier of the slider.
     *
     * @param dflt the default value to set
     * @return this instance of {@code SliderBuilder} for method chaining.
     */
    public B setDefault(Supplier<@Nullable V> dflt) {
        this.dflt = dflt;
        return self();
    }

    /**
     * Applies the given consumer function to the input object if the object is not null.
     * This method serves as a utility to conditionally execute operations on non-null objects.
     *
     * @param <T> the type of the object being checked and passed to the consumer
     * @param obj the object to check for nullability and pass to the consumer if non-null
     * @param consumer the operation to apply to the object if it is not null
     */
    protected <T> void applyIfNonNull(@Nullable T obj, Consumer<T> consumer) {
        if (obj != null) {
            consumer.accept(obj);
        }
    }

    /**
     * Retrieves the default value supplier associated with this instance.
     *
     * @return a {@code Supplier} that provides the default value, or {@code null} if no default value supplier is set.
     */
    protected Supplier<@Nullable V> getDefault() {
        return dflt;
    }

    /**
     * Retrieves the list of change listeners associated with this control builder.
     * These listeners are invoked whenever the value of the associated input control changes.
     *
     * @return a list of {@code Consumer} instances that handle value change events,
     *         or an empty list if no listeners have been added.
     */
    protected List<Consumer<@Nullable V>> getOnChangeListeners() {
        return onChangeListeners;
    }

    /**
     * Adds listeners to monitor and respond to changes in the value of the given {@code InputControl}.
     * Each registered change listener in {@code onChangeListeners} will be called whenever the
     * control's value is updated.
     *
     * @param control the {@code InputControl} instance to which the change listeners will be applied
     */
    protected void applyTo(InputControl<V> control) {
        for (Consumer<@Nullable V> changeListener : onChangeListeners) {
            control.valueProperty().addListener((obs, old, val) -> changeListener.accept(val));
        }
    }
}
