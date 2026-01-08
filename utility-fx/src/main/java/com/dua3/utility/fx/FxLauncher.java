package com.dua3.utility.fx;

import com.dua3.utility.lang.Platform;
import com.dua3.utility.logging.DefaultLogEntryFilter;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntryFilter;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import com.dua3.utility.logging.log4j.LogEntryLog4J;
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
 * The FxLauncher class is responsible for managing the launch and runtime behavior of JavaFX applications.
 * It provides utility methods for executing tasks, launching applications, reparsing command line arguments,
 * managing log views, and handling debug behavior and assertions.
 * <p>
 * The FxLauncher utility class enhances standard the JavaFX launching process by adding this functionality:
 * <ul>
 *     <li>Better compatibility for jpackaged/jlinked applications on Windows: The standard JavaFX launcher
 *         determines arguments by splitting the text passed on the command line on whitespace. When the
 *         application is using registered file associations, the file path is passed unescaped to the
 *         application. When the path contains spaces, this leads to incorrect arguments being passed to
 *         the application, i.e. "C:\program files\foo.txt" will be passed as ["C:\program", "files\foo.txt"].
 *         {@link FxLauncher#launch(Class, String...)} will fix up the command line so that instead
 *         ["C:\program files\foo.txt"] is passed to the application.
 *     <li>Automatically add command line help and default options when
 *         {@link #launchApplication(String, String[], String, String, String, String, String, Consumer[])} is used:
 *         <ul>
 *         <li><strong>{@code --help}:</strong>
 *             show command line help.
 *         <li><strong>{@code --enable-assertions}:</strong>
 *             Enable assertions.
 *         <li><strong>{@code --debug}:</strong>
 *             Enable debug mode. Use {@link #isDebug()} in your application code to test for debug mode.
 *         <li><strong>{@code --log-window}:</strong>
 *             show a window containing the application's logging messages
 *             is added without needing any application code. Make sure you either use Log4J or have the required
 *             bridge implementations on your classpath, and make sure the logging system is not initialized
 *             before the {@code launchApplication()} is called.
 *             This option is only added when {@code com.dua3.utility.logging.log4j.LogUtilLog4J} is on the classpath.
 *         <li><strong>Application-specific options:</strong>
 *             Use the {@code addOptions}parameter to pass application-specific options.
 *     </ul>
 * </ul>
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

    private static final @Nullable Method LOGUTIL_INITIALISER;
    private static final String LOG_MESSAGES = I18NInstance.get().get("dua3.utility.fx.controls.launcher.log.messages");

    static {
        Method initialiser = null;
        try {
            Class<?> clazz = Class.forName("com.dua3.utility.logging.log4j.LogUtilLog4J");
            initialiser = clazz.getDeclaredMethod("init", LogLevel.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // do nothing
        }
        LOGUTIL_INITIALISER = initialiser;
    }

    private static final Pattern PATTERN_PATH_OR_STARTS_WITH_DOUBLE_DASH = Pattern.compile("^(--|[a-zA-Z]:[/\\\\]).*");
    static @Nullable LogBuffer logBuffer = null;
    static boolean showLogWindow = false;
    static boolean debug = false;
    static boolean enableAssertions = false;

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

        // !!! Do NOT use a static Logger instance as that interferes with setting up logging !!!
        Logger log = LogManager.getLogger(FxLauncher.class);
        log.trace("original arguments: {}", (Object) args);
        log.trace("re-parsed arguments: {}", argL);

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
                .description(I18NInstance.get().format("dua3.utility.fx.controls.launcher.about.version", version) + "\n"
                        + I18NInstance.get().format("dua3.utility.fx.controls.launcher.about.copyright", copyright, developerMail) + "\n"
                        + "\n"
                        + appDescription
                        + "\n"
                )
                .positionalArgs(0, 0);

        var flagHelp = agp.addFlag(
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.help.name"),
                I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.help.description"),
                "--help", "-h"
        );

        if (!Platform.isNativeImage()) {
            agp.addFlag(
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.assertions.name"),
                    I18NInstance.get().get("dua3.utility.fx.controls.launcher.arg.assertions.description"),
                    v -> enableAssertions = v,
                    "--enable-assertions", "-ea"
            );
        }

        if (LOGUTIL_INITIALISER != null) {
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

        arguments.handle();

        if ((showLogWindow || debug) && LOGUTIL_INITIALISER != null) {
            try {
                LOGUTIL_INITIALISER.invoke(null, LogLevel.TRACE);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
            if (logBuffer == null) {
                logBuffer = new LogBuffer();
            }
            LogUtil.getGlobalDispatcher().addLogHandler(logBuffer);
        }

        Logger log = LogManager.getLogger(FxLauncher.class);
        int rc;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            if (enableAssertions && !Platform.isNativeImage()) {
                log.debug("enabling assertions");
                loader.setDefaultAssertionStatus(true);
            }
            log.debug("loading application class: {}", applicationClassName);
            @SuppressWarnings("unchecked")
            Class<? extends Application> applicationClass = (Class<? extends Application>) loader.loadClass(applicationClassName);
            log.info("starting application: {}", applicationClass.getName());
            showLogWindow(null, appName + " - " + LOG_MESSAGES);
            launch(applicationClass, args);
            rc = RC_SUCCESS;
        } catch (Exception e) {
            log.error("exception caught", e);
            rc = RC_ERROR;
        }

        log.info("application finished with rc: {}", rc);
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
    public static boolean isDebug() {
        return debug;
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
     * @param owner the owner window for the log window; can be null if no parent stage is specified.
     * @param title the title of the log window to be displayed.
     * @return an {@link Optional} containing the {@link FxLogWindow} instance if the log window is displayed,
     *         or an empty {@link Optional} if the log window is not configured to be displayed.
     */
    public static Optional<FxLogWindow> showLogWindow(@Nullable Window owner, String title) {
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
    }

    /**
     * Displays a log window for the application, if configured to do so.
     *
     * @param owner the owner window for the log window; can be null if no parent stage is specified.
     * @return an {@link Optional} containing the {@link FxLogWindow} instance if the log window is displayed,
     *         or an empty {@link Optional} if the log window is not configured to be displayed.
     */
    public static Optional<FxLogWindow> showLogWindow(@Nullable Window owner) {
        return showLogWindow(owner, LOG_MESSAGES);
    }

    /**
     * Retrieves an instance of the {@link FxLogPane} if the log pane is configured to be displayed.
     *
     * @return an {@link Optional} containing the {@link FxLogPane} instance if the log pane is enabled,
     *         or an empty {@link Optional} if the log pane is disabled.
     */
    public static Optional<FxLogPane> getLogPane() {
        return PlatformGuard.run(() -> {
            if (!debug) {
                return Optional.empty();
            }

            return Optional.of(new FxLogPane(getLogBuffer().orElseThrow()));
        });
    }
}
