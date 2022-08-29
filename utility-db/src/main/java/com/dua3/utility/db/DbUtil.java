// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.io.CsvReader;
import com.dua3.utility.io.CsvReader.ListRowBuilder;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Database utility class.
 */
public final class DbUtil {
    private DbUtil() {
        // utility class
    }

    /** Logger instance. */
    private static final Logger LOG = LoggerFactory.getLogger(DbUtil.class);

    /** List of know JDBC drivers. */
    private static final SortedMap<String, JdbcDriverInfo> drivers = new TreeMap<>();

    static {
        try {
            // load properties
            Map<Object,Object> p = LangUtil.loadProperties(DbUtil.class.getResourceAsStream("jdbc_drivers.properties"));
            
            // parse entries
            p.forEach((key1, value) -> {
                try {
                    ListRowBuilder rb = new ListRowBuilder();
                    CsvReader reader = CsvReader.create(
                            rb,
                            new BufferedReader(new StringReader(Objects.toString(value))),
                            Arguments.of(Arguments.createEntry(IoOptions.fieldSeparator(), ';')));
                    int n = reader.readSome(1);
                    assert n == 1;
                    List<String> data = rb.getRow();

                    String key = String.valueOf(key1);

                    final int expectedFields = 5;
                    LangUtil.check(
                            data.size() == expectedFields,
                            "invalid driver data for %s: expected %d fields, found %d", key, expectedFields, data.size());

                    String name = data.get(0).trim();
                    String className = data.get(1).trim();
                    String urlPrefix = data.get(2).trim();
                    String urlScheme = data.get(3).trim();
                    String link = data.get(4).trim();

                    JdbcDriverInfo di = new JdbcDriverInfo(name, className, urlPrefix, urlScheme, link);
                    JdbcDriverInfo rc = drivers.put(name, di);

                    LangUtil.check(rc == null, "duplicate entry for URL prefix %s", urlPrefix);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            LOG.warn("could not load JDBC driver data", e);
        }

    }

    /**
     * Get Map with known JDBC drivers.
     *
     * @return collection of DriverInfo instances for the known JDBC drivers
     */
    public static Collection<JdbcDriverInfo> getJdbcDrivers() {
        return drivers.values();
    }

    /**
     * Get {@link java.time.LocalDate} from SQL result.
     * {@link java.sql.ResultSet#getObject(String)} might return
     * either {@link java.sql.Date} or {@link LocalDate}. This method
     * returns an instance of {@link java.time.LocalDate} in both cases.
     *
     * @param  item
     *                               an Object representing a date in an SQL
     *                               ResultSet
     * @return
     *                               LocalDate instance or {@code null}
     * @throws IllegalStateException
     *                               if {@code item} is neither {@code null} nor of the supported types
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public static LocalDate toLocalDate(@Nullable Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof LocalDate localDate) {
            return localDate;
        }
        if (item instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalDate");
    }

    /**
     * Get {@link java.time.LocalDateTime} from SQL result.
     * {@link java.sql.ResultSet#getObject(String)} might return
     * either {@link java.sql.Timestamp} or {@link LocalDateTime}. This method
     * returns an instance of {@link java.time.LocalDateTime} in both cases.
     *
     * @param  item
     *                               an Object representing a timestamp in an SQL
     *                               ResultSet
     * @return
     *                               LocalDateTime instance or {@code null}
     * @throws IllegalStateException
     *                               if {@code item} is neither {@code null} nor of the supported types
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    public static LocalDateTime toLocalDateTime(@Nullable Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (item instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalDateTime");
    }

    /**
     * Get {@link java.time.LocalTime} from SQL result.
     * {@link java.sql.ResultSet#getObject(String)} might return
     * either {@link java.sql.Time} or {@link LocalTime}. This method
     * returns an instance of {@link java.time.LocalTime} in both cases.
     *
     * @param  item
     *                               an Object representing a time value in an SQL
     *                               ResultSet
     * @return
     *                               LocalTime instance or {@code null}
     * @throws IllegalStateException
     *                               if {@code item} is neither {@code null} nor of the supported types
     */
    public static LocalTime toLocalTime(@Nullable Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof LocalTime localTime) {
            return localTime;
        }
        if (item instanceof java.sql.Time time) {
            return time.toLocalTime();
        }
        throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalTime");
    }

    /**
     * Load JDBC driver.
     *
     * @param  urls
     *                                the URLs used to construct a ClassLoader
     *                                instance
     * @return
     *                                an Optional containing the driver object or an
     *                                empty Optional if not driver class is found
     * @throws ClassNotFoundException
     *                                if a driver class could be determined but
     *                                could not be loaded
     * @throws SQLException
     *                                if a driver class was found and loaded but
     *                                could not be instantiated
     */
    public static Optional<? extends java.sql.Driver> loadDriver(URL... urls)
            throws ClassNotFoundException, SQLException {
        LOG.atDebug().setMessage(() -> "loadDriver() - URLs: {}").addArgument(() -> Arrays.toString(urls)).log();
        return loadDriver(new URLClassLoader(urls));
    }

    /**
     * Load JDBC driver.
     * <p>
     * <strong>Note:</strong> the Driver returned can in general cannot be used with
     * `DriverManager.getConnection()`
     * because the `DriverManager` class checks that the class can be loaded from
     * the parent class loader which
     * is not the case. It can however be used directly or as driver for a
     * `JdbcDataSource`.
     *
     * @param  loader
     *                                the ClassLoader instance
     * @return
     *                                an Optional containing the driver object or an
     *                                empty Optional if not driver class is found on
     *                                the classpath
     * @throws ClassNotFoundException
     *                                if a driver class could be determined but
     *                                could not be loaded
     * @throws SQLException
     *                                if a driver class was found and loaded but
     *                                could not be instantiated
     */
    public static Optional<? extends java.sql.Driver> loadDriver(ClassLoader loader)
            throws ClassNotFoundException, SQLException {
        final String RESOURCE_PATH_TO_DRIVER_INFO = "META-INF/services/java.sql.Driver";

        try {
            // read JDBC driver class name from metadata
            Enumeration<URL> meta = loader.getResources(RESOURCE_PATH_TO_DRIVER_INFO);
            URL driverInfo = meta.hasMoreElements() ? meta.nextElement() : null;
            if (driverInfo == null) {
                LOG.warn(RESOURCE_PATH_TO_DRIVER_INFO + " not found");
                return Optional.empty();
            }
            if (meta.hasMoreElements()) {
                LOG.warn("more than one entries found, which one gets loaded is undefined: {}",
                        RESOURCE_PATH_TO_DRIVER_INFO);
            }
            String driverClassName = IoUtil.read(driverInfo, StandardCharsets.UTF_8).trim();

            // load the driver class
            Class<?> cls = loader.loadClass(driverClassName);

            if (!java.sql.Driver.class.isAssignableFrom(cls)) {
                LOG.warn("{} does not implement java.sql.Driver", cls.getName());
                return Optional.empty();
            }

            @SuppressWarnings("unchecked") // type is checked above
            Class<? extends java.sql.Driver> driverClass = (Class<? extends java.sql.Driver>) cls;

            LOG.debug("loaded driver class: {}", driverClass.getName());

            try {
                Driver driver = driverClass.getConstructor().newInstance();
                return Optional.of(driver);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SecurityException e) {
                throw new SQLException("could instantiate driver", e);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("IOException while trying to load driver", e);
        }
    }

    /**
     * Create DataSource.
     *
     * @param  driver
     *                      the driver to use
     * @param  url
     *                      the database URL
     * @param  user
     *                      the database user
     * @param  password
     *                      the database password
     * @return
     *                      DataSource instance
     * @throws SQLException
     *                      if the driver dies not accept the URL or connecting
     *                      fails
     */
    public static DataSource createDataSource(Driver driver, String url, String user, String password)
            throws SQLException {
        LangUtil.check(driver.acceptsURL(url), () -> new SQLException("URL not accepted by driver"));

        JdbcDataSource ds = new JdbcDataSource();
        ds.setDriver(driver);
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        return ds;
    }

}
