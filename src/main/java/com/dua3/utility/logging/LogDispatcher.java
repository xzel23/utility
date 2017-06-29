package com.dua3.utility.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogDispatcher extends Handler {

	private List<LogListener> listeners = new LinkedList<>();;

	public LogDispatcher() {
		// nop
	}

	public LogDispatcher(LogListener listener) {
		listeners.add(Objects.requireNonNull(listener));
	}

	@Override
	public void publish(LogRecord record) {
		listeners.stream().forEach(listener -> listener.publish(record));
	}

	@Override
	public void flush() {
		listeners.stream().forEach(LogListener::flush);
	}

	@Override
	public void close() throws SecurityException {
		listeners.stream().forEach(LogListener::close);
	}

	public void addListener(LogListener listener) {
		listeners.add(Objects.requireNonNull(listener));
	}

	public void removeListener(LogListener listener) {
		listeners.remove(Objects.requireNonNull(listener));
	}

}
