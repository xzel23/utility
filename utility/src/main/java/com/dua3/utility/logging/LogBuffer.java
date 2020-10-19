package com.dua3.utility.logging;

import com.dua3.utility.lang.RingBuffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

public class LogBuffer extends RingBuffer<LogEntry> implements LogListener {

    public static final int DEFAULT_CAPACITY = 10000;

    public interface LogBufferListener {
        void entries(LogEntry[] entries, int removed);
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
        super(capacity);
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
    public int add(LogEntry... entries) {
        int added = super.add(entries);
        int removed = entries.length-added;
        listeners.forEach(listener -> listener.entries(entries, removed));
        return added;
    }

    @Override
    public void clear() {
        super.clear();
        listeners.forEach(LogBufferListener::clear);
    }

    @Override
    public void setCapacity(int n) {
        super.setCapacity(n);
        listeners.forEach(listener -> listener.capacity(n));
    }

    @Override
    public void entry(LogEntry entry) {
        add(entry);
    }
}
