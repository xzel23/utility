/*
 * Copyright 2017 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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

    // Utility class, should not be instantiated
    private SwingUtil() {
        // nop
    }

    /**
     * Set the Swing Look&Feel to the native Look&Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     */
    public static void setNativeLookAndFeel() {
        setNativeLookAndFeel(null);
    }

    /**
     * Set the Swing Look&Feel to the native Look&Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     *
     * @param applicationName
     *  the application name to set
     */
	public static void setNativeLookAndFeel(String applicationName) {
		if(System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
			LOG.info("enabling global menu");
		    if (applicationName!=null) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
                System.setProperty("apple.awt.application.name", applicationName);
		    }
            //Need for macos global menubar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
		}

        try {
            // Set system L&F
            String lafName = UIManager.getSystemLookAndFeelClassName();
			LOG.log(Level.INFO, "setting L&F to {}", lafName);
			UIManager.setLookAndFeel(lafName);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
	}

    /**
     * Set clipboard content.
     *
     * @param text
     *  the text to set
     */
    public static void setClipboardText(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
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
