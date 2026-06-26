// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.ClosePath2f;
import com.dua3.utility.math.geometry.Curve2f;
import com.dua3.utility.math.geometry.Line2f;
import com.dua3.utility.math.geometry.MoveTo2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.HtmlConverter;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RtfConverter;
import com.dua3.utility.text.ToRichText;
import com.dua3.utility.ui.Graphics;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.lang.Platform;
import com.dua3.utility.math.geometry.Scale2f;
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
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
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
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final ClipboardOwner NO_OP_CLIPBOARD_OWNER = (clipboard, contents) -> {};
    private static final DataFlavor RTF_STRING_FLAVOR = new DataFlavor("text/rtf", "RTF");
    private static final DataFlavor HTML_STRING_FLAVOR = new DataFlavor("text/html", "HTML");
    private static final String COULD_NOT_SET_CLIPBOARD_CONTENTS = "could not set clipboard contents";
    private static final String COULD_NOT_GET_CLIPBOARD_CONTENTS = "could not get clipboard contents";

    static {
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        boolean isJavaAwtHeadless = Boolean.getBoolean("java.awt.headless");
        if (isHeadless) {
            LOG.warn("No GraphicsEnvironment, swing will not work!");
        }
        if (isJavaAwtHeadless) {
            LOG.warn("Headless mode is enabled, swing will not work!");
        }
    }

    // Utility class, should not be instantiated
    private SwingUtil() { /* utility class */ }

    /**
     * Retrieves the display scale of a given {@code Component}.
     *
     * @param component the component for which to retrieve the display scale
     * @return the display scale of the component, or the default scale if no {@link GraphicsConfiguration} is set
     * for the component
     */
    public static Scale2f getDisplayScale(Component component) {
        GraphicsConfiguration conf = component.getGraphicsConfiguration();
        return conf == null ? Scale2f.identity() : getDisplayScale(conf);
    }

    /**
     * Returns the display scale factor of the given GraphicsConfiguration.
     *
     * @param conf the GraphicsConfiguration to retrieve the display scale factor from
     * @return the display scale
     */
    public static Scale2f getDisplayScale(GraphicsConfiguration conf) {
        AffineTransform defaultTransform = conf.getDefaultTransform();
        return new Scale2f((float) defaultTransform.getScaleX(), (float) defaultTransform.getScaleY());
    }

    /**
     * Create an action to be used in menus.
     *
     * @param name              the name to display
     * @param onActionPerformed the Consumer that gets called when the Action is
     *                          invoked
     * @return new Action instance
     */
    @SuppressWarnings({"UseOfClone", "java:S2975"})
    public static Action createAction(String name, Consumer<? super ActionEvent> onActionPerformed) {
        return new AbstractAction(name) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                onActionPerformed.accept(evt);
            }

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
    @SuppressWarnings({"UseOfClone", "java:S2975"})
    public static Action createAction(String name, Runnable onActionPerformed) {
        return new AbstractAction(name) {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.run();
            }

            @Override
            public AbstractAction clone() throws CloneNotSupportedException {
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
     * Copy rich text to the clipboard.
     *
     * @param csq the text
     */
    public static void copyToClipboard(CharSequence csq) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(createTextClipboardTransferable(csq), NO_OP_CLIPBOARD_OWNER);
        } catch (HeadlessException e) {
            LOG.warn(COULD_NOT_SET_CLIPBOARD_CONTENTS, e);
        }
    }

    /**
     * Copy image to clipboard.
     *
     * @param image the image
     */
    public static void copyToClipboard(BufferedImage image) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new ImageTransferable(image), NO_OP_CLIPBOARD_OWNER);
        } catch (HeadlessException e) {
            LOG.warn(COULD_NOT_SET_CLIPBOARD_CONTENTS, e);
        }
    }

    /**
     * Copy file/folder to clipboard.
     *
     * @param path the path to the file/folder to copy to the clipboard
     */
    public static void copyToClipboard(Path path) {
        copyToClipboard(List.of(path));
    }

    /**
     * Copy files/folders to clipboard.
     *
     * @param paths the list of paths to copy to the clipboard
     */
    public static void copyToClipboard(Collection<? extends Path> paths) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            List<File> files = new ArrayList<>(paths.size());
            for (Path path : paths) {
                files.add(path.toAbsolutePath().toFile());
            }
            clipboard.setContents(new FileListTransferable(files), NO_OP_CLIPBOARD_OWNER);
        } catch (HeadlessException e) {
            LOG.warn(COULD_NOT_SET_CLIPBOARD_CONTENTS, e);
        }
    }

    /**
     * Retrieves a string from the system clipboard, if available.
     * Attempts to access the clipboard contents and checks if the data can
     * be interpreted as a string.
     *
     * @return an {@code Optional} containing the string from the clipboard if present and accessible;
     *         otherwise, an empty {@code Optional}.
     */
    public static Optional<String> getStringFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            return getTransferDataAsString(transferable, DataFlavor.stringFlavor);
        } catch (HeadlessException e) {
            LOG.warn(COULD_NOT_GET_CLIPBOARD_CONTENTS, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves text content from the system clipboard. The method first attempts to fetch RTF-formatted
     * text from the clipboard. If RTF content is available, it is converted to a RichText instance and returned.
     * If RTF content is not present, it falls back to retrieving plain text from the clipboard,
     * ensuring a RichText instance is returned with either the retrieved string or an empty string if no text is available.
     *
     * @return an Optional containing a RichText instance with the clipboard's content,
     *         or an Optional with an empty RichText if no text is available in the clipboard.
     */
    public static Optional<RichText> getTextFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);

            if (transferable != null) {
                Optional<String> rtfString = getTransferDataAsString(transferable, RTF_STRING_FLAVOR);
                if (rtfString.isPresent()) {
                    Optional<RtfConverter> rtfConverter = RtfConverter.get();
                    if (rtfConverter.isPresent()) {
                        return Optional.of(rtfConverter.get().toRichText(rtfString.get()));
                    }
                }
            }

            return Optional.of(RichText.valueOf(getStringFromClipboard().orElse("")));
        } catch (HeadlessException e) {
            LOG.warn(COULD_NOT_GET_CLIPBOARD_CONTENTS, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves an image from the system clipboard if one is available.
     *
     * @return an {@code Optional} containing the image if the clipboard contains an image,
     *         or an empty {@code Optional} if no image is present in the clipboard.
     */
    public static Optional<BufferedImage> getImageFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Object data = transferable.getTransferData(DataFlavor.imageFlavor);
                if (data instanceof BufferedImage bufferedImage) {
                    return Optional.of(bufferedImage);
                }
                if (data instanceof Image image) {
                    return Optional.of(toBufferedImage(image));
                }
            }
        } catch (HeadlessException | IOException | UnsupportedFlavorException e) {
            LOG.warn(COULD_NOT_GET_CLIPBOARD_CONTENTS, e);
        }
        return Optional.empty();
    }

    /**
     * Retrieves a collection of file paths from the system clipboard.
     * If the clipboard contains files, their paths are returned as a collection.
     * Otherwise, an empty collection is returned.
     *
     * @return A collection of file paths retrieved from the clipboard or an empty collection
     *         if no files are present on the clipboard.
     */
    public static Collection<Path> getFilesFromClipboard() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                Object data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (data instanceof Collection<?> files) {
                    List<Path> paths = new ArrayList<>();
                    for (Object file : files) {
                        if (file instanceof File f) {
                            paths.add(f.toPath());
                        }
                    }
                    return paths;
                }
            }
        } catch (HeadlessException | IOException | UnsupportedFlavorException e) {
            LOG.warn(COULD_NOT_GET_CLIPBOARD_CONTENTS, e);
        }
        return List.of();
    }

    private static Transferable createTextClipboardTransferable(CharSequence csq) {
        Map<DataFlavor, Object> data = new LinkedHashMap<>();
        data.put(DataFlavor.stringFlavor, csq.toString());

        if (csq instanceof ToRichText text) {
            RtfConverter.get().ifPresent(rtfConverter -> data.put(RTF_STRING_FLAVOR, rtfConverter.convert(text)));

            HtmlConverter htmlConverter = HtmlConverter.create(
                    HtmlConverter.useCss(false),
                    HtmlConverter.convertLineBreaksTo("<br>\n")
            );
            data.put(HTML_STRING_FLAVOR, htmlConverter.convert(text));
        }

        return new MultiFlavorTransferable(data);
    }

    private static Optional<String> getTransferDataAsString(@Nullable Transferable transferable, DataFlavor flavor) {
        if (transferable == null || !transferable.isDataFlavorSupported(flavor)) {
            return Optional.empty();
        }
        try {
            Object data = transferable.getTransferData(flavor);
            return switch (data) {
                case String s -> Optional.of(s);
                case ByteArrayInputStream bais -> Optional.of(new String(bais.readAllBytes(), StandardCharsets.UTF_8));
                case null, default -> Optional.empty();
            };
        } catch (IOException | UnsupportedFlavorException e) {
            LOG.warn("could not read clipboard data for flavor {}", flavor, e);
            return Optional.empty();
        }
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage bufferedImage) {
            return bufferedImage;
        }

        int width = Math.max(1, image.getWidth(null));
        int height = Math.max(1, image.getHeight(null));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = result.createGraphics();
        try {
            g2.drawImage(image, 0, 0, null);
        } finally {
            g2.dispose();
        }
        return result;
    }

    private static DataFlavor createTextFlavor(String mimeType) {
        try {
            return new DataFlavor(mimeType);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("failed to initialize clipboard data flavor: " + mimeType, e);
        }
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     * On macOS, the global menubar is also enabled.
     */
    public static void setNativeLookAndFeel() {
        setNativeLookAndFeelInternal(null);
    }

    /**
     * Set the Swing Look&amp;Feel to the native Look&amp;Feel.
     * On macOS, the global menubar is also enabled.
     *
     * @param applicationName the application name to set
     */
    public static void setNativeLookAndFeel(String applicationName) {
        setNativeLookAndFeelInternal(applicationName);
    }

    private static void setNativeLookAndFeelInternal(@Nullable String applicationName) {
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
    public static Optional<Path> showDirectoryOpenDialog(@Nullable Component parent, Path current) {
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
    public static Optional<Path> showFileOpenDialog(@Nullable Component parent, Path current, Pair<String, String[]>... types) {
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
    public static Optional<Path> showFileSaveDialog(@Nullable Component parent, Path current, Pair<String, String[]>... types) {
        return showFileDialog(parent, current, JFileChooser.FILES_ONLY, JFileChooser::showSaveDialog, types);
    }

    /**
     * Convert java.awt.Color to Color.
     *
     * @param color java.awt.Color to be converted
     * @return Color
     */
    public static Color convert(java.awt.Color color) {
        return new RGBColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Convert Color to java.awt.Color.
     *
     * @param color Color to be converted
     * @return java.awt.Color
     */
    public static java.awt.Color convert(Color color) {
        return new java.awt.Color(color.argb(), color.a() != 0xff);
    }

    /**
     * Converts a {@link Path2f} object to a {@link Path2D}.
     *
     * @param path the Path2f object containing segments to be converted to a JavaFX Path
     * @return a JavaFX Path object representing the equivalent structure of the input Path2f
     * @throws IllegalArgumentException if an unsupported segment type or unsupported number of control points is encountered
     */
    public static Path2D convertToSwingPath(Path2f path) {
        Path2D swingPath = new Path2D.Float();
        path.segments().forEach(segment -> {
            switch (segment) {
                case MoveTo2f s -> swingPath.moveTo(s.end().x(), s.end().y());
                case Line2f s -> swingPath.lineTo(s.end().x(), s.end().y());
                case Curve2f s -> {
                    int n = s.numberOfControls();
                    switch (n) {
                        case 3 -> swingPath.quadTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y()
                        );
                        case 4 -> swingPath.curveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y(),
                                s.control(3).x(), s.control(3).y()
                        );
                        default -> throw new IllegalArgumentException("Unsupported number of control points: " + n);
                    }
                }
                case Arc2f s -> {
                    Consumer<Vector2f[]> generateBezierSegment = points -> {
                        assert points.length == 3;
                        swingPath.curveTo(
                                points[0].x(), points[0].y(),
                                points[1].x(), points[1].y(),
                                points[2].x(), points[2].y()
                        );
                    };
                    Graphics.approximateArc(s, p -> swingPath.moveTo(p.x(), p.y()), generateBezierSegment);
                }
                case ClosePath2f c -> swingPath.closePath();
                default ->
                        throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass().getName());
            }
        });
        return swingPath;
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
    public static Optional<Path> showOpenDialog(@Nullable Component parent, Path current, int selectionMode,
                                                Pair<String, String[]>... types) {
        return showFileDialog(parent, current, selectionMode, JFileChooser::showOpenDialog, types);
    }

    @SafeVarargs
    private static Optional<Path> showFileDialog(@Nullable Component parent, Path current, int selectionMode, BiFunction<? super JFileChooser, ? super Component, Integer> showDialog,
                                                 Pair<String, String[]>... types) {
        boolean isFileOnlySelection = selectionMode == JFileChooser.FILES_ONLY;
        File file;
        File directory = null;
        try {
            if (!Files.exists(current)) {
                file = current.toFile().getAbsoluteFile();
                directory = file;
            } else {
                if (Files.isDirectory(current)) {
                    directory = current.toFile().getAbsoluteFile();
                    file = isFileOnlySelection ? null : directory;
                } else {
                    file = current.toFile().getAbsoluteFile();
                }
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            LOG.warn("path cannot be converted to file: {}", current, e);
            file = new File(".").getAbsoluteFile();
        }

        JFileChooser jfc = new JFileChooser(directory);
        for (var entry : types) {
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
        @SuppressWarnings("MagicConstant")
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
        @SuppressWarnings("MagicConstant")
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
        if (GraphicsEnvironment.isHeadless()) {
            LOG.warn("addDropFilesSupport(): cannot add drop support in headless mode");
            return;
        }
        component.setDropTarget(new SwingDropFilesTarget(test, action, exceptionHandler));
    }

    /**
     * Add support for dropping text on a component.
     *
     * @param component the component to add drop support to
     * @param action    the action to perform when text is dropped
     */
    public static void addDropTextSupport(JComponent component, Consumer<? super String> action) {
        addDropTextSupport(component, action, text -> !text.isEmpty(), e -> {});
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
        if (GraphicsEnvironment.isHeadless()) {
            LOG.warn("addDropTextSupport(): cannot add drop support in headless mode");
            return;
        }

        component.setDropTarget(new SwingDropTextTarget(test, action, exceptionHandler));
    }

    /**
     * Configures the rendering quality settings for a Graphics2D object to enhance visual quality.
     *
     * @param g2d the Graphics2D object whose rendering settings are to be configured
     */
    public static void setRenderingQualityHigh(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private static final class MultiFlavorTransferable implements Transferable {
        private final Map<DataFlavor, Object> dataByFlavor;

        private MultiFlavorTransferable(Map<DataFlavor, Object> dataByFlavor) {
            this.dataByFlavor = Map.copyOf(dataByFlavor);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return dataByFlavor.keySet().toArray(DataFlavor[]::new);
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return dataByFlavor.containsKey(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return dataByFlavor.get(flavor);
        }
    }

    private static final class ImageTransferable implements Transferable {
        private final BufferedImage image;

        private ImageTransferable(BufferedImage image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }

    private static final class FileListTransferable implements Transferable {
        private final List<File> files;

        private FileListTransferable(List<File> files) {
            this.files = List.copyOf(files);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return files;
        }
    }

    private static class SwingDropFilesTarget extends DropTarget {
        private final Predicate<? super Collection<File>> test;
        private final Consumer<? super Collection<File>> action;
        private final Consumer<? super Exception> exceptionHandler;

        SwingDropFilesTarget(Predicate<? super Collection<File>> test, Consumer<? super Collection<File>> action, Consumer<? super Exception> exceptionHandler) throws HeadlessException {
            this.test = test;
            this.action = action;
            this.exceptionHandler = exceptionHandler;
        }

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

        @Serial
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {throw new java.io.NotSerializableException("com.dua3.utility.swing.SwingUtil.SwingDropTarget");}

        @Serial
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {throw new java.io.NotSerializableException("com.dua3.utility.swing.SwingUtil.SwingDropTarget");}
    }

    private static class SwingDropTextTarget extends DropTarget {
        private final transient Predicate<? super String> test;
        private final transient Consumer<? super String> action;
        private final transient Consumer<? super Exception> exceptionHandler;

        SwingDropTextTarget(Predicate<? super String> test, Consumer<? super String> action, Consumer<? super Exception> exceptionHandler) throws HeadlessException {
            this.test = test;
            this.action = action;
            this.exceptionHandler = exceptionHandler;
        }

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
    }
}
