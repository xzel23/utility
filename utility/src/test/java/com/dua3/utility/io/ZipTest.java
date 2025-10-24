// Copyright (c) 2025 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Zip helper.
 */
class ZipTest {

    @Test
    void add_file_with_bytes_without_directory_creates_entry_at_root() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "Hello, ZIP".getBytes(StandardCharsets.UTF_8);

        try (Zip zip = new Zip(baos)) {
            zip.add("hello.txt", content);
        }

        ZipContent zc = readZip(baos.toByteArray());
        assertTrue(zc.directories.isEmpty(), "No directory entries expected");
        assertEquals(1, zc.files.size());
        assertArrayEquals(content, zc.files.get("hello.txt"));
    }

    @Test
    void directory_then_add_file_places_file_under_directory_and_writes_directory_entry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "data".getBytes(StandardCharsets.UTF_8);

        try (Zip zip = new Zip(baos)) {
            zip.directory("dir");
            zip.add("a.txt", content);
        }

        ZipContent zc = readZip(baos.toByteArray());
        assertTrue(zc.directories.contains("dir/"));
        assertArrayEquals(content, zc.files.get("dir/a.txt"));
    }

    @Test
    void absolute_directory_resets_path_then_add_file_under_new_root() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Zip zip = new Zip(baos)) {
            zip.directory("first");
            zip.add("one.txt", "1".getBytes(StandardCharsets.UTF_8));
            zip.directory("/root"); // should reset to root/ independent of previous path
            zip.add("two.txt", "2".getBytes(StandardCharsets.UTF_8));
        }

        ZipContent zc = readZip(baos.toByteArray());
        assertTrue(zc.directories.contains("first/"));
        assertTrue(zc.directories.contains("root/"));
        assertArrayEquals("1".getBytes(StandardCharsets.UTF_8), zc.files.get("first/one.txt"));
        assertArrayEquals("2".getBytes(StandardCharsets.UTF_8), zc.files.get("root/two.txt"));
    }

    @Test
    void add_throws_on_empty_filename_and_on_slash_in_filename() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Zip zip = new Zip(baos)) {
            assertThrows(IllegalArgumentException.class, () -> zip.add("", new byte[0]));
            assertThrows(IllegalArgumentException.class, () -> zip.add("", new ByteArrayInputStream(new byte[0])));
            assertThrows(IllegalArgumentException.class, () -> zip.add("a/b.txt", new byte[0]));
            assertThrows(IllegalArgumentException.class, () -> zip.add("a/b.txt", new ByteArrayInputStream(new byte[0])));
        }
    }

    @Test
    void directory_throws_on_empty_dirname() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Zip zip = new Zip(baos)) {
            assertThrows(IllegalArgumentException.class, () -> zip.directory(""));
        }
    }

    @Test
    void flush_and_close_do_not_throw() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Zip zip = new Zip(baos);
        assertDoesNotThrow(zip::flush);
        assertDoesNotThrow(zip::close);
    }

    // Helper structure and method to read ZIP content
    private static class ZipContent {
        final Map<String, byte[]> files = new HashMap<>();
        final Set<String> directories = new HashSet<>();
    }

    private static ZipContent readZip(byte[] data) throws IOException {
        ZipContent result = new ZipContent();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || name.endsWith("/")) {
                    result.directories.add(name);
                } else {
                    result.files.put(name, zis.readAllBytes());
                }
                zis.closeEntry();
            }
        }
        return result;
    }
}
