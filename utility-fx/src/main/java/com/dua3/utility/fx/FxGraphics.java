package com.dua3.utility.fx;

import com.dua3.utility.data.Image;
import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.ClosePath2f;
import com.dua3.utility.math.geometry.Curve2f;
import com.dua3.utility.math.geometry.Line2f;
import com.dua3.utility.math.geometry.MoveTo2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

/**
 * The FxGraphics class implements the {@link Graphics} interface for rendering graphics in JavaFX based applications.
 */
public class FxGraphics implements Graphics {
    private static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();
    private static final FxImageUtil IMAGE_UTIL = FxImageUtil.getInstance();
    private static final Font DEFAULT_FONT = FONT_UTIL.getDefaultFont();
    private static final javafx.scene.text.Font DEFAULT_FONT_FX = FONT_UTIL.convert(DEFAULT_FONT);
    private static final String INSTANCE_HAS_ALREADY_BEEN_CLOSED = "instance has already been closed!";
    private static final String UNSUPPORTED_NUMBER_OF_CONTROL_POINTS = "Unsupported number of control points: ";
    private static final String UNSUPPORTED_SEGMENT_TYPE = "Unsupported segment type: ";

    private final GraphicsContext gc;
    private final AffineTransformation2f parentTransform;
    private final float scale;
    private float width;
    private float height;

    private boolean isDrawing = true;

    private static final class State {
        AffineTransformation2f transform = AffineTransformation2f.identity();

        Font font = DEFAULT_FONT;
        javafx.scene.text.Font fxFont = DEFAULT_FONT_FX;
        javafx.scene.paint.Color fxTextColor = FxUtil.convert(font.getColor());

        boolean isStrikeThrough = false;
        boolean isUnderline = false;

        Color strokeColor = Color.BLACK;
        javafx.scene.paint.Paint fxStrokeColor = javafx.scene.paint.Color.BLACK;
        float strokeWidth = 1.0f;

        Color fillColor = Color.BLACK;
        javafx.scene.paint.Color fxFillColor = javafx.scene.paint.Color.BLACK;
    }

    private final State state = new State();

    private void applyCurrentState() {
        gc.setTransform(FxUtil.convert(state.transform.append(parentTransform)));

        gc.setFill(state.fxFillColor);
        gc.setStroke(state.fxStrokeColor);
        gc.setLineWidth(state.strokeWidth);
    }

    /**
     * Constructs an FxGraphics instance using the provided Canvas object.
     *
     * @param canvas the Canvas object used to initialize the graphics instance
     */
    public FxGraphics(Canvas canvas) {
        this(canvas.getGraphicsContext2D(), (float) canvas.getWidth(), (float) canvas.getHeight());
    }

    /**
     * Creates a new instance of FxGraphics with the given parameters.
     *
     * @param gc      the GraphicsContext object
     * @param width       the width of the graphics object
     * @param height  the height of the graphics object
     */
    public FxGraphics(GraphicsContext gc, float width, float height) {
        this.gc = gc;
        this.width = width;
        this.height = height;
        this.scale = 1.0f;
        this.parentTransform = FxUtil.convert(gc.getTransform());
        gc.save();
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public FontUtil<?> getFontUtil() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        return FONT_UTIL;
    }

    @Override
    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    @Override
    public void reset() {
        width = (float) gc.getCanvas().getWidth();
        height = (float) gc.getCanvas().getHeight();
        gc.clearRect(0, 0, width, height);
        isDrawing = true;
    }

    @Override
    public void close() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        isDrawing = false;
    }

    @Override
    public void strokeRect(float x, float y, float w, float h) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.setStroke(state.fxStrokeColor);
        gc.setLineWidth(state.strokeWidth);
        gc.strokeRect(x, y, w, h);
    }

    @Override
    public void fillRect(float x, float y, float w, float h) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.setFill(state.fxFillColor);
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void strokeEllipse(float x, float y, float rx, float ry, float angle) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        Vector2f p0 = Vector2f.of(x + rx, y);
        Vector2f p1 = Vector2f.of(x - rx, y);
        Vector2f r = Vector2f.of(rx, ry);

        gc.setStroke(state.fxStrokeColor);
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

        gc.setFill(state.fxFillColor);
        fillPath(Path2f.builder()
                .moveTo(p0)
                .arcTo(p1, r, angle, false, true)
                .arcTo(p0, r, angle, false, true)
                .build()
        );
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.setStroke(state.fxStrokeColor);
        gc.setLineWidth(state.strokeWidth);
        gc.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void strokePath(Path2f path) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.beginPath();
        path(path);
        gc.stroke();
    }

    @Override
    public void fillPath(Path2f path) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.beginPath();
        path(path);
        gc.fill();
    }

    @Override
    public void drawImage(Image image, float x, float y) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.drawImage(IMAGE_UTIL.convert(image), x, y);
    }

    @Override
    public void clip(Path2f path) {
        gc.beginPath();
        path(path);
        gc.clip();
    }

    @Override
    public void clip(Rectangle2f r) {
        gc.beginPath();
        gc.rect(r.x(), r.y(), r.width(), r.height());
        gc.clip();
    }

    @Override
    public void resetClip() {
        gc.restore();
        gc.save();
        applyCurrentState();
    }

    private void path(Path2f path) {
        path.segments().forEach(segment -> {
            switch (segment) {
                case MoveTo2f s -> gc.moveTo(s.end().x(), s.end().y());
                case Line2f s -> gc.lineTo(s.end().x(), s.end().y());
                case Curve2f s -> {
                    switch (s.numberOfControls()) {
                        case 3 -> gc.quadraticCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y()
                        );
                        case 4 -> gc.bezierCurveTo(
                                s.control(1).x(), s.control(1).y(),
                                s.control(2).x(), s.control(2).y(),
                                s.control(3).x(), s.control(3).y()
                        );
                        default ->
                                throw new IllegalArgumentException(UNSUPPORTED_NUMBER_OF_CONTROL_POINTS + s.numberOfControls());
                    }
                }
                case Arc2f s -> Graphics.approximateArc(s, this::moveTo, this::generateBezierSegment);
                case ClosePath2f s -> gc.closePath();
                default ->
                        throw new IllegalArgumentException(UNSUPPORTED_SEGMENT_TYPE + segment.getClass().getName());
            }
        });
    }

    private void moveTo(Vector2f p) {
        gc.moveTo(p.x(), p.y());
    }

    private void generateBezierSegment(Vector2f[] points) {
        gc.bezierCurveTo(
                points[0].x(), points[0].y(),
                points[1].x(), points[1].y(),
                points[2].x(), points[2].y()
        );
    }

    @Override
    public void setStroke(Color c, float width) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeColor = c;
        state.fxStrokeColor = FxUtil.convert(state.strokeColor);
        state.strokeWidth = width;

        gc.setStroke(state.fxStrokeColor);
        gc.setLineWidth(state.strokeWidth);
    }

    @Override
    public void setStrokeColor(Color c) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeColor = c;
        state.fxStrokeColor = FxUtil.convert(state.strokeColor);

        gc.setStroke(state.fxStrokeColor);
    }

    @Override
    public void setStrokeWidth(float width) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.strokeWidth = width;
        gc.setLineWidth(state.strokeWidth);
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
    public void setFill(Color c) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.fillColor = c;
        state.fxFillColor = FxUtil.convert(state.fillColor);
        gc.setFill(state.fxFillColor);
    }

    @Override
    public Color getFill() {
        return state.fillColor;
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.transform = t;
        gc.setTransform(FxUtil.convert(t.append(parentTransform)));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        return state.transform;
    }

    @Override
    public void setFont(Font font) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        state.fxTextColor = FxUtil.convert(font.getColor());
        state.font = font;
        state.fxFont = FONT_UTIL.convert(state.font.scaled(scale));
        state.isStrikeThrough = font.isStrikeThrough();
        state.isUnderline = font.isUnderline();
    }

    @Override
    public Font getFont() {
        return state.font;
    }

    @Override
    public void drawText(CharSequence text, float x, float y) {
        assert isDrawing : INSTANCE_HAS_ALREADY_BEEN_CLOSED;

        gc.setFont(state.fxFont);
        gc.setFill(state.fxTextColor);
        gc.fillText(text.toString(), x, y);

        if (state.isStrikeThrough || state.isUnderline) {
            double strokeWidth = state.fxFont.getSize() / 15.0f;

            Text t = new Text(text.toString());
            t.setFont(state.fxFont);
            Bounds r = t.getBoundsInLocal();
            double wStroke = r.getWidth();

            gc.setStroke(state.fxTextColor);
            gc.setLineWidth(strokeWidth);

            if (state.isUnderline) {
                double yStroke = y + r.getMaxY() / 2.0f;
                gc.strokeLine(x, yStroke, x + wStroke, yStroke);
            }
            if (state.isStrikeThrough) {
                double yStroke = y + r.getMinY() / 2.0f + r.getMaxY();
                gc.strokeLine(x, yStroke, x + wStroke, yStroke);
            }
        }
    }

}
