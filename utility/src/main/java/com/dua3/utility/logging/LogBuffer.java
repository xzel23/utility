package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A log buffer class intended to provide a buffer for log messages to display in GUI applications.
 */
public class LogBuffer implements LogListener {

    /** The default capacity. */
    public static final int DEFAULT_CAPACITY = 10000;
    
    private static final List<Function<LogEntry, Object>> DEFAULT_FORMAT_PARTS = Arrays.asList(
            LogEntry::time,
            e -> " ",
            LogEntry::category,
            e -> "\n",
            LogEntry::message,
            e -> (e.cause().map(cause -> "\n"+cause.format()).orElse("")),
            e -> "\n"
    );

    private final RingBuffer<LogEntry> buffer;

    /**
     * Interface for Listeners on changes of a {@link LogBuffer} instance's contents.
     */
    public interface LogBufferListener {
        /**
         * Called when an entry was added to the buffer.
         * @param entry the added entry
         * @param replaced true, if the buffer's capacity was reached and another entry was removed to make space for
         *                the new one
         */
        default void entry(LogEntry entry, boolean replaced) {
            entries(Collections.singleton(entry), replaced ? 1 : 0);    
        }

        /**
         * Called when multiple entries have been added in a batch. 
         * @param entries the added entries
         * @param replaced the number of replaced entries as described in {@link #entry(LogEntry, boolean)}
         */
        void entries(Collection<LogEntry> entries, int replaced);

        /**
         * Called after the buffer has been cleared.
         */
        void clear();
    }
    
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

    /**
     * Add LogBufferListener.
     * @param listener the listener to add
     */
    public void addLogBufferListener(LogBufferListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    /**
     * Remove LogBufferListener.
     * @param listener the listener to remove
     */
    public void removeLogBufferListener(LogBufferListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void entry(LogEntry entry) {
        boolean replaced;
        synchronized(buffer) {
            int oldSize = buffer.size();
            buffer.add(entry);
            replaced = buffer.size()==oldSize;
        }
        listeners.forEach(listener -> listener.entry(entry, replaced));
    }

   public void clear() {
        synchronized(buffer) {
            buffer.clear();
        }
        listeners.forEach(LogBufferListener::clear);
    }

    public List<LogEntry> entries() {
        synchronized(buffer) {
            return Arrays.asList(buffer.toArray(LogEntry[]::new));
        }
    }

    public LogEntry get(int i) {
        return buffer.get(i);
    }
    
    public int size() {
        return buffer.size();
    }
    
    public List<LogEntry> subList(int fromIndex, int toIndex) {
        synchronized(buffer) {
            return new ArrayList<>(buffer.subList(fromIndex, toIndex));
        }
    }
    
    public List<LogEntry> getLogEntries() {
        synchronized (buffer) {
            return new ArrayList<>(buffer);
        }
    }
    
    public void appendTo(Appendable app, Iterable<? extends Function<LogEntry, Object>> parts) throws IOException {
        for (LogEntry entry: getLogEntries()) {
            for (Function<LogEntry, Object> p: parts) {
                app.append(String.valueOf(p.apply(entry)));
            }
        }
    }

    @SafeVarargs
    public final void appendTo(Appendable app, Function<LogEntry, Object>... parts) throws IOException {
        appendTo(app, Arrays.asList(parts));
    }
    
    public void appendTo(Appendable app) throws IOException {
        appendTo(app, DEFAULT_FORMAT_PARTS);
    }
}
