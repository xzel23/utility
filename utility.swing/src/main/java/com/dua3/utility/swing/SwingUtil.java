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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.font.FontRenderContext;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;
import com.dua3.utility.text.Font;

/**
 * Utility methods for Swing applications.
 */
public class SwingUtil {

    /**
     * Create an action to be used in menus.
     *
     * @param name
     *            the name to display
     * @param onActionPerformed
     *            the Consumer that gets called when the Action is invoked
     * @return
     *         new Action instance
     */
    public static Action createAction(String name, Consumer<ActionEvent> onActionPerformed) {
        return new AbstractAction(name) {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.accept(e);
            }
        };
    }

    /**
     * Create an action to be used in menus.
     *
     * @param name
     *            the name to display
     * @param onActionPerformed
     *            the Runnable that gets called when the Action is invoked
     * @return
     *         new Action instance
     */
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
     * Scroll scrollpane to bottom.
     *
     * @param sp
     *            a scroll pane
     */
    public static void scrollToBottom(JScrollPane sp) {
        scrollToEnd(sp.getVerticalScrollBar());
    }

    /**
     * Scroll scrollbar to end.
     *
     * @param sb
     *            a scroll bar
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
     * Set clipboard content.
     *
     * @param text
     *            the text to set
     */
    public static void setClipboardText(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
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
     *            the application name to set
     */
    public static void setNativeLookAndFeel(String applicationName) {
        if (System.getProperty("os.name").toUpperCase().startsWith("MAC")) {
            if (applicationName != null) {
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
                System.setProperty("apple.awt.application.name", applicationName);
            }
            // Need for macos global menubar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        try {
            // Set system L&F
            String lafName = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lafName);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException ex) {
            java.util.logging.Logger.getGlobal().warning("Could not set Look&Feel.");
        }
    }

    /**
     * Show directory open dialog.
     *
     * @param parent
     *            the parent component for the dialog
     * @param current
     *            the current file, it determines the folder shown when the dialog opens
     * @return
     *         Optional containing the path to the selected file.
     */
    public static Optional<Path> showDirectoryOpenDialog(Component parent, Path current) {
        return showOpenDialog(parent, current, JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * Show file open dialog.
     *
     * @param parent
     *            the parent component for the dialog
     * @param current
     *            the current file, it determines the folder shown when the dialog opens
     * @param types
     *            the choosable file name filters, given as pairs of description and one or more extensions
     * @return
     *         Optional containing the path to the selected file.
     */
    @SafeVarargs
    public static Optional<Path> showFileOpenDialog(Component parent, Path current, Pair<String, String[]>... types) {
        return showOpenDialog(parent, current, JFileChooser.FILES_ONLY, types);
    }

    /**
     * Convert java.awt.Color to Color.
     *
     * @param color
     *         java.awt.Color to be converted
     * @return
     *         Color
     */
    public static Color toColor(java.awt.Color color) {
        return new Color(color.getRed(),color.getGreen(),color.getBlue(), color.getAlpha());
    }

    /**
     * Convert Color to java.awt.Color.
     *
     * @param color
     *            Color to be converted
     * @return
     *         java.awt.Color
     */
    public static java.awt.Color toAwtColor(Color color) {
        return new java.awt.Color(color.argb(), color.a()!=0xff);
    }

    /**
     * Convert String to java.awt.Color.
     *
     * @param s
     *            String to be converted
     * @return
     *         java.awt.Color
     * @see Color#valueOf(String)
     * @see #toAwtColor(Color)
     */
    public static java.awt.Color toAwtColor(String s) {
        return toAwtColor(Color.valueOf(s));
    }

    /**
     * Execute update and scroll to bottom.
     * <p>
     * If the JScrollPane given as parameter is scrolled to the bottom prior to performing the update,
     * it will be scrolled to the bottom after executing the update. Otherwise, only the update is performed.
     * </p>
     *
     * @param sp
     *            a scroll pane
     * @param update
     *            Runnable to do the update
     */
    public static void updateAndScrollToBottom(JScrollPane sp, Runnable update) {
        updateAndScrollToEnd(sp.getVerticalScrollBar(), update);
    }

    /**
     * Execute update and scroll to end.
     * <p>
     * If the JScrollBar given as parameter is scrolled to the end prior to performing the update,
     * it will be scrolled to the end again after executing the update. Otherwise, only the update is performed.
     * </p>
     *
     * @param sb
     *            a scroll bar
     * @param update
     *            Runnable to do the update
     */
    public static void updateAndScrollToEnd(JScrollBar sb, Runnable update) {
        boolean atEnd = sb.getMaximum() == sb.getValue() + sb.getVisibleAmount();

        update.run();

        if (atEnd) {
            scrollToEnd(sb);
        }
    }

    @SafeVarargs
    public static Optional<Path> showOpenDialog(Component parent, Path current, int selectionMode,
            Pair<String, String[]>... types) {
        File file = null;
        if (current != null) {
            try {
                file = current.toFile().getAbsoluteFile();
            } catch (UnsupportedOperationException e) {
                java.util.logging.Logger.getGlobal().warning("path cannot be converted to file: " + current);
                file = new File(".").getAbsoluteFile();
            }
        }

        JFileChooser jfc = new JFileChooser();
        for (Pair<String, String[]> entry : types) {
            jfc.addChoosableFileFilter(new FileNameExtensionFilter(entry.first, entry.second));
        }

        jfc.setSelectedFile(file);
        jfc.setFileSelectionMode(selectionMode);

        int rc = jfc.showOpenDialog(parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        Path path = jfc.getSelectedFile().toPath();

        return Optional.of(path);
    }

    /**
     * Set the unit increment.
     * @param sb the scroll bar, {@code null} is allowed
     * @param unitIncrement the unit increment
     */
    public static void setUnitIncrement(JScrollBar sb, int unitIncrement) {
        if (sb != null) {
            sb.setUnitIncrement(unitIncrement);
        }
    }

    /**
     * Set the unit increment.
     * @param jsp the scroll pane
     * @param unitIncrement the unit increment
     */
    public static void setUnitIncrement(JScrollPane jsp, int unitIncrement) {
        if (jsp==null) {
            return;
        }

        setUnitIncrement(jsp.getHorizontalScrollBar(), unitIncrement);
        setUnitIncrement(jsp.getVerticalScrollBar(), unitIncrement);
    }

    /**
     * Create JScrollPane with unit increment set.
     * @param unitIncrement the unit increment
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement) {
        JScrollPane jsp = new JScrollPane();
        setUnitIncrement(jsp,  unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     * @param unitIncrement the unit increment
     * @param view the view
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, Component view) {
        JScrollPane jsp = new JScrollPane(view);
        setUnitIncrement(jsp,  unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     * @param unitIncrement the unit increment
     * @param vsbPolicy vertical scroll bar policy
     * @param hsbPolicy horizontal scroll bar policy
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, int vsbPolicy, int hsbPolicy) {
        JScrollPane jsp = new JScrollPane(vsbPolicy, hsbPolicy);
        setUnitIncrement(jsp,  unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     * @param unitIncrement the unit increment
     * @param view the view
     * @param vsbPolicy vertical scroll bar policy
     * @param hsbPolicy horizontal scroll bar policy
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, Component view, int vsbPolicy, int hsbPolicy) {
        JScrollPane jsp = new JScrollPane(view, vsbPolicy, hsbPolicy);
        setUnitIncrement(jsp,  unitIncrement);
        return jsp;
    }

    /**
     * Get text width.
     * 
     * @param text
     *  the text
     * 
     * @param font
     *  the font
     * 
     * @return
     *  the text width
     */
    public static float getTextWidth(String text, Font font) {
        java.awt.Font awtFont = getAwtFont(
            font.getFamily(), 
            font.getSizeInPoints(), 
            font.getColor(), 
            font.isBold(), 
            font.isItalic(), 
            font.isUnderlined(), 
            font.isStrikeThrough());
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return (float) awtFont.getStringBounds(text, frc).getWidth();
    }

    /**
     * Get awt font for font. 
     * 
     * @param font
     *  the Font
     * @return
     *  corresponding java.awt.Font
     */
    public static java.awt.Font getAwtFont(Font font) {
        return getAwtFont(
            font.getFamily(), 
            font.getSizeInPoints(), 
            font.getColor(), 
            font.isBold(), 
            font.isItalic(), 
            font.isUnderlined(), 
            font.isStrikeThrough());
    }

    private static java.awt.Font getAwtFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
        boolean strikeThrough) {
      int style = (bold ? java.awt.Font.BOLD : 0) | (italic ? java.awt.Font.ITALIC : 0);
      return new java.awt.Font(family, style, Math.round(size));
    }


    // Utility class, should not be instantiated
    private SwingUtil() {
        // nop
    }
}