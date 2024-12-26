package com.dua3.utility.fx.controls;

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

    protected final BooleanProperty valid = new SimpleBooleanProperty(false);

    protected record ButtonDef<R>(
            ButtonType type,
            AbstractDialogPaneBuilder.ResultHandler<R> resultHandler,
            Consumer<InputDialogPane<R>> action,
            BooleanExpression enabled
    ) {}

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

    void addButton(
            ButtonType type,
            AbstractDialogPaneBuilder.@Nullable ResultHandler<R> resultHandler,
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