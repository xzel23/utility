package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.controls.ButtonDef;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Abstract base for DialogPane builders.
 *
 * @param <D> the type of the dialog or pane to build
 * @param <B> the type of the builder
 * @param <R> the result type
 */
public abstract class PaneBuilder<D extends Pane & Supplier<R>, B extends PaneBuilder<D, B, R>, R>
        extends DialogPaneBuilder<D, B, R> {
    private Map<ButtonType, String> next = Collections.emptyMap();

    private final List<ButtonDef<R>> buttons = new ArrayList<>();

    /**
     * Constructs an instance of the PaneBuilder class.
     * <p>
     * This constructor initializes the builder by associating it with a header setter
     * that configures the header text of the dialog pane.
     * The constructor must be called by subclasses to initialize the base class.
     *
     * @param formatter the {@link MessageFormatter} to use
     * @param setHeaderText the BiConsumer that sets the header text of the dialog pane
     */
    protected PaneBuilder(MessageFormatter formatter, BiConsumer<? super D, String> setHeaderText) {
        super(formatter, setHeaderText);
    }

    /**
     * Sets the next string parameter for the builder.
     *
     * @param s the next string to be set
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    public B next(String s) {
        this.next = Map.of(ButtonType.NEXT, s);
        return self();
    }

    /**
     * Sets the mapping for the "next" parameter in the builder.
     * This method accepts a map where keys are {@code ButtonType} instances and
     * values are their corresponding string representations. It allows setting
     * multiple "next" parameters at once for the builder.
     *
     * @param next a {@code Map} containing {@code ButtonType} keys and their associated string values
     * @return the current builder instance of type {@code B}
     */
    public B next(Map<ButtonType, String> next) {
        this.next = next;
        return self();
    }

    /**
     * Retrieves the value of the "next" parameter if it is set.
     *
     * @return an {@code Optional<String>} containing the value of the next parameter,
     *         or an empty {@code Optional} if the value is not set.
     */
    public Map<ButtonType, String> getNext() {
        return next;
    }

    @Override
    public final List<ButtonDef<R>> getButtonDefs() {
        return buttons;
    }
}
