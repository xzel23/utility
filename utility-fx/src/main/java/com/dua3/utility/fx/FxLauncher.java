package com.dua3.utility.fx;

import com.dua3.utility.i18n.I18N;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.options.Repetitions;
import javafx.application.Application;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.slb4j.LogLevel;
import org.slb4j.LoggingConfiguration;
import org.slb4j.SLB4J;
import org.slb4j.ext.LogBuffer;
import org.slb4j.ext.fx.FxLogPane;
import org.slb4j.ext.fx.FxLogWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
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
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(FxLauncher.class);

    private static final Predicate<String> LOG_PREFIX_VALIDATOR =
            Pattern.compile("^[\\p{L}_$][\\p{L}\\p{N}_$]*+(?:\\.[\\p{L}_$][\\p{L}\\p{N}_$]*+)*+$")
                    .asMatchPredicate();

    /**
     * Exit code indicating successful execution of the application.
     */
    public static final int RC_SUCCESS = 0;

    /**
     * Exit code indicating that an error occurred during the execution of the application.
     */
    public static final int RC_ERROR = 1;

    private static final boolean HAS_SLB4J = LangUtil.isClassOnClasspath("org.slb4j.SLB4J");
    private static final boolean HAS_SLB4J_EXT = HAS_SLB4J && LangUtil.isClassOnClasspath("org.slb4j.ext.LogBuffer");

    private static final String I18N_KEY_LOG_MESSAGES = "dua3.utility.fx.launcher.log.messages";

    private static final Pattern PATTERN_PATH_OR_STARTS_WITH_DOUBLE_DASH = Pattern.compile("^(--|[a-zA-Z]:[/\\\\]).*");

    static @Nullable LogBuffer logBuffer = null;
    static @Nullable LoggingConfiguration loggingConfiguration = null;
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
                    LOG.debug("interrupted while waiting for platform startup", e);
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
            LOG.debug("arguments: {}", (Object) args);

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
        Logger log = LOG;
        log.trace("original arguments: {}", (Object) args);
        log.trace("re-parsed arguments: {}", argL);

        return argL;
    }

    /**
     * Launches an internationalized application with the specified configuration and parameters.
     *
     * @param applicationClassName The fully qualified class name of the main application to be launched.
     * @param args An array of command-line arguments passed to the application.
     * @param initI18n A function that initializes the internationalization (I18N) logic for the application, based on the provided locale.
     * @param appName The name of the application.
     * @param version The version of the application.
     * @param copyright The copyright information of the application.
     * @param developerMail The contact email address of the developer or support team.
     * @param appDescription A brief description of the application.
     * @param addOptions A varargs of consumers capable of adding additional options to the argument parser builder.
     * @return An integer exit code indicating the status of the application launch process. Typically, 0 indicates success, while non-zero values indicate failure.
     */
    @SafeVarargs
    public static int launchApplicationI18N(
            String applicationClassName,
            String[] args,
            Function<Locale, I18N> initI18n,
            String appName,
            String version,
            String copyright,
            String developerMail,
            String appDescription,
            Consumer<ArgumentsParserBuilder>... addOptions
    ) {
        return launchApplicationI18N(applicationClassName, args, initI18n, appName, version, copyright, developerMail, appDescription, Arrays.asList(addOptions));
    }

    /**
     * Launches an internationalized application with specified parameters and options.
     * <p>
     * This method sets the locale for the application based on the command line arguments,
     * initializes the internationalization (I18N) resources, extracts the text for the passed
     * I18N keys, and integrates optional command line arguments for displaying locale-related information.
     *
     * @param applicationClassName the fully qualified class name of the application to be launched
     * @param args the command line arguments passed to the application
     * @param initI18n A function that initializes the internationalization (I18N) logic for the application, based on the provided locale.
     * @param appNameI18nKey the I18N key for the name of the application
     * @param versionI18nKey the I18N key for the version of the application
     * @param copyrightI18nKey the I18N key for the copyright information for the application
     * @param developerMailI18nKey the I18N key for the developer's contact email
     * @param appDescriptionI18nKey the I18N key for a brief description of the application
     * @param addOptions a collection of additional options to be added for argument parsing
     * @return an integer status code from the application launch process; typically 0 for success
     */
    public static int launchApplicationI18N(
            String applicationClassName,
            String[] args,
            Function<Locale, I18N> initI18n,
            String appNameI18nKey,
            String versionI18nKey,
            String copyrightI18nKey,
            String developerMailI18nKey,
            String appDescriptionI18nKey,
            Collection<? extends Consumer<ArgumentsParserBuilder>> addOptions
    ) {
        // set the locale
        List<String> argList = Arrays.asList(args);
        int localeIdx = argList.indexOf("--locale");
        if (localeIdx >= 0 && localeIdx < args.length - 1) {
            String tag = args[localeIdx + 1];
            try {
                Locale locale = Locale.forLanguageTag(tag);
                Locale.setDefault(locale);
                LOG.info("setting locale to: {}", locale);
            } catch (Exception e) {
                LOG.warn("Invalid locale specified: {}", tag);
            }
        }

        // init I18N
        I18N i18n = initI18n.apply(Locale.getDefault());

        // add an option so that it shows up in the help output
        List<Consumer<ArgumentsParserBuilder>> addOptionsI18N = new ArrayList<>(addOptions);
        addOptionsI18N.add(agp ->
                agp.addStringOption(
                        I18NInstance.get().get("dua3.utility.fx.launcher.arg.locale.name"),
                        I18NInstance.get().get("dua3.utility.fx.launcher.arg.locale.description"),
                        Repetitions.ZERO_OR_ONE,
                        "language tag",
                        // the locale has already been set at method start, just give a diagnostic here
                        locale -> LOG.info("requested locale is: {}, effective locale is: {}", locale, Locale.getDefault()),
                        "--locale"
                )
        );

        return launchApplication(
                applicationClassName,
                args,
                i18n.get(appNameI18nKey),
                i18n.get(versionI18nKey),
                i18n.get(copyrightI18nKey),
                i18n.get(developerMailI18nKey),
                i18n.get(appDescriptionI18nKey),
                addOptionsI18N
        );
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
        return launchApplication(applicationClassName, args, appName, version, copyright, developerMail, appDescription, Arrays.asList(addOptions));
    }

    /**
     * Launches a JavaFX application with the specified configurations.
     *
     * @param applicationClassName The fully qualified class name of the JavaFX application to be launched.
     * @param args The command-line arguments to be passed to the application.
     * @param appName The name of the application.
     * @param version The version of the application.
     * @param copyright The copyright information for the application.
     * @param developerMail The developer's email address for support or inquiries.
     * @param appDescription A brief description of the application.
     * @param addOptions A collection of consumers for configuring additional command-line arguments.
     * @return The return code indicating the application's exit status. Typically returns {@code RC_SUCCESS} on successful execution or {@code RC_ERROR} in case of an exception.
     */
    public static int launchApplication(
            String applicationClassName,
            String[] args,
            String appName,
            String version,
            String copyright,
            String developerMail,
            String appDescription,
            Collection<? extends Consumer<ArgumentsParserBuilder>> addOptions
    ) {
        var agp = ArgumentsParser.builder()
                .name(appName)
                .description(I18NInstance.get().format("dua3.utility.fx.launcher.about.version", version) + "\n"
                        + I18NInstance.get().format("dua3.utility.fx.launcher.about.copyright", copyright, developerMail) + "\n"
                        + "\n"
                        + appDescription
                        + "\n"
                )
                .positionalArgs(0, 0);

        var flagHelp = agp.addFlag(
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.help.name"),
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.help.description"),
                "--help", "-h"
        );

        if (!Platform.isNativeImage()) {
            agp.addFlag(
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.assertions.name"),
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.assertions.description"),
                    v -> enableAssertions = v,
                    "--enable-assertions", "-ea"
            );
        }

        if (HAS_SLB4J) {
            addLoggingOptions(agp);
        }

        // add additional options
        addOptions.forEach(addOption -> addOption.accept(agp));

        // parse the arguments
        ArgumentsParser ap = agp.build(flagHelp);

        var arguments = ap.parse(args);

        if (arguments.isSet(flagHelp)) {
            //noinspection UseOfSystemOutOrSystemErr
            System.out.println(ap.help());
            return RC_SUCCESS;
        }

        arguments.handle();

        if (HAS_SLB4J) {
            configureLogging();
        }

        Logger log = LOG;
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
            showLogWindow(null, appName + " - " + I18NInstance.get().get(I18N_KEY_LOG_MESSAGES));
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
     * Configures the logging system for the application by setting the SLF4J logging configuration
     * and optionally enabling a log buffer for debugging purposes.
     * <p>
     * This method performs the following actions:
     * <ul>
     * <li>Updates the SLF4J logging configuration using the specified configuration object.
     * <li>Logs the updated logging configuration for debugging purposes.
     * <li>If either the `showLogWindow` or `debug` flag is enabled:
     *   <ul>
     *   <li>Initializes and assigns a `LogBuffer` instance to the `logBuffer` field if it is not already created.
     *   <li>Adds the `logBuffer` instance as a log handler using SLF4J's dispatcher.
     *   </ul>
     * </ul>
     */
    private static void configureLogging() {
        assert loggingConfiguration != null : "internal error: Logging configuration not initialized";

        SLB4J.setConfiguration(loggingConfiguration);
        LOG.debug("SLF4J configuration updated: {}", loggingConfiguration);

        if (showLogWindow || debug) {
            if (logBuffer == null) {
                logBuffer = new LogBuffer();
            }
            SLB4J.getDispatcher().addLogHandler(logBuffer);
        }
    }

    /**
     * Adds logging-related options to the given {@link ArgumentsParserBuilder}. This method configures
     * logging flags, string options for log levels, and options for log buffer sizes. These settings
     * allow fine-grained control over logging behavior and output for the application.
     *
     * @param agp the {@link ArgumentsParserBuilder} to which logging options will be added
     */
    private static void addLoggingOptions(ArgumentsParserBuilder agp) {
        loggingConfiguration = SLB4J.getConfiguration();
        agp.addFlag(
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.debug.name"),
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.debug.description"),
                v -> debug = v,
                "--debug"
        );
        agp.addStringOption(
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_level.name"),
                I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_level.description"),
                Repetitions.ZERO_OR_MORE,
                "rule",
                rule -> {
                    String[] parts = rule.split("=");
                    switch (parts.length) {
                        case 1 -> loggingConfiguration.setRootLevel(parseLogLevel(parts[0]));
                        case 2 -> {
                            LangUtil.check(LOG_PREFIX_VALIDATOR.test(parts[0]), "Not a valid logger name prefix: %s", parts[0]);
                            loggingConfiguration.getRootFilter().setLevel(parts[0], parseLogLevel(parts[1]));
                        }
                        default -> throw new IllegalStateException("Invalid log level rule format: " + rule);
                    }
                },
                "--log-level"
        );

        if (HAS_SLB4J_EXT) {
            agp.addFlag(
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_window.name"),
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_window.description"),
                    v -> showLogWindow = v,
                    "--log-window", "-lw"
            );
            agp.addIntegerOption(
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_buffer_size.name"),
                    I18NInstance.get().get("dua3.utility.fx.launcher.arg.log_buffer_size.description"),
                    Repetitions.ZERO_OR_ONE,
                    "size",
                    size -> logBuffer = new LogBuffer("Application Log Buffer", size),
                    "--log-buffer-size", "-ls"
            );
        }
    }

    /**
     * Parses a string representation of a log level and returns the corresponding {@code LogLevel} enum value.
     * If the provided string does not match any valid log level, an {@code IllegalStateException} is thrown.
     *
     * @param levelStr the string representation of the log level to parse
     * @return the {@code LogLevel} corresponding to the provided string
     * @throws IllegalStateException if the provided string does not match any valid log level
     */
    private static LogLevel parseLogLevel(String levelStr) {
        LogLevel level;
        try {
            level = LogLevel.valueOf(levelStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Not a valid log level: " + levelStr, e);
        }
        return level;
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
            if (HAS_SLB4J_EXT && logBuffer != null) {
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
        return showLogWindow(owner, I18NInstance.get().get(I18N_KEY_LOG_MESSAGES));
    }

    /**
     * Retrieves an instance of the {@link FxLogPane} if the log pane is configured to be displayed.
     *
     * @return an {@link Optional} containing the {@link FxLogPane} instance if the log pane is enabled,
     *         or an empty {@link Optional} if the log pane is disabled.
     */
    public static Optional<FxLogPane> getLogPane() {
        return PlatformGuard.run(() -> {
            if (HAS_SLB4J_EXT && debug) {
                return Optional.of(new FxLogPane(getLogBuffer().orElseThrow()));
            }
            return Optional.empty();

        });
    }
}
