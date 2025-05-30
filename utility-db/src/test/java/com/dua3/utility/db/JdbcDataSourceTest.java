// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JdbcDataSource.
 */
class JdbcDataSourceTest {

    private MockDriver mockDriver;
    private JdbcDataSource dataSource;
    private static final String TEST_URL = "jdbc:mock:test";
    private static final String TEST_USER = "testUser";
    private static final String TEST_PASSWORD = "testPassword";

    @BeforeEach
    void setUp() {
        mockDriver = new MockDriver();
        dataSource = new JdbcDataSource(mockDriver);
        dataSource.setUrl(TEST_URL);
    }

    @Test
    void testConstructor() {
        // Verify the data source was created successfully
        assertNotNull(dataSource);
    }

    @Test
    void testGetParentLogger() {
        // getParentLogger should throw SQLFeatureNotSupportedException
        Exception exception = assertThrows(SQLFeatureNotSupportedException.class, () -> {
            dataSource.getParentLogger();
        });

        String expectedMessage = "getParentLogger() is not supported";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testUnwrap() throws SQLException {
        // Test unwrap with JdbcDataSource.class
        JdbcDataSource unwrapped = dataSource.unwrap(JdbcDataSource.class);
        assertSame(dataSource, unwrapped);

        // Test unwrap with incompatible interface
        Exception exception = assertThrows(SQLException.class, () -> {
            dataSource.unwrap(String.class);
        });

        String expectedMessage = "is not assignable from";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testIsWrapperFor() throws SQLException {
        // isWrapperFor should return false
        assertFalse(dataSource.isWrapperFor(JdbcDataSource.class));
    }

    @Test
    void testGetConnection() throws SQLException {
        // Test getConnection()
        Connection connection = dataSource.getConnection();

        // Verify the connection was created with the correct URL and properties
        assertNotNull(connection);
        assertEquals(TEST_URL, mockDriver.getLastUrl());
        assertNotNull(mockDriver.getLastProperties());
    }

    @Test
    void testGetConnectionWithCredentials() throws SQLException {
        // Test getConnection(username, password)
        Connection connection = dataSource.getConnection(TEST_USER, TEST_PASSWORD);

        // Verify the connection was created with the correct URL and properties
        assertNotNull(connection);
        assertEquals(TEST_URL, mockDriver.getLastUrl());
        assertEquals(TEST_USER, mockDriver.getLastProperties().getProperty("user"));
        assertEquals(TEST_PASSWORD, mockDriver.getLastProperties().getProperty("password"));
    }

    @Test
    void testLogWriter() throws SQLException {
        // Test getLogWriter and setLogWriter
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        dataSource.setLogWriter(printWriter);
        assertSame(printWriter, dataSource.getLogWriter());

        // Test that log messages are written to the log writer
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(stringWriter.toString().contains("getConnection()"));
        }

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        dataSource.setLogWriter(printWriter);

        try (Connection connection = dataSource.getConnection(TEST_USER, TEST_PASSWORD)) {
            assertTrue(stringWriter.toString().contains("getConnection(username, password)"));
        }
    }

    @Test
    void testLoginTimeout() throws SQLException {
        // Test getLoginTimeout and setLoginTimeout
        dataSource.setLoginTimeout(30);
        assertEquals(30, dataSource.getLoginTimeout());
    }

    @Test
    void testSetUrl() throws SQLException {
        // Test setUrl
        String newUrl = "jdbc:mock:newtest";
        dataSource.setUrl(newUrl);

        // Verify the URL was updated
        try (Connection connection = dataSource.getConnection()) {
            assertEquals(newUrl, mockDriver.getLastUrl());
        }
    }

    @Test
    void testSetUser() throws SQLException {
        // Test setUser
        dataSource.setUser(TEST_USER);

        // Verify the user was set in the properties
        try (Connection connection = dataSource.getConnection()) {
            assertEquals(TEST_USER, mockDriver.getLastProperties().getProperty("user"));
        }

        // Test setting user to null
        dataSource.setUser(null);
        try (Connection connection = dataSource.getConnection()) {
            assertFalse(mockDriver.getLastProperties().containsKey("user"));
        }
    }

    @Test
    void testSetPassword() throws SQLException {
        // Test setPassword
        dataSource.setPassword(TEST_PASSWORD);

        // Verify the password was set in the properties
        try (Connection connection = dataSource.getConnection()) {
            assertEquals(TEST_PASSWORD, mockDriver.getLastProperties().getProperty("password"));
        }
        // Test setting password to null
        dataSource.setPassword(null);
        try (Connection connection = dataSource.getConnection()) {
            assertFalse(mockDriver.getLastProperties().containsKey("password"));
        }
    }

    /**
     * Mock implementation of Driver for testing.
     */
    private static class MockDriver implements Driver {
        private String lastUrl;
        private Properties lastProperties;
        private final MockConnection mockConnection = new MockConnection();

        public String getLastUrl() {
            return lastUrl;
        }

        public Properties getLastProperties() {
            return lastProperties;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            this.lastUrl = url;
            this.lastProperties = new Properties();
            if (info != null) {
                this.lastProperties.putAll(info);
            }
            return mockConnection;
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return url != null && url.startsWith("jdbc:mock:");
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 1;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
    }

    /**
     * Mock implementation of Connection for testing.
     */
    private static class MockConnection implements Connection {
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public java.sql.Statement createStatement() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            // No-op for mock
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return false;
        }

        @Override
        public void commit() throws SQLException {
            // No-op for mock
        }

        @Override
        public void rollback() throws SQLException {
            // No-op for mock
        }

        @Override
        public void close() throws SQLException {
            // No-op for mock
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public java.sql.DatabaseMetaData getMetaData() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            // No-op for mock
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return false;
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            // No-op for mock
        }

        @Override
        public String getCatalog() throws SQLException {
            return null;
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            // No-op for mock
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return Connection.TRANSACTION_NONE;
        }

        @Override
        public java.sql.SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {
            // No-op for mock
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            // No-op for mock
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            // No-op for mock
        }

        @Override
        public int getHoldability() throws SQLException {
            return 0;
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException {
            // No-op for mock
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
            // No-op for mock
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.Clob createClob() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.Blob createBlob() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.NClob createNClob() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.SQLXML createSQLXML() throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return true;
        }

        @Override
        public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException {
            // No-op for mock
        }

        @Override
        public void setClientInfo(Properties properties) throws java.sql.SQLClientInfoException {
            // No-op for mock
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return null;
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return new Properties();
        }

        @Override
        public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public java.sql.Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            throw new UnsupportedOperationException("Not implemented for mock");
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            // No-op for mock
        }

        @Override
        public String getSchema() throws SQLException {
            return null;
        }

        @Override
        public void abort(java.util.concurrent.Executor executor) throws SQLException {
            // No-op for mock
        }

        @Override
        public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
            // No-op for mock
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
        }
    }
}