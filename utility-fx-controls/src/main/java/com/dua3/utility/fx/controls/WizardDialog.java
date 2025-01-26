package com.dua3.utility.fx.controls;

import javafx.beans.binding.BooleanExpression;
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

/**
 * Represents a wizard dialog that guides the user through a sequence of pages.
 * Each page can represent a step in a process, and the wizard dialog allows
 * navigation between these steps.
 */
public class WizardDialog extends Dialog<@Nullable Map<String, Object>> {

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
    private boolean cancelable = true;
    /**
     * Flag: show 'previous'-button?
     */
    private boolean showPreviousButton = true;
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
     */
    public WizardDialog() {
        setResultConverter(btn -> {
            if (btn != ButtonType.FINISH) {
                return null;
            }

            // add current page to the stack, then build and return the result map
            pageStack.add(Objects.requireNonNull(current, "no pages"));

            // WARNING: do not use collect(Collectors.toMap(...)) because it cannot handle null
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            pageStack.forEach(p -> {
                assert p.second().result != null;
                result.put(p.first(), p.second().result);
            });

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

        checkPages();

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
    private void checkPages() {
        if (pages == null) {
            return;
        }
        // get and translate result

        Set<String> pageNames = pages.keySet();
        for (Entry<String, Page<?, ?>> entry : pages.entrySet()) {
            String name = entry.getKey();
            Page<?, ?> page = entry.getValue();
            InputDialogPane<?> pane = page.getPane();

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
                        pane.validProperty()
                );
            } else {
                page.addButton(
                        ButtonType.NEXT,
                        p -> {
                            pageStack.add(Pair.of(name, page));
                            setPage(page.getNext());
                        },
                        pane.validProperty()
                );
            }

            // prev button
            if (isShowPreviousButton()) {
                page.addButton(
                        ButtonType.PREVIOUS,
                        p -> setPage(pageStack.remove(pageStack.size() - 1).first()),
                        Bindings.isNotEmpty(pageStack)
                );
            }
        }
    }

    private void setPage(String pageName) {
        this.current = Pair.of(pageName, Objects.requireNonNull(pages, "pages not set").get(pageName));

        InputDialogPane<?> pane = current.second().pane;
        setDialogPane(pane);

        pane.init();
        pane.layout();
        pane.getScene().getWindow().sizeToScene();

        LOG.debug("current page: {}", pageName);
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
     * Check if a 'previous' ( or 'navigate-back') button should be displayed.
     *
     * @return true if dialog is cancelable
     */
    public boolean isShowPreviousButton() {
        return showPreviousButton;
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
     * Wizard page information class.
     */
    public static class Page<D extends InputDialogPane<R>, R> {
        private final D pane;
        private final ResultHandler<R> resultHandler;
        private @Nullable String next;
        private @Nullable R result;

        Page(D pane, ResultHandler<R> resultHandler) {
            this.pane = pane;
            this.resultHandler = (btn, result) -> {
                boolean ok = resultHandler.handleResult(btn, result);
                this.result = result;
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

        public void addButton(ButtonType type, Consumer<InputDialogPane<R>> action, @Nullable BooleanExpression enabled) {
            pane.addButton(type, resultHandler, action, enabled);
        }
    }

}
