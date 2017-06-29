package com.dua3.utility.logging;

import java.awt.BorderLayout;
import java.awt.Color;
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

	LogPanel() {
		super();
		setLayout(new BorderLayout());

		textPane = new JTextPane();
		add(textPane);
	}

	@Override
	public void publish(LogRecord record) {
		StyledDocument doc = textPane.getStyledDocument();

		Style style = textPane.addStyle("Color Style", null);
		Color color = Color.BLUE;
		StyleConstants.setForeground(style, color);
		try {
			String text = record.toString();
			doc.insertString(doc.getLength(), text, style);
		} catch (BadLocationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub

	}

}
