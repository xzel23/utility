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

package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.controls.ButtonDef;
import com.dua3.utility.text.MessageFormatter;
import org.jspecify.annotations.Nullable;
import javafx.scene.control.ButtonType;

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
public abstract class DialogPaneBuilder<D, B extends DialogPaneBuilder<D, B, R>, R> {

    private final BiConsumer<? super D, ? super String> headerSetter;
    private Supplier<? extends D> dialogSupplier;
    private final MessageFormatter formatter;
    private @Nullable String header = null;
    private ResultHandler<R> resultHandler = (b, r) -> true;

    /**
     * Constructs an instance of DialogPaneBuilder with the specified header setter.
     * The header setter is used to configure the header text of the dialog.
     *
     * @param formatter    the {@link MessageFormatter} to use
     * @param headerSetter a {@code BiConsumer} used to set the header text for the dialog.
     *                     The first parameter is the dialog instance, and the second parameter is the header text.
     */
    protected DialogPaneBuilder(
            MessageFormatter formatter,
            BiConsumer<? super D, ? super String> headerSetter
    ) {
        this.dialogSupplier = () -> {throw new IllegalStateException("call setDialogSupplier() first");};
        this.headerSetter = headerSetter;
        this.formatter = formatter;
    }

    /**
     * Sets the supplier that provides instances of the dialog type.
     * The supplied {@link Supplier} is responsible for creating specific instances of the dialog
     * whenever required by this dialog pane builder.
     *
     * @param dialogSupplier the supplier that provides instances of the dialog type {@code D}
     */
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

    /**
     * Formats the given format string using the provided arguments.
     * This method delegates the formatting process to an associated formatter instance.
     *
     * @param fmt  the format string containing placeholders as specified for formatting
     * @param args the arguments to be inserted into the format string's placeholders
     * @return the formatted string after inserting the provided arguments
     */
    public String format(String fmt, Object... args) {
        return formatter.format(fmt, args);
    }

    /**
     * Applies the given {@link BiConsumer} to the specified inputs if both inputs are non-null.
     * This utility method ensures that the consumer is only executed when both provided arguments
     * are non-null, avoiding potential {@code NullPointerException}.
     *
     * @param <C>      the type of the first input
     * @param <D>      the type of the second input
     * @param consumer the {@link BiConsumer} to execute if both inputs are non-null
     * @param a        the first input, may be null
     * @param b        the second input, may be null
     */
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
        this.header = formatter.format(fmt, args);
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
        boolean handleResult(ButtonType btn, @Nullable R result);
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
    @SuppressWarnings("unchecked")
    protected B button(ButtonDef<R> button) {
        getButtonDefs().add(button);
        return (B) this;
    }

    /**
     * <strong>Replaces</strong> the buttons for this dialog pane builder with the specified button types.
     * Each button type provided is added using the {@link #button(ButtonDef)} method.
     *
     * @param buttons an array of {@link ButtonType} representing the types of buttons to be added
     * @return the current builder instance with the specified buttons added
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final B setButtons(ButtonDef<R>... buttons) {
        getButtonDefs().clear();
        for (var btn: buttons) {
            button(btn);
        }
        return (B) this;
    }

    /**
     * Replaces the current button definitions in the dialog pane builder with the specified button types.
     * Each {@code ButtonType} in the provided array is converted into a corresponding button definition
     * and added to the builder using the {@link #button(ButtonDef)} method.
     *
     * @param buttons an array of {@link ButtonType} representing the types of buttons to be added
     * @return the current builder instance with the specified buttons added
     */
    @SuppressWarnings("unchecked")
    public final B setButtons(ButtonType... buttons) {
        getButtonDefs().clear();
        for (var btn: buttons) {
            button(ButtonDef.of(btn));
        }
        return (B) this;
    }

    /**
     * Retrieves the list of button definitions associated with this dialog pane builder.
     * If no button definitions have been explicitly provided, a default list containing
     * "OK" and "Cancel" buttons is returned. The buttons are defined with their respective
     * actions, result handlers, and enablement state.
     *
     * @return a modifiable list of button definitions for this dialog pane builder
     */
    public abstract List<ButtonDef<R>> getButtonDefs();

    /**
     * Retrieves the message formatter associated with this instance of {@code DialogPaneBuilder}.
     * The message formatter is used to format messages for this dialog pane.
     *
     * @return the {@code MessageFormatter} used for message formatting
     */
    public MessageFormatter getMessageFormatter() {
        return formatter;
    }
}
