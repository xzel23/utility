package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class represents a table model for displaying log entries in a Swing LogPane.
 */
final class LogTableModel extends AbstractTableModel implements LogBuffer.LogBufferListener {
    private static final Logger LOG = LogManager.getLogger(LogTableModel.class);
    public static final LogEntry[] EMPTY_LOG_ENTRIES = {};

    private final LogBuffer buffer;
    @SuppressWarnings("VolatileArrayField")
    private volatile LogEntry[] data = EMPTY_LOG_ENTRIES;
    private final AtomicInteger queuedRemoves = new AtomicInteger();

    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final Lock updateReadLock = updateLock.readLock();
    private final Lock updateWriteLock = updateLock.writeLock();
    private final Condition updatesAvailableCondition = updateWriteLock.newCondition();

    /**
     * Constructs a new LogTableModel with the specified LogBuffer.
     *
     * @param buffer the LogBuffer to use for storing log messages
     * @throws NullPointerException if the buffer is null
     */
    LogTableModel(LogBuffer buffer) {
        this.buffer = buffer;
        buffer.addLogBufferListener(this);

        Thread updateThread = new Thread(() -> {
            while (true) {
                updateWriteLock.lock();
                try {
                    updatesAvailableCondition.await();

                    int oldSz = data.length;
                    data = this.buffer.toArray();
                    int sz = data.length;
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
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return SwingLogPane.COLUMNS.length;
    }

    @Override
    public LogEntry getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex];
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
            queuedRemoves.set(data.length);
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

    /**
     *
     */
    public void executeRead(Runnable readTask) {
        updateReadLock.lock();
        try {
            readTask.run();
        } finally {
            updateReadLock.unlock();
        }
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.swing.LogTableModel");}

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {throw new java.io.NotSerializableException("com.dua3.utility.swing.LogTableModel");}
}
