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
import com.dua3.utility.lang.LangUtil;
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

    /**
     * Utility class constructor.
     */
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
        LOG.trace("populating TableView with ResultSet data");
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
                protected void updateItem(@Nullable Object item, boolean empty) {
                    //noinspection DataFlowIssue - false positive, super class method allows null
                    super.updateItem(item, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(getFormat(sqlType, dateFormatter, timestampFormatter, timeFormatter, scale, locale).apply(item));
                    }
                }
            };

            // create column
            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(label);
            //noinspection NullableProblems - false positive
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

    /**
     * Returns a Function that formats an Object value based on the given JDBCType.
     *
     * @param sqlType the JDBCType to determine the formatting behavior
     * @param dateFormatter the DateTimeFormatter for DATE type
     * @param timestampFormatter the DateTimeFormatter for TIMESTAMP type
     * @param timeFormatter the DateTimeFormatter for TIME type
     * @param scale the scale for decimal and numeric types
     * @param locale the Locale to use for formatting
     * @return a Function to format the input Object based on the provided JDBCType
     */
    @SuppressWarnings("java:S3457")
    private static Function<@Nullable Object, String> getFormat(JDBCType sqlType, DateTimeFormatter dateFormatter, DateTimeFormatter timestampFormatter, DateTimeFormatter timeFormatter, int scale, Locale locale) {
        return switch (sqlType) {
            case DATE ->
                    item -> LangUtil.mapNonNullOrElse(DbUtil.toLocalDate(item), v -> v.format(dateFormatter), LangUtil.NULL_STRING);
            case TIMESTAMP ->
                    item -> LangUtil.mapNonNullOrElse(DbUtil.toLocalDateTime(item), v -> v.format(timestampFormatter), LangUtil.NULL_STRING);
            case TIME ->
                    item -> LangUtil.mapNonNullOrElse(DbUtil.toLocalTime(item), v -> v.format(timeFormatter), LangUtil.NULL_STRING);

            // numbers that have scale
            case DECIMAL, NUMERIC -> {
                if (scale > 0) {
                    //noinspection StringConcatenationInFormatCall,StringConcatenationMissingWhitespace
                    yield item -> item == null ? LangUtil.NULL_STRING : String.format(locale, "%.0" + scale + "f", ((Number) item).doubleValue());
                } else {
                    yield String::valueOf;
                }
            }

            // numbers that do not have scale
            case DOUBLE, REAL, FLOAT -> String::valueOf;
            default -> String::valueOf;
        };
    }

    /**
     * Retrieves an object from the specified ResultSet at the given index.
     * If the retrieved object is an instance of Clob, it is converted to a String.
     *
     * @param rs the ResultSet from which to retrieve the object
     * @param i the index of the object to retrieve
     * @return the retrieved object or its String representation in case of a Clob
     * @throws SQLException if an error occurs during object retrieval
     */
    private static @Nullable Object getObject(ResultSet rs, int i) throws SQLException {
        Object obj = rs.getObject(i);

        if (obj instanceof Clob clob) {
            obj = toString(clob);
        }

        return obj;
    }

    /**
     * Retrieves the content of a Clob object as a String.
     *
     * @param clob the Clob object to extract the content from
     * @return the content of the Clob object as a String
     */
    private static String toString(Clob clob) {
        try {
            return clob.getSubString(1L, Math.toIntExact(Math.min(Integer.MAX_VALUE, clob.length())));
        } catch (SQLException e) {
            LOG.warn("could not convert Clob to String", e);
            return ERROR_TEXT;
        }
    }

}
