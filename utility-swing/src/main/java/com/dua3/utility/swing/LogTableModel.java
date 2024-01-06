package com.dua3.utility.swing;

import com.dua3.utility.logging.LogBuffer;
import com.dua3.utility.logging.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class LogTableModel extends AbstractTableModel implements LogBuffer.LogBufferListener {
    private static final Logger LOG = LogManager.getLogger(LogTableModel.class);

    private final LogBuffer buffer;
    private volatile LogEntry[] data = new LogEntry[0];
    private final AtomicInteger queuedRemoves = new AtomicInteger();
    private final AtomicInteger queuedAdds = new AtomicInteger();
    private volatile boolean done = false;

    private final ReentrantLock snapshotLock = new ReentrantLock();
    private final ReadWriteLock updateLock = new ReentrantReadWriteLock();
    private final Lock updateReadLock = updateLock.readLock();
    private final Lock updateWriteLock = updateLock.writeLock();
    private final Condition updatesAvailableCondition = updateWriteLock.newCondition();

    LogTableModel(LogBuffer buffer) {
        this.buffer = buffer;
        buffer.addLogBufferListener(this);

        Thread updateThread = new Thread(() -> {
            while (!done) {
                try {
                    waitForUpdate();
                    sync();
                } catch (InterruptedException e) {
                    LOG.debug("interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOG.warn("unexpected exception in update thread: {}", e.getMessage(), e);
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
    public void entries(Collection<LogEntry> entries, int replaced) {
        updateWriteLock.lock();
        try {
            queuedRemoves.addAndGet(replaced);
            queuedAdds.addAndGet(entries.size());
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
            queuedAdds.set(0);
            updatesAvailableCondition.signalAll();
        } finally {
            updateWriteLock.unlock();
        }
    }

    public void waitForUpdate() throws InterruptedException {
        updateWriteLock.lock();
        try {
            while (queuedAdds.get() == 0 && queuedRemoves.get() == 0) {
                updatesAvailableCondition.await();
            }
        } finally {
            updateWriteLock.unlock();
        }
    }

    public void executeRead(Runnable readTask) {
        snapshotLock.lock();
        try {
            readTask.run();
        } finally {
            snapshotLock.unlock();
        }
    }

    private void sync() {
        snapshotLock.lock();
        try {
            int sz = data.length;
            int add;
            int remove;
            updateReadLock.lock();
            try {
                data = buffer.toArray();
                add = queuedAdds.getAndSet(0);
                remove = queuedRemoves.getAndSet(0);
            } finally {
                updateReadLock.unlock();
            }

            if (remove > 0) {
                fireTableRowsDeleted(0, Math.max(0, remove - 1));
                sz -= remove;
            }

            if (add > 0) {
                fireTableRowsInserted(Math.max(0, sz - add), Math.max(0, sz - 1));
            }
        } finally {
            snapshotLock.unlock();
        }
    }
}
