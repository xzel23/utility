// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.jspecify.annotations.Nullable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

/**
 * <p>
 * Code was taken from the Java World article "Named Parameters for
 * PreparedStatement"
 * and adapted to work with up-to-date Java versions.
 * </p>
 * Original comment:
 * <br>
 * This class wraps around a {@link PreparedStatement} and allows the programmer
 * to set parameters by name instead of by index. This eliminates any confusion
 * as to which parameter index represents what. This also means that rearranging
 * the SQL statement or adding a parameter doesn't involve renumbering your
 * indices. Code such as this:
 *
 * <pre>
 *     Connection con = getConnection();
 *     String query = "select * from my_table where name=? or address=?";
 *     PreparedStatement p = con.prepareStatement(query);
 *     p.setString(1, "bob");
 *     p.setString(2, "123 terrace ct");
 *     ResultSet rs = p.executeQuery();
 * </pre>
 * <p>
 * can be replaced with:
 *
 * <pre>
 *     Connection con = getConnection();
 *     String query = "select * from my_table where name=:name or address=:address";
 *     NamedParameterStatement p = new NamedParameterStatement(con, query);
 *     p.setString("name", "bob");
 *     p.setString("address", "123 terrace ct");
 *     ResultSet rs = p.executeQuery();
 * </pre>
 *
 * @author adam_crume
 * @author Axel Howind
 */
@SuppressWarnings("MagicCharacter")
public class NamedParameterStatement implements AutoCloseable {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(NamedParameterStatement.class);
    private static boolean showUnknownParameterTypeAsWarning = true;
    /**
     * The statement this object is wrapping.
     */
    private final PreparedStatement statement;
    /**
     * Maps parameter names to arrays of ints which are the parameter indices.
     */
    private final Map<String, ParameterInfo> indexMap;
    /**
     * flag: has meta data been added to parameter info?
     */
    private boolean hasMeta;

    /**
     * Creates a NamedParameterStatement. Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
     *
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException          if the statement could not be created
     * @throws IllegalStateException if the same parameter is used for different types
     */
    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap<>();
        String parsedQuery = parse(query, indexMap);
        //noinspection JDBCPrepareStatementWithNonConstantString - by design
        statement = connection.prepareStatement(parsedQuery);
    }

    private static @Nullable JDBCType getParameterType(ParameterMetaData meta, int index) {
        try {
            return JDBCType.valueOf(meta.getParameterType(index));
        } catch (SQLException e) {
            // same bug as
            // https://jira.spring.io/si/jira.issueviews:issue-html/SPR-13825/SPR-13825.html
            // [SPR-13825] Oracle 12c JDBC driver throws inconsistent exception from
            // getParameterType (affecting setNull calls)
            if (showUnknownParameterTypeAsWarning) {
                LOG.warn("could not determine parameter types");
                showUnknownParameterTypeAsWarning = false;
            } else {
                LOG.debug("(REPEAT) could not determine parameter types");
            }
            return null;
        }
    }

    /**
     * Parses a query with named parameters. The parameter-index mappings are put
     * into the map, and the parsed query is returned. DO NOT CALL FROM CLIENT
     * CODE. This method is non-private so JUnit code can test it.
     *
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    static String parse(String query, Map<String, ParameterInfo> paramMap) {
        // I was originally using regular expressions, but they didn't work well for
        // ignoring parameter-like strings inside quotes.
        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for (int i = 0; i < length; i++) {
            char c = query.charAt(i);
            switch (c) {
                case '\'' -> inSingleQuote = !inSingleQuote;
                case '"' -> inDoubleQuote = !inDoubleQuote;
                case ':' -> {
                    if (!inSingleQuote && ! inDoubleQuote && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                        int j = i + 2;
                        while (j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                            j++;
                        }
                        String name = query.substring(i + 1, j);
                        c = '?'; // replace the parameter with a question mark
                        i += name.length(); // skip past the end if the parameter

                        ParameterInfo info = paramMap.computeIfAbsent(name, ParameterInfo::new);
                        info.addIndex(index);

                        index++;
                    }
                }
                default -> { /* do nothing */ }
            }
            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }

    /**
     * Add parameter metadata to the statement.
     * <em>Note:</em>This is done only once and the result is cached. You only need to call this method if you are
     * interested in the Exceptions thrown when the parameter info could not be determined. It has to be called
     * directly after creating the NamedParameterStatement because results are cached and so exceptions will not be
     * thrown the second time this method is called.
     * <strong>Warning:</strong> Not all databases support querying parameter metadata, and
     * some that do have serious bugs (like returning wrong data types), so check your database
     * manufacturer's documentation and test that you get the correct results when using this feature.
     *
     * @throws UnsupportedOperationException if the database (driver) does not support querying parameter metadata
     * @throws IllegalStateException         if the same variable is used in different places that require different datatypes
     * @throws SQLException                  if something else goes wrong
     */
    public void addParameterInfo() throws SQLException, UnsupportedOperationException, IllegalStateException {
        if (hasMeta) {
            return;
        }

        ParameterMetaData meta = statement.getParameterMetaData();
        for (ParameterInfo param : indexMap.values()) {
            param.type = null;
            for (int index : param.indexes) {
                JDBCType type = getParameterType(meta, index);
                if (param.type != null && type != param.type) {
                    String msg = String.format(Locale.ROOT, "parameter type mismatch for parameter '%s': %s, %s", param.name,
                            param.type,
                            meta);
                    throw new IllegalStateException(msg);
                }
                param.type = type;
            }
        }

        hasMeta = true;
    }

    private void initParameterInfo() {
        try {
            addParameterInfo();
        } catch (SQLException e) {
            LOG.warn("could not get parameter info for PreparedStatement", e);
        } catch (UnsupportedOperationException e) {
            LOG.warn("could not get parameter info for PreparedStatement (unsupported operation)", e);
        } catch (IllegalStateException e) {
            LOG.warn("could not get parameter info for PreparedStatement (conflicting types for the same parameter)", e);
        }
    }

    /**
     * Returns the indexes for a parameter.
     *
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private List<Integer> getIndexes(String name) {
        return Objects.requireNonNull(indexMap.get(name), () -> "unknown parameter '" + name + "'.").indexes;
    }

    private <T> void set(SQLType type, String name, @Nullable T value, SetParameter<T> setter) throws SQLException {
        if (value == null) {
            setNull(name, type);
        } else {
            setNonNull(name, value, setter);
        }
    }

    private <T> void setNonNull(String name, T value, SetParameter<T> setter) throws SQLException {
        for (int idx : getIndexes(name)) {
            setter.accept(idx, value);
        }
    }

    private <T> void setNonNullWithIntArg(String name, T value, int arg, SetParameterInt<T> setter) throws SQLException {
        for (int idx : getIndexes(name)) {
            setter.accept(idx, value, arg);
        }
    }

    private <T> void setNonNullWithLongArg(String name, T value, long arg, SetParameterLong<T> setter) throws SQLException {
        for (int idx : getIndexes(name)) {
            setter.accept(idx, value, arg);
        }
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setArray(int, Array)
     */
    public void setArray(String name, @Nullable Array value) throws SQLException {
        set(JDBCType.ARRAY, name, value, statement::setArray);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setAsciiStream(int, InputStream)
     */
    public void setAsciiStream(String name, InputStream value) throws SQLException {
        setNonNull(name, value, statement::setAsciiStream);
    }

    /**
     * Set a parameter.
     *
     * @param name   parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value  parameter value
     * @param length the number of bytes
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setAsciiStream(int, InputStream, int)
     */
    public void setAsciiStream(String name, InputStream value, int length) throws SQLException {
        setNonNullWithIntArg(name, value, length, statement::setAsciiStream);
    }

    /**
     * Set a parameter.
     *
     * @param name   parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value  parameter value
     * @param length the number of bytes
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setAsciiStream(int, InputStream, long)
     */
    public void setAsciiStream(String name, InputStream value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setAsciiStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBigDecimal(int, BigDecimal)
     */
    public void setBigDecimal(String name, @Nullable BigDecimal value) throws SQLException {
        set(JDBCType.DECIMAL, name, value, statement::setBigDecimal);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBinaryStream(int, InputStream)
     */
    public void setBinaryStream(String name, InputStream value) throws SQLException {
        setNonNull(name, value, statement::setBinaryStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBinaryStream(int, InputStream, int)
     */
    void setBinaryStream(String name, InputStream value, int length) throws SQLException {
        setNonNullWithIntArg(name, value, length, statement::setBinaryStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBinaryStream(int, InputStream, long)
     */
    void setBinaryStream(String name, InputStream value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setBinaryStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBlob(int, InputStream)
     */
    void setBlob(String name, InputStream value) throws SQLException {
        setNonNull(name, value, statement::setBlob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBlob(int, InputStream, long) (int, Array)
     */
    void setBlob(String name, InputStream value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setBlob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBlob(int, Blob)
     */
    public void setBlob(String name, @Nullable Blob value) throws SQLException {
        set(JDBCType.BLOB, name, value, statement::setBlob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBoolean(int, boolean)
     */
    public void setBoolean(String name, boolean value) throws SQLException {
        setNonNull(name, value, statement::setBoolean);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setByte(int, byte)
     */
    public void setByte(String name, byte value) throws SQLException {
        setNonNull(name, value, statement::setByte);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setBytes(int, byte[])
     */
    public void setBytes(String name, byte[] value) throws SQLException {
        set(JDBCType.BINARY, name, value, statement::setBytes);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setCharacterStream(int, Reader)
     */
    void setCharacterStream(String name, Reader value) throws SQLException {
        setNonNull(name, value, statement::setCharacterStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setCharacterStream(int, Reader, int)
     */
    void setCharacterStream(String name, Reader value, int length) throws SQLException {
        setNonNullWithIntArg(name, value, length, statement::setCharacterStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setCharacterStream(int, Reader, long)
     */
    void setCharacterStream(String name, Reader value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setCharacterStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setClob(int, Reader)
     */
    void setClob(String name, Reader value) throws SQLException {
        setNonNull(name, value, statement::setClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setClob(int, Reader, long)
     */
    void setClob(String name, Reader value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setClob(int, Clob)
     */
    public void setClob(String name, @Nullable Clob value) throws SQLException {
        set(JDBCType.CLOB, name, value, statement::setClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setDouble(int, double)
     */
    public void setDouble(String name, double value) throws SQLException {
        setNonNull(name, value, statement::setDouble);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setFloat(int, float)
     */
    public void setFloat(String name, float value) throws SQLException {
        setNonNull(name, value, statement::setFloat);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, int value) throws SQLException {
        setNonNull(name, value, statement::setInt);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setLong(int, long)
     */
    public void setLong(String name, long value) throws SQLException {
        setNonNull(name, value, statement::setLong);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNCharacterStream(int, Reader)
     */
    void setNCharacterStream(String name, Reader value) throws SQLException {
        setNonNull(name, value, statement::setNCharacterStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNCharacterStream(int, Reader, long)
     */
    void setNCharacterStream(String name, Reader value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setNCharacterStream);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNClob(int, Reader)
     */
    void setNClob(String name, Reader value) throws SQLException {
        setNonNull(name, value, statement::setNClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNClob(int, Reader, long)
     */
    void setNClob(String name, Reader value, long length) throws SQLException {
        setNonNullWithLongArg(name, value, length, statement::setNClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNClob(int, NClob)
     */
    void setNClob(String name, @Nullable NClob value) throws SQLException {
        set(JDBCType.NCLOB, name, value, statement::setNClob);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNString(int, String)
     */
    void setNString(String name, @Nullable String value) throws SQLException {
        set(JDBCType.NCHAR, name, value, statement::setNString);
    }

    /**
     * Set a parameter to {@code null}.
     *
     * @param name parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNull(int, int)
     */
    void setNull(String name, SQLType type) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setNull(idx, type.getVendorTypeNumber());
        }
    }

    /**
     * Set a parameter to {@code null}.
     *
     * @param name parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNull(int, int)
     */
    void setNull(String name, int sqlType) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setNull(idx, sqlType);
        }
    }

    /**
     * Set a parameter to {@code null}.
     *
     * @param name parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setNull(int, int, String)
     */
    void setNull(String name, int sqlType, String typeName) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setNull(idx, sqlType, typeName);
        }
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object)
     */
    public void setObject(String name, @Nullable Object value) throws SQLException {
        set(JDBCType.JAVA_OBJECT, name, value, statement::setObject);
    }

    /**
     * Set a parameter.
     *
     * @param name parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object, int)
     */
    void setObject(String name, @Nullable Object value, int targetSqlType) throws SQLException {
        if (value == null) {
            setNull(name, targetSqlType);
        } else {
            setNonNullWithIntArg(name, value, targetSqlType, statement::setObject);
        }
    }

    /**
     * Set a parameter.
     *
     * @param name          parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value         parameter value
     * @param targetSqlType the SQL type (as defined in {@link java.sql.Types})
     * @param scaleOrLength for {@link java.sql.Types#DECIMAL} or {@link java.sql.Types#NUMERIC} types, this is the number of digits
     *                      after the decimal point. For Java Object types {@link InputStream} and {@link Reader}, this is the length
     *                      of the data in the stream or reader. For all other types, this value will be ignored.
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object, int, int) (int, Array)
     */
    public void setObject(String name, @Nullable Object value, int targetSqlType, int scaleOrLength) throws SQLException {
        if (value == null) {
            setNull(name, targetSqlType);
        } else {
            for (int idx : getIndexes(name)) {
                statement.setObject(idx, value, targetSqlType, scaleOrLength);
            }
        }
    }

    /**
     * Set a parameter.
     *
     * @param name          parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value         parameter value
     * @param targetSqlType the SQL type (as defined in {@link java.sql.Types})
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object, SQLType)
     */
    public void setObject(String name, @Nullable Object value, SQLType targetSqlType) throws SQLException {
        if (value == null) {
            setNull(name, targetSqlType);
        } else {
            for (int idx : getIndexes(name)) {
                statement.setObject(idx, value, targetSqlType);
            }
        }
    }

    /**
     * Set a parameter.
     *
     * @param name          parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value         parameter value
     * @param targetSqlType the SQL type (as defined in {@link java.sql.Types})
     * @param scaleOrLength for {@link java.sql.Types#DECIMAL} or {@link java.sql.Types#NUMERIC} types, this is the number of digits
     *                      after the decimal point. For Java Object types {@link InputStream} and {@link Reader}, this is the length
     *                      of the data in the stream or reader. For all other types, this value will be ignored.
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setObject(int, Object, SQLType, int)
     */
    public void setObject(String name, @Nullable Object value, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        if (value == null) {
            setNull(name, targetSqlType);
        } else {
            for (int idx : getIndexes(name)) {
                statement.setObject(idx, value, targetSqlType, scaleOrLength);
            }
        }
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setRef(int, Ref)
     */
    public void setRef(String name, @Nullable Ref value) throws SQLException {
        set(JDBCType.REF, name, value, statement::setRef);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setRowId(int, RowId)
     */
    public void setRowId(String name, @Nullable RowId value) throws SQLException {
        set(JDBCType.ROWID, name, value, statement::setRowId);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setShort(int, short)
     */
    public void setShort(String name, short value) throws SQLException {
        set(JDBCType.SMALLINT, name, value, statement::setShort);
    }

    /**
     * Set a parameter.
     *
     * @param name parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setSQLXML(int, SQLXML)
     */
    void setSQLXML(String name, @Nullable SQLXML value) throws SQLException {
        set(JDBCType.SQLXML, name, value, statement::setSQLXML);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setString(int, String)
     */
    public void setString(String name, @Nullable String value) throws SQLException {
        set(JDBCType.CHAR, name, value, statement::setString);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name (replaces the index parameter of the corresponding method of {@link PreparedStatement}).
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setURL(int, URL)
     */
    public void setURL(String name, @Nullable URL value) throws SQLException {
        set(JDBCType.DATALINK, name, value, statement::setURL);
    }

    /**
     * Returns the underlying statement.
     *
     * @return the statement
     */
    public PreparedStatement getStatement() {
        return statement;
    }

    /**
     * Executes the statement.
     *
     * @return true if the first result is a {@link ResultSet}
     * @throws SQLException if an error occurred
     * @see PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return statement.execute();
    }

    /**
     * Executes the statement, which must be a query.
     *
     * @return the query results
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
     * statement; or an SQL statement that returns nothing, such as a DDL
     * statement.
     *
     * @return number of rows affected
     * @throws SQLException if an error occurred
     * @see PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    /**
     * Closes the statement.
     *
     * @throws SQLException if an error occurred
     * @see Statement#close()
     */
    @Override
    public void close() throws SQLException {
        statement.close();
    }

    /**
     * Adds the current set of parameters as a batch entry.
     *
     * @throws SQLException if something went wrong
     */
    public void addBatch() throws SQLException {
        statement.addBatch();
    }

    /**
     * Executes all the batched statements.
     * See {@link Statement#executeBatch()} for details.
     *
     * @return update counts for each statement
     * @throws SQLException if something went wrong
     */
    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    /**
     * Set the fetch size for the statement (see {#link {@link Statement#setFetchSize(int)}}).
     *
     * @param rows the fetch size to set
     * @throws SQLException if an error occurs
     */
    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setDate(int, Date)
     */
    public void setLocalDate(String name, @Nullable LocalDate value) throws SQLException {
        if (value == null) {
            setNull(name, JDBCType.DATE);
        } else {
            setNonNull(name, Date.valueOf(value), statement::setDate);
        }
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, Timestamp)
     */
    public void setLocalDateTime(String name, @Nullable LocalDateTime value) throws SQLException {
        if (value == null) {
            setNull(name, JDBCType.TIMESTAMP);
        } else {
            setNonNull(name, Timestamp.valueOf(value), statement::setTimestamp);
        }
    }

    /**
     * Set a parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTime(int, Time)
     */
    public void setLocalTime(String name, @Nullable LocalTime value) throws SQLException {
        if (value == null) {
            setNull(name, JDBCType.TIME);
        } else {
            setNonNull(name, Time.valueOf(value), statement::setTime);
        }
    }

    /**
     * Set a parameter.
     * The timestamp is set using the {@link PreparedStatement#setTimestamp(int, Timestamp, Calendar)}
     * method so that the JDBC driver can make any timezone conversions necessary.
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public void setZonedDateTime(String name, @Nullable ZonedDateTime value) throws SQLException {
        if (value == null) {
            setNull(name, JDBCType.TIMESTAMP);
        } else {
            Calendar cal = GregorianCalendar.from(value);
            Timestamp ts = Timestamp.valueOf(value.toLocalDateTime());
            for (int idx : getIndexes(name)) {
                statement.setTimestamp(idx, ts, cal);
            }
        }
    }

    /**
     * Set a parameter.
     * The timestamp is set using the {@link PreparedStatement#setTimestamp(int, Timestamp, Calendar)}
     * method so that the JDBC driver can make any timezone conversions necessary.
     *
     * @param name  parameter name
     * @param value parameter value
     * @throws SQLException             if an error occurred
     * @throws IllegalArgumentException if the parameter does not exist
     * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public void setInstant(String name, @Nullable Instant value) throws SQLException {

        if (value == null) {
            setNull(name, JDBCType.TIMESTAMP);
        } else {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Timestamp ts = Timestamp.from(value);
            for (int idx : getIndexes(name)) {
                statement.setTimestamp(idx, ts, cal);
            }
        }
    }

    /**
     * Get update count.
     *
     * @return the update count
     * @throws SQLException on error
     * @see Statement#getUpdateCount()
     */
    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    /**
     * Get result set.
     *
     * @return the result set
     * @throws SQLException on error
     * @see Statement#getResultSet()
     */
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }

    /**
     * Get parameter information.
     *
     * @return list with parameter metadata
     */
    public List<ParameterInfo> getParameterInfo() {
        initParameterInfo();
        return List.copyOf(indexMap.values());
    }

    /**
     * Get parameter information.
     *
     * @param name name of the parameter
     * @return parameter meta data
     */
    public Optional<ParameterInfo> getParameterInfo(String name) {
        initParameterInfo();
        return Optional.ofNullable(indexMap.get(name));
    }

    /* Some helper methods to set parameter values. */
    @FunctionalInterface
    private interface SetParameter<T> {
        void accept(int idx, T value) throws SQLException;
    }

    @FunctionalInterface
    private interface SetParameterInt<T> {
        void accept(int idx, @Nullable T value, int arg) throws SQLException;
    }

    @FunctionalInterface
    private interface SetParameterLong<T> {
        void accept(int idx, @Nullable T value, long arg) throws SQLException;
    }

    /**
     * A class holding parameter information.
     */
    public static class ParameterInfo {
        final String name;
        final List<Integer> indexes = new ArrayList<>();
        @Nullable
        JDBCType type;

        ParameterInfo(String name) {
            this.name = name;
            this.type = null;
        }

        void addIndex(int index) {
            indexes.add(index);
        }

        /**
         * Get parameter name.
         *
         * @return the parameter name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the JDBC type for this parameter (if supported by database/driver).
         *
         * @return the type of this parameter or JDBCType.JAVA_OBJECT if unknown
         */
        public JDBCType getType() {
            return type != null ? type : JDBCType.JAVA_OBJECT;
        }

        /**
         * Get the list of positional indexes for this parameter.
         *
         * @return positional indexes
         */
        public List<Integer> getIndexes() {
            return Collections.unmodifiableList(indexes);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s[%s] : %s", name, type, indexes);
        }
    }

}
