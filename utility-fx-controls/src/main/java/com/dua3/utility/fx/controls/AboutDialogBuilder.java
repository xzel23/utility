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

import org.jspecify.annotations.Nullable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;


/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class AboutDialogBuilder {
    private static final Logger LOG = LogManager.getLogger(AboutDialogBuilder.class);

    private String title = "";
    private String name = "";
    private String copyright = "";
    private String version = "";
    private String mailText = "";
    private String mailAddress = "";

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
     * Sets the name to be used in the AboutDialog.
     *
     * @param name the name to set
     * @return the current instance of AboutDialogBuilder
     */
    public AboutDialogBuilder name(String name) {
        this.name = name;
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
    public AboutDialog build() {
        AboutDialog dlg = new AboutDialog(css);

        if (parentWindow != null) {
            Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
            stage.getIcons().addAll(((Stage) parentWindow).getIcons());
        }
        if (graphic != null) {
            dlg.setGraphic(graphic);
        }
        if (!title.isBlank()) {
            dlg.setTitle(title);
        }
        if (!name.isBlank()) {
            dlg.setName(name);
        }
        if (!copyright.isBlank()) {
            dlg.setCopyright(copyright);
        }
        if (!version.isBlank()) {
            dlg.setVersion(version);
        }
        if (!mailText.isBlank()) {
            dlg.setEmailText(mailText);
        }
        if (mailAddress != null) {
            dlg.setEmailAddress(mailAddress);
        }
        if (expandableContent != null) {
            dlg.getDialogPane().setExpandableContent(expandableContent);
        }

        return dlg;
    }
}
