// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * Code was taken from the Java World article "Named Parameters for PreparedStatement"
 * and adapted to work with up-to-date Java versions.
 * </p>
 *
 * Original comment:
 * <br>
 *
 * This class wraps around a {@link PreparedStatement} and allows the programmer
 * to set parameters by name instead of by index. This eliminates any confusion
 * as to which parameter index represents what. This also means that rearranging
 * the SQL statement or adding a parameter doesn't involve renumbering your
 * indices. Code such as this:
 *
 * <pre>
 * {@code
 *   Connection con=getConnection();
 *   String query="select * from my_table where name=? or address=?";
 *   PreparedStatement p=con.prepareStatement(query);
 *   p.setString(1, "bob");
 *   p.setString(2, "123 terrace ct");
 *   ResultSet rs=p.executeQuery();
 * }
 * </pre>
 *
 * can be replaced with:
 *
 * <pre>
 * {@code
 *   Connection con=getConnection();
 *   String query="select * from my_table where name=:name or address=:address";
 *   NamedParameterStatement p=new NamedParameterStatement(con, query);
 *   p.setString("name", "bob");
 *   p.setString("address", "123 terrace ct");
 *   ResultSet rs=p.executeQuery();
 * }
 * </pre>
 *
 * @author adam_crume
 * @author Axel Howind
 */
public class NamedParameterStatement implements AutoCloseable {
  /** The statement this object is wrapping. */
  private final PreparedStatement statement;

  /**
   * Maps parameter names to arrays of ints which are the parameter indices.
   */
  private final NamedParameterQuery query;

  /**
   * Creates a NamedParameterStatement. Wraps a call to
   * c.{@link Connection#prepareStatement(java.lang.String) prepareStatement}.
   *
   * @param connection
   *          the database connection
   * @param query
   *          the parameterized query
   * @throws SQLException
   *           if the statement could not be created
   */
  public NamedParameterStatement(Connection connection, String query) throws SQLException {
	this(connection, new NamedParameterQuery(query));
  }

  public NamedParameterStatement(Connection connection, NamedParameterQuery query) throws SQLException {
	this.query = query;
	this.statement = connection.prepareStatement(query.getParsedQuery());
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setObject(int, java.lang.Object)
   */
  public void setObject(String name, Object value) throws SQLException {
    for (int idx: query.getIndexes(name)) {
      statement.setObject(idx, value);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setString(int, java.lang.String)
   */
  public void setString(String name, String value) throws SQLException {
    for (int idx: query.getIndexes(name)) {
      statement.setString(idx, value);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setInt(int, int)
   */
  public void setInt(String name, int value) throws SQLException {
    for (int idx: query.getIndexes(name)) {
      statement.setInt(idx, value);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setInt(int, int)
   */
  public void setLong(String name, long value) throws SQLException {
    for (int idx: query.getIndexes(name)) {
      statement.setLong(idx, value);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  public void setTimestamp(String name, Timestamp value) throws SQLException {
    for (int idx: query.getIndexes(name)) {
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
   * @return true if the first result is a {@link ResultSet}
   * @throws SQLException
   *           if an error occurred
   * @see PreparedStatement#execute()
   */
  public boolean execute() throws SQLException {
    return statement.execute();
  }

  /**
   * Executes the statement, which must be a query.
   *
   * @return the query results
   * @throws SQLException
   *           if an error occurred
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
   * @throws SQLException
   *           if an error occurred
   * @see PreparedStatement#executeUpdate()
   */
  public int executeUpdate() throws SQLException {
    return statement.executeUpdate();
  }

  /**
   * Closes the statement.
   *
   * @throws SQLException
   *           if an error occurred
   * @see Statement#close()
   */
  @Override
  public void close() throws SQLException {
    statement.close();
  }

  /**
   * Adds the current set of parameters as a batch entry.
   *
   * @throws SQLException
   *           if something went wrong
   */
  public void addBatch() throws SQLException {
    statement.addBatch();
  }

  /**
   * Executes all of the batched statements.
   *
   * See {@link Statement#executeBatch()} for details.
   *
   * @return update counts for each statement
   * @throws SQLException
   *           if something went wrong
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
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  public void setLocalDate(String name, LocalDate value) throws SQLException {
    Date date = Date.valueOf(value);
    for (int idx: query.getIndexes(name)) {
      statement.setDate(idx, date);
    }
  }

  /**
   * Sets a parameter.
   *
   * @param name
   *          parameter name
   * @param value
   *          parameter value
   * @throws SQLException
   *           if an error occurred
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   * @see PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  public void setLocalDateTime(String name, LocalDateTime value) throws SQLException {
    Timestamp t = Timestamp.valueOf(value);
    for (int idx: query.getIndexes(name)) {
      statement.setTimestamp(idx, t);
    }
  }

}
