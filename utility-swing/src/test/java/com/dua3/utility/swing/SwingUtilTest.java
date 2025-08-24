package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.math.geometry.Path2f;
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
import java.awt.geom.Path2D;
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
        assertDoesNotThrow(() ->
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
        assertDoesNotThrow(() ->
                SwingUtil.addDropTextSupport(testComponent, text -> {
                    // Do nothing in this test
                })
        );
    }

    /**
     * Test the convert method for java.awt.Color to Color conversion.
     */
    @Test
    void testConvertAwtColorToColor() {
        // Test with opaque color
        java.awt.Color awtColor = new java.awt.Color(255, 0, 0);
        Color color = SwingUtil.convert(awtColor);
        RGBColor rgbColor = color.toRGBColor();
        assertEquals(255, rgbColor.r(), "Red component should match");
        assertEquals(0, rgbColor.g(), "Green component should match");
        assertEquals(0, rgbColor.b(), "Blue component should match");
        assertEquals(255, color.a(), "Alpha component should match");

        // Test with transparent color
        java.awt.Color awtColorWithAlpha = new java.awt.Color(255, 0, 0, 128);
        Color colorWithAlpha = SwingUtil.convert(awtColorWithAlpha);
        RGBColor rgbColorWithAlpha = colorWithAlpha.toRGBColor();
        assertEquals(255, rgbColorWithAlpha.r(), "Red component should match");
        assertEquals(0, rgbColorWithAlpha.g(), "Green component should match");
        assertEquals(0, rgbColorWithAlpha.b(), "Blue component should match");
        assertEquals(128, colorWithAlpha.a(), "Alpha component should match");
    }

    /**
     * Test the convert method for Color to java.awt.Color conversion.
     */
    @Test
    void testConvertColorToAwtColor() {
        // Test with opaque color
        Color color = new RGBColor(255, 0, 0);
        java.awt.Color awtColor = SwingUtil.convert(color);
        assertEquals(255, awtColor.getRed(), "Red component should match");
        assertEquals(0, awtColor.getGreen(), "Green component should match");
        assertEquals(0, awtColor.getBlue(), "Blue component should match");
        assertEquals(255, awtColor.getAlpha(), "Alpha component should match");

        // Test with transparent color
        Color colorWithAlpha = new RGBColor(255, 0, 0, 128);
        java.awt.Color awtColorWithAlpha = SwingUtil.convert(colorWithAlpha);
        assertEquals(255, awtColorWithAlpha.getRed(), "Red component should match");
        assertEquals(0, awtColorWithAlpha.getGreen(), "Green component should match");
        assertEquals(0, awtColorWithAlpha.getBlue(), "Blue component should match");
        assertEquals(128, awtColorWithAlpha.getAlpha(), "Alpha component should match");
    }

    /**
     * Test the convertToSwingPath method.
     */
    @Test
    void testConvertToSwingPath() {
        // Create a simple path with a move and a line
        Path2f path = Path2f.builder()
                .moveTo(10, 10)
                .lineTo(20, 20)
                .build();

        // Convert to Swing path
        Path2D swingPath = SwingUtil.convertToSwingPath(path);

        // We can't easily check the path points, but we can verify it's not null
        assertNotNull(swingPath, "Converted path should not be null");
    }

    /**
     * Test the additional createJScrollPane overloads.
     */
    @Test
    void testCreateJScrollPaneOverloads() {
        // Test with vertical and horizontal scroll bar policies
        JScrollPane scrollPane = SwingUtil.createJScrollPane(20, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        assertNotNull(scrollPane, "Scroll pane should not be null");
        assertEquals(20, scrollPane.getVerticalScrollBar().getUnitIncrement(), "Vertical scroll bar should have the correct unit increment");
        assertEquals(20, scrollPane.getHorizontalScrollBar().getUnitIncrement(), "Horizontal scroll bar should have the correct unit increment");
        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, scrollPane.getVerticalScrollBarPolicy(), "Vertical scroll bar policy should match");
        assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, scrollPane.getHorizontalScrollBarPolicy(), "Horizontal scroll bar policy should match");

        // Test with view component and scroll bar policies
        JScrollPane scrollPaneWithView = SwingUtil.createJScrollPane(20, testComponent, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        assertNotNull(scrollPaneWithView, "Scroll pane with view should not be null");
        assertEquals(testComponent, scrollPaneWithView.getViewport().getView(), "Scroll pane should have the correct view component");
        assertEquals(20, scrollPaneWithView.getVerticalScrollBar().getUnitIncrement(), "Vertical scroll bar should have the correct unit increment");
        assertEquals(20, scrollPaneWithView.getHorizontalScrollBar().getUnitIncrement(), "Horizontal scroll bar should have the correct unit increment");
        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, scrollPaneWithView.getVerticalScrollBarPolicy(), "Vertical scroll bar policy should match");
        assertEquals(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, scrollPaneWithView.getHorizontalScrollBarPolicy(), "Horizontal scroll bar policy should match");
    }

    /**
     * Test the scrollToEnd method.
     * This test verifies that the adjustment listener is added correctly.
     */
    @Test
    void testScrollToEnd() {
        // Create a scroll bar
        JScrollBar scrollBar = new JScrollBar();

        // Call scrollToEnd
        SwingUtil.scrollToEnd(scrollBar);

        // Verify that an adjustment listener was added
        // We can't easily check if it's the right one, but we can check that there is at least one
        assertTrue(scrollBar.getAdjustmentListeners().length > 0, "Scroll bar should have at least one adjustment listener");
    }

    /**
     * Test the updateAndScrollToEnd method.
     * This test verifies that the update is performed and scrollToEnd is called when needed.
     */
    @Test
    void testUpdateAndScrollToEnd() {
        // Create a scroll bar with maximum value and visible amount
        JScrollBar scrollBar = new JScrollBar();
        scrollBar.setMaximum(100);
        scrollBar.setValue(90);
        scrollBar.setVisibleAmount(10);

        // Create a flag to track if the update was performed
        AtomicBoolean updatePerformed = new AtomicBoolean(false);

        // Call updateAndScrollToEnd
        SwingUtil.updateAndScrollToEnd(scrollBar, () -> updatePerformed.set(true));

        // Verify the update was performed
        assertTrue(updatePerformed.get(), "Update should have been performed");

        // Verify that an adjustment listener was added (since we were at the end)
        assertTrue(scrollBar.getAdjustmentListeners().length > 0, "Scroll bar should have at least one adjustment listener");
    }
}
