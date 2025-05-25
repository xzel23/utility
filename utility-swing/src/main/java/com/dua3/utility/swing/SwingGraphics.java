package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.data.Image;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.AttributedString;

/**
 * The SwingGraphics class implements the {@link Graphics} interface for rendering graphics in swing based applications.
 */
public class SwingGraphics implements Graphics {
    private static final Logger LOG = LogManager.getLogger(SwingGraphics.class);
    private static final AwtFontUtil FONT_UTIL = AwtFontUtil.getInstance();
    private static final AwtImageUtil IMAGE_UTIL = AwtImageUtil.getInstance();
    private static final java.awt.Font DEFAULT_FONT_AWT;
    private static final Font DEFAULT_FONT;
    private static final String INSTANCE_HAS_ALREADY_BEEN_CLOSED = "instance has already been closed!";

    // determine default font
    static {
        java.awt.Font defaultFontAwt = null;
        try {
            // Attempt to load the UIManager class
            Class<?> uiManagerClass = Class.forName("javax.swing.UIManager");

            // Check if the getDefaults method exists
            Method getFontMethod = uiManagerClass.getMethod("getFont", Object.class);
            if (getFontMethod.invoke(null, "Label.font") instanceof java.awt.Font font) {
                defaultFontAwt = font;
                LOG.debug("determined default font using UIManager: {}", defaultFontAwt);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            LOG.debug("UIManager class or UIManager.getFont() method is not available");
        }
        if (defaultFontAwt == null) {
            LOG.debug("setting default font to awt dialog font with size 12");
            defaultFontAwt = new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.PLAIN, 12);
        }
        DEFAULT_FONT = AwtFontUtil.getInstance().convert(defaultFontAwt);
        DEFAULT_FONT_AWT = defaultFontAwt;
    }

    @Override
    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    private final Graphics2D g2d;
    private final Rectangle parentBounds;
    private final AffineTransformation2f parentTransform;

    private static final class State {
        private AffineTransformation2f transform = AffineTransformation2f.IDENTITY;
        private Color strokeColor = Color.BLACK;
        private java.awt.Color awtStrokeColor = java.awt.Color.BLACK;
        private float strokeWidth = 1.0f;
        private Color fillColor = Color.BLACK;
        private java.awt.Color awtFillColor = java.awt.Color.BLACK;
        private Font font = DEFAULT_FONT;
        private java.awt.Font awtFont = DEFAULT_FONT_AWT;
        private java.awt.Color awtTextColor = SwingUtil.convert(font.getColor());
        private boolean isUnderlined = false;
        private boolean isStrikeThrough = false;
    }

    private final Line2D.Float line = new Line2D.Float();
    private final Rectangle2D.Float rect = new Rectangle2D.Float();
    private final double[] double6 = new double[6];

    private boolean isDrawing = true;

    private final State state;

    /**
     * Constructor.
     *
     * @param g2d     the Graphics2D object to render on
     * @param bounds  the bounds of the parent component
     */
    public SwingGraphics(Graphics2D g2d, Rectangle bounds) {
        this.g2d = g2d;
        this.parentBounds = bounds;
        this.parentTransform = convert(g2d.getTransform());
        this.state = new State();
    }

    @Override
    public float getWidth() {
        return (float) parentBounds.getWidth();
    }

    @Override
    public float getHeight() {
        return (float) parentBounds.getHeight();
    }

    /**
     * Convert a {@link Rectangle} object to a {@link Rectangle2f} object.
     *
     * @param r the {@link Rectangle} object to convert
     * @return a {@link Rectangle2f} object with the same position and size as the input
     */
    public Rectangle2f convert(Rectangle r) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        return Rectangle2f.of(r.x, r.y, r.width, r.height);
    }

    /**
     * Converts a {@link Rectangle} object to a {@link Rectangle2f} object.
     *
     * @param r the Rectangle object to convert
     * @return a {@link Rectangle2f} object with the same position and size as the input
     */
    public static Rectangle convert(Rectangle2f r) {
        return new Rectangle(
                Math.round(r.x()), Math.round(r.y()),
                Math.round(r.width()), Math.round(r.height())
        );
    }

    /**
     * Converts a {@link Rectangle2f} object to a Rectangle object.
     *
     *
     * <p>The difference to the {@link #convert(Rectangle2f)} is that the returned rectangle will completely cover the
     * area of the transformed source rectangle even if the covered area of pixels on the border only is covered
     * to less than 50% by the transformed source rectangle.
     *
     * @param r the {@link Rectangle2f} object to convert
     * @return a Rectangle object with the same position and size as the input
     */
    public static Rectangle convertCovering(Rectangle2f r) {
        int xMin = (int) Math.floor(r.xMin());
        int xMax = (int) Math.ceil(r.xMax());
        int yMin = (int) Math.floor(r.yMin());
        int yMax = (int) Math.ceil(r.yMax());
        return new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    /**
     * Convert an AffineTransformation2f object to an AffineTransform object.
     *
     * @param t the AffineTransformation2f object to convert
     * @return an AffineTransform object with the same transformation as the input
     */
    public static AffineTransform convert(AffineTransformation2f t) {
        return new AffineTransform(t.a(), t.d(), t.b(), t.e(), t.c(), t.f());
    }

    /**
     * Converts an instance of {@link AffineTransform} to an instance of {@link AffineTransformation2f}.
     *
     * @param t the affine transform to convert
     * @return the converted affine transformation
     */
    public AffineTransformation2f convert(AffineTransform t) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        t.getMatrix(double6);
        return new AffineTransformation2f(
                (float) double6[0], (float) double6[2], (float) double6[4], (float) double6[1], (float) double6[3], (float) double6[5]
        );
    }

    @Override
    public void reset() {
        // ignore isDrawing for reset()
        int w = parentBounds.width;
        int h = parentBounds.height;
        g2d.clipRect(0, 0, w, h);
        g2d.setColor(SwingUtil.convert(Color.TRANSPARENT_WHITE));
        g2d.fillRect(0, 0, w, h);

        isDrawing = true;
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.transform = t;
        g2d.setTransform(convert(t.append(parentTransform)));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        return state.transform;
    }

    @Override
    public void close() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        isDrawing = false;
    }

    @Override
    public void setFill(Color color) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.fillColor = color;
        state.awtFillColor = SwingUtil.convert(color);
    }

    @Override
    public Color getFill() {
        return state.fillColor;
    }

    @Override
    public void setStroke(Color c, float width) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeWidth = width;
        state.strokeColor = c;
        state.awtStrokeColor = SwingUtil.convert(state.strokeColor);
        g2d.setStroke(new BasicStroke(state.strokeWidth));
    }

    @Override
    public void setStrokeColor(Color c) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeColor = c;
        state.awtStrokeColor = SwingUtil.convert(state.strokeColor);
    }

    @Override
    public void setStrokeWidth(float width) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeWidth = width;
        g2d.setStroke(new BasicStroke(state.strokeWidth));
    }

    @Override
    public Color getStrokeColor() {
        return state.strokeColor;
    }

    @Override
    public float getStrokeWidth() {
        return state.strokeWidth;
    }

    @Override
    public void setFont(Font font) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.awtTextColor = (SwingUtil.convert(font.getColor()));
        state.isUnderlined = font.isUnderline();
        state.isStrikeThrough = font.isStrikeThrough();
        state.font = font;
        state.awtFont = FONT_UTIL.convert(font);
    }

    @Override
    public Font getFont() {
        return state.font;
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.setColor(state.awtStrokeColor);
        line.setLine(x1, y1, x2, y2);
        g2d.draw(line);
    }

    @Override
    public void strokePath(Path2f path) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.setColor(state.awtStrokeColor);
        Path2D swingPath = SwingUtil.convertToSwingPath(path);
        g2d.draw(swingPath);
    }

    @Override
    public void fillPath(Path2f path) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.setColor(state.awtFillColor);
        Path2D swingPath = SwingUtil.convertToSwingPath(path);
        g2d.fill(swingPath);
    }

    @Override
    public void drawImage(Image image, float x, float y) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.drawImage(
                IMAGE_UTIL.convert(image),
                g2d.getTransform(),
                null
        );
    }

    @Override
    public void clip(Path2f path) {
        g2d.setClip(SwingUtil.convertToSwingPath(path));
    }

    @Override
    public void clip(Rectangle2f r) {
        Path2D path = new Path2D.Float();
        path.moveTo(r.xMin(), r.yMin());
        path.lineTo(r.xMax(), r.yMin());
        path.lineTo(r.xMax(), r.yMax());
        path.closePath();
        g2d.setClip(path);
    }

    @Override
    public void resetClip() {
        g2d.setClip(null);
    }

    @Override
    public void strokeRect(float x, float y, float width, float height) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.setColor(state.awtStrokeColor);
        rect.setRect(x, y, width, height);
        g2d.draw(rect);
    }

    @Override
    public void fillRect(float x, float y, float width, float height) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        g2d.setColor(state.awtFillColor);
        rect.setRect(x, y, width, height);
        g2d.fill(rect);
    }

    @Override
    public void strokeEllipse(float x, float y, float rx, float ry, float angle) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        Vector2f p0 = Vector2f.of(x + rx, y);
        Vector2f p1 = Vector2f.of(x - rx, y);
        Vector2f r = Vector2f.of(rx, ry);

        g2d.setColor(state.awtStrokeColor);
        strokePath(Path2f.builder()
                .moveTo(p0)
                .arcTo(p1, r, angle, false, true)
                .arcTo(p0, r, angle, false, true)
                .build()
        );
    }

    @Override
    public void fillEllipse(float x, float y, float rx, float ry, float angle) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        Vector2f p0 = Vector2f.of(x + rx, y);
        Vector2f p1 = Vector2f.of(x - rx, y);
        Vector2f r = Vector2f.of(rx, ry);

        g2d.setColor(state.awtFillColor);
        fillPath(Path2f.builder()
                .moveTo(p0)
                .arcTo(p1, r, angle, false, true)
                .arcTo(p0, r, angle, false, true)
                .build()
        );
    }

    @Override
    public void drawText(CharSequence text, float x, float y) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        if (text.isEmpty()) {
            return;
        }

        g2d.setColor(state.awtTextColor);

        // Line height, derived from the font metric
        float lineHeight = g2d.getFontMetrics(state.awtFont).getHeight();

        int lineStart = 0;
        float offsetY = 0;

        for (int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == '\n' || i == text.length() - 1) {
                // Handle the last line when there is no trailing newline
                int lineEnd = (currentChar == '\n') ? i : i + 1;

                if (lineEnd > lineStart) {
                    // Extract the line using subSequence
                    CharSequence line = text.subSequence(lineStart, lineEnd);

                    // Create AttributedString for the line
                    AttributedString as = new AttributedString(line.toString()); // FIXME direct to AttributedCharacterIterator
                    as.addAttribute(TextAttribute.FONT, state.awtFont, 0, line.length());
                    as.addAttribute(TextAttribute.UNDERLINE, state.isUnderlined ? TextAttribute.UNDERLINE_ON : null, 0, line.length());
                    as.addAttribute(TextAttribute.STRIKETHROUGH, state.isStrikeThrough ? TextAttribute.STRIKETHROUGH_ON : null, 0, line.length());

                    // Draw the line
                    g2d.drawString(as.getIterator(), x, y + offsetY);
                }

                // Move to the next line
                lineStart = i + 1; // Skip '\n'
                offsetY += lineHeight;
            }
        }
    }

    @Override
    public FontUtil<?> getFontUtil() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        return FONT_UTIL;
    }
}
