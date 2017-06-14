package com.dua3.utility.swing;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SwingUtil {
    private static final Logger LOG = Logger.getLogger(SwingUtil.class.getName());

	public static void setNativeLookAndFeel() {
		if(System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "My app name");
            System.setProperty("apple.awt.application.name", "My app name");
            //Need for macos global menubar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
		}
		
        try {
            // Set system L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
	}
	
    public static Action createAction(String name, Consumer<ActionEvent> onActionPerformed) {
        return new AbstractAction(name) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.accept(e);
            }
        };
    }

    public static Action createAction(String name, Runnable onActionPerformed) {
        return new AbstractAction(name) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.run();
            }
        };
    }

}
