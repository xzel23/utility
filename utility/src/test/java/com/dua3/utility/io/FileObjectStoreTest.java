package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileObjectStoreTest extends AbstractObjectStoreTest {
    @Override
    protected ObjectStore createStore(Path root) throws IOException {
        return FileObjectStore.newObjectStore(root);
    }

    @Test
    void publicFactoryCreatesFileStore() throws Exception {
        try (ObjectStore store = ObjectStores.fileStore(tempDir.resolve("factory-store"))) {
            assertTrue(store.getRoot().isAbsolute());
        }
    }

    @Test
    void readableByteChannel_enforcesReadableModeAndDataObjects() throws Exception {
        Path root = tempDir.resolve("channel-validation");
        try (ObjectStore seed = FileObjectStore.newObjectStore(root)) {
            seed.write(URI.create("data.bin"), "data".getBytes(StandardCharsets.UTF_8));
            seed.createFolder(URI.create("folder"));
        }

        try (ReadableObjectStore readable = FileObjectStore.newReadableObjectStore(root)) {
            assertThrows(ObjectNotFoundException.class, () -> readable.openReadableByteChannel(URI.create("missing.bin")));
            assertThrows(IOException.class, () -> readable.openReadableByteChannel(URI.create("folder")));
        }

        try (FileObjectStore writable = (FileObjectStore) FileObjectStore.newWritableObjectStore(root)) {
            assertThrows(IllegalStateException.class, () -> writable.openReadableByteChannel(URI.create("data.bin")));
        }
    }
}
