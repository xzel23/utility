package com.dua3.utility.ui;

import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Shared helper for editor visual-line layout and caret hit testing.
 */
public final class RichTextVisualLayoutHelper {

    /**
     * Logical text block corresponding to one source line without the trailing '\n'.
     *
     * @param start start offset in source text
     * @param end end offset in source text
     * @param text detached block text
     */
    public record LogicalBlock(int start, int end, RichText text) {}

    /**
     * Mapping function from layout positions back to source positions.
     */
    @FunctionalInterface
    public interface LayoutToSourcePosition {
        int map(int layoutPosition);
    }

    /**
     * Block layout result used to derive editor visual lines.
     *
     * @param renderLines fragment lines for this block
     * @param height rendered block height
     * @param layoutToSourcePosition layout-to-source position mapper
     */
    public record BlockLayout(
            List<List<FragmentedText.Fragment>> renderLines,
            double height,
            LayoutToSourcePosition layoutToSourcePosition
    ) {
        /**
         * Constructor.
         *
         * @param renderLines fragment lines
         * @param height rendered height
         * @param layoutToSourcePosition mapping callback
         */
        public BlockLayout {
            Objects.requireNonNull(renderLines, "renderLines");
            Objects.requireNonNull(layoutToSourcePosition, "layoutToSourcePosition");

            List<List<FragmentedText.Fragment>> immutableLines = new ArrayList<>(renderLines.size());
            for (List<FragmentedText.Fragment> line : renderLines) {
                immutableLines.add(List.copyOf(line));
            }

            renderLines = List.copyOf(immutableLines);
            height = Math.max(1.0, height);
        }
    }

    /**
     * Visual line in source-coordinate space.
     *
     * @param start source start (inclusive)
     * @param end source end (inclusive, caret-right boundary)
     * @param top line top in pixels
     * @param height line height in pixels
     * @param boundaries x boundaries for caret positions in {@code [start, end]}
     */
    public record VisualLine(int start, int end, double top, double height, double[] boundaries) {
        /**
         * Constructor.
         *
         * @param start line start
         * @param end line end
         * @param top line top
         * @param height line height
         * @param boundaries x boundaries
         */
        public VisualLine {
            boundaries = boundaries.clone();
        }

        /**
         * Number of source characters on this line.
         *
         * @return line length
         */
        public int length() {
            return Math.max(0, end - start);
        }

        /**
         * Left-most x coordinate for this line.
         *
         * @return minimum x
         */
        public double minX() {
            return boundaries.length == 0 ? 0.0 : boundaries[0];
        }

        /**
         * Right-most x coordinate for this line.
         *
         * @return maximum x
         */
        public double maxX() {
            return boundaries.length == 0 ? 0.0 : boundaries[boundaries.length - 1];
        }
    }

    private RichTextVisualLayoutHelper() {
        // utility class
    }

    /**
     * Splits text into logical source blocks, preserving empty lines.
     *
     * @param text source text
     * @return immutable list of logical blocks
     */
    public static List<LogicalBlock> splitLogicalBlocks(RichText text) {
        Objects.requireNonNull(text, "text");

        List<LogicalBlock> logicalBlocks = new ArrayList<>();
        int lineStart = 0;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == '\n') {
                logicalBlocks.add(new LogicalBlock(lineStart, i, detachedSubSequence(text, lineStart, i)));
                lineStart = i + 1;
            }
        }
        logicalBlocks.add(new LogicalBlock(lineStart, length, detachedSubSequence(text, lineStart, length)));
        return List.copyOf(logicalBlocks);
    }

    /**
     * Builds visual lines for logical blocks.
     *
     * @param logicalBlocks logical blocks
     * @param defaultLineHeight fallback line height
     * @param blockLayoutFactory layout factory per logical block
     * @return immutable visual lines
     */
    public static List<VisualLine> buildVisualLines(
            List<LogicalBlock> logicalBlocks,
            double defaultLineHeight,
            Function<? super RichText, BlockLayout> blockLayoutFactory
    ) {
        Objects.requireNonNull(logicalBlocks, "logicalBlocks");
        Objects.requireNonNull(blockLayoutFactory, "blockLayoutFactory");

        double lineHeight = Math.max(1.0, defaultLineHeight);
        if (logicalBlocks.isEmpty()) {
            return List.of(new VisualLine(0, 0, 0.0, lineHeight, new double[]{0.0}));
        }

        FontUtil fontUtil = FontUtil.getInstance();
        List<VisualLine> lines = new ArrayList<>();
        double yOffset = 0.0;
        for (LogicalBlock block : logicalBlocks) {
            if (block.start() == block.end()) {
                lines.add(new VisualLine(block.start(), block.start(), yOffset, lineHeight, new double[]{0.0}));
                yOffset += lineHeight;
                continue;
            }

            BlockLayout layout = Objects.requireNonNull(blockLayoutFactory.apply(block.text()));
            List<VisualLine> blockLines = new ArrayList<>();
            for (List<FragmentedText.Fragment> fragmentLine : layout.renderLines()) {
                VisualLine localLine = toLocalVisualLine(fragmentLine, layout.layoutToSourcePosition(), fontUtil, lineHeight);
                if (localLine == null) {
                    continue;
                }

                blockLines.add(new VisualLine(
                        block.start() + localLine.start(),
                        block.start() + localLine.end(),
                        yOffset + localLine.top(),
                        localLine.height(),
                        localLine.boundaries()
                ));
            }

            if (blockLines.isEmpty()) {
                blockLines.add(new VisualLine(block.start(), block.start(), yOffset, lineHeight, new double[]{0.0}));
            }

            lines.addAll(blockLines);
            yOffset += Math.max(lineHeight, layout.height());
        }

        if (lines.isEmpty()) {
            lines.add(new VisualLine(0, 0, 0.0, lineHeight, new double[]{0.0}));
        }
        return List.copyOf(lines);
    }

    /**
     * Resolves source index for a visual point.
     *
     * @param lines visual lines
     * @param x x coordinate
     * @param y y coordinate
     * @return nearest source index
     */
    public static int indexForPoint(List<VisualLine> lines, double x, double y) {
        if (lines.isEmpty()) {
            return 0;
        }

        if (y <= lines.getFirst().top()) {
            return indexForX(lines.getFirst(), 0.0);
        }

        for (VisualLine line : lines) {
            if (y < line.top() + line.height()) {
                return indexForX(line, x);
            }
        }

        return indexForX(lines.getLast(), Double.MAX_VALUE);
    }

    /**
     * Finds visual line index for a caret offset.
     *
     * @param lines visual lines
     * @param caret source caret position
     * @return line index
     */
    public static int lineIndexForCaret(List<VisualLine> lines, int caret) {
        int fallback = lines.size() - 1;
        for (int i = 0; i < lines.size(); i++) {
            VisualLine line = lines.get(i);
            if (caret < line.start()) {
                return Math.max(0, i - 1);
            }

            if (caret > line.end()) {
                fallback = i;
                continue;
            }

            // At shared boundaries (especially empty lines), prefer the latest matching line
            // so caret positions can resolve to visually empty lines.
            int candidate = i;
            while (candidate + 1 < lines.size()) {
                VisualLine next = lines.get(candidate + 1);
                if (caret < next.start() || caret > next.end()) {
                    break;
                }
                candidate++;
            }
            return candidate;
        }
        return fallback;
    }

    /**
     * Returns the caret x coordinate for the given source index in a visual line.
     *
     * @param line visual line
     * @param index source index
     * @return x coordinate
     */
    public static double xForIndex(VisualLine line, int index) {
        int offset = Math.clamp((long) index - line.start(), 0, line.length());
        return line.boundaries()[offset];
    }

    /**
     * Resolves source index for x coordinate in a visual line.
     *
     * @param line visual line
     * @param x x coordinate
     * @return nearest source index
     */
    public static int indexForX(VisualLine line, double x) {
        if (x <= line.minX()) {
            return line.start();
        }
        if (x >= line.maxX()) {
            return line.end();
        }

        double[] boundaries = line.boundaries();
        for (int i = 0; i < line.length(); i++) {
            double midpoint = (boundaries[i] + boundaries[i + 1]) * 0.5;
            if (x < midpoint) {
                return line.start() + i;
            }
        }
        return line.end();
    }

    private static @Nullable VisualLine toLocalVisualLine(
            List<FragmentedText.Fragment> fragmentLine,
            LayoutToSourcePosition layoutToSourcePosition,
            FontUtil fontUtil,
            double defaultLineHeight
    ) {
        if (fragmentLine.isEmpty()) {
            return null;
        }

        double lineTop = fragmentLine.getFirst().y();
        double lineHeight = fragmentLine.stream()
                .mapToDouble(FragmentedText.Fragment::h)
                .max()
                .orElse(defaultLineHeight);
        Map<Integer, Double> sourceBoundaries = new LinkedHashMap<>();

        int lineStart = Integer.MAX_VALUE;
        int lineEnd = Integer.MIN_VALUE;

        for (FragmentedText.Fragment fragment : fragmentLine) {
            if (!(fragment.text() instanceof Run run)) {
                continue;
            }

            int fragmentStart = run.getStart();
            int fragmentEnd = run.getEnd();
            for (int layoutPos = fragmentStart; layoutPos <= fragmentEnd; layoutPos++) {
                int rel = layoutPos - fragmentStart;
                double x = fragment.x() + textWidth(fontUtil, run, rel, fragment.font());
                int sourcePos = layoutToSourcePosition.map(layoutPos);
                lineStart = Math.min(lineStart, sourcePos);
                lineEnd = Math.max(lineEnd, sourcePos);
                sourceBoundaries.merge(sourcePos, x, Math::min);
            }
        }

        if (lineStart == Integer.MAX_VALUE || lineEnd < lineStart) {
            return new VisualLine(0, 0, lineTop, Math.max(1.0, lineHeight), new double[]{0.0});
        }

        double[] boundaries = new double[lineEnd - lineStart + 1];
        double x = sourceBoundaries.getOrDefault(lineStart, 0.0);
        for (int sourcePos = lineStart; sourcePos <= lineEnd; sourcePos++) {
            Double mappedX = sourceBoundaries.get(sourcePos);
            if (mappedX != null) {
                x = mappedX;
            }
            boundaries[sourcePos - lineStart] = x;
        }

        return new VisualLine(lineStart, lineEnd, lineTop, Math.max(1.0, lineHeight), boundaries);
    }

    private static double textWidth(FontUtil fontUtil, Run run, int length, com.dua3.utility.text.Font font) {
        if (length <= 0) {
            return 0.0;
        }
        if (length >= run.length()) {
            return fontUtil.getTextWidth(run, font);
        }
        return fontUtil.getTextWidth(run.subSequence(0, length), font);
    }

    private static RichText detachedSubSequence(RichText text, int start, int end) {
        if (start >= end) {
            return RichText.emptyText();
        }

        RichTextBuilder builder = new RichTextBuilder(end - start);
        text.appendTo(builder, start, end);
        return builder.toRichText();
    }
}
