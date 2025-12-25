// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import com.dua3.utility.application.LicenseData;
import com.dua3.utility.text.MessageFormatter;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

/**
 * Builder for an application "About" dialog.
 * <p>
 * Provides a fluent API to compose a dialog showing application metadata
 * such as title, version, copyright, contact, and license details.
 */
public class AboutDialogBuilder {
    private static final Logger LOG = LogManager.getLogger(AboutDialogBuilder.class);

    private final @Nullable Window parentWindow;
    private final MessageFormatter messageFormatter;

    private String title = "";
    private String applicationName = "";
    private String copyright = "";
    private String version = "";
    private String mailText = "";
    private @Nullable URI mailTo = null;
    private String licenseNote = "";
    private @Nullable Runnable showLicenseDetails;

    private @Nullable URL css;
    private @Nullable Node graphic;
    private @Nullable Node expandableContent;
    private @Nullable Modality modality;

    /**
     * Constructs a new AboutDialogBuilder with the specified parent window.
     *
     * @param parentWindow the window that will be the parent of the dialog, or null if there is no parent window.
     */
    AboutDialogBuilder(@Nullable Window parentWindow, MessageFormatter messageFormatter) {
        this.parentWindow = parentWindow;
        this.messageFormatter = messageFormatter;
    }

    private String format(MessageFormatter.MessageFormatterArgs mfargs) {
        return format(mfargs.fmt(), mfargs.args());
    }

    private String format(String fmt, @Nullable Object... args) {
        return messageFormatter.format(fmt, args);
    }

    /**
     * Sets the title for the AboutDialog.
     *
     * @param fmt the format string for the title
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder title(String fmt, Object... args) {
        this.title = format(fmt, args);
        return this;
    }

    /**
     * Configures the license details for the AboutDialog. It sets the license
     * information including the licensee and validity, and optionally sets the action
     * to display detailed license information when requested.
     *
     * @param licenseData the license data object that contains information
     *                    about the licensee, validity period, and license text
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder license(LicenseData licenseData) {
        licenseNote("License-ID {} licensed to {} valid until {}.", licenseData.licenseId(), licenseData.licensee(), licenseData.validUntil());
        licenseData.licenseText().ifPresent(licenseText ->
                onShowLicenseDetails(() ->
                        Dialogs.alert(parentWindow, Alert.AlertType.INFORMATION)
                                .title("License Details")
                                .header("License valid until " + licenseData.validUntil())
                                .text(licenseText.toString())
                                .build()
                                .show()
                )
        );
        return this;
    }

    /**
     * Configures the license text and action to show license details for the AboutDialog.
     *
     * @param fmt the format string for the license text
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of {@code AboutDialogBuilder} for method chaining
     */
    public AboutDialogBuilder licenseNote(String fmt, Object... args) {
        this.licenseNote = format(fmt, args);
        return this;
    }

    /**
     * Sets the action to show license details for the AboutDialog.
     *
     * @param showLicenseDetails a {@code Runnable} that defines the action to
     *                           be executed for displaying the license details
     * @return the current instance of {@code AboutDialogBuilder} for method chaining
     */
    public AboutDialogBuilder onShowLicenseDetails(Runnable showLicenseDetails) {
        this.showLicenseDetails = showLicenseDetails;
        return this;
    }

    /**
     * Sets the application name for the AboutDialog.
     *
     * @param fmt the format string for the application name
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder applicationName(String fmt, Object... args) {
        this.applicationName = format(fmt, args);
        return this;
    }

    /**
     * Sets the application name by formatting the provided arguments.
     *
     * @param args the arguments to be formatted into the application name
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder applicationName(MessageFormatter.MessageFormatterArgs args) {
        this.applicationName = format(args);
        return this;
    }

    /**
     * Sets the version information for the AboutDialog.
     *
     * @param fmt  the format string for the version information
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder version(String fmt, Object... args) {
        this.version = format(fmt, args);
        return this;
    }

    /**
     * Sets the version information for the AboutDialog.
     *
     * @param args the arguments to be formatted into the version string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder version(MessageFormatter.MessageFormatterArgs args) {
        this.version = format(args);
        return this;
    }

    /**
     * Sets the copyright information for the AboutDialog.
     *
     * @param fmt  the format string for the copyright text
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder copyright(String fmt, Object... args) {
        this.copyright = format(fmt, args);
        return this;
    }

    /**
     * Sets the copyright information for the AboutDialog.
     *
     * @param args the arguments to be formatted into the copyright text
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder copyright(MessageFormatter.MessageFormatterArgs args) {
        this.copyright = format(args);
        return this;
    }

    /**
     * Sets the email address for the AboutDialog by creating a mailto link.
     *
     * @param address the email address to be used in the mailto link
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(String address) {
        URI mailtoUri = URI.create("mailto:" + address);
        this.mailText = address;
        this.mailTo = mailtoUri;
        return this;
    }

    /**
     * Sets the email address for the About dialog.
     *
     * @param address the email address to be displayed and used in the mailto link
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(URI address) {
        if (!address.getScheme().equalsIgnoreCase("mailto")) {
            LOG.warn("invalid mailto URI, ignnoring: {}", address);
        }
        this.mailText = address.getSchemeSpecificPart();
        this.mailTo = address;
        return this;
    }

    /**
     * Sets the email address and corresponding email content for the AboutDialog.
     * The email content is generated using the specified format string and arguments.
     *
     * @param mailtoUri the email address to be displayed and used in the mailto link
     * @param fmt the format string for the email content
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(URI mailtoUri, String fmt, Object... args) {
        this.mailText = format(fmt, args);
        this.mailTo = mailtoUri;
        return this;
    }

    /**
     * Sets the email address and corresponding email content for the AboutDialog.
     * The email content is generated using the specified format string and arguments.
     *
     * @param mailtoUri the email address to be displayed and used in the mailto link
     * @param args the arguments to be formatted into mail display text
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(URI mailtoUri, MessageFormatter.MessageFormatterArgs args) {
        this.mailText = format(args);
        this.mailTo = mailtoUri;
        return this;
    }

    /**
     * Sets the CSS file to be used for styling the dialog.
     *
     * @param css the URL of the CSS file
     * @return the updated instance of AboutDialogBuilder
     */
    public AboutDialogBuilder css(URL css) {
        this.css = css;
        return this;
    }

    /**
     * Sets the graphic for the about dialog using a URL pointing to the image.
     * If the URL is null, the graphic will be set to null.
     * If the URL points to a valid image, the image will be loaded and set as the graphic.
     * If an error occurs while reading the image, a warning will be logged and the graphic will be set to null.
     *
     * @param url the URL pointing to the image to be used as the graphic, can be null
     * @return the AboutDialogBuilder instance with the updated graphic
     */
    public AboutDialogBuilder graphic(@Nullable URL url) {
        if (url == null) {
            this.graphic = null;
            return this;
        }

        try (var in = url.openStream()) {
            Image image = new Image(in);
            graphic(new javafx.scene.image.ImageView(image));
        } catch (IOException e) {
            LOG.warn("could not read image: {}", url, e);
            this.graphic = null;
        }
        return this;
    }

    /**
     * Sets the graphic node for the About dialog.
     *
     * @param graphic the graphic node to set
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder graphic(Node graphic) {
        this.graphic = graphic;
        return this;
    }

    /**
     * Sets the expandable content for the AboutDialog.
     *
     * @param c the node to be displayed as expandable content in the dialog
     * @return the AboutDialogBuilder instance for method chaining
     */
    public AboutDialogBuilder expandableContent(Node c) {
        this.expandableContent = c;
        return this;
    }

    /**
     * Sets the expandable content for the AboutDialog using a formatted string and arguments.
     * If the resulting text is blank, the expandable content will be set to null.
     *
     * @param fmt  the format string for the expandable content
     * @param args the arguments referenced by the format specifiers in the format string
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder expandableContent(String fmt, Object... args) {
        String text = format(fmt, args);
        if (text.isBlank()) {
            expandableContent = null;
            return this;
        }

        this.expandableContent = new StackPane(new Text(text));
        return this;
    }

    /**
     * Set the modality of the dialog.
     *
     * @param modality the modality to set
     * @return the current builder instance, to allow method chaining
     */
    public AboutDialogBuilder modality(Modality modality) {
        this.modality = modality;
        return this;
    }

    /**
     * Displays the dialog and waits for the user to respond before returning.
     *
     * <p>This method constructs an instance of AboutDialog using the current configuration
     * and then invokes its showAndWait method to display it. The dialog will be modal
     * and will block execution until the user dismisses it.
     */
    public void showAndWait() {
        build().showAndWait();
    }

    /**
     * Constructs and configures an instance of AboutDialog based on the properties set in the AboutDialogBuilder.
     *
     * @return a configured AboutDialog instance
     */
    public Dialog<Void> build() {
        VBox vBox = new VBox();
        vBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        vBox.setMaxWidth(Double.POSITIVE_INFINITY);
        vBox.setAlignment(Pos.CENTER);

        ObservableList<Node> children = vBox.getChildren();
        addLabelLiteral(children, "application-name", applicationName);
        addLabelLiteral(children, "version", version);
        addLabelLiteral(children, "copyright", copyright);

        if (!mailText.isEmpty()) {
            Hyperlink hlMail = new Hyperlink(mailText);
            hlMail.setId("mail");
            hlMail.setOnAction(e -> sendMailTo(mailTo));
            children.add(hlMail);
        }

        if (!licenseNote.isEmpty()) {
            if (showLicenseDetails != null) {
                Hyperlink hlLicense = new Hyperlink(licenseNote);
                hlLicense.setText(mailText.isBlank() ? "Email" : mailText);
                hlLicense.setId("license");
                hlLicense.setOnAction(e -> showLicenseDetails.run());
                children.add(hlLicense);
            } else {
                addLabel(children, "license", licenseNote);
            }
        }

        StackPane content = new StackPane(vBox);
        content.setId("content");
        content.setMaxHeight(Double.NEGATIVE_INFINITY);
        content.setMaxWidth(Double.POSITIVE_INFINITY);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(content);

        URL dialogCss = css != null ? css : AboutDialogBuilder.class.getResource("about.css");
        assert dialogCss != null;
        dialogPane.getStylesheets().add(dialogCss.toExternalForm());

        Dialog<Void> dlg = new Dialog<>();
        dlg.setDialogPane(dialogPane);

        if (!title.isBlank()) {
            dlg.setTitle(title);
        } else if (!applicationName.isBlank()) {
            dlg.setTitle("About " + applicationName);
        } else {
            dlg.setTitle("About");
        }
        dialogPane.getButtonTypes().addAll(ButtonType.OK);

        if (parentWindow != null) {
            Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
            stage.getIcons().addAll(((Stage) parentWindow).getIcons());
        }
        if (graphic != null) {
            dlg.setGraphic(graphic);
        }
        if (expandableContent != null) {
            dialogPane.setExpandableContent(expandableContent);
        }

        if (modality != null) {
            dlg.initModality(modality);
        }

        return dlg;
    }

    /**
     * Adds a labeled node to the specified collection if the formatted text is not empty.
     *
     * @param nodes the collection of nodes to which the new label will be added
     * @param id the identifier to assign to the label
     * @param fmt the format string to generate the label's text
     * @param args the arguments referenced by the format specifiers in the format string
     */
    private void addLabel(Collection<Node> nodes, String id, String fmt, Object... args) {
        String text = format(fmt, args);
        if (!text.isEmpty()) {
            Label label = new Label(text);
            label.setId(id);
            nodes.add(label);
        }
    }

    /**
     * Adds a labeled node to the specified collection if the text is not empty.
     *
     * @param nodes the collection of nodes to which the new label will be added
     * @param id the identifier to assign to the label
     * @param text the label text
     */
    private void addLabelLiteral(Collection<Node> nodes, String id, String text) {
        if (!text.isEmpty()) {
            Label label = new Label(text);
            label.setId(id);
            nodes.add(label);
        }
    }

    /**
     * Opens the default mail application with the specified email address.
     * If the desktop environment does not support the MAIL action, the method does nothing.
     * Logs informational messages and warnings as appropriate when attempting to open the mail application.
     *
     * @param address the email address to open in the default mail application. This must be properly formatted as a URI.
     */
    private static void sendMailTo(URI address) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            try {
                LOG.debug("opening mail application");
                desktop.mail(address);
            } catch (IOException | IllegalArgumentException e) {
                LOG.warn("could not open mail application", e);
            }
        }
    }
}
