package com.dua3.utility.spi;

import com.dua3.utility.lang.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Loader<T> {
    private static final Logger LOG = LogManager.getLogger(Loader.class);

    public static class LoaderBuilder<T> {
        private final Class<T> type;
        private Predicate<T> predicate;
        private Supplier<? extends T> defaultSupplier;

        public LoaderBuilder(Class<T> type) {
            this.type = type;
        }

        public LoaderBuilder<T> accept(Predicate<T> predicate) {
            LangUtil.check(this.predicate == null, "predicate already set");
            this.predicate = predicate;
            return this;
        }

        public LoaderBuilder<T> defaultSupplier(Supplier<? extends T> defaultSupplier) {
            LangUtil.check(this.defaultSupplier == null, "default supplier already set");
            this.defaultSupplier = defaultSupplier;
            return this;
        }

        public Loader<T> build() {
            Predicate<T> p = predicate != null ? predicate : t -> true;
            Supplier<? extends T> d = this.defaultSupplier != null ? defaultSupplier : () -> null;
            return new Loader<>(type, p, d);
        }
    }

    private final Class<T> type;
    private final Predicate<T> predicate;
    private final Supplier<? extends T> defaultSupplier;

    private Loader(Class<T> type, Predicate<T> p, Supplier<? extends T> d) {
        this.type = type;
        this.predicate = p;
        this.defaultSupplier = d;
    }

    public static <T> LoaderBuilder<T> builder(Class<T> type) {
        return new LoaderBuilder<>(type);
    }

    public T load() {
        LOG.debug("loading service: {}", type);
        Iterator<T> serviceIterator = ServiceLoader
                .load(type)
                .iterator();

        T instance = null;
        while (instance ==null && serviceIterator.hasNext()) {
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
