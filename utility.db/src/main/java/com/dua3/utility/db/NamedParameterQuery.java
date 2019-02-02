// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class NamedParameterQuery {

  /**
   * The parsed query.
   */
  private final String parsedQuery;
  /**
   * Maps parameter names to arrays of ints which are the parameter indices.
   */
  private final Map<String,List<Integer>> indexMap;

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
  public NamedParameterQuery(String query) throws SQLException {
    indexMap = new HashMap<>();
    parsedQuery = parse(query, indexMap);
  }

  /**
   * Parses a query with named parameters. The parameter-index mappings are put
   * into the map, and the parsed query is returned. DO NOT CALL FROM CLIENT
   * CODE. This method is non-private so JUnit code can test it.
   *
   * @param query
   *          query to parse
   * @param paramMap
   *          map to hold parameter-index mappings
   * @return the parsed query
   */
  private static final String parse(String query, Map<String, List<Integer>> paramMap) {
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

          List<Integer> indexList = paramMap.get(name);
          if (indexList==null) {
            indexList = new ArrayList<>();
            paramMap.put(name, indexList);
          }
          indexList.add(index);

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
   * @param name
   *          parameter name
   * @return parameter indexes
   * @throws IllegalArgumentException
   *           if the parameter does not exist
   */
  List<Integer> getIndexes(String name) {
    return Objects.requireNonNull(indexMap.get(name), "Unbekannter Parameter '"+name+"'.");
  }
  
  /**
   * Return the parsed query.
   * 
   * @return the parsed query
   */
  public String getParsedQuery() {
	return parsedQuery;
  }
}
