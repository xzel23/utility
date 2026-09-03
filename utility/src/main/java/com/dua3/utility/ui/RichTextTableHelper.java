package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;

/**
 * Extracts renderer-neutral table information from structurally annotated [RichText].
 * <p>
 * Markdown conversion deliberately represents cells with ordinary text separated by tab and newline delimiters.
 * This helper consumes that contract and keeps all source offsets intact, so a renderer can place cells visually
 * without leaking Markdown syntax or inventing a parallel document model.
 */
public final class RichTextTableHelper {
    /** Attribute containing a document-local table identifier as an [Integer]. */
    public static final String ATTRIBUTE_TABLE_ID = "table-id";
    /** Attribute containing a zero-based table row index as an [Integer]. */
    public static final String ATTRIBUTE_TABLE_ROW = "table-row";
    /** Attribute containing a zero-based table column index as an [Integer]. */
    public static final String ATTRIBUTE_TABLE_COLUMN = "table-column";
    /** Attribute identifying table header rows and cells as a [Boolean]. */
    public static final String ATTRIBUTE_TABLE_HEADER = "table-header";
    /** Attribute containing the column-alignment enum emitted by the document producer. */
    public static final String ATTRIBUTE_TABLE_COLUMN_ALIGNMENT = "table-column-alignment";

    /** Style property containing a table border [Color]. */
    public static final String STYLE_ATTRIBUTE_BORDER_COLOR = "markdown-table-border-color";
    /** Style property containing a positive table border width as a [Number]. */
    public static final String STYLE_ATTRIBUTE_BORDER_WIDTH = "markdown-table-border-width";

    private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;
    private static final float DEFAULT_BORDER_WIDTH = 1.0f;
    /** Preferred lower bound for a column whose natural content is wider than this value. */
    public static final float DEFAULT_MINIMUM_COLUMN_WIDTH = 96.0f;

    private RichTextTableHelper() {
        // utility class
    }

    /**
     * Extracts complete tables from the source text in source order.
     * <p>
     * Incomplete or malformed table fragments are omitted so callers can safely use normal text layout as a
     * fallback for the original text.
     *
     * @param source structurally annotated rich text
     * @return all complete tables in source order
     */
    public static List<Table> tables(RichText source) {
        Map<Integer, TableAccumulator> tables = new LinkedHashMap<>();
        for (int index = 0; index < source.length(); index++) {
            Map<String, @Nullable Object> attributes = source.attributesAt(index);
            Integer tableId = integerAttribute(attributes, ATTRIBUTE_TABLE_ID);
            Integer rowIndex = integerAttribute(attributes, ATTRIBUTE_TABLE_ROW);
            if (tableId == null || rowIndex == null) {
                continue;
            }

            TableAccumulator table = tables.computeIfAbsent(tableId, TableAccumulator::new);
            table.include(index);
            RowAccumulator row = table.rows.computeIfAbsent(rowIndex, RowAccumulator::new);
            row.include(index, Boolean.TRUE.equals(attributes.get(ATTRIBUTE_TABLE_HEADER)));

            if (source.charAt(index) == '\t') {
                Integer columnIndex = integerAttribute(attributes, ATTRIBUTE_TABLE_COLUMN);
                if (columnIndex != null) {
                    row.addCell(
                            columnIndex,
                            columnAlignment(attributes.get(ATTRIBUTE_TABLE_COLUMN_ALIGNMENT)),
                            row.nextCellStart,
                            index,
                            source.runAt(index).getFontDef().getBackgroundColor()
                    );
                    row.nextCellStart = index + 1;
                }
            } else if (source.charAt(index) == '\n') {
                row.complete(index + 1);
            }
        }

        return tables.values().stream()
                .map(table -> table.toTable(source, presentation(source, table.id)))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(Table::start))
                .toList();
    }

    /**
     * Replaces every complete table with a single inline-node placeholder for visual layout.
     *
     * <p>The placeholder retains the full table model, including each cell's original rich text. This conversion is
     * intended for a view-only render pass; callers that need source-position mapping must retain the original text.
     *
     * @param source structurally annotated rich text
     * @return text in which complete tables are inline placeholders
     */
    public static RichText replaceTablesWithInlineNodes(RichText source) {
        List<Table> tables = tables(source);
        if (tables.isEmpty()) {
            return source;
        }

        RichTextBuilder builder = new RichTextBuilder(source.length() - tables.size());
        int position = 0;
        for (Table table : tables) {
            source.subSequence(position, table.start()).appendTo(builder);
            Style style = Style.create(
                    "rich-text-table-inline",
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, new InlineTable(table)),
                    // Tables are block-like inline nodes. Baseline alignment would position their bottom on the
                    // source line and let the table cover text that precedes it.
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR, VAnchor.TOP)
            );
            builder.push(style);
            builder.append('\uFFFC');
            builder.pop(style);
            // A complete structural table includes the newline terminating its final row. The visual placeholder
            // must restore that block break; otherwise the following paragraph is laid out on the table's line.
            builder.append('\n');
            position = table.end();
        }
        source.subSequence(position, source.length()).appendTo(builder);
        return builder.toRichText();
    }

    /**
     * Measures and positions every cell in a table.
     *
     * <p>When wrapping is enabled, the table fits the available width. Columns first receive their natural width
     * when possible, otherwise wide columns receive a readable minimum while naturally narrow columns retain their
     * smaller width. Remaining width is distributed according to each column's unmet natural demand. Cell text is
     * returned as positioned fragments so toolkit renderers can use their normal rich-text paint path.
     *
     * @param table the extracted table
     * @param fontUtil text measurement implementation
     * @param font default font
     * @param availableWidth maximum table width when wrapping is enabled
     * @param wrapText whether cells may wrap to fit the available width
     * @param cellPadding non-negative padding on each side of a cell's content
     * @return table geometry and positioned cell fragments
     */
    public static TableLayout layout(
            Table table,
            FontUtil fontUtil,
            Font font,
            float availableWidth,
            boolean wrapText,
            float cellPadding
    ) {
        if (!Float.isFinite(availableWidth) || availableWidth <= 0.0f) {
            throw new IllegalArgumentException("available width must be finite and greater than zero: " + availableWidth);
        }
        if (!Float.isFinite(cellPadding) || cellPadding < 0.0f) {
            throw new IllegalArgumentException("cell padding must be finite and non-negative: " + cellPadding);
        }

        int columnCount = table.rows().stream()
                .flatMap(row -> row.cells().stream())
                .mapToInt(Cell::column)
                .max()
                .orElse(-1) + 1;
        if (columnCount == 0) {
            return new TableLayout(table, new Rectangle2f(0, 0, 0, 0), List.of(), List.of());
        }

        float[] naturalColumnWidths = naturalColumnWidths(table, columnCount, fontUtil, font);
        float[] unbreakableColumnWidths = unbreakableColumnWidths(table, columnCount, fontUtil, font);
        float[] columnWidths = resolveColumnWidths(
                naturalColumnWidths, unbreakableColumnWidths, availableWidth, wrapText, cellPadding, font
        );
        float defaultContentHeight = defaultContentHeight(fontUtil, font);
        List<RowLayout> rows = new ArrayList<>(table.rows().size());
        float y = 0.0f;
        for (Row row : table.rows()) {
            List<MeasuredCell> measuredCells = new ArrayList<>(row.cells().size());
            float rowHeight = defaultContentHeight + 2.0f * cellPadding;
            for (Cell cell : row.cells()) {
                float contentWidth = columnWidths[cell.column()];
                FragmentedText fragments = fragments(cell.text(), fontUtil, font, contentWidth, wrapText, cell.alignment());
                float contentHeight = Math.max(defaultContentHeight, fragments.actualHeight());
                measuredCells.add(new MeasuredCell(cell, fragments, contentHeight));
                rowHeight = Math.max(rowHeight, contentHeight + 2.0f * cellPadding);
            }

            List<CellLayout> cells = new ArrayList<>(measuredCells.size());
            for (MeasuredCell measured : measuredCells) {
                float x = columnStart(columnWidths, measured.cell.column(), cellPadding);
                float width = columnWidths[measured.cell.column()] + 2.0f * cellPadding;
                Rectangle2f bounds = new Rectangle2f(x, y, width, rowHeight);
                Rectangle2f contentBounds = new Rectangle2f(
                        x + cellPadding,
                        y + cellPadding,
                        columnWidths[measured.cell.column()],
                        Math.max(0.0f, rowHeight - 2.0f * cellPadding)
                );
                cells.add(new CellLayout(
                        measured.cell,
                        bounds,
                        contentBounds,
                        translate(measured.fragments.lines(), contentBounds.x(), contentBounds.y()),
                        measured.cell.backgroundColor()
                ));
            }
            rows.add(new RowLayout(row, new Rectangle2f(0, y, tableWidth(columnWidths, cellPadding), rowHeight), List.copyOf(cells)));
            y += rowHeight;
        }

        float width = tableWidth(columnWidths, cellPadding);
        Rectangle2f bounds = new Rectangle2f(0, 0, width, y);
        return new TableLayout(table, bounds, List.copyOf(rows), gridLines(columnWidths, rows, width, y, cellPadding));
    }

    /**
     * Resolves a source insertion position for a point in table-local coordinates.
     *
     * <p>Cell delimiters have no painted glyph. A point in their visual cell resolves to the preceding cell's end,
     * while a point on a row's unused area resolves to the nearest cell in that row.
     *
     * @param layout positioned table geometry
     * @param x table-local x-coordinate
     * @param y table-local y-coordinate
     * @param fontUtil text measurement implementation
     * @return the nearest source insertion position, or empty when the point is outside the table
     */
    public static OptionalInt sourcePositionForPoint(TableLayout layout, float x, float y, FontUtil fontUtil) {
        if (!contains(layout.bounds(), x, y)) {
            return OptionalInt.empty();
        }
        RowLayout row = nearestRow(layout.rows(), y);
        if (row == null || row.cells().isEmpty()) {
            return OptionalInt.of(layout.table().start());
        }
        CellLayout cell = nearestCell(row.cells(), x);
        return OptionalInt.of(sourcePositionInCell(cell, x, y, fontUtil));
    }

    /**
     * Resolves the painted caret geometry for an insertion position in a table.
     *
     * @param layout positioned table geometry
     * @param sourcePosition source insertion position
     * @param fontUtil text measurement implementation
     * @return table-local caret geometry, or empty when the position is outside the table
     */
    public static Optional<Caret> caretForSourcePosition(TableLayout layout, int sourcePosition, FontUtil fontUtil) {
        if (sourcePosition < layout.table().start() || sourcePosition > layout.table().end()) {
            return Optional.empty();
        }
        CellLayout cell = cellForSourcePosition(layout, sourcePosition);
        if (cell == null) {
            return Optional.empty();
        }

        for (List<FragmentedText.Fragment> line : cell.fragments()) {
            for (FragmentedText.Fragment fragment : line) {
                if (!(fragment.text() instanceof Run run)) {
                    continue;
                }
                int runStart = sourceStart(cell.cell(), run);
                int runEnd = runStart + run.length();
                if (sourcePosition < runStart || sourcePosition > runEnd) {
                    continue;
                }
                float x = fragment.x() + textWidth(fontUtil, run, sourcePosition - runStart, fragment.font());
                return Optional.of(new Caret(x, fragment.y(), Math.max(1.0f, fragment.h())));
            }
        }

        // Empty cells and invisible delimiters still need a stable caret location.
        return Optional.of(new Caret(
                cell.contentBounds().x(),
                cell.contentBounds().y(),
                Math.max(1.0f, cell.contentBounds().height())
        ));
    }

    /**
     * Returns whole-cell selection rectangles for a source range in a table.
     *
     * <p>Tables are atomic visual structures: selecting any visible text or an invisible tab/newline delimiter
     * highlights the containing cell. This makes selections unambiguous even when cells contain wrapped text.
     *
     * @param layout positioned table geometry
     * @param sourceStart inclusive selection start
     * @param sourceEnd exclusive selection end
     * @return table-local cell rectangles in source order
     */
    public static List<Rectangle2f> selectionBounds(TableLayout layout, int sourceStart, int sourceEnd) {
        if (sourceStart >= sourceEnd || sourceEnd <= layout.table().start() || sourceStart >= layout.table().end()) {
            return List.of();
        }
        List<Rectangle2f> result = new ArrayList<>();
        for (RowLayout row : layout.rows()) {
            for (int index = 0; index < row.cells().size(); index++) {
                CellLayout cell = row.cells().get(index);
                int cellEnd = cell.cell().end() + 1; // include its invisible tab delimiter
                if (index == row.cells().size() - 1) {
                    cellEnd = Math.max(cellEnd, row.row().end()); // include the row newline
                }
                if (sourceStart < cellEnd && cell.cell().start() < sourceEnd) {
                    result.add(cell.bounds());
                }
            }
        }
        return List.copyOf(result);
    }

    private static boolean contains(Rectangle2f bounds, float x, float y) {
        return x >= bounds.xMin() && x <= bounds.xMax() && y >= bounds.yMin() && y <= bounds.yMax();
    }

    private static @Nullable RowLayout nearestRow(List<RowLayout> rows, float y) {
        RowLayout nearest = null;
        float distance = Float.MAX_VALUE;
        for (RowLayout row : rows) {
            if (y >= row.bounds().yMin() && y <= row.bounds().yMax()) {
                return row;
            }
            float candidate = Math.abs(y - row.bounds().yCenter());
            if (candidate < distance) {
                distance = candidate;
                nearest = row;
            }
        }
        return nearest;
    }

    private static CellLayout nearestCell(List<CellLayout> cells, float x) {
        CellLayout nearest = cells.getFirst();
        float distance = Float.MAX_VALUE;
        for (CellLayout cell : cells) {
            if (x >= cell.bounds().xMin() && x <= cell.bounds().xMax()) {
                return cell;
            }
            float candidate = Math.abs(x - cell.bounds().xCenter());
            if (candidate < distance) {
                distance = candidate;
                nearest = cell;
            }
        }
        return nearest;
    }

    private static int sourcePositionInCell(CellLayout cell, float x, float y, FontUtil fontUtil) {
        List<FragmentedText.Fragment> nearestLine = null;
        float distance = Float.MAX_VALUE;
        for (List<FragmentedText.Fragment> line : cell.fragments()) {
            if (line.isEmpty()) {
                continue;
            }
            float top = line.getFirst().y();
            float height = line.stream().map(FragmentedText.Fragment::h).max(Float::compare).orElse(0.0f);
            if (y >= top && y <= top + height) {
                nearestLine = line;
                break;
            }
            float candidate = Math.abs(y - (top + height / 2.0f));
            if (candidate < distance) {
                distance = candidate;
                nearestLine = line;
            }
        }
        if (nearestLine == null) {
            return cell.cell().start();
        }
        FragmentedText.Fragment nearest = nearestLine.getFirst();
        distance = Float.MAX_VALUE;
        for (FragmentedText.Fragment fragment : nearestLine) {
            if (!(fragment.text() instanceof Run)) {
                continue;
            }
            if (x >= fragment.x() && x <= fragment.x() + fragment.w()) {
                nearest = fragment;
                break;
            }
            float candidate = Math.abs(x - (fragment.x() + fragment.w() / 2.0f));
            if (candidate < distance) {
                distance = candidate;
                nearest = fragment;
            }
        }
        if (!(nearest.text() instanceof Run run)) {
            return cell.cell().start();
        }
        return sourceStart(cell.cell(), run) + indexForX(fontUtil, run, nearest.font(), x - nearest.x());
    }

    private static @Nullable CellLayout cellForSourcePosition(TableLayout layout, int position) {
        CellLayout previous = null;
        for (RowLayout row : layout.rows()) {
            for (CellLayout cell : row.cells()) {
                if (position >= cell.cell().start() && position <= cell.cell().end()) {
                    return cell;
                }
                if (position > cell.cell().end()) {
                    previous = cell;
                }
            }
            if (position < row.row().end()) {
                return previous;
            }
        }
        return previous;
    }

    private static int indexForX(FontUtil fontUtil, Run run, Font font, float x) {
        if (x <= 0.0f) {
            return 0;
        }
        float fullWidth = textWidth(fontUtil, run, run.length(), font);
        if (x >= fullWidth) {
            return run.length();
        }
        float previous = 0.0f;
        for (int index = 1; index <= run.length(); index++) {
            float current = textWidth(fontUtil, run, index, font);
            if (x < (previous + current) / 2.0f) {
                return index - 1;
            }
            previous = current;
        }
        return run.length();
    }

    /**
     * RichText subsequences retain run offsets in some implementations and rebase them in others. Accept both so
     * table extraction remains independent of that storage detail.
     */
    private static int sourceStart(Cell cell, Run run) {
        if (run.getStart() >= cell.start() && run.getEnd() <= cell.end()) {
            return run.getStart();
        }
        return cell.start() + run.getStart();
    }

    private static float textWidth(FontUtil fontUtil, Run run, int length, Font font) {
        if (length <= 0) {
            return 0.0f;
        }
        if (length >= run.length()) {
            return (float) fontUtil.getTextWidth(run, font);
        }
        return (float) fontUtil.getTextWidth(run.subSequence(0, length), font);
    }

    private static float[] naturalColumnWidths(Table table, int columnCount, FontUtil fontUtil, Font font) {
        float[] widths = new float[columnCount];
        for (Row row : table.rows()) {
            for (Cell cell : row.cells()) {
                FragmentedText fragments = fragments(cell.text(), fontUtil, font, Float.MAX_VALUE, false, cell.alignment());
                widths[cell.column()] = Math.max(widths[cell.column()], fragments.actualWidth());
            }
        }
        float minimum = (float) Math.max(1.0f, font.getFontData().spaceWidth() * 2.0f);
        for (int column = 0; column < widths.length; column++) {
            widths[column] = Math.max(minimum, widths[column]);
        }
        return widths;
    }

    private static float[] resolveColumnWidths(
            float[] naturalWidths,
            float[] unbreakableWidths,
            float availableWidth,
            boolean wrapText,
            float cellPadding,
            Font font
    ) {
        if (!wrapText) {
            return naturalWidths.clone();
        }

        float availableContentWidth = Math.max(1.0f, availableWidth - 2.0f * cellPadding * naturalWidths.length);
        float naturalWidth = sum(naturalWidths);
        if (naturalWidth <= availableContentWidth) {
            return naturalWidths.clone();
        }

        float readableMinimum = (float) Math.max(DEFAULT_MINIMUM_COLUMN_WIDTH, font.getFontData().spaceWidth() * 8.0f);
        float[] minimumWidths = new float[naturalWidths.length];
        for (int column = 0; column < naturalWidths.length; column++) {
            // A short heading or value must not be inflated just because an adjacent column has long prose.
            minimumWidths[column] = Math.clamp(readableMinimum, unbreakableWidths[column], naturalWidths[column]);
        }

        float minimumWidth = sum(minimumWidths);
        if (minimumWidth <= availableContentWidth) {
            return distributeAdditionalWidth(minimumWidths, naturalWidths, availableContentWidth - minimumWidth);
        }

        // Many columns can make even all readable minima wider than the viewport. Keep short columns short and
        // share the necessary reduction among larger columns before falling back to a one-pixel emergency width.
        float emergencyWidth = (float) Math.max(1.0f, font.getFontData().spaceWidth() * 2.0f);
        float[] emergencyMinimums = new float[naturalWidths.length];
        for (int column = 0; column < naturalWidths.length; column++) {
            emergencyMinimums[column] = Math.min(naturalWidths[column], emergencyWidth);
        }
        float emergencyTotal = sum(emergencyMinimums);
        if (emergencyTotal > 0 && emergencyTotal >= availableContentWidth) {
            float ratio = availableContentWidth / emergencyTotal;
            float[] widths = emergencyMinimums.clone();
            scaleToTotal(widths, ratio, availableContentWidth);
            return widths;
        }
        return distributeAdditionalWidth(emergencyMinimums, minimumWidths, availableContentWidth - emergencyTotal);
    }

    private static float[] distributeAdditionalWidth(float[] lowerBounds, float[] upperBounds, float additionalWidth) {
        float[] widths = lowerBounds.clone();
        float remainingDemand = 0.0f;
        for (int column = 0; column < widths.length; column++) {
            remainingDemand += Math.max(0.0f, upperBounds[column] - widths[column]);
        }
        if (remainingDemand <= 0.0f || additionalWidth <= 0.0f) {
            return widths;
        }

        float assigned = 0.0f;
        for (int column = 0; column < widths.length - 1; column++) {
            float demand = Math.max(0.0f, upperBounds[column] - widths[column]);
            float extra = Math.min(demand, additionalWidth * demand / remainingDemand);
            widths[column] += extra;
            assigned += extra;
        }
        int last = widths.length - 1;
        widths[last] += Math.clamp(upperBounds[last] - widths[last], 0.0f, additionalWidth - assigned);
        return widths;
    }

    private static void scaleToTotal(float[] widths, float ratio, float total) {
        float assigned = 0.0f;
        for (int column = 0; column < widths.length - 1; column++) {
            widths[column] *= ratio;
            assigned += widths[column];
        }
        widths[widths.length - 1] = Math.max(0.0f, total - assigned);
    }

    private static float sum(float[] widths) {
        float total = 0.0f;
        for (float width : widths) {
            total += width;
        }
        return total;
    }

    private static FragmentedText fragments(
            RichText text,
            FontUtil fontUtil,
            Font font,
            float width,
            boolean wrapText,
            ColumnAlignment alignment
    ) {
        float contentWidth = Math.max(1.0f, width);
        return FragmentedText.generateFragments(
                text,
                fontUtil,
                font,
                contentWidth,
                Float.MAX_VALUE,
                toTextAlignment(alignment),
                VerticalAlignment.TOP,
                HAnchor.LEFT,
                VAnchor.TOP,
                wrapText ? contentWidth : FragmentedText.NO_WRAP
        );
    }

    private static float[] unbreakableColumnWidths(Table table, int columnCount, FontUtil fontUtil, Font font) {
        float[] widths = new float[columnCount];
        for (Row row : table.rows()) {
            for (Cell cell : row.cells()) {
                RichText text = cell.text();
                int segmentStart = 0;
                for (int index = 0; index <= text.length(); index++) {
                    if (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
                        continue;
                    }
                    if (segmentStart < index) {
                        FragmentedText segment = fragments(
                                text.subSequence(segmentStart, index),
                                fontUtil,
                                font,
                                Float.MAX_VALUE,
                                false,
                                cell.alignment()
                        );
                        widths[cell.column()] = Math.max(widths[cell.column()], segment.actualWidth());
                    }
                    segmentStart = index + 1;
                }
            }
        }
        return widths;
    }

    private static Alignment toTextAlignment(ColumnAlignment alignment) {
        return switch (alignment) {
            case RIGHT -> Alignment.RIGHT;
            case CENTER -> Alignment.CENTER;
            case NONE, LEFT -> Alignment.LEFT;
        };
    }

    private static float defaultContentHeight(FontUtil fontUtil, Font font) {
        return Math.max(1.0f, fontUtil.getTextDimension(" ", font).height());
    }

    private static float columnStart(float[] widths, int column, float padding) {
        float x = 0.0f;
        for (int index = 0; index < column; index++) {
            x += widths[index] + 2.0f * padding;
        }
        return x;
    }

    private static float tableWidth(float[] widths, float padding) {
        float width = 0.0f;
        for (float columnWidth : widths) {
            width += columnWidth + 2.0f * padding;
        }
        return width;
    }

    private static List<List<FragmentedText.Fragment>> translate(
            List<List<FragmentedText.Fragment>> fragments,
            float dx,
            float dy
    ) {
        return fragments.stream()
                .map(line -> line.stream()
                        .map(fragment -> new FragmentedText.Fragment(
                                fragment.x() + dx,
                                fragment.y() + dy,
                                fragment.w(),
                                fragment.h(),
                                fragment.baseLine(),
                                fragment.font(),
                                fragment.text()
                        ))
                        .toList())
                .toList();
    }

    private static List<GridLine> gridLines(
            float[] columnWidths,
            List<RowLayout> rows,
            float width,
            float height,
            float cellPadding
    ) {
        List<GridLine> lines = new ArrayList<>(columnWidths.length + rows.size() + 2);
        float x = 0.0f;
        lines.add(new GridLine(x, 0.0f, x, height));
        for (float columnWidth : columnWidths) {
            x += columnWidth + 2.0f * cellPadding;
            lines.add(new GridLine(x, 0.0f, x, height));
        }
        float y = 0.0f;
        lines.add(new GridLine(0.0f, y, width, y));
        for (RowLayout row : rows) {
            y += row.bounds().height();
            lines.add(new GridLine(0.0f, y, width, y));
        }
        return List.copyOf(lines);
    }

    private static @Nullable Integer integerAttribute(Map<String, @Nullable Object> attributes, String name) {
        Object value = attributes.get(name);
        return value instanceof Integer integer ? integer : null;
    }

    private static ColumnAlignment columnAlignment(@Nullable Object value) {
        if (value instanceof Enum<?> alignment) {
            return columnAlignment(alignment.name());
        }
        return value instanceof String alignment ? columnAlignment(alignment) : ColumnAlignment.NONE;
    }

    private static ColumnAlignment columnAlignment(String value) {
        return switch (value) {
            case "LEFT" -> ColumnAlignment.LEFT;
            case "CENTER" -> ColumnAlignment.CENTER;
            case "RIGHT" -> ColumnAlignment.RIGHT;
            default -> ColumnAlignment.NONE;
        };
    }

    private static Presentation presentation(RichText source, int tableId) {
        Color color = DEFAULT_BORDER_COLOR;
        float width = DEFAULT_BORDER_WIDTH;
        boolean colorFound = false;
        boolean widthFound = false;
        for (Run run : source) {
            if (!Integer.valueOf(tableId).equals(run.getAttributes().get(ATTRIBUTE_TABLE_ID))) {
                continue;
            }
            for (Style style : run.getStyles()) {
                if (!colorFound && style.get(STYLE_ATTRIBUTE_BORDER_COLOR) instanceof Color borderColor) {
                    color = borderColor;
                    colorFound = true;
                }
                if (!widthFound && style.get(STYLE_ATTRIBUTE_BORDER_WIDTH) instanceof Number number) {
                    float borderWidth = number.floatValue();
                    if (Float.isFinite(borderWidth) && borderWidth > 0.0f) {
                        width = borderWidth;
                        widthFound = true;
                    }
                }
            }
            if (colorFound && widthFound) {
                break;
            }
        }
        return new Presentation(color, width);
    }

    /**
     * Horizontal alignment supplied by a table producer.
     */
    public enum ColumnAlignment {
        /** No explicit alignment is available. */
        NONE,
        /** Leading alignment. */
        LEFT,
        /** Centered alignment. */
        CENTER,
        /** Trailing alignment. */
        RIGHT
    }

    /**
     * An inline-node value carrying a table model for toolkit-specific rendering.
     *
     * @param table the table
     */
    public record InlineTable(Table table) {}

    /**
     * Renderer-facing visual settings attached to a table's rich-text styles.
     *
     * @param borderColor the border {@link Color}
     * @param borderWidth the border width
     */
    public record Presentation(Color borderColor, float borderWidth) {}

    /**
     * A complete table and the source range it occupies.
     *
     * @param id the table ID
     * @param start the start of the table
     * @param end the end of the table
     * @param rows the rows of the table
     * @param presentation the presentation of the table
     */
    public record Table(int id, int start, int end, List<Row> rows, Presentation presentation) {}

    /**
     * A complete row and the source range it occupies.
     *
     * @param index the index of the row
     * @param start the start of the row
     * @param end the end of the row
     * @param header whether the row is a header
     * @param cells the cells of the row
     */
    public record Row(int index, int start, int end, boolean header, List<Cell> cells) {}

    /**
     * A cell's source range excludes its invisible tab delimiter.
     *
     * @param column the column of the cell
     * @param start the start of the cell
     * @param end the end of the cell
     * @param alignment the alignment of the cell
     * @param backgroundColor the background color of the cell
     * @param text the text of the cell
     */
    public record Cell(
            int column,
            int start,
            int end,
            ColumnAlignment alignment,
            @Nullable Color backgroundColor,
            RichText text
    ) {}

    /**
     * A table's positioned geometry and grid segments.
     *
     * @param table the table
     * @param bounds the bounds of the table
     * @param rows the rows of the table
     * @param gridLines the grid lines of the table
     */
    public record TableLayout(Table table, Rectangle2f bounds, List<RowLayout> rows, List<GridLine> gridLines) {}

    /**
     *  A positioned table row.
     *
     * @param row the row
     * @param bounds the bounds of the row
     * @param cells the cells of the row
     */
    public record RowLayout(Row row, Rectangle2f bounds, List<CellLayout> cells) {}

    /**
     * A positioned cell with absolute rich-text fragments and its resolved background color.
     *
     * @param cell the cell
     * @param bounds the bounds of the cell
     * @param contentBounds the bounds of the cell's content
     * @param fragments the fragments of the cell's content
     * @param backgroundColor the background color of the cell
     */
    public record CellLayout(
            Cell cell,
            Rectangle2f bounds,
            Rectangle2f contentBounds,
            List<List<FragmentedText.Fragment>> fragments,
            @Nullable Color backgroundColor
    ) {}

    /**
     * Table-local caret geometry.
     *
     * @param x caret x-coordinate
     * @param y caret y-coordinate
     * @param height caret height
     */
    public record Caret(float x, float y, float height) {}

    /**
     * A logical line segment in a table grid.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     */
    public record GridLine(float x1, float y1, float x2, float y2) {}

    private static final class TableAccumulator {
        private final int id;
        private final Map<Integer, RowAccumulator> rows = new TreeMap<>();
        private int start = Integer.MAX_VALUE;
        private int end;

        private TableAccumulator(int id) {
            this.id = id;
        }

        private void include(int position) {
            start = Math.min(start, position);
            end = Math.max(end, position + 1);
        }

        private Optional<Table> toTable(RichText source, Presentation presentation) {
            if (rows.values().stream().anyMatch(row -> !row.isComplete())) {
                return Optional.empty();
            }
            List<Row> completedRows = rows.values().stream()
                    .map(row -> row.toRow(source))
                    .flatMap(Optional::stream)
                    .toList();
            if (completedRows.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Table(id, start, end, completedRows, presentation));
        }
    }

    private static final class RowAccumulator {
        private final int index;
        private final Map<Integer, CellAccumulator> cells = new TreeMap<>();
        private int start = Integer.MAX_VALUE;
        private int end = -1;
        private int nextCellStart = -1;
        private boolean header;

        private RowAccumulator(int index) {
            this.index = index;
        }

        private void include(int position, boolean header) {
            if (nextCellStart < 0) {
                nextCellStart = position;
            }
            start = Math.min(start, position);
            this.header |= header;
        }

        private void addCell(int column, ColumnAlignment alignment, int start, int end, @Nullable Color backgroundColor) {
            cells.put(column, new CellAccumulator(column, start, end, alignment, backgroundColor));
        }

        private void complete(int end) {
            this.end = end;
        }

        private Optional<Row> toRow(RichText source) {
            if (end < 0 || cells.isEmpty()) {
                return Optional.empty();
            }
            List<Cell> completedCells = new ArrayList<>(cells.size());
            for (CellAccumulator cell : cells.values()) {
                if (cell.start < 0 || cell.end < cell.start) {
                    return Optional.empty();
                }
                completedCells.add(new Cell(
                        cell.column,
                        cell.start,
                        cell.end,
                        cell.alignment,
                        cell.backgroundColor,
                        source.subSequence(cell.start, cell.end)
                ));
            }
            return Optional.of(new Row(index, start, end, header, List.copyOf(completedCells)));
        }

        private boolean isComplete() {
            return end >= 0 && !cells.isEmpty();
        }
    }

    private record CellAccumulator(
            int column,
            int start,
            int end,
            ColumnAlignment alignment,
            @Nullable Color backgroundColor
    ) {}

    private record MeasuredCell(Cell cell, FragmentedText fragments, float contentHeight) {}
}
