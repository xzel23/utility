package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.InputDialogPane.ButtonDef;
import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ButtonDefTest extends FxTestBase {

    /**
     * Tests the static create method of ButtonDef.
     * Verifies that a ButtonDef instance is created successfully with provided parameters.
     */
    @Test
    void testCreateMethodSuccessfullyCreatesButtonDef() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        DialogPaneBuilder.ResultHandler<String> resultHandler = (bt, r) -> bt == ButtonType.OK && "success".equals(r);
        BooleanExpression enabled = new SimpleBooleanProperty(true);
        InputDialogPane<String> inputDialogPane = createMockInputDialogPane();

        // Act
        ButtonDef<String> buttonDef = ButtonDef.create(buttonType, resultHandler, pane -> pane.valid.set(true), enabled);

        // Assert
        assertNotNull(buttonDef);
        assertEquals(buttonType, buttonDef.type());
        assertEquals(enabled, buttonDef.enabled());
        buttonDef.action().accept(inputDialogPane);
        assertEquals(true, inputDialogPane.validProperty().get());
    }

    /**
     * Tests the static create method of ButtonDef with null resultHandler.
     * Verifies instance creation still works with null resultHandler.
     */
    @Test
    void testCreateMethodAllowsNullResultHandler() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        BooleanExpression enabled = new SimpleBooleanProperty(false);

        // Act
        ButtonDef<String> buttonDef = ButtonDef.create(buttonType, (bt, r) -> true, pane -> {}, enabled);

        // Assert
        assertNotNull(buttonDef);
        assertEquals(buttonType, buttonDef.type());
        assertEquals(enabled, buttonDef.enabled());
    }

    /**
     * Tests the static create method of ButtonDef with a custom action.
     * Ensures that the custom action manipulates the InputDialogPane as expected.
     */
    @Test
    void testCreateMethodExecutesCustomAction() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        BooleanExpression enabled = new SimpleBooleanProperty(true);
        InputDialogPane<String> inputDialogPane = createMockInputDialogPane();
        boolean[] actionInvoked = {false};

        // Act
        ButtonDef<String> buttonDef = ButtonDef.create(buttonType, (bt, r) -> true, pane -> actionInvoked[0] = true, enabled);
        buttonDef.action().accept(inputDialogPane);

        // Assert
        assertEquals(true, actionInvoked[0]);
        assertNotNull(buttonDef);
        assertEquals(buttonType, buttonDef.type());
    }

    /**
     * Tests the static create method of ButtonDef with a disabled button.
     * Ensures the enabled expression is properly set to false.
     */
    @Test
    void testCreateMethodWithDisabledButton() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        BooleanExpression enabled = new SimpleBooleanProperty(false);

        // Act
        ButtonDef<String> buttonDef = ButtonDef.create(buttonType, (bt, r) -> true, pane -> {}, enabled);

        // Assert
        assertNotNull(buttonDef);
        assertEquals(buttonType, buttonDef.type());
        assertEquals(enabled, buttonDef.enabled());
    }

    private InputDialogPane<String> createMockInputDialogPane() {
        return new InputDialogPane<String>() {
            @Override
            public @Nullable String get() {
                return "";
            }

            @Override
            public void init() {}
        };
    }
}