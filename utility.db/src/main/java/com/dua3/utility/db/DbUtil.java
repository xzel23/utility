// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.dua3.utility.io.IOUtil;

public class DbUtil {
	private DbUtil() {
		// utility class
	}
	
	/** Logger instance. */
	private static final Logger LOG = Logger.getLogger(DbUtil.class.getName());

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
	
	/**
	 * Get {@link java.time.LocalTime} from SQL result.
	 * {@link java.sql.ResultSet#getObject(String)} might return
	 * either {@link java.sql.Time} or {@link LocalTime}. This method
	 * returns an instance of {@link java.time.LocalTime} in both cases.
	 * @param item 
	 *  an Object representating a time value in an SQL ResultSet
	 * @return
	 *  LocalTime instance or {@code null}
	 * @throws IllegalStateException
	 *  if {@code item} is neither {@code null} nor of any of the supported types
	 */
	public static LocalTime toLocalTime(Object item) {
		if (item==null) 
			return null;
		if (item instanceof LocalTime)
			return (LocalTime) item;
		if (item instanceof java.sql.Time)
			return ((java.sql.Time)item).toLocalTime();
		throw new IllegalStateException(item.getClass().getName()+" cannot be converted to LocalDate");
	}
	
	/**
	 * Load JDBC driver.
	 * 
	 * @param urls
	 *  the URLs used to construct a ClassLoader instance
	 * @return
	 *  an Optional containing the driver object or an empty Optional if not driver class is found
	 * @throws ClassNotFoundException
	 *  if a driver class could be determined but could not be loaded
	 * @throws SQLException
	 *  if a driver class was found and loaded but could not be instantiated
	 */
	public static Optional<? extends java.sql.Driver> loadDriver(URL... urls) throws ClassNotFoundException, SQLException {
		LOG.fine(() -> "loadDriver() - URLs: "+Arrays.toString(urls));
		try {
			PrivilegedExceptionAction<Optional<? extends java.sql.Driver>> action = 
				() -> loadDriver(new URLClassLoader(urls)); 
			return AccessController.doPrivileged(action);
		} catch (PrivilegedActionException e) {
			Exception ee = e.getException();
			if (ee instanceof ClassNotFoundException) {
				throw (ClassNotFoundException) ee;
			}
			if (ee instanceof SQLException) {
				throw (SQLException) ee;
			}
			LOG.log(Level.WARNING, "unexpected exception thrown in doPrivileged block", e);
			throw new IllegalStateException(ee);
		}
	}

	/**
	 * Load JDBC driver.
	 * <p>
	 * <strong>Note:</strong> the Driver returned can in general cannot be used with `DriverManager.getConnection()`
	 * because the `DriverManager` class checks that the class can be loaded from the parent class loader which
	 * is not the case. It can however be used directly or as driver for a `JdbcDataSource`.
	 * 
	 * @param loader
	 *  the ClassLoader instance
	 * @return
	 *  an Optional containing the driver object or an empty Optional if not driver class is found on the classpath
	 * @throws ClassNotFoundException
	 *  if a driver class could be determined but could not be loaded
	 * @throws SQLException
	 *  if a driver class was found and loaded but could not be instantiated
	 */
	public static Optional<? extends java.sql.Driver> loadDriver(ClassLoader loader) throws ClassNotFoundException, SQLException {
	    final String RESOURCE_PATH_TO_DRIVER_INFO = "META-INF/services/java.sql.Driver";
	    	    
	    try {
			// read JDBC driver class name from meta data
			Enumeration<URL> meta = loader.getResources(RESOURCE_PATH_TO_DRIVER_INFO);
	        URL driverInfo = meta.hasMoreElements() ? meta.nextElement() : null;
	        if (driverInfo==null) {
	        	LOG.warning(RESOURCE_PATH_TO_DRIVER_INFO + " not found.");
	        	return Optional.empty();
	        }
	        if (meta.hasMoreElements()) {
	        	LOG.warning("more than one entries found, which one gets loaded is undefined: "+RESOURCE_PATH_TO_DRIVER_INFO);
	        }
	        String driverClassName = IOUtil.read(driverInfo, StandardCharsets.UTF_8).strip();
	        
	        // load the driver class
	        Class<?> cls = loader.loadClass(driverClassName);
	        
	        if (!java.sql.Driver.class.isAssignableFrom(cls)) {
	        	LOG.warning(cls.getName()+" does not implement java.sql.Driver: ");
	        	return Optional.empty();
	        }
	
	        @SuppressWarnings("unchecked") // type is checked above
			Class<? extends java.sql.Driver> driverClass = (Class<? extends java.sql.Driver>) cls;
	
	        LOG.fine(() -> "loaded driver class "+driverClass.getName());
	        
	        try {	
	        	Driver driver = driverClass.getConstructor().newInstance();
	        	return Optional.of(driver);
	        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
	        	throw new SQLException("could instantiate driver", e);
	        }	        
	    } catch (IOException e) {
	    	throw new ClassNotFoundException("IOException while trying to load driver", e);
	    }
	}

	/**
	 * Create DataSource.
	 * 
	 * @param driver
	 *  the driver to use
	 * @param url
	 *  the database URL
	 * @param user
	 *  the database user
	 * @param password
	 *  the database password
	 * @return
	 *  DataSource instance
	 * @throws SQLException
	 *  if the driver dies not accept the URL or connecting fails
	 */
	public static DataSource createDataSource(Driver driver, String url , String user, String password) throws SQLException {
		if (!driver.acceptsURL(url)) {
			throw new SQLException("URL not accepted by driver");
		}
		
		JdbcDataSource ds = new JdbcDataSource();
		ds.setDriver(driver);
		ds.setUrl(url);
		ds.setUser(user);
		ds.setPassword(password);
		return ds;
	}
    
}
