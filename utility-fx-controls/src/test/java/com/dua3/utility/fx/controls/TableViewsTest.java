package com.dua3.utility.fx.controls;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.util.converter.DefaultStringConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class TableViewsTest extends FxTestBase {

    @Test
    void insertRowCreatesItemUsingConfiguredFactory() throws Exception {
        runOnFxThreadAndWait(() -> {
            TableView<String> tableView = TableViews.newTableView(
                    List.of(ColumnDef.<String, String>builder("Value", new DefaultStringConverter())
                            .valueGetter(value -> value)
                            .build()),
                    TableViewOptions.of(TableViewOptions.ALLOW_INSERTING_ROWS(() -> "new row"))
            );

            MenuItem insertRow = tableView.getContextMenu().getItems().getFirst();
            insertRow.fire();

            assertEquals(List.of("new row"), tableView.getItems());
            assertNotNull(tableView.getItems().getFirst());
        });
    }

    @Test
    void columnWidthsUseContentUntilTheyNeedToShrinkByWeight() {
        List<Double> widths = TableViews.allocateColumnWidths(
                400.0,
                List.of(
                        new TableViews.ColumnWidth(120.0, 50.0, Double.MAX_VALUE, 0.5, true),
                        new TableViews.ColumnWidth(180.0, 50.0, Double.MAX_VALUE, 0.5, true)
                )
        );

        assertEquals(List.of(120.0, 180.0), widths);

        widths = TableViews.allocateColumnWidths(
                200.0,
                List.of(
                        new TableViews.ColumnWidth(180.0, 50.0, Double.MAX_VALUE, 0.5, true),
                        new TableViews.ColumnWidth(120.0, 50.0, Double.MAX_VALUE, 0.5, true)
                )
        );

        assertEquals(List.of(130.0, 70.0), widths);
    }

    @Test
    void columnWidthsKeepFixedColumnsAndMinimumWidths() {
        List<Double> widths = TableViews.allocateColumnWidths(
                150.0,
                List.of(
                        new TableViews.ColumnWidth(40.0, 0.0, 40.0, 0.0, false),
                        new TableViews.ColumnWidth(100.0, 50.0, Double.MAX_VALUE, 0.5, true),
                        new TableViews.ColumnWidth(100.0, 50.0, Double.MAX_VALUE, 0.5, true)
                )
        );

        assertEquals(List.of(40.0, 55.0, 55.0), widths);
    }
}
