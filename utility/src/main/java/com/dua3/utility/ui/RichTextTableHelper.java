package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
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
                    row.addCell(columnIndex, columnAlignment(attributes.get(ATTRIBUTE_TABLE_COLUMN_ALIGNMENT)), row.nextCellStart, index);
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
     * @param start
     * @param end
     * @param rows
     * @param presentation
     */
    public record Table(int id, int start, int end, List<Row> rows, Presentation presentation) {}

    /**
     * A complete row and the source range it occupies.
     *
     * @param index
     * @param start
     * @param end
     * @param header
     * @param cells
     */
    public record Row(int index, int start, int end, boolean header, List<Cell> cells) {}

    /**
     * A cell's source range excludes its invisible tab delimiter.
     *
     * @param column
     * @param start
     * @param end
     * @param alignment
     * @param text
     */
    public record Cell(int column, int start, int end, ColumnAlignment alignment, RichText text) {}

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

        private void addCell(int column, ColumnAlignment alignment, int start, int end) {
            cells.put(column, new CellAccumulator(column, start, end, alignment));
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
                completedCells.add(new Cell(cell.column, cell.start, cell.end, cell.alignment, source.subSequence(cell.start, cell.end)));
            }
            return Optional.of(new Row(index, start, end, header, List.copyOf(completedCells)));
        }

        private boolean isComplete() {
            return end >= 0 && !cells.isEmpty();
        }
    }

    private record CellAccumulator(int column, int start, int end, ColumnAlignment alignment) {}
}
