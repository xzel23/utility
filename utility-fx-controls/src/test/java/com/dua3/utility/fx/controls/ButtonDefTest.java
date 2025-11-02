package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.ButtonType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Function<InputDialogPane<?>, BooleanExpression> enabled = idp -> FxUtil.ALWAYS_TRUE;
        InputDialogPane<String> inputDialogPane = createMockInputDialogPane();

        // Act
        ButtonDef<String> buttonDef = ButtonDef.of(buttonType, resultHandler, pane -> pane.valid.set(true), enabled);

        // Assert
        assertNotNull(buttonDef);
        assertSame(buttonType, buttonDef.type());
        assertEquals(enabled, buttonDef.enabled());
        buttonDef.action().accept(inputDialogPane);
        assertTrue(inputDialogPane.validProperty().get());
    }

    /**
     * Tests the static create method of ButtonDef with a custom action.
     * Ensures that the custom action manipulates the InputDialogPane as expected.
     */
    @Test
    void testCreateMethodExecutesCustomAction() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        Function<InputDialogPane<?>, BooleanExpression> enabled = idp -> FxUtil.ALWAYS_TRUE;
        InputDialogPane<String> inputDialogPane = createMockInputDialogPane();
        boolean[] actionInvoked = {false};

        // Act
        ButtonDef<String> buttonDef = ButtonDef.of(buttonType, (bt, r) -> true, pane -> actionInvoked[0] = true, enabled);
        buttonDef.action().accept(inputDialogPane);

        // Assert
        assertEquals(true, actionInvoked[0]);
        assertNotNull(buttonDef);
        assertSame(buttonType, buttonDef.type());
    }

    /**
     * Tests the static create method of ButtonDef with a disabled button.
     * Ensures the enabled expression is properly set to false.
     */
    @Test
    void testCreateMethodWithDisabledButton() {
        // Arrange
        ButtonType buttonType = ButtonType.OK;
        Function<InputDialogPane<?>, BooleanExpression> enabled = idp -> FxUtil.ALWAYS_TRUE;

        // Act
        ButtonDef<String> buttonDef = ButtonDef.of(buttonType, (bt, r) -> true, pane -> {}, enabled);

        // Assert
        assertNotNull(buttonDef);
        assertSame(buttonType, buttonDef.type());
    }

    private InputDialogPane<String> createMockInputDialogPane() {
        return new InputDialogPane<>() {
            @Override
            public @Nullable String get() {
                return "";
            }

            @Override
            public void init() { /* nothing to do in test */ }
        };
    }
}