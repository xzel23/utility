package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a table model for displaying log entries in a Swing LogPane.
 *
 * <p>The model uses an asynchronous update thread that processes buffer changes
 * and fires appropriate table events. Updates are batched and optimized using
 * sequence numbers to avoid unnecessary processing.
 */
final class LogTableModel extends AbstractTableModel implements LogBuffer.LogBufferListener {
    private static final Logger LOG = LogManager.getLogger(LogTableModel.class);
    private static final LogEntry[] EMPTY_LOG_ENTRIES = {};
    private static final int MAX_UPDATE_MILLISECONDS = 1000;

    private final AtomicReference<LogEntry[]> data = new AtomicReference<>(EMPTY_LOG_ENTRIES);
    private final AtomicInteger queuedRemoves = new AtomicInteger();

    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final Lock updateReadLock = updateLock.readLock();
    private final Lock updateWriteLock = updateLock.writeLock();
    private final Condition updatesAvailableCondition = updateWriteLock.newCondition();

    private volatile boolean shutdown = false;

    /**
     * Constructs a new LogTableModel with the specified LogBuffer.
     *
     * @param buffer the LogBuffer to use for storing log messages
     * @throws NullPointerException if the buffer is null
     */
    LogTableModel(LogBuffer buffer) {
        buffer.addLogBufferListener(this);

        Thread updateThread = new Thread(() -> {
            long sequence = 0L;
            while (!shutdown) {
                updateWriteLock.lock();
                try {
                    boolean hasUpdates = updatesAvailableCondition.await(MAX_UPDATE_MILLISECONDS, TimeUnit.MILLISECONDS);
                    if (!hasUpdates) {
                        if (sequence == buffer.getSequenceNumber()) {
                            LOG.debug("no updates available");
                            continue;
                        } else {
                            LOG.debug("updates detected after timeout");
                        }
                    }

                    LogBuffer.BufferState bufferState = buffer.getBufferState();
                    LogEntry[] bufferArray = bufferState.entries();
                    sequence = bufferState.getSequenceNumber();
                    int oldSz = data.getAndSet(bufferArray).length;
                    int sz = bufferArray.length;
                    int remove = queuedRemoves.getAndSet(0);

                    if (remove > 0) {
                        fireTableRowsDeleted(0, remove - 1);
                    }
                    if (sz > oldSz - remove) {
                        fireTableRowsInserted(oldSz - remove, sz - 1);
                    }
                } catch (InterruptedException e) {
                    LOG.debug("interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOG.warn("unexpected exception in update thread: {}", e.getMessage(), e);
                } finally {
                    updateWriteLock.unlock();
                }
            }
        }, "LogTableModel Update Thread");
        updateThread.setDaemon(true);
        updateThread.start();
    }


    @Override
    public int getRowCount() {
        return data.get().length;
    }

    @Override
    public int getColumnCount() {
        return SwingLogPane.COLUMNS.length;
    }

    @Override
    public LogEntry getValueAt(int rowIndex, int columnIndex) {
        return data.get()[rowIndex];
    }

    @Override
    public String getColumnName(int column) {
        return SwingLogPane.COLUMNS[column].field().name();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return SwingLogPane.LogEntryField.class;
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
            queuedRemoves.set(data.get().length);
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

    /**
     * Executes a read task using the updateReadLock to ensure thread-safe access.
     * The provided {@code readTask} is run within a lock to prevent data race
     * conditions or concurrent modification issues during read operations.
     *
     * @param readTask the task to be executed. This should be a {@link Runnable}
     *                 that encapsulates the code to perform read operations.
     *                 Cannot be null.
     * @throws NullPointerException if {@code readTask} is null
     */
    public void executeRead(Runnable readTask) {
        updateReadLock.lock();
        try {
            readTask.run();
        } finally {
            updateReadLock.unlock();
        }
    }

    /**
     * Initiates the shutdown process for the LogTableModel. This sets the internal shutdown
     * flag to true and signals any waiting threads that updates are no longer available.
     * This method ensures proper handling of thread-safe operations using a lock to avoid
     * concurrent modification issues.
     *
     * Thread safety is achieved by acquiring a write lock before modifying the condition
     * variable and releasing it after signaling all waiting threads.
     */
    public void shutdown() {
        shutdown = true;
        updateWriteLock.lock();
        try {
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.swing.LogTableModel");}

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {throw new java.io.NotSerializableException("com.dua3.utility.swing.LogTableModel");}
}
