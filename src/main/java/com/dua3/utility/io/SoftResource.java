package com.dua3.utility.io;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

public class SoftResource<T> {

    private Supplier<T> supplier;
    private SoftReference<T> ref;

    private SoftResource(Supplier<T> supplier) {
        this.supplier = supplier;
        this.ref = new SoftReference<>(null);
    }

    /**
     * Create a soft resource.
     * @param <T> the type of the resource
     * @param supplier the resource supplier; the supplier should upon each invocation return equal instances
     * @return soft resource
     */
    public static <T> SoftResource<T> of(Supplier<T> supplier) {
        return new SoftResource<>(Objects.requireNonNull(supplier));
    }

    /**
     * Create an empty soft resource.
     * @param <T> the type of the resource
     * @return empty soft resource
     */
    public static <T> SoftResource<T> emptyReference() {
        return new SoftResource<>(null);
    }

    /**
     * Get the resource.
     * @return the resource;
     *          if it has not yet been set or has been garbage collected, it will be
     *          restored by invoking the supplier
     */
    public T get() {
        T  obj = ref.get();
        if (obj==null && supplier!=null) {
            obj = supplier.get();

            if (obj==null) {
                supplier=null;
            }

            ref = new SoftReference<>(obj);
        }
        return obj;
    }

    /**
     * Check if it is already known that {@code get()} will return {@code null} for this soft resource.
     * @return true, if this soft resource is known to be {@code null}
     */
    public boolean isKnownToBeNull() {
        return supplier==null;
    }

    /**
     * Get a strong reference holder for this resource to avoid thrashing.
     * Intended for use in try-with-resources.
     * @return ResourceHolder for this Resource
     */
    public ResourceHolder<T> hold() {
        return new ResourceHolder<>(this);
    }

    /**
     * Helper class to prevent the resource from being garbage collected.
     */
    public static class ResourceHolder<T> implements AutoCloseable {
        private T strong;
        private final SoftResource<T> soft;

        private ResourceHolder(SoftResource<T> sr) {
            this.strong = sr.get();
            this.soft = sr;
        }

        /**
         * Get the resource being held.
         * @return the resource
         */
        public T get() {
            return strong;
        }

        /**
         * Get the SoftResource instance this holder belongs to.
         * @return the soft resource
         */
        public SoftResource<T> getSoftResource() {
            return soft;
        }

        @Override
        public void close() {
            strong = null;
        }

        @Override
        public String toString() {
            return "ResourceHolder("+soft+")";
        }
    }
}
