package com.dua3.utility.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogDispatcher extends Handler {

	private final LogListener listener;

	public LogDispatcher(LogListener listener) {
		this.listener=listener;
	}

	@Override
	public void publish(LogRecord record) {
		listener.publish(record);
	}

	@Override
	public void flush() {
		listener.flush();
	}

	@Override
	public void close() throws SecurityException {
		listener.close();
	}

}
