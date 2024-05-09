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

    private final Window parentWindow;
    private URL css;
    private Node graphic;
    private Node expandableContent;

    AboutDialogBuilder(@Nullable Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    public AboutDialogBuilder title(String value) {
        this.title = value;
        return this;
    }

    public AboutDialogBuilder name(String value) {
        this.name = value;
        return this;
    }

    public AboutDialogBuilder version(String value) {
        this.version = value;
        return this;
    }

    public AboutDialogBuilder copyright(String value) {
        this.copyright = value;
        return this;
    }

    public AboutDialogBuilder mail(String address) {
        this.mailText = address;
        this.mailAddress = "mailto:" + address;
        return this;
    }

    public AboutDialogBuilder mail(String text, String mailtoUri) {
        this.mailText = text;
        this.mailAddress = mailtoUri;
        return this;
    }

    /**
     * Set supplemental CSS.
     *
     * @param css the name of the CSS resource to load ({@link URL#toExternalForm()}
     * @return this
     */
    public AboutDialogBuilder css(URL css) {
        this.css = css;
        return this;
    }

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

    public AboutDialogBuilder graphic(Node graphic) {
        this.graphic = graphic;
        return this;
    }

    public AboutDialogBuilder expandableContent(Node c) {
        this.expandableContent = c;
        return this;
    }

    public AboutDialogBuilder expandableContent(@Nullable String text) {
        if (text == null || text.isBlank()) {
            expandableContent = null;
            return this;
        }

        this.expandableContent = new StackPane(new Text(text));
        return this;
    }

    public void showAndWait() {
        build().showAndWait();
    }

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
