package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.ButtonType;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a definition for a button within a dialog pane. The {@code ButtonDef} class
 * encapsulates the button's type, the result handler to process the action associated with the button,
 * the action to perform when the button is triggered, and a function to control the enablement state
 * of the button.
 *
 * @param <R>           the type of the result associated with this button
 * @param type          the type of the button (e.g., OK, CANCEL, etc.)
 * @param resultHandler the handler to process the result when the button is pressed
 * @param action        the action to execute when the button is clicked
 * @param enabled       a function taking the dialog pane as argument and returning a
 *                      BooleanExpression to determine whether the button is enabled
 */
public record ButtonDef<R>(
        ButtonType type,
        DialogPaneBuilder.ResultHandler<R> resultHandler,
        Consumer<InputDialogPane<? extends Object>> action,
        Function<InputDialogPane<? extends Object>, BooleanExpression> enabled
) {
    /**
     * Creates and returns a {@code ButtonDef} instance configured as a cancel button.
     * The cancel button is defined with a {@code ButtonType.CANCEL},
     * a result handler that always returns {@code true}, an action that performs no operation,
     * and an enablement state that is always {@code true}.
     *
     * @param <Q> the type of the result associated with this button
     * @return a {@code ButtonDef} instance representing a cancel button
     */
    public static <Q> ButtonDef<Q> cancel() {
        return of(
                ButtonType.CANCEL,
                (bt, r) -> true,
                idp -> {},
                idp -> FxUtil.ALWAYS_TRUE
        );
    }

    /**
     * Creates a new {@code ButtonDef} instance with a default configuration
     * based on the given {@code ButtonType}.
     * <p>
     * The returned {@code ButtonDef} will have:
     * - An action that performs no operation.
     * - A result handler that always returns {@code true}.
     * - An enablement state that is always {@code true}.
     *
     * @param <T> the type of the result associated with the button
     * @param btn the {@code ButtonType} representing the type of the button (e.g., OK, CANCEL, etc.)
     * @return a new {@code ButtonDef} instance configured with the provided {@code ButtonType}
     */
    public static <T> ButtonDef<T> of(ButtonType btn) {
        return of(btn, idp -> {});
    }

    /**
     * Creates a new {@code ButtonDef} instance with the specified button type, a consumer
     * action to handle button events, and a default result handler.
     * <p>
     * The returned {@code ButtonDef} will have:
     * - An action that performs no operation.
     * - An enablement state that is always {@code true}.
     *
     * @param <T>    the type of the result associated with the button
     * @param btn    the {@code ButtonType} representing the type of the button (e.g., OK, CANCEL, etc.)
     * @param action a {@link Consumer} specifying the operation to execute when the button is clicked
     * @return a new {@code ButtonDef} instance configured with the provided button type and action
     */
    public static <T> ButtonDef<T> of(ButtonType btn, Consumer<InputDialogPane<?>> action) {
        return of(btn, (bt, r) -> true, action);
    }

    /**
     * Creates a new {@code ButtonDef} instance configured with the specified button type,
     * action, and result handler. By default, the button is always enabled.
     * <p>
     * The returned {@code ButtonDef} will have:
     * - An enablement state that is always {@code true}.
     *
     * @param <T>           the type of the result associated with the button
     * @param btn           the {@code ButtonType} representing the type of the button (e.g., OK, CANCEL, etc.)
     * @param resultHandler a {@link DialogPaneBuilder.ResultHandler} to handle the result when the button is pressed
     * @param action        a {@link Consumer} specifying the operation to execute when the button is clicked
     * @return a new {@code ButtonDef} instance configured with the provided parameters and a default enablement state
     */
    public static <T> ButtonDef<T> of(
            ButtonType btn,
            DialogPaneBuilder.ResultHandler<T> resultHandler,
            Consumer<InputDialogPane<?>> action
    ) {
        return new ButtonDef<>(
                btn,
                resultHandler,
                action,
                idp -> FxUtil.ALWAYS_TRUE
        );
    }

    /**
     * Creates a new {@code ButtonDef} instance with the specified parameters.
     *
     * @param <Q>           the type of the result associated with the button
     * @param type          the {@code ButtonType} representing the type of the button (e.g., OK, CANCEL, etc.)
     * @param resultHandler a {@link DialogPaneBuilder.ResultHandler} to handle the result when the button is pressed
     * @param action        a {@link Consumer} specifying the operation to execute when the button is clicked
     * @param enabled       a {@link BooleanExpression} that determines whether the button is enabled or disabled
     * @return a new {@code ButtonDef} instance configured with the provided parameters
     */
    public static <Q> ButtonDef<Q> of(
            ButtonType type,
            DialogPaneBuilder.ResultHandler<Q> resultHandler,
            Consumer<InputDialogPane<?>> action,
            Function<InputDialogPane<?>, BooleanExpression> enabled
    ) {
        return new ButtonDef<>(
                type,
                resultHandler,
                action,
                enabled
        );
    }

}
