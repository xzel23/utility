package com.dua3.utility.samples.fx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.UiMode;
import com.dua3.utility.fx.FxLogPane;
import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.logging.LogFilter;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import com.dua3.utility.math.MathUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * This class demonstrates the use of {@link FxLogPane} to display log messages in a window at runtime.
 */
@SuppressWarnings({"ClassWithMultipleLoggers", "UseOfSystemOutOrSystemErr"})
public class FxLogPaneSample extends Application {

    static {
        // this has to be done before the first logger is initialized!
        LogUtil.initUnifiedLogging();
        if (LogUtil.getGlobalDispatcher().getFilter() instanceof LogFilter filter) {
            filter.setLevel("", LogLevel.TRACE);
            filter.setLevel("java", LogLevel.ERROR);
            filter.setLevel("javax", LogLevel.ERROR);
            filter.setLevel("javafx", LogLevel.ERROR);
            filter.setLevel("sun", LogLevel.ERROR);
        }

        ApplicationUtil.setUiMode(UiMode.SYSTEM_DEFAULT);
    }

    private static final int AVERAGE_SLEEP_MILLIS = 5;
    private static final int LOG_BUFFER_SIZE = 10000;
    private static final org.slf4j.Logger SLF4J_LOGGER = LoggerFactory.getLogger("SLF4J." + FxLogPaneSample.class.getName());
    private static final Log JCL_LOGGER = LogFactory.getLog("JCL." + FxLogPaneSample.class.getName());
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger("JUL." + FxLogPaneSample.class.getName());
    private static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger("LOG4J." + FxLogPaneSample.class.getName());
    private final FxLogPane logPane;
    private final SecureRandom random = new SecureRandom();
    private final AtomicInteger n = new AtomicInteger();


    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public FxLogPaneSample() {
        logPane = new FxLogPane(LOG_BUFFER_SIZE);

        setDarkMode(ApplicationUtil.isDarkMode());
        ApplicationUtil.addDarkModeListener(this::setDarkMode);
    }

    private void setDarkMode(boolean enabled) {
        Supplier<Theme> themeSupplier = enabled ? PrimerDark::new : PrimerLight::new;
        PlatformHelper.runAndWait(() -> setUserAgentStylesheet(themeSupplier.get().getUserAgentStylesheet()));
    }

    @Override
    public void start(Stage primaryStage) {
        int width = 1280;
        Scene scene = new Scene(logPane, width, width / MathUtil.GOLDEN_RATIO);

        primaryStage.setTitle(getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.show();

        startLoggingThreads();
    }

    @SuppressWarnings("BusyWait")
    private void startLoggingThreads() {
        // start threads
        final int numberOfImplementations = 4;
        for (final int implementation : IntStream.range(0, numberOfImplementations).toArray()) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                while (true) {
                    if (AVERAGE_SLEEP_MILLIS > 0) {
                        long wait = random.nextLong(2L * AVERAGE_SLEEP_MILLIS * numberOfImplementations);
                        try {
                            Thread.sleep(wait);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    int nr = n.incrementAndGet();

                    int bound = switch (implementation) {
                        case 1, 3 -> 6;
                        default -> 5;
                    };

                    int levelInt = random.nextInt(bound);
                    LogLevel level = LogLevel.values()[implementation == 1 || implementation == 3 ? Math.max(0, levelInt - 1) : levelInt];

                    String msg = "Message #%d, imp %s, original integer level %d, level %s".formatted(nr, implementation, levelInt, level);

                    switch (implementation) {
                        case 0 -> {
                            switch (levelInt) {
                                case 0 -> SLF4J_LOGGER.trace(msg);
                                case 1 -> SLF4J_LOGGER.debug(msg);
                                case 2 -> SLF4J_LOGGER.info(msg);
                                case 3 -> SLF4J_LOGGER.warn(msg);
                                case 4 -> SLF4J_LOGGER.error(msg, generateThrowable());
                                default -> throw new IllegalStateException("integer out of range");
                            }
                        }
                        case 1 -> {
                            switch (levelInt) {
                                case 0 -> JUL_LOGGER.finest(msg);
                                case 1 -> JUL_LOGGER.finer(msg);
                                case 2 -> JUL_LOGGER.fine(msg);
                                case 3 -> JUL_LOGGER.info(msg);
                                case 4 -> JUL_LOGGER.warning(msg);
                                case 5 -> JUL_LOGGER.log(java.util.logging.Level.SEVERE, msg, generateThrowable());
                                default -> throw new IllegalStateException("integer out of range");
                            }
                        }
                        case 2 -> {
                            switch (levelInt) {
                                case 0 -> LOG4J_LOGGER.trace(msg);
                                case 1 -> LOG4J_LOGGER.debug(msg);
                                case 2 -> LOG4J_LOGGER.info(msg);
                                case 3 -> LOG4J_LOGGER.warn(msg);
                                case 4 -> LOG4J_LOGGER.error(msg, generateThrowable());
                                default -> throw new IllegalStateException("integer out of range");
                            }
                        }
                        case 3 -> {
                            switch (levelInt) {
                                case 0 -> JCL_LOGGER.trace(msg);
                                case 1 -> JCL_LOGGER.debug(msg);
                                case 2 -> JCL_LOGGER.info(msg);
                                case 3 -> JCL_LOGGER.warn(msg);
                                case 4 -> JCL_LOGGER.error(msg);
                                case 5 -> JCL_LOGGER.fatal(msg, generateThrowable());
                                default -> throw new IllegalStateException("integer out of range");
                            }
                        }
                        default -> throw new IllegalStateException("integer out of range");
                    }

                    int current = n.get();
                    if (current % 100 == 0) {
                        System.err.format("That was %d messages%n", current);
                    } else if (current % 10 == 0) {
                        System.out.format("That was %d messages%n", current);
                    }
                }
            }, "Logger-Thread-" + implementation);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private IllegalStateException generateThrowable() {
        if (random.nextBoolean()) {
            return new IllegalStateException("Why?", new UnsupportedOperationException("Because of me!"));
        } else {
            return new IllegalStateException("What happened?");
        }
    }

}
