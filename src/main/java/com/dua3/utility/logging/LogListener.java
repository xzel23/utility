package com.dua3.utility.logging;

import java.util.logging.LogRecord;

/**
 * An interface intended to be used as replacement for {@link java.util.logging.Handler}.
 * @see LogDispatcher
 */
public interface LogListener {
	/**
	 * Publish a log record.
	 * @param record the record to publish
	 * @see java.util.logging.Handler#publish(LogRecord)
	 */
	void publish(LogRecord record);

	/**
	 * Flush log records.
	 * @see java.util.logging.Handler#flush()
	 */
	void flush();

	/**
	 * Close.
	 * @see java.util.logging.Handler#close()
	 */
	void close();
}
