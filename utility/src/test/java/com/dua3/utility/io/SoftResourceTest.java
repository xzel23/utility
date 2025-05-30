// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the SoftResource class.
 */
class SoftResourceTest {

    /**
     * Test the of method.
     */
    @Test
    void testOf() {
        String testValue = "test";
        SoftResource<String> resource = SoftResource.of(() -> testValue);

        // The resource should be created by the supplier
        assertEquals(testValue, resource.get());

        // The resource should not be known to be null
        assertFalse(resource.isKnownToBeNull());
    }

    /**
     * Test the emptyReference method.
     */
    @Test
    void testEmptyReference() {
        SoftResource<String> resource = SoftResource.emptyReference();

        // The resource should be null
        assertNull(resource.get());

        // The resource should be known to be null
        assertTrue(resource.isKnownToBeNull());
    }

    /**
     * Test the get method.
     */
    @Test
    void testGet() {
        AtomicInteger counter = new AtomicInteger(0);
        String testValue = "test";

        SoftResource<String> resource = SoftResource.of(() -> {
            counter.incrementAndGet();
            return testValue;
        });

        // The first call to get should invoke the supplier
        assertEquals(testValue, resource.get());
        assertEquals(1, counter.get());

        // Subsequent calls should return the cached value without invoking the supplier again
        assertEquals(testValue, resource.get());
        assertEquals(1, counter.get());
    }

    /**
     * Test the get method with a null-returning supplier.
     */
    @Test
    void testGetWithNullSupplier() {
        AtomicInteger counter = new AtomicInteger(0);

        SoftResource<String> resource = SoftResource.of(() -> {
            counter.incrementAndGet();
            return null;
        });

        // The first call to get should invoke the supplier
        assertNull(resource.get());
        assertEquals(1, counter.get());

        // The resource should now be known to be null
        assertTrue(resource.isKnownToBeNull());

        // Subsequent calls should return null without invoking the supplier again
        assertNull(resource.get());
        assertEquals(1, counter.get());
    }

    /**
     * Test the isKnownToBeNull method.
     */
    @Test
    void testIsKnownToBeNull() {
        // A resource with a non-null value should not be known to be null
        SoftResource<String> resource1 = SoftResource.of(() -> "test");
        assertFalse(resource1.isKnownToBeNull());

        // A resource with a null value should be known to be null after get() is called
        SoftResource<String> resource2 = SoftResource.of(() -> null);
        assertFalse(resource2.isKnownToBeNull()); // Not known to be null before get() is called
        resource2.get(); // This will set the supplier to null
        assertTrue(resource2.isKnownToBeNull()); // Now known to be null

        // An empty reference should be known to be null
        SoftResource<String> resource3 = SoftResource.emptyReference();
        assertTrue(resource3.isKnownToBeNull());
    }

    /**
     * Test the hold method.
     */
    @Test
    void testHold() {
        String testValue = "test";
        SoftResource<String> resource = SoftResource.of(() -> testValue);

        try (SoftResource.ResourceHolder<String> holder = resource.hold()) {
            // The holder should have the same value as the resource
            assertEquals(testValue, holder.get());

            // The holder should reference the original resource
            assertSame(resource, holder.getSoftResource());
        }
    }

    /**
     * Test the ResourceHolder class.
     */
    @Test
    void testResourceHolder() {
        String testValue = "test";
        SoftResource<String> resource = SoftResource.of(() -> testValue);

        SoftResource.ResourceHolder<String> holder = resource.hold();

        // The holder should have the same value as the resource
        assertEquals(testValue, holder.get());

        // The holder should reference the original resource
        assertSame(resource, holder.getSoftResource());

        // The holder's toString should contain the resource
        assertTrue(holder.toString().contains("ResourceHolder"));

        // After closing, the holder should return null because the strong reference is nullified
        holder.close();
        assertNull(holder.get());
    }

    /**
     * Test the EMPTY_REFERENCE constant.
     */
    @Test
    void testEmptyReferenceConstant() {
        assertNull(SoftResource.EMPTY_REFERENCE.get());
    }

    /**
     * Test the EMPTY_RESOURCE constant.
     */
    @Test
    void testEmptyResourceConstant() {
        assertNull(SoftResource.EMPTY_RESOURCE.get());
        assertTrue(SoftResource.EMPTY_RESOURCE.isKnownToBeNull());
    }
}
