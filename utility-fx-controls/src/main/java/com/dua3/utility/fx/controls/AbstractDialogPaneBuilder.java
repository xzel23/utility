// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.jspecify.annotations.Nullable;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Abstract base class for DialogPane builders.
 * <p>
 * Provides a fluent interface to create Dialog panes.
 *
 * @param <D> the type of the dialog or pane to build
 * @param <B> the type of the builder
 * @param <R> the result type
 */
public abstract class AbstractDialogPaneBuilder<D, B extends AbstractDialogPaneBuilder<D, B, R>, R> {

    public static final BooleanExpression ALWAYS_TRUE = new ReadOnlyBooleanWrapper(true);

    private final BiConsumer<? super D, ? super String> headerSetter;
    private Supplier<? extends D> dialogSupplier;
    private @Nullable String header = null;
    private ResultHandler<R> resultHandler = (b, r) -> true;
    private final List<InputDialogPane.ButtonDef<R>> buttons = new ArrayList<>();

    AbstractDialogPaneBuilder(
            BiConsumer<? super D, ? super String> headerSetter
    ) {
        this.dialogSupplier = () -> {throw new IllegalStateException("call setDialogSupplier() first");};
        this.headerSetter = headerSetter;
    }

    protected final void setDialogSupplier(Supplier<? extends D> dialogSupplier) {
        this.dialogSupplier = dialogSupplier;
    }

    /**
     * Create Alert instance.
     *
     * @return Alert instance
     */
    public D build() {
        D dlg = dialogSupplier.get();

        applyIfNotNull(headerSetter, dlg, header);

        return dlg;
    }

    protected static <C, D> void applyIfNotNull(BiConsumer<C, D> consumer, @Nullable C a, @Nullable D b) {
        if (a != null && b != null) {
            consumer.accept(a, b);
        }
    }

    /**
     * Set Alert header text.
     *
     * @param fmt  the format String as defined by {@link java.util.Formatter}
     * @param args the arguments passed to the formatter
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public B header(String fmt, Object... args) {
        this.header = String.format(fmt, args);
        return (B) this;
    }

    /**
     * Sets the result handler for this dialog pane builder.
     *
     * @param resultHandler the result handler to be used for handling dialog results
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    public B resultHandler(ResultHandler<R> resultHandler) {
        this.resultHandler = resultHandler;
        return (B) this;
    }

    /**
     * Gets the result handler for this dialog pane builder.
     *
     * @return the result handler managing dialog results
     */
    public ResultHandler<R> getResultHandler() {
        return resultHandler;
    }

    /**
     * Dialog(Pane) result handler.
     *
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface ResultHandler<R> {
        /**
         * Handle result.
         *
         * @param btn    the button that was pressed
         * @param result the dialog/pane result as returned by the result converter
         * @return true, if it's ok to proceed (the current page should be left)
         * false otherwise
         */
        boolean handleResult(ButtonType btn, R result);
    }

    /**
     * Adds a button definition to the dialog pane builder.
     * This method attaches the specified button definition to the builder's list of buttons
     * and returns the current builder instance for method chaining.
     *
     * @param button the button definition to be added, containing button type, result handler,
     *               action to execute, and enablement state
     * @return the current builder instance with the added button definition
     */
    protected B button(InputDialogPane.ButtonDef<R> button) {
        this.buttons.add(button);
        return (B) this;
    }

    /**
     * Retrieves the list of button definitions associated with this dialog pane builder.
     * If no button definitions have been explicitly provided, a default list containing
     * "OK" and "Cancel" buttons is returned. The buttons are defined with their respective
     * actions, result handlers, and enablement state.
     *
     * @return a list of button definitions for this dialog pane builder
     */
    protected List<InputDialogPane.ButtonDef<R>> buttons() {
        if (buttons.isEmpty()) {
            return List.of(
                    new InputDialogPane.ButtonDef<>(
                            ButtonType.CANCEL,
                            (btn, r) -> true,
                            dlg -> {},
                            ALWAYS_TRUE
                    ),
                    new InputDialogPane.ButtonDef<>(
                            ButtonType.OK,
                            (btn,r) -> true,
                            dlg -> {},
                            ALWAYS_TRUE
                    )
            );
        } else {
            return Collections.unmodifiableList(buttons);
        }
    }
}
