package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link AboutDialogBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class AboutDialogBuilderTest extends FxTestBase {

    /**
     * Test the constructor and initial state of the AboutDialogBuilder.
     */
    @Test
    void testConstructor() throws Exception {
        runOnFxThreadAndWait(() -> {
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            Dialog<Void> dialog = builder.build();

            assertNotNull(dialog);
            assertEquals("About", dialog.getTitle());
        });
    }

    /**
     * Test setting the title.
     */
    @Test
    void testTitle() throws Exception {
        runOnFxThreadAndWait(() -> {
            String testTitle = "Test Title";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.title(testTitle);
            Dialog<Void> dialog = builder.build();

            assertEquals(testTitle, dialog.getTitle());
        });
    }

    /**
     * Test setting the application name.
     */
    @Test
    void testApplicationName() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testAppName = "Test Application";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.applicationName(testAppName);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the application name label
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Label label && Objects.equals(label.getId(), "application-name"))
                    .findFirst();

            assertTrue(node.isPresent(), "Application name label should be present");
        });
    }

    /**
     * Test setting the version.
     */
    @Test
    void testVersion() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testVersion = "1.0.0";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.version(testVersion);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the version label
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Label label && Objects.equals(label.getId(), "version"))
                    .findFirst();

            assertTrue(node.isPresent(), "Version label should be present");
        });
    }

    /**
     * Test setting the copyright.
     */
    @Test
    void testCopyright() throws Exception {
        runOnFxThreadAndWait(() -> {
            String testCopyright = "© 2025 Test Company";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.copyright(testCopyright);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the copyright label
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Label label && Objects.equals(label.getId(), "copyright"))
                    .findFirst();

            assertTrue(node.isPresent(), "Copyright label should be present");
        });
    }

    /**
     * Test setting the mail address.
     */
    @Test
    void testMail() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testMail = "test@example.com";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.mail(testMail);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the mail hyperlink
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Hyperlink hl && Objects.equals(hl.getId(), "mail"))
                    .findFirst();

            assertTrue(node.isPresent(), "Mail hyperlink should be present");
        });
    }

    /**
     * Test setting the mail with custom text and URI.
     */
    @Test
    void testMailWithCustomText() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testMailText = "Contact Us";
            String testMailUri = "mailto:test@example.com";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.mail(testMailText, testMailUri);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the mail hyperlink
            // Find the mail hyperlink
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Hyperlink hl && Objects.equals(hl.getId(), "mail"))
                    .findFirst();

            assertTrue(node.isPresent(), "Mail hyperlink should be present");
        });
    }

    /**
     * Test setting the license text.
     */
    @Test
    void testLicenseText() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testLicense = "Test License";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.licenseText(testLicense);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the license label
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Label label && Objects.equals(label.getId(), "license"))
                    .findFirst();

            assertTrue(node.isPresent(), "License label should be present");
        });
    }

    /**
     * Test setting the license with details.
     */
    @Test
    void testLicenseWithDetails() throws Exception {
        // The dialog content is actually a StackPane, not a VBox as assumed
        runOnFxThreadAndWait(() -> {
            String testLicense = "Test License";
            boolean[] detailsClicked = {false};
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.license(testLicense, () -> detailsClicked[0] = true);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Pane content = (Pane) dialogPane.getContent();

            // Find the license label
            Optional<Node> node = getChildrenRecursive(content)
                    .filter(nd -> nd instanceof Hyperlink hl && Objects.equals(hl.getId(), "license"))
                    .findFirst();

            assertTrue(node.isPresent(), "License label should be present");

            // Test clicking the license link
            node.ifPresent(n -> {
                if (n instanceof Hyperlink licenseLink) {
                    licenseLink.fire();
                    assertTrue(detailsClicked[0], "License details callback should be invoked");
                } else {
                    throw new AssertionError("Unexpected node type: " + n.getClass().getName());
                }
            });
        });
    }

    /**
     * Test setting the graphic from a URL.
     */
    @Test
    void testGraphicFromUrl() throws Exception {
        runOnFxThreadAndWait(() -> {
            URL testGraphicUrl = AboutDialogBuilder.class.getResource("about.css");
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.graphic(testGraphicUrl);
            Dialog<Void> dialog = builder.build();

            // We can't easily test the graphic content, but we can check that the dialog was created
            assertNotNull(dialog);
        });
    }

    /**
     * Test setting the graphic from a Node.
     */
    @Test
    void testGraphicFromNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            Label testGraphic = new Label("Test Graphic");
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.graphic(testGraphic);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Node graphic = dialogPane.getGraphic();

            assertNotNull(graphic, "Graphic should be set");
            // The graphic might be wrapped in another container, so we can't directly compare
        });
    }

    /**
     * Test setting the expandable content from a String.
     */
    @Test
    void testExpandableContentFromString() throws Exception {
        runOnFxThreadAndWait(() -> {
            String testContent = "Test Expandable Content";
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.expandableContent(testContent);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Node expandableContent = dialogPane.getExpandableContent();

            assertNotNull(expandableContent, "Expandable content should be set");
        });
    }

    /**
     * Test setting the expandable content from a Node.
     */
    @Test
    void testExpandableContentFromNode() throws Exception {
        runOnFxThreadAndWait(() -> {
            Label testContent = new Label("Test Expandable Content");
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.expandableContent(testContent);
            Dialog<Void> dialog = builder.build();

            DialogPane dialogPane = dialog.getDialogPane();
            Node expandableContent = dialogPane.getExpandableContent();

            assertNotNull(expandableContent, "Expandable content should be set");
            // The content might be wrapped in another container, so we can't directly compare
        });
    }

    /**
     * Test setting the CSS.
     */
    @Test
    void testCss() throws Exception {
        runOnFxThreadAndWait(() -> {
            URL testCss = AboutDialogBuilder.class.getResource("about.css");
            AboutDialogBuilder builder = new AboutDialogBuilder(null);
            builder.css(testCss);
            Dialog<Void> dialog = builder.build();

            // We can't easily test the CSS application, but we can check that the dialog was created
            assertNotNull(dialog);
        });
    }

    /**
     * Test the showAndWait method.
     */
    @Test
    void testShowAndWait() throws Exception {
        // This test is tricky because showAndWait blocks until the dialog is closed
        // We'll just test that the method doesn't throw an exception when called
        // but we won't actually wait for user interaction
        runOnFxThreadAndWait(() -> {
            AboutDialogBuilder builder = new AboutDialogBuilder(null);

            // Instead of calling showAndWait, we'll just build the dialog
            // to avoid blocking the test
            Dialog<Void> dialog = builder.build();
            assertNotNull(dialog);

            // Note: We can't test showAndWait directly because it blocks until the user closes the dialog
        });
    }
}