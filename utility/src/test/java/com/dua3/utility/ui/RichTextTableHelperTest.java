package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextTableHelperTest {

    private static final Style TABLE_THEME = Style.create("table-theme", Map.of(
            RichTextTableHelper.STYLE_ATTRIBUTE_BORDER_COLOR, Color.DARKCYAN,
            RichTextTableHelper.STYLE_ATTRIBUTE_BORDER_WIDTH, 2.5f
    ));

    @Test
    void extractsRowsCellsRangesAlignmentAndPresentation() {
        RichText text = tableText();

        List<RichTextTableHelper.Table> tables = RichTextTableHelper.tables(text);

        assertEquals(1, tables.size());
        RichTextTableHelper.Table table = tables.getFirst();
        assertEquals(7, table.id());
        assertEquals(0, table.start());
        assertEquals(text.length(), table.end());
        assertEquals(new RichTextTableHelper.Presentation(Color.DARKCYAN, 2.5f), table.presentation());

        assertEquals(3, table.rows().size());
        RichTextTableHelper.Row header = table.rows().getFirst();
        assertTrue(header.header());
        assertEquals(2, header.cells().size());
        assertEquals("Title", header.cells().getFirst().text().toString());
        assertEquals(RichTextTableHelper.ColumnAlignment.LEFT, header.cells().getFirst().alignment());
        assertEquals("Value", header.cells().get(1).text().toString());
        assertEquals(RichTextTableHelper.ColumnAlignment.RIGHT, header.cells().get(1).alignment());

        RichTextTableHelper.Row secondBodyRow = table.rows().get(2);
        assertEquals(2, secondBodyRow.index());
        assertEquals("second", secondBodyRow.cells().getFirst().text().toString());
        assertEquals(RichTextTableHelper.ColumnAlignment.CENTER, secondBodyRow.cells().getFirst().alignment());
    }

    @Test
    void ignoresIncompleteTableFragments() {
        RichTextBuilder builder = new RichTextBuilder();
        appendCell(builder, 9, 0, true, 0, "LEFT", "incomplete");

        assertTrue(RichTextTableHelper.tables(builder.toRichText()).isEmpty());
    }

    private static RichText tableText() {
        RichTextBuilder builder = new RichTextBuilder();
        appendCell(builder, 7, 0, true, 0, "LEFT", "Title");
        appendCell(builder, 7, 0, true, 1, "RIGHT", "Value");
        appendRowEnd(builder, 7, 0, true);
        appendCell(builder, 7, 1, false, 0, "LEFT", "first");
        appendCell(builder, 7, 1, false, 1, "RIGHT", "one");
        appendRowEnd(builder, 7, 1, false);
        appendCell(builder, 7, 2, false, 0, "CENTER", "second");
        appendCell(builder, 7, 2, false, 1, "RIGHT", "two");
        appendRowEnd(builder, 7, 2, false);
        return builder.toRichText();
    }

    private static void appendCell(
            RichTextBuilder builder,
            int tableId,
            int row,
            boolean header,
            int column,
            String alignment,
            String value
    ) {
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ID, tableId);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ROW, row);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER, header);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN, column);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN_ALIGNMENT, alignment);
        builder.push(TABLE_THEME);
        builder.append(value).append('\t');
        builder.pop(TABLE_THEME);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN_ALIGNMENT);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_COLUMN);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ROW);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ID);
    }

    private static void appendRowEnd(RichTextBuilder builder, int tableId, int row, boolean header) {
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ID, tableId);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_ROW, row);
        builder.push(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER, header);
        builder.append('\n');
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_HEADER);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ROW);
        builder.pop(RichTextTableHelper.ATTRIBUTE_TABLE_ID);
    }
}
