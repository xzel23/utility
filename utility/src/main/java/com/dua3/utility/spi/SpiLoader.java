package com.dua3.utility.spi;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A generic Service Provider Interface (SPI) loader that dynamically loads implementations
 * of a specified type using Java's {@link ServiceLoader} mechanism. Provides mechanisms
 * to filter the implementations via a predicate and specify a default supplier.
 *
 * @param <T> the type of the service to be loaded
 */
public final class SpiLoader<T> {
    private static final Logger LOG = LogManager.getLogger(SpiLoader.class);

    /**
     * LoaderBuilder is a helper class to configure and construct an instance of SpiLoader.
     * It allows setting a custom class loader, a predicate for filtering instances,
     * and a default supplier for fallback instances.
     *
     * @param <T> the type of instances to be loaded by SpiLoader.
     */
    public static class LoaderBuilder<T> {
        private final Class<T> type;
        private @Nullable ClassLoader cl;
        private @Nullable Predicate<T> predicate;
        private @Nullable Supplier<? extends T> defaultSupplier;

        private LoaderBuilder(Class<T> type) {
            this.type = type;
        }

        /**
         * Sets the class loader for this LoaderBuilder. Ensures that the class loader is set only once.
         *
         * @param cl the {@link ClassLoader} to be used
         * @return the updated LoaderBuilder instance
         */
        public LoaderBuilder<T> classLoader(ClassLoader cl) {
            LangUtil.check(this.cl == null, "class loader already set");
            this.cl = cl;
            return this;
        }

        /**
         * Sets a predicate for filtering instances of type T. This method ensures that the predicate is only
         * set once by throwing an exception if it is already set.
         *
         * @param predicate a {@link Predicate} to filter the instances.
         * @return the current instance of LoaderBuilder with the predicate set.
         */
        public LoaderBuilder<T> accept(Predicate<T> predicate) {
            LangUtil.check(this.predicate == null, "predicate already set");
            this.predicate = predicate;
            return this;
        }

        /**
         * Sets the default supplier for fallback instances.
         *
         * @param defaultSupplier a Supplier that provides fallback instances of type T
         * @return the current LoaderBuilder instance with the updated default supplier
         */
        public LoaderBuilder<T> defaultSupplier(Supplier<? extends T> defaultSupplier) {
            LangUtil.check(this.defaultSupplier == null, "default supplier already set");
            this.defaultSupplier = defaultSupplier;
            return this;
        }

        /**
         * Constructs and returns a new instance of SpiLoader configured with the specified type,
         * class loader, predicate, and default supplier.
         *
         * @return a new instance of SpiLoader configured based on the builder's current state
         */
        public SpiLoader<T> build() {
            Predicate<T> p = predicate != null ? predicate : t -> true;
            Supplier<? extends @Nullable T> d = defaultSupplier != null ? defaultSupplier : () -> null;
            ClassLoader c = cl != null ? cl : ClassLoader.getSystemClassLoader();

            return new SpiLoader<>(type, c, p, d);
        }
    }

    private final Class<T> type;
    private final ClassLoader cl;
    private final Predicate<? super T> predicate;
    private final Supplier<? extends @Nullable T> defaultSupplier;

    private SpiLoader(Class<T> type, ClassLoader cl, Predicate<? super T> p, Supplier<? extends T> d) {
        this.type = type;
        this.cl = cl;
        this.predicate = p;
        this.defaultSupplier = d;
    }

    /**
     * Creates a new instance of LoaderBuilder for the specified type.
     *
     * @param <T> the type of instances to be loaded by the LoaderBuilder
     * @param type the class type of the instances to be loaded
     * @return a new LoaderBuilder instance configured for the specified type
     */
    public static <T> LoaderBuilder<T> builder(Class<T> type) {
        return new LoaderBuilder<>(type);
    }

    /**
     * Loads an implementation of the specified type using the provided class loader and predicate.
     * The method iterates through the available implementations using ServiceLoader.
     * If an implementation is found that matches the predicate, it is returned.
     * Otherwise, a default implementation is returned if available.
     *
     * @return an instance of type T that matches the provided predicate or a default instance if no matching implementation is found
     * @throws IllegalStateException if no implementation is found and no default supplier is available
     */
    public T load() {
        LOG.debug("loading service: {}", type);

        Iterator<T> serviceIterator = ServiceLoader
                .load(type, cl)
                .iterator();

        T instance = null;
        while (instance == null && serviceIterator.hasNext()) {
            T current = serviceIterator.next();
            if (predicate.test(current)) {
                instance = current;
                LOG.debug("accepted implementation: {}", current);
            } else {
                LOG.debug("skipping rejected implementation: {}", current);
            }
        }

        if (instance == null) {
            instance = defaultSupplier.get();

            if (instance == null) {
                throw new IllegalStateException("no implementation found for type: " + type);
            }
            LOG.debug("using default implementation for type: {}", type);
        }

        LOG.debug("loaded implementation for type {}: {}", type, instance);
        return instance;
    }

}
