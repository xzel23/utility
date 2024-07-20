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

    public AboutDialog() {
        this(null);
    }

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

    public void mail() {
        if (mailAddress.isBlank()) {
            LOG.warn("email not configured");
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            LOG.warn("Desktop API is not supported");
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
