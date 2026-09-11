package com.dua3.utility.fx;

import com.dua3.utility.application.ApplicationUtil;
import javafx.collections.ListChangeListener;
import javafx.stage.Window;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Keeps native window decorations aligned with the application UI mode.
 */
final class FxWindowDecorationUpdater {

    private static final Set<Window> TRACKED_WINDOWS = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ListChangeListener<Window> WINDOW_LIST_LISTENER = change -> {
        while (change.next()) {
            if (change.wasAdded()) {
                change.getAddedSubList().forEach(FxWindowDecorationUpdater::trackWindow);
            }
        }
    };

    private static boolean installed = false;

    private FxWindowDecorationUpdater() {
        // utility class
    }

    /**
     * Installs the process-wide JavaFX window tracker.
     * This method must run on the JavaFX Application Thread.
     */
    static void install() {
        PlatformHelper.checkApplicationThread();
        if (installed) {
            return;
        }

        installed = true;
        Window.getWindows().forEach(FxWindowDecorationUpdater::trackWindow);
        Window.getWindows().addListener(WINDOW_LIST_LISTENER);
    }

    private static void trackWindow(Window window) {
        if (!TRACKED_WINDOWS.add(window)) {
            return;
        }

        window.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing != null && isShowing) {
                ApplicationUtil.updateWindowDecorations();
            }
        });
        if (window.isShowing()) {
            ApplicationUtil.updateWindowDecorations();
        }
    }
}
