package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

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
}
