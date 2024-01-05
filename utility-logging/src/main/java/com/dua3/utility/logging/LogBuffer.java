package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A log buffer class intended to provide a buffer for log messages to display in GUI applications.
 */
public class LogBuffer implements LogEntryHandler, Externalizable {

    /**
     * The default capacity.
     */
    public static final int DEFAULT_CAPACITY = 10_000;

    private final RingBuffer<LogEntry> buffer;
    private final Collection<LogBufferListener> listeners = new ArrayList<>();

    /**
     * Construct a new LogBuffer instance with default capacity.
     */
    public LogBuffer() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Construct a new LogBuffer instance.
     *
     * @param capacity the initial capacity
     */
    public LogBuffer(int capacity) {
        buffer = new RingBuffer<>(capacity);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Object[] entries = buffer.toArray();
        out.write(entries.length);
        for (Object entry : entries) {
            out.writeObject(entry);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        buffer.clear();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            buffer.add((LogEntry) in.readObject());
        }
    }

    /**
     * Add LogBufferListener.
     *
     * @param listener the listener to add
     */
    public void addLogBufferListener(LogBufferListener listener) {
        listeners.add(Objects.requireNonNull(listener));
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
    public void handleEntry(LogEntry entry) {
        boolean replaced;
        synchronized (buffer) {
            int oldSize = buffer.size();
            buffer.add(entry);
            replaced = buffer.size() == oldSize;
        }
        listeners.forEach(listener -> listener.entry(entry, replaced));
    }

    /**
     * Clear the LogBuffer.
     * Synchronized method that clears the buffer and notifies all registered LogBufferListeners to clear their logs as well.
     */
    public void clear() {
        synchronized (buffer) {
            buffer.clear();
        }
        listeners.forEach(LogBufferListener::clear);
    }

    /**
     * Get the List of LogEntries in the LogBuffer.
     * Synchronized method that returns a List of LogEntries from the buffer.
     *
     * @return a List of LogEntries in the LogBuffer
     */
    public List<LogEntry> entries() {
        return List.of(toArray());
    }

    /**
     * Converts the LogBuffer into an array of LogEntry objects.
     *
     * @return an array of LogEntry objects representing the contents of the LogBuffer
     */
    public LogEntry[] toArray() {
        synchronized (buffer) {
            return buffer.toArray(LogEntry[]::new);
        }
    }

    /**
     * Get the LogEntry at the specified index in the LogBuffer.
     *
     * @param i the index of the LogEntry to retrieve
     * @return the LogEntry at the specified index
     */
    public LogEntry get(int i) {
        return buffer.get(i);
    }

    /**
     * Returns the size of the LogBuffer.
     *
     * @return the number of LogEntries in the LogBuffer.
     */
    public int size() {
        return buffer.size();
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
    public List<LogEntry> subList(int fromIndex, int toIndex) {
        synchronized (buffer) {
            return new ArrayList<>(buffer.subList(fromIndex, toIndex));
        }
    }

    /**
     * Returns a List containing all the LogEntries in this LogBuffer.
     *
     * @return a List containing all the LogEntries in this LogBuffer.
     */
    public List<LogEntry> getLogEntries() {
        synchronized (buffer) {
            return new ArrayList<>(buffer);
        }
    }

    /**
     * Appends all LogEntries in this LogBuffer to the specified Appendable.
     *
     * @param app the Appendable to which the LogEntries will be appended
     * @throws IOException if an I/O error occurs while appending the LogEntries
     */
    public void appendTo(Appendable app) throws IOException {
        for (LogEntry entry : getLogEntries()) {
            app.append(entry.toString()).append("\n");
        }
    }

    /**
     * Interface for Listeners on changes of a {@link LogBuffer} instance's contents.
     */
    public interface LogBufferListener {
        /**
         * Called when an entry was added to the buffer.
         *
         * @param entry    the added entry
         * @param replaced true, if the buffer's capacity was reached and another entry was removed to make space for
         *                 the new one
         */
        default void entry(LogEntry entry, boolean replaced) {
            entries(Collections.singleton(entry), replaced ? 1 : 0);
        }

        /**
         * Called when multiple entries have been added in a batch.
         *
         * @param entries  the added entries
         * @param replaced the number of replaced entries as described in {@link #entry(LogEntry, boolean)}
         */
        void entries(Collection<LogEntry> entries, int replaced);

        /**
         * Called after the buffer has been cleared.
         */
        void clear();
    }
}
