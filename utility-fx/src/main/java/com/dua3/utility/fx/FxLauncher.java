package com.dua3.utility.fx;

import com.dua3.utility.lang.Platform;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.Repetitions;
import javafx.application.Application;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;


/**
 * Application launcher class.
 */
public final class FxLauncher {

    /**
     * Exit code indicating successful execution of the application.
     */
    public static final int RC_SUCCESS = 0;
    /**
     * Exit code indicating that an error occurred during the execution of the application.
     */
    public static final int RC_ERROR = 1;

    private static final @Nullable Method LOGUTIL_INITIALISER ;

    static {
        Method initialiser = null;
        try {
            Class<?> clazz = Class.forName("com.dua3.utiliy.log.log4j.LogUtilLog4J");
            initialiser = clazz.getDeclaredMethod("init", null, LogLevel.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // do nothing
        }
        LOGUTIL_INITIALISER = initialiser;
    }

    private static final Pattern PATTERN_PATH_OR_STARTS_WITH_DOUBLE_DASH = Pattern.compile("^(--|[a-zA-Z]:[/\\\\]).*");
    static @Nullable LogBuffer logBuffer = null;
    static boolean showLogWindow = false;
    static boolean showLogPane = false;

    private static final class PlatformGuard {
        private static final AtomicBoolean launched = new AtomicBoolean(false);

        static {
            // start the runtime
            CountDownLatch latch = new CountDownLatch(1);
            javafx.application.Platform.startup(latch::countDown);
            while (true) {
                try {
                    latch.await();
                    break;
                } catch (InterruptedException e) {
                    LogManager.getLogger(FxLauncher.class).debug("interrupted while waiting for platform startup", e);
                    Thread.currentThread().interrupt(); // Restore the interrupt status
                }
            }
        }

        public static void run(Runnable r) {
            r.run();
        }

        public static <T> T run(Supplier<T> r) {
            return r.get();
        }

        public static <A extends Application>
        void launch(Class<A> cls, String... args) {
            LogManager.getLogger(FxLauncher.class).debug("arguments: {}", (Object) args);

            if (launched.getAndSet(true)) {
                throw new IllegalStateException("Application already launched.");
            }

            // prepare arguments
            var reparsedArgs = reparseCommandLine(args);

            // launch
            Application.launch(cls, reparsedArgs.toArray(String[]::new));
        }
    }

    private FxLauncher() {}

    /**
     * Executes the given Runnable object.
     * <p>
     * Delegating the task to the launcher makes sure that the platform startup is completed
     * before the task is run.
     *
     * @param r the Runnable object to be executed
     */
    public static void run(Runnable r) {
        PlatformGuard.run(r);
    }

    /**
     * Start application.
     * This method is a drop-in replacement for `Application.launch(cls, args)`.
     * <ul>
     *     <li><strong>Command line arguments</strong> are reparsed on windows, for details see
     *     {@link #reparseCommandLine(String[])}.
     * </ul>
     *
     * @param <A>  the application class
     * @param cls  the application class
     * @param args the command line arguments
     */
    public static <A extends Application>
    void launch(Class<A> cls, String... args) {
        PlatformGuard.launch(cls, args);
    }

    /**
     * Reparse command line arguments.
     * <ul>
     * <li><strong>Windows:</strong>
     * At least when using a jpackaged application with file-associations, command line arguments get messed up
     * when the application start is the result of double-clicking on a registered file type.
     * The command line args are split on whitespace, i.e. paths containing spaces
     * will be split into multiple parts. This method tries to restore what was probably meant.
     * It works by iterating over the given array of arguments like this:
     * <pre>
     * let arg = ""
     * for each s in args:
     *
     *   if s starts with "--" // start of an option
     *   or s starts with "[letter]:\" or "[letter]:/" // probable file path
     *   then
     *     append arg to the output array
     *     arg = ""
     *
     *   if arg != ""
     *     arg = arg + " "
     *
     *   arg = arg + s
     * push arg to the output array
     * </pre>
     * <li><strong>Other platforms:</strong>
     * The input array is returned without any changes.
     * </ul>
     *
     * @param args the command line arguments
     * @return the reparsed argument array
     */
    private static List<String> reparseCommandLine(String[] args) {
        if (!Platform.isWindows() || args.length < 2) {
            return List.of(args);
        }

        List<String> argL = new ArrayList<>();
        StringBuilder arg = new StringBuilder();
        for (String s : args) {
            // split if s contains spaces, starts with a double dash, or a windows path
            if (s.indexOf(' ') >= 0 || PATTERN_PATH_OR_STARTS_WITH_DOUBLE_DASH.matcher(s).matches()) {
                if (!arg.isEmpty()) {
                    argL.add(arg.toString());
                }
                arg.setLength(0);
            }

            if (!arg.isEmpty()) {
                arg.append(' ');
            }
            arg.append(s);
        }
        if (!arg.isEmpty()) {
            argL.add(arg.toString());
        }

        Logger log = LogManager.getLogger(FxLauncher.class);
        log.debug("original arguments: {}", (Object) args);
        log.debug("re-parsed arguments: {}", argL);

        return argL;
    }

    /**
     * Runs the specified JavaFX application class with the provided arguments and settings.
     *
     * @param applicationClassName the fully qualified name of the JavaFX application class to be launched
     * @param args                 the command-line arguments passed to the application
     * @param appName              the name of the application
     * @param version              the version of the application
     * @param copyright            the copyright notice of the application
     * @param developerMail        the developer's contact email
     * @param appDescription       a brief description of the application
     * @param addOptions          variable-length array of consumers that add additional command-line options to the parser
     * @return the exit code of the application; 0 indicates successful execution, 1 indicates an error
     */
    @SafeVarargs
    public static int launchApplication(
            String applicationClassName,
            String[] args,
            String appName,
            String version,
            String copyright,
            String developerMail,
            String appDescription,
            Consumer<ArgumentsParserBuilder>... addOptions
    ) {
        var agp = ArgumentsParser.builder()
                .name(appName)
                .description("Version " + version + "\n"
                        + copyright + " (" + developerMail + ")\n"
                        + "\n"
                        + appDescription
                        + "\n"
                )
                .positionalArgs(0, 0);

        var flagHelp = agp.addFlag(
                "Help",
                "Show Help and quit.",
                "--help", "-h"
        );
        var flagEnableAssertions = agp.addFlag(
                "Runtime Checks",
                "Enable runtime checks.",
                "--enable-assertions", "-ea"
        );

        if (LOGUTIL_INITIALISER != null) {
            Consumer<LogLevel> setLogLevel = level -> {
                try {
                    LOGUTIL_INITIALISER.invoke(null, level);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
            };

            agp.addEnumOption(
                    "Log Level",
                    "Set the global Log Level.",
                    Repetitions.ZERO_OR_ONE,
                    "level",
                    setLogLevel,
                    LogLevel.class,
                    "--log-level", "-ll"
            );
            agp.addFlag(
                    "Show Log Window",
                    "Show Log Messages in a separate Window.",
                    v -> showLogWindow = v,
                    "--log-window", "-lw"
            );
            agp.addFlag(
                    "Show Log Messages",
                    "Show Log Messages in Main Window.",
                    v -> showLogPane = v,
                    "--log-pane", "-lp"
            );
            agp.addStringOption(
                    "Log Filfter",
                    "Set global Filter for Logger Names.",
                    Repetitions.ZERO_OR_ONE,
                    "regex",
                    pattern -> {
                        Predicate<String> predicate = Pattern.compile(pattern).asMatchPredicate();
                        LogUtil.getGlobalDispatcher().setFilter(entry -> predicate.test(entry.loggerName()));
                    },
                    "--log-filter", "-lf"
            );
            agp.addIntegerOption(
                    "Log Buffer Size",
                    "Set the Size of the Log Buffer.",
                    Repetitions.ZERO_OR_ONE,
                    "size",
                    size -> logBuffer = new LogBuffer(size),
                    "--log-buffer-size", "-ls"
            );
        }

        for (Consumer<ArgumentsParserBuilder> addOption : addOptions) {
            addOption.accept(agp);
        }

        ArgumentsParser ap = agp.build(flagHelp);

        var arguments = ap.parse(args);

        if (arguments.isSet(flagHelp)) {
            //noinspection UseOfSystemOutOrSystemErr
            System.out.println(ap.help());
            return RC_SUCCESS;
        }

        boolean enableAssertions = arguments.isSet(flagEnableAssertions);

        if (showLogWindow || showLogPane) {
            if (logBuffer == null) {
                logBuffer = new LogBuffer();
            }
            LogUtil.getGlobalDispatcher().addLogEntryHandler(logBuffer);
        }

        Logger log = LogManager.getLogger(FxLauncher.class);
        int rc;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();

            if (enableAssertions) {
                log.info("enabling assertions");
                loader.setDefaultAssertionStatus(true);
            }
            log.info("loading application class: {}", applicationClassName);
            @SuppressWarnings("unchecked")
            Class<? extends Application> applicationClass = (Class<? extends Application>) loader.loadClass(applicationClassName);
            log.info("starting application: {}", applicationClass.getName());
            launch(applicationClass, args);
            rc = RC_SUCCESS;
        } catch (Exception e) {
            log.error("exception caught", e);
            rc = RC_ERROR;
        }

        log.info("finished with rc: {}", rc);
        return rc;
    }

    /**
     * Retrieves the current LogBuffer instance if it is available.
     *
     * @return an {@link Optional} containing the LogBuffer instance if it exists, or an empty {@link Optional} if not.
     */
    public static Optional<LogBuffer> getLogBuffer() {
        return Optional.ofNullable(logBuffer);
    }

    /**
     * Determines whether the log pane should be displayed.
     *
     * @return true if the log pane is set to be displayed, false otherwise.
     */
    public static boolean isShowLogPane() {
        return showLogPane;
    }

    /**
     * Determines whether the log window should be displayed.
     *
     * @return true if the log window is configured to be displayed, false otherwise.
     */
    public static boolean isShowLogWindow() {
        return showLogWindow;
    }

    /**
     * Displays a log window for the application, if configured to do so.
     *
     * @param stage the owner window for the log window; can be null if no parent stage is specified.
     * @param title the title of the log window to be displayed.
     * @return an {@link Optional} containing the {@link FxLogWindow} instance if the log window is displayed,
     *         or an empty {@link Optional} if the log window is not configured to be displayed.
     */
    public static Optional<FxLogWindow> showLogWindow(Window stage, String title) {
        return PlatformGuard.run(() -> {
            if (showLogWindow) {
                FxLogWindow logWindow = new FxLogWindow(title, getLogBuffer().orElseThrow());
                logWindow.initOwner(stage);
                logWindow.show();
                return Optional.of(logWindow);
            } else {
                return Optional.empty();
            }
        });
    }

    /**
     * Displays a log window for the application, if configured to do so.
     *
     * @param stage the owner window for the log window; can be null if no parent stage is specified.
     * @return an {@link Optional} containing the {@link FxLogWindow} instance if the log window is displayed,
     *         or an empty {@link Optional} if the log window is not configured to be displayed.
     */
    public static Optional<FxLogWindow> showLogWindow(Window stage) {
        return PlatformGuard.run(() -> showLogWindow(stage, "Log Messages"));
    }

    /**
     * Retrieves an instance of the {@link FxLogPane} if the log pane is configured to be displayed.
     *
     * @return an {@link Optional} containing the {@link FxLogPane} instance if the log pane is enabled,
     *         or an empty {@link Optional} if the log pane is disabled.
     */
    public static Optional<FxLogPane> getLogPane() {
        return PlatformGuard.run( () -> {
            if (!showLogPane) {
                return Optional.empty();
            }

            return Optional.of(new FxLogPane(getLogBuffer().orElseThrow()));
        });
    }
}
