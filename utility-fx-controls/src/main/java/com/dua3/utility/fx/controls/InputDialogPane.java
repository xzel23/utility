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
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
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
     * Checks and returns the validity state of the input in the dialog pane.
     * <p>
     * This method retrieves the current value of the {@code valid} property,
     * which indicates whether the user input meets the required validation criteria.
     *
     * @return {@code true} if the input is valid, otherwise {@code false}.
     */
    public boolean isValid() {
        return valid.get();
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
            Consumer<? super InputDialogPane<?>> action,
            @Nullable Function<InputDialogPane<?>, BooleanExpression> enabled
    ) {
        ObservableList<ButtonType> bt = getButtonTypes();

        bt.add(type);
        Button btn = (Button) lookupButton(type);

        // it seems counter-intuitive to use an event filter instead of a handler, but
        // when using an event handler, Dialog.close() is called before our own
        // event handler.
        btn.addEventFilter(ActionEvent.ACTION, evt -> {
            if (resultHandler != null && !resultHandler.handleResult(type, get())) {
                LOG.warn("Button {}: result conversion failed", bt);
                evt.consume();
            }
            action.accept(this);
        });

        if (enabled != null) {
            btn.disableProperty().bind(Bindings.not(enabled.apply(this)));
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

    /**
     * Closes the dialog window associated with this InputDialogPane.
     *
     * This method retrieves the current window of the dialog pane and
     * triggers its closing operation. Typically used to dismiss the dialog
     * after the necessary action is performed or when cancelling the dialog.
     */
    public void closeDialog() {
        ((Stage) getScene().getWindow()).close();
    }
}
