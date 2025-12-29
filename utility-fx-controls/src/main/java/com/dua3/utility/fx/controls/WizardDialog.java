package com.dua3.utility.fx.controls;

import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.controls.abstract_builders.DialogPaneBuilder.ResultHandler;
import com.dua3.utility.data.Pair;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a wizard dialog that guides the user through a sequence of pages.
 * Each page can represent a step in a process, and the wizard dialog allows
 * navigation between these steps.
 */
public class WizardDialog extends Dialog<Map<String, @Nullable Object>> {

    /**
     * Logger instance
     */
    private static final Logger LOG = LogManager.getLogger(WizardDialog.class);
    /**
     * Stack of displayed pages (for navigating back).
     */
    private final ObservableList<Pair<String, Page<?, ?>>> pageStack = FXCollections.observableArrayList();
    /**
     * Cancelable flag.
     */
    private boolean cancelable;
    /**
     * Map {@code <page-name> |-> <page-information>}.
     */
    private @Nullable Map<String, Page<?, ?>> pages;
    /**
     * The currently displayed page.
     */
    private @Nullable Pair<String, Page<?, ?>> current;

    /**
     * WizardDialog initializes a new dialog that handles the navigation and data collection
     * of a sequence of wizard pages.
     *
     * @param parentWindow the parent window
     */
    WizardDialog(@Nullable Window parentWindow, boolean cancelable) {
        initOwner(parentWindow);
        this.cancelable = cancelable;

        setResultConverter(btn -> {
            if (btn != ButtonType.FINISH) {
                return null;
            }

            // add current page to the stack, then build and return the result map
            addPageToStack(Objects.requireNonNull(current, "no pages"));

            // WARNING: do not use collect(Collectors.toMap(...)) because it cannot handle null
            LinkedHashMap<String, @Nullable Object> result = new LinkedHashMap<>();
            pageStack.forEach(p -> result.put(p.first(), p.second().result));

            return result;
        });
    }

    /**
     * Sets the wizard pages and the initial page to start from.
     *
     * @param pages a map where keys are page identifiers and values are the corresponding Page objects
     * @param startPage the identifier of the page where the wizard dialog should start
     */
    public void setPages(Map<String, Page<?, ?>> pages, String startPage) {
        this.pages = pages;

        checkPages(startPage);

        setPage(startPage);
    }

    /**
     * Verifies the configuration of each page within the wizard, ensuring that next page references exist,
     * and sets up the dialog pane buttons for each page.
     *
     * <p>This method performs the following tasks for each page in the wizard:
     *<ol>
     * <li> Confirms that the 'next' page reference for each page exists in the set of pages. If a 'next' page is
     *    referenced that does not exist, an IllegalStateException is thrown.
     * <li> Prepares the buttons for the page's dialog pane by initializing the buttons through `initButtons()` method.
     * <li> Adds a 'cancel' button to the dialog pane if the wizard is cancelable.
     * <li> Adds a 'next' button or 'finish' button to the dialog pane depending on whether the current page has a
     *    'next' reference. The 'next' button pushes the current page onto the stack and navigates to the 'next' page.
     *    The 'finish' button is added if there is no 'next' page.
     * <li> Adds a 'previous' button to navigate to the previous page if the previous button functionality is enabled.
     * </ol>
     * @throws IllegalStateException if any page refers to a 'next' page that does not exist
     */
    private void checkPages(String startPage) {
        if (pages == null) {
            return;
        }

        // avoid flickering
        getDialogPane().getScene().setFill(Color.TRANSPARENT);

        // get and translate the result
        Set<String> pageNames = pages.keySet();
        for (Entry<String, Page<?, ?>> entry : pages.entrySet()) {
            String name = entry.getKey();
            Page<?, ?> page = entry.getValue();

            // check page names
            String next = page.getNext();
            if (next != null && !pageNames.contains(next)) {
                throw new IllegalStateException(String.format("Page '%s': next page doesn't exist ['%s']", name, next));
            }

            // cancel button
            if (isCancelable()) {
                page.addButton(
                        ButtonType.CANCEL,
                        p -> {},
                        null
                );
            }

            // next button
            if (page.getNext() == null) {
                page.addButton(
                        ButtonType.FINISH,
                        p -> {},
                        InputDialogPane::validProperty
                );
            } else {
                page.addButton(
                        ButtonType.NEXT,
                        p -> {
                            addPageToStack(Pair.of(name, page));
                            setPage(page.getNext());
                        },
                        InputDialogPane::validProperty
                );
            }

            // prev button
            if (!entry.getKey().equals(startPage)) {
                page.addButton(
                        ButtonType.PREVIOUS,
                        p -> setPage(pageStack.removeLast().first()),
                        p -> Bindings.isNotEmpty(pageStack)
                );
            }
        }

        // determine dialog dimensions
        double minWidth = -1;
        double minHeight = -1;
        double maxWidth = Integer.MAX_VALUE;
        double maxHeight = Integer.MAX_VALUE;
        double prefWidth = -1;
        double prefHeight = -1;
        for (var page : pages.values()) {
            InputDialogPane<?> pane = page.pane;

            setDialogPane(pane);
            pane.applyCss();
            pane.layout();

            if (pane.getMinWidth() > 0) {minWidth = Math.max(minWidth, pane.getMinWidth());}
            if (pane.getMinHeight() > 0) {minHeight = Math.max(minHeight, pane.getMinHeight());}
            if (pane.getMaxWidth() < Integer.MAX_VALUE) {maxWidth = Math.min(maxWidth, pane.getMaxWidth());}
            if (pane.getMaxHeight() < Integer.MAX_VALUE) {maxHeight = Math.min(maxHeight, pane.getMaxHeight());}
            if (pane.getPrefWidth() > 0) {prefWidth = Math.max(prefWidth, pane.getPrefWidth());}
            if (pane.getPrefHeight() > 0) {prefHeight = Math.max(prefHeight, pane.getPrefHeight());}
        }

        if (minWidth > 0 && maxWidth < Integer.MAX_VALUE && minWidth > maxWidth) {minWidth = maxWidth;}
        if (minHeight > 0 && maxHeight < Integer.MAX_VALUE && minHeight > maxHeight) {minHeight = maxHeight;}

        for (var page : pages.values()) {
            InputDialogPane<?> pane = page.pane;
            if (minWidth > 0) {pane.setMinWidth(minWidth);}
            if (maxWidth < Integer.MAX_VALUE) {pane.setMaxWidth(maxWidth);}
            if (prefWidth > 0) {pane.setPrefWidth(Math.clamp(pane.getPrefWidth(), minWidth, maxWidth));}
            if (prefHeight > 0) {pane.setPrefHeight(Math.clamp(pane.getPrefHeight(), minHeight, maxHeight));}
            pane.layout();
        }
    }

    private void addPageToStack(Pair<String, Page<?, ?>> pair) {
        pageStack.add(pair);
    }

    private void setPage(String pageName) {
        this.current = Pair.of(pageName, Objects.requireNonNull(pages, "pages not set").get(pageName));

        assert current.second() != null;
        InputDialogPane<?> pane = current.second().pane;

        // make sure the dialog does not shrink
        DialogPane previousPane = getDialogPane();
        if (previousPane != null) {
            pane.setMinSize(Math.max(previousPane.getWidth(), pane.getMinWidth()), Math.max(previousPane.getHeight(), pane.getMinHeight()));
        }

        setDialogPane(pane);

        pane.init();
        pane.layout();
        Window window = pane.getScene().getWindow();
        window.sizeToScene();

        LOG.trace("current page: {}", pageName);
    }

    /**
     * Check if dialog can be canceled.
     *
     * @return true if dialog is cancelable
     */
    public boolean isCancelable() {
        return cancelable;
    }

    /**
     * Retrieves the current wizard page.
     *
     * @return the current wizard page as a {@link Page} object
     */
    public Optional<Page<?, ?>> getCurrentPage() {
        return Optional.ofNullable(current).map(Pair::second);
    }

    /**
     * Represents a single page within the wizard dialog. Each page consists of an input dialog pane
     * and a result handler to process its content. Pages can also define a navigation flow by
     * specifying the next page to proceed to.
     *
     * @param <D> the type of the input dialog pane used in the page, extending {@link InputDialogPane}
     * @param <R> the result type produced by the input dialog pane
     */
    public static class Page<D extends InputDialogPane<R>, R> {
        private final D pane;
        private final ResultHandler<R> resultHandler;
        private @Nullable String next;
        private @Nullable R result;

        Page(D pane, ResultHandler<R> resultHandler) {
            this.pane = pane;
            this.resultHandler = (btn, r) -> {
                boolean ok = resultHandler.handleResult(btn, r);
                this.result = r;
                return ok;
            };
        }

        @Nullable
        String getNext() {
            return next;
        }

        void setNext(@Nullable String next) {
            this.next = next;
        }

        D getPane() {
            return pane;
        }

        /**
         * Adds a button to the current page of the wizard dialog. This method delegates
         * the button creation and configuration to the underlying input dialog pane,
         * associating the button with a specific type, an action to execute, and an
         * optional enablement state.
         *
         * @param type    the type of the button to add, specifying its purpose and behavior
         * @param action  a consumer that performs an action on the {@code InputDialogPane}
         *                when the button is triggered
         * @param enabled an optional {@code BooleanExpression} that determines whether the
         *                button is enabled or disabled dynamically; can be null
         */
        public void addButton(ButtonType type, Consumer<InputDialogPane<?>> action, @Nullable Function<InputDialogPane<?>, BooleanExpression> enabled) {
            pane.addButton(type, resultHandler, action, enabled);
        }
    }

}
