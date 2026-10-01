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

@SuppressWarnings("java:S5778")
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
}
