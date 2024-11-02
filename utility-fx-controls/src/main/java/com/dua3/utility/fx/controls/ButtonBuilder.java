package com.dua3.utility.fx.controls;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The ButtonBuilder class is a utility class used to build Button instances in a fluent way.
 * It provides methods to set properties such as text, graphic, tooltip, action, and disabled state
 * of the button. The built button can be obtained by calling the build() method.
 * <p>
 * Usage example:
 * <pre>
 *     {@code
 *     ButtonBuilder<Button> builder = new ButtonBuilder<>(Button::new);
 *     Button button = builder
 *         .text("Click me")
 *         .graphic(new ImageView("icon.png"))
 *         .tooltip("Tooltip text")
 *         .action(event -> System.out.println("Button clicked"))
 *         .bindDisabled(disabledProperty)
 *         .build();
 *     }
 * </pre>
 * In the example above, a new ButtonBuilder instance is created and bound to the Button class.
 * The text, graphic, tooltip, action, and disabled state of the button are set using the builder
 * methods. Finally, the build() method is called to create the button instance with the specified properties.
 *
 * @param <B> the type of Button subclass to build
 */
public class ButtonBuilder<B extends ButtonBase> {
    private final Supplier<? extends B> factory;
    private @Nullable String text = null;
    private @Nullable Node graphic = null;
    private @Nullable String tooltip = null;
    private @Nullable EventHandler<ActionEvent> action = null;
    private @Nullable ObservableValue<Boolean> disabled = null;

    /**
     * Constructor.
     *
     * @param factory the factory method for Button instances
     */
    ButtonBuilder(Supplier<? extends B> factory) {
        this.factory = factory;
    }

    /**
     * Set text for the button.
     *
     * @param text the text
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Set graphic for the button.
     *
     * @param graphic the graphic to use
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> graphic(Node graphic) {
        this.graphic = graphic;
        return this;
    }

    /**
     * Set tooltip for the button.
     *
     * @param tooltip the tooltip text
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> tooltip(String tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    /**
     * Set event handler for the button.
     *
     * @param action the {@link EventHandler}
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> action(EventHandler<ActionEvent> action) {
        this.action = action;
        return this;
    }

    /**
     * Set action for the button.
     *
     * @param action the action to perform when pressed
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> action(Runnable action) {
        this.action = evt -> action.run();
        return this;
    }

    /**
     * Bind the button's disabled state to an {@link ObservableValue}.
     * @param disabled the value to bind the button's disableProperty to
     * @return this ButtonBuilder instance
     */
    public ButtonBuilder<B> bindDisabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Build the button.
     *
     * @return new button instance
     */
    public B build() {
        B control = factory.get();

        if (text != null) {
            control.setText(text);
        }
        if (graphic != null) {
            control.setGraphic(graphic);
        }
        if (tooltip != null) {
            control.setTooltip(new Tooltip(tooltip));
        }
        if (action != null) {
            control.setOnAction(action);
        }
        if (disabled != null) {
            control.disableProperty().bind(disabled);
        }

        return control;
    }

}
