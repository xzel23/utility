package com.dua3.utility.io;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The Loader interface defines a contract for loading objects of a specific type
 * from a given data source, such as a URI, using provided options.
 * It provides a factory method and abstract methods for different loading scenarios.
 *
 * @param <T> the type of object this loader is capable of loading
 */
public interface Loader<T> {

    /**
     * Loads an object of the specified type using a {@link Loader} implementation
     * that supports the provided class, URI, and options.
     *
     * @param <T>   the type of the object to be loaded
     * @param cls   the class of the object to be loaded
     * @param uri   the URI pointing to the resource to be loaded
     * @param options an array of options for configuring the loading process
     * @return an instance of type T loaded from the provided URI and options
     * @throws IOException if no suitable {@link Loader} implementation is found
     *                     or an I/O error occurs during the loading process
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T load(Class<?> cls, URI uri, Object[] options) throws IOException {

        Payload payload = Payload.fromUri(uri);

        Optional<Loader> loader = ServiceLoader.load(Loader.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(current -> current.isSupported(cls, payload.magic8Bytes(), options))
                .findFirst();

        if (loader.isEmpty()) {
            throw new IOException("no Loader implementation supports " + cls.getName());
        }

        Loader<T> typedLoader = (Loader<T>) loader.get();
        return typedLoader.load(payload, options);
    }

    /**
     * Determines whether this loader supports the given class, magic value, and options.
     *
     * @param cls     the class of the object to be loaded
     * @param magic   a magic value used for identifying the data format
     * @param options an array of options for configuring the loading process
     * @return {@code true} if the loader supports the input combination, {@code false} otherwise
     */
    boolean isSupported(Class<?> cls, long magic, Object[] options);

    /**
     * Loads an object of type T using the provided payload and options.
     *
     * @param payload the payload containing data required for loading the object
     * @param options an array of objects representing additional configuration or options for the load process
     * @return an instance of type T loaded from the provided payload
     * @throws IOException if an I/O error occurs during the loading process
     */
    T load(Payload payload, Object[] options) throws IOException;

}
