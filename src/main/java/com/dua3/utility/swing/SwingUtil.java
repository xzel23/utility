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

import java.awt.Adjustable;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwingUtil.class);

    // Utility class, should not be instantiated
    private SwingUtil() {
        // nop
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     */
    public static void setNativeLookAndFeel() {
        setNativeLookAndFeel(null);
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     *
     * On Mac OS, the global menubar is also enabled.
     *
     * @param applicationName
     *  the application name to set
     */
	public static void setNativeLookAndFeel(String applicationName) {
		if(System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
			LOGGER.info("enabling global menu");
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
			LOGGER.info("setting L&F to {}", lafName);
			UIManager.setLookAndFeel(lafName);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            LOGGER.warn("Could not set Look&Feel.", ex);
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

    /**
     * Scroll scrollbar to end.
     * @param sb a scroll bar
     */
    public static void scrollToEnd(JScrollBar sb) {
        AdjustmentListener autoScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                sb.removeAdjustmentListener(this);
            }
        };
        sb.addAdjustmentListener(autoScroller);
    }
    
    /**
     * Execute update and scroll to end.
     * <p>
     * If the JScrollBar given as parameter is scrolled to the end prior to performing the update,
     * it will be scrolled to the end again after executing the update. Otherwise, only the update is performed.
     * </p>
     * @param sb a scroll bar
     * @param update Runnable to do the update
     */
    public static void updateAndScrollToEnd(JScrollBar sb, Runnable update) {
    		boolean atEnd = sb.getMaximum () == sb.getValue () + sb.getVisibleAmount ();
    		
    		update.run();
    		
    		if (atEnd) {
    			scrollToEnd(sb);
    		}
    }    
    
    /**
     * Scroll scrollpane to bottom.
     * @param sp a scroll pane
     */
    public static void scrollToBottom(JScrollPane sp) {
    		scrollToEnd(sp.getVerticalScrollBar());
    }
    
    /**
     * Execute update and scroll to bottom.
     * <p>
     * If the JScrollPane given as parameter is scrolled to the bottom prior to performing the update,
     * it will be scrolled to the bottom after executing the update. Otherwise, only the update is performed.
     * </p>
     * @param sp a scroll pane
     * @param update Runnable to do the update
     */
    public static void updateAndScrollToBottom(JScrollPane sp, Runnable update) {
    		updateAndScrollToEnd(sp.getVerticalScrollBar(), update);
    }

}