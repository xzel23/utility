package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;

import java.util.*;

public class LogBuffer implements LogListener {

    public static final int DEFAULT_CAPACITY = 10000;

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
}
