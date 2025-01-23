package com.dua3.utility.fx.controls.abstract_builders;

import javafx.scene.control.DialogPane;
import org.jspecify.annotations.Nullable;

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

    protected PaneBuilder() {
        super(DialogPane::setHeaderText);
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
        return (B) this;
    }

    public Optional<String> getNext() {
        return Optional.ofNullable(next);
    }
}
