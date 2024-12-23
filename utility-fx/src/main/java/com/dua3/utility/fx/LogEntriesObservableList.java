package com.dua3.utility.fx;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import javafx.application.Platform;
import javafx.collections.ObservableListBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a table model for displaying log entries in a Swing LogPane.
 */
final class LogEntriesObservableList extends ObservableListBase<LogEntry> implements LogBuffer.LogBufferListener {
    private static final Logger LOG = LogManager.getLogger(LogEntriesObservableList.class);

    private static final long REST_TIME_IN_MS = 10;

    private volatile List<LogEntry> data = Collections.emptyList();
    private final AtomicInteger queuedRemoves = new AtomicInteger();

    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final Lock updateWriteLock = updateLock.writeLock();
    private final Condition updatesAvailableCondition = updateWriteLock.newCondition();

    /**
     * Constructs a new LogTableModel with the specified LogBuffer.
     *
     * @param buffer the LogBuffer to use for storing log messages
     * @throws NullPointerException if the buffer is null
     */
    LogEntriesObservableList(LogBuffer buffer) {
        buffer.addLogBufferListener(this);

        Thread updateThread = new Thread(() -> {
            while (true) {
                updateWriteLock.lock();
                try {
                    updatesAvailableCondition.await();

                    List<LogEntry> newData = Arrays.asList(buffer.toArray());

                    Platform.runLater(() -> {
                    int newSz = newData.size();
                    int oldSz = data.size();
                    int removedRows = queuedRemoves.getAndSet(0);
                    int remainingRows = oldSz - removedRows;
                    int addedRows = newSz - remainingRows;

                        try {
                            beginChange();
                    List<LogEntry> removed = List.copyOf(data.subList(0, removedRows));
                            data = newData;
                            nextRemove(0, removed);
                            nextAdd(newSz - addedRows, newSz);
                        } finally {
                            endChange();
                        }
                    });
                } catch (InterruptedException e) {
                    LOG.debug("interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOG.warn("unexpected exception in update thread: {}", e.getMessage(), e);
                } finally {
                    updateWriteLock.unlock();
                }
                try {
                    //noinspection BusyWait - to avoid using up all CPU cycles when entries come in fast
                    Thread.sleep(REST_TIME_IN_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "LogEntriesObservableList Update Thread");
        updateThread.setPriority(Thread.NORM_PRIORITY - 1);
        updateThread.setDaemon(true);
        updateThread.start();
    }

    @Override
    public int size() {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        return data.size();
    }

    @Override
    public LogEntry get(int idx) {
        assert Platform.isFxApplicationThread() : "not on FX Application Thread";
        return data.get(idx);
    }

    @Override
    public void entries(int removed, int added) {
        updateWriteLock.lock();
        try {
            queuedRemoves.addAndGet(removed);
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

    @Override
    public void clear() {
        updateWriteLock.lock();
        try {
            queuedRemoves.set(data.size());
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

}
