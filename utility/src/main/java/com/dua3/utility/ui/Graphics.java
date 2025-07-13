package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.math.MathUtil;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.VerticalAlignment;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;

/**
 * A generic interface defining drawing commands.
 * <p>
 * The Graphics interface provides an abstraction from the underlying rendering toolkit.
 */
public interface Graphics extends AutoCloseable {

    /**
     * Get the width of this {@code Graphics} instance.
     *
     * @return the width as a float value
     */
    float getWidth();

    /**
     * Get the height of this {@code Graphics} instance.
     *
     * @return the height as a float.
     */
    float getHeight();

    /**
     * Retrieves the FontUtil compatible with this Graphics object.
     *
     * @return the FontUtil object
     */
    FontUtil<?> getFontUtil();

    /**
     * Retrieves the default font for the graphics object.
     *
     * @return the default font
     */
    Font getDefaultFont();

    /**
     * Get the dimension (size) of this {code Graphics} instance in screen units.
     *
     * @return the bounding rectangle
     */
    default Dimension2f getDimension() {
        return new Dimension2f(getWidth(), getHeight());
    }

    /**
     * Resets the state of the object and clears it to a transparent color.
     */
    void reset();

    /**
     * Draws the specified image at the given coordinates.
     *
     * @param image The image to be drawn.
     * @param x The x-coordinate of the top-left corner where the image will be drawn.
     * @param y The y-coordinate of the top-left corner where the image will be drawn.
     */
    void drawImage(Image image, float x, float y);

    /**
     * Draws the specified image at the given coordinates.
     *
     * @param image The image to be drawn.
     * @param p The top-left corner where the image will be drawn.
     */
    default void drawImage(Image image, Vector2f p) {
        drawImage(image, p.x(), p.y());
    }

    /**
     * Stroke rectangle.
     * @param r the rectangle
     */
    default void strokeRect(Rectangle2f r) {
        strokeRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    /**
     * Stroke rectangle.
     * @param pos the position
     * @param dim the size
     */
    default void strokeRect(Vector2f pos, Dimension2f dim) {
        strokeRect(pos.x(), pos.y(), dim.width(), dim.height());
    }

    /**
     * Draws a series of connected lines, forming a polyline, between the given vertices.
     *
     * @param vertices An array of {@link Vector2f} instances representing the vertices in sequential order.
     */
    default void strokePolyLines(Vector2f... vertices) {
        for (int i = 0; i < vertices.length - 1; i++) {
            strokeLine(vertices[i], vertices[i + 1]);
        }
    }

    /**
     * Strokes a polygon by connecting its vertices and closing the shape by linking the last vertex to the first.
     *
     * @param vertices the sequence of 2D points representing the vertices of the polygon to be stroked
     */
    default void strokePolygon(Vector2f... vertices) {
        strokePolyLines(vertices);
        if (vertices.length > 1) {
            strokeLine(vertices[vertices.length - 1], vertices[0]);
        }
    }

    /**
     * Draws the outline of a rectangle with the specified dimensions and coordinates.
     *
     * @param x the x-coordinate of the top-left corner of the rectangle
     * @param y the y-coordinate of the top-left corner of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    void strokeRect(float x, float y, float w, float h);

    /**
     * Fill rectangle.
     *
     * @param r the rectangle
     */
    default void fillRect(Rectangle2f r) {
        fillRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    /**
     * Fills a rectangle with the specified dimensions and coordinates.
     *
     * @param x the x-coordinate of the top-left corner of the rectangle
     * @param y the y-coordinate of the top-left corner of the rectangle
     * @param w the width of the rectangle
     * @param h the height of the rectangle
     */
    void fillRect(float x, float y, float w, float h);

    /**
     * Draws the outline of a circle centered at the specified coordinates with the given radius.
     *
     * @param x The x-coordinate of the circle's center.
     * @param y The y-coordinate of the circle's center.
     * @param r The radius of the circle.
     */
    default void strokeCircle(float x, float y, float r) {
        strokeEllipse(x, y, r, r, 0);
    }

    /**
     * Draws the outline of a circle centered at the specified coordinates with the given radius.
     *
     * @param c the center of the circle
     * @param r The radius of the circle.
     */
    default void strokeCircle(Vector2f c, float r) {
        strokeEllipse(c.x(), c.y(), r, r, 0);
    }

    /**
     * Draws the outline of an ellipse on the canvas with the specified center coordinates and radii.
     *
     * @param x  The x-coordinate of the center of the ellipse.
     * @param y  The y-coordinate of the center of the ellipse.
     * @param rx The horizontal radius (semi-major axis) of the ellipse.
     * @param ry The vertical radius (semi-minor axis) of the ellipse.
     * @param angle The rotation angle.
     */
    void strokeEllipse(float x, float y, float rx, float ry, float angle);

    /**
     * Fill a circle centered at the specified coordinates with the given radius.
     *
     * @param x The x-coordinate of the circle's center.
     * @param y The y-coordinate of the circle's center.
     * @param r The radius of the circle.
     */
    default void fillCircle(float x, float y, float r) {
        fillEllipse(x, y, r, r, 0);
    }

    /**
     * Fill a circle centered at the specified coordinates with the given radius.
     *
     * @param c The center of the circle.
     * @param r The radius of the circle.
     */
    default void fillCircle(Vector2f c, float r) {
        fillEllipse(c.x(), c.y(), r, r, 0);
    }

    /**
     * Fill an ellipse on the canvas with the specified center coordinates and radii.
     *
     * @param x  The x-coordinate of the center of the ellipse.
     * @param y  The y-coordinate of the center of the ellipse.
     * @param rx The horizontal radius (semi-major axis) of the ellipse.
     * @param ry The vertical radius (semi-minor axis) of the ellipse.
     * @param angle The rotation angle.
     */
    void fillEllipse(float x, float y, float rx, float ry, float angle);

    /**
     * Draws a line between two specified points.
     *
     * @param a the starting point of the line
     * @param b the ending point of the line
     */
    default void strokeLine(Vector2f a, Vector2f b) {
        strokeLine(a.x(), a.y(), b.x(), b.y());
    }

    /**
     * Draws a line on the graphics context from the specified starting point (x1, y1) to
     * the specified ending point (x2, y2).
     *
     * @param x1 the x-coordinate of the starting point
     * @param y1 the y-coordinate of the starting point
     * @param x2 the x-coordinate of the ending point
     * @param y2 the y-coordinate of the ending point
     */
    void strokeLine(float x1, float y1, float x2, float y2);

    /**
     * Strokes the outline of the specified path using the current stroke settings.
     *
     * @param path the path to be stroked
     */
    void strokePath(Path2f path);

    /**
     * Fills the specified path using the current fill color and settings.
     *
     * @param path the path to be filled
     */
    void fillPath(Path2f path);

    /**
     * Intersects the clip area with the specified path.
     *
     * @param path the path that determines the clipping area
     */
    void clip(Path2f path);

    /**
     * Intersects the clip area with the specified rectangle.
     *
     * @param r the rectangle to intersect the clip area with
     */
    void clip(Rectangle2f r);

    /**
     * Resets the clipping area to the default state.
     * This method clears any existing clipping boundaries that
     * might have been set previously and restores the clip to
     * encompass the entire drawing area.
     */
    void resetClip();

    /**
     * Sets the stroke for drawing shapes. The stroke determines the color and width of the lines used to outline shapes.
     *
     * @param c     the color of the stroke
     * @param width the width of the stroke, in pixels
     */
    void setStroke(Color c, float width);

    /**
     * Sets the stroke color for drawing operations.
     *
     * @param c the color to be used for the stroke; must not be null
     */
    void setStrokeColor(Color c);

    /**
     * Sets the width for the stroke to be applied in drawing operations.
     *
     * @param width The stroke width to set, specified as a float value. It must be a positive number
     *              representing the desired thickness of the stroke.
     */
    void setStrokeWidth(float width);

    /**
     * Retrieves the stroke color used for drawing shapes or lines.
     *
     * @return the current stroke color as a Color object.
     */
    Color getStrokeColor();

    /**
     * Retrieves the current stroke width value.
     *
     * @return the width of the stroke as a floating-point value
     */
    float getStrokeWidth();

    /**
     * Sets the fill color for drawing operations.
     *
     * @param c the color to set as the fill color
     */
    void setFill(Color c);

    /**
     * Retrieves the fill color of a given object.
     *
     * @return the current fill color as a Color object.
     */
    Color getFill();

    /**
     * Sets the transformation for the graphics context.
     *
     * @param t the affine transformation to set
     */
    void setTransformation(AffineTransformation2f t);

    /**
     * Apply the given transformations to this {@code Graphics} instance.
     *
     * <p>These two are equivalent:
     * <pre>
     * {@code
     *     AffineTransformation2f t = g.transform(t1, t2);
     * }
     * </pre>
     * and
     * <pre>
     * {@code
     *     AffineTransformation2f t = g.getTransformation();
     *     setTransformation(AffineTransformation2f.combine(t1, t2, t);
     * }
     * </pre>
     *
     * @param t the transformation to add
     * @return the transformation that was active before updating
     */
    default AffineTransformation2f transform(AffineTransformation2f... t) {
        if (t.length == 0) {
            return getTransformation();
        }

        AffineTransformation2f[] transformations = Arrays.copyOf(t, t.length + 1);
        transformations[t.length] = getTransformation();
        setTransformation(AffineTransformation2f.combine(transformations));

        return transformations[t.length];
    }

    /**
     * Sets the font used for text rendering.
     *
     * @param f the font to set
     */
    void setFont(Font f);

    /**
     * Get the current font.
     *
     * @return the current font
     */
    Font getFont();

    /**
     * Draws the specified text at the given coordinates.
     *
     * <p>This is equivalent to calling
     * {@code drawText(text, x, y, HAnchor.LEFT, VAnchor.BASELINE)}
     *
     * @param text the text to be drawn
     * @param x the x-coordinate of the starting point
     * @param y the y-coordinate of the starting point
     */
    void drawText(CharSequence text, float x, float y);

    /**
     * Draws the specified text at the given position.
     *
     * @param text the text to be drawn
     * @param p the starting point
     */
    default void drawText(CharSequence text, Vector2f p) {
        drawText(text, p.x(), p.y());
    }

    /**
     * Calculates the transformation of a point.
     *
     * <p>The transforms a point given in local coordinates to user coordinates.
     *
     * @param p the point to transform
     * @return the point transformed to local coordinates as described above
     */
    default Vector2f transform(Vector2f p) {
        return getTransformation().transform(p);
    }

    /**
     * Calculates the transformation of a point.
     *
     * <p>The transforms a point given in local coordinates to user coordinates.
     *
     * @param x the x-coordinate of the point
     * @param y the x-coordinate of the point
     * @return the point transformed to local coordinates as described above
     */
    default Vector2f transform(float x, float y) {
        return getTransformation().transform(x, y);
    }

    /**
     * Calculates the inverse transformation of a point.
     *
     * <p>The method applies the inverse transformation to convert a point in user coordinates
     * back to local coordinates. In the resulting local space, (0, 0) represents the top-left
     * corner of the output area, and the coordinates are measured in device units (e.g., pixels).
     *
     * @param p the point to transform, specified in user coordinates
     * @return the point transformed to local coordinates as described above
     */
    default Vector2f inverseTransform(Vector2f p) {
        return getInverseTransformation().transform(p);
    }

    /**
     * Calculates the inverse transformation of a point.
     *
     * <p>The method applies the inverse transformation to convert a point in user coordinates
     * back to local coordinates. In the resulting local space, (0, 0) represents the top-left
     * corner of the output area, and the coordinates are measured in device units (e.g., pixels).
     *
     * @param x the x-coordinate of the point, specified in user coordinates
     * @param y the x-coordinate of the point, specified in user coordinates
     * @return the point transformed to local coordinates as described above
     */
    default Vector2f inverseTransform(float x, float y) {
        return getInverseTransformation().transform(x, y);
    }

    /**
     * Get the inverse transformation for this {@code Graphics} instance.
     * @return the inverse of the current transformation
     */
    default AffineTransformation2f getInverseTransformation() {
        return getTransformation()
                .inverse()
                .orElseThrow(() -> new IllegalStateException("no inverse transformation available"));
    }

    /**
     * Enum representing horizontal anchor points for aligning text or graphics elements relative to a reference point.
     */
    enum HAnchor {
        /**
         * Aligns element to the left side.
         */
        LEFT,
        /**
         * Aligns element to the right side.
         */
        RIGHT,
        /**
         * Centers element text horizontally.
         */
        CENTER
    }

    /**
     * Enum representing vertical anchor points for aligning text or graphics elements relative to a reference point.
     */
    enum VAnchor {
        /**
         * Align the top of the element.
         */
        TOP,
        /**
         * Align the bottom of the element.
         */
        BOTTOM,
        /**
         * Aligns the baseline of the element.
         */
        BASELINE,
        /**
         * Align the middle of the element relative to the reference point.
         */
        MIDDLE
    }

    /**
     * Draw text at the specified coordinates with the given horizontal and vertical anchor.
     *
     * @param text     the text to be drawn
     * @param x        the x-coordinate of the starting position for the text
     * @param y        the y-coordinate of the starting position for the text
     * @param hAnchor  the horizontal anchor for the text position (LEFT, RIGHT, or CENTER)
     * @param vAnchor  the vertical anchor for the text position (TOP, BOTTOM, BASELINE, or MIDDLE)
     */
    default void drawText(CharSequence text, float x, float y, HAnchor hAnchor, VAnchor vAnchor) {
        Rectangle2f r = getFontUtil().getTextDimension(text, getFont());

        float tx;
        float ty;

        tx = switch (hAnchor) {
            case LEFT -> x;
            case RIGHT -> x - r.width();
            case CENTER -> x - r.width() / 2;
        };

        ty = switch (vAnchor) {
            case TOP -> y - r.yMin();
            case BOTTOM -> y - r.yMax();
            case BASELINE -> y;
            case MIDDLE -> y + r.height() / 2 - r.yMax();
        };

        drawText(text, tx, ty);
    }

    /**
     * Retrieves the affine transformation of the graphics object.
     *
     * @return the affine transformation of the graphics object
     */
    AffineTransformation2f getTransformation();

    /**
     * Enum defining the supported text rotation modes.
     */
    enum TextRotationMode {
        /**
         * Rotate the output area as a whole, i.e., the text is rendered the same as with no rotation applied and
         * then the whole block is rotated with the anchor as pivot. This means that for example when rotating
         * counter-clockwise with {@link HAnchor#LEFT} and {@link VAnchor#BOTTOM}, the upper left corner will be
         * to the left of the pivot's x-coordinate.
         */
        ROTATE_OUTPUT_AREA,
        /**
         * Rotate the text as a whole, then translate the text to be placed inside the output area
         */
        ROTATE_AND_TRANSLATE,
        /**
         * Rotate each line independently. Align lines horizontally, i.e., all lines start at the same y-coordinate.
         */
        ROTATE_LINES,
        /**
         * Rotate each line independently, then translate the rotated lines into the original output area.
         */
        ROTATE_AND_TRANSLATE_LINES,
    }

    /**
     * Enum defining the different text wrapping modes.
     */
    enum TextWrapping {
        /**
         * Wrap text when the current line width exceeds the output area width.
         */
        WRAP,
        /**
         * Do not wrap text.
         */
        NO_WRAP
    }

    /**
     * Renders the given text at the specified position with alignment, anchoring, and wrapping options.
     *
     * @param pos the position where the text should be rendered, represented as a 2D vector
     * @param text the rich text object containing the text and its formatting details
     * @param hAnchor the horizontal anchoring reference for positioning the text
     * @param vAnchor the vertical anchoring reference for positioning the text
     * @param hAlign the horizontal alignment of the text within its bounds
     * @param vAlign the vertical alignment of the text within its bounds
     * @param outputDimension the dimension representing the maximum width and height available for rendering
     * @param wrapText the text wrapping mode, which determines how the text should wrap within the available width
     */
    default void renderText(
            Vector2f pos,
            RichText text,
            HAnchor hAnchor,
            VAnchor vAnchor,
            Alignment hAlign,
            VerticalAlignment vAlign,
            Dimension2f outputDimension,
            TextWrapping wrapText) {
        float width = outputDimension.width();
        float height = outputDimension.height();

        float wrapWidth = switch (wrapText) {
            case WRAP -> width;
            case NO_WRAP -> FragmentedText.NO_WRAP;
        };
        FragmentedText fragments = FragmentedText.generateFragments(text, getFontUtil(), getFont(), width, height, hAlign, vAlign, hAnchor, vAnchor, wrapWidth);

        renderFragments(
                pos,
                fragments,
                0.0
        );
    }

    /**
     * Renders the given text within the specified bounding rectangle using the provided font,
     * alignment, wrapping, and rotation settings.
     *
     * @param pos             the position
     * @param text            the text to be rendered
     * @param hAnchor         the horizontal anchor setting
     * @param vAnchor         the vertical anchor setting
     * @param hAlign          the horizontal alignment of the text within the bounding rectangle
     * @param vAlign          the vertical alignment of the text within the bounding rectangle
     * @param outputDimension the dimension of the output area
     * @param wrapText        true, to apply automatic text wrapping
     * @param angle           the rotation angle in radians
     * @param mode            the {@link TextRotationMode} to use
     */
    default void renderText(
            Vector2f pos,
            RichText text,
            HAnchor hAnchor,
            VAnchor vAnchor,
            Alignment hAlign,
            VerticalAlignment vAlign,
            Dimension2f outputDimension,
            TextWrapping wrapText,
            double angle,
            TextRotationMode mode) {
        // normalize angle to the range [0, 2pi)
        angle = MathUtil.normalizeRadians(angle);

        float width = outputDimension.width();
        float height = outputDimension.height();

        // layout text
        float wrapWidth = switch (wrapText) {
            case WRAP -> width;
            case NO_WRAP -> FragmentedText.NO_WRAP;
        };

        switch (mode) {
            case ROTATE_OUTPUT_AREA -> {
                FragmentedText fragments = FragmentedText.generateFragments(text, getFontUtil(), getFont(), width, height, hAlign, vAlign, hAnchor, vAnchor, wrapWidth);
                AffineTransformation2f t = transform(AffineTransformation2f.rotate(angle, pos));
                renderFragments(
                        pos,
                        fragments,
                        0.0
                );
                setTransformation(t);
            }
            case ROTATE_AND_TRANSLATE -> {
                FragmentedText fragments = FragmentedText.generateFragments(text, getFontUtil(), getFont(), width, height, hAlign, vAlign, hAnchor, vAnchor, wrapWidth);
                Vector2f tl = getBlockTranslation(AffineTransformation2f.rotate(angle), fragments, hAnchor, vAnchor);
                AffineTransformation2f rotate = AffineTransformation2f.rotate(angle, pos);
                AffineTransformation2f transform = transform(rotate, AffineTransformation2f.translate(tl));
                renderFragments(pos, fragments, 0.0);
                setTransformation(transform);
            }
            case ROTATE_LINES -> {
                FragmentedText fragments = FragmentedText.generateFragments(text, getFontUtil(), getFont(), width, height, hAlign, vAlign, hAnchor, vAnchor, wrapWidth);
                renderFragments(pos, fragments, angle);
            }
            case ROTATE_AND_TRANSLATE_LINES -> {
                record OctantSettings(HAnchor hAnchor,
                                      VAnchor vAnchor,
                                      float fx,
                                      float fy
                ) {}

                OctantSettings[] octs = {
                        new OctantSettings(HAnchor.LEFT, VAnchor.TOP, 0, 0),
                        new OctantSettings(HAnchor.LEFT, VAnchor.BOTTOM, 0, 0),
                        new OctantSettings(HAnchor.LEFT, VAnchor.TOP, 1, 0),
                        new OctantSettings(HAnchor.LEFT, VAnchor.BOTTOM, 1, 0),
                        new OctantSettings(HAnchor.LEFT, VAnchor.TOP, 1, 1),
                        new OctantSettings(HAnchor.LEFT, VAnchor.BOTTOM, 1, 1),
                        new OctantSettings(HAnchor.LEFT, VAnchor.TOP, 0, 1),
                        new OctantSettings(HAnchor.LEFT, VAnchor.BOTTOM, 0, 1),
                };

                OctantSettings oct = octs[MathUtil.octantIndexRadians(angle)];
                FragmentedText fragments = FragmentedText.generateFragments(
                        text,
                        getFontUtil(),
                        getFont(),
                        width,
                        height,
                        Alignment.LEFT,
                        VerticalAlignment.TOP,
                        oct.hAnchor,
                        oct.vAnchor,
                        wrapWidth
                );

                renderFragments(
                        Vector2f.of(pos.x() + oct.fx() * width, pos.y() + oct.fy() * height),
                        fragments,
                        angle
                );
            }
        }
    }

    /**
     * Calculates the translation vector required to align a fragmented text block
     * based on a specified horizontal and vertical anchor, after applying a given
     * affine transformation to the block.
     *
     * @param at the affine transformation applied to the fragmented text.
     * @param fragments the fragmented text containing geometric and structural data.
     * @param hAnchor the horizontal anchor indicating alignment (e.g., LEFT, RIGHT, CENTER).
     * @param vAnchor the vertical anchor indicating alignment (e.g., TOP, BOTTOM, MIDDLE, BASELINE).
     * @return a {@link Vector2f} representing the computed translation to align the text block.
     */
    private static Vector2f getBlockTranslation(AffineTransformation2f at, FragmentedText fragments, HAnchor hAnchor, VAnchor vAnchor) {
        Rectangle2f r = fragments.getTextRec();
        DoubleBinaryOperator reduceX = switch (hAnchor) {
            case LEFT -> Math::min;
            case RIGHT -> Math::max;
            case CENTER -> Double::sum;
        };
        DoubleBinaryOperator reduceY = switch (vAnchor) {
            case TOP -> Math::min;
            case BOTTOM, BASELINE -> Math::max;
            case MIDDLE -> Double::sum;
        };

        List<Vector2f> corners = List.of(
                at.transform(new Vector2f(r.x(), r.y())),
                at.transform(new Vector2f(r.x() + r.width(), r.y())),
                at.transform(new Vector2f(r.x(), r.y() + r.height())),
                at.transform(new Vector2f(r.x() + r.width(), r.y() + r.height()))
        );

        float fx = hAnchor == HAnchor.CENTER ? 0.25f : 1.0f;
        float fy = vAnchor == VAnchor.MIDDLE ? 0.25f : 1.0f;

        float x = fx * (float) corners.stream().mapToDouble(Vector2f::x).reduce(reduceX).orElse(0.0);
        float y = fy * (float) corners.stream().mapToDouble(Vector2f::y).reduce(reduceY).orElse(0.0);

        float tx = switch (hAnchor) {
            case LEFT -> -x;
            case RIGHT -> fragments.width() - x;
            case CENTER -> 0.5f * fragments.width() - x;
        };

        float ty = switch (vAnchor) {
            case TOP -> -y;
            case BOTTOM, BASELINE -> fragments.height() - y;
            case MIDDLE -> 0.5f * fragments.height() - y;
        };

        return Vector2f.of(tx, ty);
    }

    /**
     * Renders text fragments within the specified bounding rectangle. The text fragments are provided as a list of fragment lines.
     * Each fragment line contains a list of fragments that are either whitespace or text with uniform attributes (font, text decoration).
     * The method calculates the positioning of each fragment based on the alignment and distributes whitespace and remaining space accordingly.
     * The rendered text is drawn on the graphics context.
     *
     * @param pos   the rendering position
     * @param text  a list of fragment lines, where each line contains a list of fragments
     * @param angle the angle in radians to rotate each line (must be normalized)
     */
    private void renderFragments(
            Vector2f pos,
            FragmentedText text,
            double angle
    ) {
        assert 0 <= angle && angle < MathUtil.TWO_PI : "invalid angle: " + angle;

        // get font and transformation
        Font font = getFont();
        AffineTransformation2f t = getTransformation();

        try {
            float sxY;
            float sxH;

            if (angle == 0.0) {
                transform(AffineTransformation2f.translate(pos));
                sxY = 0.0f;
                sxH = 0.0f;
            } else {
                transform(AffineTransformation2f.translate(pos), AffineTransformation2f.rotate(angle, pos));
                int layoutCase = (int) (angle / MathUtil.PI_QUARTER) % 4;
                switch (layoutCase) {
                    case 0 -> {
                        sxY = (float) (Math.tan(angle));
                        sxH = sxY;
                    }
                    case 1 -> {
                        sxY = (float) (Math.tan(angle + MathUtil.PI_HALF));
                        sxH = 0;
                    }
                    case 2 -> {
                        sxY = (float) (Math.tan(angle + MathUtil.PI_HALF));
                        sxH = sxY;
                    }
                    case 3 -> {
                        sxY = (float) (Math.tan(angle));
                        sxH = 0;
                    }
                    default -> throw new IllegalStateException("invalid octant");
                }
            }

            List<List<FragmentedText.Fragment>> lines = text.lines();

            for (List<FragmentedText.Fragment> fragments : lines) {
                for (FragmentedText.Fragment fragment : fragments) {
                    setFont(fragment.font());
                    drawText(
                            fragment.text().toString(),
                            fragment.x() + sxY * fragment.y() + sxH * fragment.h(),
                            fragment.y(),
                            HAnchor.LEFT,
                            VAnchor.TOP
                    );
                }
            }
        } finally {
            setTransformation(t);
            setFont(font);
        }
    }


    @Override
    void close();

    /**
     * Approximates an elliptical arc using Bézier curves and processes each segment
     * using the provided consumer function.
     *
     * @param arc the {@code Arc2f} object representing the elliptical arc to approximate,
     *            containing the arc's start point, end point, radii, rotation angle,
     *            and flags indicating large arc or sweep direction.
     * @param moveTo                a {@code Consumer<Vector2f>} that processes the starting point of the arc.
     * @param generateBezierSegment a {@code Consumer<Vector2f[]>} that processes
     *                              the generated Bézier segment points for each
     *                              subdivided portion of the arc.
     */
    static void approximateArc(Arc2f arc, Consumer<Vector2f> moveTo, Consumer<Vector2f[]> generateBezierSegment) {
        approximateArc(
                arc.start(),
                arc.end(),
                Vector2f.of(arc.rx(), arc.ry()),
                arc.angle(),
                arc.largeArc(),
                arc.sweep(),
                moveTo,
                generateBezierSegment
        );
    }

    /**
     * Approximates an elliptical arc using Bézier curves. This method converts the arc parameters
     * into one or more quadratic Bézier curves that closely match the arc.
     *
     * @param p0 the starting point of the arc
     * @param p1 the ending point of the arc
     * @param r the radii of the ellipse (x-axis and y-axis radii)
     * @param angle the rotation of the ellipse in radians
     * @param largeArc a boolean indicating whether to draw the larger arc segment
     * @param sweep a boolean indicating the direction of the arc sweep (clockwise or counter-clockwise)
     * @param moveTo a consumer to handle the initial move-to operation for the starting point
     * @param generateBezierSegment a consumer to process the generated Bézier segments for the arc
     * @throws IllegalArgumentException if the parameters do not define a valid arc (e.g., the distance between points
     *         is too great for the provided radii or arc degenerates to a line)
     */
    static void approximateArc(
            Vector2f p0,
            Vector2f p1,
            Vector2f r,
            float angle,
            boolean largeArc,
            boolean sweep,
            Consumer<Vector2f> moveTo,
            Consumer<Vector2f[]> generateBezierSegment) {
        // 1. if either rx = 0 or ry = 0 the arc degenerates to a line
        if (r.x() == 0.0f || r.y() == 0.0f || p0.equals(p1)) {
            if (2 * Math.max(r.x(), r.y()) <= p1.subtract(p0).length()) {
                throw new IllegalArgumentException("no solution: points are equal or rx or ry is zero and the distance between points is too great");
            }
            // solution is a straight line connecting the points
            Vector2f center = Vector2f.of((p0.x() + p1.x()) / 2, (p0.y() + p1.y()) / 2);
            generateBezierSegment.accept(new Vector2f[]{center, center, p1});
            return;
        }

        // 2. Rotate to the standard coordinate System to align the ellipsis' axes with the x- and y-axes of the coordinate system
        AffineTransformation2f at = AffineTransformation2f.combine(
                AffineTransformation2f.rotate(-angle),
                AffineTransformation2f.scale(1 / r.x(), 1 / r.y())
        );
        AffineTransformation2f atI = AffineTransformation2f.combine(
                AffineTransformation2f.scale(r.x(), r.y()),
                AffineTransformation2f.rotate(angle)
        );

        Vector2f p0l = at.transform(p0);
        Vector2f p1l = at.transform(p1);

        // 3. find circle's center
        double sign = sweep != largeArc ? -1.0f : 1.0f;

        double mx = (p1l.x() - p0l.x()) / 2;
        double my = (p1l.y() - p0l.y()) / 2;
        double sx = p0l.x() + mx;
        double sy = p0l.y() + my;
        double tx = my;
        double ty = -mx;
        double slen2 = Math.min(1.0, mx * mx + my * my); // catch rounding errors (slen2 > 1 => NaN)
        double factor = sign * Math.sqrt((1 - slen2) / slen2);
        double cx = sx + factor * tx;
        double cy = sy + factor * ty;

        Vector2f cl = Vector2f.of((float) cx, (float) cy);

        // 4. generate Bézier curves
        double startAngle = Math.atan2(p0l.y() - cy, p0l.x() - cx);
        double endAngle = Math.atan2(p1l.y() - cl.y(), p1l.x() - cl.x());
        double sweepAngle;
        float sweepSign = sweep ? 1 : -1;
        if (sweepSign * endAngle < sweepSign * startAngle) {
            endAngle += sweepSign * 2 * Math.PI;
        }
        sweepAngle = endAngle - startAngle;
        if (sweepSign * sweepAngle <= Math.PI && largeArc) {
            sweepAngle = 2 * Math.PI - sweepAngle;
        }

        int segments = (int) Math.ceil(Math.abs(sweepAngle) / (Math.PI / 4));
        AffineTransformation2f atB = AffineTransformation2f.combine(
                AffineTransformation2f.translate(cl.x(), cl.y()),
                atI
        );
        double cosCurrent = Math.cos(startAngle);
        double sinCurrent = Math.sin(startAngle);
        double stepAngle = sweepAngle / segments;
        double f = (4.0 / 3.0) * Math.tan(stepAngle / 4.0);
        moveTo.accept(atB.transform(Vector2f.of((float) (cosCurrent), (float) (sinCurrent))));
        for (int i = 0; i < segments; i++) {
            double nextAngle = startAngle + (i + 1) * sweepAngle / segments;
            double cosNext = Math.cos(nextAngle);
            double sinNext = Math.sin(nextAngle);

            double x0T = -f * sinCurrent;
            double y0T = f * cosCurrent;
            double x1T = f * sinNext;
            double y1T = -f * cosNext;

            generateBezierSegment.accept(new Vector2f[]{
                    atB.transform(Vector2f.of((float) (cosCurrent + x0T), (float) (sinCurrent + y0T))),
                    atB.transform(Vector2f.of((float) (cosNext + x1T), (float) (sinNext + y1T))),
                    atB.transform(Vector2f.of((float) (cosNext), (float) (sinNext))),
            });

            sinCurrent = sinNext;
            cosCurrent = cosNext;
        }
    }
}