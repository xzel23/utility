package com.dua3.utility.fx.controls;

import javafx.scene.control.DialogPane;

import java.util.function.Supplier;

/**
 * Abstract base for DialogPane builders.
 *
 * @param <D> the type of the dialog or pane to build
 * @param <B> the type of the builder
 * @param <R> the result type
 */
public class AbstractPaneBuilder<D extends DialogPane & Supplier<R>, B extends AbstractPaneBuilder<D, B, R>, R>
        extends AbstractDialogPaneBuilder<D, B, R> {
    protected String next;

    protected AbstractPaneBuilder() {
        super(DialogPane::setHeaderText);
    }

    @SuppressWarnings("unchecked")
    public B next(String s) {
        this.next = s;
        return (B) this;
    }

}
