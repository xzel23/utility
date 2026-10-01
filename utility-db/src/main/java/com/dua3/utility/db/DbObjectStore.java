/*
 * Copyright (c) 2026. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.db;

import com.dua3.utility.io.AbsolutePathException;
import com.dua3.utility.io.FolderNotEmptyException;
import com.dua3.utility.io.IllegalPathException;
import com.dua3.utility.io.NotADataObjectException;
import com.dua3.utility.io.NotAFolderException;
import com.dua3.utility.io.ObjectExistsException;
import com.dua3.utility.io.ObjectNotFoundException;
import com.dua3.utility.io.ObjectStore;
import com.dua3.utility.io.ReadableObjectStore;
import com.dua3.utility.io.WritableObjectStore;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An implementation of {@link ObjectStore} that stores objects in a relational database table using
 * an adjacency-list hierarchy layout.
 */
public class DbObjectStore implements ObjectStore {

    /**
     * Default table name used when no custom table name is provided.
     */
    public static final String DEFAULT_TABLE_NAME = "object_store";

    /**
     * Functional interface for supplying database connections with checked {@link SQLException}.
     */
    @FunctionalInterface
    public interface ConnectionSupplier {
        /**
         * Obtains a connection.
         *
         * @return a database connection
         * @throws SQLException if a database access error occurs
         */
        Connection get() throws SQLException;
    }

    private record DbNode(
            long id,
            @Nullable Long parentId,
            String name,
            ObjectType type,
            long size,
            Instant created,
            Instant lastModified
    ) {}

    private final URI root;
    private final Connection connection;
    private final String tableName;
    private final AccessMode accessMode;
    private volatile boolean closed = false;

    private final PreparedStatement stmtFindRoot;
    private final PreparedStatement stmtFindChild;
    private final PreparedStatement stmtInsertFolder;
    private final PreparedStatement stmtInsertData;
    private final PreparedStatement stmtUpdateData;
    private final PreparedStatement stmtDelete;
    private final PreparedStatement stmtFindChildrenIds;
    private final PreparedStatement stmtHasChildren;
    private final PreparedStatement stmtGetData;
    private final PreparedStatement stmtListChildren;
    private final PreparedStatement stmtGetNextId;

    /**
     * Constructs a new {@code DbObjectStore} using a {@link Connection}.
     *
     * @param root       the root URI
     * @param connection the database connection
     * @param tableName  the table name
     * @param accessMode the access mode
     * @throws IOException if table verification, creation, or statement preparation fails
     */
    @SuppressWarnings("java:S2077")
    public DbObjectStore(
            URI root,
            Connection connection,
            String tableName,
            AccessMode accessMode
    ) throws IOException {
        LangUtil.check(root.isAbsolute(), "Root URI must be absolute: %s", root);
        LangUtil.check(tableName.matches("[a-z_][a-z0-9_]*"), "Invalid table name: %s", tableName);

        String rootStr = root.toString();
        this.root = rootStr.endsWith("/") ? root : URI.create(rootStr + "/");
        this.connection = Objects.requireNonNull(connection, "connection");
        this.tableName = tableName;
        this.accessMode = Objects.requireNonNull(accessMode, "accessMode");

        try {
            ensureTableExists();

            this.stmtFindRoot = connection.prepareStatement(
                    "SELECT id, parent_id, name, type, size, created, last_modified FROM " + tableName + " WHERE parent_id IS NULL AND name = ''");
            this.stmtFindChild = connection.prepareStatement(
                    "SELECT id, parent_id, name, type, size, created, last_modified FROM " + tableName + " WHERE parent_id = ? AND name = ?");
            this.stmtInsertFolder = connection.prepareStatement(
                    "INSERT INTO " + tableName + " (id, parent_id, name, type, size, created, last_modified, data) VALUES (?, ?, ?, 'F', NULL, ?, ?, NULL)");
            this.stmtInsertData = connection.prepareStatement(
                    "INSERT INTO " + tableName + " (id, parent_id, name, type, size, created, last_modified, data) VALUES (?, ?, ?, 'D', ?, ?, ?, ?)");
            this.stmtUpdateData = connection.prepareStatement(
                    "UPDATE " + tableName + " SET size = ?, last_modified = ?, data = ? WHERE id = ?");
            this.stmtDelete = connection.prepareStatement(
                    "DELETE FROM " + tableName + " WHERE id = ?");
            this.stmtFindChildrenIds = connection.prepareStatement(
                    "SELECT id FROM " + tableName + " WHERE parent_id = ?");
            this.stmtHasChildren = connection.prepareStatement(
                    "SELECT 1 FROM " + tableName + " WHERE parent_id = ?");
            this.stmtGetData = connection.prepareStatement(
                    "SELECT data FROM " + tableName + " WHERE id = ?");
            this.stmtListChildren = connection.prepareStatement(
                    "SELECT id, parent_id, name, type, size, created, last_modified FROM " + tableName + " WHERE parent_id = ? ORDER BY name ASC");
            this.stmtGetNextId = connection.prepareStatement(
                    "SELECT COALESCE(MAX(id), 0) + 1 FROM " + tableName);

            if (accessMode != AccessMode.READ) {
                ensureRootExists();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to initialize database object store for table " + tableName, e);
        }
    }

    /**
     * Constructs a new {@code DbObjectStore} using a {@link ConnectionSupplier}.
     *
     * @param root               the root URI
     * @param connectionSupplier supplier for database connections
     * @param tableName          the table name
     * @param accessMode         the access mode
     * @throws IOException if table verification, creation, or statement preparation fails
     */
    public DbObjectStore(
            URI root,
            ConnectionSupplier connectionSupplier,
            String tableName,
            AccessMode accessMode
    ) throws IOException {
        this(root, obtainConnection(connectionSupplier), tableName, accessMode);
    }

    private static Connection obtainConnection(ConnectionSupplier supplier) throws IOException {
        Objects.requireNonNull(supplier, "connectionSupplier");
        try {
            Connection conn = supplier.get();
            if (conn == null) {
                throw new IOException("Connection supplier returned null");
            }
            return conn;
        } catch (SQLException e) {
            throw new IOException("Failed to obtain database connection", e);
        }
    }

    /**
     * Creates a default root URI for a given table name.
     *
     * @param tableName the table name
     * @return the default root URI
     */
    public static URI defaultRootUri(String tableName) {
        return URI.create("db://localhost/" + tableName + "/");
    }

    // Factory methods for Connection

    /**
     * Creates a new read-write object store using the specified connection and default table name.
     *
     * @param connection the database connection
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(Connection connection) throws IOException {
        return newObjectStore(connection, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-write object store using the specified connection and table name.
     *
     * @param connection the database connection
     * @param tableName  the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(Connection connection, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connection, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-write object store.
     *
     * @param root       the root URI
     * @param connection the database connection
     * @param tableName  the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(URI root, Connection connection, String tableName) throws IOException {
        return new DbObjectStore(root, connection, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-only object store using the specified connection and default table name.
     *
     * @param connection the database connection
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(Connection connection) throws IOException {
        return newReadableObjectStore(connection, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-only object store using the specified connection and table name.
     *
     * @param connection the database connection
     * @param tableName  the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(Connection connection, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connection, tableName, AccessMode.READ);
    }

    /**
     * Creates a new read-only object store.
     *
     * @param root       the root URI
     * @param connection the database connection
     * @param tableName  the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(URI root, Connection connection, String tableName) throws IOException {
        return new DbObjectStore(root, connection, tableName, AccessMode.READ);
    }

    /**
     * Creates a new write-only object store using the specified connection and default table name.
     *
     * @param connection the database connection
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(Connection connection) throws IOException {
        return newWritableObjectStore(connection, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new write-only object store using the specified connection and table name.
     *
     * @param connection the database connection
     * @param tableName  the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(Connection connection, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connection, tableName, AccessMode.WRITE);
    }

    /**
     * Creates a new write-only object store.
     *
     * @param root       the root URI
     * @param connection the database connection
     * @param tableName  the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(URI root, Connection connection, String tableName) throws IOException {
        return new DbObjectStore(root, connection, tableName, AccessMode.WRITE);
    }

    /**
     * Creates a new object store.
     *
     * @param root       the root URI
     * @param connection the database connection
     * @param tableName  the table name
     * @param accessMode the access mode
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore create(URI root, Connection connection, String tableName, AccessMode accessMode) throws IOException {
        return new DbObjectStore(root, connection, tableName, accessMode);
    }

    // Factory methods for DataSource

    /**
     * Creates a new read-write object store using the specified data source and default table name.
     *
     * @param dataSource the data source
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(DataSource dataSource) throws IOException {
        return newObjectStore(dataSource, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-write object store using the specified data source and table name.
     *
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), dataSource::getConnection, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-write object store.
     *
     * @param root       the root URI
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(URI root, DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(root, dataSource::getConnection, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-only object store using the specified data source and default table name.
     *
     * @param dataSource the data source
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(DataSource dataSource) throws IOException {
        return newReadableObjectStore(dataSource, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-only object store using the specified data source and table name.
     *
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), dataSource::getConnection, tableName, AccessMode.READ);
    }

    /**
     * Creates a new read-only object store.
     *
     * @param root       the root URI
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(URI root, DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(root, dataSource::getConnection, tableName, AccessMode.READ);
    }

    /**
     * Creates a new write-only object store using the specified data source and default table name.
     *
     * @param dataSource the data source
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(DataSource dataSource) throws IOException {
        return newWritableObjectStore(dataSource, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new write-only object store using the specified data source and table name.
     *
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), dataSource::getConnection, tableName, AccessMode.WRITE);
    }

    /**
     * Creates a new write-only object store.
     *
     * @param root       the root URI
     * @param dataSource the data source
     * @param tableName  the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(URI root, DataSource dataSource, String tableName) throws IOException {
        return new DbObjectStore(root, dataSource::getConnection, tableName, AccessMode.WRITE);
    }

    // Factory methods for ConnectionSupplier

    /**
     * Creates a new read-write object store using the specified connection supplier and default table name.
     *
     * @param connectionSupplier the connection supplier
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(ConnectionSupplier connectionSupplier) throws IOException {
        return newObjectStore(connectionSupplier, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-write object store using the specified connection supplier and table name.
     *
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connectionSupplier, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-write object store.
     *
     * @param root               the root URI
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(URI root, ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(root, connectionSupplier, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new read-only object store using the specified connection supplier and default table name.
     *
     * @param connectionSupplier the connection supplier
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(ConnectionSupplier connectionSupplier) throws IOException {
        return newReadableObjectStore(connectionSupplier, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-only object store using the specified connection supplier and table name.
     *
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connectionSupplier, tableName, AccessMode.READ);
    }

    /**
     * Creates a new read-only object store.
     *
     * @param root               the root URI
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the readable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static ReadableObjectStore newReadableObjectStore(URI root, ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(root, connectionSupplier, tableName, AccessMode.READ);
    }

    /**
     * Creates a new write-only object store using the specified connection supplier and default table name.
     *
     * @param connectionSupplier the connection supplier
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(ConnectionSupplier connectionSupplier) throws IOException {
        return newWritableObjectStore(connectionSupplier, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new write-only object store using the specified connection supplier and table name.
     *
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(defaultRootUri(tableName), connectionSupplier, tableName, AccessMode.WRITE);
    }

    /**
     * Creates a new write-only object store.
     *
     * @param root               the root URI
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the writable object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static WritableObjectStore newWritableObjectStore(URI root, ConnectionSupplier connectionSupplier, String tableName) throws IOException {
        return new DbObjectStore(root, connectionSupplier, tableName, AccessMode.WRITE);
    }

    // Factory methods for java.util.function.Supplier<Connection>

    /**
     * Creates a new read-write object store using the specified connection supplier and default table name.
     *
     * @param connectionSupplier the connection supplier
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(Supplier<Connection> connectionSupplier) throws IOException {
        return newObjectStore(connectionSupplier, DEFAULT_TABLE_NAME);
    }

    /**
     * Creates a new read-write object store using the specified connection supplier and table name.
     *
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore newObjectStore(Supplier<Connection> connectionSupplier, String tableName) throws IOException {
        ConnectionSupplier supplier = connectionSupplier::get;
        return new DbObjectStore(defaultRootUri(tableName), supplier, tableName, AccessMode.READ_AND_WRITE);
    }

    /**
     * Creates a new object store.
     *
     * @param root               the root URI
     * @param connectionSupplier the connection supplier
     * @param tableName          the table name
     * @param accessMode         the access mode
     * @return the object store
     * @throws IOException if an I/O or SQL error occurs
     */
    public static DbObjectStore create(URI root, ConnectionSupplier connectionSupplier, String tableName, AccessMode accessMode) throws IOException {
        return new DbObjectStore(root, connectionSupplier, tableName, accessMode);
    }

    @Override
    public AccessMode getAccessMode() {
        return accessMode;
    }

    @Override
    public URI getRoot() {
        return root;
    }

    @Override
    public synchronized void createFolder(URI path) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            return;
        }

        boolean origAutoCommit = false;
        try {
            origAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String parentPath = getParentPath(normalizedPath);
                DbNode parent = ensureParentFoldersExist(parentPath);
                String name = getFileName(normalizedPath);
                DbNode existing = findChild(parent.id(), name);
                if (existing != null) {
                    if (existing.type() != ObjectType.FOLDER) {
                        throw new NotAFolderException("Path is not a folder: " + path);
                    }
                } else {
                    long newId = getNextId();
                    Timestamp now = Timestamp.from(Instant.now());
                    stmtInsertFolder.setLong(1, newId);
                    stmtInsertFolder.setLong(2, parent.id());
                    stmtInsertFolder.setString(3, name);
                    stmtInsertFolder.setTimestamp(4, now);
                    stmtInsertFolder.setTimestamp(5, now);
                    stmtInsertFolder.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IOException ioe) throw ioe;
                if (e instanceof SQLException sqle) throw new IOException(sqle.getMessage(), sqle);
                throw new IOException(e);
            } finally {
                connection.setAutoCommit(origAutoCommit);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void removeFolder(URI path) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);

        boolean origAutoCommit = false;
        try {
            origAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                DbNode target = findNode(normalizedPath);
                if (target == null) {
                    throw new ObjectNotFoundException("Object not found: " + path);
                }
                if (target.type() != ObjectType.FOLDER) {
                    throw new NotAFolderException("Not a folder: " + path);
                }
                if (target.parentId() == null) {
                    if (hasChildren(target.id())) {
                        throw new FolderNotEmptyException("Folder is not empty: " + path);
                    }
                    throw new IOException("Cannot remove root folder");
                }

                if (hasChildren(target.id())) {
                    throw new FolderNotEmptyException("Folder is not empty: " + path);
                }

                stmtDelete.setLong(1, target.id());
                stmtDelete.executeUpdate();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IOException ioe) throw ioe;
                if (e instanceof SQLException sqle) throw new IOException(sqle.getMessage(), sqle);
                throw new IOException(e);
            } finally {
                connection.setAutoCommit(origAutoCommit);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void delete(URI path) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new IllegalArgumentException("Cannot delete root");
        }

        boolean origAutoCommit = false;
        try {
            origAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                DbNode target = findNode(normalizedPath);
                if (target == null) {
                    throw new ObjectNotFoundException("Object not found: " + path);
                }
                if (target.type() == ObjectType.FOLDER && hasChildren(target.id())) {
                    throw new FolderNotEmptyException("Folder is not empty: " + path);
                }

                stmtDelete.setLong(1, target.id());
                stmtDelete.executeUpdate();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IOException ioe) throw ioe;
                if (e instanceof SQLException sqle) throw new IOException(sqle.getMessage(), sqle);
                throw new IOException(e);
            } finally {
                connection.setAutoCommit(origAutoCommit);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void deleteRecursively(URI path) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);

        boolean origAutoCommit = false;
        try {
            origAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                DbNode target = findNode(normalizedPath);
                if (target == null) {
                    throw new ObjectNotFoundException("Object not found: " + path);
                }
                if (target.parentId() == null) {
                    deleteChildrenRecursively(target.id());
                } else {
                    deleteSubtreeRecursively(target.id());
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IOException ioe) throw ioe;
                if (e instanceof SQLException sqle) throw new IOException(sqle.getMessage(), sqle);
                throw new IOException(e);
            } finally {
                connection.setAutoCommit(origAutoCommit);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private void deleteSubtreeRecursively(long nodeId) throws SQLException {
        deleteChildrenRecursively(nodeId);
        stmtDelete.setLong(1, nodeId);
        stmtDelete.executeUpdate();
    }

    private void deleteChildrenRecursively(long parentId) throws SQLException {
        List<Long> childIds = new ArrayList<>();
        stmtFindChildrenIds.setLong(1, parentId);
        try (ResultSet rs = stmtFindChildrenIds.executeQuery()) {
            while (rs.next()) {
                childIds.add(rs.getLong("id"));
            }
        }
        for (long childId : childIds) {
            deleteSubtreeRecursively(childId);
        }
    }

    private boolean hasChildren(long parentId) throws SQLException {
        stmtHasChildren.setLong(1, parentId);
        try (ResultSet rs = stmtHasChildren.executeQuery()) {
            return rs.next();
        }
    }

    @Override
    public long write(URI path, InputStream in, OutputOption... options) throws IOException {
        byte[] data = in.readAllBytes();
        return write(path, data, options);
    }

    @Override
    public long write(URI path, byte[] data, int from, int to, OutputOption... options) throws IOException {
        Objects.checkFromToIndex(from, to, data.length);
        byte[] slice = Arrays.copyOfRange(data, from, to);
        return write(path, slice, options);
    }

    @Override
    public synchronized long write(URI path, byte[] data, OutputOption... options) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new NotADataObjectException("Cannot write to root path");
        }
        OutputOption option = getWriteOption(options);

        boolean origAutoCommit = false;
        try {
            origAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String parentPath = getParentPath(normalizedPath);
                DbNode parent = ensureParentFoldersExist(parentPath);
                String name = getFileName(normalizedPath);
                DbNode existing = findChild(parent.id(), name);
                Timestamp now = Timestamp.from(Instant.now());

                if (existing != null) {
                    if (existing.type() == ObjectType.FOLDER) {
                        throw new NotADataObjectException("Path is a folder: " + path);
                    }
                    if (option == OutputOption.CREATE_NEW) {
                        throw new ObjectExistsException("Object already exists: " + path);
                    }
                    stmtUpdateData.setLong(1, data.length);
                    stmtUpdateData.setTimestamp(2, now);
                    stmtUpdateData.setBytes(3, data);
                    stmtUpdateData.setLong(4, existing.id());
                    stmtUpdateData.executeUpdate();
                } else {
                    long newId = getNextId();
                    stmtInsertData.setLong(1, newId);
                    stmtInsertData.setLong(2, parent.id());
                    stmtInsertData.setString(3, name);
                    stmtInsertData.setLong(4, data.length);
                    stmtInsertData.setTimestamp(5, now);
                    stmtInsertData.setTimestamp(6, now);
                    stmtInsertData.setBytes(7, data);
                    stmtInsertData.executeUpdate();
                }
                connection.commit();
                return data.length;
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof IOException ioe) throw ioe;
                if (e instanceof SQLException sqle) throw new IOException(sqle.getMessage(), sqle);
                throw new IOException(e);
            } finally {
                connection.setAutoCommit(origAutoCommit);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized OutputStream openOutputStream(URI path, OutputOption... options) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new NotADataObjectException("Cannot write to root path");
        }
        OutputOption option = getWriteOption(options);

        try {
            DbNode target = findNode(normalizedPath);
            if (target != null) {
                if (target.type() == ObjectType.FOLDER) {
                    throw new NotADataObjectException("Path is a folder: " + path);
                }
                if (option == OutputOption.CREATE_NEW) {
                    throw new ObjectExistsException("Object already exists: " + path);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }

        return new ByteArrayOutputStream() {
            private boolean closed = false;

            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    byte[] data = toByteArray();
                    DbObjectStore.this.write(path, data, option);
                }
                super.close();
            }
        };
    }

    @Override
    public synchronized WritableByteChannel openWritableByteChannel(URI path, OutputOption... options) throws IOException {
        assertWritable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new NotADataObjectException("Cannot write to root path");
        }
        OutputOption option = getWriteOption(options);

        try {
            DbNode target = findNode(normalizedPath);
            if (target != null) {
                if (target.type() == ObjectType.FOLDER) {
                    throw new NotADataObjectException("Path is a folder: " + path);
                }
                if (option == OutputOption.CREATE_NEW) {
                    throw new ObjectExistsException("Object already exists: " + path);
                }
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }

        return new ByteArrayWritableByteChannel(bytes -> write(path, bytes, option));
    }

    @Override
    public synchronized void copy(URI source, URI target, OutputOption... options) throws IOException {
        assertReadable();
        assertWritable();
        String srcPath = resolvePath(source);
        if (srcPath.isEmpty()) {
            throw new NotADataObjectException("Source is root folder");
        }

        byte[] data;
        try {
            DbNode node = findNode(srcPath);
            if (node == null) {
                throw new ObjectNotFoundException("Source object not found: " + source);
            }
            if (node.type() != ObjectType.DATA) {
                throw new NotADataObjectException("Source object is not a data object: " + source);
            }
            stmtGetData.setLong(1, node.id());
            try (ResultSet rs = stmtGetData.executeQuery()) {
                if (!rs.next()) {
                    throw new ObjectNotFoundException("Source object not found: " + source);
                }
                data = rs.getBytes("data");
                if (data == null) {
                    data = new byte[0];
                }
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }

        write(target, data, options);
    }

    @Override
    public void move(URI source, URI target, OutputOption... options) throws IOException {
        copy(source, target, options);
        delete(source);
    }

    @Override
    public synchronized InputStream openInputStream(URI path) throws IOException {
        assertReadable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new NotADataObjectException("Path is root folder");
        }

        try {
            DbNode target = findNode(normalizedPath);
            if (target == null) {
                throw new ObjectNotFoundException("Object not found: " + path);
            }
            if (target.type() != ObjectType.DATA) {
                throw new NotADataObjectException("Not a data object: " + path);
            }
            stmtGetData.setLong(1, target.id());
            try (ResultSet rs = stmtGetData.executeQuery()) {
                if (!rs.next()) {
                    throw new ObjectNotFoundException("Object not found: " + path);
                }
                byte[] bytes = rs.getBytes("data");
                if (bytes == null) {
                    bytes = new byte[0];
                }
                return new ByteArrayInputStream(bytes);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized SeekableByteChannel openReadableByteChannel(URI path) throws IOException {
        assertReadable();
        String normalizedPath = resolvePath(path);
        if (normalizedPath.isEmpty()) {
            throw new NotADataObjectException("Path is root folder");
        }

        try {
            DbNode target = findNode(normalizedPath);
            if (target == null) {
                throw new ObjectNotFoundException("Object not found: " + path);
            }
            if (target.type() != ObjectType.DATA) {
                throw new NotADataObjectException("Not a data object: " + path);
            }
            stmtGetData.setLong(1, target.id());
            try (ResultSet rs = stmtGetData.executeQuery()) {
                if (!rs.next()) {
                    throw new ObjectNotFoundException("Object not found: " + path);
                }
                byte[] bytes = rs.getBytes("data");
                if (bytes == null) {
                    bytes = new byte[0];
                }
                return new ByteArraySeekableByteChannel(bytes);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized ObjectInfo getInfo(URI path) throws IOException {
        assertReadable();
        String normalizedPath = resolvePath(path);

        try {
            DbNode target;
            try {
                target = findNode(normalizedPath);
            } catch (NotAFolderException e) {
                return new ObjectInfo(toUri(normalizedPath, ObjectType.MISSING), ObjectType.MISSING, ObjectInfo.UNKNOWN_SIZE, Instant.MIN, Instant.MIN);
            }
            if (target != null) {
                return new ObjectInfo(toUri(normalizedPath, target.type()), target.type(), target.size(), target.created(), target.lastModified());
            } else {
                return new ObjectInfo(toUri(normalizedPath, ObjectType.MISSING), ObjectType.MISSING, ObjectInfo.UNKNOWN_SIZE, Instant.MIN, Instant.MIN);
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized Stream<ObjectInfo> list(URI path) throws IOException {
        assertReadable();
        String normalizedPath = resolvePath(path);

        try {
            DbNode target = findNode(normalizedPath);
            if (target == null) {
                throw new ObjectNotFoundException("Object not found: " + path);
            }
            if (target.type() != ObjectType.FOLDER) {
                throw new NotAFolderException("Not a folder: " + path);
            }

            List<ObjectInfo> list = new ArrayList<>();
            stmtListChildren.setLong(1, target.id());
            try (ResultSet rs = stmtListChildren.executeQuery()) {
                while (rs.next()) {
                    DbNode child = readNode(rs);
                    String childPath = normalizedPath.isEmpty() ? child.name() : (normalizedPath + '/' + child.name());
                    list.add(new ObjectInfo(toUri(childPath, child.type()), child.type(), child.size(), child.created(), child.lastModified()));
                }
            }
            return list.stream();
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        List<Exception> exceptions = new ArrayList<>();
        closeStatement(stmtFindRoot, exceptions);
        closeStatement(stmtFindChild, exceptions);
        closeStatement(stmtInsertFolder, exceptions);
        closeStatement(stmtInsertData, exceptions);
        closeStatement(stmtUpdateData, exceptions);
        closeStatement(stmtDelete, exceptions);
        closeStatement(stmtFindChildrenIds, exceptions);
        closeStatement(stmtHasChildren, exceptions);
        closeStatement(stmtGetData, exceptions);
        closeStatement(stmtListChildren, exceptions);
        closeStatement(stmtGetNextId, exceptions);
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            exceptions.add(e);
        }
        if (!exceptions.isEmpty()) {
            IOException ioe = new IOException("Error closing DbObjectStore");
            exceptions.forEach(ioe::addSuppressed);
            throw ioe;
        }
    }

    private static void closeStatement(@Nullable PreparedStatement stmt, List<Exception> exceptions) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
    }

    /**
     * Checks if this object store has been closed.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed() {
        return closed;
    }

    private void assertNotClosed() {
        if (closed) {
            throw new IllegalStateException("Object store is closed");
        }
    }

    @Override
    public void assertReadable() {
        assertNotClosed();
        if (accessMode == AccessMode.WRITE) {
            throw new IllegalStateException("Object store is not readable (mode is WRITE)");
        }
    }

    @Override
    public void assertWritable() {
        assertNotClosed();
        if (accessMode == AccessMode.READ) {
            throw new IllegalStateException("Object store is not writable (mode is READ)");
        }
    }

    @SuppressWarnings({"java:S2077", "JDBCExecuteWithNonConstantString"})
    private void ensureTableExists() throws SQLException, IOException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, null)) {
            if (rs.next()) {
                return;
            }
        }

        try (Statement stmt1 = connection.createStatement()) {
            stmt1.executeQuery("SELECT 1 FROM " + tableName + " WHERE 1=0");
            return;
        } catch (SQLException ignored) {
            // ignore
        }

        if (accessMode == AccessMode.READ) {
            throw new IOException("Table does not exist: " + tableName);
        }
        try (Statement stmt = connection.createStatement()) {
            int res = stmt.executeUpdate("CREATE TABLE " + tableName + " (" +
                    "id BIGINT PRIMARY KEY, " +
                    "parent_id BIGINT REFERENCES " + tableName + "(id), " +
                    "name VARCHAR(1024) NOT NULL, " +
                    "type CHAR(1) NOT NULL, " +
                    "size BIGINT, " +
                    "created TIMESTAMP NOT NULL, " +
                    "last_modified TIMESTAMP NOT NULL, " +
                    "data BLOB, " +
                    "UNIQUE (parent_id, name))");
            LangUtil.ignore(res);
        }
}

    private void ensureRootExists() throws SQLException {
        DbNode rootNode = findRoot();
        if (rootNode == null) {
            long rootId = getNextId();
            Timestamp now = Timestamp.from(Instant.now());
            stmtInsertFolder.setLong(1, rootId);
            stmtInsertFolder.setNull(2, Types.BIGINT);
            stmtInsertFolder.setString(3, "");
            stmtInsertFolder.setTimestamp(4, now);
            stmtInsertFolder.setTimestamp(5, now);
            stmtInsertFolder.executeUpdate();
        }
    }

    private static DbNode readNode(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long parentIdVal = rs.getLong("parent_id");
        Long parentId = rs.wasNull() ? null : parentIdVal;
        String name = rs.getString("name");
        String typeStr = rs.getString("type");
        ObjectType type = "F".equalsIgnoreCase(typeStr) ? ObjectType.FOLDER : ObjectType.DATA;
        long size = rs.getLong("size");
        if (rs.wasNull()) {
            size = ObjectInfo.UNKNOWN_SIZE;
        }
        Timestamp createdTs = rs.getTimestamp("created");
        Timestamp lastModTs = rs.getTimestamp("last_modified");
        Instant created = createdTs != null ? createdTs.toInstant() : Instant.MIN;
        Instant lastModified = lastModTs != null ? lastModTs.toInstant() : Instant.MIN;
        return new DbNode(id, parentId, name, type, size, created, lastModified);
    }

    private @Nullable DbNode findRoot() throws SQLException {
        try (ResultSet rs = stmtFindRoot.executeQuery()) {
            if (rs.next()) {
                return readNode(rs);
            }
            return null;
        }
    }

    private @Nullable DbNode findChild(long parentId, String name) throws SQLException {
        stmtFindChild.setLong(1, parentId);
        stmtFindChild.setString(2, name);
        try (ResultSet rs = stmtFindChild.executeQuery()) {
            if (rs.next()) {
                return readNode(rs);
            }
            return null;
        }
    }

    private @Nullable DbNode findNode(String normalizedPath) throws SQLException, IOException {
        DbNode current = findRoot();
        if (current == null) {
            return null;
        }
        if (normalizedPath.isEmpty()) {
            return current;
        }
        String[] segments = normalizedPath.split("/", -1);
        for (String segment : segments) {
            if (current.type() != ObjectType.FOLDER) {
                throw new NotAFolderException("Path segment is not a folder: " + current.name());
            }
            current = findChild(current.id(), segment);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private DbNode ensureParentFoldersExist(String parentNormalizedPath) throws SQLException, IOException {
        DbNode current = findRoot();
        if (current == null) {
            throw new IOException("Root folder not found in table " + tableName);
        }
        if (parentNormalizedPath.isEmpty()) {
            return current;
        }
        String[] segments = parentNormalizedPath.split("/", -1);
        for (String segment : segments) {
            if (!segment.isEmpty()) {
                if (current.type() != ObjectType.FOLDER) {
                    throw new NotAFolderException("Path segment is not a folder: " + current.name());
                }
                DbNode child = findChild(current.id(), segment);
                if (child == null) {
                    long newId = getNextId();
                    Timestamp now = Timestamp.from(Instant.now());
                    stmtInsertFolder.setLong(1, newId);
                    stmtInsertFolder.setLong(2, current.id());
                    stmtInsertFolder.setString(3, segment);
                    stmtInsertFolder.setTimestamp(4, now);
                    stmtInsertFolder.setTimestamp(5, now);
                    stmtInsertFolder.executeUpdate();
                    child = new DbNode(newId, current.id(), segment, ObjectType.FOLDER, ObjectInfo.UNKNOWN_SIZE, now.toInstant(), now.toInstant());
                } else if (child.type() != ObjectType.FOLDER) {
                    throw new NotAFolderException("Parent path is not a folder: " + segment);
                }
                current = child;
            }
        }
        return current;
    }

    private long getNextId() throws SQLException {
        try (ResultSet rs = stmtGetNextId.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 1L;
        }
    }

    private String resolvePath(URI path) throws IOException {
        if (path.isAbsolute()) {
            throw new AbsolutePathException("absolute path not allowed: " + path);
        }
        if (path.getQuery() != null || path.getFragment() != null) {
            throw new IllegalPathException("path contains query or fragment: " + path);
        }
        String raw = path.getPath();
        if (raw == null || raw.startsWith("/")) {
            throw new IllegalPathException("path must not start with /: " + path);
        }

        String[] parts = raw.split("/", -1);
        List<String> segments = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && !".".equals(part)) {
                if ("..".equals(part)) {
                    if (segments.isEmpty()) {
                        throw new IllegalPathException("path points outside root: " + path);
                    }
                    segments.removeLast();
                } else {
                    segments.add(part);
                }
            }
        }
        return String.join("/", segments);
    }

    private static String getParentPath(String normalizedPath) {
        int idx = normalizedPath.lastIndexOf('/');
        return idx >= 0 ? normalizedPath.substring(0, idx) : "";
    }

    private static String getFileName(String normalizedPath) {
        int idx = normalizedPath.lastIndexOf('/');
        return idx >= 0 ? normalizedPath.substring(idx + 1) : normalizedPath;
    }

    private static URI toUri(String normalizedPath, ObjectType type) throws IOException {
        String path = normalizedPath;
        if (type == ObjectType.FOLDER && !path.isEmpty() && !path.endsWith("/")) {
            path += "/";
        }
        try {
            return new URI(null, null, path, null);
        } catch (URISyntaxException e) {
            throw new IOException("Could not create URI for path: " + path, e);
        }
    }

    private static OutputOption getWriteOption(OutputOption... options) {
        Set<OutputOption> optionSet = Set.of(options);
        return switch (optionSet.size()) {
            case 0 -> OutputOption.CREATE_NEW;
            case 1 -> optionSet.iterator().next();
            default -> throw new IllegalArgumentException("Multiple incompatible output options specified: " + Arrays.toString(options));
        };
    }

    @Override
    public String toString() {
        return "DbObjectStore[root=" + root + ", table=" + tableName + ", accessMode=" + accessMode + "]";
    }

    @FunctionalInterface
    private interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }

    private static final class ByteArraySeekableByteChannel implements SeekableByteChannel {
        private final byte[] data;
        private int position = 0;
        private boolean open = true;

        private ByteArraySeekableByteChannel(byte[] data) {
            this.data = data;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            if (position >= data.length) {
                return -1;
            }
            int available = data.length - position;
            int toRead = Math.min(dst.remaining(), available);
            dst.put(data, position, toRead);
            position += toRead;
            return toRead;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            throw new NonWritableChannelException();
        }

        @Override
        public long position() throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            return position;
        }

        @Override
        public SeekableByteChannel position(long newPosition) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            if (newPosition < 0 || newPosition > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid position: " + newPosition);
            }
            this.position = (int) newPosition;
            return this;
        }

        @Override
        public long size() throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            return data.length;
        }

        @Override
        public SeekableByteChannel truncate(long size) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            throw new NonWritableChannelException();
        }
    }

    private static final class ByteArrayWritableByteChannel implements WritableByteChannel {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final IOConsumer<byte[]> onFlush;
        private boolean open = true;

        private ByteArrayWritableByteChannel(IOConsumer<byte[]> onFlush) {
            this.onFlush = onFlush;
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            if (open) {
                open = false;
                onFlush.accept(buffer.toByteArray());
            }
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (!open) {
                throw new ClosedChannelException();
            }
            int len = src.remaining();
            byte[] bytes = new byte[len];
            src.get(bytes);
            buffer.write(bytes);
            return len;
        }
    }
}
