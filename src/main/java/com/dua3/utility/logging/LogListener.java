package com.dua3.utility.logging;

import java.util.logging.LogRecord;

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
	 * @throws SecurityException
	 * @see java.util.logging.Handler#close()
	 */
	void close();
}
