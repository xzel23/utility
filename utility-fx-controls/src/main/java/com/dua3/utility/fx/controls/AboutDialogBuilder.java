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

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;


/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class AboutDialogBuilder {
    private static final Logger LOG = LogManager.getLogger(AboutDialogBuilder.class);

    private String title = "";
    private String applicationName = "";
    private String copyright = "";
    private String version = "";
    private String mailText = "";
    private String mailAddress = "";
    private String licenseText = "";
    private @Nullable Runnable showLicenseDetails;

    private final @Nullable Window parentWindow;
    private @Nullable URL css;
    private @Nullable Node graphic;
    private @Nullable Node expandableContent;

    /**
     * Constructs a new AboutDialogBuilder with the specified parent window.
     *
     * @param parentWindow the window that will be the parent of the dialog, or null if there is no parent window.
     */
    AboutDialogBuilder(@Nullable Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    /**
     * Sets the title for the about dialog.
     *
     * @param title the title to be set for the about dialog
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the license text to be used in the AboutDialog.
     *
     * @param licenseText the license text to set
     * @return the current instance of AboutDialogBuilder
     */
    public AboutDialogBuilder licenseText(String licenseText) {
        this.licenseText = licenseText;
        this.showLicenseDetails = null;
        return this;
    }

    /**
     * Sets the license information for the AboutDialog, including the license text
     * and an optional action to show detailed license information.
     *
     * @param licenseText the license text to set for the dialog
     * @param showLicenseDetails a {@link Runnable} that will be invoked to display additional details
     *                           about the license when requested, or null if no action is needed
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder license(String licenseText, Runnable showLicenseDetails) {
        this.licenseText = licenseText;
        this.showLicenseDetails = showLicenseDetails;
        return this;
    }

    /**
     * Sets the name to be used in the AboutDialog.
     *
     * @param applicationName the application name to set
     * @return the current instance of AboutDialogBuilder
     */
    public AboutDialogBuilder applicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * Sets the version information for the about dialog.
     *
     * @param version the version information to be displayed in the dialog.
     * @return the current instance of AboutDialogBuilder for method chaining.
     */
    public AboutDialogBuilder version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the copyright information for the About dialog.
     *
     * @param text the copyright text to be displayed in the About dialog
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder copyright(String text) {
        this.copyright = text;
        return this;
    }

    /**
     * Sets the email address for the About dialog.
     *
     * @param address the email address to be displayed and used in the mailto link
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(String address) {
        this.mailText = address;
        this.mailAddress = "mailto:" + address;
        return this;
    }

    /**
     * Sets the text and mailto URI for the mail link in the About Dialog.
     *
     * @param text the text to be displayed for the mail link
     * @param mailtoUri the mailto URI to be assigned to the mail link
     * @return the current instance of AboutDialogBuilder for method chaining
     */
    public AboutDialogBuilder mail(String text, String mailtoUri) {
        this.mailText = text;
        this.mailAddress = mailtoUri;
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
     * Sets expandable content for the AboutDialog.
     *
     * @param text the text to set as expandable content; if null or blank, the expandable content is set to null
     * @return the AboutDialogBuilder instance for method chaining
     */
    public AboutDialogBuilder expandableContent(@Nullable String text) {
        if (text == null || text.isBlank()) {
            expandableContent = null;
            return this;
        }

        this.expandableContent = new StackPane(new Text(text));
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
        addLabel(children,"application-name", applicationName);
        addLabel(children,"version", version);
        addLabel(children,"copyright", copyright);

        if (!mailText.isEmpty()) {
            Hyperlink hlMail = new Hyperlink(mailText);
            hlMail.setId("mail");
            hlMail.setOnAction(e -> sendMailTo(mailAddress));
            children.add(hlMail);
        }

        if (!licenseText.isEmpty()) {
            if (showLicenseDetails != null) {
                Hyperlink hlLicense = new Hyperlink(licenseText);
                hlLicense.setText(mailText.isBlank() ? "Email" : mailText);
                hlLicense.setId("license");
                hlLicense.setOnAction(e -> showLicenseDetails.run());
                children.add(hlLicense);
            } else {
                addLabel(children, "license", licenseText);
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


        return dlg;
    }

    private static void addLabel(Collection<Node> nodes, String id, String text) {
        if (!text.isEmpty()) {
            Label label = new Label(text);
            label.setId(id);
            nodes.add(label);
        }
    }

    private static void sendMailTo(String address) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            try {
                LOG.info("opening mail application");
                desktop.mail(URI.create(address));
            } catch (IOException | IllegalArgumentException e) {
                LOG.warn("could not open mail application", e);
            }
        }
    }
}
