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

import org.jspecify.annotations.Nullable;
import javafx.scene.control.ButtonType;

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

    private final BiConsumer<? super D, ? super String> headerSetter;
    private Supplier<? extends D> dialogSupplier;
    private @Nullable String header = null;
    private ResultHandler<R> resultHandler = (b, r) -> true;

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
}
