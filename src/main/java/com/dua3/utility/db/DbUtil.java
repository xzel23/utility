package com.dua3.utility.db;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DbUtil {

	/**
	 * Get {@link java.time.LocalDate} from SQL result.
	 * {@link java.sql.ResultSet#getObject(String)} might return
	 * either {@link java.sql.Date} or {@link LocalDate}. This method
	 * returns an instance of {@link java.time.LocalDate} in both cases.
	 * @param item 
	 *  an Object representating a date in an SQL ResultSet
	 * @return
	 *  LocalDate instance or {@code null}
	 * @throws IllegalStateException
	 *  if {@code item} is neither {@code null} nor of any of the supported types
	 */
	public static LocalDate toLocalDate(Object item) {
		if (item==null) 
			return null;
		if (item instanceof LocalDate)
			return (LocalDate)item;
		if (item instanceof java.sql.Date)
			return ((java.sql.Date)item).toLocalDate();
		throw new IllegalStateException(item.getClass().getName()+" cannot be converted to LocalDate");
	}
	
	/**
	 * Get {@link java.time.LocalDateTime} from SQL result.
	 * {@link java.sql.ResultSet#getObject(String)} might return
	 * either {@link java.sql.Timestamp} or {@link LocalDateTime}. This method
	 * returns an instance of {@link java.time.LocalDateTime} in both cases.
	 * @param item 
	 *  an Object representating a timestamp in an SQL ResultSet
	 * @return
	 *  LocalDateTime instance or {@code null}
	 * @throws IllegalStateException
	 *  if {@code item} is neither {@code null} nor of any of the supported types
	 */
	public static LocalDateTime toLocalDateTime(Object item) {
		if (item==null) 
			return null;
		if (item instanceof LocalDateTime)
			return (LocalDateTime)item;
		if (item instanceof java.sql.Timestamp)
			return ((java.sql.Timestamp)item).toLocalDateTime();
		throw new IllegalStateException(item.getClass().getName()+" cannot be converted to LocalDateTime");
	}
	
}
