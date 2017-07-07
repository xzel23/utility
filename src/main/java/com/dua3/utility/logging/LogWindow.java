package com.dua3.utility.logging;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.JFrame;

public class LogWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private final LogPanel logPanel;

	public LogWindow() {
		this("Log");
	}

	public LogWindow(String title, Logger... loggers) {
		super(title);
		setLayout(new BorderLayout());
		logPanel = new LogPanel(loggers);
		add(logPanel, BorderLayout.CENTER);
	}

	public void addLogger(Logger logger) {
		logPanel.addLogger(logger);
	}

}
