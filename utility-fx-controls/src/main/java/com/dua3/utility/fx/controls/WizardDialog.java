package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.fx.controls.AbstractDialogPaneBuilder.ResultHandler;
import com.dua3.utility.data.Pair;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

public class WizardDialog extends Dialog<Map<String, Object>> {

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
    private Map<String, Page<?, ?>> pages;
    /**
     * The currently displayed page.
     */
    private Pair<String, Page<?, ?>> current;

    public WizardDialog() {
        setResultConverter(btn -> {
            if (btn != ButtonType.FINISH) {
                return null;
            }

            // add current page to the stack, then build and return the result map
            pageStack.add(current);

            // WARNING: do not use collect(Collectors.toMap(...)) because it cannot handle null
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            pageStack.forEach(p -> result.put(p.first(), p.second().result));

            return result;
        });
    }

    public void setPages(Map<String, Page<?, ?>> pages, String startPage) {
        this.pages = pages;

        checkPages();

        setPage(startPage);
    }

    private void checkPages() {
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

            // prepare buttons
            pane.initButtons();

            // cancel button
            if (isCancelable()) {
                addButtonToDialogPane(page, ButtonType.CANCEL, p -> {}, null);
            }

            // next button
            if (page.getNext() == null) {
                addButtonToDialogPane(page, ButtonType.FINISH, p -> {}, pane.validProperty());
            } else {
                addButtonToDialogPane(
                        page,
                        ButtonType.NEXT,
                        p -> {
                            pageStack.add(Pair.of(name, page));
                            setPage(page.getNext());
                        },
                        pane.validProperty());
            }

            // prev button
            if (isShowPreviousButton()) {
                addButtonToDialogPane(
                        page,
                        ButtonType.PREVIOUS,
                        p -> setPage(pageStack.remove(pageStack.size() - 1).first()),
                        Bindings.isNotEmpty(pageStack)
                );
            }
        }
    }

    private void setPage(String pageName) {
        this.current = Pair.of(pageName, pages.get(pageName));

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

    private static void addButtonToDialogPane(
            Page<?, ?> page,
            ButtonType bt,
            Consumer<? super InputDialogPane<?>> action,
            @Nullable BooleanExpression enabled) {
        InputDialogPane<?> pane = page.pane;
        List<ButtonType> buttons = pane.getButtonTypes();

        buttons.add(bt);
        Button btn = (Button) pane.lookupButton(bt);

        // it seems counter-intuitive to use an event filter instead of a handler, but
        // when using an event handler, Dialog.close() is called before our own
        // event handler.
        btn.addEventFilter(ActionEvent.ACTION, evt -> {
            // get and translate result
            if (!page.apply(bt)) {
                LOG.debug("Button {}: result conversion failed", bt);
                evt.consume();
            }

            action.accept(page.getPane());
        });

        if (enabled != null) {
            btn.disableProperty().bind(Bindings.not(enabled));
        }
    }

    /**
     * Check if a 'previous' ( or 'navigate-back') button should be displayed.
     *
     * @return true if dialog is cancelable
     */
    public boolean isShowPreviousButton() {
        return showPreviousButton;
    }

    public Page<?, ?> getCurrentPage() {
        return current.second();
    }

    /**
     * Wizard page information class.
     */
    public static class Page<D extends InputDialogPane<R>, R> {
        private D pane;
        private String next;
        private R result;
        private ResultHandler<? super R> resultHandler;

        String getNext() {
            return next;
        }

        void setNext(@Nullable String next) {
            this.next = next;
        }

        D getPane() {
            return pane;
        }

        void setPane(D pane, ResultHandler<? super R> resultHandler) {
            this.pane = pane;
            this.resultHandler = resultHandler;
        }

        boolean apply(ButtonType btn) {
            R r = pane.get();
            boolean done = resultHandler.handleResult(btn, r);
            this.result = done ? r : null;
            return done;
        }
    }

}
