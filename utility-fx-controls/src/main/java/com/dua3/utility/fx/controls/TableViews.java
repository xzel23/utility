// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.function.Supplier;

/**
 * Utility class providing methods for manipulating TableView instances.
 */
public final class TableViews {

    // utility - no instances
    private TableViews() {}

    /**
     * Clear TableView.
     * <p>
     * Clears both items and columns of the TableView instance.
     *
     * @param tv  the TableView
     * @param <T> the TableView type parameter
     */
    public static <T> void clear(TableView<T> tv) {
        Platform.runLater(() -> {
            tv.getItems().clear();
            tv.getColumns().clear();
        });
    }

    /**
     * Creates a new {@link TableView} instance configured with the specified columns.
     *
     * @param <S>          The type of the items to be displayed in the {@link TableView}.
     * @param columns      A {@link SequencedCollection} of {@link ColumnDefText} that defines the columns of the {@link TableView}.
     *                     Each {@link ColumnDefText} includes information such as the column header, value provider, and cell converter.
     * @param options       A {@link TableViewOptions} instance specifying configuration options.
     *
     * @return A configured {@link TableView} instance containing the defined columns.
     */
    public static <S> TableView<S> newTableView(SequencedCollection<ColumnDef<S, ?>> columns, TableViewOptions options) {
        return newTableView(columns, options, FXCollections.observableArrayList());
    }

    /**
     * Creates a new {@link TableView} instance configured with the specified columns, initial items,
     * and optional row-draggability behavior.
     *
     * @param <S>           The type of the items to be displayed in the {@link TableView}.
     * @param columns       A {@link SequencedCollection} of {@link ColumnDef} defining the table columns.
     *                      Each {@link ColumnDef} contains information such as header, value provider,
     *                      cell properties, and customization options.
     * @param options       A {@link TableViewOptions} instance specifying configuration options.
     * @param initialItems  A {@link SequencedCollection} containing the initial items to be displayed in the {@link TableView}.
     * @return A configured {@link TableView} instance containing the specified columns, initial items,
     *         and row-draggability behavior (if enabled).
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <S> TableView<S> newTableView(
            SequencedCollection<? extends ColumnDef<S, ?>> columns,
            TableViewOptions options,
            SequencedCollection<? extends S> initialItems) {
        boolean editable = options.isEnabled(TableViewOptions.EDITABLE_OPTION);
        boolean sortable = options.isEnabled(TableViewOptions.SORTABLE_OPTION);
        boolean reorderableColumns = options.isEnabled(TableViewOptions.REORDERABLE_COLUMNS_OPTION);
        boolean reorderableRows = options.isEnabled(TableViewOptions.REORDERABLE_ROWS_OPTION);
        boolean multiSelection = options.isEnabled(TableViewOptions.MULTIPLE_ROWS_SELECTABLE_OPTION);
        boolean allowDeletingRows = options.isEnabled(TableViewOptions.ALLOW_DELETING_ROWS_OPTION);
        boolean allowInsertingRows = options.isEnabled(TableViewOptions.ALLOW_INSERTING_ROWS_OPTION);

        ObservableList<S> items = FXCollections.observableArrayList(initialItems);
        TableView<S> tv = new TableView<>(items);
        Map<TableColumn<?, ?>, ColumnDef<?, ?>> configMap = new HashMap<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tv.getColumns().setAll(
                columns.stream().map(cd -> {
                    TableColumn<S, Object> tc = new TableColumn<>(cd.text());
                    cd.graphic().ifPresent(tc::setGraphic);
                    switch (cd) {
                        case ColumnDefText cdt ->
                                tc.setCellFactory(TableCellAutoCommit.forTableColumn(cdt.converter()));
                        case ColumnDefGeneric cdg ->
                                tc.setCellFactory(new GenericTableCellFactory<>(cdg.nodeFactory(), cdg.startEdit(), cdg.cancelEdit()));
                    }
                    tc.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(cd.get(f.getValue())));
                    tc.setOnEditCommit(event -> ((ColumnDef) cd).set(event.getRowValue(), event.getNewValue()));
                    tc.setEditable(editable && cd.editable());
                    tc.setSortable(sortable && cd.sortable());
                    tc.setReorderable(reorderableColumns && cd.reorderable());
                    tc.setMinWidth(cd.minWidth());
                    tc.setMaxWidth(cd.maxWidth());
                    tc.setResizable(cd.resizable());

                    configMap.put(tc, cd);

                    return tc;
                }).toList()
        );

        tv.setEditable(editable);
        tv.getSelectionModel().setSelectionMode(multiSelection ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);

        FlexibleColumnCoordinator.apply(tv, configMap);

        if (reorderableRows || allowDeletingRows || allowInsertingRows) {
            Supplier<S> itemFactory = (Supplier<S>) options.getParams(TableViewOptions.ALLOW_INSERTING_ROWS_OPTION).get(TableViewOptions.ITEM_FACTORY);
            tv.setRowFactory(new CustomRowFactory<>(reorderableRows, allowDeletingRows, allowInsertingRows, itemFactory));
        }

        if (allowInsertingRows) {
            ContextMenu tableMenu = new ContextMenu();
            MenuItem insertItem = new MenuItem("Insert row");
            insertItem.setOnAction(evt -> tv.getItems().add(null));
            tableMenu.getItems().add(insertItem);
            tv.setContextMenu(tableMenu);
        }

        return tv;
    }

    /**
     * A custom implementation of the {@link Callback} interface for creating and managing {@link TableRow} instances
     * within a {@link TableView}. This class provides customizable behavior for row interactions such as row reordering,
     * deleting, and inserting.
     *
     * @param <S> The type of the items contained in the {@link TableView}.
     */
    private static final class CustomRowFactory<S> implements Callback<TableView<S>, TableRow<S>> {
        private final boolean reorderableRows;
        private final boolean allowDeletingRows;
        private final boolean allowInsertingRows;
        private final @Nullable Supplier<S> itemFactory;

        CustomRowFactory(boolean reorderableRows, boolean allowDeletingRows, boolean allowInsertingRows, @Nullable Supplier<S> itemFactory) {
            this.reorderableRows = reorderableRows;
            this.allowDeletingRows = allowDeletingRows;
            this.allowInsertingRows = allowInsertingRows;
            this.itemFactory = itemFactory;
        }

        @Override
        public TableRow<S> call(TableView<S> tv) {
            TableRow<S> row = reorderableRows ? new DraggableRow<>() : new TableRow<>();

            if (allowDeletingRows || allowInsertingRows) {
                ContextMenu rowMenu = new ContextMenu();

                if (allowDeletingRows) {
                    MenuItem deleteItem = new MenuItem();
                    deleteItem.setOnAction(evt -> {
                        List<Integer> selectedIndices = new ArrayList<>(tv.getSelectionModel().getSelectedIndices());
                        int count = selectedIndices.size();
                        if (count == 0) return;

                        String title = "Delete row" + (count > 1 ? "s" : "");
                        String header = "Delete " + (count > 1 ? "selected rows" : "row") + "?";
                        String text = "Do you really want to delete the selected " + (count > 1 ? "rows" : "row") + "?";
                        Dialogs.alert(tv.getScene().getWindow(), AlertType.CONFIRMATION)
                                .title(title)
                                .header(header)
                                .text(text)
                                .showAndWait()
                                .filter(bt -> bt == ButtonType.OK)
                                .ifPresent(bt -> {
                                    selectedIndices.sort(Comparator.reverseOrder());
                                    for (int index : selectedIndices) {
                                        tv.getItems().remove(index);
                                    }
                                });
                    });
                    rowMenu.setOnShowing(evt -> {
                        int count = tv.getSelectionModel().getSelectedItems().size();
                        deleteItem.setText(count > 1 ? "Delete rows" : "Delete row");
                    });
                    rowMenu.getItems().add(deleteItem);
                }

                if (allowInsertingRows) {
                    assert itemFactory != null : "internal error: itemFactory must not be null when inserting rows is enabled";

                    if (allowDeletingRows) {
                        rowMenu.getItems().add(new SeparatorMenuItem());
                    }

                    MenuItem insertAbove = new MenuItem("Insert row above");
                    insertAbove.setOnAction(evt -> {
                        int index = row.getIndex();
                        tv.getItems().add(index, itemFactory.get());
                    });

                    MenuItem insertBelow = new MenuItem("Insert row below");
                    insertBelow.setOnAction(evt -> {
                        int index = row.getIndex();
                        tv.getItems().add(index + 1, itemFactory.get());
                    });

                    rowMenu.getItems().addAll(insertAbove, insertBelow);
                }

                row.contextMenuProperty().bind(
                        Bindings.when(row.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(rowMenu)
                );
            }

            return row;
        }
    }

    /**
     * Manages a {@link TableView} to ensure column widths respect predefined constraints
     * and dynamically adjust based on the table's width and column weights.
     * <p>
     * This utility allows the distribution of column widths in a way that accounts
     * for both fixed-width columns and resizable columns with weight-based proportions.
     */
    private static final class FlexibleColumnCoordinator {
        private final TableView<?> table;
        private final Map<TableColumn<?, ?>, ColumnDef<?, ?>> configMap;
        private boolean isInternalAdjusting = false;

        /**
         * Applies the configuration map to the specified {@link TableView} by instantiating
         * a {@link FlexibleColumnCoordinator}, which is responsible for managing the
         * behavior and layout of the table's columns based on the provided configuration.
         *
         * @param table The {@link TableView} to which the column configurations will be applied.
         *              This parameter must not be {@code null} and should contain all the columns
         *              listed in the configuration map.
         * @param configMap A map of {@link TableColumn} to {@link ColumnDef}, where each entry
         *                  defines the behavior and properties of a specific column in the {@link TableView}.
         *                  The key represents a table column, and the value contains its corresponding
         *                  configuration details. This parameter must not be {@code null}.
         */
        public static void apply(TableView<?> table, Map<TableColumn<?, ?>, ColumnDef<?, ?>> configMap) {
            new FlexibleColumnCoordinator(table, configMap);
        }

        private FlexibleColumnCoordinator(TableView<?> table, Map<TableColumn<?, ?>, ColumnDef<?, ?>> configMap) {
            this.table = table;
            this.configMap = configMap;

            // Initial setup for each column
            configMap.forEach((column, constraints) -> {
                column.setResizable(constraints.resizable());
                column.setMinWidth(constraints.minWidth());
                column.setMaxWidth(constraints.maxWidth());

                // Listen for manual resizing to update weights dynamically
                column.widthProperty().addListener((obs, oldW, newW) -> {
                    if (!isInternalAdjusting && constraints.resizable()) {
                        recalculateWeights();
                    }
                });
            });

            // Listen for table width changes
            table.widthProperty().addListener((obs, oldW, newW) -> adjustAll());
        }

        private void recalculateWeights() {
            double resizableSpace = 0;

            // 1. Find the new total space occupied by all resizable columns
            for (var entry : configMap.entrySet()) {
                if (entry.getValue().resizable()) {
                    resizableSpace += entry.getKey().getWidth();
                }
            }

            // 2. Redistribute weights based on the new actual pixel widths
            if (resizableSpace > 0) {
                for (var entry : configMap.entrySet()) {
                    if (entry.getValue().resizable()) {
                        // Update weight to reflect the current percentage of the resizable area
                        entry.getValue().setWeight(entry.getKey().getWidth() / resizableSpace);
                    }
                }
            }
        }

        private void adjustAll() {
            if (isInternalAdjusting || table.getWidth() <= 0) return;
            isInternalAdjusting = true;

            double totalWidth = table.getWidth() - 2.0; // Buffer for borders
            double fixedSpace = 0;
            double totalWeight = 0;

            // First pass: Calculate space occupied by non-resizable columns
            for (var entry : configMap.entrySet()) {
                if (!entry.getValue().resizable()) {
                    fixedSpace += entry.getKey().getWidth();
                } else {
                    totalWeight += entry.getValue().weight();
                }
            }

            double availableSpace = totalWidth - fixedSpace;

            // Second pass: Distribute remaining space by weight
            if (availableSpace > 0 && totalWeight > 0) {
                for (var entry : configMap.entrySet()) {
                    if (entry.getValue().resizable()) {
                        double share = (entry.getValue().weight() / totalWeight) * availableSpace;
                        // Clamp between min and max
                        double finalWidth = Math.clamp(share, entry.getValue().minWidth(), entry.getValue().maxWidth());
                        entry.getKey().setPrefWidth(finalWidth);
                    }
                }
            }

            isInternalAdjusting = false;
        }
    }

    /**
     * Represents a draggable row within a {@link TableView}, enabling drag-and-drop functionality
     * for reordering rows. This class extends {@link TableRow} and adds event handling
     * for drag initiation, drag over, and drop events to achieve row rearrangement.
     *
     * @param <T> The type of the items contained in the {@link TableView}.
     */
    private static final class DraggableRow<T> extends TableRow<T> {
        private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

        DraggableRow() {
            // 1. Initiate Drag
            this.setOnDragDetected(event -> {
                if (!isEmpty()) {
                    Integer index = getIndex();
                    Dragboard db = startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(snapshot(null, null));

                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            // 2. Handle Drag Over
            this.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE) && getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });

            // 3. Handle Drop and List Mutation
            this.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    TableView<T> tableView = getTableView();

                    T draggedItem = tableView.getItems().remove(draggedIndex);

                    int dropIndex = isEmpty() ? tableView.getItems().size() : getIndex();

                    tableView.getItems().add(dropIndex, draggedItem);
                    tableView.getSelectionModel().select(dropIndex);

                    event.setDropCompleted(true);
                    event.consume();
                }
            });
        }
    }
}
