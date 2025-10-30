// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for DbUtil.
 */
@Execution(ExecutionMode.SAME_THREAD) // all tests use the same Database instance
class DbUtilTest {

    private static class RecordingCloseable implements AutoCloseable {
        private final String name;
        private final java.util.List<String> log;
        private final ResultSet rs;
        private boolean closed = false;
        RecordingCloseable(String name, java.util.List<String> log, ResultSet rs) {
            this.name = name;
            this.log = log;
            this.rs = rs;
        }
        @Override
        public void close() throws Exception {
            // when this closeable is closed, the ResultSet must not yet be closed
            if (rs != null) {
                try {
                    if (rs.isClosed()) {
                        throw new IllegalStateException("ResultSet was closed before closeable '"+name+"'");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            log.add(name);
            closed = true;
        }
        boolean isClosed() { return closed; }
    }

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Set up an in-memory H2 database for testing
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        // Create a test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_table (" + "id INT PRIMARY KEY, " + "string_val VARCHAR(255), " + "int_val INT, " + "date_val DATE, " + "time_val TIME, " + "timestamp_val TIMESTAMP)");

            // Insert some test data
            stmt.execute("INSERT INTO test_table VALUES (1, 'test1', 100, '2023-01-15', '14:30:15', '2023-01-15 14:30:15')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Drop the test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_table");
        }

        // Close the connection
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testGetJdbcDrivers() {
        // Test that we can get JDBC drivers
        Collection<JdbcDriverInfo> drivers = DbUtil.getJdbcDrivers();

        // Verify that the collection is not null and contains some drivers
        assertNotNull(drivers);
        assertFalse(drivers.isEmpty());

        // Verify that H2 driver is in the list
        boolean foundH2Driver = false;
        for (JdbcDriverInfo driver : drivers) {
            if (driver.name.contains("H2") || driver.className.contains("h2")) {
                foundH2Driver = true;
                break;
            }
        }
        assertTrue(foundH2Driver, "H2 driver should be in the list of JDBC drivers");
    }

    @Test
    void testToLocalDate() {
        // Test with null
        assertNull(DbUtil.toLocalDate(null));

        // Test with LocalDate
        LocalDate date = LocalDate.of(2023, 1, 15);
        assertEquals(date, DbUtil.toLocalDate(date));

        // Test with java.sql.Date
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        assertEquals(date, DbUtil.toLocalDate(sqlDate));

        // Test with unsupported type
        Exception exception = assertThrows(IllegalStateException.class, () -> DbUtil.toLocalDate("2023-01-15"));
        assertTrue(exception.getMessage().contains("cannot be converted to LocalDate"));
    }

    @Test
    void testToLocalDateTime() {
        // Test with null
        assertNull(DbUtil.toLocalDateTime(null));

        // Test with LocalDateTime
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 15, 14, 30, 15);
        assertEquals(dateTime, DbUtil.toLocalDateTime(dateTime));

        // Test with java.sql.Timestamp
        java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(dateTime);
        assertEquals(dateTime, DbUtil.toLocalDateTime(sqlTimestamp));

        // Test with unsupported type
        Exception exception = assertThrows(IllegalStateException.class, () -> DbUtil.toLocalDateTime("2023-01-15 14:30:15"));
        assertTrue(exception.getMessage().contains("cannot be converted to LocalDateTime"));
    }

    @Test
    void testToLocalTime() {
        // Test with null
        assertNull(DbUtil.toLocalTime(null));

        // Test with LocalTime
        LocalTime time = LocalTime.of(14, 30, 15);
        assertEquals(time, DbUtil.toLocalTime(time));

        // Test with java.sql.Time
        java.sql.Time sqlTime = java.sql.Time.valueOf(time);
        assertEquals(time, DbUtil.toLocalTime(sqlTime));

        // Test with unsupported type
        Exception exception = assertThrows(IllegalStateException.class, () -> DbUtil.toLocalTime("14:30:15"));
        assertTrue(exception.getMessage().contains("cannot be converted to LocalTime"));
    }

    @Test
    void testStream() throws SQLException {
        // Test streaming results from a query
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id")) {

            // Use DbUtil.stream to convert ResultSet to Stream
            Stream<Integer> idStream = DbUtil.stream(rs, resultSet -> {
                try {
                    return resultSet.getInt("id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            // Verify the stream contains the expected data
            assertEquals(1, idStream.findFirst().orElse(0));
        }
    }

    @Test
    void testStreamWithMultipleRows() throws SQLException {
        // Insert additional test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO test_table VALUES (2, 'test2', 200, '2023-02-15', '15:30:15', '2023-02-15 15:30:15')");
            stmt.execute("INSERT INTO test_table VALUES (3, 'test3', 300, '2023-03-15', '16:30:15', '2023-03-15 16:30:15')");
        }

        // Test streaming results from a query with multiple rows
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id")) {

            // Use DbUtil.stream to convert ResultSet to Stream
            Stream<String> stringStream = DbUtil.stream(rs, resultSet -> {
                try {
                    return resultSet.getString("string_val");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            // Collect all values and verify
            Object[] values = stringStream.toArray();
            assertEquals(3, values.length);
            assertEquals("test1", values[0]);
            assertEquals("test2", values[1]);
            assertEquals("test3", values[2]);
        }
    }

    @Test
    void testLoadDriver() {
        // Test loading driver from the current ClassLoader
        try {
            Optional<Driver> driver = DbUtil.loadDriver(getClass().getClassLoader());

            // The H2 driver should be available
            assertTrue(driver.isPresent());
            assertNotNull(driver.get());
            assertTrue(driver.get().getClass().getName().contains("h2") || driver.get().getClass().getName().contains("H2"), "Expected H2 driver but got: " + driver.get().getClass().getName());
        } catch (ClassNotFoundException | SQLException e) {
            fail("Exception while testing loadDriver: " + e.getMessage());
        }
    }

    @Test
    void testLoadDriverWithURLs() throws URISyntaxException {
        // Test loading driver using URLs
        try {
            // Get the URL of the H2 driver JAR that's already in the classpath
            // This is a bit of a hack, but it allows us to test the method without needing external files
            String h2ClassName = "org.h2.Driver";
            URL h2ClassUrl = getClass().getClassLoader().getResource(h2ClassName.replace('.', '/') + ".class");
            assertNotNull(h2ClassUrl, "Could not find H2 driver class URL");

            // Convert the class URL to the JAR URL
            String urlString = h2ClassUrl.toString();
            // Extract the JAR URL part (everything before the ! character)
            String jarUrlString = urlString.substring(0, urlString.indexOf('!'));
            // Remove the "jar:" prefix if present
            if (jarUrlString.startsWith("jar:")) {
                jarUrlString = jarUrlString.substring(4);
            }
            URL jarUrl = new URI(jarUrlString).toURL();

            // Test the loadDriver method with the JAR URL
            Optional<Driver> driver = DbUtil.loadDriver(jarUrl);

            // The H2 driver should be available
            assertTrue(driver.isPresent(), "Driver should be present");
            assertNotNull(driver.get(), "Driver should not be null");
            assertTrue(driver.get().getClass().getName().contains("h2") || driver.get().getClass().getName().contains("H2"), "Expected H2 driver but got: " + driver.get().getClass().getName());
        } catch (ClassNotFoundException | SQLException | java.net.MalformedURLException e) {
            fail("Exception while testing loadDriver with URLs: " + e.getMessage());
        }
    }

    @Test
    void testCreateDataSource() {
        // Test creating a DataSource with a Driver
        try {
            // First, get a Driver instance
            Optional<Driver> driverOpt = DbUtil.loadDriver(getClass().getClassLoader());
            assertTrue(driverOpt.isPresent(), "Driver should be present");
            Driver driver = driverOpt.get();

            // Test URL, user, and password for H2 in-memory database
            String url = "jdbc:h2:mem:testdb";
            String user = "sa";
            String password = "";

            // Create the DataSource
            DataSource dataSource = DbUtil.createDataSource(driver, url, user, password);

            // Verify the DataSource is not null
            assertNotNull(dataSource, "DataSource should not be null");

            // Verify we can get a connection from the DataSource
            try (Connection conn = dataSource.getConnection()) {
                assertNotNull(conn, "Connection should not be null");
                assertFalse(conn.isClosed(), "Connection should be open");

                // Verify we can execute a simple query
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    assertTrue(rs.next(), "ResultSet should have at least one row");
                    assertEquals(1, rs.getInt(1), "Query result should be 1");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            fail("Exception while testing createDataSource: " + e.getMessage());
        }
    }

    @Test
    void testCreateDataSourceWithInvalidURL() {
        // Test creating a DataSource with an invalid URL
        try {
            // First, get a Driver instance
            Optional<Driver> driverOpt = DbUtil.loadDriver(getClass().getClassLoader());
            assertTrue(driverOpt.isPresent(), "Driver should be present");
            Driver driver = driverOpt.get();

            // Invalid URL for H2 driver
            String url = "jdbc:invalid:mem:testdb";
            String user = "sa";
            String password = "";

            // Attempt to create the DataSource with invalid URL
            // This should throw a SQLException
            SQLException exception = assertThrows(SQLException.class, () -> DbUtil.createDataSource(driver, url, user, password));

            // Verify the exception message
            assertTrue(exception.getMessage().contains("URL not accepted by driver"), "Exception message should indicate URL is not accepted");
        } catch (ClassNotFoundException | SQLException e) {
            fail("Exception while testing createDataSource with invalid URL: " + e.getMessage());
        }
    }

    @Test
    void testStreamAndClose_withThreeCloseables_ordering() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet realRs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id");

            java.util.List<String> log = new java.util.ArrayList<>();

            // Create a proxy ResultSet that records when close() is called, then delegates to real ResultSet
            ResultSet rs = (ResultSet) java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{ResultSet.class},
                    (proxy, method, args) -> {
                        LangUtil.addIf("close".equals(method.getName()), log, "RS");
                        try {
                            return method.invoke(realRs, args);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw e.getCause();
                        }
                    }
            );

            RecordingCloseable a = new RecordingCloseable("A", log, rs);
            RecordingCloseable b = new RecordingCloseable("B", log, rs);
            RecordingCloseable c = new RecordingCloseable("C", log, rs);

            Stream<Integer> stream = DbUtil.streamAndClose(rs, resultSet -> {
                try {
                    return resultSet.getInt("id");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, a, b, c);

            // consume the stream
            int count = (int) stream.count();
            assertEquals(1, count);

            // close the stream to trigger closing chain
            stream.close();

            // Validate ordering: closeables in reverse order, then ResultSet
            assertEquals(java.util.List.of("C", "B", "A", "RS"), log);

            // Ensure closeables are closed and ResultSet is closed at the end
            assertTrue(a.isClosed());
            assertTrue(b.isClosed());
            assertTrue(c.isClosed());
            assertTrue(realRs.isClosed());
        }
    }

}
