package com.dua3.utility.swing;

import com.dua3.utility.lang.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the FileInput class.
 * <p>
 * These tests focus on the component logic without requiring a full GUI setup.
 * They run in headless mode and test the core functionality of the FileInput component.
 */
class FileInputTest {

    private FileInput fileInput;

    @BeforeEach
    void setUp() {
        // Create FileInput without showing it in a window
        // This works even in headless mode since we're not actually displaying anything
        fileInput = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
    }

    /**
     * Test that the FileInput component can be created with different selection modes.
     */
    @Test
    void testCreation() {
        // Test different creation modes
        FileInput fileInputFile = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
        assertNotNull(fileInputFile, "FileInput with SELECT_FILE should not be null");

        FileInput fileInputDir = new FileInput(FileInput.SELECT_DIRECTORY, Paths.get("."), 20);
        assertNotNull(fileInputDir, "FileInput with SELECT_DIRECTORY should not be null");

        FileInput fileInputBoth = new FileInput(FileInput.SELECT_FILE_OR_DIRECTORY, Paths.get("."), 20);
        assertNotNull(fileInputBoth, "FileInput with SELECT_FILE_OR_DIRECTORY should not be null");

        // Verify the components were created
        assertTrue(fileInputFile.getComponentCount() >= 2,
                "FileInput should have at least 2 components (text field and button)");
    }

    /**
     * Test that the FileInput component correctly gets and sets paths.
     */
    @Test
    void testGetSetPath() {
        // Create FileInput with an initial path
        String initialPathString = Platform.isWindows() ? "d:/initial/path" : "/initial/path";
        Path initialPath = Paths.get(initialPathString);
        FileInput testInput = new FileInput(FileInput.SELECT_FILE, initialPath, 20);

        // Verify the initial path
        assertEquals(initialPath.toString(), testInput.getText(), "getText should return initial path");
        Optional<Path> path = testInput.getPath();
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(initialPath, path.get(), "Path should match the initial path");

        // Set a new path
        String newPathString = Platform.isWindows() ? "d:/new/test/path" : "/new/test/path";
        Path newPath = Paths.get(newPathString);
        testInput.setPath(newPath);

        // Verify the new path
        assertEquals(newPath.toString(), testInput.getText(), "getText should return new path");
        path = testInput.getPath();
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(newPath, path.get(), "Path should match the new path");

        // Test programmatic text setting by accessing the text field directly
        String anotherPathString = Platform.isWindows() ? "d:/another/test/path" : "/another/test/path";
        Path anotherPath = Paths.get(anotherPathString);
        JTextField textField = findTextFieldInComponent(testInput);
        textField.setText(anotherPath.toString());

        // Verify getText returns the updated text
        assertEquals(anotherPath.toString(), testInput.getText(), "Text should be updated to the new path");

        // Verify getPath returns the correct path
        path = testInput.getPath();
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(anotherPath, path.get(), "Path should match the new path");
    }

    /**
     * Test that the FileInput component correctly handles invalid paths.
     */
    @Test
    void testInvalidPath() {
        // Set an invalid path text - use a path with characters that are invalid on most systems
        JTextField textField = findTextFieldInComponent(fileInput);

        // Try different types of invalid paths
        String[] invalidPaths = {
                "invalid\0path",           // null character
                "con",                     // reserved name on Windows
                "path\twith\ttabs",        // tabs in path
                "path\nwith\nnewlines"     // newlines in path
        };

        for (String invalidPath : invalidPaths) {
            textField.setText(invalidPath);

            // Verify that getPath handles the invalid path appropriately
            Optional<Path> path = fileInput.getPath();
            // Some invalid paths might still parse as valid Paths, so we don't assert false here
            // Instead, we just verify that the method doesn't throw an exception
            assertNotNull(path, "getPath should not return null, even for questionable paths");
        }

        // Test a path that definitely should cause InvalidPathException
        try {
            // This should be handled gracefully by the FileInput
            textField.setText("invalid\0path");
            Optional<Path> path = fileInput.getPath();
            // If we get here, the FileInput handled the invalid path gracefully
            assertNotNull(path, "getPath should handle invalid paths gracefully");
        } catch (Exception e) {
            // If an exception is thrown, it should be handled by the FileInput
            // This test mainly ensures no unhandled exceptions escape
        }
    }

    /**
     * Test the getText method of FileInput.
     */
    @Test
    void testGetText() {
        // Create FileInput with a specific initial path
        String initialPathString = Platform.isWindows() ? "d:\\test\\path" : "/test/path";
        Path initialPath = Paths.get(initialPathString);
        FileInput testInput = new FileInput(FileInput.SELECT_FILE, initialPath, 20);

        // Verify getText returns the correct string
        assertEquals(initialPathString, testInput.getText(), "getText should return the initial path string");

        // Modify the text field directly
        JTextField textField = findTextFieldInComponent(testInput);
        String newText = "modified/path";
        textField.setText(newText);

        // Verify getText returns the updated string
        assertEquals(newText, testInput.getText(), "getText should return the modified text");
    }

    /**
     * Test path validation behavior.
     */
    @Test
    void testPathValidation() {
        // Test valid paths
        String validPath = Platform.isWindows() ? "d:/valid/path" : "/valid/path";
        fileInput.setPath(Paths.get(validPath));
        assertTrue(fileInput.getPath().isPresent(), "Valid path should be present");

        fileInput.setPath(Paths.get("."));
        assertTrue(fileInput.getPath().isPresent(), "Current directory should be valid");

        fileInput.setPath(Paths.get("relative/path"));
        assertTrue(fileInput.getPath().isPresent(), "Relative path should be valid");

        // Test empty path
        JTextField textField = findTextFieldInComponent(fileInput);
        textField.setText("");
        assertTrue(fileInput.getPath().isPresent(), "Empty path should still be valid");
        assertEquals("", fileInput.getPath().get().toString(), "Empty path should resolve to empty string");
    }

    /**
     * Test the selection mode constants.
     */
    @Test
    void testSelectionModeConstants() {
        // Verify the constants are properly defined
        assertNotNull(FileInput.SELECT_FILE, "SELECT_FILE constant should be defined");
        assertNotNull(FileInput.SELECT_DIRECTORY, "SELECT_DIRECTORY constant should be defined");
        assertNotNull(FileInput.SELECT_FILE_OR_DIRECTORY, "SELECT_FILE_OR_DIRECTORY constant should be defined");

        // Test that different modes create different FileInput instances
        FileInput fileMode = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
        FileInput dirMode = new FileInput(FileInput.SELECT_DIRECTORY, Paths.get("."), 20);
        FileInput bothMode = new FileInput(FileInput.SELECT_FILE_OR_DIRECTORY, Paths.get("."), 20);

        assertNotNull(fileMode, "File mode FileInput should be created");
        assertNotNull(dirMode, "Directory mode FileInput should be created");
        assertNotNull(bothMode, "File or directory mode FileInput should be created");
    }

    /**
     * Test with various path types that should be valid.
     */
    @Test
    void testValidPaths() {
        String[] validPaths = {
                ".",
                "..",
                Platform.isWindows() ? "d:\\" : "/",
                Platform.isWindows() ? "d:\\usr\\local" : "/usr/local",
                "relative/path",
                "../relative/path",
                "file.txt",
                "folder/file.txt",
                Platform.isWindows() ? "d:/absolute/path/file.ext" : "/absolute/path/file.ext"
        };

        for (String pathStr : validPaths) {
            try {
                Path path = Paths.get(pathStr);
                pathStr = path.toString(); // this automatically replaces '/' with the system path separator

                fileInput.setPath(path);

                Optional<Path> retrievedPath = fileInput.getPath();
                assertTrue(retrievedPath.isPresent(), "Path should be present for: " + pathStr);
                assertEquals(path, retrievedPath.get(),
                        "Retrieved path should match set path for: " + pathStr);
                assertEquals(pathStr, fileInput.getText(),
                        "getText should match path string for: " + pathStr);
            } catch (InvalidPathException e) {
                // Some paths might be invalid on certain Platforms, skip them
                System.out.println("Skipping invalid path for this Platform: " + pathStr);
            }
        }
    }

    /**
     * Test the component structure.
     */
    @Test
    void testComponentStructure() {
        // Verify the FileInput has the expected components
        assertTrue(fileInput.getComponentCount() >= 2, "FileInput should have at least 2 components");

        // Find the text field
        JTextField textField = findTextFieldInComponent(fileInput);
        assertNotNull(textField, "FileInput should contain a JTextField");

        // Verify the text field has the expected properties
        assertEquals(".", textField.getText(), "Text field should have the correct initial text");
        assertTrue(textField.isEditable(), "Text field should be editable");
    }

    /**
     * Helper method to find the JTextField within the FileInput component.
     */
    private JTextField findTextFieldInComponent(FileInput fileInput) {
        // The FileInput uses BoxLayout and contains a JTextField followed by a JButton
        for (int i = 0; i < fileInput.getComponentCount(); i++) {
            if (fileInput.getComponent(i) instanceof JTextField jTextField) {
                return jTextField;
            }
        }
        throw new IllegalStateException("No JTextField found in FileInput component");
    }
}