package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.PlatformHelper;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link PinBoard} class.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS) // Add a global timeout to prevent tests from hanging
class PinBoardTest {

    private static final Object lock = new Object();
    private static boolean platformInitialized = false;
    private static Stage sharedStage;

    /**
     * Initialize the JavaFX platform if it's not already initialized and create the shared Stage.
     */
    @BeforeAll
    public static void initializePlatform() {
        synchronized (lock) {
            if (!platformInitialized) {
                try {
                    Platform.startup(() -> {
                        System.out.println("JavaFX Platform initialized");
                    });
                    platformInitialized = true;
                } catch (IllegalStateException e) {
                    // Platform already running, which is fine
                    System.out.println("JavaFX Platform was already running");
                    platformInitialized = true;
                }
            }
            
            // Initialize the shared Stage
            CountDownLatch stageLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    sharedStage = new Stage();
                    sharedStage.setTitle("PinBoard Test");
                    sharedStage.setWidth(800);
                    sharedStage.setHeight(600);
                } finally {
                    stageLatch.countDown();
                }
            });
            
            try {
                // Wait for the stage to be created with a timeout
                if (!stageLatch.await(10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timed out waiting for Stage to be created");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Stage to be created", e);
            }
        }
    }

    /**
     * Test the constructor and initial state of the PinBoard.
     */
    @Test
    void testConstructor() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();
            
            // Add to scene to ensure skin is initialized
            Scene scene = addToScene(pinBoard);
            
            // Check initial state
            assertEquals(Rectangle2D.EMPTY, pinBoard.getArea());
            assertTrue(pinBoard.getItems().isEmpty());
            assertTrue(pinBoard.getVisibleItems().isEmpty());
            assertTrue(pinBoard.pannableProperty().get());
            assertEquals(1.0, pinBoard.getDisplayScale());
            assertEquals(0.0, pinBoard.scrollHValuePropertyProperty().get());
            assertEquals(0.0, pinBoard.scrollVValuePropertyProperty().get());
            
            // Verify the skin is properly initialized
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Utility method to run code on the JavaFX thread and wait for completion with a timeout.
     */
    private void runOnFxThreadAndWait(Runnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        
        // Add a timeout of 30 seconds to prevent tests from hanging
        if (!latch.await(30, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new java.util.concurrent.TimeoutException("Timed out waiting for JavaFX thread to complete");
        }
    }

    /**
     * Test pinning items to the board.
     */
    @Test
    void testPinItems() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();
            
            // Add to scene to ensure skin is initialized
            Scene scene = addToScene(pinBoard);
            
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
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
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
     * Helper method to add a PinBoard to a Scene and Stage to ensure the skin is properly initialized.
     * This is necessary because the skin is only set when the control is added to the scene graph
     * and the Stage is shown.
     * 
     * @param pinBoard the PinBoard to add to a scene
     * @return the created Scene containing the PinBoard
     */
    private Scene addToScene(PinBoard pinBoard) {
        StackPane root = new StackPane();
        root.getChildren().add(pinBoard);
        Scene scene = new Scene(root, 800, 600);
        
        // Use the shared Stage to ensure the skin is initialized
        PlatformHelper.runAndWait(() -> {
            // Set the scene on the shared stage
            sharedStage.setScene(scene);

            // Make sure the stage is showing
            if (!sharedStage.isShowing()) {
                sharedStage.show();
            }

            // Process a pulse to ensure the scene graph is processed
            Platform.requestNextPulse();
        });

        return scene;
    }

    /**
     * Test clearing the board.
     */
    @Test
    void testClear() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();
            
            // Add to scene to ensure skin is initialized
            Scene scene = addToScene(pinBoard);
            
            // Add items
            pinBoard.pin(createTestItem("Item1", 0, 0, 100, 100));
            pinBoard.pin(createTestItem("Item2", 150, 150, 100, 100));
            
            // Clear the board
            pinBoard.clear();
            
            // Check state after clearing
            assertTrue(pinBoard.getItems().isEmpty());
            assertEquals(Rectangle2D.EMPTY, pinBoard.getArea());
            
            // Verify the skin is properly initialized
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
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
            Scene scene = addToScene(pinBoard);
            
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
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
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
            Scene scene = addToScene(pinBoard);
            
            // Test initial scale
            assertEquals(1.0, pinBoard.getDisplayScale());
            
            // Test setting scale
            pinBoard.setDisplayScale(2.0);
            assertEquals(2.0, pinBoard.getDisplayScale());
            
            // Test property binding
            pinBoard.displayScaleProperty().set(0.5);
            assertEquals(0.5, pinBoard.getDisplayScale());
            
            // Verify the skin is properly initialized
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
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
            Scene scene = addToScene(pinBoard);
            
            // Set policies
            pinBoard.setHBarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            pinBoard.setVBarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            
            // Now we can verify the skin is properly initialized
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
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
            Scene scene = addToScene(pinBoard);
            
            // Pin an item at the bottom
            Dimension2D dimension = new Dimension2D(100, 50);
            pinBoard.pinBottom("BottomItem", () -> new Label("Bottom"), dimension);
            
            // Check item was added
            assertEquals(1, pinBoard.getItems().size());
            
            // Check item position (should be centered horizontally at the bottom)
            PinBoard.Item item = pinBoard.getItems().get(0);
            assertEquals("BottomItem", item.name());
            
            // The item should be centered horizontally (x = -50) and at y = 0
            Rectangle2D area = item.area();
            assertEquals(-50, area.getMinX());
            assertEquals(0, area.getMinY());
            assertEquals(100, area.getWidth());
            assertEquals(50, area.getHeight());
            
            // Verify the skin is properly initialized
            assertTrue(pinBoard.getSkin() instanceof PinBoardSkin, "PinBoard should have a PinBoardSkin");
        });
    }

    /**
     * Test getting item at position.
     */
    @Test
    void testGetItemAt() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();
            
            // Add to scene to ensure skin is initialized
            Scene scene = addToScene(pinBoard);
            
            // Add test items
            PinBoard.Item item1 = createTestItem("Item1", 0, 0, 100, 100);
            PinBoard.Item item2 = createTestItem("Item2", 150, 150, 100, 100);
            pinBoard.pin(item1);
            pinBoard.pin(item2);
            
            // Now that the skin is properly initialized, we can test getItemAt
            Optional<PinBoard.Item> foundItem = pinBoard.getItemAt(50, 50);
            
            // Verify the result
            assertTrue(foundItem.isPresent(), "Should find an item at position (50, 50)");
            assertEquals(item1, foundItem.get(), "Should find item1 at position (50, 50)");
        });
    }

    /**
     * Test getting position in item.
     */
    @Test
    void testGetPositionInItem() throws Exception {
        runOnFxThreadAndWait(() -> {
            PinBoard pinBoard = new PinBoard();
            
            // Add to scene to ensure skin is initialized
            Scene scene = addToScene(pinBoard);
            
            // Add test items
            PinBoard.Item item1 = createTestItem("Item1", 0, 0, 100, 100);
            PinBoard.Item item2 = createTestItem("Item2", 150, 150, 100, 100);
            pinBoard.pin(item1);
            pinBoard.pin(item2);
            
            // Now that the skin is properly initialized, we can test getPositionInItem
            Optional<PinBoard.PositionInItem> position = pinBoard.getPositionInItem(50, 50);
            
            // Verify the result
            assertTrue(position.isPresent(), "Should find a position in item at (50, 50)");
            assertEquals(item1, position.get().item(), "Should find position in item1");
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
            Scene scene = addToScene(pinBoard);
            
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
            Scene scene = addToScene(pinBoard);
            
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