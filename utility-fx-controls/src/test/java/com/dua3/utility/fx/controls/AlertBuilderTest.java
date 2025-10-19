package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link AlertBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class AlertBuilderTest extends FxTestBase {

    /**
     * Test the constructor and initial state of the AlertBuilder.
     */
    @Test
    void testConstructor() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder for each alert type
            AlertBuilder infoBuilder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());
            AlertBuilder warningBuilder = Dialogs.alert(null, AlertType.WARNING, MessageFormatter.standard());
            AlertBuilder errorBuilder = Dialogs.alert(null, AlertType.ERROR, MessageFormatter.standard());
            AlertBuilder confirmBuilder = Dialogs.alert(null, AlertType.CONFIRMATION, MessageFormatter.standard());

            // Verify initial state
            assertNotNull(infoBuilder);
            assertNotNull(warningBuilder);
            assertNotNull(errorBuilder);
            assertNotNull(confirmBuilder);
        });
    }

    /**
     * Test building an alert dialog.
     */
    @Test
    void testBuildAlert() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created
            assertNotNull(alert);
            assertNotNull(alert.getDialogPane());
            assertEquals(AlertType.INFORMATION, alert.getAlertType());
        });
    }

    /**
     * Test the text method.
     */
    @Test
    void testText() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Set text
            String text = "Test alert message";
            builder.text(text);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with text
            assertNotNull(alert);
            assertEquals(text, alert.getContentText());
        });
    }

    /**
     * Test the text method with formatting.
     */
    @Test
    void testTextWithFormatting() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Set text with formatting
            builder.text("Hello, %s! You have %d new messages.", "User", 5);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with formatted text
            assertNotNull(alert);
            assertEquals("Hello, User! You have 5 new messages.", alert.getContentText());
        });
    }

    /**
     * Test the buttons method.
     */
    @Test
    void testSetButtons() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.CONFIRMATION, MessageFormatter.standard());

            // Set custom buttons
            builder.buttons(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with custom buttons
            assertNotNull(alert);
            assertEquals(3, alert.getButtonTypes().size());
            assertTrue(alert.getButtonTypes().contains(ButtonType.YES));
            assertTrue(alert.getButtonTypes().contains(ButtonType.NO));
            assertTrue(alert.getButtonTypes().contains(ButtonType.CANCEL));
        });
    }

    /**
     * Test the defaultButton method.
     */
    @Test
    void testDefaultButton() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.CONFIRMATION, MessageFormatter.standard());

            // Set custom buttons and default button
            builder.buttons(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            builder.defaultButton(ButtonType.NO);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with default button
            assertNotNull(alert);
            DialogPane pane = alert.getDialogPane();
            Button yesButton = (Button) pane.lookupButton(ButtonType.YES);
            Button noButton = (Button) pane.lookupButton(ButtonType.NO);
            Button cancelButton = (Button) pane.lookupButton(ButtonType.CANCEL);

            assertNotNull(yesButton);
            assertNotNull(noButton);
            assertNotNull(cancelButton);

            assertTrue(noButton.isDefaultButton());
        });
    }

    /**
     * Test the title method.
     */
    @Test
    void testTitle() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Set title
            String title = "Test Alert Title";
            builder.title(title);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with title
            assertNotNull(alert);
            assertEquals(title, alert.getTitle());
        });
    }

    /**
     * Test the header method.
     */
    @Test
    void testHeader() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Set header text
            String headerText = "Test Header Text";
            builder.header(headerText);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with header text
            assertNotNull(alert);
            assertEquals(headerText, alert.getHeaderText());
        });
    }

    /**
     * Test the css method.
     */
    @Test
    void testCss() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION, MessageFormatter.standard());

            // Set CSS
            String css = "test-style.css";
            builder.css(css);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with CSS
            assertNotNull(alert);
            assertTrue(alert.getDialogPane().getScene().getStylesheets().contains(css));
        });
    }
}