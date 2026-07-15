package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;

import java.io.IOException;
import java.nio.file.Path;

class FileObjectStoreTest extends AbstractObjectStoreTest {
    @Override
    protected ObjectStore createStore(Path root) throws IOException {
        return FileObjectStore.newObjectStore(root);
    }
}
