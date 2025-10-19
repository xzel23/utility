package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.controls.ButtonDef;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.DialogPane;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Abstract base for DialogPane builders.
 *
 * @param <D> the type of the dialog or pane to build
 * @param <B> the type of the builder
 * @param <R> the result type
 */
public abstract class PaneBuilder<D extends DialogPane & Supplier<R>, B extends PaneBuilder<D, B, R>, R>
        extends DialogPaneBuilder<D, B, R> {
    private @Nullable String next;

    private final List<ButtonDef<R>> buttons = new ArrayList<>();

    /**
     * Constructs an instance of the PaneBuilder class.
     * <p>
     * This constructor initializes the builder by associating it with a header setter
     * that configures the header text of the dialog pane.
     * The constructor must be called by subclasses to initialize the base class.
     *
     * @param formatter the {@link MessageFormatter} to use
     */
    protected PaneBuilder(MessageFormatter formatter) {
        super(formatter, DialogPane::setHeaderText);
    }

    /**
     * Sets the next string parameter for the builder.
     *
     * @param s the next string to be set
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    public B next(String s) {
        this.next = s;
        return self();
    }

    /**
     * Retrieves the value of the "next" parameter if it is set.
     *
     * @return an {@code Optional<String>} containing the value of the next parameter,
     *         or an empty {@code Optional} if the value is not set.
     */
    public Optional<String> getNext() {
        return Optional.ofNullable(next);
    }

    @Override
    public final List<ButtonDef<R>> getButtonDefs() {
        return buttons;
    }
}
