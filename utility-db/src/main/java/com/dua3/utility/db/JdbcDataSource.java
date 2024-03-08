// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import com.dua3.cabe.annotations.Nullable;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DataSource implementation for JDBC.
 */
@SuppressWarnings("RedundantThrows")
public class JdbcDataSource implements DataSource {

    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private final Properties properties = new Properties();
    private String url;
    private PrintWriter logWriter;
    private int loginTimeout;
    private Driver driver;

    /**
     * Constructor.
     */
    public JdbcDataSource() {
        // nop
    }

    private void log(String message) {
        if (logWriter != null) {
            logWriter.format("%s%n", message);
        }
    }

    /**
     * Set the JDBC driver for this instance.
     *
     * @param driver the driver
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger() is not supported");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!iface.isAssignableFrom(getClass())) {
            throw new SQLException(iface.getName() + " is not assignable from " + getClass().getName());
        }
        return (T) this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        log("getConnection()");
        return driver.connect(url, properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        log("getConnection(username, password)");
        Properties p = new Properties(properties);
        p.setProperty(USER, username);
        p.setProperty(PASSWORD, password);
        return driver.connect(url, p);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        logWriter = out;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    /**
     * Set database URL.
     *
     * @param url the URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set database user.
     *
     * @param user the database user or `null` to unset
     */
    public void setUser(@Nullable String user) {
        if (user == null) {
            // Properties class does not support storing null values!
            properties.remove(USER);
        } else {
            properties.setProperty(USER, user);
        }
    }

    /**
     * Set database password.
     *
     * @param password the database password or `null` to unset
     */
    public void setPassword(@Nullable String password) {
        if (password == null) {
            // Properties class does not support storing null values!
            properties.remove(PASSWORD);
        } else {
            properties.setProperty(PASSWORD, password);
        }
    }
}
