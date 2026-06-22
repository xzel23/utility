package com.dua3.utility.fx.controls;

import javafx.application.Platform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Large-file editing and layout microbenchmarks for {@link TextEditorPane}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@Threads(1)
@State(Scope.Benchmark)
public class TextEditorPaneLargeFileBenchmark {

    private static final int LINE_LENGTH = 72;
    private static final double WRAP_WIDTH = 920.0;

    @Param({"5000", "20000"})
    public int lineCount;

    @Param({"false", "true"})
    public boolean wrapText;

    private String largeText = "";
    private TextEditorPane editor;
    private int middlePos;
    private int nearEndPos;

    @Setup(Level.Trial)
    public void setupTrial() {
        FxJmhSupport.ensureStarted();
        largeText = createLargeDocument(lineCount, LINE_LENGTH);
        middlePos = Math.max(0, largeText.length() / 2);
        nearEndPos = Math.max(0, largeText.length() - 1);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        FxJmhSupport.runOnFxThreadAndWait(() -> {
            editor = new TextEditorPane(largeText);
            editor.setWrapText(wrapText);
            editor.positionCaret(middlePos);
            editor.buildVisualLines(wrapText ? WRAP_WIDTH : Double.POSITIVE_INFINITY);
        });
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        FxJmhSupport.shutdown();
    }

    @Benchmark
    public int replaceMiddleCharThenUndo() {
        return FxJmhSupport.callOnFxThreadAndWait(() -> {
            TextEditorPane textEditor = editor();
            textEditor.replaceText(middlePos, middlePos + 1, "X");
            textEditor.undo();
            return textEditor.getLength();
        });
    }

    @Benchmark
    public int insertNearEndThenUndo() {
        return FxJmhSupport.callOnFxThreadAndWait(() -> {
            TextEditorPane textEditor = editor();
            textEditor.replaceText(nearEndPos, nearEndPos, "Y");
            textEditor.undo();
            return textEditor.getLength();
        });
    }

    @Benchmark
    public int buildVisualLinesAfterSingleEdit() {
        return FxJmhSupport.callOnFxThreadAndWait(() -> {
            TextEditorPane textEditor = editor();
            textEditor.replaceText(middlePos, middlePos + 1, "Z");
            List<TextEditorPane.VisualLine> lines = textEditor.buildVisualLines(wrapText ? WRAP_WIDTH : Double.POSITIVE_INFINITY);
            textEditor.undo();
            return lines.size();
        });
    }

    @Benchmark
    public int buildVisualLinesCacheHit() {
        return FxJmhSupport.callOnFxThreadAndWait(() -> editor().buildVisualLines(wrapText ? WRAP_WIDTH : Double.POSITIVE_INFINITY).size());
    }

    private TextEditorPane editor() {
        return Objects.requireNonNull(editor, "editor not initialized");
    }

    private static String createLargeDocument(int lines, int lineLength) {
        StringBuilder sb = new StringBuilder(Math.max(lines * (lineLength + 1), 16));
        for (int i = 0; i < lines; i++) {
            int lineStart = sb.length();
            sb.append("line-").append(i).append(' ');
            while (sb.length() - lineStart < lineLength) {
                sb.append("alpha beta gamma delta ");
            }
            int overflow = (sb.length() - lineStart) - lineLength;
            if (overflow > 0) {
                sb.setLength(sb.length() - overflow);
            }
            if (i + 1 < lines) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static final class FxJmhSupport {
        private static final AtomicBoolean STARTED = new AtomicBoolean(false);

        private FxJmhSupport() {
        }

        static void ensureStarted() {
            if (STARTED.compareAndSet(false, true)) {
                CountDownLatch latch = new CountDownLatch(1);
                try {
                    Platform.startup(latch::countDown);
                } catch (IllegalStateException ex) {
                    // if the Platform is already running, start right now
                    latch.countDown();
                }
                await(latch);
            }
        }

        static void shutdown() {
            if (!STARTED.get()) {
                return;
            }

            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.runLater(() -> {
                    try {
                        Platform.exit();
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (IllegalStateException ex) {
                latch.countDown();
            }
            await(latch);
        }

        static void runOnFxThreadAndWait(Runnable action) {
            callOnFxThreadAndWait(() -> {
                action.run();
                return null;
            });
        }

        static <T> T callOnFxThreadAndWait(Supplier<T> action) {
            if (Platform.isFxApplicationThread()) {
                return action.get();
            }

            FutureTask<T> task = new FutureTask<>(action::get);
            Platform.runLater(task);
            try {
                return task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for FX task", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause() == null ? e : e.getCause();
                throw new IllegalStateException("Failed to execute FX task", cause);
            }
        }

        private static void await(CountDownLatch latch) {
            try {
                if (!latch.await(30, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Timed out while starting JavaFX toolkit");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while starting JavaFX toolkit", e);
            }
        }
    }
}
