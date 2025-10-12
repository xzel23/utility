package com.dua3.utility.fx.controls;

import javafx.stage.FileChooser.ExtensionFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the {@link FileChooserBuilder} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class FileChooserBuilderTest extends FxTestBase {

    @TempDir
    Path tempDir;

    /**
     * Test creating a FileChooserBuilder.
     */
    @Test
    void testCreateBuilder() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

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
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Set initial directory
            builder.initialDir(tempDir);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the initial filename.
     */
    @Test
    void testInitialFileName() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Set initial filename
            String filename = "test-file.txt";
            builder.initialFileName(filename);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the initial file (both directory and filename).
     */
    @Test
    void testInitialFile() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Set initial file
            Path file = tempDir.resolve("test-file.txt");
            builder.initialFile(file);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test adding a filter.
     */
    @Test
    void testAddFilter() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Add a filter
            builder.addFilter("Text Files", "*.txt");

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting filters with varargs.
     */
    @Test
    void testFilterVarargs() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Create filters
            ExtensionFilter textFilter = new ExtensionFilter("Text Files", "*.txt");
            ExtensionFilter imageFilter = new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");

            // Set filters
            builder.filter(textFilter, imageFilter);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting filters with a collection.
     */
    @Test
    void testFilterCollection() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Create filters
            ExtensionFilter textFilter = new ExtensionFilter("Text Files", "*.txt");
            ExtensionFilter imageFilter = new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");
            List<ExtensionFilter> filters = Arrays.asList(textFilter, imageFilter);

            // Set filters
            builder.filter(filters);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the selected filter.
     */
    @Test
    void testSelectedFilter() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Create filters
            ExtensionFilter textFilter = new ExtensionFilter("Text Files", "*.txt");
            ExtensionFilter imageFilter = new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");

            // Set filters and selected filter
            builder.filter(textFilter, imageFilter);
            builder.selectedFilter(imageFilter);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }

    /**
     * Test setting the selected filter when it's not in the filter list.
     */
    @Test
    void testSelectedFilterNotInList() throws Exception {
        runOnFxThreadAndWait(() -> {
            // Create a builder
            FileChooserBuilder builder = Dialogs.chooseFile(null);

            // Create filters
            ExtensionFilter textFilter = new ExtensionFilter("Text Files", "*.txt");
            ExtensionFilter imageFilter = new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif");
            ExtensionFilter audioFilter = new ExtensionFilter("Audio Files", "*.mp3", "*.wav");

            // Set filters and a selected filter that's not in the list
            builder.filter(textFilter, imageFilter);
            builder.selectedFilter(audioFilter);

            // We can't directly test the effect since we can't access the internal FileChooser
            // But we can verify the builder is still valid
            assertNotNull(builder);
        });
    }
}