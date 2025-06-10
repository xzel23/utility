// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.db;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.io.CsvReader;
import com.dua3.utility.io.CsvReader.ListRowBuilder;
import com.dua3.utility.io.IoOptions;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.WrappedException;
import com.dua3.utility.options.Arguments;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Database utility class.
 */
@SuppressWarnings("MagicCharacter")
public final class DbUtil {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(DbUtil.class);
    /**
     * List of know JDBC drivers.
     */
    private static final SortedMap<String, JdbcDriverInfo> drivers = new TreeMap<>();

    static {
        try {
            // load properties
            Map<Object, Object> p;
            String resource = "jdbc_drivers.properties";
            try (InputStream in = DbUtil.class.getResourceAsStream(resource)) {
                p = LangUtil.loadProperties(Objects.requireNonNull(in, "resource nott found: " + resource));
            }

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

    private DbUtil() { /* utility class private constructor */ }

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
     * @param item an Object representing a date in an SQL
     *             ResultSet
     * @return LocalDate instance or {@code null}
     * @throws IllegalStateException if {@code item} is neither {@code null} nor of the supported types
     */
    public static @Nullable LocalDate toLocalDate(@Nullable Object item) {
        return switch (item) {
            case null -> null;
            case LocalDate localDate -> localDate;
            case java.sql.Date date -> date.toLocalDate();
            default -> throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalDate");
        };
    }

    /**
     * Get {@link java.time.LocalDateTime} from SQL result.
     * {@link java.sql.ResultSet#getObject(String)} might return
     * either {@link java.sql.Timestamp} or {@link LocalDateTime}. This method
     * returns an instance of {@link java.time.LocalDateTime} in both cases.
     *
     * @param item an Object representing a timestamp in an SQL
     *             ResultSet
     * @return LocalDateTime instance or {@code null}
     * @throws IllegalStateException if {@code item} is neither {@code null} nor of the supported types
     */
    public static @Nullable LocalDateTime toLocalDateTime(@Nullable Object item) {
        return switch (item) {
            case null -> null;
            case LocalDateTime localDateTime -> localDateTime;
            case java.sql.Timestamp timestamp -> timestamp.toLocalDateTime();
            default ->
                    throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalDateTime");
        };
    }

    /**
     * Get {@link java.time.LocalTime} from SQL result.
     * {@link java.sql.ResultSet#getObject(String)} might return
     * either {@link java.sql.Time} or {@link LocalTime}. This method
     * returns an instance of {@link java.time.LocalTime} in both cases.
     *
     * @param item an Object representing a time value in an SQL
     *             ResultSet
     * @return LocalTime instance or {@code null}
     * @throws IllegalStateException if {@code item} is neither {@code null} nor of the supported types
     */
    public static @Nullable LocalTime toLocalTime(@Nullable Object item) {
        return switch (item) {
            case null -> null;
            case LocalTime localTime -> localTime;
            case java.sql.Time time -> time.toLocalTime();
            default -> throw new IllegalStateException(item.getClass().getName() + " cannot be converted to LocalTime");
        };
    }

    /**
     * Load JDBC driver.
     *
     * @param urls the URLs used to construct a ClassLoader
     *             instance
     * @return an Optional containing the driver object or an
     * empty Optional if not driver class is found
     * @throws ClassNotFoundException if a driver class could be determined but
     *                                could not be loaded
     * @throws SQLException           if a driver class was found and loaded but
     *                                could not be instantiated
     */
    public static Optional<java.sql.Driver> loadDriver(URL... urls)
            throws ClassNotFoundException, SQLException {
        LOG.debug("loadDriver() - URLs: {}", (Object) urls);
        return loadDriver(new URLClassLoader(urls));
    }

    /**
     * Load JDBC driver.
     * <p>
     * <strong>Note:</strong> the Driver returned can in general not be used with
     * `DriverManager.getConnection()` because the `DriverManager` class checks that
     * the class can be loaded from the parent class loader which is not the case.
     * It can however be used directly or as driver for a `JdbcDataSource`.
     *
     * @param loader the ClassLoader instance
     * @return an Optional containing the driver object or an
     * empty Optional if not driver class is found on
     * the classpath
     * @throws ClassNotFoundException if a driver class could be determined but
     *                                could not be loaded
     * @throws SQLException           if a driver class was found and loaded but
     *                                could not be instantiated
     */
    public static Optional<java.sql.Driver> loadDriver(ClassLoader loader)
            throws ClassNotFoundException, SQLException {
        final String RESOURCE_PATH_TO_DRIVER_INFO = "META-INF/services/java.sql.Driver";

        try {
            // read JDBC driver class name from metadata
            Enumeration<URL> meta = loader.getResources(RESOURCE_PATH_TO_DRIVER_INFO);
            URL driverInfo = meta.hasMoreElements() ? meta.nextElement() : null;
            if (driverInfo == null) {
                LOG.warn("not found: {}", RESOURCE_PATH_TO_DRIVER_INFO);
                return Optional.empty();
            }
            if (meta.hasMoreElements()) {
                LOG.warn("more than one entry found, which one gets loaded is undefined: {}", RESOURCE_PATH_TO_DRIVER_INFO);
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

            return getDriver(driverClass);
        } catch (IOException e) {
            throw new ClassNotFoundException("IOException while trying to load driver", e);
        }
    }

    /**
     * Instantiates and returns an instance of the given JDBC driver class.
     *
     * @param driverClass the class of the JDBC driver to be instantiated
     * @return an {@code Optional} containing the instantiated driver, or an empty {@code Optional} if an error occurs
     * @throws SQLException if the driver class cannot be instantiated due to reflection-related issues
     */
    private static Optional<Driver> getDriver(Class<? extends Driver> driverClass) throws SQLException {
        try {
            Driver driver = driverClass.getConstructor().newInstance();
            return Optional.of(driver);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | SecurityException e) {
            throw new SQLException("could instantiate driver", e);
        }
    }

    /**
     * Create DataSource.
     *
     * @param driver   the driver to use
     * @param url      the database URL
     * @param user     the database user
     * @param password the database password
     * @return DataSource instance
     * @throws SQLException if the driver does not accept the URL or connecting
     *                      fails
     */
    public static DataSource createDataSource(Driver driver, String url, String user, String password)
            throws SQLException {
        LangUtil.check(driver.acceptsURL(url), () -> new SQLException("URL not accepted by driver"));

        JdbcDataSource ds = new JdbcDataSource(driver);
        ds.setUrl(url);
        ds.setUser(user);
        ds.setPassword(password);
        return ds;
    }

    @FunctionalInterface
    private interface UncheckedCloser extends AutoCloseable {
        default void doClose() {
            try {
                close();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new WrappedException(e);
            }
        }

        static UncheckedCloser wrap(AutoCloseable c) {
            return c::close;
        }

        default UncheckedCloser nest(AutoCloseable c) {
            return () -> {
                try (UncheckedCloser unused = this) {
                    c.close();
                }
            };
        }
    }

    /**
     * Create a stream of objects from a {@link ResultSet}.
     * <p>
     * <strong>IMPORTANT:</strong> do not close the database or the result set before all stream items are consumed.
     * The result set will be automatically closed together with the stream. If more resources have to be closed,
     * pass them in the {@code closables} parameter; the items given in {@code closeables} will be closed in reversed
     * order (right to left) before closing the result set.
     *
     * @param rs the ResultSet
     * @param mapper mapping function to convert the current result item to the object type
     * @param closeables additional {@link AutoCloseable} instances to close when the stream is closed
     * @return stream of objects created from the result set items
     * @param <T> stream item type
     * @throws SQLException if an SQL exception occurrs
     */
    public static <T> Stream<T> stream(ResultSet rs, Function<? super ResultSet, ? extends T> mapper, AutoCloseable... closeables) throws SQLException {
        UncheckedCloser closer = rs::close;
        for (AutoCloseable closeable : closeables) {
            closer = closer.nest(closeable);
        }
        try {
            return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(
                    Long.MAX_VALUE, Spliterator.ORDERED) {
                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    try {
                        if (!rs.next()) return false;
                        action.accept(mapper.apply(rs));
                        return true;
                    } catch (SQLException ex) {
                        throw new WrappedException(ex);
                    }
                }
            }, false).onClose(closer::doClose);
        } catch (WrappedException e) {
            switch (e.getCause()) {
                case SQLException sqlEx -> throw sqlEx;
                case IOException ioEx -> throw new UncheckedIOException(ioEx);
                default -> throw e;
            }
        }
    }
}
