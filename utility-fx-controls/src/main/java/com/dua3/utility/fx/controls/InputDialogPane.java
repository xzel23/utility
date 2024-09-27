package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

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
public abstract class InputDialogPane<R> extends DialogPane implements Supplier<R> {

    protected final BooleanProperty valid = new SimpleBooleanProperty(false);

    protected final List<Pair<ButtonType, Consumer<InputDialogPane<R>>>> buttons = new ArrayList<>();

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
     * Initializes the buttons for the dialog pane based on the list of button types and their corresponding actions.
     * This method clears any existing button types, then iterates over the list of button-action pairs, adding each
     * button type to the observable list and setting their respective actions.
     */
    public void initButtons() {
        ObservableList<ButtonType> bt = getButtonTypes();
        bt.clear();
        for (var b : buttons) {
            bt.add(b.first());
            Button btn = (Button) lookupButton(b.first());
            btn.setOnAction(evt -> b.second().accept(this));
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
