// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls.abstract_builders;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Abstract class for building dialog boxes.
 *
 * @param <D> the type of dialog box to build
 * @param <B> the type of the builder class itself
 * @param <R> the type of the result returned by the dialog box
 */
public abstract class DialogBuilder<D extends @NonNull Dialog<R>, B extends @NonNull DialogBuilder<D, B, R>, R>
        extends DialogPaneBuilder<D, B, R> {

    private final BiConsumer<D, String> titleSetter;
    private final @Nullable Window parentWindow;
    private @Nullable String title;

    protected DialogBuilder(@Nullable Window parentWindow) {
        super(Dialog::setHeaderText);
        this.parentWindow = parentWindow;
        this.titleSetter = Dialog::setTitle;
    }

    /**
     * Set dialog title.
     *
     * @param fmt  the format String as defined by {@link java.util.Formatter}
     * @param args the arguments passed to the formatter
     * @return {@code this}
     */
    @SuppressWarnings("unchecked")
    public B title(String fmt, Object... args) {
        this.title = String.format(fmt, args);
        return (B) this;
    }

    /**
     * Build and show the dialog.
     * <p>
     * This is equivalent to calling build().showAndWait().
     *
     * @return Optional containing the result as defined by the dialog
     */
    public Optional<R> showAndWait() {
        return build().showAndWait();
    }

    /**
     * Create Dialog instance.
     *
     * @return Dialog instance
     */
    @Override
    public D build() {
        D dlg = super.build();

        // copy stage icons from parent
        if (parentWindow != null) {
            Stage stage = (Stage) dlg.getDialogPane().getScene().getWindow();
            stage.getIcons().addAll(((Stage) parentWindow).getIcons());
        }

        // set title
        applyIfNotNull(titleSetter, dlg, title);

        return dlg;
    }
}
