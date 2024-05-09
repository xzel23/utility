package com.dua3.utility.fx.controls;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WizardDialogBuilder {

    final LinkedHashMap<String, WizardDialog.Page<?, ?>> pages = new LinkedHashMap<>();
    private String title = "";
    private String startPage = null;

    WizardDialogBuilder() {}

    public WizardDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public <D extends InputDialogPane<R>, B extends AbstractPaneBuilder<D, B, R>, R> WizardDialogBuilder page(String name, B builder) {
        WizardDialog.Page<D, R> page = new WizardDialog.Page<>();
        page.setNext(builder.next);
        D pane = builder.build();
        AbstractDialogPaneBuilder.ResultHandler<R> resultHandler = builder.getResultHandler();
        page.setPane(pane, resultHandler);
        pages.put(name, page);

        if (startPage == null) {
            setStartPage(name);
        }

        return this;
    }

    @SuppressWarnings("OptionalContainsCollection")
    public Optional<Map<String, Object>> showAndWait() {
        return build().showAndWait();
    }

    public WizardDialog build() {
        WizardDialog dlg = new WizardDialog();

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

    public String getStartPage() {
        return startPage;
    }

    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

}
