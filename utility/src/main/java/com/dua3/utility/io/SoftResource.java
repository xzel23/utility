// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.jspecify.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

/**
 * A lazily loaded and cached resource. Instances are created from a supplier that creates the resource on demand
 * when {@link #get()} is called and hold a {@link SoftReference} to it. When {@link #get()} is called the next time,
 * the same instance is returned if it has not yet been garbage collected. Otherwise, the supplier is called again.
 *
 * @param <T> the resource type
 */
public final class SoftResource<T extends @Nullable Object> {

    /**
     * A static final SoftReference holding a null reference.
     * Represents an empty soft reference that can be reused to avoid unnecessary
     * allocations of empty references. This is helpful in scenarios where a common
     * empty reference is required or when representing the absence of a value.
     */
    public static final SoftReference<?> EMPTY_REFERENCE = new SoftReference<>(null);
    /**
     * A constant representing an empty soft resource.
     * This instance is created with a null supplier and serves as a predefined empty resource
     * to avoid unnecessary instantiations of empty resources for generic use cases.
     */
    public static final SoftResource<?> EMPTY_RESOURCE = new SoftResource<>(null);

    private @Nullable Supplier<? extends T> supplier;
    private SoftReference<T> ref;

    @SuppressWarnings("unchecked")
    private SoftResource(@Nullable Supplier<? extends T> supplier) {
        this.supplier = supplier;
        this.ref = (SoftReference<T>) EMPTY_REFERENCE;
    }

    /**
     * Create a soft resource.
     *
     * @param <T>      the type of the resource
     * @param supplier the resource supplier; the supplier should upon each
     *                 invocation return equal instances
     * @return soft resource
     */
    public static <T> SoftResource<T> of(Supplier<? extends T> supplier) {
        return new SoftResource<>(supplier);
    }

    /**
     * Create an empty soft resource.
     *
     * @param <T> the type of the resource
     * @return empty soft resource
     */
    @SuppressWarnings("unchecked")
    public static <T extends @Nullable Object> SoftResource<T> emptyReference() {
        return (SoftResource<T>) EMPTY_RESOURCE;
    }

    /**
     * Get the resource.
     *
     * @return the resource;
     * if it has not yet been set or has been garbage collected, it will be
     * restored by invoking the supplier
     */
    @SuppressWarnings("unchecked")
    public @Nullable T get() {
        T obj = ref.get();
        if (obj == null && supplier != null) {
            obj = supplier.get();

            if (obj == null) {
                supplier = null;
                ref = (SoftReference<T>) EMPTY_REFERENCE;
            } else {
                ref = new SoftReference<>(obj);
            }
        }
        return obj;
    }

    /**
     * Check if it is already known that {@code get()} will return {@code null} for
     * this soft resource.
     *
     * @return true, if this soft resource is known to be {@code null}
     */
    public boolean isKnownToBeNull() {
        return supplier == null;
    }

    /**
     * Get a strong reference holder for this resource to avoid thrashing.
     * Intended for use in try-with-resources.
     *
     * @return ResourceHolder for this Resource
     */
    public ResourceHolder<T> hold() {
        return new ResourceHolder<>(this);
    }

    /**
     * Helper class to prevent the resource from being garbage collected.
     * @param <T> the generic resource type
     */
    public static final class ResourceHolder<T extends @Nullable Object> implements AutoCloseable {
        private final SoftResource<T> soft;
        private T strong;

        private ResourceHolder(SoftResource<T> sr) {
            this.strong = sr.get();
            this.soft = sr;
        }

        /**
         * Get the resource being held.
         *
         * @return the resource
         */
        public T get() {
            return strong;
        }

        /**
         * Get the SoftResource instance this holder belongs to.
         *
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
            return "ResourceHolder(" + soft + ")";
        }
    }
}
