// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class JdbcDataSource implements DataSource {

    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private String url;
    private PrintWriter logWriter = null;
    private int loginTimeout;
    private Driver driver = null;
    private Properties properties = new Properties();

    public JdbcDataSource() {
        // nop
    }

    private void log(String message) {
        if (logWriter != null) {
            logWriter.format("%s%n", message);
        }
    }

    public void setDriver(Driver driver) {
        this.driver = Objects.requireNonNull(driver);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!iface.isAssignableFrom(this.getClass())) {
            throw new SQLException();
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
        p.put(USER, username);
        p.put(PASSWORD, password);
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
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        properties.put(USER, user);
    }

    public void setPassword(String password) {
        properties.put(PASSWORD, password);
    }
}
