package com.dua3.utility.samples;

import com.dua3.utility.concurrent.ProgressTracker;
import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.options.ArgumentsParser;
import com.dua3.utility.swing.ArgumentsDialog;
import com.dua3.utility.swing.ComboBoxEx;
import com.dua3.utility.swing.SwingLogPane;
import com.dua3.utility.swing.SwingProgressView;
import com.dua3.utility.swing.SwingUtil;
import net.miginfocom.swing.MigLayout;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.event.Level;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"ClassWithMultipleLoggers", "BusyWait"})
public class SwingComponentsSample extends JFrame {

    public static final String TASK_INDETERMINATE_1 = "Indeterminate Task";
    public static final String TASK_INDETERMINATE_2 = "Another Indeterminate Task";
    public static final int SLEEP_MILLIS = 25;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("SLF4J." + SwingComponentsSample.class.getName());
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger("JUL." + SwingComponentsSample.class.getName());
    private static final org.apache.logging.log4j.Logger LOG4J_LOGGER = org.apache.logging.log4j.LogManager.getLogger("LOG4J." + SwingComponentsSample.class.getName());

    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    @SuppressWarnings("UnsecureRandomNumberGeneration") // used only to create a random sequence of log levels in tests
    private final Random random = new Random();
    private final AtomicInteger n = new AtomicInteger();
    private volatile boolean done;

    public static void main(String[] args) {
        LOG.info("starting up");

        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComponentsSample instance = new SwingComponentsSample();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }

    public SwingComponentsSample() {
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
        ArgumentsParser parser = new ArgumentsParser();
        var optFirstName = parser.simpleOption(String.class, "firstName").displayName("First Name").defaultValue(op.map(pp -> pp.firstName).orElse(""));
        var optLastName = parser.simpleOption(String.class, "lastName").displayName("Last Name").defaultValue(op.map(pp -> pp.lastName).orElse(""));
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
        JSeparator separator1 = new JSeparator(JSeparator.HORIZONTAL);
        separator1.setMinimumSize(new Dimension(8, 8));

        // -- SwingProcessView
        SwingProgressView<Object> progress = new SwingProgressView<>();
        int max = 100;

        HashMap<Level, Integer> counter = new HashMap<>();
        Arrays.stream(Level.values()).forEach(lvl -> {
            counter.put(lvl, 0);
            progress.start(lvl);
        });
        progress.start(TASK_INDETERMINATE_1);

        // -- Spacer
        JSeparator separator2 = new JSeparator(JSeparator.HORIZONTAL);
        separator2.setMinimumSize(new Dimension(8, 8));

        // -- SwingLogPane

        // setup logging
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (!(loggerFactory instanceof com.dua3.utility.logging.LoggerFactory)) {
            throw new IllegalStateException("wrong logging implementation!");
        }
        LogBuffer buffer = ((com.dua3.utility.logging.LoggerFactory) loggerFactory).getLogBuffer()
                .orElseThrow(() -> new IllegalStateException("buffer not configured"));

        // create the log pane
        SwingLogPane logPane = new SwingLogPane(buffer);

        // add components
        add(comboBoxEx, "wrap");
        add(separator1, "grow x, wrap");
        add(progress, "wrap");
        add(separator2, "grow x, wrap");
        add(logPane);

        // start threads
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            while (!done) {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                int nr = n.incrementAndGet();
                String msg = "Message " + nr + ".";

                int implementation = random.nextInt(3);
                int bound = implementation == 1 ? 6 : 5;
                int levelInt = random.nextInt(bound);
                Level level = Level.values()[implementation == 1 ? Math.max(0, levelInt - 1) : levelInt];

                switch (implementation) {
                    case 0 -> {
                        switch (levelInt) {
                            case 0 -> LOG.trace(msg);
                            case 1 -> LOG.debug(msg);
                            case 2 -> LOG.info(msg);
                            case 3 -> LOG.warn(msg);
                            case 4 -> LOG.error(msg, generateThrowable());
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
        thread.start();

        Thread thread2 = new Thread(() -> {
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
        thread2.start();
    }

    private IllegalStateException generateThrowable() {
        if (random.nextBoolean()) {
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