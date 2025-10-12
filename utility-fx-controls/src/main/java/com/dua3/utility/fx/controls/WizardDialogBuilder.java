package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder;
import com.dua3.utility.fx.controls.abstract_builders.PaneBuilder;
import com.dua3.utility.text.MessageFormatter;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
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
        DialogPaneBuilder.ResultHandler<R> resultHandler = builder.getResultHandler();
        WizardDialog.Page<D, R> page = new WizardDialog.Page<>(pane, resultHandler);
        page.setNext(builder.getNext().orElse(null));
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
        WizardDialog dlg = new WizardDialog(parentWindow);

        WizardDialog.Page<?, ?> prev = null;
        for (var entry : pages.entrySet()) {
            String name = entry.getKey();
            WizardDialog.Page<?, ?> page = entry.getValue();

            if (prev != null && prev.getNext() == null) {
                prev.setNext(name);
            }

            prev = page;
        }

        dlg.setTitle(title);
        dlg.setPages(new LinkedHashMap<>(pages), getStartPage());

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
