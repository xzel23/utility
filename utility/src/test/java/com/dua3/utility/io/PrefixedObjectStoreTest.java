/*
 * Copyright (c) 2026. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrefixedObjectStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void readableStoreReturnsPathsRelativeToPrefix() throws Exception {
        Path root = tempDir.resolve("readable");
        try (ObjectStore seed = ObjectStores.fileStore(root);
             ReadableObjectStore delegate = ObjectStores.readableFileStore(root)) {
            seed.write(URI.create("reports/current/result.txt"), "result".getBytes(StandardCharsets.UTF_8));

            ReadableObjectStore prefixed = delegate.prefixed(URI.create("reports/current"));

            assertEquals(root.resolve("reports/current/").toUri(), prefixed.getRoot());
            try (InputStream in = prefixed.openInputStream(URI.create("result.txt"))) {
                assertArrayEquals("result".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }
            try (var objects = prefixed.list(URI.create(""))) {
                assertEquals(List.of(URI.create("result.txt")), objects.map(ObjectStore.ObjectInfo::uri).toList());
            }
        }
    }

    @Test
    void readableStoreGlobsRelativeToPrefix() throws Exception {
        Path root = tempDir.resolve("readable-glob");
        try (ObjectStore seed = ObjectStores.fileStore(root);
             ReadableObjectStore delegate = ObjectStores.readableFileStore(root)) {
            seed.write(URI.create("reports/current/result.txt"), "result".getBytes(StandardCharsets.UTF_8));
            seed.write(URI.create("reports/current/archive/old.txt"), "old".getBytes(StandardCharsets.UTF_8));
            seed.write(URI.create("reports/other/result.txt"), "other".getBytes(StandardCharsets.UTF_8));

            ReadableObjectStore prefixed = delegate.prefixed(URI.create("reports/current"));

            try (Stream<URI> matches = prefixed.glob("*.txt")) {
                assertEquals(List.of(URI.create("result.txt")), matches.toList());
            }
            try (Stream<URI> matches = prefixed.glob(URI.create("archive"), "*.txt")) {
                assertEquals(List.of(URI.create("archive/old.txt")), matches.toList());
            }
        }
    }

    @Test
    void writableStoreWritesBelowPrefix() throws Exception {
        Path root = tempDir.resolve("writable");
        try (WritableObjectStore delegate = ObjectStores.writableFileStore(root)) {
            WritableObjectStore prefixed = delegate.prefixed(URI.create("reports/current"));

            prefixed.write(
                    URI.create("details/result.html"),
                    "result".getBytes(StandardCharsets.UTF_8),
                    ObjectStore.OutputOption.CREATE_NEW
            );

            assertEquals(root.resolve("reports/current/").toUri(), prefixed.getRoot());
            assertEquals("result", Files.readString(root.resolve("reports/current/details/result.html")));
            assertThrows(IllegalPathException.class, () -> prefixed.write(
                    URI.create("../outside.txt"),
                    "outside".getBytes(StandardCharsets.UTF_8)
            ));
        }
    }

    @Test
    void objectStoreProvidesReadWriteViewBelowPrefix() throws Exception {
        Path root = tempDir.resolve("object");
        try (ObjectStore delegate = ObjectStores.fileStore(root)) {
            ObjectStore prefixed = delegate.prefixed(URI.create("reports/current"));

            prefixed.write(URI.create("settings.dcompare"), "settings".getBytes(StandardCharsets.UTF_8));

            assertEquals(root.resolve("reports/current/").toUri(), prefixed.getRoot());
            assertEquals(ObjectStore.ObjectType.FOLDER, prefixed.getInfo(URI.create("")).orElseThrow().type());
            assertEquals(ObjectStore.ObjectType.DATA, prefixed.getInfo(URI.create("settings.dcompare")).orElseThrow().type());
            try (InputStream in = prefixed.openInputStream(URI.create("settings.dcompare"))) {
                assertArrayEquals("settings".getBytes(StandardCharsets.UTF_8), in.readAllBytes());
            }

            prefixed.delete(URI.create("settings.dcompare"));
            try (var files = Files.list(root.resolve("reports/current"))) {
                assertEquals(List.of(), files.toList());
            }
        }
    }

    @Test
    void emptyPrefixReturnsAnIdentityView() throws Exception {
        Path root = tempDir.resolve("identity");
        try (ObjectStore delegate = ObjectStores.fileStore(root)) {
            ObjectStore prefixed = delegate.prefixed(URI.create(""));
            prefixed.write(URI.create("data.txt"), "data".getBytes(StandardCharsets.UTF_8));

            assertEquals(delegate.getRoot(), prefixed.getRoot());
            try (var objects = prefixed.list(URI.create(""))) {
                assertEquals(List.of(URI.create("data.txt")), objects.map(ObjectStore.ObjectInfo::uri).toList());
            }
        }
    }
}
