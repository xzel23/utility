// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.h2.jdbc.JdbcSQLFeatureNotSupportedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for NamedParameterStatement.
 * This class adds tests for methods that were not covered in the original test class.
 */
@Execution(ExecutionMode.SAME_THREAD) // all tests use the same Database instance
class NamedParameterStatementAdditionalTest {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Set up an in-memory H2 database for testing
        connection = DriverManager.getConnection("jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1");

        // Create a test table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE test_table (" +
                    "id INT PRIMARY KEY, " +
                    "string_val VARCHAR(255), " +
                    "int_val INT, " +
                    "double_val DOUBLE, " +
                    "decimal_val DECIMAL(10,2), " +
                    "boolean_val BOOLEAN, " +
                    "date_val DATE, " +
                    "time_val TIME, " +
                    "timestamp_val TIMESTAMP, " +
                    "blob_val BLOB, " +
                    "clob_val CLOB)");
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
    void testSetArray() throws SQLException {
        try {
            String sql = "INSERT INTO test_table (id, string_val) VALUES (:id, :arrayVal)";

            // Create a mock Array since H2 might not support arrays directly
            Array mockArray = new Array() {
                private final String[] data = {"value1", "value2", "value3"};

                @Override
                public String getBaseTypeName() throws SQLException {
                    return "VARCHAR";
                }

                @Override
                public int getBaseType() throws SQLException {
                    return java.sql.Types.VARCHAR;
                }

                @Override
                public Object getArray() throws SQLException {
                    return data;
                }

                @Override
                public Object getArray(long index, int count) throws SQLException {
                    return Arrays.copyOfRange(data, (int) index - 1, (int) index - 1 + count);
                }

                @Override
                public Object getArray(long index, int count, java.util.Map<String, Class<?>> map) throws SQLException {
                    return getArray(index, count);
                }

                @Override
                public Object getArray(java.util.Map<String, Class<?>> map) throws SQLException {
                    return data;
                }

                @Override
                public ResultSet getResultSet() throws SQLException {
                    throw new SQLException("Not implemented");
                }

                @Override
                public ResultSet getResultSet(long index, int count) throws SQLException {
                    throw new SQLException("Not implemented");
                }

                @Override
                public ResultSet getResultSet(long index, int count, java.util.Map<String, Class<?>> map) throws SQLException {
                    throw new SQLException("Not implemented");
                }

                @Override
                public ResultSet getResultSet(java.util.Map<String, Class<?>> map) throws SQLException {
                    throw new SQLException("Not implemented");
                }

                @Override
                public void free() throws SQLException {
                    // No resources to free
                }
            };

            try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
                stmt.setInt("id", 100);
                stmt.setArray("arrayVal", mockArray);
                stmt.executeUpdate();
            }

            // Verify - in a real database, we would query the array
            // Here we just check if the record was inserted
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM test_table WHERE id = 100")) {
                assertTrue(rs.next());
                assertNotNull(rs.getObject("string_val"));
            }

            // Test with null value
            sql = "UPDATE test_table SET string_val = :arrayVal WHERE id = :id";
            try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
                stmt.setInt("id", 100);
                stmt.setArray("arrayVal", null);
                stmt.executeUpdate();
            }

            // Verify null value was stored
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT string_val FROM test_table WHERE id = 100")) {
                assertTrue(rs.next());
                assertNull(rs.getString("string_val"));
            }
        } catch (JdbcSQLFeatureNotSupportedException e) {
            Assumptions.assumeTrue(false, "Array type not supported by this database: " + e.getMessage());
        }
    }

    @Test
    void testSetAsciiStreamWithIntLength() throws Exception {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :asciiStream)";
        String testString = "test ascii stream with int length";
        byte[] testBytes = testString.getBytes(StandardCharsets.US_ASCII);

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             ByteArrayInputStream stream = new ByteArrayInputStream(testBytes)) {

            stmt.setInt("id", 101);
            stmt.setAsciiStream("asciiStream", stream, testBytes.length);
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 101")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetAsciiStreamWithLongLength() throws Exception {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :asciiStream)";
        String testString = "test ascii stream with long length";
        byte[] testBytes = testString.getBytes(StandardCharsets.US_ASCII);

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             ByteArrayInputStream stream = new ByteArrayInputStream(testBytes)) {

            stmt.setInt("id", 102);
            stmt.setAsciiStream("asciiStream", stream, (long) testBytes.length);
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 102")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetBinaryStreamWithIntLength() throws Exception {
        String sql = "INSERT INTO test_table (id, blob_val) VALUES (:id, :binaryStream)";
        byte[] testBytes = "test binary stream with int length".getBytes(StandardCharsets.UTF_8);

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             ByteArrayInputStream stream = new ByteArrayInputStream(testBytes)) {

            stmt.setInt("id", 103);
            stmt.setBinaryStream("binaryStream", stream, testBytes.length);
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT blob_val FROM test_table WHERE id = 103")) {
            assertTrue(rs.next());
            byte[] result = rs.getBytes("blob_val");
            assertArrayEquals(testBytes, result);
        }
    }

    @Test
    void testSetBinaryStreamWithLongLength() throws Exception {
        String sql = "INSERT INTO test_table (id, blob_val) VALUES (:id, :binaryStream)";
        byte[] testBytes = "test binary stream with long length".getBytes(StandardCharsets.UTF_8);

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             ByteArrayInputStream stream = new ByteArrayInputStream(testBytes)) {

            stmt.setInt("id", 104);
            stmt.setBinaryStream("binaryStream", stream, (long) testBytes.length);
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT blob_val FROM test_table WHERE id = 104")) {
            assertTrue(rs.next());
            byte[] result = rs.getBytes("blob_val");
            assertArrayEquals(testBytes, result);
        }
    }

    @Test
    void testSetBlobWithInputStreamAndLength() throws Exception {
        String sql = "INSERT INTO test_table (id, blob_val) VALUES (:id, :blobVal)";
        byte[] testBytes = "test blob with input stream and length".getBytes(StandardCharsets.UTF_8);

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             ByteArrayInputStream stream = new ByteArrayInputStream(testBytes)) {

            stmt.setInt("id", 105);
            stmt.setBlob("blobVal", stream, testBytes.length);
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT blob_val FROM test_table WHERE id = 105")) {
            assertTrue(rs.next());
            byte[] result = rs.getBytes("blob_val");
            assertArrayEquals(testBytes, result);
        }
    }

    @Test
    void testSetCharacterStreamWithIntLength() throws SQLException {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :charStream)";
        String testString = "test character stream with int length";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             StringReader reader = new StringReader(testString)) {

            stmt.setInt("id", 106);
            stmt.setCharacterStream("charStream", reader, testString.length());
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 106")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetCharacterStreamWithLongLength() throws SQLException {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :charStream)";
        String testString = "test character stream with long length";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             StringReader reader = new StringReader(testString)) {

            stmt.setInt("id", 107);
            stmt.setCharacterStream("charStream", reader, (long) testString.length());
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 107")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetClobWithReaderAndLength() throws SQLException {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :clobVal)";
        String testString = "test clob with reader and length";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             StringReader reader = new StringReader(testString)) {

            stmt.setInt("id", 108);
            stmt.setClob("clobVal", reader, testString.length());
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 108")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetNCharacterStreamWithLength() throws SQLException {
        String sql = "INSERT INTO test_table (id, clob_val) VALUES (:id, :nCharStream)";
        String testString = "test N character stream with length";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql);
             StringReader reader = new StringReader(testString)) {

            stmt.setInt("id", 109);
            stmt.setNCharacterStream("nCharStream", reader, testString.length());
            stmt.executeUpdate();
        }

        // Verify
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT clob_val FROM test_table WHERE id = 109")) {
            assertTrue(rs.next());
            String result = rs.getString("clob_val");
            assertEquals(testString, result);
        }
    }

    @Test
    void testSetObjectWithTypeAndScale() throws SQLException {
        String sql = "INSERT INTO test_table (id, decimal_val) VALUES (:id, :decimalVal)";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
            stmt.setInt("id", 110);
            // Use setObject with targetSqlType and scale
            stmt.setObject("decimalVal", 123.456, java.sql.Types.DECIMAL, 2);
            stmt.executeUpdate();
        }

        // Verify - the value should be rounded to 2 decimal places
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT decimal_val FROM test_table WHERE id = 110")) {
            assertTrue(rs.next());
            assertEquals(123.46, rs.getDouble("decimal_val"), 0.001);
        }

        // Test with null value
        sql = "UPDATE test_table SET decimal_val = :decimalVal WHERE id = :id";
        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
            stmt.setInt("id", 110);
            stmt.setObject("decimalVal", null, java.sql.Types.DECIMAL, 2);
            stmt.executeUpdate();
        }

        // Verify null value was stored
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT decimal_val FROM test_table WHERE id = 110")) {
            assertTrue(rs.next());
            assertNull(rs.getObject("decimal_val"));
        }
    }

    @Test
    void testSetObjectWithSQLTypeAndScale() throws SQLException {
        String sql = "INSERT INTO test_table (id, decimal_val) VALUES (:id, :decimalVal)";

        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
            stmt.setInt("id", 111);
            // Use setObject with SQLType and scale
            stmt.setObject("decimalVal", 123.456, JDBCType.DECIMAL, 2);
            stmt.executeUpdate();
        }

        // Verify - the value should be rounded to 2 decimal places
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT decimal_val FROM test_table WHERE id = 111")) {
            assertTrue(rs.next());
            assertEquals(123.46, rs.getDouble("decimal_val"), 0.001);
        }

        // Test with null value
        sql = "UPDATE test_table SET decimal_val = :decimalVal WHERE id = :id";
        try (NamedParameterStatement stmt = new NamedParameterStatement(connection, sql)) {
            stmt.setInt("id", 111);
            stmt.setObject("decimalVal", null, JDBCType.DECIMAL, 2);
            stmt.executeUpdate();
        }

        // Verify null value was stored
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT decimal_val FROM test_table WHERE id = 111")) {
            assertTrue(rs.next());
            assertNull(rs.getObject("decimal_val"));
        }
    }
}
