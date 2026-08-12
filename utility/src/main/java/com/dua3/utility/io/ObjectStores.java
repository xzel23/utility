package com.dua3.utility.io;

import com.dua3.utility.io.imp.FileObjectStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
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

    /**
     * Copies an object between object stores.
     * <p>
     * File-backed stores use the file-system copy operation directly. Other
     * store combinations copy through streams.
     *
     * @param sourceStore the readable source store
     * @param source the source path relative to {@code sourceStore}
     * @param targetStore the writable target store
     * @param target the target path relative to {@code targetStore}
     * @param options options controlling replacement of an existing target
     * @throws IOException if the object cannot be read or written
     */
    public static void copy(
            ReadableObjectStore sourceStore,
            URI source,
            WritableObjectStore targetStore,
            URI target,
            ObjectStore.OutputOption... options
    ) throws IOException {
        if (sourceStore instanceof FileObjectStore sourceFileStore
                && targetStore instanceof FileObjectStore targetFileStore) {
            sourceFileStore.copyTo(targetFileStore, source, target, options);
            return;
        }

        try (InputStream in = sourceStore.openInputStream(source);
             OutputStream out = targetStore.openOutputStream(target, options)) {
            in.transferTo(out);
        }
    }

    /**
     * Moves an object between object stores.
     * <p>
     * File-backed stores use the file-system move operation directly. Other
     * store combinations copy through streams and delete the source after a
     * successful copy.
     *
     * @param sourceStore the readable and writable source store
     * @param source the source path relative to {@code sourceStore}
     * @param targetStore the writable target store
     * @param target the target path relative to {@code targetStore}
     * @param options options controlling replacement of an existing target
     * @throws IOException if the object cannot be read, written, or deleted
     */
    public static void move(
            ObjectStore sourceStore,
            URI source,
            WritableObjectStore targetStore,
            URI target,
            ObjectStore.OutputOption... options
    ) throws IOException {
        if (sourceStore instanceof FileObjectStore sourceFileStore
                && targetStore instanceof FileObjectStore targetFileStore) {
            sourceFileStore.moveTo(targetFileStore, source, target, options);
            return;
        }

        copy(sourceStore, source, targetStore, target, options);
        sourceStore.delete(source);
    }
}
