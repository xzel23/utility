package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Factory methods for standard object store implementations.
 */
public final class ObjectStores {

    private ObjectStores() {
        // utility class
    }

    /**
     * Creates a read-only object store rooted at a local directory.
     *
     * @param root the local root directory
     * @return the readable store
     * @throws IOException if the store cannot be created
     */
    public static ReadableObjectStore readableFileStore(Path root) throws IOException {
        return FileObjectStore.newReadableObjectStore(root);
    }

    /**
     * Creates a write-only object store rooted at a local directory.
     *
     * @param root the local root directory
     * @return the writable store
     * @throws IOException if the store cannot be created
     */
    public static WritableObjectStore writableFileStore(Path root) throws IOException {
        return FileObjectStore.newWritableObjectStore(root);
    }

    /**
     * Creates a read-write object store rooted at a local directory.
     *
     * @param root the local root directory
     * @return the object store
     * @throws IOException if the store cannot be created
     */
    public static ObjectStore fileStore(Path root) throws IOException {
        return FileObjectStore.newObjectStore(root);
    }
}
