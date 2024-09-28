package com.dua3.utility.fx;

import com.dua3.utility.ui.Graphics;
import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Text;

/**
 * The FxGraphics class implements the {@link Graphics} interface for rendering graphics in JavaFX based applications.
 */
public class FxGraphics implements Graphics {
    private static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();
    private static final javafx.scene.text.Font DEFAULT_FONT_FX = javafx.scene.text.Font.getDefault();
    private static final Font DEFAULT_FONT = FONT_UTIL.convert(DEFAULT_FONT_FX);

    private final GraphicsContext gc;
    private final float width;
    private final float height;
    private final AffineTransformation2f parentTransform;

    private boolean isDrawing = true;

    private AffineTransformation2f transform;

    private float scale;
    private javafx.scene.paint.Color textColor = javafx.scene.paint.Color.BLACK;
    private javafx.scene.text.Font font = DEFAULT_FONT_FX;
    private boolean isStrikeThrough = false;
    private boolean isUnderline = false;
    private javafx.scene.paint.Paint strokeColor = javafx.scene.paint.Color.BLACK;
    private double strokeWidth = 1.0;
    private javafx.scene.paint.Color fillColor = javafx.scene.paint.Color.BLACK;

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
    }

    @Override
    public FontUtil<?> getFontUtil() {
        assert isDrawing : "instance has already been closed!";

        return FONT_UTIL;
    }

    @Override
    public Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    @Override
    public Rectangle2f getBounds() {
        assert isDrawing : "instance has already been closed!";
        return new Rectangle2f(0, 0, width, height);
    }

    @Override
    public Rectangle2f getTextDimension(CharSequence text) {
        assert isDrawing : "instance has already been closed!";

        return FONT_UTIL.getTextDimension(text, font);
    }

    @Override
    public void close() {
        assert isDrawing : "instance has already been closed!";

        isDrawing = false;
    }

    @Override
    public void strokeRect(float x, float y, float w, float h) {
        assert isDrawing : "instance has already been closed!";

        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);
        gc.strokeRect(x, y, w, h);
    }

    @Override
    public void fillRect(float x, float y, float w, float h) {
        assert isDrawing : "instance has already been closed!";

        gc.setFill(fillColor);
        gc.fillRect(x, y, w, h);
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2) {
        assert isDrawing : "instance has already been closed!";

        gc.setStroke(strokeColor);
        gc.setLineWidth(strokeWidth);
        gc.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void setStroke(Color c, float width) {
        assert isDrawing : "instance has already been closed!";

        this.strokeColor = FxUtil.convert(c);
        this.strokeWidth = width;
    }

    @Override
    public void setFill(Color c) {
        assert isDrawing : "instance has already been closed!";

        this.fillColor = FxUtil.convert(c);
    }

    @Override
    public void setTransformation(AffineTransformation2f t) {
        assert isDrawing : "instance has already been closed!";

        this.transform = t;
        gc.setTransform(FxUtil.convert(t.append(parentTransform)));
    }

    @Override
    public AffineTransformation2f getTransformation() {
        assert isDrawing : "instance has already been closed!";

        return transform;
    }

    @Override
    public void setFont(Font font) {
        assert isDrawing : "instance has already been closed!";

        this.textColor = FxUtil.convert(font.getColor());
        this.font = FONT_UTIL.convert(font.scaled(scale));
        this.isStrikeThrough = font.isStrikeThrough();
        this.isUnderline = font.isUnderline();
    }

    @Override
    public void drawText(CharSequence text, float x, float y) {
        assert isDrawing : "instance has already been closed!";

        gc.setFont(font);
        gc.setFill(textColor);
        gc.fillText(text.toString(), x, y);

        if (isStrikeThrough || isUnderline) {
            double strokeWidth = font.getSize() / 15.0f;

            Text t = new Text(text.toString());
            t.setFont(font);
            Bounds r = t.getBoundsInLocal();
            double wStroke = r.getWidth();

            gc.setStroke(textColor);
            gc.setLineWidth(strokeWidth);

            if (isUnderline) {
                double yStroke = y + r.getMaxY() / 2.0f;
                gc.strokeLine(x, yStroke, (double) x +wStroke, yStroke);
            }
            if (isStrikeThrough) {
                double yStroke = y + r.getMinY() / 2.0f + r.getMaxY();
                gc.strokeLine(x, yStroke, (double) x +wStroke, yStroke);
            }
        }
    }

}
