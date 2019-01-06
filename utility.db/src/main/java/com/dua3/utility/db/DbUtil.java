package com.dua3.utility.db;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.logging.Logger;

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
	
	public static Optional<Class<? extends java.sql.Driver>> loaDriver(URLClassLoader loader) throws ClassNotFoundException, IOException {
	    final String RESOURCE_PATH_TO_DRIVER_INFO = "/META-INF/services/java.sql.Driver";
	    
		// read JDBC driver class name from meta data
		Enumeration<URL> meta = loader.getResources(RESOURCE_PATH_TO_DRIVER_INFO);
        URL driverInfo = meta.hasMoreElements() ? meta.nextElement() : null;
        if (driverInfo==null) {
        	LOG.warning(RESOURCE_PATH_TO_DRIVER_INFO + " not found.");
        	LOG.warning("URLs: "+Arrays.toString(loader.getURLs()));
        	return Optional.empty();
        }
        if (meta.hasMoreElements()) {
        	LOG.warning("more than one entries found, which one gets loaded is undefined: "+RESOURCE_PATH_TO_DRIVER_INFO);
        	LOG.warning("URLs: "+Arrays.toString(loader.getURLs()));
        }
        String driverClassName = IOUtil.read(driverInfo, StandardCharsets.UTF_8).strip();
        
        // load the driver class
        Class<?> cls = loader.loadClass(driverClassName);
        
        if (!java.sql.Driver.class.isInstance(cls)) {
        	LOG.warning(cls.getName()+" does not implement java.sql.Driver: ");
        	return Optional.empty();
        }

        @SuppressWarnings("unchecked") // type is checked above
		Class<? extends java.sql.Driver> driver = (Class<? extends java.sql.Driver>) cls;

        LOG.info(() -> "loaded "+driver.getName());
        
        return Optional.of(driver);
	}

}
