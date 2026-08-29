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
                    Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, new InlineTable(table))
            );
            builder.push(style);
            builder.append('\uFFFC');
            builder.pop(style);
            position = table.end();
        }
        source.subSequence(position, source.length()).appendTo(builder);
        return builder.toRichText();
    }

    /**
     * Measures and positions every cell in a table.
     *
     * <p>When wrapping is enabled, columns are reduced proportionally to fit the available width. Otherwise the
     * table keeps its natural width and its caller may provide horizontal scrolling. Cell text is returned as
     * positioned fragments so toolkit renderers can use their normal rich-text paint path.
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
        float[] columnWidths = resolveColumnWidths(naturalColumnWidths, availableWidth, wrapText, cellPadding);
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

    private static float[] resolveColumnWidths(float[] naturalWidths, float availableWidth, boolean wrapText, float cellPadding) {
        float[] widths = naturalWidths.clone();
        if (!wrapText) {
            return widths;
        }

        float availableContentWidth = Math.max(widths.length, availableWidth - 2.0f * cellPadding * widths.length);
        float naturalWidth = 0.0f;
        for (float width : widths) {
            naturalWidth += width;
        }
        if (naturalWidth <= availableContentWidth) {
            return widths;
        }

        float ratio = availableContentWidth / naturalWidth;
        float assigned = 0.0f;
        for (int column = 0; column < widths.length - 1; column++) {
            widths[column] = Math.max(1.0f, widths[column] * ratio);
            assigned += widths[column];
        }
        widths[widths.length - 1] = Math.max(1.0f, availableContentWidth - assigned);
        return widths;
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
