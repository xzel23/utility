package com.dua3.utility.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SpiLoader class.
 * <p>
 * Note: Testing SPI in a unit test is not feasible because of classloader constraints. That's why only some basic
 * functionality is tested gere.
 */
class SpiLoaderTest {

    // Define a test service interface
    interface TestService {
        String getName();
    }

    // Define a test service implementation
    static class TestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "TestServiceImpl";
        }
    }

    // Define another test service implementation
    static class AnotherTestServiceImpl implements TestService {
        @Override
        public String getName() {
            return "AnotherTestServiceImpl";
        }
    }

    @Test
    void testBuilder() {
        // Test that the builder method returns a non-null builder
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class);
        assertNotNull(builder);
    }

    @Test
    void testBuilderClassLoader() {
        // Test that the classLoader method returns the builder for chaining
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class);

        SpiLoader.LoaderBuilder<TestService> result = builder.classLoader(classLoader);

        assertSame(builder, result);
    }

    @Test
    void testBuilderAccept() {
        // Test that the accept method returns the builder for chaining
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class);

        SpiLoader.LoaderBuilder<TestService> result = builder.accept(service -> true);

        assertSame(builder, result);
    }

    @Test
    void testBuilderDefaultSupplier() {
        // Test that the defaultSupplier method returns the builder for chaining
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class);

        SpiLoader.LoaderBuilder<TestService> result = builder.defaultSupplier(TestServiceImpl::new);

        assertSame(builder, result);
    }

    @Test
    void testBuilderBuild() {
        // Test that the build method returns a non-null SpiLoader
        SpiLoader<TestService> loader = SpiLoader.builder(TestService.class).build();

        assertNotNull(loader);
    }

    @Test
    void testBuilderSettingClassLoaderTwice() {
        // Test that setting the class loader twice throws an exception
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class)
                .classLoader(classLoader);

        assertThrows(IllegalStateException.class, () -> builder.classLoader(classLoader));
    }

    @Test
    void testBuilderSettingPredicateTwice() {
        // Test that setting the predicate twice throws an exception
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class)
                .accept(service -> true);

        assertThrows(IllegalStateException.class, () -> builder.accept(service -> false));
    }

    @Test
    void testBuilderSettingDefaultSupplierTwice() {
        // Test that setting the default supplier twice throws an exception
        SpiLoader.LoaderBuilder<TestService> builder = SpiLoader.builder(TestService.class)
                .defaultSupplier(TestServiceImpl::new);

        assertThrows(IllegalStateException.class, () -> builder.defaultSupplier(AnotherTestServiceImpl::new));
    }

}
