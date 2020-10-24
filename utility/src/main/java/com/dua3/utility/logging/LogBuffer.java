package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class LogBuffer implements LogListener {

    public static final int DEFAULT_CAPACITY = 10000;
    
    private static final List<Function<LogEntry, Object>> DEFAULT_FORMAT_PARTS = Arrays.asList(
            LogEntry::time,
            e -> " ",
            LogEntry::category,
            e -> "\n",
            LogEntry::message,
            e -> (e.cause().map(cause -> "\n"+cause.toString()).orElse("")),
            e -> "\n"
    );

    private final RingBuffer<LogEntry> buffer;

    public interface LogBufferListener {
        default void entry(LogEntry entry, boolean replaced) {
            entries(Collections.singleton(entry), replaced ? 1 : 0);    
        }
        void entries(Collection<LogEntry> entries, int replaced);
        void clear();
        void capacity(int n);
    }
    
    private final Collection<LogBufferListener> listeners = new LinkedList<>();

    /**
     * Construct a new LogBuffer instance eith default capacity.
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
            return Arrays.asList(buffer.toArray(new LogEntry[0]));
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
    
    public void appendTo(Appendable app, Iterable<Function<LogEntry,Object>> parts) throws IOException {
        for (LogEntry entry: getLogEntries()) {
            for (Function<LogEntry, Object> p: parts) {
                app.append(String.valueOf(p.apply(entry)));
            }
        }
    }

    public void appendTo(Appendable app, Function<LogEntry,Object>... parts) throws IOException {
        appendTo(app, Arrays.asList(parts));
    }
    
    public void appendTo(Appendable app) throws IOException {
        appendTo(app, DEFAULT_FORMAT_PARTS);
    }
}
