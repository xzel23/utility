package com.dua3.utility.io;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

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
    static <T> T load(Class<? extends T> cls, URI uri, Object... options) throws IOException {
        return tryLoad(cls, uri, options).orElseThrow(() -> new IOException("no Loader implementation supports " + cls.getName()));
    }

    /**
     * Attempts to load an object of the specified type using a {@link Loader} implementation
     * that supports the provided class, URI, and options. Unlike {@link #load(Class, URI, Object...)},
     * this method does not throw an exception if no suitable {@link Loader} is found.
     *
     * @param <T>    the type of the object to be loaded
     * @param cls    the class of the object to be loaded
     * @param uri    the URI pointing to the resource to be loaded
     * @param options an array of options for configuring the loading process
     * @return an {@link Optional} containing the loaded object of type T, or an empty {@link Optional}
     * if no suitable loader is found or the loading process fails
     * @throws IOException if <strong>all</strong> suitable loaders throw an I/O exception during the loading process
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> Optional<T> tryLoad(Class<? extends T> cls, URI uri, Object... options) throws IOException {
        List[] ex = {null};
        try (Payload payload = Payload.fromUri(uri)) {
            Optional<T> loaded = Stream.of(Thread.currentThread().getContextClassLoader(), Loader.class.getClassLoader(), ClassLoader.getSystemClassLoader())
                    .distinct()
                    .flatMap(cl -> ServiceLoader.load(Loader.class, cl).stream())
                    .map(ServiceLoader.Provider::get)
                    .filter(current -> current.isSupported(cls, payload.magic8Bytes(), options))
                    .map(loader -> {
                        try {
                            return (T) loader.load(payload, options);
                        } catch (IOException e) {
                            if (ex[0] == null) {
                                ex[0] = new ArrayList<IOException>();
                            }
                            ex[0].add(e);
                            LogManager.getLogger(Loader.class).warn("Loader {} failed to load {}", loader, uri, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

            // only throw an exception if all loaders failed
            if (loaded.isEmpty() && ex[0] != null) {
                List<IOException> exceptions = ex[0];
                assert !exceptions.isEmpty();
                IOException ioe = new IOException("failed to load " + uri, exceptions.getFirst());
                for (int i = 1; i < exceptions.size(); i++) {
                    ioe.addSuppressed(exceptions.get(i));
                }
                throw ioe;
            }

            return loaded;
        }
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
