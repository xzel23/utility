package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.RGBColor;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
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
@Timeout(value = 30, unit = TimeUnit.SECONDS)
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

    @Test
    void testSetRenderingQualityHighSetsAllQualityHints() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        try {
            SwingUtil.setRenderingQualityHigh(graphics);

            assertEquals(RenderingHints.VALUE_ANTIALIAS_ON, graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING));
            assertEquals(RenderingHints.VALUE_TEXT_ANTIALIAS_ON, graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
            assertEquals(RenderingHints.VALUE_COLOR_RENDER_QUALITY, graphics.getRenderingHint(RenderingHints.KEY_COLOR_RENDERING));
            assertEquals(RenderingHints.VALUE_STROKE_NORMALIZE, graphics.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL));
            assertEquals(RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY, graphics.getRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION));
            assertEquals(RenderingHints.VALUE_INTERPOLATION_BICUBIC, graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION));
        } finally {
            graphics.dispose();
        }
    }

    @Test
    void testGetDisplayScaleForUnrealizedComponent() {
        assertEquals(Scale2f.identity(), SwingUtil.getDisplayScale(testComponent));
    }

    @Test
    void testScrollToEndMovesToMaximumAndRemovesListener() {
        JScrollBar scrollBar = new JScrollBar();
        scrollBar.setMaximum(100);
        scrollBar.setVisibleAmount(10);
        scrollBar.setValue(25);

        SwingUtil.scrollToEnd(scrollBar);
        assertEquals(1, scrollBar.getAdjustmentListeners().length);

        scrollBar.setValue(50);

        assertEquals(90, scrollBar.getValue());
        assertEquals(0, scrollBar.getAdjustmentListeners().length);
    }

    @Test
    void testUpdateAndScrollToEndDoesNotInstallListenerWhenNotAtEnd() {
        JScrollBar scrollBar = new JScrollBar();
        scrollBar.setMaximum(100);
        scrollBar.setVisibleAmount(10);
        scrollBar.setValue(25);
        AtomicBoolean updatePerformed = new AtomicBoolean();

        SwingUtil.updateAndScrollToEnd(scrollBar, () -> updatePerformed.set(true));

        assertTrue(updatePerformed.get());
        assertEquals(25, scrollBar.getValue());
        assertEquals(0, scrollBar.getAdjustmentListeners().length);
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

    @Test
    void testConvertToSwingPathPreservesCurvesAndClosePath() {
        Path2f path = Path2f.builder()
                .moveTo(10, 10)
                .lineTo(100, 10)
                .curveTo(new Vector2f(150, 50), new Vector2f(100, 100))
                .curveTo(new Vector2f(80, 120), new Vector2f(40, 120), new Vector2f(10, 100))
                .arcTo(new Vector2f(10, 10), new Vector2f(50, 50), 45, false, false)
                .closePath()
                .build();

        PathIterator iterator = SwingUtil.convertToSwingPath(path).getPathIterator(null);
        int moveCount = 0;
        int lineCount = 0;
        int quadCount = 0;
        int cubicCount = 0;
        int closeCount = 0;
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(new float[6])) {
                case PathIterator.SEG_MOVETO -> moveCount++;
                case PathIterator.SEG_LINETO -> lineCount++;
                case PathIterator.SEG_QUADTO -> quadCount++;
                case PathIterator.SEG_CUBICTO -> cubicCount++;
                case PathIterator.SEG_CLOSE -> closeCount++;
                default -> throw new AssertionError("Unexpected path segment");
            }
            iterator.next();
        }

        assertEquals(2, moveCount, "The arc approximation starts a new subpath");
        assertEquals(1, lineCount);
        assertEquals(1, quadCount);
        assertTrue(cubicCount >= 2, "The cubic curve and arc should produce cubic segments");
        assertEquals(1, closeCount);
    }

    @Test
    void testClipboardOperationsAreSafeInHeadlessMode() {
        assertDoesNotThrow(() -> SwingUtil.copyToClipboard("text"));
        assertDoesNotThrow(() -> SwingUtil.copyToClipboard(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));
        assertDoesNotThrow(() -> SwingUtil.copyToClipboard(java.nio.file.Path.of("test.txt")));

        if (GraphicsEnvironment.isHeadless()) {
            assertTrue(SwingUtil.getStringFromClipboard().isEmpty());
            assertTrue(SwingUtil.getTextFromClipboard().isEmpty());
            assertTrue(SwingUtil.getImageFromClipboard().isEmpty());
            assertTrue(SwingUtil.getFilesFromClipboard().isEmpty());
        }
    }

    @Test
    void testSetNativeLookAndFeelIsSafe() {
        assertDoesNotThrow(() -> SwingUtil.setNativeLookAndFeel("SwingUtilTest"));
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
