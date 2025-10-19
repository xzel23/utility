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

package com.dua3.utility.fx.db;

import com.dua3.utility.db.DbUtil;
import com.dua3.utility.fx.PlatformHelper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.sql.Clob;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * The {@code FxDbUtil} class provides utility methods for populating a JavaFX {@link TableView} with data from a ResultSet.
 * It also includes methods for converting and formatting data to be displayed in the TableView.
 */
public final class FxDbUtil {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger(FxDbUtil.class);
    private static final String ERROR_TEXT = "###";

    // utility - no instances
    private FxDbUtil() {}

    /**
     * Fill TableView instance with data from {@link ResultSet}.
     * <p>
     * All columns will be removed and recreated based on the ResultSet's metadata.
     *
     * @param tv the TableView
     * @param rs the ResultSet
     * @return the number of rows read
     * @throws SQLException if an error occurs while reading from the ResultSet.
     */
    public static int fill(TableView<ObservableList<Object>> tv, ResultSet rs) throws SQLException {
        LOG.debug("populating TableView with ResultSet data");
        ObservableList<TableColumn<ObservableList<Object>, ?>> columns = tv.getColumns();
        var items = tv.getItems();

        LOG.trace("clearing tableview contents ...");
        Platform.runLater(items::clear);

        List<TableColumn<ObservableList<Object>, ?>> newColumns = new ArrayList<>();
        List<ObservableList<Object>> newItems = new ArrayList<>();

        Locale locale = Locale.getDefault();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
        DateTimeFormatter timestampFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

        // read result metadata
        LOG.trace("reading result meta data ...");
        ResultSetMetaData meta = rs.getMetaData();
        int nColumns = meta.getColumnCount();
        for (int i = 1; i <= nColumns; i++) {
            final int idx = i - 1;

            String label = meta.getColumnLabel(i);
            String name = meta.getColumnName(i);
            JDBCType sqlType = JDBCType.valueOf(meta.getColumnType(i));
            int scale = meta.getScale(i);

            // define the formatting
            Function<Object, String> format;
            switch (sqlType) {
                case DATE -> //noinspection DataFlowIssue - format is not called for null arguments
                        format = item -> DbUtil.toLocalDate(item).format(dateFormatter);
                case TIMESTAMP -> //noinspection DataFlowIssue - format is not called for null arguments
                        format = item -> DbUtil.toLocalDateTime(item).format(timestampFormatter);
                case TIME ->  //noinspection DataFlowIssue - format is not called for null arguments
                        format = item -> DbUtil.toLocalDateTime(item).format(timeFormatter);

                // numbers that have scale
                case DECIMAL, NUMERIC -> {
                    if (scale > 0) {
                        //noinspection StringConcatenationInFormatCall,StringConcatenationMissingWhitespace
                        format = item -> String.format(
                                locale,
                                "%.0" + scale + "f",
                                ((Number) item).doubleValue());
                    } else {
                        format = String::valueOf;
                    }
                }

                // numbers that do not have scale
                case DOUBLE, REAL, FLOAT -> format = String::valueOf;
                default -> format = String::valueOf;
            }
            LOG.trace("column name: {} label: {} type: {} scale: {}", name, label, sqlType, scale);

            // CellValueFactory
            Callback<CellDataFeatures<ObservableList<Object>, Object>, ObservableValue<@Nullable Object>> cellValueFactory
                    = param -> {
                var list = param.getValue();
                Object x = idx < list.size() ? list.get(idx) : null;
                return new ReadOnlyObjectWrapper<>(x);
            };

            // CellFactory
            Callback<TableColumn<ObservableList<Object>, Object>, TableCell<ObservableList<Object>, Object>> cellFactory
                    = col -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(format.apply(item));
                    }
                }
            };

            // create column
            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(label);
            column.setCellValueFactory(cellValueFactory);
            column.setCellFactory(cellFactory);
            newColumns.add(column);
        }

        // read result
        LOG.trace("reading result data ...");
        while (rs.next()) {
            var list = FXCollections.observableArrayList();
            for (int i = 1; i <= nColumns; i++) {
                list.add(getObject(rs, i));
            }
            newItems.add(list);
        }
        LOG.trace("read {} rows of data", newItems.size());

        LOG.trace("setting rows");
        PlatformHelper.runLater(() -> {
            columns.setAll(newColumns);
            items.setAll(newItems);
        });

        return newItems.size();
    }

    private static Object getObject(ResultSet rs, int i) throws SQLException {
        Object obj = rs.getObject(i);

        if (obj instanceof Clob clob) {
            obj = toString(clob);
        }

        return obj;
    }

    private static String toString(Clob clob) {
        try {
            return clob.getSubString(1L, Math.toIntExact(Math.min(Integer.MAX_VALUE, clob.length())));
        } catch (SQLException e) {
            LOG.warn("could not convert Clob to String", e);
            return ERROR_TEXT;
        }
    }

}
