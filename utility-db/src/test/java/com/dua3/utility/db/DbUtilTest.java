// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DbUtil.
 */
@Execution(ExecutionMode.SAME_THREAD) // all tests use the same Database instance
class DbUtilTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Set up an in-memory H2 database for testing
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");

        // Create a test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_table (" +
                    "id INT PRIMARY KEY, " +
                    "string_val VARCHAR(255), " +
                    "int_val INT, " +
                    "date_val DATE, " +
                    "time_val TIME, " +
                    "timestamp_val TIMESTAMP)");

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
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            DbUtil.toLocalDate("2023-01-15");
        });
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
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            DbUtil.toLocalDateTime("2023-01-15 14:30:15");
        });
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
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            DbUtil.toLocalTime("14:30:15");
        });
        assertTrue(exception.getMessage().contains("cannot be converted to LocalTime"));
    }

    @Test
    void testStream() throws SQLException {
        // Test streaming results from a query
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id")) {

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
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_table ORDER BY id")) {

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
            assertTrue(driver.get().getClass().getName().contains("h2") || 
                       driver.get().getClass().getName().contains("H2"),
                      "Expected H2 driver but got: " + driver.get().getClass().getName());
        } catch (ClassNotFoundException | SQLException e) {
            fail("Exception while testing loadDriver: " + e.getMessage());
        }
    }

    // Note: We can't test createDataSource directly because it requires a Driver instance,
    // which we would normally get from the private getDriver method.

    // Note: We can't test UncheckedCloser because it's a private interface in DbUtil.
}
