package com.dua3.utility.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectStoresTest {

    @TempDir
    Path tempDir;

    @Test
    void copyBetweenFileStoresUsesFileStoreSemantics() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");
        URI source = URI.create("nested/source.txt");
        URI target = URI.create("copied/result.txt");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(source, "copied content".getBytes(StandardCharsets.UTF_8));

            ObjectStores.copy(sourceStore, source, targetStore, target, ObjectStore.OutputOption.CREATE_NEW);

            assertEquals("copied content", Files.readString(targetRoot.resolve("copied/result.txt")));
            assertEquals("copied content", sourceStore.readString(source));
        }
    }

    @Test
    void moveBetweenFileStoresReplacesTargetAndDeletesSource() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");
        URI source = URI.create("source.txt");
        URI target = URI.create("nested/result.txt");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(source, "new content".getBytes(StandardCharsets.UTF_8));
            targetStore.write(target, "old content".getBytes(StandardCharsets.UTF_8));

            ObjectStores.move(sourceStore, source, targetStore, target, ObjectStore.OutputOption.CREATE_OR_REPLACE);

            assertTrue(sourceStore.getInfo(source).isEmpty());
            assertEquals("new content", targetStore.readString(target));
        }
    }

    @Test
    void copyAndMoveFallbackWorksForPrefixedStoreViews() throws Exception {
        Path sourceRoot = tempDir.resolve("source");
        Path targetRoot = tempDir.resolve("target");

        try (ObjectStore sourceStore = ObjectStores.fileStore(sourceRoot);
             ObjectStore targetStore = ObjectStores.fileStore(targetRoot)) {
            sourceStore.write(URI.create("copy.txt"), "copy".getBytes(StandardCharsets.UTF_8));
            sourceStore.write(URI.create("move.txt"), "move".getBytes(StandardCharsets.UTF_8));

            ObjectStore sourceView = sourceStore.prefixed(URI.create(""));
            ObjectStore targetView = targetStore.prefixed(URI.create(""));

            ObjectStores.copy(sourceView, URI.create("copy.txt"), targetView, URI.create("copy.txt"));
            ObjectStores.move(sourceView, URI.create("move.txt"), targetView, URI.create("move.txt"));

            assertEquals("copy", targetStore.readString(URI.create("copy.txt")));
            assertEquals("move", targetStore.readString(URI.create("move.txt")));
            assertTrue(sourceStore.getInfo(URI.create("move.txt")).isEmpty());
        }
    }
}
