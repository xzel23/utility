package com.dua3.utility.fx.controls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link DirectoryChooserBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class DirectoryChooserBuilderTest extends FxTestBase {

    @TempDir
    Path tempDir;

    /**
     * Test creating a DirectoryChooserBuilder.
     */
    @Test
    void testCreateBuilder() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            DirectoryChooserBuilder builder = Dialogs.chooseDirectory();

            // Verify builder was created
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the initial directory.
     */
    @Test
    void testInitialDir() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            DirectoryChooserBuilder builder = Dialogs.chooseDirectory();

            // Set initial directory
            builder.initialDir(tempDir);

            // We can't directly test the effect since we can't access the internal DirectoryChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the initial directory to null (should default to user home).
     */
    @Test
    void testInitialDirNull() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            DirectoryChooserBuilder builder = Dialogs.chooseDirectory();

            // Set initial directory to null
            builder.initialDir(null);

            // We can't directly test the effect since we can't access the internal DirectoryChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test method chaining.
     */
    @Test
    void testMethodChaining() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder and chain methods
            DirectoryChooserBuilder builder = Dialogs.chooseDirectory().initialDir(tempDir);

            // Verify builder is valid after chaining
            assertNotNull(builder);
        });
    }
}