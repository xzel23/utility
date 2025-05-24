package com.dua3.utility.text;

import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.ui.Graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Represents a fragmented text that can be rendered within a specified bounding rectangle.
 * <p>
 * The FragmentedText class holds a list of lines, where each line is represented by a list of {@link Fragment}
 * objects. Fragments are text segments that cannot be split and have uniform font attributes. The text is split
 * based on whitespace characters and text decorations (font, text decoration). The class also stores the dimensions
 * and position of the text within the bounding rectangle.
 *
 * @param lines         a list of lines, where each line is represented by a list of Fragment objects
 * @param width         the width of the layout area for the text
 * @param height        the height of the layout area for the text
 * @param baseLine      the baseline value of the line the fragment belongs to
 * @param actualWidth   the calculated width of the text after layout
 * @param actualHeight  the calculated height of the text after layout
 */
public record FragmentedText(
        List<List<Fragment>> lines,
        float width,
        float height,
        float baseLine,
        float actualWidth,
        float actualHeight) {
    /**
     * Value to pass for {@code wrapWidth} to disable automatic wrapping.
     */
    public static final float NO_WRAP = Float.MAX_VALUE;

    private static final Pattern PATTERN = Pattern.compile("(?<=\\s)|(?=\\s)");
    private static final Pattern PATTERN_SPLIT_PRESERVE_WHITESPACE = Pattern.compile("(?<=\\s)|(?=\\s)");
    private static final FragmentedText EMPTY_FRAGMENTED_TEXT = new FragmentedText(Collections.emptyList(), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);

    /**
     * Returns an empty instance of {@code FragmentedText}.
     *
     * @return an empty {@code FragmentedText} object.
     */
    public static FragmentedText empty() {
        return EMPTY_FRAGMENTED_TEXT;
    }

    /**
     * Retrieves the layout dimensions of the text as a {@code Dimension2f} object.
     *
     * <p>>The dimensions are the ones received as input for the layout operation.
     *
     * @return a {@code Dimension2f} object representing the width and height of the text
     */
    public Dimension2f getLayoutDimension() {
        return Dimension2f.of(width, height);
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

    /**
     * Calculates the horizontal delta between the defined width and the actual width of the text.
     *
     * @return the difference between the defined width and the actual width of the text.
     */
    public float getHDelta() {
        return width - actualWidth;
    }

    /**
     * Calculates the vertical delta between the layout height and the actual height of the text.
     *
     * @return the difference between the layout height and the actual height of the text as a float
     */
    public float getVDelta() {
        return height - actualHeight;
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
     * @param text      the text
     * @param fontUtil  the {@link FontUtil} to use for generating fonts
     * @param font      the default {@link Font}
     * @param width     the height of the output area; used for horizontal alignment
     * @param height    the height of the output area; used for vertical alignment
     * @param hAlign    the horizontal alignment setting
     * @param vAlign    the vertical alignment setting
     * @param hAnchor   the horizontal anchor setting
     * @param vAnchor   the vertical anchor setting
     * @param wrapWidth the width at which to apply wrapping; pass NO_WRAP to disable wrapping
     * @return the fragmented text as a {@code FragmentedText} instance
     */
    public static FragmentedText generateFragments(
            RichText text,
            FontUtil<?> fontUtil,
            Font font,
            float width,
            float height,
            Alignment hAlign,
            VerticalAlignment vAlign,
            Graphics.HAnchor hAnchor,
            Graphics.VAnchor vAnchor,
            float wrapWidth) {
        boolean wrap = wrapWidth != Float.MAX_VALUE;

        UnaryOperator<RichText> trimLine = switch (hAlign) {
            case LEFT -> RichText::stripTrailing;
            case RIGHT -> RichText::stripLeading;
            case CENTER, JUSTIFY, DISTRIBUTE -> RichText::strip;
        };

        // generate lists of chunks for each line
        List<List<Fragment>> fragmentLines = new ArrayList<>();
        float textWidth = 0.0f;
        float textHeight = 0.0f;
        float baseLine = 0.0f;
        RichText[] split = text.split(TextUtil.PATTERN_LINE_END);
        for (int i = 0; i < split.length; i++) {
            RichText line = split[i];
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
            for (int j = 0; j < parts.size(); j++) {
                var run = parts.get(j);

                Font f = fontUtil.deriveFont(font, run.getFontDef());
                Rectangle2f tr = fontUtil.getTextDimension(run, f);
                if (wrapAllowed && xAct + tr.width() > wrapWidth) {
                    if (!fragments.isEmpty() && TextUtil.isBlank(fragments.getLast().text())) {
                        // remove trailing whitespace
                        Fragment removed = fragments.removeLast();
                        whitespace -= removed.w();
                        assert whitespace >= 0.0f : "whitespace must be non-negative after removing trailing whitespace";
                    } else if (TextUtil.isBlank(run)) {
                        // skip leading whitespace after wrapped line
                        continue;
                    }
                    boolean isLastLine = j == parts.size() - 1;
                    lineWidth = applyHAlign(fragments, hAlign, width, lineWidth, whitespace, isLastLine);
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

            boolean isLastLine = i == split.length - 1;
            lineWidth = applyHAlign(fragments, hAlign, width, lineWidth, whitespace, isLastLine);
            textWidth = Math.max(textWidth, lineWidth);
            textHeight += lineHeight;
            baseLine = lineBaseLine;
        }

        // apply anchor and vertical alignment
        float tx = switch (hAnchor) {
            case LEFT -> 0.0f;
            case RIGHT -> -width;
            case CENTER -> -width / 2.0f;
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
                width,
                height,
                baseLine,
                textWidth,
                textHeight
        );
    }

    private static Alignment getEffectiveHAlign(Alignment hAlign, boolean isLastLine) {
        return hAlign == Alignment.JUSTIFY
                ? (isLastLine ? Alignment.LEFT : Alignment.DISTRIBUTE)
                : hAlign;
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

        for (List<Fragment> line : fragmentLines) {
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
    private static float applyHAlign(
            List<Fragment> line,
            Alignment hAlign,
            float width,
            float lineWidth,
            float whitespace,
            boolean isLastLine) {
        float availableSpace = Math.max(0.0f, width - lineWidth);
        float f = whitespace > 0 ? 1.0f + availableSpace / whitespace : 1.0f;
        Alignment effectiveHAlign = getEffectiveHAlign(hAlign, isLastLine);
        switch (effectiveHAlign) {
            case LEFT -> { /* nothing to do */ }
            case RIGHT -> line.replaceAll(fragment -> fragment.translate(availableSpace, 0.0f));
            case CENTER -> line.replaceAll(fragment -> fragment.translate(availableSpace / 2.0f, 0.0f));
            case DISTRIBUTE -> {
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
            default -> throw new IllegalStateException(effectiveHAlign.name() + " is not allowed here");
        }

        if (line.isEmpty()) {
            return 0.0f;
        } else {
            Fragment first = line.getFirst();
            Fragment last = line.getLast();
            return last.x() + last.w() - first.x();
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
        return Arrays.stream(line.split(PATTERN_SPLIT_PRESERVE_WHITESPACE))
                .<Run>mapMulti(Iterable::forEach)
                .toList();
    }

    /**
     * Retrieves the rectangle that bounds the text based on its position and actual dimensions.
     *
     * <p>This method calculates the bounding rectangle by determining the smallest x-coordinate,
     * y-coordinate, and incorporating the actual width and height of the text. If no lines
     * of text are present, it returns a rectangle with all dimensions set to zero.
     *
     * @return a {@code Rectangle2f} object representing the bounding box of the text.
     */
    public Rectangle2f getTextRec() {
        if (isEmpty()) {
            return Rectangle2f.of(0, 0, 0, 0);
        }

        float y = lines.getFirst().getFirst().y();
        float x = (float) lines.stream()
                .filter(list -> !list.isEmpty())
                .mapToDouble(line -> line.getFirst().x())
                .min()
                .orElse(0.0);

        return Rectangle2f.of(x, y, actualWidth, actualHeight);
    }

    /**
     * Test if this {@code FragmentedText} is empty.
     *
     * @return true, if this text is empty.
     */
    public boolean isEmpty() {
        return lines.isEmpty();
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
    public record Fragment(float x, float y, float w, float h, float baseLine, Font font, CharSequence text) {
        Fragment translate(float dx, float dy) {
            return new Fragment(x + dx, y + dy, w, h, baseLine, font, text);
        }
    }
}
