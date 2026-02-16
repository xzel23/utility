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
import javafx.util.StringConverter;

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
        tv.getColumns().setAll(
                columns.stream().map(cd -> {
                    TableColumn<S, Object> tc = new TableColumn<>(cd.header());
                    switch (cd) {
                        case ColumnDefText cdt -> tc.setCellFactory(TableCellAutoCommit.forTableColumn(cdt.converter()));
                        case ColumnDefGeneric cdg -> tc.setCellFactory(new GenericTableCellFactory<>(cdg.nodeFactory(), cdg.startEdit(), cdg.cancelEdit()));
                    }
                    tc.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(cd.get(f.getValue())));
                    tc.setOnEditCommit(event -> ((ColumnDef) cd).set(event.getRowValue(), event.getNewValue()));
                    tc.setEditable(cd.editable());
                    editable[0] = editable[0] || cd.editable();
                    return tc;
                }).toList()
        );
        tv.setEditable(editable[0]);
        return tv;
    }
}
