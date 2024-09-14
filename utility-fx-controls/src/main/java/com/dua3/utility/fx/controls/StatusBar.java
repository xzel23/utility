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

import com.dua3.utility.fx.FxTaskTracker;
import com.dua3.utility.fx.PlatformHelper;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dialog to configure a editor settings.
 */
public class StatusBar extends CustomControl<HBox> implements FxTaskTracker {

    /**
     * Logger instance
     */
    private static final Logger LOG = LogManager.getLogger(StatusBar.class);

    // -- input controls
    Label text;
    ProgressBar progressBar;

    /**
     * Construct new StatusBar instance.
     */
    public StatusBar() {
        super(new HBox());
        container.setAlignment(Pos.CENTER_LEFT);

        getStyleClass().setAll("statusbar");

        text = new Label();
        progressBar = new ProgressBar(0.0);
        HBox.setHgrow(text, Priority.ALWAYS);

        progressBar.setPrefWidth(100);

        Region region = new Region();
        region.setPrefHeight(1);
        region.setPrefWidth(4);

        container.getChildren().setAll(text, region, progressBar);
    }

    public void setText(String s) {
        PlatformHelper.runLater(() -> text.setText(s));
    }

    public void setProgress(double p) {
        PlatformHelper.runLater(() -> progressBar.setProgress(p));
    }

    @Override
    public void updateTaskState(Task<?> task, State state) {
        PlatformHelper.runLater(() -> {
            switch (state) {
                case RUNNING -> {
                    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    progressBar.setVisible(true);
                }
                case SUCCEEDED -> {
                    progressBar.setProgress(1.0);
                    progressBar.setVisible(false);
                }
                case READY, CANCELLED, FAILED -> {
                    progressBar.setProgress(0.0);
                    progressBar.setVisible(false);
                }
                case SCHEDULED -> {
                    progressBar.setProgress(0.0);
                    progressBar.setVisible(true);
                }
                default -> LOG.warn("StatusBar.updateTaskState() - unexpected state: {}", state);
            }
        });
    }

    @Override
    public void updateTaskProgress(Task<?> task, double p) {
        progressBar.setProgress(p);
    }

    @Override
    public void updateTaskTitle(Task<?> task, String s) {
        text.setText(s);
    }

    @Override
    public void updateTaskMessage(Task<?> task, String s) {
        progressBar.setTooltip(new Tooltip(s));
    }
}
