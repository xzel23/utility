package com.dua3.utility.fx;

import com.dua3.utility.options.ArgumentsParserBuilder;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

public final class FxLauncherLogging {

    private static final @Nullable Method LOGUTIL_INITIALISER;

    private static final String LOG_MESSAGES = I18NInstance.get().get("dua3.utility.fx.controls.launcher.log.messages");

    /*
    private static final Method GET_LOG_BUFFER;
    private static final Constructor FX_LOG_WINDOW_CONSTRUCTOR;
    private static final Constructor FX_LOG_PANE_CONSTRUCTOR;
*/
    static {
        Method initialiser = null;
        try {
            Class<?> clazz = Class.forName("com.dua3.sawmill.lumberjack.Lumberjack");
            initialiser = clazz.getDeclaredMethod("init");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // do nothing
        }
        LOGUTIL_INITIALISER = initialiser;
    }
/*
    static @Nullable LogBuffer logBuffer = null;
*/
    static boolean showLogWindow = false;
    private FxLauncherLogging() {}

    public static boolean isLoggingSupported() {
        return LOGUTIL_INITIALISER != null;
    }
    /**
     * Retrieves the current LogBuffer instance if it is available.
     *
     * @return an {@link Optional} containing the LogBuffer instance if it exists, or an empty {@link Optional} if not.
     */
/*
    public static Optional<LogBuffer> getLogBuffer() {
        return Optional.ofNullable(GET_LOG_BUFFER.invoke(null));
    }
*/
    /**
     * Determines whether the log window should be displayed.
     *
     * @return true if the log window is configured to be displayed, false otherwise.
     */
    public static boolean isShowLogWindow() {
        return showLogWindow;
    }

    public static Optional<Stage> showLogWindow(@Nullable Window owner, String title) {
        return Optional.empty();
        /*
        return PlatformGuard.run(() -> {
            if (logBuffer != null) {
                FxLogWindow logWindow = PlatformHelper.runAndWait(() -> {
                    FxLogWindow window = new FxLogWindow(title, getLogBuffer().orElseThrow());
                    window.initOwner(owner);
                    window.show();
                    return window;
                });
                return Optional.ofNullable(logWindow);
            } else {
                return Optional.empty();
            }
        });
         */
    }

    public static Optional<Stage> showLogWindow(@Nullable Window owner) {
        return showLogWindow(owner, LOG_MESSAGES);
    }

    public static Optional<Pane> getLogPane() {
        return Optional.empty();
        /*
        return PlatformGuard.run(() -> {
            if (!debug) {
                return Optional.empty();
            }

            return Optional.of(new FxLogPane(getLogBuffer().orElseThrow()));
        });
        */
    }

    public static void addLoggingOptions(ArgumentsParserBuilder apb) {
        /*
        agp.addFlag(
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_window.name"),
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_window.description"),
                v -> showLogWindow = v,
                "--log-window", "-lw"
        );
        agp.addFlag(
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.debug.name"),
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.debug.description"),
                v -> debug = v,
                "--debug"
        );
        if (LOGUTIL_INITIALISER != null) {
            agp.addStringOption(
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_filter.name"),
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_filter.description"),
                    Repetitions.ZERO_OR_ONE,
                    "regex",
                    pattern -> {
                        Predicate<String> predicate = Pattern.compile(pattern).asMatchPredicate();
                        LogUtil.getGlobalDispatcher().setFilter((LogEntryFilter) (entry -> predicate.test(entry.loggerName())));
                    },
                    "--log-filter", "-lf"
            );
            agp.addIntegerOption(
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_buffer_size.name"),
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.log_buffer_size.description"),
                    Repetitions.ZERO_OR_ONE,
                    "size",
                    size -> logBuffer = new LogBuffer("Application Log Buffer", size),
                    "--log-buffer-size", "-ls"
            );

        }
         */
    }
}
