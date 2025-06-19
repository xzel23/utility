package com.dua3.utility.samples;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.options.ArgumentsParserBuilder;
import com.dua3.utility.swing.ArgumentsDialog;
import com.dua3.utility.swing.ComboBoxEx;
import com.dua3.utility.swing.SwingLogPane;
import com.dua3.utility.swing.SwingProgressView;
import com.dua3.utility.swing.SwingUtil;
import net.miginfocom.swing.MigLayout;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An abstract base class for demonstrating logging using various frameworks in a Swing application.
 * This class extends {@link JFrame} and initializes multiple logging frameworks (SLF4J, JUL, LOG4J).
 * It also sets up and manages the Swing UI components, including a ComboBox with custom objects,
 * a progress view, and a log pane.
 */
@SuppressWarnings({"ClassWithMultipleLoggers", "BusyWait", "NonConstantLogger", "UseOfSystemOutOrSystemErr"})
public abstract class SwingComponentsSampleLogBase extends JFrame {

    private static final String TASK_INDETERMINATE_1 = "Indeterminate Task";
    private static final String TASK_INDETERMINATE_2 = "Another Indeterminate Task";
    private static final int AVERAGE_SLEEP_MILLIS = 10;
    private static final int LOG_BUFFER_SIZE = 1000;

    /**
     * The SLF4J logger.
     */
    private final transient org.slf4j.Logger slf4JLogger = LoggerFactory.getLogger("SLF4J." + getClass().getName());
    /**
     * The JUL logger.
     */
    private final transient java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("JUL." + getClass().getName());

    /**
     * The Log4J2 logger.
     */
    private final transient org.apache.logging.log4j.Logger log4JLogger = org.apache.logging.log4j.LogManager.getLogger("LOG4J." + getClass().getName());

    /**
     * An AtomicInteger to count the number of messages sent to the various logging frameworks.
     */
    private final AtomicInteger n = new AtomicInteger();

    /**
     * A flag to indicate that the application should terminate.
     */
    private volatile boolean done;

    /**
     * Starts the Swing application by setting the native look and feel and
     * creating an instance of SwingComponentsSampleLogBase using a provided factory.
     *
     * @param factory A supplier that provides an instance of SwingComponentsSampleLogBase.
     * @param args Command-line arguments passed to the application.
     */
    public static void start(Supplier<? extends SwingComponentsSampleLogBase> factory, String[] args) {
        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComponentsSampleLogBase instance = factory.get();
            instance.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    /**
     * Constructs a new instance of SwingComponentsSampleLogBase.
     * <p>
     * This constructor initializes the frame layout using the MigLayout manager,
     * defines the minimum size of the frame, and sets its initial size. Additionally,
     * it calls the {@code init()} method to set up the frame's components and
     * functionalities.
     */
    protected SwingComponentsSampleLogBase() {
        setLayout(new MigLayout("fill", "[grow,fill]", "[][][grow,fill]"));
        setMinimumSize(new Dimension(400, 400));
        setSize(800, 600);

        init();
    }

    static class Person {
        String firstName;
        String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        @Override
        public String toString() {
            return Stream.of(lastName, firstName).filter(Objects::nonNull).collect(Collectors.joining(", "));
        }
    }

    private Person showPersonDialog() {
        return showPersonDialog(null);
    }

    private Person showPersonDialog(Person initialPerson) {
        Optional<Person> op = Optional.ofNullable(initialPerson);
        ArgumentsParserBuilder builder = ArgumentsParser.builder();
        var optFirstName = builder.option(
                        "First name",
                        "The person's first name.",
                        String.class
                )
                .defaultSupplier(() -> op.map(pp -> pp.firstName).orElse(""))
                .build("--first-name");
        var optLastName = builder.option(
                        "Last name",
                        "The person's last or family name.",
                        String.class
                )
                .defaultSupplier(() -> op.map(pp -> pp.lastName).orElse(""))
                .build("--last-name");
        ArgumentsParser parser = builder.build();
        return ArgumentsDialog.showDialog(this, parser)
                .map(args -> {
                    Person p = new Person("", "");
                    p.firstName = args.get(optFirstName).orElse("");
                    p.lastName = args.get(optLastName).orElse("");
                    return p;
                })
                .orElse(null);
    }

    private void init() {

        // -- ComboboxEx
        ComboBoxEx<Person> comboBoxEx = new ComboBoxEx<>(
                this::showPersonDialog,
                this::showPersonDialog,
                ComboBoxEx::askBeforeRemoveSelectedItem,
                Object::toString,
                new Person("John", "Doe"),
                new Person("Jane", "Doe"),
                new Person("Baby", "Doe"),
                new Person("Richard", "Roe"),
                new Person("Jeanny", "Roe")
        );
        comboBoxEx.setComparator(Comparator.comparing(Person::toString));

        // -- Spacer
        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        separator1.setMinimumSize(new Dimension(8, 8));

        // -- SwingProcessView
        SwingProgressView<Object> progress = new SwingProgressView<>();
        int max = 100;

        Map<LogLevel, Integer> counter = new EnumMap<>(LogLevel.class);
        Arrays.stream(LogLevel.values()).forEach(lvl -> {
            counter.put(lvl, 0);
            progress.start(lvl);
        });
        progress.start(TASK_INDETERMINATE_1);

        // -- Spacer
        JSeparator separator2 = new JSeparator(SwingConstants.HORIZONTAL);
        separator2.setMinimumSize(new Dimension(8, 8));

        // -- SwingLogPane
        SwingLogPane logPane = new SwingLogPane(LOG_BUFFER_SIZE);

        // add components
        add(comboBoxEx, "wrap");
        add(separator1, "grow x, wrap");
        add(progress, "wrap");
        add(separator2, "grow x, wrap");
        add(logPane);

        // start threads
        final int numberOfImplementations = 3;
        for (final int implementation : IntStream.range(0, numberOfImplementations).toArray()) {
            Thread.ofVirtual()
                    .name("Logger-Thread-" + implementation)
                    .start(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        while (!done) {
                            long wait = ThreadLocalRandom.current().nextLong(2L * AVERAGE_SLEEP_MILLIS * numberOfImplementations);
                            try {
                                Thread.sleep(wait);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            int nr = n.incrementAndGet();

                            int bound = implementation == 1 ? 6 : 5;
                            int levelInt = ThreadLocalRandom.current().nextInt(bound);
                            LogLevel level = LogLevel.values()[implementation == 1 ? Math.max(0, levelInt - 1) : levelInt];

                            String msg = "Message #%d, imp %s, original integer level %d, level %s".formatted(nr, implementation, levelInt, level);

                            switch (implementation) {
                                case 0 -> {
                                    switch (levelInt) {
                                        case 0 -> slf4JLogger.trace(msg);
                                        case 1 -> slf4JLogger.debug(msg);
                                        case 2 -> slf4JLogger.info(msg);
                                        case 3 -> slf4JLogger.warn(msg);
                                        case 4 -> slf4JLogger.error(msg, generateThrowable());
                                        default -> throw new IllegalStateException("integer out of range");
                                    }
                                }
                                case 1 -> {
                                    switch (levelInt) {
                                        case 0 -> julLogger.finest(msg);
                                        case 1 -> julLogger.finer(msg);
                                        case 2 -> julLogger.fine(msg);
                                        case 3 -> julLogger.info(msg);
                                        case 4 -> julLogger.warning(msg);
                                        case 5 -> julLogger.log(java.util.logging.Level.SEVERE, msg, generateThrowable());
                                        default -> throw new IllegalStateException("integer out of range");
                                    }
                                }
                                case 2 -> {
                                    switch (levelInt) {
                                        case 0 -> log4JLogger.trace(msg);
                                        case 1 -> log4JLogger.debug(msg);
                                        case 2 -> log4JLogger.info(msg);
                                        case 3 -> log4JLogger.warn(msg);
                                        case 4 -> log4JLogger.error(msg, generateThrowable());
                                        default -> throw new IllegalStateException("integer out of range");
                                    }
                                }
                                default -> throw new IllegalStateException("integer out of range");
                            }

                            Integer v = counter.compute(level, (lvl, old) -> old != null && old < max ? old + 1 : null);
                            if (v != null) {
                                if (v < max) {
                                    progress.update(level, max, v);
                                } else {
                                    ProgressTracker.State s = switch (level) {
                                        case INFO, DEBUG, TRACE, WARN -> ProgressTracker.State.COMPLETED_SUCCESS;
                                        case ERROR -> ProgressTracker.State.COMPLETED_FAILURE;
                                    };
                                    progress.finish(level, s);
                                }

                            }

                            int current = n.get();
                            if (current % 100 == 0) {
                                System.err.format("That was %d messages%n", current);
                            } else if (current % 10 == 0) {
                                System.out.format("That was %d messages%n", current);
                            }
                        }
                    });
        }

        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            progress.finish(TASK_INDETERMINATE_1, ProgressTracker.State.COMPLETED_SUCCESS);

            progress.start(TASK_INDETERMINATE_2);
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            progress.finish(TASK_INDETERMINATE_2, ProgressTracker.State.COMPLETED_SUCCESS);
        });
    }

    private static IllegalStateException generateThrowable() {
        if (ThreadLocalRandom.current().nextBoolean()) {
            return new IllegalStateException("Why?", new UnsupportedOperationException("Because of me!"));
        } else {
            return new IllegalStateException("What happened?");
        }
    }

    @Override
    public void dispose() {
        done = true;
        super.dispose();
    }
}
