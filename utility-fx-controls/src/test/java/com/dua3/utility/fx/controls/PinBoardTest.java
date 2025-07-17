package com.dua3.utility.fx.controls;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link PinBoard} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class PinBoardTest extends FxTestBase {

    /**
     * Test the constructor and initial state of the PinBoard.
     */
    @Test
    void testConstructor() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Check initial state
            assertEquals(Rectangle2D.EMPTY, pinBoard.getArea());
            assertTrue(pinBoard.getItems().isEmpty());
            assertTrue(pinBoard.getVisibleItems().isEmpty());
            assertTrue(pinBoard.pannableProperty().get());
            assertEquals(1.0, pinBoard.getDisplayScale());
            assertEquals(0.0, pinBoard.scrollHValuePropertyProperty().get());
            assertEquals(0.0, pinBoard.scrollVValuePropertyProperty().get());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test pinning items to the board.
     */
    @Test
    void testPinItems() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Create test items
            PinBoard.Item item1 = createTestItem("Item1", 0, 0, 100, 100);
            PinBoard.Item item2 = createTestItem("Item2", 150, 150, 100, 100);

            // Pin items
            pinBoard.pin(item1);
            assertEquals(1, pinBoard.getItems().size());
            assertTrue(pinBoard.getItems().contains(item1));

            // Pin another item
            pinBoard.pin(item2);
            assertEquals(2, pinBoard.getItems().size());
            assertTrue(pinBoard.getItems().contains(item2));

            // Check area is updated
            Rectangle2D expectedArea = new Rectangle2D(0, 0, 250, 250);
            assertEquals(expectedArea, pinBoard.getArea());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Helper method to create a test item.
     */
    private PinBoard.Item createTestItem(String name, double x, double y, double width, double height) {
        Rectangle2D area = new Rectangle2D(x, y, width, height);
        Supplier<Node> nodeBuilder = () -> new Label(name);
        return new PinBoard.Item(name, area, nodeBuilder);
    }

    /**
     * Test clearing the board.
     */
    @Test
    void testClear() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Add items
            pinBoard.pin(createTestItem("Item1", 0, 0, 100, 100));
            pinBoard.pin(createTestItem("Item2", 150, 150, 100, 100));

            // Clear the board
            pinBoard.clear();

            // Check state after clearing
            assertTrue(pinBoard.getItems().isEmpty());
            assertEquals(Rectangle2D.EMPTY, pinBoard.getArea());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test pinning multiple items at once.
     */
    @Test
    void testPinMultipleItems() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Create test items
            List<PinBoard.Item> items = new ArrayList<>();
            items.add(createTestItem("Item1", 0, 0, 100, 100));
            items.add(createTestItem("Item2", 150, 150, 100, 100));
            items.add(createTestItem("Item3", 300, 300, 100, 100));

            // Pin all items at once
            pinBoard.pin(items);

            // Check all items are added
            assertEquals(3, pinBoard.getItems().size());

            // Check area is updated correctly
            Rectangle2D expectedArea = new Rectangle2D(0, 0, 400, 400);
            assertEquals(expectedArea, pinBoard.getArea());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test setting the display scale.
     */
    @Test
    void testDisplayScale() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Test initial scale
            assertEquals(1.0, pinBoard.getDisplayScale());

            // Test setting scale
            pinBoard.setDisplayScale(2.0);
            assertEquals(2.0, pinBoard.getDisplayScale());

            // Test property binding
            pinBoard.displayScaleProperty().set(0.5);
            assertEquals(0.5, pinBoard.getDisplayScale());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test setting scroll bar policies.
     */
    @Test
    void testScrollBarPolicies() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Set policies
            pinBoard.setHBarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            pinBoard.setVBarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            // Now we can verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test pinning an item at the bottom.
     */
    @Test
    void testPinBottom() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Pin an item at the bottom
            Dimension2D dimension = new Dimension2D(100, 50);
            pinBoard.pinBottom("BottomItem", () -> new Label("Bottom"), dimension);

            // Check item was added
            assertEquals(1, pinBoard.getItems().size());

            // Check item position (should be centered horizontally at the bottom)
            PinBoard.Item item = pinBoard.getItems().getFirst();
            assertEquals("BottomItem", item.name());

            // The item should be centered horizontally (x = -50) and at y = 0
            Rectangle2D area = item.area();
            assertEquals(-50, area.getMinX());
            assertEquals(0, area.getMinY());
            assertEquals(100, area.getWidth());
            assertEquals(50, area.getHeight());

            // Verify the skin is properly initialized
            assertInstanceOf(PinBoardSkin.class, pinBoard.getSkin(), "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test getting item at position.
     */
    @Test
    void testGetItemAt() throws Exception {
        PinBoard pinBoard = new PinBoard();

        runOnFxThreadAndWait(() -> {
            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);
            pinBoard.setVisible(true);

            // Add test items
            PinBoard.Item item1 = createTestItem("Item1", 0, 0, 100, 100);
            PinBoard.Item item2 = createTestItem("Item2", 150, 150, 100, 100);
            pinBoard.pin(item1);
            pinBoard.pin(item2);
        });

        // give JavaFX the oppurtunity to run a layout pass
        Platform.requestNextPulse();
        Thread.sleep(500);
        Platform.requestNextPulse();

        runOnFxThreadAndWait(() -> {
            // Now that the skin is properly initialized, we can test getItemAt
            Optional<PinBoard.Item> foundItem = pinBoard.getItemAt(50, 50);

            // Verify the result
            assertTrue(foundItem.isPresent(), "Should find an item at position (50, 50)");
            assertEquals("Item1", foundItem.get().name(), "Should find item1 at position (50, 50)");
        });
    }

    /**
     * Test getting position in item.
     */
    @Test
    void testGetPositionInItem() throws Exception {
        PinBoard pinBoard = new PinBoard();
        runOnFxThreadAndWait(() -> {
            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Add test items
            PinBoard.Item item1 = createTestItem("Item1", 0, 0, 100, 100);
            PinBoard.Item item2 = createTestItem("Item2", 150, 150, 100, 100);
            pinBoard.pin(item1);
            pinBoard.pin(item2);
        });

        // give JavaFX the oppurtunity to run a layout pass
        Platform.requestNextPulse();
        Thread.sleep(500);
        Platform.requestNextPulse();

        runOnFxThreadAndWait(() -> {
            // Now that the skin is properly initialized, we can test getPositionInItem
            Optional<PinBoard.PositionInItem> position = pinBoard.getPositionInItem(50, 50);

            // Verify the result
            assertTrue(position.isPresent(), "Should find a position in item at (50, 50)");
            assertEquals("Item1", position.get().item().name(), "Should find position in item1");
            assertEquals(50, position.get().x(), "X coordinate should be 50");
            assertEquals(50, position.get().y(), "Y coordinate should be 50");
        });
    }

    /**
     * Test getting position in board.
     */
    @Test
    void testGetPositionInBoard() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Now that the skin is properly initialized, we can test getPositionInBoard
            PinBoard.BoardPosition position = pinBoard.getPositionInBoard(50, 50);

            // Verify the result
            assertNotNull(position, "Position should not be null");
            // The actual position values will depend on the skin's implementation,
            // but we can at least verify that a position is returned
            assertNotEquals(PinBoard.BoardPosition.ORIGIN, position, "Position should not be ORIGIN if skin is properly initialized");
        });
    }

    /**
     * Test setting and getting scroll position.
     */
    @Test
    void testScrollPosition() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();

            // Add to scene to ensure skin is initialized
            addToScene(pinBoard);

            // Set scroll position
            pinBoard.setScrollPosition(0.5, 0.5);

            // Now that the skin is properly initialized, we can test getScrollPosition
            ScrollPosition position = pinBoard.getScrollPosition();

            // Verify the result
            assertNotNull(position, "ScrollPosition should not be null");
            assertEquals(0.5, position.hValue(), 0.01, "Horizontal scroll value should be 0.5");
            assertEquals(0.5, position.vValue(), 0.01, "Vertical scroll value should be 0.5");
        });
    }
}