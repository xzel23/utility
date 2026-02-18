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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Map;
import java.util.SequencedCollection;

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
     *
     * @return A configured {@link TableView} instance containing the defined columns.
     */
    public static <S> TableView<S> newTableView(SequencedCollection<ColumnDef<S, ?>> columns) {
        return newTableView(columns, FXCollections.observableArrayList());
    }

    /**
     * Creates a new {@link TableView} instance configured with the specified columns and initial items.
     *
     * @param <S>          The type of the items to be displayed in the {@link TableView}.
     * @param columns      A {@link SequencedCollection} of {@link ColumnDefText} that defines the columns of the {@link TableView}.
     *                     Each {@link ColumnDefText} includes information such as the column header, value provider, and cell converter.
     * @param initialItems A {@link SequencedCollection} containing the initial items to be displayed in the {@link TableView}.
     *
     * @return A configured {@link TableView} instance containing the defined columns and initial items.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> TableView<S> newTableView(SequencedCollection<ColumnDef<S, ?>> columns, SequencedCollection<S> initialItems) {
        ObservableList<S> items = FXCollections.observableArrayList(initialItems);
        TableView<S> tv = new TableView<>(items);
        boolean[] editable = {false};
        Map<TableColumn<?, ?>, ColumnDef<?, ?>> configMap = new HashMap<>();
        tv.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tv.getColumns().setAll(
                columns.stream().map(cd -> {
                    TableColumn<S, Object> tc = new TableColumn<>(cd.text());
                    cd.graphic().ifPresent(tc::setGraphic);
                    switch (cd) {
                        case ColumnDefText cdt -> tc.setCellFactory(TableCellAutoCommit.forTableColumn(cdt.converter()));
                        case ColumnDefGeneric cdg -> tc.setCellFactory(new GenericTableCellFactory<>(cdg.nodeFactory(), cdg.startEdit(), cdg.cancelEdit()));
                    }
                    tc.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(cd.get(f.getValue())));
                    tc.setOnEditCommit(event -> ((ColumnDef) cd).set(event.getRowValue(), event.getNewValue()));
                    tc.setEditable(cd.editable());
                    tc.setMinWidth(cd.minWidth());
                    tc.setMaxWidth(cd.maxWidth());
                    tc.setResizable(cd.resizable());
                    editable[0] = editable[0] || cd.editable();

                    configMap.put(tc, cd);

                    return tc;
                }).toList()
        );
        tv.setEditable(editable[0]);

        FlexibleColumnCoordinator.apply(tv, configMap);

        return tv;
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
}
