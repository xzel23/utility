/*
 * Copyright (c) 2026. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.db;

import com.dua3.utility.io.AbstractObjectStoreTest;
import com.dua3.utility.io.ObjectStore;
import com.dua3.utility.io.ReadableObjectStore;
import com.dua3.utility.io.WritableObjectStore;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"java:S5778", "ProhibitedExceptionThrown"})
class DbObjectStoreTest extends AbstractObjectStoreTest {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    @Override
    protected ObjectStore createStore(Path root) throws IOException {
        int dbId = DB_COUNTER.incrementAndGet();
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test_store_" + dbId + ";DB_CLOSE_DELAY=-1");
        return DbObjectStore.newObjectStore(ds);
    }

    @Test
    void testCustomTableNameAndRootUri() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:custom_table_test;DB_CLOSE_DELAY=-1");

        URI customRoot = URI.create("db://my-cluster/custom_store/");
        try (DbObjectStore store = DbObjectStore.newObjectStore(customRoot, ds::getConnection, "my_objects")) {
            assertEquals(customRoot, store.getRoot());

            URI path = URI.create("sample.txt");
            store.writeString(path, "custom content");
            assertEquals("custom content", store.readString(path));
        }
    }

    @Test
    void testReadOnlyAndWriteOnlyModes() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:modes_test;DB_CLOSE_DELAY=-1");

        // Seed via read-write store
        try (DbObjectStore store = DbObjectStore.newObjectStore(ds, "items")) {
            store.writeString(URI.create("seed.txt"), "seeded");
        }

        // Test readable store
        try (ReadableObjectStore readable = DbObjectStore.newReadableObjectStore(ds, "items")) {
            if (readable instanceof ObjectStore os) {
                assertEquals(ObjectStore.AccessMode.READ, os.getAccessMode());
                assertThrows(IllegalStateException.class, () -> os.writeString(URI.create("new.txt"), "fail"));
                assertThrows(IllegalStateException.class, () -> os.createFolder(URI.create("folder")));
            }
            assertEquals("seeded", readable.readString(URI.create("seed.txt")));
        }

        // Test writable store
        try (WritableObjectStore writable = DbObjectStore.newWritableObjectStore(ds, "items")) {
            if (writable instanceof ObjectStore os) {
                assertEquals(ObjectStore.AccessMode.WRITE, os.getAccessMode());
                assertThrows(IllegalStateException.class, () -> os.readString(URI.create("written.txt")));
                assertThrows(IllegalStateException.class, () -> os.getInfo(URI.create("written.txt")));
                assertThrows(IllegalStateException.class, () -> os.list(URI.create("")));
            }
            writable.writeString(URI.create("written.txt"), "hello");
        }
    }

    @Test
    void testCloseBehavior() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:close_test;DB_CLOSE_DELAY=-1");

        DbObjectStore store = DbObjectStore.newObjectStore(ds);
        assertFalse(store.isClosed());
        store.writeString(URI.create("test.txt"), "content");

        store.close();
        assertTrue(store.isClosed());

        assertThrows(IllegalStateException.class, () -> store.readString(URI.create("test.txt")));
        assertThrows(IllegalStateException.class, () -> store.writeString(URI.create("test2.txt"), "content"));
    }

    @Test
    void testConnectionSupplierOverloads() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:supplier_test;DB_CLOSE_DELAY=-1");

        DbObjectStore.ConnectionSupplier supplier = ds::getConnection;
        try (DbObjectStore store = DbObjectStore.newObjectStore(supplier)) {
            store.writeString(URI.create("hello.txt"), "world");
            assertEquals("world", store.readString(URI.create("hello.txt")));
        }
    }

    @Test
    void testTableLayoutSchemaAndAdjacencyList() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:schema_layout_test;DB_CLOSE_DELAY=-1");

        try (DbObjectStore store = DbObjectStore.newObjectStore(ds, "store_objects")) {
            store.writeString(URI.create("a/b/c.txt"), "hello world");
        }

        try (java.sql.Connection conn = ds.getConnection()) {
            // Check root row
            long rootId;
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, parent_id, name, type, size, data FROM STORE_OBJECTS WHERE parent_id IS NULL AND name = ''")) {
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Root node should exist");
                    rootId = rs.getLong("id");
                    assertEquals("", rs.getString("name"));
                    assertEquals("F", rs.getString("type"));
                    rs.getLong("size");
                    assertTrue(rs.wasNull(), "Folder size should be null");
                    org.junit.jupiter.api.Assertions.assertNull(rs.getBytes("data"));
                }
            }

            // Check folder a
            long folderAId;
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, parent_id, name, type, size, data FROM STORE_OBJECTS WHERE parent_id = ? AND name = 'a'")) {
                stmt.setLong(1, rootId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Folder a should exist");
                    folderAId = rs.getLong("id");
                    assertEquals("F", rs.getString("type"));
                    rs.getLong("size");
                    assertTrue(rs.wasNull(), "Folder size should be null");
                }
            }

            // Check folder b
            long folderBId;
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, parent_id, name, type, size, data FROM STORE_OBJECTS WHERE parent_id = ? AND name = 'b'")) {
                stmt.setLong(1, folderAId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Folder b should exist");
                    folderBId = rs.getLong("id");
                    assertEquals("F", rs.getString("type"));
                    rs.getLong("size");
                    assertTrue(rs.wasNull(), "Folder size should be null");
                }
            }

            // Check file c.txt
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, parent_id, name, type, size, data FROM STORE_OBJECTS WHERE parent_id = ? AND name = 'c.txt'")) {
                stmt.setLong(1, folderBId);
                try (java.sql.ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "File c.txt should exist");
                    assertEquals("D", rs.getString("type"));
                    assertEquals(11L, rs.getLong("size"));
                    assertFalse(rs.wasNull());
                    org.junit.jupiter.api.Assertions.assertArrayEquals(
                            "hello world".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                            rs.getBytes("data")
                    );
                }
            }

            // Verify unique constraint on (parent_id, name)
            try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO STORE_OBJECTS (id, parent_id, name, type, size, created, last_modified, data) VALUES (999, ?, 'c.txt', 'D', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)")) {
                stmt.setLong(1, folderBId);
                assertThrows(java.sql.SQLException.class, stmt::executeUpdate);
            }
        }
    }

    @Test
    void testDirectConnection() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:direct_conn_test;DB_CLOSE_DELAY=-1");

        try (java.sql.Connection conn = ds.getConnection();
             DbObjectStore store = DbObjectStore.newObjectStore(conn, "direct_store")) {
            store.writeString(URI.create("direct.txt"), "hello direct");
            assertEquals("hello direct", store.readString(URI.create("direct.txt")));
        }
    }

    @Test
    void testPublicFactoryOverloads() throws Exception {
        JdbcDataSource ds = dataSource("factory_overloads");
        URI root = URI.create("db://factory/root/");

        try (DbObjectStore store = DbObjectStore.newObjectStore(ds)) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (DbObjectStore store = DbObjectStore.newObjectStore(ds, "ds_rw_named")) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, "ds_rw_named");
        }
        try (DbObjectStore store = DbObjectStore.newObjectStore(root, ds, "ds_rw_root")) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, root);
        }

        seedTable(ds, "ds_read_named");
        seedTable(ds, "ds_read_root");
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(ds)) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(ds, "ds_read_named")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, "ds_read_named");
        }
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(root, ds, "ds_read_root")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, root);
        }

        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(ds)) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(ds, "ds_write_named")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, "ds_write_named");
        }
        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(root, ds, "ds_write_root")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, root);
        }

        try (java.sql.Connection connection = ds.getConnection();
             DbObjectStore store = DbObjectStore.create(root, connection, "conn_create", ObjectStore.AccessMode.READ_AND_WRITE)) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, root);
        }

        DbObjectStore.ConnectionSupplier connectionSupplier = ds::getConnection;
        try (DbObjectStore store = DbObjectStore.newObjectStore(connectionSupplier)) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (DbObjectStore store = DbObjectStore.newObjectStore(connectionSupplier, "supplier_rw_named")) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, "supplier_rw_named");
        }
        try (DbObjectStore store = DbObjectStore.newObjectStore(root, connectionSupplier, "supplier_rw_root")) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, root);
        }

        seedTable(ds, "supplier_read_named");
        seedTable(ds, "supplier_read_root");
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(connectionSupplier)) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(connectionSupplier, "supplier_read_named")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, "supplier_read_named");
        }
        try (ReadableObjectStore store = DbObjectStore.newReadableObjectStore(root, connectionSupplier, "supplier_read_root")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.READ, root);
        }

        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(connectionSupplier)) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(connectionSupplier, "supplier_write_named")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, "supplier_write_named");
        }
        try (WritableObjectStore store = DbObjectStore.newWritableObjectStore(root, connectionSupplier, "supplier_write_root")) {
            assertStore((DbObjectStore) store, ObjectStore.AccessMode.WRITE, root);
        }
        try (DbObjectStore store = DbObjectStore.create(root, connectionSupplier, "supplier_create", ObjectStore.AccessMode.READ_AND_WRITE)) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, root);
        }

        java.util.function.Supplier<java.sql.Connection> javaSupplier = () -> {
            try {
                return ds.getConnection();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        };
        try (DbObjectStore store = DbObjectStore.newObjectStore(javaSupplier)) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, DbObjectStore.DEFAULT_TABLE_NAME);
        }
        try (DbObjectStore store = DbObjectStore.newObjectStore(javaSupplier, "java_supplier_named")) {
            assertStore(store, ObjectStore.AccessMode.READ_AND_WRITE, "java_supplier_named");
        }
    }

    @Test
    void testPublicStateAndLifecycleMethods() throws Exception {
        assertEquals(URI.create("db://localhost/example/"), DbObjectStore.defaultRootUri("example"));

        JdbcDataSource ds = dataSource("state_methods");
        DbObjectStore store = DbObjectStore.newObjectStore(ds, "state_store");
        assertEquals(ObjectStore.AccessMode.READ_AND_WRITE, store.getAccessMode());
        assertEquals(URI.create("db://localhost/state_store/"), store.getRoot());
        assertTrue(store.toString().contains("table=state_store"));
        store.assertReadable();
        store.assertWritable();
        assertFalse(store.isClosed());
        store.close();
        store.close();
        assertTrue(store.isClosed());
        assertThrows(IllegalStateException.class, store::assertReadable);
        assertThrows(IllegalStateException.class, store::assertWritable);
    }

    @Test
    void testDirectByteArrayChannelsAndRootDeletion() throws Exception {
        JdbcDataSource ds = dataSource("direct_methods");
        try (DbObjectStore store = DbObjectStore.newObjectStore(ds, "direct_methods_store")) {
            URI path = URI.create("channel.bin");
            try (var channel = store.openWritableByteChannel(path)) {
                channel.write(java.nio.ByteBuffer.wrap(new byte[]{1, 2, 3}));
            }
            try (var channel = store.openReadableByteChannel(path)) {
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(3);
                assertEquals(3, channel.read(buffer));
                assertEquals(3, channel.position());
                channel.position(1);
                assertEquals(2, channel.read(java.nio.ByteBuffer.allocate(2)));
            }

            store.createFolder(URI.create("tree/branch"));
            store.write(URI.create("tree/branch/file"), new byte[]{4}, ObjectStore.OutputOption.CREATE_NEW);
            store.deleteRecursively(URI.create("tree"));
            assertEquals(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("tree")).type());
            store.deleteRecursively(URI.create(""));
            assertEquals(ObjectStore.ObjectType.MISSING, store.getInfo(URI.create("channel.bin")).type());
        }
    }

    private static JdbcDataSource dataSource(String name) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        return ds;
    }

    @SuppressWarnings("EmptyTryBlock")
    private static void seedTable(JdbcDataSource ds, String tableName) throws IOException {
        try (DbObjectStore ignored = DbObjectStore.newObjectStore(ds, tableName)) {
            // Creating and closing the read-write store creates the table and root row.
        }
    }

    private static void assertStore(DbObjectStore store, ObjectStore.AccessMode mode, String tableName) {
        assertStore(store, mode, URI.create("db://localhost/" + tableName + "/"));
    }

    private static void assertStore(DbObjectStore store, ObjectStore.AccessMode mode, URI root) {
        assertEquals(mode, store.getAccessMode());
        assertEquals(root, store.getRoot());
        assertFalse(store.isClosed());
    }
}
