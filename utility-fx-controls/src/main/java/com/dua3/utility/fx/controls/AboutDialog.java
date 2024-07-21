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

import com.dua3.cabe.annotations.Nullable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * The AboutDialog class represents a dialog box that displays information about the application.
 * It extends the Dialog class and does not return any value (Void).
 * This class includes various labels and buttons to display information such as the application name, version, copyright, and email.
 * The AboutDialog class also provides methods to set the name, version, copyright, and email text.
 * The class also includes a mail() method, which opens the default email application with the specified email address.
 * The mail() method checks if the email address is configured and if the Desktop API is supported before opening the email application.
 * <p>
 * Example Usage:
 * <pre>{@code
 * AboutDialog aboutDialog = new AboutDialog();
 * aboutDialog.setName("MyApp");
 * aboutDialog.setVersion("1.0");
 * aboutDialog.setCopyright("© 2021 My Company");
 * aboutDialog.setEmailAddress("info@company.com");
 * aboutDialog.showAndWait();
 * }</pre>
 *
 * Note: The {@link AboutDialogBuilder} class is responsible for constructing the AboutDialog instance and setting its properties.
 */
public class AboutDialog extends Dialog<Void> {
    /**
     * Logger instance
     */
    private static final Logger LOG = LogManager.getLogger(AboutDialog.class);

    Label lTitle;
    Label lVersion;
    Label lCopyright;
    Hyperlink hlMail;
    Button btnOk;

    /**
     * The email URI, i.e. {@code "mailto:info@domain.com"}.
     */
    private String mailAddress = "";

    /**
     * Constructs a new dialog window that displays information about the application.
     */
    public AboutDialog() {
        this(null);
    }

    /**
     * Constructs a new dialog window that displays information about the application.
     *
     * @param css the URL to the CSS file to be applied to the dialog window
     */
    public AboutDialog(@Nullable URL css) {
        DialogPane dialogPane = new DialogPane();

        VBox vBox = new VBox();
        vBox.setMaxHeight(Double.NEGATIVE_INFINITY);
        vBox.setMaxWidth(Double.POSITIVE_INFINITY);

        lTitle = new Label("name");
        lTitle.setId("title");

        lVersion = new Label("version");
        lVersion.setId("version");

        lCopyright = new Label("copyright");
        lCopyright.setId("copyright");

        hlMail = new Hyperlink("email");
        hlMail.setId("mail");
        hlMail.setOnAction(e -> mail());

        vBox.getChildren().setAll(lTitle, lVersion, lCopyright, hlMail);

        StackPane content = new StackPane(vBox);
        content.setId("content");
        content.setMaxHeight(Double.NEGATIVE_INFINITY);
        content.setMaxWidth(Double.POSITIVE_INFINITY);

        dialogPane.setContent(content);

        URL dialogCss = css != null ? css : AboutDialog.class.getResource("about.css");
        assert dialogCss != null;
        dialogPane.getStylesheets().add(dialogCss.toExternalForm());
        setDialogPane(dialogPane);
        dialogPane.getButtonTypes().addAll(ButtonType.OK);
    }

    /**
     * Sends an email using the default mail application on the user's system.
     * The email address to send to must be provided as a valid email address (mailto URI).
     * The method checks if the email address is configured and if the desktop API is supported before attempting to send the email.
     */
    public void mail() {
        if (!isMailAvailable()) {
            LOG.info("Email not configured or Desktop API not supported.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            try {
                LOG.info("opening mail application");
                desktop.mail(URI.create(mailAddress));
            } catch (IOException | IllegalArgumentException e) {
                LOG.warn("could not open mail application", e);
            }
        }
    }

    /**
     * Checks if the mail is configured and mail functionality is supported.
     *
     * @return true if the mail functionality is supported, false otherwise
     */
    public boolean isMailAvailable() {
        if (mailAddress.isBlank()) {
            LOG.info("email not configured");
            return false;
        }

        if (!Desktop.isDesktopSupported()) {
            LOG.info("Desktop API is not supported");
            return false;
        }
        return true;
    }

    /**
     * Set name.
     *
     * @param value the text to display
     */
    public void setName(String value) {
        lTitle.setText(value);
    }

    /**
     * Set version text.
     *
     * @param value the text to display
     */
    public void setVersion(String value) {
        lVersion.setText(value);
    }

    /**
     * Set copyright text.
     *
     * @param value the text to display
     */
    public void setCopyright(String value) {
        lCopyright.setText(value);
    }

    /**
     * Set the email text.
     *
     * @param value the text to display for the email
     */
    public void setEmailText(String value) {
        hlMail.setText(value);
    }

    /**
     * Set the email URI.
     *
     * @param value the mail URI to set
     */
    public void setEmailAddress(String value) {
        mailAddress = value;
    }
}
