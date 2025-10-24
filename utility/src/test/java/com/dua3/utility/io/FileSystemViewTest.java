// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.lang.LangUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the FileSystemView class.
 */
class FileSystemViewTest {

    private static void testClassHelper(Class<?> clazz) throws IOException {
        try (FileSystemView fsv = FileSystemView.forClass(clazz)) {
            assertNotNull(fsv);

            String pathToClassFile = clazz.getSimpleName() + ".class";
            Path path = fsv.resolve(pathToClassFile);

            assertTrue(Files.exists(path));
            assertTrue(Files.size(path) > 0);
        }
    }

    /**
     * Construct a FileSystemView for classpath resources and try to access a class
     * file.
     * In this test, a user supplied class is used which at least when run from
     * eclipse
     * is loaded from a class file on the file system.
     *
     * @throws IOException if resource could not be loaded
     */
    @Test
    void testClass() throws IOException {
        // at least when run from within eclipse (and probably other IDEs as well)
        // *.class is loaded from the file
        // system
        testClassHelper(getClass());
    }

    /**
     * Construct a FileSystemView for classpath resources and try to access a class
     * file.
     * In this test, a JDK class is used which is loaded from within a jar file.
     *
     * @throws IOException if resource could not be loaded
     */
    @Test
    void testJarClass() throws IOException {
        // org.junit.Assert should be loaded from rt.jar, so this tests the jar
        // functionality
        testClassHelper(org.junit.jupiter.api.Test.class);
    }

    @TempDir
    Path tempDir;

    @Test
    void forArchive_opens_existing_zip_and_allows_resolving_inside() throws IOException {
        // create a zip file with one entry
        Path zipPath = tempDir.resolve("sample.zip");
        createZipWithEntry(zipPath, "folder/hello.txt", "hi".getBytes(StandardCharsets.UTF_8));

        try (FileSystemView fsv = FileSystemView.forArchive(zipPath)) {
            assertNotNull(fsv);
            // resolve a path that exists in the archive
            Path p = fsv.resolve(fsv.getRoot().getFileSystem().getPath("folder/hello.txt"));
            assertTrue(Files.exists(p), () -> "Expected entry to exist: " + p);
            assertEquals("hi", Files.readString(p));
        }
    }

    @Test
    void forArchive_creates_missing_zip_if_flag_set_and_is_writable() throws IOException {
        Path zipPath = tempDir.resolve("created.zip");
        assertFalse(Files.exists(zipPath));

        try (FileSystemView fsv = FileSystemView.forArchive(zipPath, FileSystemView.Flags.CREATE_IF_MISSING)) {
            // should have created a new (empty) archive filesystem
            assertNotNull(fsv);
            Path entry = fsv.resolve(fsv.getRoot().getFileSystem().getPath("a/b.txt"));
            // ensure parent dirs exist and write a file
            Files.createDirectories(entry.getParent());
            Files.writeString(entry, "data");
            assertEquals("data", Files.readString(entry));
        }

        // After closing the FS, the zip file should exist on the host FS
        assertTrue(Files.exists(zipPath), "Archive file should have been created");
        assertTrue(Files.size(zipPath) > 0, "Archive should not be empty after writing");
    }

    @Test
    void resolve_path_prevents_traversal_outside_root() throws IOException {
        Path root = Files.createDirectories(tempDir.resolve("root"));
        try (FileSystemView fsv = FileSystemView.forDirectory(root)) {
            // normal resolution stays inside
            Path inside = fsv.resolve(fsv.getRoot().getFileSystem().getPath("sub/file.txt"));
            Files.createDirectories(inside.getParent());
            Files.writeString(inside, "x");
            assertEquals("x", Files.readString(inside));

            // attempts to escape should be rejected
            assertThrows(LangUtil.FailedCheckException.class, () -> fsv.resolve(fsv.getRoot().getFileSystem().getPath("..")));
            assertThrows(LangUtil.FailedCheckException.class, () -> fsv.resolve(fsv.getRoot().getFileSystem().getPath("../outside.txt")));
            assertThrows(LangUtil.FailedCheckException.class, () -> fsv.resolve(fsv.getRoot().getFileSystem().getPath("sub/../../etc/passwd")));
        }
    }

    // helper to create a zip with a single entry via NIO FS
    private static void createZipWithEntry(Path zipPath, String entryName, byte[] data) throws IOException {
        URI uri = URI.create("jar:" + zipPath.toUri());
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"))) {
            Path p = fs.getPath("/" + entryName);
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            Files.write(p, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
