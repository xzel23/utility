// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 *
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
public class NamedParameterStatement implements AutoCloseable {
    /** Logger instance. */
    private static final Logger LOG = Logger.getLogger(NamedParameterStatement.class.getName());

    /** The statement this object is wrapping. */
    private final PreparedStatement statement;

    /** flag: has meta data been added to parameter info? */
    private boolean hasMeta = false;

    /**
     * A class holding parameter information.
     */
    public static class ParameterInfo {
        final String name;
        final List<Integer> indexes = new LinkedList<>();
        JDBCType type;

        ParameterInfo(String name) {
            this.name = name;
            this.type = null;
        }

        public void addIndex(int index) {
            indexes.add(index);
        }

        /**
         * Get parameter name.
         *
         * @return
         *         the parameter name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the JDBC type for this parameter (if supported by database/driver).
         *
         * @return
         *         the type of this parameter or JDBCType.JAVA_OBJECT if unknown
         */
        public JDBCType getType() {
            return type != null ? type : JDBCType.JAVA_OBJECT;
        }

        /**
         * Get the list of positional indexes for this parameter.
         *
         * @return
         *         positional indexes
         */
        public List<Integer> getIndexes() {
            return Collections.unmodifiableList(indexes);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT,"%s[%s] : %s", name, type, indexes);
        }
    }

    /**
     * Maps parameter names to arrays of ints which are the parameter indices.
     */
    private final Map<String, ParameterInfo> indexMap;

    /**
     * Creates a NamedParameterStatement. Wraps a call to
     * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
     *
     * @param  connection
     *                      the database connection
     * @param  query
     *                      the parameterized query
     * @throws SQLException
     *                      if the statement could not be created
     * @throws IllegalStateException
     *                      if the same parameter is used for different types
     */
    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap<>();
        String parsedQuery = parse(query, indexMap);
        statement = connection.prepareStatement(parsedQuery);
        addParameterMetInfo();
    }

    /**
     * Query parameter meta data.
     * This is done only once and the result is cached.
     * <strong>Warning:</strong> Not all databases support querying parameter meta data, and
     * some that do have serious bugs (like returning wrong data types), so check your database
     * manufacturer's documentation and test that you get the correct results when using this feature.
     * @throws UnsupportedOperationException
     *  if the database (driver) does not support querying parameter meta data
     * @throws IllegalStateException
     *  if the same variable is used in different places that require different datatypes
     * @throws SQLException
     *  if something else goes wrong
     */
    public void addParameterMetInfo() throws SQLException {
        if (hasMeta) {
            return;
        }

        ParameterMetaData meta = statement.getParameterMetaData();
        for (ParameterInfo param : indexMap.values()) {
            param.type = null;
            for (int index : param.indexes) {
                JDBCType type = getParameterType(meta, index);
                if (param.type != null && type != param.type) {
                    String msg = String.format(Locale.ROOT,"parameter type mismatch for parameter '%s': %s, %s", param.name,
                            param.type,
                            meta);
                    throw new IllegalStateException(msg);
                }
                param.type = type;
            }
        }

        hasMeta = true;
    }

    private static boolean showUnknownParameterTypeAsWarning = true;

    private static JDBCType getParameterType(ParameterMetaData meta, int index) {
        try {
            return JDBCType.valueOf(meta.getParameterType(index));
        } catch (SQLException e) {
            // same bug as
            // https://jira.spring.io/si/jira.issueviews:issue-html/SPR-13825/SPR-13825.html
            // [SPR-13825] Oracle 12c JDBC driver throws inconsistent exception from
            // getParameterType (affecting setNull calls)
            if (showUnknownParameterTypeAsWarning) {
                LOG.log(Level.WARNING, "Could not determine parameter types");
                showUnknownParameterTypeAsWarning = false;
            } else {
                LOG.log(Level.FINE, "(REPEAT) Could not determine parameter types");
            }
            return null;
        }
    }

    /**
     * Parses a query with named parameters. The parameter-index mappings are put
     * into the map, and the parsed query is returned. DO NOT CALL FROM CLIENT
     * CODE. This method is non-private so JUnit code can test it.
     *
     * @param  query
     *                  query to parse
     * @param  paramMap
     *                  map to hold parameter-index mappings
     * @return          the parsed query
     */
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
            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
            } else if (inDoubleQuote) {
                if (c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (c == ':' && i + 1 < length
                        && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
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
            parsedQuery.append(c);
        }

        return parsedQuery.toString();
    }

    /**
     * Returns the indexes for a parameter.
     *
     * @param  name
     *                                  parameter name
     * @return                          parameter indexes
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     */
    private List<Integer> getIndexes(String name) {
        return Objects.requireNonNull(indexMap.get(name), () -> "unknown parameter '" + name + "'.").indexes;
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setObject(int,
     *                                  java.lang.Object)
     */
    public void setObject(String name, Object value) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setObject(idx, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setString(int,
     *                                  java.lang.String)
     */
    public void setString(String name, String value) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setString(idx, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setInt(int, int)
     */
    public void setInt(String name, int value) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setInt(idx, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setInt(int, int)
     */
    public void setLong(String name, long value) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setLong(idx, value);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setTimestamp(int,
     *                                  java.sql.Timestamp)
     */
    public void setTimestamp(String name, Timestamp value) throws SQLException {
        for (int idx : getIndexes(name)) {
            statement.setTimestamp(idx, value);
        }
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
     * @return              true if the first result is a {@link ResultSet}
     * @throws SQLException
     *                      if an error occurred
     * @see                 PreparedStatement#execute()
     */
    public boolean execute() throws SQLException {
        return statement.execute();
    }

    /**
     * Executes the statement, which must be a query.
     *
     * @return              the query results
     * @throws SQLException
     *                      if an error occurred
     * @see                 PreparedStatement#executeQuery()
     */
    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    /**
     * Executes the statement, which must be an SQL INSERT, UPDATE or DELETE
     * statement; or an SQL statement that returns nothing, such as a DDL
     * statement.
     *
     * @return              number of rows affected
     * @throws SQLException
     *                      if an error occurred
     * @see                 PreparedStatement#executeUpdate()
     */
    public int executeUpdate() throws SQLException {
        return statement.executeUpdate();
    }

    /**
     * Closes the statement.
     *
     * @throws SQLException
     *                      if an error occurred
     * @see                 Statement#close()
     */
    @Override
    public void close() throws SQLException {
        statement.close();
    }

    /**
     * Adds the current set of parameters as a batch entry.
     *
     * @throws SQLException
     *                      if something went wrong
     */
    public void addBatch() throws SQLException {
        statement.addBatch();
    }

    /**
     * Executes all of the batched statements.
     * See {@link Statement#executeBatch()} for details.
     *
     * @return              update counts for each statement
     * @throws SQLException
     *                      if something went wrong
     */
    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setTimestamp(int,
     *                                  java.sql.Timestamp)
     */
    public void setLocalDate(String name, LocalDate value) throws SQLException {
        Date date = Date.valueOf(value);
        for (int idx : getIndexes(name)) {
            statement.setDate(idx, date);
        }
    }

    /**
     * Sets a parameter.
     *
     * @param  name
     *                                  parameter name
     * @param  value
     *                                  parameter value
     * @throws SQLException
     *                                  if an error occurred
     * @throws IllegalArgumentException
     *                                  if the parameter does not exist
     * @see                             PreparedStatement#setTimestamp(int,
     *                                  java.sql.Timestamp)
     */
    public void setLocalDateTime(String name, LocalDateTime value) throws SQLException {
        Timestamp t = Timestamp.valueOf(value);
        for (int idx : getIndexes(name)) {
            statement.setTimestamp(idx, t);
        }
    }

    /**
     * Get update count.
     *
     * @return              the update count
     * @throws SQLException on error
     * @see                 Statement#getUpdateCount()
     */
    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    /**
     * Get result set.
     *
     * @return              the result set
     * @throws SQLException on error
     * @see                 Statement#getResultSet()
     */
    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }

    /**
     * Get parameter information.
     *
     * @return
     *         list with parameter meta data
     */
    public List<ParameterInfo> getParameterInfo() {
        return Collections.unmodifiableList(new ArrayList<>(indexMap.values()));
    }

    /**
     * Get parameter information.
     *
     * @param  name
     *              name of the parameter
     * @return
     *              parameter meta data
     */
    public Optional<ParameterInfo> getParameterInfo(String name) {
        return Optional.ofNullable(indexMap.get(name));
    }

}
