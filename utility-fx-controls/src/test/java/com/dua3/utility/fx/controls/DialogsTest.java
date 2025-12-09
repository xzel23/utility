package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link Dialogs} utility class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class DialogsTest extends FxTestBase {

    /**
     * Test the warning method.
     */
    @Test
    void testWarning() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a warning alert builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.WARNING, MessageFormatter.standard());

            // Verify builder was created
            assertNotNull(builder);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with correct type
            assertNotNull(alert);
            assertEquals(AlertType.WARNING, alert.getAlertType());
        });
    }

    /**
     * Test the warningPane method.
     */
    @Test
    void testWarningPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a warning alert pane builder
            AlertPaneBuilder builder = Dialogs.alertPane(AlertType.WARNING);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the error method.
     */
    @Test
    void testError() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an error alert builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.ERROR);

            // Verify builder was created
            assertNotNull(builder);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with correct type
            assertNotNull(alert);
            assertEquals(AlertType.ERROR, alert.getAlertType());
        });
    }

    /**
     * Test the errorPane method.
     */
    @Test
    void testErrorPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an error alert pane builder
            AlertPaneBuilder builder = Dialogs.alertPane(AlertType.ERROR);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the information method.
     */
    @Test
    void testInformation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an information alert builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.INFORMATION);

            // Verify builder was created
            assertNotNull(builder);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with correct type
            assertNotNull(alert);
            assertEquals(AlertType.INFORMATION, alert.getAlertType());
        });
    }

    /**
     * Test the informationPane method.
     */
    @Test
    void testInformationPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an information alert pane builder
            AlertPaneBuilder builder = Dialogs.alertPane(AlertType.INFORMATION);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the confirmation method.
     */
    @Test
    void testConfirmation() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a confirmation alert builder
            AlertBuilder builder = Dialogs.alert(null, AlertType.CONFIRMATION);

            // Verify builder was created
            assertNotNull(builder);

            // Build an alert
            Alert alert = builder.build();

            // Verify alert was created with correct type
            assertNotNull(alert);
            assertEquals(AlertType.CONFIRMATION, alert.getAlertType());
        });
    }

    /**
     * Test the confirmationPane method.
     */
    @Test
    void testConfirmationPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a confirmation alert pane builder
            AlertPaneBuilder builder = Dialogs.alertPane(AlertType.CONFIRMATION);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the chooseFile method.
     */
    @Test
    void testChooseFile() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a file chooser builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the chooseDirectory method.
     */
    @Test
    void testChooseDirectory() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a directory chooser builder
            DirectoryChooserBuilder builder = Dialogs.chooseDirectory(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the about method.
     */
    @Test
    void testAbout() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an about dialog builder
            AboutDialogBuilder builder = Dialogs.about(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the prompt method.
     */
    @Test
    void testPrompt() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a prompt dialog builder
            PromptBuilder builder = Dialogs.prompt(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the promptPane method.
     */
    @Test
    void testPromptPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a prompt pane builder
            PromptPaneBuilder builder = Dialogs.promptPane(MessageFormatter.standard());

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the input method.
     */
    @Test
    void testInput() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an input dialog builder
            InputDialogBuilder builder = Dialogs.input(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the inputPane method.
     */
    @Test
    void testInputPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an input pane builder
            InputDialogPaneBuilder builder = Dialogs.inputDialogPane(MessageFormatter.standard());

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the inputGrid method.
     */
    @Test
    void testInputGrid() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an input grid builder
            GridBuilder builder = Dialogs.inputGrid(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the options method.
     */
    @Test
    void testOptions() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create an options dialog builder
            OptionsDialogBuilder builder = Dialogs.options(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test the wizard method.
     */
    @Test
    void testWizard() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a wizard dialog builder
            WizardDialogBuilder builder = Dialogs.wizard(null);

            // Verify builder was created
            assertNotNull(builder);
        });
    }
}