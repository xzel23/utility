package com.dua3.utility.logging;

import java.util.logging.Logger;

import javax.swing.JFrame;

import com.dua3.utility.logging.LogWindow;

public class LogWindowTest {

	private static Logger LOG = Logger.getLogger(LogWindowTest.class.getSimpleName());
	
	public static void main(String[] args) {
		LOG.info("Creating window");
		LogWindow w = new LogWindow();
		w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		w.addLogger(LOG);
		w.setSize(600, 400);
		w.setVisible(true);
		
		LOG.info("initialized.");
		LOG.warning("Is everything alright?");
		LOG.severe("I dunno.");
		LOG.fine("lalalala.");
	}
	
}
