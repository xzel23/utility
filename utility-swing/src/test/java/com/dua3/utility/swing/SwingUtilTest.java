package com.dua3.utility.swing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the SwingUtil class.
 * <p>
 * These tests focus on the utility methods without requiring a full GUI setup.
 * They run in headless mode and test the core functionality of the SwingUtil class.
 */
class SwingUtilTest {

    private JComponent testComponent;

    @BeforeEach
    void setUp() {
        // Create a simple component for testing
        testComponent = new JTextField();
    }

    /**
     * Test the createAction method with a Consumer.
     */
    @Test
    void testCreateActionWithConsumer() {
        // Create a flag to track if the action was performed
        AtomicBoolean actionPerformed = new AtomicBoolean(false);
        
        // Create an action that sets the flag when performed
        Action action = SwingUtil.createAction("Test Action", (ActionEvent e) -> actionPerformed.set(true));
        
        // Verify the action was created with the correct name
        assertEquals("Test Action", action.getValue(Action.NAME), "Action should have the correct name");
        
        // Perform the action
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test"));
        
        // Verify the action was performed
        assertTrue(actionPerformed.get(), "Action should have been performed");
    }

    /**
     * Test the createAction method with a Runnable.
     */
    @Test
    void testCreateActionWithRunnable() {
        // Create a flag to track if the action was performed
        AtomicBoolean actionPerformed = new AtomicBoolean(false);
        
        // Create an action that sets the flag when performed
        Action action = SwingUtil.createAction("Test Action", () -> actionPerformed.set(true));
        
        // Verify the action was created with the correct name
        assertEquals("Test Action", action.getValue(Action.NAME), "Action should have the correct name");
        
        // Perform the action
        action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "test"));
        
        // Verify the action was performed
        assertTrue(actionPerformed.get(), "Action should have been performed");
    }

    /**
     * Test the createJScrollPane methods.
     */
    @Test
    void testCreateJScrollPane() {
        // Test creating a scroll pane with a specific unit increment
        JScrollPane scrollPane = SwingUtil.createJScrollPane(20);
        assertNotNull(scrollPane, "Scroll pane should not be null");
        assertEquals(20, scrollPane.getVerticalScrollBar().getUnitIncrement(), "Vertical scroll bar should have the correct unit increment");
        assertEquals(20, scrollPane.getHorizontalScrollBar().getUnitIncrement(), "Horizontal scroll bar should have the correct unit increment");
        
        // Test creating a scroll pane with a view component
        JScrollPane scrollPaneWithView = SwingUtil.createJScrollPane(20, testComponent);
        assertNotNull(scrollPaneWithView, "Scroll pane with view should not be null");
        assertEquals(testComponent, scrollPaneWithView.getViewport().getView(), "Scroll pane should have the correct view component");
    }

    /**
     * Test the setUnitIncrement methods.
     */
    @Test
    void testSetUnitIncrement() {
        // Test setting unit increment on a scroll bar
        JScrollBar scrollBar = new JScrollBar();
        SwingUtil.setUnitIncrement(scrollBar, 25);
        assertEquals(25, scrollBar.getUnitIncrement(), "Scroll bar should have the correct unit increment");
        
        // Test setting unit increment on a scroll pane
        JScrollPane scrollPane = new JScrollPane();
        SwingUtil.setUnitIncrement(scrollPane, 30);
        assertEquals(30, scrollPane.getVerticalScrollBar().getUnitIncrement(), "Vertical scroll bar should have the correct unit increment");
        assertEquals(30, scrollPane.getHorizontalScrollBar().getUnitIncrement(), "Horizontal scroll bar should have the correct unit increment");
    }

    /**
     * Test the setRenderingQualityHigh method.
     */
    @Test
    void testSetRenderingQualityHigh() {
        // Create a graphics context
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        // Set rendering quality high
        SwingUtil.setRenderingQualityHigh(g2d);
        assertEquals(RenderingHints.VALUE_RENDER_QUALITY, g2d.getRenderingHint(RenderingHints.KEY_RENDERING));
        
        // Clean up
        g2d.dispose();
    }

    /**
     * Test the addDropFilesSupport method.
     * This test only verifies that the method doesn't throw exceptions.
     */
    @Test
    void testAddDropFilesSupport() {
        // Add drop files support to the test component
        assertDoesNotThrow( () ->
                SwingUtil.addDropFilesSupport(testComponent, files -> {
                    // Do nothing in this test
                })
        );
    }

    /**
     * Test the addDropTextSupport method.
     * This test only verifies that the method doesn't throw exceptions.
     */
    @Test
    void testAddDropTextSupport() {
        // Add drop text support to the test component
        assertDoesNotThrow( () ->
                SwingUtil.addDropTextSupport(testComponent, text -> {
                    // Do nothing in this test
                })
        );
    }
}