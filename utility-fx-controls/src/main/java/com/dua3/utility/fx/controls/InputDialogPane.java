package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
        // nothing to do
    }

    /**
     * Represents a property that indicates whether the current state of the input dialog pane is valid.
     * This property is used to enable or disable functionality (such as buttons) depending on the
     * validation state of the input. It is initialized to {@code false} by default and can be updated
     * based on the implementation's specific validation logic.
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
    ) {}

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
     *
     * <p>Concrete implementations must define the behavior for initializing
     * input fields, validation logic, and any other setup needed for the dialog pane.
     */
    public abstract void init();

    /**
     * Get valid state property.
     *
     * @return the valid state property of the input
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
            DialogPaneBuilder.@Nullable ResultHandler<@Nullable R> resultHandler,
            Consumer<InputDialogPane<R>> action,
            @Nullable BooleanExpression enabled
    ) {
        ObservableList<ButtonType> bt = getButtonTypes();

        bt.add(type);
        Button btn = (Button) lookupButton(type);

        // it seems counter-intuitive to use an event filter instead of a handler, but
        // when using an event handler, Dialog.close() is called before our own
        // event handler.
        btn.addEventFilter(ActionEvent.ACTION, evt -> {
            if (resultHandler != null) {
                boolean done = resultHandler.handleResult(type, get());
                if (!done) {
                    LOG.debug("Button {}: result conversion failed", bt);
                    evt.consume();
                }
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