package com.dua3.utility.fx.db;

import com.dua3.utility.fx.PlatformHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FxDbUtilTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    void testFillTableViewWithMockResultSet() throws Exception {
        record ColDef(String name, String label, JDBCType type, int scale) {}
        List<ColDef> columns = List.of(
                new ColDef("ID", "ID", JDBCType.INTEGER, 0),
                new ColDef("NAME", "Item Name", JDBCType.VARCHAR, 0),
                new ColDef("PRICE", "Price", JDBCType.DECIMAL, 2),
                new ColDef("AMOUNT", "Amount", JDBCType.NUMERIC, 0),
                new ColDef("FACTOR", "Factor", JDBCType.DOUBLE, 0),
                new ColDef("DATE_COL", "Date", JDBCType.DATE, 0),
                new ColDef("TIME_COL", "Time", JDBCType.TIME, 0),
                new ColDef("TS_COL", "Timestamp", JDBCType.TIMESTAMP, 0),
                new ColDef("CLOB_OK", "Clob OK", JDBCType.CLOB, 0),
                new ColDef("CLOB_ERR", "Clob Err", JDBCType.CLOB, 0)
        );

        Clob goodClob = (Clob) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Clob.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "length" -> 14L;
                    case "getSubString" -> "Good Clob Text";
                    default -> null;
                }
        );

        Clob badClob = (Clob) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Clob.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "length" -> throw new SQLException("Clob read error");
                    default -> null;
                }
        );

        List<List<Object>> rows = List.of(
                java.util.Arrays.asList(
                        1,
                        "Widget",
                        12.345,
                        100,
                        3.14159,
                        Date.valueOf("2023-05-15"),
                        Time.valueOf("14:30:00"),
                        Timestamp.valueOf("2023-05-15 14:30:00"),
                        goodClob,
                        badClob
                ),
                java.util.Arrays.asList(2, null, null, null, null, null, null, null, null, null)
        );

        ResultSetMetaData meta = (ResultSetMetaData) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSetMetaData.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("toString".equals(name)) {
                        return "MockMetaData";
                    }
                    if ("getColumnCount".equals(name)) {
                        return columns.size();
                    }
                    if (args != null && args.length > 0 && args[0] instanceof Integer colNum) {
                        int colIdx = colNum - 1;
                        return switch (name) {
                            case "getColumnName" -> columns.get(colIdx).name();
                            case "getColumnLabel" -> columns.get(colIdx).label();
                            case "getColumnType" -> columns.get(colIdx).type().getVendorTypeNumber();
                            case "getScale" -> columns.get(colIdx).scale();
                            default -> "";
                        };
                    }
                    return "";
                }
        );

        AtomicInteger currentRow = new AtomicInteger(-1);
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("toString".equals(name)) {
                        return "MockResultSet";
                    }
                    if ("getMetaData".equals(name)) {
                        return meta;
                    }
                    if ("next".equals(name)) {
                        int next = currentRow.incrementAndGet();
                        return next < rows.size();
                    }
                    if ("getObject".equals(name) && args != null && args.length > 0) {
                        int colIdx = ((Integer) args[0]) - 1;
                        return rows.get(currentRow.get()).get(colIdx);
                    }
                    return "";
                }
        );

        TableView<ObservableList<Object>> tv = new TableView<>();
        int count = FxDbUtil.fill(tv, rs);
        assertEquals(2, count);

        // Wait for JavaFX UI thread
        CountDownLatch latch = new CountDownLatch(1);
        PlatformHelper.runLater(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        assertEquals(10, tv.getColumns().size());
        assertEquals(2, tv.getItems().size());

        ObservableList<Object> row1 = tv.getItems().get(0);
        assertEquals(1, row1.get(0));
        assertEquals("Widget", row1.get(1));
        assertEquals("Good Clob Text", row1.get(8));
        assertEquals("###", row1.get(9));

        // Test cell factories & cellValueFactories
        for (int i = 0; i < tv.getColumns().size(); i++) {
            @SuppressWarnings("unchecked")
            TableColumn<ObservableList<Object>, Object> col = (TableColumn<ObservableList<Object>, Object>) tv.getColumns().get(i);
            assertEquals(columns.get(i).label(), col.getText());

            var cellVal = col.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(tv, col, row1));
            assertNotNull(cellVal);

            TableCell<ObservableList<Object>, Object> cell = col.getCellFactory().call(col);
            callUpdateItem(cell, cellVal.getValue(), false);
            assertNotNull(cell.getText());

            // Empty rendering
            callUpdateItem(cell, null, true);
            assertNull(cell.getText());
        }
    }

    private static void callUpdateItem(TableCell<ObservableList<Object>, Object> cell, Object item, boolean empty) throws Exception {
        Method method = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
        method.setAccessible(true);
        method.invoke(cell, item, empty);
    }
}
