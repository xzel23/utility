package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;
import org.jspecify.annotations.Nullable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A thread-safe log buffer class intended to provide a buffer for log messages
 * to display in GUI applications.
 *
 * <p>All operations are thread-safe. For compound operations requiring consistency
 * across multiple calls, use {@link #getBufferState()} to get an atomic snapshot.
 */
public class LogBuffer implements LogHandler, Externalizable {

    /**
     * The default capacity.
     */
    public static final int DEFAULT_CAPACITY = 10_000;

    private String name;
    private final RingBuffer<SimpleLogEntry> buffer;
    private final Collection<LogBufferListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong totalAdded = new AtomicLong(0);
    private final AtomicLong totalRemoved = new AtomicLong(0);
    private LogFilter filter = LogFilter.allPass();

    /**
     * Constructs a new LogBuffer instance with a default name "unnamed" and default capacity.
     * This constructor is needed for serialization.
     */
    public LogBuffer() {
        this("unnamed");
    }

    /**
     * Construct a new LogBuffer instance with default capacity.
     *
     * @param name the name of the buffer
     */
    public LogBuffer(String name) {
        this(name, DEFAULT_CAPACITY);
    }

    /**
     * Construct a new LogBuffer instance.
     *
     * @param name the name of the buffer
     * @param capacity the initial capacity
     */
    public LogBuffer(String name, int capacity) {
        this.name = name;
        this.buffer = new RingBuffer<>(capacity);
    }

    /**
     * Updates the capacity of the buffer while retaining the existing elements. If the new capacity is less than
     * the current size of the buffer, only the most recent elements within the new capacity are retained.
     *
     * @param n the new capacity for the buffer. Must be a non-negative integer.
     */
    public void setCapacity(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + n);
        }
        synchronized (buffer) {
            int oldSize = buffer.size();
            buffer.setCapacity(n);
            int removed = Math.max(0, oldSize - buffer.size());
            totalRemoved.addAndGet(removed);
            if (removed > 0) {
                listeners.forEach(listener -> listener.entries(removed, 0));
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        BufferState bufferState = getBufferState();
        out.writeInt(bufferState.entries.length);
        for (SimpleLogEntry entry : bufferState.entries) {
            // Serialize individual fields to avoid non-serializable components
            out.writeObject(entry.message());
            out.writeObject(entry.loggerName());
            out.writeObject(entry.time());
            out.writeObject(entry.level());
            out.writeObject(entry.marker());
            out.writeObject(entry.location());

            // Handle throwable carefully
            Throwable t = entry.throwable();
            if (t != null) {
                out.writeBoolean(true);
                out.writeObject(t.getClass().getName());
                out.writeObject(t.getMessage());
                // Serialize stack trace as string to avoid serialization issues
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    t.printStackTrace(pw);
                    out.writeObject(sw.toString());
                }
            } else {
                out.writeBoolean(false);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int n = in.readInt();
        List<SimpleLogEntry> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String message = (String) in.readObject();
            String loggerName = (String) in.readObject();
            Instant time = (Instant) in.readObject();
            LogLevel level = (LogLevel) in.readObject();
            String marker = (String) in.readObject();
            String location = (String) in.readObject();

            Throwable throwable = null;
            if (in.readBoolean()) {
                String throwableClass = (String) in.readObject();
                String throwableMessage = (String) in.readObject();
                String stackTrace = (String) in.readObject();
                // Create a simple throwable representation
                throwable = new RuntimeException(
                        "Deserialized " + throwableClass + ": " + throwableMessage +
                                "\nOriginal stack trace:\n" + stackTrace);
            }

            entries.add(new SimpleLogEntry(time, loggerName, level, marker, message, location, throwable));
        }

        // Update buffer state and notify listeners
        int removed;
        synchronized (buffer) {
            removed = buffer.size();
            buffer.clear();
            buffer.addAll(entries);
            totalAdded.set(entries.size());
            totalRemoved.set(0);
        }

        // Notify listeners about the state change
        if (removed > 0) {
            listeners.forEach(LogBufferListener::clear);
        }
        if (!entries.isEmpty()) {
            listeners.forEach(listener -> listener.entries(0, entries.size()));
        }
    }

    /**
     * Add LogBufferListener.
     *
     * @param listener the listener to add
     */
    public void addLogBufferListener(LogBufferListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove LogBufferListener.
     *
     * @param listener the listener to remove
     */
    public void removeLogBufferListener(LogBufferListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void handle(Instant instant, String loggerName, LogLevel lvl, String mrk, Supplier<String> msg, String location, @Nullable Throwable t) {
        SimpleLogEntry entry = new SimpleLogEntry(instant, loggerName, lvl, mrk, msg.get(), location, t);
        int removed;
        synchronized (buffer) {
            removed = buffer.put(entry) ? 0 : 1;
            totalAdded.incrementAndGet();
            totalRemoved.addAndGet(removed);
        }

        // Notify listeners outside the buffer synchronization to avoid deadlock
        listeners.forEach(listener -> listener.entries(removed, 1));
    }

    @Override
    public void setFilter(LogFilter filter) {
        this.filter = filter;
    }

    @Override
    public LogFilter getFilter() {
        return filter;
    }

    /**
     * Clear the LogBuffer.
     * Synchronized method that clears the buffer and notifies all registered LogBufferListeners to clear their logs as well.
     */
    public void clear() {
        synchronized (buffer) {
            totalRemoved.addAndGet(buffer.size());
            buffer.clear();
        }

        // Notify listeners outside the buffer synchronization to avoid deadlock
        listeners.forEach(LogBufferListener::clear);
    }

    /**
     * Converts the LogBuffer into an array of LogEntry objects.
     *
     * @return an array of LogEntry objects representing the contents of the LogBuffer
     */
    public SimpleLogEntry[] toArray() {
        synchronized (buffer) {
            return buffer.toArray(SimpleLogEntry[]::new);
        }
    }

    /**
     * Represents the state of a buffer.
     *
     * <p>This record is used to encapsulate the current state of a LogBuffer,
     * including its entries, the total number of log entries that have been
     * removed, and the total number of log entries that have been added.
     *
     * @param entries      the array of LogEntry objects currently in the buffer
     * @param totalRemoved the total count of log entries that have been removed from the buffer
     * @param totalAdded   the total count of log entries that have been added to the buffer
     */
    public record BufferState(SimpleLogEntry[] entries, long totalRemoved, long totalAdded) {
        @Override
        public boolean equals(@Nullable Object o) {
            if (!(o instanceof BufferState state)) {
                return false;
            }
            return totalRemoved == state.totalRemoved
                    && totalAdded == state.totalAdded
                    && java.util.Arrays.equals(entries, state.entries);
        }

        @Override
        public int hashCode() {
            int result = java.util.Arrays.hashCode(entries);
            result = 31 * result + Long.hashCode(totalRemoved);
            result = 17 * result + Long.hashCode(totalAdded);
            return result;
        }

        @Override
        public String toString() {
            return "BufferState[entries=" + java.util.Arrays.toString(entries) +
                    ", totalRemoved=" + totalRemoved +
                    ", totalAdded=" + totalAdded + "]";
        }

        /**
         * Calculates and retrieves the current sequence number of the buffer state.
         * The sequence number is determined by summing the total number of log entries
         * that have been added and removed from the buffer.
         *
         * @return the sequence number, calculated as the sum of totalAdded and totalRemoved values
         */
        public long getSequenceNumber() {
            return totalAdded + totalRemoved;
        }
    }

    /**
     * Retrieves the current state of the buffer, encapsulating the entries within the buffer,
     * the total number of entries removed, and the total number of entries added.
     * This method is thread-safe as it synchronizes on the buffer while performing operations.
     *
     * @return a {@code BufferState} instance containing the current buffer entries,
     *         total removed entries, and total added entries
     */
    public BufferState getBufferState() {
        synchronized (buffer) {
            SimpleLogEntry[] array = toArray();
            long r = totalRemoved.get();
            long a = totalAdded.get();
            assert array.length == a - r;
            return new BufferState(array, r, a);
        }
    }

    /**
     * Retrieves the sequence number of the log buffer. The sequence number is calculated
     * as the sum of the total added entries and the total removed entries in the buffer.
     * This method is thread-safe as it synchronizes on the buffer during execution.
     *
     * @return the calculated sequence number of the log buffer as a long value
     */
    public long getSequenceNumber() {
        synchronized (buffer) {
            return totalAdded.get() + totalRemoved.get();
        }
    }

    /**
     * Get the LogEntry at the specified index in the LogBuffer.
     *
     * @param i the index of the LogEntry to retrieve
     * @return the LogEntry at the specified index
     */
    public SimpleLogEntry get(int i) {
        synchronized (buffer) {
            return buffer.get(i);
        }
    }

    /**
     * Returns a view of the portion of this LogBuffer between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * @param fromIndex the index of the first LogEntry to be included in the
     *                  returned subList.
     * @param toIndex the index after the last LogEntry to be included in the
     *                returned subList.
     * @return a view of the specified range within this LogBuffer.
     * @throws IndexOutOfBoundsException if {@code fromIndex} or {@code toIndex} is
     *         out of range (fromIndex &lt; 0 || toIndex &gt; size() || fromIndex &gt; toIndex).
     */
    public List<SimpleLogEntry> subList(int fromIndex, int toIndex) {
        synchronized (buffer) {
            return new ArrayList<>(buffer.subList(fromIndex, toIndex));
        }
    }

    /**
     * Appends all LogEntries in this LogBuffer to the specified Appendable.
     *
     * @param app the Appendable to which the LogEntries will be appended
     * @throws IOException if an I/O error occurs while appending the LogEntries
     */
    public void appendTo(Appendable app) throws IOException {
        for (SimpleLogEntry entry : toArray()) {
            app.append(entry.toString()).append("\n");
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.logging.LogBuffer");}

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {throw new java.io.NotSerializableException("com.dua3.utility.logging.LogBuffer");}

    /**
     * Interface for Listeners on changes of a {@link LogBuffer} instance's contents.
     */
    public interface LogBufferListener {
        /**
         * Called when multiple entries have been added in a batch.
         *
         * @param removed the number of removed entries
         * @param added   the number added entries
         */
        void entries(int removed, int added);

        /**
         * Called after the buffer has been cleared.
         */
        void clear();
    }
}