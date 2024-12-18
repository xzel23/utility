package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Arc2f;
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
     * Stroke rectangle.
     * @param r the recatngle
     */
    default void strokeRect(Rectangle2f r) {
        strokeRect(r.xMin(), r.yMin(), r.width(), r.height());
    }

    /**
     * Draws the specified image at the given coordinates.
     *
     * @param image The image to be drawn.
     * @param x The x-coordinate of the top-left corner where the image will be drawn.
     * @param y The y-coordinate of the top-left corner where the image will be drawn.
     */
    void drawImage(Image image, float x, float y);

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
     * Calculates the bounding rectangle of the graphics in the local coordinate space.
     *
     * <p>The method first retrieves the inverse of the current transformation using {@link #getTransformation()} and
     * throws an exception if the transformation is not present. Then, it calculates the bounds of the graphics using
     * the {@link #getBounds()} method. Finally, it transforms the minimum and maximum coordinates of the bounds using
     * the inverse transformation to obtain the bounds in the local coordinate space.</p>
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

    /**
     * Renders the given text within the specified bounding rectangle using the provided font,
     * alignment, and wrapping settings.
     *
     * @param r        the bounding rectangle to render the text into
     * @param text     the text to be rendered
     * @param hAlign   the horizontal alignment of the text within the bounding rectangle
     * @param vAlign   the vertical alignment of the text within the bounding rectangle
     * @param wrapping determines if text wrapping should be applied
     */
    default void renderText(Rectangle2f r, RichText text, Alignment hAlign, VerticalAlignment vAlign, boolean wrapping) {
        FragmentedText fragments = generateFragments(text, r, hAlign, vAlign, wrapping);
        renderFragments(r, hAlign, vAlign, fragments.textWidth(), fragments.textHeight(), fragments.baseLine(), fragments.fragmentLines());
    }

    /**
     * A text fragment that can't be split (i.e., contains no whitespace) and has a uniform font so that it can be
     * drawn in a single operation.
     *
     * @param x the x-position
     * @param y the y-position
     * @param w the width
     * @param h the height
     * @param baseLine the basline value (of the line the fragment belongs to
     * @param font the font
     * @param text the text
     */
    record Fragment (float x, float y, float w, float h, float baseLine, Font font, CharSequence text) {}

    /**
     * Represents a fragmented text that can be rendered within a specified bounding rectangle.
     * <p>
     * The FragmentedText class holds a list of lines, where each line is represented by a list of {@link Fragment}
     * objects. Fragments are text segments that cannot be split and have uniform font attributes. The text is split
     * based on whitespace characters and text decorations (font, text decoration). The class also stores the dimensions
     * and position of the text within the bounding rectangle.
     *
     * @param fragmentLines a list of lines, where each line is represented by a list of Fragment objects
     * @param textWidth the width of the rendered text
     * @param textHeight the height of the rendered text
     * @param baseLine the baseline value of the line the fragment belongs to
     */
    record FragmentedText(List<List<Fragment>> fragmentLines, float textWidth, float textHeight, float baseLine) {}

    /**
     * Split text into fragments.
     *
     * <p>Split the text into fragments that are either whitespace or free of whitespace and have uniform
     * text attributes (font, text decoration). For each line, a list of such fragments is generated and added
     * to the list of fragment lines (see {@link FragmentedText#fragmentLines()}).
     *
     * @param text   the text
     * @param r      the bounding rectangle to render the text into
     * @param hAlign the horizontal alignment
     * @param vAlign thee vertical alignment
     * @param wrap   if wrapping should be applied (
     * @return the fragmented text as a {@link FragmentedText} instance
     */
    private FragmentedText generateFragments(RichText text, Rectangle2f r, Alignment hAlign, VerticalAlignment vAlign, boolean wrap) {
        float wrapWidth = wrap ? r.width() : Float.MAX_VALUE;

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
        for (RichText line: text.split("\n")) {
            line = trimLine.apply(line);

            List<Fragment> fragments = new ArrayList<>();
            fragmentLines.add(fragments);

            float xAct = 0.0f;
            float lineHeight = 0.0f;
            float lineWidth = 0.0f;
            float lineBaseLine = 0.0f;
            boolean wrapAllowed = false;
            for (var run: splitLinePreservingWhitespace(line, wrap)) {
                Font f = fontUtil.deriveFont(getFont(), run.getFontDef());
                Rectangle2f tr = fontUtil.getTextDimension(run, f);
                if (wrapAllowed && xAct + tr.width() > wrapWidth) {
                    if (!fragments.isEmpty() && TextUtil.isBlank(fragments.get(fragments.size() - 1).text())) {
                        // remove trailing whitespace
                        fragments.remove(fragments.size() - 1);
                    } else if (TextUtil.isBlank(run)) {
                        // skip leading whitespace after wrapped line
                        continue;
                    }
                    fragments = new ArrayList<>();
                    fragmentLines.add(fragments);
                    xAct = 0.0f;
                    textHeight += lineHeight;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth = tr.width();
                    lineHeight = tr.height();
                    wrapAllowed = false;
                    lineBaseLine = -tr.yMin();
                } else {
                    wrapAllowed = wrap;
                    fragments.add(new Fragment(xAct, textHeight, tr.width(), tr.height(), lineBaseLine, f, run));
                    xAct += tr.width();
                    lineWidth += tr.width();
                    lineHeight = Math.max(lineHeight, tr.height());
                    lineBaseLine = Math.max(lineBaseLine, -tr.yMin());
                }
            }
            textWidth = Math.max(textWidth, lineWidth);
            textHeight += lineHeight;
            baseLine = lineBaseLine;
        }
        return new FragmentedText(fragmentLines, textWidth, textHeight, baseLine);
    }

    /**
     * Renders text fragments within the specified bounding rectangle. The text fragments are provided as a list of fragment lines.
     * Each fragment line contains a list of fragments that are either whitespace or text with uniform attributes (font, text decoration).
     * The method calculates the positioning of each fragment based on the alignment and distributes whitespace and remaining space accordingly.
     * The rendered text is drawn on the graphics context.
     *
     * @param cr             the bounding rectangle within which the text fragments will be rendered
     * @param hAlign         the horizontal alignment of the text within the bounding rectangle
     * @param vAlign         the vertical alignment of the text within the bounding rectangle
     * @param textWidth      the total width of the text fragments within a line
     * @param textHeight     the total height of the text fragments within all lines
     * @param baseLine       the baseline position of the text fragments
     * @param fragmentLines  a list of fragment lines, where each line contains a list of fragments
     */
    private void renderFragments(Rectangle2f cr, Alignment hAlign, VerticalAlignment vAlign, float textWidth, float textHeight, float baseLine, List<List<Fragment>> fragmentLines) {
        float y = switch (vAlign) {
            case TOP, DISTRIBUTED -> cr.yMin();
            case MIDDLE -> cr.yCenter() - textHeight /2;
            case BOTTOM -> cr.yMax() - textHeight;
        };
        float fillerHeight = vAlign == VerticalAlignment.DISTRIBUTED ?  (cr.height()- textHeight)/Math.max(1, fragmentLines.size()-1) : 0.0f;

        record LineStatistics(float text, float whiteSpace, int nSpace) {}
        for (int i = 0; i < fragmentLines.size(); i++) {
            List<Fragment> fragments = fragmentLines.get(i);

            // determine the number and size of whitespace and text fragments
            LineStatistics fi = fragments.stream().map(fragment -> {
                        boolean isWS = TextUtil.isBlank(fragment.text());
                        return new LineStatistics(isWS ? 0.0f : fragment.w(), isWS ? fragment.w() : 0.0f, isWS ? 1 : 0);
                    })
                    .reduce((a, b) -> new LineStatistics(a.text + b.text, a.whiteSpace + b.whiteSpace, a.nSpace + b.nSpace))
                    .orElseGet(() -> new LineStatistics(0.0f, 0.0f, 1));

            float spaceToDistribute = cr.width() - fi.text - fi.whiteSpace;
            float totalSpace = fi.whiteSpace + spaceToDistribute;

            // when justify aligning text, use left alignment for the last line
            boolean isLastLine = i == fragmentLines.size() - 1;
            Alignment effectiveHAlign = (hAlign == Alignment.JUSTIFY && isLastLine) ? Alignment.LEFT : hAlign;

            float x = cr.xMin();
            for (Fragment fragment : fragments) {
                switch (effectiveHAlign) {
                    case JUSTIFY -> {
                        // distribute the remaining space by evenly expanding existind whitespace
                        if (TextUtil.isBlank(fragment.text())) {
                            x += fragment.w() * (totalSpace / fi.whiteSpace() - 1);
                        }
                    }
                    case RIGHT -> {
                        if (fragment.x() == 0.0f) {
                            // push everything to the right
                            x += spaceToDistribute;
                        }
                    }
                    case CENTER -> {
                        if (fragment.x() == 0.0f) {
                            // push everything halfway right
                            x += spaceToDistribute / 2.0f;
                        }
                    }
                    case LEFT -> { /* nothing to do */}
                }
                setFont(fragment.font);
                drawText(fragment.text.toString(), x + fragment.x, y + fragment.y + baseLine);
            }
            y += fillerHeight;
        }
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
    static void approximateArc(Arc2f arc, Consumer<Vector2f[]> generateBezierSegment) {
        Vector2f p0 = arc.start();
        Vector2f p1 = arc.end();

        double rx = arc.rx();
        double ry = arc.ry();
        double alpha = arc.angle();
        boolean largeArcFlag = arc.largeArc();
        boolean sweepFlag = arc.sweep();

        // Step 1: Transform to ellipse local coordinates
        double[] transformedPoints = transformToLocalCoordinates
                (p0.x(), p0.y(), p1.x(), p1.y(), rx, ry, alpha, largeArcFlag, sweepFlag
                );
        double cx = transformedPoints[0];
        double cy = transformedPoints[1];
        double startAngle = transformedPoints[2];
        double sweepAngle = transformedPoints[3];

        // Step 2: Adjust sweep angle based on flags
        if (!sweepFlag) {
            sweepAngle = -sweepAngle;
        }
        if (largeArcFlag && Math.abs(sweepAngle) < Math.PI - EPSILON) {
            sweepAngle += 2 * Math.PI * (sweepAngle > 0 ? 1 : -1);
        } else if (!largeArcFlag && Math.abs(sweepAngle) > Math.PI) {
            sweepAngle -= 2 * Math.PI * (sweepAngle > 0 ? 1 : -1);
        }

        // Step 3: Divide the arc into smaller segments (if needed)
        int segments = (int) Math.ceil(Math.abs(sweepAngle) / (Math.PI / 2.0));
        double segmentAngle = sweepAngle / segments;

        double currentAngle = startAngle;
        for (int i = 0; i < segments; i++) {
            double nextAngle = currentAngle + segmentAngle;

            // Step 4: Calculate Bézier points for the segment
            Vector2f[] t = calculateBezierPoints(cx, cy, rx, ry, currentAngle, nextAngle, alpha);
            generateBezierSegment.accept(t);

            currentAngle = nextAngle;
        }
    }

    private static double[] transformToLocalCoordinates(
            double x,
            double y,
            double x1,
            double y1,
            double rx,
            double ry,
            double alpha,
            boolean largeArc,
            boolean sweep) {

        // Rotate points to align with ellipse's axes
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);

        double xPrime = cosAlpha * (x1 - x) / 2 + sinAlpha * (y1 - y) / 2;
        double yPrime = -sinAlpha * (x1 - x) / 2 + cosAlpha * (y1 - y) / 2;

        // Adjust radii if needed
        double scale = Math.sqrt((xPrime * xPrime) / (rx * rx) + (yPrime * yPrime) / (ry * ry));
        if (scale == 0) {
            return new double[]{(float) x - cosAlpha * rx, (float) y - sinAlpha * ry, 0, 0};
        }
        if (scale > 1) {
            rx *= scale;
            ry *= scale;
        }

        // Compute center point of ellipse
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double xPrime2 = xPrime * xPrime;
        double yPrime2 = yPrime * yPrime;

        double centerFactor = Math.sqrt(Math.abs((rx2 * ry2 - rx2 * yPrime2 - ry2 * xPrime2) /
                (rx2 * yPrime2 + ry2 * xPrime2)));
        double cxPrime = centerFactor * rx * yPrime / ry;
        double cyPrime = centerFactor * ry * xPrime / rx;

        // Adjust center point based on flags
        if (largeArc != sweep) {
            cxPrime = -cxPrime;
            cyPrime = -cyPrime;
        }

        // Transform center back to global coordinates
        double cx = (x + x1) / 2 + cosAlpha * cxPrime - sinAlpha * cyPrime;
        double cy = (y + y1) / 2 - sinAlpha * cxPrime - cosAlpha * cyPrime;

        // Compute angles
        // Transform start point into local coordinates
        double startxLocal = cosAlpha * (x - cx) + sinAlpha * (y - cy);
        double startyLocal = -sinAlpha * (x - cx) + cosAlpha * (y - cy);
        double startAngle = atan2(startyLocal / ry, startxLocal / rx);

        double endxLocal = cosAlpha * (x1 - cx) + sinAlpha * (y1 - cy);
        double endyLocal = -sinAlpha * (x1 - cx) + cosAlpha * (y1 - cy);
        double endAngle = atan2(endyLocal / ry, endxLocal / rx);
        double sweepAngle = endAngle - startAngle;

        return new double[]{(float) cx, (float) cy, (float) startAngle, (float) sweepAngle};
    }

    double EPSILON = 1.0E-6;

    private static double atan2(double y, double x) {
        if (x >= -EPSILON) {
            x = Math.max(0, x);
        }
        if (y >= -EPSILON) {
            y = Math.max(0, y);
        }

        double theta = Math.atan2(y, x);
        if (theta < 0) {
            theta += 2 * Math.PI;
        }

        return theta;
    }

    private static Vector2f[] calculateBezierPoints(
            double cx, double cy, double rx, double ry, double startAngle, double endAngle, double alpha) {

        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);
        double cosStart = Math.cos(startAngle);
        double sinStart = Math.sin(startAngle);
        double cosEnd = Math.cos(endAngle);
        double sinEnd = Math.sin(endAngle);

        // Start and end points
        double x0 = rx * cosStart;
        double y0 = ry * sinStart;
        double x1 = rx * cosEnd;
        double y1 = ry * sinEnd;

        // Tangent vectors at start and end
        double k = (4.0 / 3.0) * Math.tan((endAngle - startAngle) / 4.0);
        double x0T = -k * rx * sinStart;
        double y0T = k * ry * cosStart;
        double x1T = k * rx * sinEnd;
        double y1T = -k * ry * cosEnd;

        // Transform to global coordinates
        return new Vector2f[]{
                Vector2f.of(
                        (float) (cosAlpha * (x0 + x0T) - sinAlpha * (y0 + y0T) + cx),
                        (float) (sinAlpha * (x0 + x0T) + cosAlpha * (y0 + y0T) + cy)
                ),
                Vector2f.of(
                        (float) (cosAlpha * (x1 + x1T) - sinAlpha * (y1 + y1T) + cx),
                        (float) (sinAlpha * (x1 + x1T) + cosAlpha * (y1 + y1T) + cy)
                ),
                Vector2f.of(
                        (float) (cosAlpha * x1 - sinAlpha * y1 + cx),
                        (float) (sinAlpha * x1 + cosAlpha * y1 + cy)
                )
        };
    }

}