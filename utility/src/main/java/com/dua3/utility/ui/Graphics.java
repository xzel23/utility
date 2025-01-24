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
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.VerticalAlignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A generic interface defining drawing commands.
 * <p>
 * The Graphics interface provides an abstraction from the underlying rendering toolkit.
 */
public interface Graphics extends AutoCloseable {

    /**
     * Retrieves the width of an object. The width is typically measured in the object's respective unit.
     *
     * @return the width as a float value
     */
    float getWidth();

    /**
     * Retrieves the height value.
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
     * Get bounds.
     *
     * @return the bounding rectangle
     */
    Rectangle2f getBounds();

    /**
     * Get text dimensions using the current font.
     *
     * @param text the text
     * @return the text dimensions
     */
    Rectangle2f getTextDimension(CharSequence text);

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
     * Calculates the bounding rectangle of the graphics in the local coordinate space.
     *
     * <p>The method first retrieves the inverse of the current transformation using {@link #getTransformation()} and
     * throws an exception if the transformation is not present. Then, it calculates the bounds of the graphics using
     * the {@link #getBounds()} method. Finally, it transforms the minimum and maximum coordinates of the bounds using
     * the inverse transformation to get the bounds in the local coordinate space.</p>
     *
     * @return the bounding rectangle in the local coordinate space
     * @throws NoSuchElementException if the inverse of the current transformation is not present
     */
    default Rectangle2f getBoundsInLocal() {
        AffineTransformation2f ti = getTransformation().inverse().orElseThrow();
        Rectangle2f bounds = getBounds();
        return Rectangle2f.withCorners(
                ti.transform(bounds.min()),
                ti.transform(bounds.max())
        );
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
        Rectangle2f r = getTextDimension(text);

        float tx, ty;

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

    enum TextRotationMode {
        /**
         * Rotate the block of text as a whole, i.e., the text is rendered the same as with no rotation applied and
         * then the whole block is rotated with the lower left corner as pivot. This means that when rotating
         * counter-clockwise, the upper left corner will be left of the pivot's x-coordinate.
         */
        ROTATE_BLOCK,
        /**
         * Rotate the block of text as a whole, like with {@code ROTATE_BLOCK}, but translate the whole block of text
         * so that the left most x-coordinate of the rotated rectangle containing the text will align with the given
         * x-coordinate. Also apply a vertical translation accordingly.
         */
        ROTATE_AND_TRANSLATE_BLOCK,
        /**
         * Rotate each line independently. Align lines horizontally, i.e., all lines start at the same y-coordinate.
         */
        ROTATE_LINES,
    }

    enum AlignmentAxis {
        AUTOMATIC,
        X_AXIS,
        Y_AXIS,
    }

    /**
     * Renders the given text within the specified bounding rectangle using the provided font,
     * alignment, and wrapping settings.
     *
     * @param pos               the rendering position
     * @param text              the text to be rendered
     * @param hAlign            the horizontal alignment of the text within the bounding rectangle
     * @param vAlign            the vertical alignment of the text within the bounding rectangle
     * @param outputDimension   the dimension of the output area
     */
    default void renderText(
            Vector2f pos,
            RichText text,
            HAnchor hAnchor,
            VAnchor vAnchor,
            Alignment hAlign,
            VerticalAlignment vAlign,
            Dimension2f outputDimension
    ) {
        float width = outputDimension.width();
        float height = outputDimension.height();

        FragmentedText fragments = generateFragments(text, width, height, hAlign, vAlign, hAnchor, vAnchor);

        renderFragments(
                pos,
                fragments,
                pos,
                0.0,
                AlignmentAxis.AUTOMATIC
        );
    }

    /**
     * Renders the given text within the specified bounding rectangle using the provided font,
     * alignment, wrapping, and rotation settings.
     *
     * @param pos               the position
     * @param text              the text to be rendered
     * @param hAnchor           the horizontal anchor setting
     * @param vAnchor           the vertical anchor setting
     * @param hAlign            the horizontal alignment of the text within the bounding rectangle
     * @param vAlign            the vertical alignment of the text within the bounding rectangle
     * @param outputDimension   the dimension of the output area
     * @param angle             the rotation angle in radians
     * @param mode              the {@link TextRotationMode} to use
     * @param alignmentAxis     the axis to align rotated text on
     * @param pivot             determines which of the rectangle corners to use as the center of rotation
     */
    default void renderText(
            Vector2f pos,
            RichText text,
            HAnchor hAnchor,
            VAnchor vAnchor,
            Alignment hAlign,
            VerticalAlignment vAlign,
            Dimension2f outputDimension,
            Vector2f pivot,
            double angle,
            TextRotationMode mode,
            AlignmentAxis alignmentAxis) {
        // normalize angle to the range [0, 2pi)
        angle = MathUtil.normalizeRadians(angle);

        float width = outputDimension.width();
        float height = outputDimension.height();

        // layout text
        FragmentedText fragments = generateFragments(text, width, height, hAlign, vAlign, hAnchor, vAnchor);

        switch (mode) {
            case ROTATE_BLOCK -> {
                AffineTransformation2f t = getTransformation();
                setTransformation(AffineTransformation2f.combine(t, AffineTransformation2f.rotate(angle, pivot)));
                renderFragments(
                        pos,
                        fragments,
                        Vector2f.ORIGIN,
                        0.0,
                        AlignmentAxis.AUTOMATIC
                );
                setTransformation(t);
            }
            case ROTATE_AND_TRANSLATE_BLOCK -> {
                float tx;
                float ty;
                int quadrant = MathUtil.quadrantIndexRadians(angle);

                switch (quadrant) {
                    case 0 -> {
                        tx = (float) (Math.sin(angle) * fragments.textHeight());
                        ty = 0;
                    }
                    case 1 -> {
                        tx = (float) (Math.sin(angle) * fragments.textHeight()
                                - Math.cos(angle) * fragments.textWidth()
                        );
                        ty = (float) (-Math.cos(angle) * fragments.textHeight());
                    }
                    case 2 -> {
                        tx = (float) (-Math.cos(angle) * fragments.textWidth());
                        ty = (float) (-Math.sin(angle) * fragments.textWidth()
                                - Math.cos(angle) * fragments.textHeight()
                        );
                    }
                    case 3 -> {
                        tx = 0;
                        ty = (float) (-Math.sin(angle) * fragments.textWidth());
                    }
                    default -> {
                        throw new IllegalStateException("invalid quadrant index: " + quadrant);
                    }
                }

                AffineTransformation2f t = getTransformation();
                setTransformation(AffineTransformation2f.combine(
                        t,
                        AffineTransformation2f.rotate(angle, pivot),
                        AffineTransformation2f.translate(tx, ty)
                ));
                renderFragments(
                        pos,
                        fragments,
                        Vector2f.ORIGIN,
                        0.0,
                        AlignmentAxis.AUTOMATIC
                );
                setTransformation(t);
            }
            case ROTATE_LINES -> {
                renderFragments(
                        pos,
                        fragments,
                        pivot,
                        angle,
                        alignmentAxis
                );
            }
        }
    }

    private static Vector2f getAnchor(FragmentedText text, HAnchor hAnchor, VAnchor vAnchor) {
        return Vector2f.of(
                switch (hAnchor) {
                    case LEFT -> 0;
                    case RIGHT -> text.textWidth();
                    case CENTER -> text.textWidth() / 2;
                },
                switch (vAnchor) {
                    case TOP -> 0;
                    case BOTTOM -> -text.textHeight();
                    case MIDDLE -> -text.textHeight() / 2;
                    case BASELINE -> -text.textHeight() + text.baseLine();
                }
        );
    }

    /**
     * A text fragment that can't be split (i.e., contains no whitespace) and has a uniform font so that it can be
     * drawn in a single operation.
     *
     * @param x the x-position
     * @param y the y-position
     * @param w the width
     * @param h the height
     * @param baseLine the baseline value (of the line the fragment belongs to
     * @param font the font
     * @param text the text
     */
    record Fragment(float x, float y, float w, float h, float baseLine, Font font, CharSequence text) {
        Fragment translate(float dx, float dy) {
            return new Fragment(x + dx, y + dy, w, h, baseLine, font, text);
        }
    }

    /**
     * Represents a fragmented text that can be rendered within a specified bounding rectangle.
     * <p>
     * The FragmentedText class holds a list of lines, where each line is represented by a list of {@link Fragment}
     * objects. Fragments are text segments that cannot be split and have uniform font attributes. The text is split
     * based on whitespace characters and text decorations (font, text decoration). The class also stores the dimensions
     * and position of the text within the bounding rectangle.
     *
     * @param lines a list of lines, where each line is represented by a list of Fragment objects
     * @param textWidth the width of the rendered text
     * @param textHeight the height of the rendered text
     * @param baseLine the baseline value of the line the fragment belongs to
     */
    record FragmentedText(
            List<List<Fragment>> lines,
            float textWidth,
            float textHeight,
            float baseLine,
            float actualWidth,
            float actualHeight) {
        /**
         * Retrieves the layout dimensions of the text as a {@code Dimension2f} object.
         *
         * <p>>The dimensions are the ones received as input for the layout operation.
         *
         * @return a {@code Dimension2f} object representing the width and height of the text
         */
        public Dimension2f getLayoutDimension() {
            return Dimension2f.of(textWidth, textHeight);
        }

        /**
         * Retrieves the actual dimensions of the text as a {@code Dimension2f} object.
         *
         * <p>The dimensions are the calculated values after laying out the text.
         *
         * @return a {@code Dimension2f} object representing the width and height of the text
         */
        public Dimension2f getActualDimension() {
            return Dimension2f.of(actualWidth, actualHeight);
        }
    }

    /**
     * Split text into fragments and layout according to alignment settings.
     *
     * <p>Split the text into fragments that are either whitespace or free of whitespace and have uniform
     * text attributes (font, text decoration). For each line, a list of such fragments is generated and added
     * to the list of fragment lines (see {@link FragmentedText#lines()}).
     *
     * <p><b>The layout algorithm</b>
     * <ul>
     * <li>For each line of text, automatic wrapping is applied when the line width exceeds {@code width}.
     * <li>The fragments of each line are horizontally distributed according to the {@code hAlign} parameter.
     * <li>The lines of fragments are then distributed vertically according to {@code vAlign} and {@code height}. If the
     * given height is smaller than the required height, the text overflows.
     * <li>The anchor is applied so that when the text is rendered, it is displayed according to the anchor setting.
     * </ul>
     *
     * @param text              the text
     * @param width             the width at which to apply wrapping; pass NO_WRAP to disable wrapping
     * @param height            the height of the output area; used for vertical alignment
     * @param hAlign            the horizontal alignment setting
     * @param vAlign            the vertical alignment setting
     * @param hAnchor           the horizontal anchor setting
     * @param vAnchor           the vertical anchor setting
     * @return the fragmented text as a {@link FragmentedText} instance
     */
    private FragmentedText generateFragments(
            RichText text,
            float width,
            float height,
            Alignment hAlign,
            VerticalAlignment vAlign,
            HAnchor hAnchor,
            VAnchor vAnchor
    ) {
        boolean wrap = width != Float.MAX_VALUE;

        Function<RichText, RichText> trimLine = switch (hAlign) {
            case LEFT -> RichText::stripTrailing;
            case RIGHT -> RichText::stripLeading;
            case CENTER, JUSTIFY -> RichText::strip;
        };

        // generate lists of chunks for each line
        FontUtil<?> fontUtil = getFontUtil();
        List<List<Fragment>> fragmentLines = new ArrayList<>();
        float textWidth = 0.0f;
        float textHeight = 0.0f;
        float baseLine = 0.0f;
        for (RichText line : text.split("\n")) {
            line = trimLine.apply(line);

            List<Fragment> fragments = new ArrayList<>();
            fragmentLines.add(fragments);

            float xAct = 0.0f;
            float lineHeight = 0.0f;
            float lineWidth = 0.0f;
            float lineBaseLine = 0.0f;
            float whitespace = 0.0f;
            boolean wrapAllowed = false;
            List<Run> parts = splitLinePreservingWhitespace(line, wrap);
            for (int i = 0; i < parts.size(); i++) {
                var run = parts.get(i);

                // when using JUSTIFY alignment, the last line
                boolean isLastLine = i == parts.size() - 1;
                Alignment effectiveHAlign = hAlign == Alignment.JUSTIFY && isLastLine ? Alignment.LEFT : hAlign;

                Font f = fontUtil.deriveFont(getFont(), run.getFontDef());
                Rectangle2f tr = fontUtil.getTextDimension(run, f);
                if (wrapAllowed && xAct + tr.width() > width) {
                    if (!fragments.isEmpty() && TextUtil.isBlank(fragments.get(fragments.size() - 1).text())) {
                        // remove trailing whitespace
                        Fragment removed = fragments.remove(fragments.size() - 1);
                        whitespace -= removed.w();
                        assert whitespace >= 0.0f : "whitespace must be non-negative after removing trailing whitespace";
                    } else if (TextUtil.isBlank(run)) {
                        // skip leading whitespace after wrapped line
                        continue;
                    }
                    lineWidth = applyHAlign(fragments, hAlign, width, lineWidth, whitespace);
                    textWidth = Math.max(textWidth, lineWidth);

                    // start new line
                    fragments = new ArrayList<>();
                    fragmentLines.add(fragments);
                    xAct = 0.0f;
                    textHeight += lineHeight;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth = tr.width();
                    whitespace = TextUtil.isBlank(run) ? tr.width() : 0.0f;
                    lineHeight = tr.height();
                    wrapAllowed = false;
                    lineBaseLine = tr.height() + tr.yMin();
                } else {
                    wrapAllowed = wrap;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth += tr.width();
                    whitespace += TextUtil.isBlank(run) ? tr.width() : 0.0f;
                    lineHeight = Math.max(lineHeight, tr.height());
                    lineBaseLine = Math.max(lineBaseLine, tr.height() + tr.yMin());
                }
            }
            lineWidth = applyHAlign(fragments, hAlign, width, lineWidth, whitespace);
            textWidth = Math.max(textWidth, lineWidth);
            textHeight += lineHeight;
            baseLine = lineBaseLine;
        }

        // fix the x-position for justified layout (the last line should be left aligned)
        if (hAlign == Alignment.JUSTIFY && !fragmentLines.isEmpty()) {
            List<Fragment> lastLine = fragmentLines.get(fragmentLines.size() - 1);
            if (!lastLine.isEmpty()) {
                float dx = -lastLine.get(0).x();
                lastLine.replaceAll(fragment -> fragment.translate(dx, 0));
            }
        }

        // apply anchor and vertical alignment
        float tx = switch(hAnchor) {
            case LEFT -> 0.0f;
            case RIGHT -> -width;
            case CENTER -> -width/2.0f;
        };
        float ty = switch (vAnchor) {
            case TOP -> 0.0f;
            case MIDDLE -> -textHeight / 2.0f;
            case BOTTOM -> -textHeight;
            case BASELINE -> -textHeight + baseLine;
        };

        float verticalSpace = Math.max(0, height - textHeight);
        switch (vAlign) {
            case TOP -> translateFragments(fragmentLines, tx, ty);
            case MIDDLE -> translateFragments(fragmentLines, tx, ty - verticalSpace / 2.0f);
            case BOTTOM -> translateFragments(fragmentLines, tx, ty - verticalSpace);
            case DISTRIBUTED -> {
                int n = fragmentLines.size();
                float k = n > 1 ? verticalSpace / (n - 1) : 0.0f;
                for (int i = 0; i < n; i++) {
                    float dy = i * k;
                    fragmentLines.get(i).replaceAll(fragment -> fragment.translate(tx, ty + dy));
                }
                textHeight += (n - 1) * k;
            }
        }

        return new FragmentedText(
                fragmentLines,
                textWidth,
                textHeight,
                baseLine,
                Math.max(textWidth, width),
                Math.max(textHeight, height)
        );
    }

    /**
     * Translates all the fragments within the provided list of fragment lines by the specified
     * horizontal and vertical offsets. The method skips translation if both offsets are zero.
     *
     * @param fragmentLines a list of fragment lines where each line is a list of fragments to be translated
     * @param dx the horizontal offset by which the fragments should be translated
     * @param dy the vertical offset by which the fragments should be translated
     */
    private static void translateFragments(List<List<Fragment>> fragmentLines, float dx, float dy) {
        if (dx == 0.0f && dy == 0.0f) {
            return;
        }

        for (List<Fragment> line: fragmentLines) {
            line.replaceAll(fragment -> fragment.translate(dx, dy));
        }
    }

    /**
     * Applies horizontal alignment to a list of text fragments according to the specified alignment type.
     * Adjusts the position and size of the fragments within the given line width and whitespace parameters.
     *
     * <p><strong>NOTE: </strong>The method assumes lines start at x-coordinate 0.
     *
     * @param line       the list of fragments representing the line of text to be aligned
     * @param hAlign     the horizontal alignment type (e.g., LEFT, RIGHT, CENTER, JUSTIFY)
     * @param width      the total width available for the line
     * @param lineWidth  the current width of the line before alignment
     * @param whitespace the amount of whitespace available for distribution in the line
     * @return the new width of the line after alignment adjustments
     */
    private static float applyHAlign(List<Fragment> line, Alignment hAlign, float width, float lineWidth, float whitespace) {
        float availableSpace = Math.max(0.0f, width - lineWidth);
        float f = whitespace > 0 ? 1.0f + availableSpace / whitespace : 1.0f;
        switch (hAlign) {
            case LEFT -> {
                // nothing to do
            }
            case RIGHT -> {
                line.replaceAll(fragment -> fragment.translate(availableSpace, 0.0f));
            }
            case CENTER -> {
                line.replaceAll(fragment -> fragment.translate(availableSpace / 2.0f, 0.0f));
            }
            case JUSTIFY -> {
                // distribute the remaining space by evenly expanding existing whitespace
                float x = 0.0f;
                for (int i = 0; i < line.size(); i++) {
                    Fragment original = line.get(i);
                    float w = original.w() * (TextUtil.isBlank(original.text()) ? f : 1.0f);
                    Fragment aligned = new Fragment(
                            x,
                            original.y(),
                            w,
                            original.h(),
                            original.baseLine(),
                            original.font(),
                            original.text()
                    );
                    line.set(i, aligned);
                    x += w;
                }
            }
        }

        if (line.isEmpty()) {
            return 0.0f;
        } else {
            Fragment first = line.get(0);
            Fragment last = line.get(line.size()-1);
            return last.x() + last.w();
        }
    }

    /**
     * Renders text fragments within the specified bounding rectangle. The text fragments are provided as a list of fragment lines.
     * Each fragment line contains a list of fragments that are either whitespace or text with uniform attributes (font, text decoration).
     * The method calculates the positioning of each fragment based on the alignment and distributes whitespace and remaining space accordingly.
     * The rendered text is drawn on the graphics context.
     *
     * @param pos           the rendering position
     * @param text          a list of fragment lines, where each line contains a list of fragments
     * @param pivot         determines which of the rectangle corners to use as the center of rotation
     * @param angle         the angle in radians to rotate each line (must be normalized)
     * @param alignmentAxis the axis on which to align the text on
     */
    private void renderFragments(
            Vector2f pos,
            FragmentedText text,
            Vector2f pivot,
            double angle,
            AlignmentAxis alignmentAxis
    ) {
        assert 0 <= angle && angle < MathUtil.TWO_PI : "invalid angle: " + angle;

        // get font and transformation
        Font font = getFont();
        AffineTransformation2f t = getTransformation();

        float sx_y;
        float sx_h;

        if (angle == 0.0) {
            setTransformation(AffineTransformation2f.combine(AffineTransformation2f.translate(pos), t));
            sx_y = 0.0f;
            sx_h = 0.0f;
        } else {
            setTransformation(AffineTransformation2f.combine(t, AffineTransformation2f.translate(pos), AffineTransformation2f.rotate(angle, pivot)));
            int[] layoutCases = switch (alignmentAxis) {
                case AUTOMATIC -> new int[]{0, 1, 2, 3};
                case X_AXIS -> new int[]{1, 1, 2, 2};
                case Y_AXIS -> new int[]{0, 0, 3, 3};
            };
            int layoutCase = layoutCases[(int) (angle / MathUtil.PI_QUARTER) % 4];
            switch (layoutCase) {
                case 0 -> {
                    sx_y = (float) (Math.tan(angle));
                    sx_h = sx_y;
                }
                case 1 -> {
                    sx_y = (float) (Math.tan(angle + MathUtil.PI_HALF));
                    sx_h = 0;
                }
                case 2 -> {
                    sx_y = (float) (Math.tan(angle + MathUtil.PI_HALF));
                    sx_h = sx_y;
                }
                case 3 -> {
                    sx_y = (float) (Math.tan(angle));
                    sx_h = 0;
                }
                default -> {
                    throw new IllegalStateException("invalid octant");
                }
            }
        }

        List<List<Fragment>> lines = text.lines;

        for (int i = 0; i < lines.size(); i++) {
            List<Fragment> fragments = lines.get(i);

            for (Fragment fragment : fragments) {
                setFont(fragment.font);
                drawText(
                        fragment.text.toString(),
                        fragment.x() + sx_y * fragment.y() + sx_h * fragment.h(),
                        fragment.y(),
                        HAnchor.LEFT,
                        VAnchor.TOP
                );
            }
        }

        setTransformation(t);
        setFont(font);
    }

    /**
     * Splits a RichText line into fragments, preserving whitespace if wrapping is enabled.
     * Each fragment is either whitespace or text with uniform attributes (font, text decoration).
     * For each line, a list of such fragments is generated and returned.
     *
     * @param line      the RichText line to be split
     * @param wrapping  a boolean value indicating whether wrapping should be applied
     * @return a List of Run fragments representing the split line
     */
    private static List<Run> splitLinePreservingWhitespace(RichText line, boolean wrapping) {
        if (!wrapping) {
            return line.runs();
        }
// fixme use pattern
        return Arrays.stream(line.split("(?<=\\s)|(?=\\s)"))
                .flatMap(part -> part.runs().stream())
                .toList();
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
     * Approximates an elliptical arc with cubic Bézier curves and generates Bézier segments using the provided consumer.
     *
     * @param p0 The starting point of the arc.
     * @param p1 The ending point of the arc.
     * @param r The radii of the ellipse, with `r.x` representing the x-radius and `r.y` representing the y-radius.
     * @param angle The rotation angle of the ellipse’s axes in radians.
     * @param largeArc A flag indicating whether the larger arc spanning more than 180 degrees should be chosen.
     * @param sweep A flag indicating whether the arc should be drawn in a clockwise direction.
     * @param generateBezierSegment A consumer to process each Bézier segment, with each segment represented by an array of control points.
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
        AffineTransformation2f M = AffineTransformation2f.combine(
                AffineTransformation2f.rotate(-angle),
                AffineTransformation2f.scale(1 / r.x(), 1 / r.y())
        );
        AffineTransformation2f MI = AffineTransformation2f.combine(
                AffineTransformation2f.scale(r.x(), r.y()),
                AffineTransformation2f.rotate(angle)
        );

        Vector2f p0l = M.transform(p0);
        Vector2f p1l = M.transform(p1);

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
        AffineTransformation2f MB = AffineTransformation2f.combine(
                AffineTransformation2f.translate(cl.x(), cl.y()),
                MI
        );
        double cosCurrent = Math.cos(startAngle);
        double sinCurrent = Math.sin(startAngle);
        double stepAngle = sweepAngle / segments;
        double f = (4.0 / 3.0) * Math.tan(stepAngle / 4.0);
        moveTo.accept(MB.transform(Vector2f.of((float) (cosCurrent), (float) (sinCurrent))));
        for (int i = 0; i < segments; i++) {
            double nextAngle = startAngle + (i + 1) * sweepAngle / segments;
            double cosNext = Math.cos(nextAngle);
            double sinNext = Math.sin(nextAngle);

            double x0T = -f * sinCurrent;
            double y0T = f * cosCurrent;
            double x1T = f * sinNext;
            double y1T = -f * cosNext;

            generateBezierSegment.accept(new Vector2f[]{
                    MB.transform(Vector2f.of((float) (cosCurrent + x0T), (float) (sinCurrent + y0T))),
                    MB.transform(Vector2f.of((float) (cosNext + x1T), (float) (sinNext + y1T))),
                    MB.transform(Vector2f.of((float) (cosNext), (float) (sinNext))),
            });

            sinCurrent = sinNext;
            cosCurrent = cosNext;
        }
    }
}