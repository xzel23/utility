package com.dua3.utility.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A dispatcher class for LogRecords.
 * <p>
 * This class enables classes that implement the {@link LogListener} interface to act as
 * {@link Handler}. The dispatcher class is needed because {@link Handler} is an abstract class
 * and not an interface. 
 */
public class LogDispatcher extends Handler {

	private final LogListener listener;

	/**
	 * Create a new instance that forwards all method calls to a {@link LogListener}.
	 * @param listener the listener
	 */
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
	public void close() {
		listener.close();
	}

}
