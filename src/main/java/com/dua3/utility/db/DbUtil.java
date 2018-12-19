package com.dua3.utility.db;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DbUtil {
	
	public static LocalDate toLocalDate(Object item) {
		if (item==null) 
			return null;
		if (item instanceof LocalDate)
			return (LocalDate)item;
		if (item instanceof java.util.Date)
			return ((java.sql.Date)item).toLocalDate();
		throw new IllegalStateException(item.getClass().getName()+" cannot be converted to LocalDate");
	}
	
	public static LocalDateTime toLocalDateTime(Object item) {
		if (item==null) 
			return null;
		if (item instanceof LocalDateTime)
			return (LocalDateTime)item;
		if (item instanceof java.util.Date)
			return ((java.sql.Timestamp)item).toLocalDateTime();
		throw new IllegalStateException(item.getClass().getName()+" cannot be converted to LocalDateTime");
	}
	
}
