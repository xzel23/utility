// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.lang.Platform;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Adjustable;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Utility methods for Swing applications.
 */
public final class SwingUtil {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(SwingUtil.class);

    // Utility class, should not be instantiated
    private SwingUtil() {
        // nop
    }

    /**
     * Record holding the scale factors for x and y.
     * @param x the x-scaling factor
     * @param y the y-scaling factor
     */
    public record Scale(float x, float y) {
        /**
         * Multiplies the current scale by another scale.
         *
         * @param other the scale to multiply with
         * @return a new Scale object with the multiplied scale factors
         */
        public Scale multiply(Scale other) {
            return new Scale(x*other.x, y*other.y);
        }

        /**
         * Multiplies the scale factors by the specified values and returns a new Scale object with the multiplied values.
         *
         * @param sx the scaling factor for the x-axis
         * @param sy the scaling factor for the y-axis
         * @return a new Scale object with the multiplied values
         */
        public Scale multiply(float sx, float sy) {
            return new Scale(x*sx, y*sy);
        }

        /**
         * Multiplies the scale factors of the current Scale object by a scalar value.
         *
         * @param s the scalar value to multiply the scale factors by
         * @return a new Scale object with the multiplied scale factors
         */
        public Scale multiply(float s) {
            return new Scale(x*s, y*s);
        }
    }

    /**
     * Retrieves the display scale of a given {@code Component}.
     *
     * @param component the component for which to retrieve the display scale
     * @return the display scale of the component
     */
    public static Scale getDisplayScale(Component component) {
        return getDisplayScale(component.getGraphicsConfiguration());
    }

    /**
     * Returns the display scale factor of the given GraphicsConfiguration.
     *
     * @param conf the GraphicsConfiguration to retrieve the display scale factor from
     * @return the display scale
     */
    public static Scale getDisplayScale(GraphicsConfiguration conf) {
        if (conf == null) {
            return new Scale(1, 1);
        }

        AffineTransform defaultTransform = conf.getDefaultTransform();
        return new Scale((float) defaultTransform.getScaleX(), (float) defaultTransform.getScaleY());
    }

    /**
     * Create an action to be used in menus.
     *
     * @param name              the name to display
     * @param onActionPerformed the Consumer that gets called when the Action is
     *                          invoked
     * @return new Action instance
     */
    public static Action createAction(String name, Consumer<? super ActionEvent> onActionPerformed) {
        return new AbstractAction(name) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                onActionPerformed.accept(evt);
            }

            @SuppressWarnings("UseOfClone")
            @Override
            public AbstractAction clone() throws CloneNotSupportedException {
                return (AbstractAction) super.clone();
            }
        };
    }

    /**
     * Create an action to be used in menus.
     *
     * @param name              the name to display
     * @param onActionPerformed the Runnable that gets called when the Action is
     *                          invoked
     * @return new Action instance
     */
    public static Action createAction(String name, Runnable onActionPerformed) {
        return new AbstractAction(name) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.run();
            }

            @SuppressWarnings("UseOfClone")
            @Override
            public AbstractAction clone() throws CloneNotSupportedException {
                //noinspection UseOfClone
                return (AbstractAction) super.clone();
            }
        };
    }

    /**
     * Scroll pane to bottom.
     *
     * @param sp a scroll pane
     */
    public static void scrollToBottom(JScrollPane sp) {
        scrollToEnd(sp.getVerticalScrollBar());
    }

    /**
     * Scroll scrollbar to end.
     *
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
     * Set clipboard content.
     *
     * @param text the text to set
     */
    public static void setClipboardText(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     * On macOS, the global menubar is also enabled.
     */
    public static void setNativeLookAndFeel() {
        setNativeLookAndFeel_(null);
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     * On macOS, the global menubar is also enabled.
     *
     * @param applicationName the application name to set
     */
    public static void setNativeLookAndFeel(String applicationName) {
        setNativeLookAndFeel_(applicationName);
    }

    private static void setNativeLookAndFeel_(@Nullable String applicationName) {
        if (Platform.isMacOS()) {
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
                 | IllegalAccessException e) {
            LOG.warn("could not set look&feel", e);
        }
    }

    /**
     * Show directory open dialog.
     *
     * @param parent  the parent component for the dialog
     * @param current the current file, it determines the folder shown when the
     *                dialog opens
     * @return Optional containing the path to the selected file.
     */
    public static Optional<Path> showDirectoryOpenDialog(Component parent, Path current) {
        return showOpenDialog(parent, current, JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * Show file open dialog.
     *
     * @param parent  the parent component for the dialog
     * @param current the current file, it determines the folder shown when the
     *                dialog opens
     * @param types   the selectable file name filters, given as pairs of
     *                description
     *                and one or more extensions
     * @return Optional containing the path to the selected file.
     */
    @SafeVarargs
    public static Optional<Path> showFileOpenDialog(Component parent, Path current, Pair<String, String[]>... types) {
        return showOpenDialog(parent, current, JFileChooser.FILES_ONLY, types);
    }

    /**
     * Show file save dialog.
     *
     * @param parent  the parent component for the dialog
     * @param current the current file, it determines the folder shown when the
     *                dialog opens
     * @param types   the selectable file name filters, given as pairs of
     *                description and one or more extensions
     * @return Optional containing the path to the selected file.
     */
    @SafeVarargs
    public static Optional<Path> showFileSaveDialog(Component parent, Path current, Pair<String, String[]>... types) {
        return showFileDialog(parent, current, JFileChooser.FILES_ONLY, JFileChooser::showSaveDialog, types);
    }

    /**
     * Convert java.awt.Color to Color.
     *
     * @param color java.awt.Color to be converted
     * @return Color
     */
    public static Color toColor(java.awt.Color color) {
        return new RGBColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Convert Color to java.awt.Color.
     *
     * @param color Color to be converted
     * @return java.awt.Color
     */
    public static java.awt.Color toAwtColor(Color color) {
        return new java.awt.Color(color.argb(), color.a() != 0xff);
    }

    /**
     * Convert String to java.awt.Color.
     *
     * @param s String to be converted
     * @return java.awt.Color
     * @see Color#valueOf(String)
     * @see #toAwtColor(Color)
     */
    public static java.awt.Color toAwtColor(String s) {
        return toAwtColor(Color.valueOf(s));
    }

    /**
     * Execute update and scroll to bottom.
     * <p>
     * If the JScrollPane given as parameter is scrolled to the bottom prior to
     * performing the update,
     * it will be scrolled to the bottom after executing the update. Otherwise, only
     * the update is performed.
     * </p>
     *
     * @param sp     a scroll pane
     * @param update Runnable to do the update
     */
    public static void updateAndScrollToBottom(JScrollPane sp, Runnable update) {
        updateAndScrollToEnd(sp.getVerticalScrollBar(), update);
    }

    /**
     * Execute update and scroll to end.
     * <p>
     * If the JScrollBar given as parameter is scrolled to the end prior to
     * performing the update,
     * it will be scrolled to the end again after executing the update. Otherwise,
     * only the update is performed.
     * </p>
     *
     * @param sb     a scroll bar
     * @param update Runnable to do the update
     */
    public static void updateAndScrollToEnd(JScrollBar sb, Runnable update) {
        boolean atEnd = sb.getMaximum() == sb.getValue() + sb.getVisibleAmount();

        update.run();

        if (atEnd) {
            scrollToEnd(sb);
        }
    }

    /**
     * Show open dialog.
     *
     * @param parent        the parent component for the dialog
     * @param current       the current path selected when the dialog opens
     * @param selectionMode the selection mode as used in {@link JFileChooser#setFileSelectionMode(int)}
     * @param types         pairs(description, list of extensions) used to create {@link FileNameExtensionFilter} instances
     * @return an Optional holding the selected path or an empty Optional if nothing was selected
     */
    @SafeVarargs
    public static Optional<Path> showOpenDialog(Component parent, Path current, int selectionMode,
                                                Pair<String, String[]>... types) {
        return showFileDialog(parent, current, selectionMode, JFileChooser::showOpenDialog, types);
    }

    @SafeVarargs
    private static Optional<Path> showFileDialog(Component parent, Path current, int selectionMode, BiFunction<? super JFileChooser, ? super Component, Integer> showDialog,
                                                 Pair<String, String[]>... types) {
        File file;
        try {
            file = current.toFile().getAbsoluteFile();
        } catch (UnsupportedOperationException | SecurityException e) {
            LOG.warn("path cannot be converted to file: {}", current, e);
            file = new File(".").getAbsoluteFile();
        }

        JFileChooser jfc = new JFileChooser();
        for (Pair<String, String[]> entry : types) {
            jfc.addChoosableFileFilter(new FileNameExtensionFilter(entry.first(), entry.second()));
        }

        jfc.setSelectedFile(file);
        jfc.setFileSelectionMode(selectionMode);

        int rc = showDialog.apply(jfc, parent);

        if (rc != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        Path path = jfc.getSelectedFile().toPath();

        return Optional.of(path);
    }

    /**
     * Set the unit increment.
     *
     * @param sb            the scroll bar, {@code null} is allowed
     * @param unitIncrement the unit increment
     */
    public static void setUnitIncrement(JScrollBar sb, int unitIncrement) {
        sb.setUnitIncrement(unitIncrement);
    }

    /**
     * Set the unit increment.
     *
     * @param jsp           the scroll pane
     * @param unitIncrement the unit increment
     */
    public static void setUnitIncrement(JScrollPane jsp, int unitIncrement) {
        setUnitIncrement(jsp.getHorizontalScrollBar(), unitIncrement);
        setUnitIncrement(jsp.getVerticalScrollBar(), unitIncrement);
    }

    /**
     * Create JScrollPane with unit increment set.
     *
     * @param unitIncrement the unit increment
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement) {
        JScrollPane jsp = new JScrollPane();
        setUnitIncrement(jsp, unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     *
     * @param unitIncrement the unit increment
     * @param view          the view
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, Component view) {
        JScrollPane jsp = new JScrollPane(view);
        setUnitIncrement(jsp, unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     *
     * @param unitIncrement the unit increment
     * @param vsbPolicy     vertical scroll bar policy
     * @param hsbPolicy     horizontal scroll bar policy
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, int vsbPolicy, int hsbPolicy) {
        JScrollPane jsp = new JScrollPane(vsbPolicy, hsbPolicy);
        setUnitIncrement(jsp, unitIncrement);
        return jsp;
    }

    /**
     * Create JScrollPane with unit increment set.
     *
     * @param unitIncrement the unit increment
     * @param view          the view
     * @param vsbPolicy     vertical scroll bar policy
     * @param hsbPolicy     horizontal scroll bar policy
     * @return new JScrollPane
     */
    public static JScrollPane createJScrollPane(int unitIncrement, Component view, int vsbPolicy, int hsbPolicy) {
        JScrollPane jsp = new JScrollPane(view, vsbPolicy, hsbPolicy);
        setUnitIncrement(jsp, unitIncrement);
        return jsp;
    }

    /**
     * Add support for dropping Files on a component.
     *
     * @param component the component to add drop support to
     * @param action    the action to perform when files are dropped
     */
    public static void addDropFilesSupport(JComponent component, Consumer<? super Collection<File>> action) {
        addDropFilesSupport(component, action, files -> !files.isEmpty(), e -> {
        });
    }

    /**
     * Add support for dropping Files on a component.
     *
     * @param component        the component to add drop support to
     * @param action           the action to perform when files are dropped
     * @param test             Predicate to decide whether dropping is allowed (should execute fast; called frequently during drag)
     * @param exceptionHandler handler to call when an exception is caught
     */
    public static void addDropFilesSupport(JComponent component, Consumer<? super Collection<File>> action, Predicate<? super Collection<File>> test, Consumer<? super Exception> exceptionHandler) {
        component.setDropTarget(new DropTarget() {
            @Override
            public synchronized void dragEnter(DropTargetDragEvent evt) {
                try {
                    if (test.test(getFiles(evt.getTransferable()))) {
                        evt.acceptDrag(DnDConstants.ACTION_COPY);
                    } else {
                        evt.rejectDrag();
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drag enter", e);
                    evt.rejectDrag();
                }
                super.dragEnter(evt);
            }

            @Override
            public synchronized void dragOver(DropTargetDragEvent evt) {
                try {
                    if (test.test(getFiles(evt.getTransferable()))) {
                        evt.acceptDrag(DnDConstants.ACTION_COPY);
                    } else {
                        evt.rejectDrag();
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drag over", e);
                    evt.rejectDrag();
                }
                super.dragOver(evt);
            }

            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Collection<File> files = getFiles(evt.getTransferable());
                    if (test.test(files)) {
                        action.accept(files);
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drop", e);
                }
            }

            @SuppressWarnings("unchecked")
            private Collection<File> getFiles(Transferable transferable) {
                try {
                    return (Collection<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    LOG.warn("exception getting data", e);
                    exceptionHandler.accept(e);
                    return Collections.emptyList();
                }
            }
        });
    }

    /**
     * Add support for dropping text on a component.
     *
     * @param component the component to add drop support to
     * @param action    the action to perform when text is dropped
     */
    public static void addDropTextSupport(JComponent component, Consumer<? super String> action) {
        addDropTextSupport(component, action, text -> !text.isEmpty(), e -> {
        });
    }

    /**
     * Add support for dropping text on a component.
     *
     * @param component        the component to add drop support to
     * @param action           the action to perform when files are dropped
     * @param test             Predicate to decide whether dropping is allowed (should execute fast; called frequently during drag)
     * @param exceptionHandler handler to call when an exception is caught
     */
    public static void addDropTextSupport(JComponent component, Consumer<? super String> action, Predicate<? super String> test, Consumer<? super Exception> exceptionHandler) {
        component.setDropTarget(new DropTarget() {
            @Override
            public synchronized void dragEnter(DropTargetDragEvent evt) {
                try {
                    if (test.test(getText(evt.getTransferable()))) {
                        evt.acceptDrag(DnDConstants.ACTION_COPY);
                    } else {
                        evt.rejectDrag();
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drag enter", e);
                    evt.rejectDrag();
                }

                super.dragEnter(evt);
            }

            @Override
            public synchronized void dragOver(DropTargetDragEvent evt) {
                try {
                    if (evt.getTransferable().isDataFlavorSupported(DataFlavor.stringFlavor) && test.test(getText(evt.getTransferable()))) {
                        evt.acceptDrag(DnDConstants.ACTION_COPY);
                    } else {
                        evt.rejectDrag();
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drag over", e);
                    evt.rejectDrag();
                }

                super.dragOver(evt);
            }

            @Override
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    String text = getText(evt.getTransferable());
                    if (test.test(text)) {
                        action.accept(text);
                    }
                } catch (Exception e) {
                    LOG.warn("exception on drop", e);
                }
            }

            private String getText(Transferable transferable) {
                try {
                    return (String) transferable.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    LOG.warn("exception getting data", e);
                    exceptionHandler.accept(e);
                    return "";
                }
            }
        });
    }

}
