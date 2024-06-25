package com.dua3.utility.fx;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.concurrent.Value;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Image;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.Curve2f;
import com.dua3.utility.math.geometry.FillRule;
import com.dua3.utility.math.geometry.Line2f;
import com.dua3.utility.math.geometry.MoveTo2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.text.FontDef;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Window;

import java.awt.GraphicsConfiguration;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * JavaFX utility class.
 */
public final class FxUtil {
    private static final Pattern PATTERN_FILENAME_AND_DOT = Pattern.compile("^\\*\\.");

    /**
     * Private constructor.
     */
    private FxUtil() {}

    /**
     * Convert JavaFX {@link Font} to {@link FontDef}.
     *
     * @param font the font
     * @return the FontDef
     */
    public static FontDef toFontDef(Font font) {
        FontDef fd = new FontDef();
        fd.setFamily(font.getFamily());
        fd.setSize((float) font.getSize());
        return fd;
    }

    /**
     * Convert {@link com.dua3.utility.data.Color} to {@link Color}.
     *
     * @param color the color
     * @return the JavaFX color
     */
    public static Color convert(com.dua3.utility.data.Color color) {
        int argb = color.argb();

        int a = (argb >> 24) & 0xff;
        int r = (argb >> 16) & 0xff;
        int g = (argb >> 8) & 0xff;
        int b = (argb) & 0xff;

        return Color.rgb(r, g, b, a / 255.0);
    }

    /**
     * Convert {@link Color to }{@link com.dua3.utility.data.Color}.
     *
     * @param color the JavaFX color
     * @return the color
     */
    public static com.dua3.utility.data.Color convert(Color color) {
        return com.dua3.utility.data.Color.rgba(
                (int) Math.round(color.getRed() * 255.0),
                (int) Math.round(color.getGreen() * 255.0),
                (int) Math.round(color.getBlue() * 255.0),
                (int) Math.round(color.getOpacity() * 255.0)
        );
    }

    /**
     * Convert {@link FillRule} to JavaFX {@link javafx.scene.shape.FillRule}.
     *
     * @param rule the fill rule
     * @return the JavaFX fill rule
     */
    public static javafx.scene.shape.FillRule convert(FillRule rule) {
        return rule == FillRule.EVEN_ODD ? javafx.scene.shape.FillRule.EVEN_ODD : javafx.scene.shape.FillRule.NON_ZERO;
    }

    /**
     * Convert {@link javafx.scene.shape.FillRule} to JavaFX {@link FillRule}.
     *
     * @param rule JavaFX the fill rule
     * @return the fill rule
     */
    public static FillRule convert(javafx.scene.shape.FillRule rule) {
        return rule == javafx.scene.shape.FillRule.EVEN_ODD ? FillRule.EVEN_ODD : FillRule.NON_ZERO;
    }

    /**
     * Convert {@link AffineTransformation2f} to JavaFX {@link Affine}.
     *
     * @param at the affine transformation
     * @return the JavaFX affine transformation
     */
    public static Affine convert(AffineTransformation2f at) {
        return new Affine(
                at.getScaleX(), at.getShearX(), at.getTranslateX(),
                at.getShearY(), at.getScaleY(), at.getTranslateY()
        );
    }

    /**
     * Convert JavaFX {@link Affine} to {@link AffineTransformation2f}.
     *
     * @param a the JavaFX affine transformation
     * @return the affine transformation
     */
    public static AffineTransformation2f convert(Affine a) {
        return new AffineTransformation2f(
                (float) a.getMxx(), (float) a.getMyx(), (float) a.getTx(),
                (float) a.getMyx(), (float) a.getMyy(), (float) a.getTy()
        );
    }

    /**
     * Converts a {@link Path2f} object to a {@link javafx.scene.shape.Path} object.
     *
     * @param path the Path2f object to convert
     * @return the converted javafx.scene.shape.Path object
     * @throws IllegalArgumentException if the path contains unsupported segment types
     */
    public static javafx.scene.shape.Path convert(Path2f path) {
        javafx.scene.shape.Path jfxPath = new javafx.scene.shape.Path();
        path.segments().forEach(segment -> {
            if (Objects.requireNonNull(segment, "segment is null") instanceof MoveTo2f moveTo) {
                jfxPath.getElements().add(new MoveTo(moveTo.end().x(), moveTo.end().y()));
            } else if (segment instanceof Line2f line2f) {
                jfxPath.getElements().add(new LineTo(line2f.end().x(), line2f.end().y()));
            } else if (segment instanceof Curve2f curve) {
                int n = curve.numberOfControls();
                if (n == 3) {
                    jfxPath.getElements().add(new QuadCurveTo(
                            curve.control(1).x(), curve.control(1).y(),
                            curve.control(2).x(), curve.control(2).y()
                    ));
                } else if (n == 4) {
                    jfxPath.getElements().add(new CubicCurveTo(
                            curve.control(1).x(), curve.control(1).y(),
                            curve.control(2).x(), curve.control(2).y(),
                            curve.control(3).x(), curve.control(3).y()
                    ));
                } else {
                    throw new IllegalArgumentException("Unsupported number of control points: " + n);
                }
            } else if (segment instanceof Arc2f arc) {
                jfxPath.getElements().add(new ArcTo(arc.rx(), arc.ry(), arc.angle(), arc.control(1).x(), arc.control(1).y(), false, false));
            } else {
                throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass().getName());
            }
        });
        return jfxPath;
    }

    /**
     * Convert a {@link Rectangle2f} object to a JavaFX Rectangle object.
     *
     * @param r the Rectangle2f object to convert
     * @return the converted JavaFX Rectangle object
     */
    public static javafx.scene.shape.Rectangle convert(Rectangle2f r) {
        return new javafx.scene.shape.Rectangle(r.x(), r.y(), r.width(), r.height());
    }

    /**
     * Returns the bounds of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the bounds of the text string
     */
    public static Bounds getTextBounds(CharSequence s, com.dua3.utility.text.Font f) {
        return boundsInLocal(s, f);
    }

    /**
     * Returns the bounds of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the bounds of the text string
     */
    public static Bounds getTextBounds(CharSequence s, Font f) {
        return boundsInLocal(s, f);
    }

    /**
     * Calculates the bounds of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the bounds of the text string
     */
    private static Bounds boundsInLocal(CharSequence s, com.dua3.utility.text.Font f) {
        Text text = new Text(s.toString());
        text.setFont(convert(f));
        return text.getBoundsInLocal();
    }

    /**
     * Calculates the bounds of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the bounds of the text string
     */
    private static Bounds boundsInLocal(CharSequence s, Font f) {
        Text text = new Text(s.toString());
        text.setFont(f);
        return text.getBoundsInLocal();
    }

    /**
     * Convert {@link com.dua3.utility.text.Font} to JavaFX {@link Font}.
     *
     * @param font the font
     * @return the JavaFX Font
     */
    public static Font convert(com.dua3.utility.text.Font font) {
        if (font instanceof FxFontEmbedded fxf) {
            return fxf.fxFont();
        }

        return Font.font(
                font.getFamily(),
                font.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                font.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR,
                font.getSizeInPoints()
        );
    }

    /**
     * Convert JavaFX {@link Font} to {@link com.dua3.utility.text.Font}.
     *
     * @param fxFont the font
     * @return the JavaFX Font
     */
    public static com.dua3.utility.text.Font convert(Font fxFont) {
        String style = fxFont.getStyle().toLowerCase(Locale.ROOT);
        return new com.dua3.utility.text.Font(
                fxFont.getFamily(),
                (float) fxFont.getSize(),
                com.dua3.utility.data.Color.BLACK,
                style.contains("bold"),
                style.contains("italic") || style.contains("oblique"),
                style.contains("line-through"),
                style.contains("line-under")
        );
    }

    /**
     * Returns the width of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the width of the text string
     */
    public static double getTextWidth(CharSequence s, com.dua3.utility.text.Font f) {
        return boundsInLocal(s, f).getWidth();
    }

    /**
     * Returns the height of the given text string when rendered with the specified font.
     *
     * @param s the text string
     * @param f the font
     * @return the height of the text string
     */
    public static double getTextHeight(CharSequence s, com.dua3.utility.text.Font f) {
        return boundsInLocal(s, f).getHeight();
    }

    /**
     * Calculates the dimensions required for a given {@link Dimension2D} object to fit within the specified {@link Bounds}.
     *
     * @param a the original dimension
     * @param b the bounds to fit into
     * @return a new Dimension2D object with the adjusted dimensions
     */
    public static Dimension2D growToFit(Dimension2D a, Bounds b) {
        return new Dimension2D(Math.max(a.getWidth(), b.getWidth()), Math.max(a.getHeight(), b.getHeight()));
    }

    /**
     * Test if file matches filter.
     *
     * @param filter the filter
     * @param file   the file
     * @return true if filename matches filter
     */
    public static boolean matches(FileChooser.ExtensionFilter filter, Path file) {
        return matches(filter, file.toString());
    }

    /**
     * Test if filename matches filter.
     *
     * @param filter   the filter
     * @param filename the filename
     * @return true if filename matches filter
     */
    public static boolean matches(FileChooser.ExtensionFilter filter, String filename) {
        String fext = IoUtil.getExtension(filename).toLowerCase(Locale.ROOT);
        return filter.getExtensions().stream()
                .map(ext -> PATTERN_FILENAME_AND_DOT.matcher(ext).replaceFirst("").toLowerCase(Locale.ROOT))
                .anyMatch(ext -> Objects.equals(ext, fext));
    }

    /**
     * Test if filename matches filter.
     *
     * @param filter the filter
     * @param file   the file
     * @return true if file matches filter
     */
    public static boolean matches(FileChooser.ExtensionFilter filter, File file) {
        return matches(filter, file.getName());
    }

    /**
     * Test if URI matches filter.
     *
     * @param filter the filter
     * @param uri    the URI
     * @return true if file matches filter
     */
    public static boolean matches(FileChooser.ExtensionFilter filter, URI uri) {
        return matches(filter, uri.getPath());
    }

    /**
     * Copy text to clipboard.
     *
     * @param s the text
     */
    public static void copyToClipboard(String s) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    /**
     * Copy image to clipboard.
     *
     * @param img the image
     */
    public static void copyToClipboard(Image img) {
        copyToClipboard(FxImageUtil.getInstance().convert(img));
    }

    /**
     * Copy image to clipboard.
     *
     * @param img the image
     */
    public static void copyToClipboard(javafx.scene.image.Image img) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putImage(img);
        clipboard.setContent(content);
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
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        List<File> files = paths.stream().map(Path::toAbsolutePath).map(Path::toFile).toList();
        content.putFiles(files);
        clipboard.setContent(content);
    }

    /**
     * Create an {@link EventHandler<DragEvent>} that accepts dragging files.
     *
     * @param modeGetter Function that determines the supported {@link TransferMode}s.
     *                   Should return an empty list if the drag is not accepted.
     * @return event handler
     */
    public static EventHandler<DragEvent> dragEventHandler(Function<? super List<Path>, ? extends Collection<TransferMode>> modeGetter) {
        return event -> {
            Dragboard db = event.getDragboard();
            List<Path> files = DataUtil.convert(db.getFiles(), File::toPath);
            TransferMode[] modes = modeGetter.apply(files).toArray(TransferMode[]::new);
            event.acceptTransferModes(modes);
            event.consume();
        };
    }

    /**
     * Create an {@link EventHandler<DragEvent>} that accepts paths.
     *
     * @param processor consumer that processes the drop event
     * @return event handler
     */
    public static EventHandler<DragEvent> dropEventHandler(Consumer<? super List<Path>> processor) {
        return event -> {
            Dragboard db = event.getDragboard();
            List<Path> paths = DataUtil.convert(db.getFiles(), File::toPath);
            processor.accept(paths);
            event.setDropCompleted(true);
            event.consume();
        };
    }

    /**
     * Create the union of two rectangles. The union here is defined as the rectangle r of minimum size that contains
     * both rectangles r1 and r2.
     *
     * @param r1 first rectangle
     * @param r2 second rectangle
     * @return minimal rectangle containing both r1 and r2
     */
    public static Rectangle2D union(Rectangle2D r1, Rectangle2D r2) {
        var xMin = Math.min(r1.getMinX(), r2.getMinX());
        var yMin = Math.min(r1.getMinY(), r2.getMinY());
        var xMax = Math.max(r1.getMaxX(), r2.getMaxX());
        var yMax = Math.max(r1.getMaxY(), r2.getMaxY());
        return new Rectangle2D(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    /**
     * Represents an adapter for an InvalidationListener.
     * <p>
     * This class implements the {@link BiConsumer} functional interface.
     * It accepts two values of type T and calls the {@link InvalidationListener#invalidated(Observable)}
     * method of the invalidationListener, passing the observable object.
     *
     * @param <T> The type of the values accepted by the adapter.
     */
    private record InvalidationListenerAdapter<T>(Observable observable, InvalidationListener invalidationListener)
            implements BiConsumer<T, T> {
        @Override
        public void accept(@Nullable T t1, @Nullable T t2) {
            invalidationListener.invalidated(observable);
        }
    }

    /**
     * A class that adapts a {@code ChangeListener} to a {@code BiConsumer}.
     *
     * @param <T> the type of the value being observed by the {@code ObservableValue}
     */
    private record ChangeListenerAdapter<T>(ObservableValue<T> observableValue,
                                            ChangeListener<? super T> changeListener)
            implements BiConsumer<T, T> {
        @Override
        public void accept(@Nullable T t1, @Nullable T t2) {
            changeListener.changed(observableValue, t1, t2);
        }
    }

    /**
     * Converts a {@link Value} object into an {@link ObservableValue}.
     *
     * @param value the Value object to be converted
     * @param <T> the type of the value stored in the Value object
     * @return an ObservableValue object that reflects changes in the Value object
     */
    public static <T> ObservableValue<T> toObservableValue(Value<T> value) {
        return new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super T> listener) {
                value.addChangeListener(new ChangeListenerAdapter<>(this, listener));
            }

            @Override
            public void removeListener(ChangeListener<? super T> listener) {
                List.copyOf(value.getChangeListeners()).forEach(changeListener -> {
                    if (changeListener instanceof ChangeListenerAdapter<?> a && a.changeListener == listener) {
                        value.removeChangeListener(changeListener);
                    }
                });
            }

            @Override
            public T getValue() {
                return value.get();
            }

            @Override
            public void addListener(InvalidationListener listener) {
                value.addChangeListener(new InvalidationListenerAdapter<>(this, listener));
            }

            @Override
            public void removeListener(InvalidationListener listener) {
                List.copyOf(value.getChangeListeners()).forEach(changeListener -> {
                    if (changeListener instanceof InvalidationListenerAdapter<?> cla && cla.invalidationListener == listener) {
                        value.removeChangeListener(changeListener);
                    }
                });
            }
        };
    }

    /**
     * Attaches a mouse event handler to a given node for a specific event type.
     * <p>
     * If an event handler is already registered, the new handler is called first. If the event is not consumed by the#
     * handler, the old handler is called too.
     *
     * @param node     The node to attach the mouse event handler to.
     * @param eventType The type of the mouse event to handle.
     * @param handler  The event handler to be called when the specified mouse event occurs.
     */
    public static void addMouseEventHandler(Node node, EventType<MouseEvent> eventType, EventHandler<? super MouseEvent> handler) {
        if (eventType == MouseEvent.MOUSE_PRESSED) {
            addHandler(node::getOnMousePressed, node::setOnMousePressed, handler);
        } else if (eventType == MouseEvent.MOUSE_RELEASED) {
            addHandler(node::getOnMouseReleased, node::setOnMouseReleased, handler);
        } else if (eventType == MouseEvent.MOUSE_CLICKED) {
            addHandler(node::getOnMouseClicked, node::setOnMouseClicked, handler);
        } else if (eventType == MouseEvent.MOUSE_ENTERED) {
            addHandler(node::getOnMouseEntered, node::setOnMouseEntered, handler);
        } else if (eventType == MouseEvent.MOUSE_EXITED) {
            addHandler(node::getOnMouseExited, node::setOnMouseExited, handler);
        } else if (eventType == MouseEvent.MOUSE_MOVED) {
            addHandler(node::getOnMouseMoved, node::setOnMouseMoved, handler);
        } else if (eventType == MouseEvent.MOUSE_DRAGGED) {
            addHandler(node::getOnMouseDragged, node::setOnMouseDragged, handler);
        }
    }

    /**
     * Adds a new event handler to a node. If the node already has an event handler of the same event type,
     * the new handler is invoked before the existing handler is invoked.
     *
     * @param getHandler  a supplier function that returns the current event handler for the node
     * @param setHandler  a consumer function that sets the event handler for the node
     * @param newHandler  the new event handler to be added
     */
    private static void addHandler(
            Supplier<? extends EventHandler<? super MouseEvent>> getHandler,
            Consumer<EventHandler<? super MouseEvent>> setHandler,
            EventHandler<? super MouseEvent> newHandler) {
        EventHandler<? super MouseEvent> currentHandler = getHandler.get();
        if (currentHandler == null) {
            setHandler.accept(newHandler);
        } else {
            setHandler.accept(evt -> handleEventChained(evt, newHandler, currentHandler));
        }
    }

    /**
     * Handles an event by invoking two event handlers in sequence.
     *
     * @param <E>            the type of the event
     * @param evt            the event to be handled
     * @param firstHandler   the first event handler to be invoked
     * @param secondHandler  the second event handler to be invoked if the event is not consumed by the first handler
     */
    private static <E extends Event> void handleEventChained(E evt, EventHandler<? super E> firstHandler, EventHandler<? super E> secondHandler) {
        firstHandler.handle(evt);
        if (!evt.isConsumed()) {
            secondHandler.handle(evt);
        }
    }

    /**
     * MappedList is a subclass of TransformationList that maps elements from a source list to a new type using a converter function.
     * It maintains a one-to-one mapping between elements in the source list and the mapped list.
     *
     * @param <A> the type of elements in the mapped list
     * @param <B> the type of elements in the source list
     */
    static class MappedList<A, B> extends TransformationList<A, B> {
        private final Function<B, A> converter;

        MappedList(ObservableList<? extends B> list, Function<B, A> converter) {
            super(list);
            this.converter = converter;
        }

        @Override
        public int getSourceIndex(int index) {
            return index;
        }

        @Override
        public int getViewIndex(int i) {
            return i;
        }

        @Override
        protected void sourceChanged(javafx.collections.ListChangeListener.Change<? extends B> changeB) {
            ListChangeListener.Change<A> changeA = new ListChangeListener.Change<>(this) {
                @Override
                public boolean next() {
                    return changeB.next();
                }

                @Override
                public void reset() {
                    changeB.reset();
                }

                @Override
                public int getFrom() {
                    return changeB.getFrom();
                }

                @Override
                public int getTo() {
                    return changeB.getTo();
                }

                @Override
                public List<A> getRemoved() {
                    return DataUtil.convert(changeB.getRemoved(), converter);
                }

                @Override
                protected int[] getPermutation() {
                    return new int[0];
                }
            };
            fireChange(changeA);
        }

        @Override
        public A get(int index) {
            return converter.apply(getSource().get(index));
        }

        @Override
        public int size() {
            return getSource().size();
        }
    }

    /**
     * Maps each element of the given ObservableList using the provided mapping function.
     *
     * @param list    the ObservableList to be mapped
     * @param mapping the mapping function to apply to each element of the list
     * @param <A>     the type of elements in the original list
     * @param <B>     the type of elements returned by the mapping function
     * @return a new ObservableList containing the mapped elements
     */
    public static <A, B> ObservableList<B> map(ObservableList<A> list, Function<A,B> mapping) {
        return new MappedList<>(list, mapping);
    }

    /**
     * Retrieves the display scale of a given {@code Screen}.
     *
     * @param screen the screen for which to retrieve the display scale
     * @return the display scale of the screen, or the default scale if no {@link GraphicsConfiguration} is set
     * for the component
     */
    public static Scale2f getDisplayScale(Screen screen) {
        return new Scale2f((float) screen.getOutputScaleX(), (float) screen.getOutputScaleY());
    }

    /**
     * Returns the display scale of the given window. The display scale is a scaling factor that is applied
     * to the window's content to adjust for high-resolution displays.
     *
     * @param window the window for which to retrieve the display scale
     * @return the scale factor applied to the window's content
     */
    public static Scale2f getDisplayScale(Window window) {
        return getDisplayScale(getScreen(window));
    }

    /**
     * Returns the Screen where the given Window is located.
     *
     * @param window the Window to get the Screen for
     * @return the Screen where the window is located or the primary screen if the screen could not be determined
     */
    public static Screen getScreen(Window window) {
        double minX = window.getX();
        double minY = window.getY();
        double width = window.getWidth();
        double height = window.getHeight();
        ObservableList<Screen> screens = Screen.getScreensForRectangle(minX, minY, width, height);
        return screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
    }
}
