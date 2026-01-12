package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.PropertyConverter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An abstract base class for building {@link MenuItem}s.
 *
 * @param <M> the type of MenuItem to be built
 * @param <B> the type of the concrete builder
 */
public abstract class MenuItemBuilder<M extends MenuItem, B extends MenuItemBuilder<M, B>> {
    private final Supplier<? extends M> factory;
    private @Nullable ObservableValue<String> text;
    private @Nullable ObservableValue<Node> graphic;
    private @Nullable ObservableValue<EventHandler<ActionEvent>> action;
    private @Nullable ObservableValue<KeyCombination> accelerator;
    private @Nullable ObservableValue<Boolean> disabled;
    private @Nullable ObservableValue<String> tooltip;

    /**
     * Constructs a new instance of the MenuItemBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the MenuItem type to be built
     */
    protected MenuItemBuilder(Supplier<? extends M> factory) {
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
     * Provides an observable value representing the disabled state.
     *
     * @return an ObservableValue of type Boolean that indicates whether the state is disabled.
     */
    protected @Nullable ObservableValue<Boolean> disabledValue() {
        return disabled;
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
     * Builds and returns a new instance of the MenuItem type specified by the builder.
     *
     * <p>Derived classes should always call {@code super.build()} to get an instance and then apply
     * the added configuration.
     *
     * @return a new instance of the MenuItem created by the factory associated with this builder
     */
    public M build() {
        M item = factory.get();
        apply(text, item.textProperty());
        apply(graphic, item.graphicProperty());
        apply(action, item.onActionProperty());
        apply(accelerator, item.acceleratorProperty());
        apply(disabled, item.disableProperty());
        if (tooltip != null) {
            Tooltip tt = new Tooltip();
            tt.textProperty().bind(tooltip);
            // Best effort, MenuItem has no tooltip property. 
            // We can try to install it on the graphic if it exists.
            item.graphicProperty().addListener((obs, oldGraphic, newGraphic) -> {
                if (oldGraphic != null) {
                    Tooltip.uninstall(oldGraphic, tt);
                }
                if (newGraphic != null) {
                    Tooltip.install(newGraphic, tt);
                }
            });
            Node currentGraphic = item.getGraphic();
            if (currentGraphic != null) {
                Tooltip.install(currentGraphic, tt);
            }
        }
        return item;
    }

    /**
     * Set the tooltip for the MenuItem.
     *
     * @param tooltip the tooltip text
     * @return this MenuItemBuilder instance
     */
    public B tooltip(String tooltip) {
        this.tooltip = new SimpleStringProperty(tooltip);
        return self();
    }

    /**
     * Set the tooltip for the MenuItem.
     *
     * @param tooltip the tooltip text
     * @return this MenuItemBuilder instance
     */
    public B tooltip(ObservableValue<String> tooltip) {
        this.tooltip = tooltip;
        return self();
    }

    /**
     * Set the text for the MenuItem.
     *
     * @param text the text
     * @return this MenuItemBuilder instance
     */
    public B text(String text) {
        this.text = new SimpleStringProperty(text);
        return self();
    }

    /**
     * Set the text for the MenuItem.
     *
     * @param text the text
     * @return this MenuItemBuilder instance
     */
    public B text(ObservableValue<String> text) {
        this.text = text;
        return self();
    }

    /**
     * Set the graphic for the MenuItem.
     *
     * @param graphic the graphic to use
     * @return this MenuItemBuilder instance
     */
    public B graphic(Node graphic) {
        this.graphic = new SimpleObjectProperty<>(graphic);
        return self();
    }

    /**
     * Set the graphic for the MenuItem.
     *
     * @param graphic the graphic to use
     * @return this MenuItemBuilder instance
     */
    public B graphic(ObservableValue<Node> graphic) {
        this.graphic = graphic;
        return self();
    }

    /**
     * Set the event handler for the MenuItem.
     *
     * @param action the {@link EventHandler}
     * @return this MenuItemBuilder instance
     */
    public B action(EventHandler<ActionEvent> action) {
        this.action = new SimpleObjectProperty<>(action);
        return self();
    }

    /**
     * Set the action for the MenuItem.
     *
     * @param action the action to perform when pressed
     * @return this MenuItemBuilder instance
     */
    public B action(Runnable action) {
        return action(evt -> action.run());
    }

    /**
     * Set action for the MenuItem.
     *
     * @param action the action to perform when pressed
     * @return this MenuItemBuilder instance
     */
    public B action(ObservableValue<? extends Runnable> action) {
        this.action = PropertyConverter.convertReadOnly(action, r -> event -> r.run());
        return self();
    }

    /**
     * Set the accelerator for the MenuItem.
     *
     * @param accelerator the {@link KeyCombination}
     * @return this MenuItemBuilder instance
     */
    public B accelerator(KeyCombination accelerator) {
        this.accelerator = new SimpleObjectProperty<>(accelerator);
        return self();
    }

    /**
     * Set the accelerator for the MenuItem.
     *
     * @param accelerator the {@link KeyCombination}
     * @return this MenuItemBuilder instance
     */
    public B accelerator(ObservableValue<KeyCombination> accelerator) {
        this.accelerator = accelerator;
        return self();
    }

    /**
     * Set the MenuItem's disabled state to the supplied value.
     *
     * <p><strong>NOTE: </strong>Do not use together with {@link #enabled(ObservableValue)} and
     * {@link #disabled(ObservableValue)}.
     *
     * @param disabled the value to bind the MenuItem's disableProperty to
     * @return this MenuItemBuilder instance
     */
    public B disabled(boolean disabled) {
        this.disabled = new SimpleBooleanProperty(disabled);
        return self();
    }

    /**
     * Bind the MenuItem's disabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #enabled(ObservableValue)}, not both.
     *
     * @param disabled the value to bind the MenuItem's disableProperty to
     * @return this MenuItemBuilder instance
     */
    public B disabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return self();
    }

    /**
     * Bind the MenuItem's enabled state to an {@link ObservableValue}.
     *
     * <p><strong>NOTE: </strong>Use either this method or {@link #disabled(ObservableValue)}, not both.
     *
     * @param enabled the value to bind the MenuItem's disableProperty to
     * @return this MenuItemBuilder instance
     */
    public B enabled(ObservableValue<Boolean> enabled) {
        this.disabled = Bindings.createBooleanBinding(() -> !enabled.getValue(), enabled);
        return self();
    }
}
