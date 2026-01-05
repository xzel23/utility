package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import com.dua3.utility.fx.controls.abstract_builders.PaneBuilder;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A builder class for constructing a {@link WizardDialog} instance.
 * This builder helps in setting up the title and configuring the pages
 * of the wizard dialog.
 */
public class WizardDialogBuilder {

    final LinkedHashMap<String, WizardDialog.Page<?, ?>> pages = new LinkedHashMap<>();
    private final @Nullable Window parentWindow;
    private final MessageFormatter formatter;
    private String title = "";
    private String startPage = "";
    private boolean cancelable = true;
    private double prefWidth = -1;
    private double prefHeight = -1;
    private double minWidth = -1;
    private double minHeight = -1;
    private double maxWidth = Integer.MAX_VALUE;
    private double maxHeight = Integer.MAX_VALUE;
    private @Nullable Modality modality;

    /**
     * Constructs a new instance of {@code WizardDialogBuilder} with the specified parent window
     * and message formatter.
     *
     * @param parentWindow the parent window that owns the wizard dialog, or {@code null} if
     *                     the dialog does not have a parent window
     * @param formatter    the message formatter used for formatting dialog messages
     */
    WizardDialogBuilder(@Nullable Window parentWindow, MessageFormatter formatter) {
        this.parentWindow = parentWindow;
        this.formatter = formatter;
    }

    /**
     * Sets the title for the wizard dialog being built.
     *
     * @param fmt  the title format pattern.
     * @param args formatting arguments
     * @return The current instance of {@code WizardDialogBuilder}, for method chaining.
     */
    public WizardDialogBuilder title(String fmt, Object... args) {
        this.title = formatter.format(fmt, args);
        return this;
    }

    /**
     * Sets whether the wizard dialog being built should be cancelable.
     *
     * @param flag a boolean value where {@code true} indicates that the wizard
     *             dialog can be canceled, and {@code false} indicates it cannot.
     * @return The current instance of {@code WizardDialogBuilder}, allowing method chaining.
     */
    public WizardDialogBuilder cancelable(boolean flag) {
        this.cancelable = flag;
        return this;
    }

    /**
     * Sets the preferred width of the wizard dialog being built.
     *
     * @param value the preferred width value to set, in pixels
     * @return the current instance of {@code WizardDialogBuilder}, allowing method chaining
     */
    public WizardDialogBuilder prefWidth(double value) {
        this.prefWidth = value;
        return this;
    }

    /**
     * Sets the preferred height for the wizard dialog being built.
     *
     * @param value the preferred height value in pixels
     * @return The current instance of {@code WizardDialogBuilder}, allowing method chaining
     */
    public WizardDialogBuilder prefHeight(double value) {
        this.prefHeight = value;
        return this;
    }

    /**
     * Sets the minimum width for the wizard dialog being built.
     *
     * @param value the minimum width to be applied to the wizard dialog
     * @return The current instance of {@code WizardDialogBuilder}, allowing method chaining
     */
    public WizardDialogBuilder minWidth(double value) {
        this.minWidth = value;
        return this;
    }

    /**
     * Sets the minimum height for the wizard dialog being built.
     *
     * @param value the minimum height of the dialog, specified as a double
     * @return the current instance of {@code WizardDialogBuilder}, for method chaining
     */
    public WizardDialogBuilder minHeight(double value) {
        this.minHeight = value;
        return this;
    }

    /**
     * Sets the maximum width for the wizard dialog being built.
     *
     * @param value the maximum width of the wizard dialog in pixels
     * @return the current instance of {@code WizardDialogBuilder}, allowing method chaining
     */
    public WizardDialogBuilder maxWidth(double value) {
        this.maxWidth = value;
        return this;
    }

    /**
     * Sets the maximum height for the wizard dialog being built.
     *
     * @param value the maximum height value to be set, specified as a double.
     * @return The current instance of {@code WizardDialogBuilder}, allowing method chaining.
     */
    public WizardDialogBuilder maxHeight(double value) {
        this.maxHeight = value;
        return this;
    }

    /**
     * Set the modality of the dialog.
     *
     * @param modality the modality to set
     * @return the current builder instance, to allow method chaining
     */
    public WizardDialogBuilder modality(Modality modality) {
        this.modality = modality;
        return this;
    }

    /**
     * Adds a page to the wizard dialog.
     *
     * @param <D> the type of the input dialog pane
     * @param <B> the type of the abstract pane builder
     * @param <R> the type of the result produced by the pane
     * @param name the name of the page to add
     * @param builder the builder used to create and configure the pane
     * @return the current instance of {@code WizardDialogBuilder}, for method chaining
     */
    public <D extends InputDialogPane<R>, B extends PaneBuilder<D, B, R>, R> WizardDialogBuilder page(String name, B builder) {
        D pane = builder.build();

        if (minWidth > 0) pane.setMinWidth(minWidth);
        if (minHeight > 0) pane.setMinHeight(minHeight);
        if (maxWidth < Integer.MAX_VALUE) pane.setMaxWidth(maxWidth);
        if (maxHeight < Integer.MAX_VALUE) pane.setMaxHeight(maxHeight);
        if (prefWidth > 0) pane.setPrefWidth(prefWidth);
        if (prefHeight > 0) pane.setPrefHeight(prefHeight);

        DialogPaneBuilder.ResultHandler<R> resultHandler = builder.getResultHandler();
        WizardDialog.Page<D, R> page = new WizardDialog.Page<>(pane, resultHandler);
        page.setNext(builder.getNext());
        pages.put(name, page);

        if (startPage.isEmpty()) {
            setStartPage(name);
        }

        return this;
    }

    /**
     * Displays the constructed wizard dialog and waits for the user to close it.
     *
     * @return An Optional containing a map with the results from the wizard dialog if the dialog was completed,
     *         or an empty Optional if the dialog was canceled or closed without completion.
     */
    @SuppressWarnings("OptionalContainsCollection")
    public Optional<Map<String, Object>> showAndWait() {
        return build().showAndWait();
    }

    /**
     * Constructs and returns a new instance of {@link WizardDialog} with the configured title and pages.
     *
     * <p>The pages are linked sequentially based on their insertion order. If a page does not have
     * a "next" page set, it will be linked to the subsequent page in the insertion order.
     *
     * @return A new {@link WizardDialog} instance with the configured title and pages.
     */
    public WizardDialog build() {
        WizardDialog dlg = new WizardDialog(parentWindow, cancelable);

        WizardDialog.Page<?, ?> prev = null;
        for (var entry : pages.entrySet()) {
            String name = entry.getKey();
            WizardDialog.Page<?, ?> page = entry.getValue();

            if (prev != null && prev.getNext().isEmpty()) {
                prev.setNext(Map.of(ButtonType.NEXT, name));
            }

            prev = page;
        }

        dlg.setTitle(title);
        dlg.setPages(new LinkedHashMap<>(pages), getStartPage());

        dlg.initModality(Objects.requireNonNullElse(modality, Modality.WINDOW_MODAL));

        return dlg;
    }

    /**
     * Retrieves the start page of the wizard dialog being built.
     *
     * @return The name of the start page as a String.
     */
    public String getStartPage() {
        return startPage;
    }

    /**
     * Sets the start page for the wizard dialog.
     *
     * @param startPage The name of the page to be set as the start page.
     */
    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

}
