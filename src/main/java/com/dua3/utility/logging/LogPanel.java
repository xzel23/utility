package com.dua3.utility.logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.time.Instant;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LogPanel extends JPanel implements LogListener {

	private static final long serialVersionUID = 1L;
	private final JTextPane textPane;
	private final LogDispatcher dispatcher;

	class MessageStyle {
		public MessageStyle(Style style) {
			this.styleMessage = style;
			this.styleLevel = style;
			this.styleTimestamp = style;
		}

		Style styleMessage;
		Style styleTimestamp;
		Style styleLevel;
	}

	private final TreeMap<Integer, MessageStyle> styles = new TreeMap<>();

	LogPanel() {
		super();
		setLayout(new BorderLayout());

		textPane = new JTextPane();
		add(textPane);

		initStyles();

		dispatcher = new LogDispatcher(this);
	}

	@Override
	public void publish(LogRecord record) {
		Level level = record.getLevel();

		MessageStyle ms = getStyle(level);
		StyledDocument doc = textPane.getStyledDocument();

		String time = Instant.ofEpochMilli(record.getMillis()).toString() + ": ";
		String levelStr = level.getName() + " - ";
		String message = record.getMessage() + "\n";

		try {
			doc.insertString(doc.getLength(), time, ms.styleTimestamp);
			doc.insertString(doc.getLength(), levelStr, ms.styleLevel);
			doc.insertString(doc.getLength(), message, ms.styleMessage);
		} catch (BadLocationException e) {
			throw new IllegalStateException(e);
		}
	}

	private MessageStyle getStyle(Level level) {
		Entry<Integer, MessageStyle> entry = styles.ceilingEntry(level.intValue());
		return entry == null ? styles.lastEntry().getValue() : entry.getValue();
	}

	private MessageStyle createStyle(Level level, Color color) {
		String name = level.getName();

		Style style = textPane.addStyle(name, null);
		StyleConstants.setForeground(style, color);

		MessageStyle ms = new MessageStyle(style);
		styles.put(level.intValue(), ms);

		return ms;
	}

	private void initStyles() {
		createStyle(Level.SEVERE, Color.RED);
		createStyle(Level.WARNING, Color.RED);
		createStyle(Level.INFO, Color.BLUE);
		createStyle(Level.CONFIG, Color.BLACK);
		createStyle(Level.FINE, Color.BLACK);
		createStyle(Level.FINER, Color.BLACK);
		createStyle(Level.FINEST, Color.BLACK);
	}

	@Override
	public void flush() {
		// nop

	}

	@Override
	public void close() throws SecurityException {
		// nop
	}

	public Handler getHandler() {
		return dispatcher;
	}

}
