package com.dua3.utility.swing;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FileInput class.
 * 
 * These tests use AssertJ Swing to test the actual FileInput class.
 */
class FileInputTest {

    @BeforeAll
    static void setupCaciocavallo() {
        // Print debug information about the environment
        System.out.println("[DEBUG_LOG] Headless mode: " + GraphicsEnvironment.isHeadless());
        System.out.println("[DEBUG_LOG] AWT Toolkit: " + System.getProperty("awt.toolkit"));
    }

    private FrameFixture window;
    private JFrame frame;
    private Robot robot;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG_LOG] Starting setUp");

        // Create the robot
        robot = BasicRobot.robotWithCurrentAwtHierarchy();

        // Create the frame that will be tested
        frame = SwingTestUtil.createTestFrame();

        // Make sure the frame is not null
        System.out.println("[DEBUG_LOG] Frame created: " + (frame != null));

        // Create a fixture for the frame
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame to test

        System.out.println("[DEBUG_LOG] Window created: " + (window != null));
        System.out.println("[DEBUG_LOG] Frame created and shown in setUp");
    }

    @AfterEach
    void tearDown() {
        // Clean up
        if (window != null) {
            window.cleanUp();
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }

    /**
     * Test that the FileInput component can be created with different selection modes.
     */
    @Test
    void testCreation() {
        System.out.println("[DEBUG_LOG] Starting testCreation");

        // Create FileInput with SELECT_FILE mode and add it to the frame
        FileInput fileInput = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });

        // Verify the FileInput component exists
        assertNotNull(fileInput, "FileInput should not be null");

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
        assertNotNull(fileInputFixture, "FileInput fixture should not be null");
        assertNotNull(textFieldFixture, "Text field fixture should not be null");

        // Verify the text field has the expected initial path
        textFieldFixture.requireText(".");

        System.out.println("[DEBUG_LOG] testCreation completed");
    }

    /**
     * Test that the FileInput component correctly gets and sets paths.
     */
    @Test
    void testGetSetPath() {
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
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(initialPath, path.get(), "Path should match the initial path");

        // Set a new path
        Path newPath = Paths.get("/new/test/path");
        GuiActionRunner.execute(() -> fileInput.setPath(newPath));

        // Verify the text field shows the new path
        textFieldFixture.requireText(newPath.toString());

        // Verify getPath returns the correct path
        path = fileInput.getPath();
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(newPath, path.get(), "Path should match the new path");

        // Set another path by using GuiActionRunner to set the text directly
        Path anotherPath = Paths.get("/another/test/path");
        GuiActionRunner.execute(() -> {
            JTextField textField = (JTextField) textFieldFixture.target();
            textField.setText(anotherPath.toString());
        });

        // Verify getText returns the updated text
        assertEquals(anotherPath.toString(), fileInput.getText(), "Text should be updated to the new path");

        // Verify getPath returns the correct path
        path = fileInput.getPath();
        assertTrue(path.isPresent(), "Path should be present");
        assertEquals(anotherPath, path.get(), "Path should match the new path");

        System.out.println("[DEBUG_LOG] testGetSetPath completed");
    }

    /**
     * Test that the FileInput component correctly handles invalid paths.
     */
    @Test
    void testInvalidPath() {
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
        assertFalse(path.isPresent(), "Path should not be present for invalid path");

        System.out.println("[DEBUG_LOG] testInvalidPath completed");
    }

    /**
     * Test that the FileInput component works with different selection modes.
     * 
     * Note: This test only verifies that the component can be created with different modes.
     * Testing the actual file selection dialog would require user interaction, which is
     * difficult to automate in a headless test environment.
     */
    @Test
    void testSelectionModes() {
        System.out.println("[DEBUG_LOG] Starting testSelectionModes");

        // Create FileInput with SELECT_FILE mode
        FileInput fileInputFile = GuiActionRunner.execute(() -> {
            FileInput fi = new FileInput(FileInput.SELECT_FILE, Paths.get("."), 20);
            frame.getContentPane().add(fi);
            frame.revalidate();
            return fi;
        });
        assertNotNull(fileInputFile, "FileInput with SELECT_FILE should not be null");

        // Create FileInput with SELECT_DIRECTORY mode
        FileInput fileInputDir = GuiActionRunner.execute(() -> 
            new FileInput(FileInput.SELECT_DIRECTORY, Paths.get("."), 20)
        );
        assertNotNull(fileInputDir, "FileInput with SELECT_DIRECTORY should not be null");

        // Create FileInput with SELECT_FILE_OR_DIRECTORY mode
        FileInput fileInputBoth = GuiActionRunner.execute(() -> 
            new FileInput(FileInput.SELECT_FILE_OR_DIRECTORY, Paths.get("."), 20)
        );
        assertNotNull(fileInputBoth, "FileInput with SELECT_FILE_OR_DIRECTORY should not be null");

        System.out.println("[DEBUG_LOG] testSelectionModes completed");
    }

    /**
     * Test the getText method of FileInput.
     */
    @Test
    void testGetText() {
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
        assertEquals(initialPathString, fileInput.getText(), "getText should return the initial path string");
        textFieldFixture.requireText(initialPathString);

        // Modify the text field directly using GuiActionRunner
        String newText = "modified/path";
        GuiActionRunner.execute(() -> {
            JTextField textField = (JTextField) textFieldFixture.target();
            textField.setText(newText);
        });

        // Verify getText returns the updated string
        assertEquals(newText, fileInput.getText(), "getText should return the modified text");

        System.out.println("[DEBUG_LOG] testGetText completed");
    }
}
