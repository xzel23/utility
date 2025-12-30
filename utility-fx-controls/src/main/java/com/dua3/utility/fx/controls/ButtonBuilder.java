package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ButtonBaseBuilder;
import javafx.scene.control.ButtonBase;

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
 *         .disabled(disabledProperty)
 *         .build();
 *     }
 * </pre>
 * In the example above, a new ButtonBuilder instance is created and bound to the Button class.
 * The text, graphic, tooltip, action, and disabled state of the button are set using the builder
 * methods. Finally, the build() method is called to create the button instance with the specified properties.
 *
 * @param <B> the type of Button subclass to build
 */
public class ButtonBuilder<B extends ButtonBase> extends ButtonBaseBuilder<B, ButtonBuilder<B>> {

    /**
     * Constructor.
     *
     * @param factory the factory method for Button instances
     */
    ButtonBuilder(Supplier<? extends B> factory) {
        super(factory);
    }

}
