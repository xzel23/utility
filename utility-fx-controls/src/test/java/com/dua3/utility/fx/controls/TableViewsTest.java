package com.dua3.utility.fx.controls;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.util.converter.DefaultStringConverter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
