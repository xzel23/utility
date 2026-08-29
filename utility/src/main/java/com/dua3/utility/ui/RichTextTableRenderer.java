package com.dua3.utility.ui;

import com.dua3.utility.data.Color;

/**
 * Renders geometry created by {@link RichTextTableHelper} using a toolkit-neutral graphics target.
 */
public final class RichTextTableRenderer {

    private RichTextTableRenderer() {
        // utility class
    }

    /**
     * Paints cell backgrounds, grid lines, and rich cell contents in that order.
     *
     * @param graphics graphics target
     * @param layout positioned table geometry
     */
    public static void render(Graphics graphics, RichTextTableHelper.TableLayout layout) {
        for (RichTextTableHelper.RowLayout row : layout.rows()) {
            for (RichTextTableHelper.CellLayout cell : row.cells()) {
                Color background = cell.backgroundColor();
                if (background != null) {
                    graphics.setFill(background);
                    graphics.fillRect(cell.bounds());
                }
            }
        }

        RichTextTableHelper.Presentation presentation = layout.table().presentation();
        graphics.setStroke(presentation.borderColor(), presentation.borderWidth());
        for (RichTextTableHelper.GridLine line : layout.gridLines()) {
            graphics.strokeLine(line.x1(), line.y1(), line.x2(), line.y2());
        }

        for (RichTextTableHelper.RowLayout row : layout.rows()) {
            for (RichTextTableHelper.CellLayout cell : row.cells()) {
                RichTextRenderer.renderFragmentLines(graphics, cell.fragments());
            }
        }
    }
}
