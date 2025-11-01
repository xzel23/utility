package com.dua3.utility.fx;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.ClosePath2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.HtmlConverter;
import com.dua3.utility.text.RichText;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.scene.layout.Region;
import javafx.scene.shape.ClosePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
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
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * JavaFX utility class.
 */
public final class FxUtil {
    private static final Logger LOG = LogManager.getLogger(FxUtil.class);
    /**
     * A constant that represents a boolean expression which always evaluates to true.
     * This can be used in scenarios where an always-true condition is required,
     * commonly for default or unconditional bindings in JavaFX applications.
     */
    public static final BooleanExpression ALWAYS_TRUE = new ReadOnlyBooleanWrapper(true);

    /**
     * A constant that represents a boolean expression which always evaluates to false.
     * This can be used in scenarios where an always-false condition is required,
     * commonly for default or unconditional bindings in JavaFX applications.
     */
    public static final BooleanExpression ALWAYS_FALSE = new ReadOnlyBooleanWrapper(false);

    private static final Pattern PATTERN_FILENAME_AND_DOT = Pattern.compile("^\\*\\.");
    private static final FxFontUtil FX_FONT_UTIL = FxFontUtil.getInstance();

    private FxUtil() { /* utility class */ }

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
                (float) a.getMxx(), (float) a.getMxy(), (float) a.getTx(),
                (float) a.getMyx(), (float) a.getMyy(), (float) a.getTy()
        );
    }

    /**
     * Converts a {@link Path2f} object to a {@link javafx.scene.shape.Path} object.
     *
     * @param path the {@link Path2f} object to convert
     * @return the converted javafx.scene.shape.Path object
     * @throws IllegalArgumentException if the path contains unsupported segment types
     */
    public static javafx.scene.shape.Path convert(Path2f path) {
        javafx.scene.shape.Path jfxPath = new javafx.scene.shape.Path();
        path.segments().forEach(segment -> {
            switch (segment) {
                case MoveTo2f s -> jfxPath.getElements().add(new MoveTo(s.end().x(), s.end().y()));
                case Line2f s -> jfxPath.getElements().add(new LineTo(s.end().x(), s.end().y()));
                case Curve2f s -> {
                    switch (s.numberOfControls()) {
                        case 3 -> jfxPath.getElements().add(new QuadCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y()
                        ));
                        case 4 -> jfxPath.getElements().add(new CubicCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y(),
                                s.control(3).x(), s.control(3).y()
                        ));
                        default ->
                                throw new IllegalArgumentException("Unsupported number of control points: " + s.numberOfControls());
                    }
                }
                case Arc2f s -> jfxPath.getElements().add(new ArcTo(
                        s.rx(), s.ry(), s.angle(),
                        s.control(1).x(), s.control(1).y(),
                        false, false)
                );
                case ClosePath2f s -> jfxPath.getElements().add(new ClosePath());
                default ->
                        throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass().getName());
            }
        });
        return jfxPath;
    }

    /**
     * Convert a {@link Rectangle2f} object to a JavaFX {@link javafx.scene.shape.Rectangle} object.
     *
     * @param r the {@link Rectangle2f} object to convert
     * @return the converted JavaFX Rectangle object
     */
    public static javafx.scene.shape.Rectangle convert(Rectangle2f r) {
        return new javafx.scene.shape.Rectangle(r.x(), r.y(), r.width(), r.height());
    }

    /**
     * Convert a JavaFX {@link javafx.scene.shape.Rectangle} object to a {@link Rectangle2f} object.
     *
     * @param r the Rectangle object to convert
     * @return the converted {@link Rectangle2f} object
     */
    public static Rectangle2f convert(Rectangle2D r) {
        return new Rectangle2f((float) r.getMinX(), (float) r.getMinY(), (float) r.getWidth(), (float) r.getHeight());
    }

    /**
     * Convert a JavaFX {@link Bounds} object to a {@link Rectangle2f} object.
     *
     * @param b the {@link Bounds} object to convert
     * @return the converted {@link Rectangle2f} object
     */
    public static Rectangle2f convert(Bounds b) {
        return new Rectangle2f((float) b.getMinX(), (float) b.getMinY(), (float) b.getWidth(), (float) b.getHeight());
    }

    /**
     * Converts a Dimension2D object to a Dimension2f object.
     *
     * @param d the Dimension2D object to be converted
     * @return a new Dimension2f object with the width and height values
     *         of the provided Dimension2D object, cast to float
     */
    public static Dimension2f convert(Dimension2D d) {
        return new Dimension2f((float) d.getWidth(), (float) d.getHeight());
    }

    /**
     * Converts a Dimension2f object to a Dimension2D object.
     *
     * @param d the Dimension2f object to be converted
     * @return a new Dimension2D object with the width and height values from the given Dimension2f
     */
    public static Dimension2D convert(Dimension2f d) {
        return new Dimension2D(d.width(), d.height());
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
        return FX_FONT_UTIL.convert(font);
    }

    /**
     * Convert JavaFX {@link Font} to {@link com.dua3.utility.text.Font}.
     *
     * @param fxFont the font
     * @return the JavaFX Font
     */
    public static com.dua3.utility.text.Font convert(Font fxFont) {
        return FX_FONT_UTIL.convert(fxFont);
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
                .anyMatch(Predicate.isEqual(fext));
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
     * Copy text to clipboard.
     *
     * @param text the text
     */
    public static void copyToClipboard(RichText text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text.toString());
        content.putHtml(HtmlConverter.create(HtmlConverter.useCss(false)).convert(text));
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
        final ClipboardContent content = new ClipboardContent();
        content.putImage(img);
        Clipboard.getSystemClipboard().setContent(content);
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
     * Retrieves a string from the system clipboard if available.
     *
     * @return an {@code Optional} containing the string from the clipboard if present,
     *         or an empty {@code Optional} otherwise
     */
    public static Optional<String> getStringFromClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            return Optional.of(clipboard.getString());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves an image from the system clipboard if one is available.
     *
     * @return an {@code Optional} containing the image if the clipboard contains an image,
     *         or an empty {@code Optional} if no image is present in the clipboard.
     */
    public static Optional<Image> getImageFromClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasImage()) {
            return Optional.of(FxImageUtil.getInstance().convert(clipboard.getImage()));
        } else {
            return Optional.empty();
        }
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
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasFiles()) {
            return DataUtil.convert(clipboard.getFiles(), File::toPath);
        } else {
            return List.of();
        }
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
        if (r1.getWidth() == 0 && r1.getHeight() == 0) {
            return r2;
        }
        if (r2.getWidth() == 0 && r2.getHeight() == 0) {
            return r1;
        }

        double xMin = Math.min(r1.getMinX(), r2.getMinX());
        double yMin = Math.min(r1.getMinY(), r2.getMinY());
        double xMax = Math.max(r1.getMaxX(), r2.getMaxX());
        double yMax = Math.max(r1.getMaxY(), r2.getMaxY());

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
    public static <T extends @Nullable Object> ObservableValue<T> toObservableValue(Value<T> value) {
        return new ObservableValue<>() {
            @Override
            public void addListener(ChangeListener<? super T> listener) {
                value.addChangeListener(new ChangeListenerAdapter<>(this, listener));
            }

            @Override
            public void removeListener(ChangeListener<? super T> listener) {
                List.copyOf(value.getChangeListeners()).stream()
                        .filter(changeListener -> changeListener instanceof ChangeListenerAdapter<?> a && a.changeListener == listener)
                        .forEach(value::removeChangeListener);
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
                List.copyOf(value.getChangeListeners()).stream().filter(changeListener -> changeListener instanceof InvalidationListenerAdapter<?> cla && cla.invalidationListener == listener).forEach(value::removeChangeListener);
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
            Supplier<? extends @Nullable EventHandler<? super MouseEvent>> getHandler,
            Consumer<@Nullable EventHandler<? super MouseEvent>> setHandler,
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
                    return LangUtil.EMPTY_INT_ARRAY;
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
    public static <A, B> ObservableList<B> map(ObservableList<A> list, Function<A, B> mapping) {
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
     * Converts a Path2f object to a JavaFX Path.
     *
     * @param path the Path2f object containing segments to be converted to a JavaFX Path
     * @return a JavaFX Path object representing the equivalent structure of the input Path2f
     * @throws IllegalArgumentException if an unsupported segment type or unsupported number of control points is encountered
     */
    public static javafx.scene.shape.Path convertToJavaFxPath(Path2f path) {
        javafx.scene.shape.Path jfxPath = new javafx.scene.shape.Path();
        path.segments().forEach(segment -> {
            switch (segment) {
                case MoveTo2f s -> jfxPath.getElements().add(new MoveTo(s.end().x(), s.end().y()));
                case Line2f s -> jfxPath.getElements().add(new LineTo(s.end().x(), s.end().y()));
                case Curve2f s -> {
                    int n = s.numberOfControls();
                    jfxPath.getElements().add(switch (n) {
                        case 3 -> new QuadCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y()
                        );
                        case 4 -> new CubicCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y(),
                                s.control(3).x(), s.control(3).y()
                        );
                        default -> throw new IllegalArgumentException("Unsupported number of control points: " + n);
                    });
                }
                case Arc2f s -> jfxPath.getElements().add(new ArcTo(
                        s.rx(), s.ry(), s.angle(),
                        s.control(1).x(), s.control(1).y(),
                        false, false
                ));
                case ClosePath2f s -> jfxPath.getElements().add(new ClosePath());
                default ->
                        throw new IllegalArgumentException("Unsupported segment type: " + segment.getClass().getName());
            }
        });
        return jfxPath;
    }

    /**
     * Returns the Screen where the given Window is located.
     *
     * @param window the Window to get the Screen for
     * @return the Screen where the window is located or the primary screen if the screen could not be determined
     */
    public static Screen getScreen(@Nullable Window window) {
        if (window == null) {
            return Screen.getPrimary();
        }

        double minX = window.getX();
        double minY = window.getY();
        double width = window.getWidth();
        double height = window.getHeight();
        ObservableList<Screen> screens = Screen.getScreensForRectangle(minX, minY, width, height);
        return screens.isEmpty() ? Screen.getPrimary() : screens.getFirst();
    }

    private static final class ReadOnlyBooleanValue implements ObservableBooleanValue {
        private final boolean value;
        private final Boolean wrappedValue;

        private ReadOnlyBooleanValue(boolean value) {
            this.value = value;
            this.wrappedValue = value;
        }

        @Override
        public boolean get() {
            return value;
        }

        @Override
        public Boolean getValue() {
            return wrappedValue;
        }

        @Override
        public void addListener(ChangeListener<? super Boolean> listener) { /* nothing to do */ }

        @Override
        public void removeListener(ChangeListener<? super Boolean> listener) { /* nothing to do */ }

        @Override
        public void addListener(InvalidationListener listener) { /* nothing to do */ }

        @Override
        public void removeListener(InvalidationListener listener) { /* nothing to do */ }
    }

    /**
     * A constant ObservableBooleanValue that always holds the value {@code true}.
     * This variable is immutable and can be reliably used wherever a constant
     * boolean value of {@code true} is required in an observable context.
     */
    public static final ObservableBooleanValue TRUE = new ReadOnlyBooleanValue(true);

    /**
     * A static constant representing a boolean property with a fixed value of {@code false}.
     * This property is immutable and can be used wherever an {@code ObservableBooleanValue}
     * with a value of {@code false} is required.
     */
    public static final ObservableBooleanValue FALSE = new ReadOnlyBooleanValue(false);

    /**
     * Returns an ObservableBooleanValue that represents the specified boolean constant.
     *
     * @param b the boolean value to be wrapped as an ObservableBooleanValue
     * @return an ObservableBooleanValue representing the specified boolean
     */
    public static ObservableBooleanValue constant(boolean b) {
        return b ? TRUE : FALSE;
    }

    /**
     * Provides a converter between com.dua3.utility.text.Font and javafx.scene.text.Font.
     *
     * @return a Converter instance that can convert between com.dua3.utility.text.Font
     *         and javafx.scene.text.Font.
     */
    public static Converter<com.dua3.utility.text.Font, javafx.scene.text.Font> fontConverter() {
        return Converter.create(
                FX_FONT_UTIL::convert,
                FX_FONT_UTIL::convert
        );
    }

    /**
     * Creates and returns a converter between com.dua3.utility.data.Color and javafx.scene.paint.Color.
     *
     * @return a bidirectional converter that facilitates conversion between com.dua3.utility.data.Color
     *         and javafx.scene.paint.Color.
     */
    public static Converter<com.dua3.utility.data.Color, Color> colorConverter() {
        return Converter.create(
                FxUtil::convert,
                FxUtil::convert
        );
    }

    /**
     * Schedules a task to be executed on the next frame of the JavaFX application thread.
     * The provided action will be invoked with the timestamp of the frame in nanoseconds.
     * <p>
     * The task is run when the next frame is rendered. Use it to avoid flickering when making updates to the UI.
     *
     * @param action a {@code LongConsumer} representing the task to be executed. The input to the consumer
     *               is the timestamp of the frame in nanoseconds.
     */
    public static void runOnNextFrame(LongConsumer action) {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    action.accept(now);
                } catch (Exception e) {
                    LOG.warn("runOnNextFrame(LongConsumer): error executing task", e);
                } finally {
                    stop();
                }
            }
        }.start();
    }

    /**
     * Schedules the provided action to execute on the next frame using an AnimationTimer.
     * The action will run once, and the timer will stop immediately afterward.
     * <p>
     * The task is run when the next frame is rendered. Use it to avoid flickering when making updates to the UI.
     *
     * @param action the Runnable task to be executed on the next frame
     */
    public static void runOnNextFrame(Runnable action) {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    action.run();
                } catch (Exception e) {
                    LOG.warn("runOnNextFrame(Runnable): error executing task", e);
                } finally {
                    stop();
                }
            }
        }.start();
    }

    /**
     * Creates an observable list from the given collection. If the collection is already an instance
     * of ObservableList, it is returned as-is. Otherwise, a new ObservableList is created from
     * the contents of the given collection.
     *
     * @param <T> the type of elements contained in the collection and observable list
     * @param collection the collection to be converted to an observable list
     * @return an observable list containing the elements of the supplied collection
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends @Nullable Object> ObservableList<T> makeObservable(Collection<? extends @Nullable T> collection) {
        return collection instanceof ObservableList ol ? ol : FXCollections.observableArrayList(collection);
    }

    /**
     * Creates a new {@link Region} with specified horizontal space by setting
     * its minimum width and preferred width to the provided value.
     *
     * @param v the width value to be set as both the minimum width
     *          and preferred width of the region
     * @return a new {@link Region} with the specified horizontal space
     */
    public static Region hspace(double v) {
        Region r = new Region();
        r.setMinWidth(v);
        r.setPrefWidth(v);
        return r;
    }

    /**
     * Creates a vertical spacer Region with the specified height.
     * The minimum height and preferred height of the Region are set to the given value.
     *
     * @param v the height to set as the minimum and preferred height of the Region
     * @return a Region with the specified vertical size
     */
    public static Region vspace(double v) {
        Region r = new Region();
        r.setMinHeight(v);
        r.setPrefHeight(v);
        return r;
    }

    /**
     * Creates a bidirectional binding between a JavaFX property and a model property represented by getter and setter methods.
     * Updates to either the JavaFX property or the model property will be reflected in the other.
     *
     * @param <T> The type of the value in the properties, which must be nullable.
     * @param fxProperty The JavaFX {@code Property} to be bound bidirectionally to the model property.
     * @param getter A supplier that retrieves the current value of the model property.
     * @param setter A consumer that sets the value of the model property when the JavaFX property changes.
     * @param listenerAdder A consumer that accepts a listener, allowing it to observe changes in the model's property.
     */
    public static <T extends @Nullable Object> void bindBidirectional(
            Property<T> fxProperty,
            Supplier<T> getter,
            Consumer<T> setter,
            Consumer<Consumer<T>> listenerAdder) {

        // 1. initialize FX property from model
        fxProperty.setValue(getter.get());

        // 2. update FX property when model changes
        listenerAdder.accept(fxProperty::setValue);

        // 3. update model when FX property changes
        fxProperty.addListener((obs, oldVal, newVal) -> {
            setter.accept(newVal);
        });
    }

    /**
     * Adds a strong reference to the given property, ensuring that the specified arguments
     * are retained and not garbage collected as long as the property is active.
     *
     * @param <T> the type of the property being referenced
     * @param a the property to which a strong reference is added
     * @param args the objects to be retained by the strong reference
     */
    public static <T> void addStrongReference(Property<T> a, Object... args) {
        a.addListener(obj -> noOperation(args));
    }

    private static void noOperation(Object... args) {
        // do nothing
    }

}
