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
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

/**
 * Utility class for creating various types of dialogs and panes.
 */
public final class Dialogs {

    // utility - no instances
    private Dialogs() {}

    /**
     * Start definition of new Warning dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder warning(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.WARNING, parentWindow);
    }

    /**
     * Start definition of new Warning pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder warningPane() {
        return new AlertPaneBuilder(AlertType.WARNING);
    }

    /**
     * Start definition of new Error dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder error(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.ERROR, parentWindow);
    }

    /**
     * Start definition of new Error pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder errorPane() {
        return new AlertPaneBuilder(AlertType.ERROR);
    }

    /**
     * Start definition of new Information dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder information(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.INFORMATION, parentWindow);
    }

    /**
     * Start definition of new Information pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder informationPane() {
        return new AlertPaneBuilder(AlertType.INFORMATION);
    }

    /**
     * Start definition of new Confirmation dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder confirmation(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.CONFIRMATION, parentWindow);
    }

    /**
     * Start definition of new Confirmation pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder confirmationPane() {
        return new AlertPaneBuilder(AlertType.CONFIRMATION);
    }

    /**
     * Start definition of new FileChooser.
     *
     * @return new {@link FileChooserBuilder} instance
     */
    public static FileChooserBuilder chooseFile() {
        return new FileChooserBuilder();
    }

    /**
     * Start definition of new DirectoryChooser.
     *
     * @return new {@link DirectoryChooserBuilder} instance
     */
    public static DirectoryChooserBuilder chooseDirectory() {
        return new DirectoryChooserBuilder();
    }

    /**
     * Start definition of new AboutDialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AboutDialogBuilder} instance
     */
    public static AboutDialogBuilder about(@Nullable Window parentWindow) {
        return new AboutDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new prompt dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link PromptBuilder} instance
     */
    public static PromptBuilder prompt(@Nullable Window parentWindow) {
        return new PromptBuilder(parentWindow);
    }

    /**
     * Start definition of new prompt pane.
     *
     * @return new {@link PromptBuilder} instance
     */
    public static PromptPaneBuilder promptPane() {
        return new PromptPaneBuilder();
    }

    /**
     * Start definition of a new input dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link InputDialogBuilder} instance
     */
    public static InputDialogBuilder input(@Nullable Window parentWindow) {
        return new InputDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new input pane.
     *
     * @return new {@link InputPaneBuilder} instance
     */
    public static InputPaneBuilder inputPane() {
        return new InputPaneBuilder();
    }

    /**
     * Start definition of new input dialog.
     *
     * @return new {@link InputDialogBuilder} instance
     */
    public static InputGridBuilder inputGrid() {
        return new InputGridBuilder();
    }

    /**
     * Start the definition of a new {@link OptionsDialog}.
     *
     * @param parentWindow the parent window
     * @return new {@link OptionsDialogBuilder} instance
     */
    public static OptionsDialogBuilder options(@Nullable Window parentWindow) {
        return new OptionsDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new wizard dialog.
     *
     * @return new {@link WizardDialogBuilder} instance
     */
    public static WizardDialogBuilder wizard() {
        return new WizardDialogBuilder();
    }
}
