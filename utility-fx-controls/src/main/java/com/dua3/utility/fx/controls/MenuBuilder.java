package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.input.KeyCombination;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class MenuBuilder<B extends MenuBuilder<B>, M> {
    private Supplier<M> factory;

    private @Nullable String text;
    private @Nullable Node graphic;
    private @Nullable KeyCombination accelerator;
    private @Nullable Runnable action;

    protected MenuBuilder(Supplier<M> factory) {
        this.factory = factory;
    }

    protected <T> void applyIfNonNull(@Nullable T value, BiConsumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public M build() {
        M m = factory.get();
        applyIfNonNull(text, m::setText);
        applyIfNonNull(graphic, m::setGraphic);
        applyIfNonNull(accelerator, m::setAccelerator);
        applyIfNonNull(action, m::setOnAction);
        return m;
    }

    public B text(String text) {
        this.text = text;
        return self();
    }

    public B graphic(Node graphic) {
        this.graphic = graphic;
        return self();
    }

    public B accelerator(KeyCombination accelerator) {
        this.accelerator = accelerator;
        return self();
    }

    public B action(Runnable action) {
        this.action = action;
        return self();
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    ;
}
