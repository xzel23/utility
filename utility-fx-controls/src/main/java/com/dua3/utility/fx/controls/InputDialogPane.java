package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An abstract base class for dialog panes that handles input and maintains a validity state.
 * This class also manages button actions within the dialog pane.
 *
 * @param <R> the type of the result produced by the input dialog pane
 */
public abstract class InputDialogPane<R> extends DialogPane implements Supplier<@Nullable R> {
    private static final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(InputDialogPane.class);

    /**
     * Constructor.
     */
    protected InputDialogPane() {
    }

    /**
     * Property holding the valid state.
     *
     * @see #validProperty()
     */
    protected final BooleanProperty valid = new SimpleBooleanProperty(false);

    /**
     * Represents the definition of a button in an {@link InputDialogPane}.
     * This class encapsulates the details required to configure a button,
     * including its type, associated action, result handler, and enablement state.
     *
     * @param <R> the result type associated with the dialog
     *
     * @param type          the {@code ButtonType} representing the type of the button
     * @param resultHandler a {@link DialogPaneBuilder.ResultHandler} to handle the result when the button is pressed
     * @param action        a {@link Consumer} that specifies the action to be executed when the button is clicked
     * @param enabled       a {@link BooleanExpression} indicating whether the button is enabled or disabled
     */
    public record ButtonDef<R>(
            ButtonType type,
            DialogPaneBuilder.ResultHandler<R> resultHandler,
            Consumer<InputDialogPane<R>> action,
            BooleanExpression enabled
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
            return new ButtonDef<>(
                    ButtonType.CANCEL,
                    (bt, r) -> true,
                    idp -> {},
                    FxUtil.ALWAYS_TRUE
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
        public static <Q> ButtonDef<Q> create(
                ButtonType type,
                DialogPaneBuilder.ResultHandler<Q> resultHandler,
                Consumer<InputDialogPane<Q>> action,
                BooleanExpression enabled
        ) {
            return new ButtonDef<>(
                    type,
                    resultHandler,
                    action,
                    enabled
            );
        }
    }

    /**
     * A collection of button definitions used to configure actions and behavior
     * within an {@link InputDialogPane}. Each item in this list represents a
     * {@link ButtonDef} instance that specifies the type, action, result handling,
     * and enablement state for a button.
     *
     * <p>This list is initialized as an empty {@link ArrayList} and is typically
     * populated by invoking methods that define the buttons for the dialog pane.
     * The order of buttons in this list reflects their intended arrangement
     * within the dialog.
     */
    protected final List<ButtonDef<? super R>> buttons = new ArrayList<>();

    /**
     * Initializes the input dialog pane, setting up necessary configurations
     * or state required before the pane is displayed. This method should be
     * invoked prior to rendering the dialog pane to ensure all components
     * are properly prepared.
     * <p>
     * Concrete implementations must define the behavior for initializing
     * input fields, validation logic, and any other setup needed for the dialog pane.
     */
    public abstract void init();

    /**
     * Get the valid state property.
     * <p>
     * This boolean property indicates the validity state of the input in the dialog pane.
     * It is used to dynamically track and manage whether the input provided by the user
     * meets certain predefined validation criteria.
     * <p>
     * The default value for this property is {@code false}, meaning the input is considered invalid
     * until explicitly validated or updated by the dialog's logic.
     * <p>+
     * This property is bound to the validation mechanisms within the dialog pane,
     * allowing it to automatically update based on user interactions or changes in the input fields.
     *
     * @return the valid property
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Adds a button to the dialog pane with the specified type, result handler, action,
     * and enablement state. The button is configured to handle actions, execute
     * specified logic, and optionally bind its enablement state to a BooleanExpression.
     *
     * @param type          the type of the button to add
     * @param resultHandler an optional result handler to process the action associated
     *                      with the button; can be null
     * @param action        a consumer that performs an action on the InputDialogPane
     *                      when the button is triggered
     * @param enabled       an optional BooleanExpression that determines if the button
     *                      should be enabled or disabled dynamically; can be null
     */
    public void addButton(
            ButtonType type,
            DialogPaneBuilder.@Nullable ResultHandler<? super @Nullable R> resultHandler,
            Consumer<? super InputDialogPane<R>> action,
            @Nullable ObservableBooleanValue enabled
    ) {
        ObservableList<ButtonType> bt = getButtonTypes();

        bt.add(type);
        Button btn = (Button) lookupButton(type);

        // it seems counter-intuitive to use an event filter instead of a handler, but
        // when using an event handler, Dialog.close() is called before our own
        // event handler.
        btn.addEventFilter(ActionEvent.ACTION, evt -> {
            if (resultHandler != null && !resultHandler.handleResult(type, get())) {
                LOG.debug("Button {}: result conversion failed", bt);
                evt.consume();
            }
            action.accept(this);
        });

        if (enabled != null) {
            btn.disableProperty().bind(Bindings.not(enabled));
        }
    }

    @Override
    protected Node createButton(ButtonType buttonType) {
        // a wizard dialog should only close when finish or cancel is clicked
        if (LangUtil.isOneOf(buttonType, ButtonType.OK, ButtonType.FINISH, ButtonType.CANCEL)) {
            return super.createButton(buttonType);
        }

        final Button button = new Button(buttonType.getText());
        final ButtonBar.ButtonData buttonData = buttonType.getButtonData();
        ButtonBar.setButtonData(button, buttonData);
        button.setDefaultButton(buttonData.isDefaultButton());
        button.setCancelButton(buttonData.isCancelButton());

        return button;
    }
}