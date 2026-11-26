package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPaneTextureTest extends FxTestBase {
    private static final String WIDE_LINE = "M".repeat(700);
    private static final String LARGE_TEXT = WIDE_LINE + "\n" + "line\n".repeat(600) + WIDE_LINE;

    @Test
    void textPaneTilesOversizedContent() throws Exception {
        runOnFxThreadAndWait(() -> checkScrolling(new TextPane(LARGE_TEXT)));
    }

    @Test
    void editorTilesOversizedContent() throws Exception {
        runOnFxThreadAndWait(() -> checkScrolling(new TextEditorPane(LARGE_TEXT)));
    }

    @Test
    void wrappedContentBackgroundIsLimitedToViewport() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextPane pane = new TextPane("word ".repeat(20_000));
            pane.setWrapText(true);
            StackPane root = new StackPane(pane);
            new Scene(root, 240, 600);
            root.applyCss();
            root.layout();

            ScrollPane scroll = pane.getScrollPane();
            assertNotNull(scroll);
            assertTrue(scroll.getContent().getLayoutBounds().getHeight() > 4096,
                    "Narrow wrapped content must exceed the texture limit");
            double scale = outputScale();
            List<Region> backgrounds = pane.lookupAll(".content").stream()
                    .map(Region.class::cast)
                    .toList();
            assertTrue(!backgrounds.isEmpty());
            for (Region background : backgrounds) {
                assertTrue(background.getWidth() * scale <= TiledCanvas.MAX_TEXTURE_SIZE);
                assertTrue(background.getHeight() * scale <= TiledCanvas.MAX_TEXTURE_SIZE);
            }
        });
    }

    private static void checkScrolling(TextPane pane) {
        pane.setWrapText(false);
        StackPane root = new StackPane(pane);
        new Scene(root, 800, 600);
        root.applyCss();
        root.layout();

        ScrollPane scroll = pane.getScrollPane();
        assertNotNull(scroll);
        Node content = scroll.getContent();
        double width = content.getLayoutBounds().getWidth();
        double height = content.getLayoutBounds().getHeight();
        assertTrue(width > 4096 && height > 4096, "Test content must exceed the limit in both axes");
        List<Canvas> initial = checkedCanvases(content);
        assertTrue(initial.stream().anyMatch(tile -> tile.getLayoutX() == 0 && tile.getLayoutY() == 0));

        scroll.setHvalue(scroll.getHmax());
        scroll.setVvalue(scroll.getVmax());
        root.layout();

        List<Canvas> end = checkedCanvases(content);
        assertTrue(end.stream().anyMatch(tile -> tile.getLayoutX() > 0 && tile.getLayoutY() > 0
                && tile.getLayoutX() + tile.getWidth() >= width - 1
                && tile.getLayoutY() + tile.getHeight() >= height - 1), "Bottom-right content must still be rendered");
    }

    private static List<Canvas> checkedCanvases(Node content) {
        double scale = outputScale();
        List<Canvas> tiles = canvases(content).toList();
        assertFalse(tiles.isEmpty());
        for (Canvas tile : tiles) {
            assertTrue(tile.getWidth() * scale <= 4096, "Oversized canvas width: " + tile.getWidth());
            assertTrue(tile.getHeight() * scale <= 4096, "Oversized canvas height: " + tile.getHeight());
        }
        return tiles;
    }

    private static double outputScale() {
        return Math.ceil(Screen.getScreens().stream()
                .mapToDouble(screen -> Math.max(screen.getOutputScaleX(), screen.getOutputScaleY()))
                .max().orElse(1.0));
    }

    private static Stream<Canvas> canvases(Node node) {
        if (node instanceof Canvas canvas) {
            return Stream.of(canvas);
        }
        if (node instanceof Parent parent) {
            return parent.getChildrenUnmodifiable().stream().flatMap(TextPaneTextureTest::canvases);
        }
        return Stream.empty();
    }
}