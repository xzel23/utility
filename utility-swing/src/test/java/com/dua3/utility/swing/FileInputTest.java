package com.dua3.utility.swing;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the FileInput class.
 * <p>
 * These tests use AssertJ Swing to test the actual FileInput class.
 */
public class FileInputTest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;
    private JFrame frame;
    private Robot robot;

    @Override
    protected void onSetUp() throws Exception {
        System.out.println("[DEBUG_LOG] Starting onSetUp");

        // Create the frame that will be tested
        frame = SwingTestUtil.createTestFrame();

        // Make sure the frame is not null
        System.out.println("[DEBUG_LOG] Frame created: " + (frame != null));

        // Create a fixture for the frame
        window = new FrameFixture(robot(), frame);
        window.show(); // shows the frame to test

        System.out.println("[DEBUG_LOG] Window created: " + (window != null));
        System.out.println("[DEBUG_LOG] Frame created and shown in onSetUp");
    }

    /**
     * Test that the FileInput component can be created with different selection modes.
     */
    @Test
    public void testCreation() {
        System.out.println("[DEBUG_LOG] Starting testCreation");

        // Create FileInput with SELECT_FILE mode and add it to the frame
        FileInput fileInput = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });

        // Verify the FileInput component exists
        assertNotNull("FileInput should not be null", fileInput);

        // Create fixtures for the FileInput component and its text field
        JPanelFixture fileInputFixture = window.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel component) {
                return component instanceof FileInput;
            }
        });

        JTextComponentFixture textFieldFixture = window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField component) {
                return component.getParent() instanceof FileInput;
            }
        });

        // Verify the fixtures exist
        assertNotNull("FileInput fixture should not be null", fileInputFixture);
        assertNotNull("Text field fixture should not be null", textFieldFixture);

        // Verify the text field has the expected initial path
        textFieldFixture.requireText(".");

        System.out.println("[DEBUG_LOG] testCreation completed");
    }

    /**
     * Test that the FileInput component correctly gets and sets paths.
     */
    @Test
    public void testGetSetPath() {
        System.out.println("[DEBUG_LOG] Starting testGetSetPath");

        // Create FileInput with an initial path and add it to the frame
        Path initialPath = Paths.get("/initial/path");
        FileInput fileInput = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, initialPath, 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });

        // Create fixtures for the FileInput component and its text field
        JPanelFixture fileInputFixture = window.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel component) {
                return component instanceof FileInput;
            }
        });

        JTextComponentFixture textFieldFixture = window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField component) {
                return component.getParent() instanceof FileInput;
            }
        });

        // Verify the initial path
        textFieldFixture.requireText(initialPath.toString());
        Optional<Path> path = fileInput.getPath();
        assertTrue("Path should be present", path.isPresent());
        assertEquals("Path should match the initial path", initialPath, path.get());

        // Set a new path
        Path newPath = Paths.get("/new/test/path");
        GuiActionRunner.execute(() -> fileInput.setPath(newPath));

        // Verify the text field shows the new path
        textFieldFixture.requireText(newPath.toString());

        // Verify getPath returns the correct path
        path = fileInput.getPath();
        assertTrue("Path should be present", path.isPresent());
        assertEquals("Path should match the new path", newPath, path.get());

        // Set another path by using GuiActionRunner to set the text directly
        Path anotherPath = Paths.get("/another/test/path");
        GuiActionRunner.execute(() -> {
            JTextField textField = (JTextField) textFieldFixture.target();
            textField.setText(anotherPath.toString());
        });

        // Verify getText returns the updated text
        assertEquals("Text should be updated to the new path", anotherPath.toString(), fileInput.getText());

        // Verify getPath returns the correct path
        path = fileInput.getPath();
        assertTrue("Path should be present", path.isPresent());
        assertEquals("Path should match the new path", anotherPath, path.get());

        System.out.println("[DEBUG_LOG] testGetSetPath completed");
    }

    /**
     * Test that the FileInput component correctly handles invalid paths.
     */
    @Test
    public void testInvalidPath() {
        System.out.println("[DEBUG_LOG] Starting testInvalidPath");

        // Create FileInput with a valid initial path and add it to the frame
        FileInput fileInput = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });

        // Create fixtures for the FileInput component and its text field
        JPanelFixture fileInputFixture = window.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel component) {
                return component instanceof FileInput;
            }
        });

        JTextComponentFixture textFieldFixture = window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField component) {
                return component.getParent() instanceof FileInput;
            }
        });

        // Set an invalid path text
        // Use a path with a null character, which is definitely invalid on all platforms
        String invalidPath = "invalid\0path";
        GuiActionRunner.execute(() -> {
            // We need to set the text directly since the text field won't accept the null character
            JTextField textField = (JTextField) textFieldFixture.target();
            textField.setText(invalidPath);
        });

        // Verify that getPath returns an empty Optional
        Optional<Path> path = fileInput.getPath();
        assertFalse("Path should not be present for invalid path", path.isPresent());

        System.out.println("[DEBUG_LOG] testInvalidPath completed");
    }

    /**
     * Test that the FileInput component works with different selection modes.
     * <p>
     * Note: This test only verifies that the component can be created with different modes.
     * Testing the actual file selection dialog would require user interaction, which is
     * difficult to automate in a headless test environment.
     */
    @Test
    public void testSelectionModes() {
        System.out.println("[DEBUG_LOG] Starting testSelectionModes");

        // Create FileInput with SELECT_FILE mode
        FileInput fileInputFile = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });
        assertNotNull("FileInput with SELECT_FILE should not be null", fileInputFile);

        // Create FileInput with SELECT_DIRECTORY mode
        FileInput fileInputDir = GuiActionRunner.execute(() ->
                new FileInput(FileInput.SELECT_DIRECTORY, Paths.get("."), 20)
        );
        assertNotNull("FileInput with SELECT_DIRECTORY should not be null", fileInputDir);

        // Create FileInput with SELECT_FILE_OR_DIRECTORY mode
        FileInput fileInputBoth = GuiActionRunner.execute(() ->
                new FileInput(FileInput.SELECT_FILE_OR_DIRECTORY, Paths.get("."), 20)
        );
        assertNotNull("FileInput with SELECT_FILE_OR_DIRECTORY should not be null", fileInputBoth);

        System.out.println("[DEBUG_LOG] testSelectionModes completed");
    }

    /**
     * Test the getText method of FileInput.
     */
    @Test
    public void testGetText() {
        System.out.println("[DEBUG_LOG] Starting testGetText");

        // Create FileInput with a specific initial path and add it to the frame
        String initialPathString = "/test/path";
        Path initialPath = Paths.get(initialPathString);
        FileInput fileInput = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, initialPath, 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });

        // Create fixtures for the FileInput component and its text field
        JPanelFixture fileInputFixture = window.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
            @Override
            protected boolean isMatching(JPanel component) {
                return component instanceof FileInput;
            }
        });

        JTextComponentFixture textFieldFixture = window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField component) {
                return component.getParent() instanceof FileInput;
            }
        });

        // Verify getText returns the correct string
        assertEquals("getText should return the initial path string", initialPathString, fileInput.getText());
        textFieldFixture.requireText(initialPathString);

        // Modify the text field directly using GuiActionRunner
        String newText = "modified/path";
        GuiActionRunner.execute(() -> {
            JTextField textField = (JTextField) textFieldFixture.target();
            textField.setText(newText);
        });

        // Verify getText returns the updated string
        assertEquals("getText should return the modified text", newText, fileInput.getText());

        System.out.println("[DEBUG_LOG] testGetText completed");
    }
}