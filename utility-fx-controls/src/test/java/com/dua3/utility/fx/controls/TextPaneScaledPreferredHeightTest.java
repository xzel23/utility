package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPaneScaledPreferredHeightTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void preferredHeightContainsTheScaledCanvasWhenTextWraps() throws Exception {
        assertPreferredHeightContainsScaledCanvas(true);
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void preferredHeightContainsTheScaledCanvasWhenTextDoesNotWrap() throws Exception {
        assertPreferredHeightContainsScaledCanvas(false);
    }

    private void assertPreferredHeightContainsScaledCanvas(boolean wrapText) throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane pane = new TextEditorPane("final line");
            pane.setWrapText(wrapText);
            pane.setDisplayScale(2.5);
            pane.setMinWidth(400.0);
            pane.setPrefWidth(400.0);
            pane.setMaxWidth(400.0);
            pane.setMaxHeight(Region.USE_PREF_SIZE);
            addToScene(pane);

            pane.applyCss();
            pane.layout();

            ScrollPane scrollPane = pane.lookupAll(".scroll-pane").stream()
                    .filter(ScrollPane.class::isInstance)
                    .map(ScrollPane.class::cast)
                    .findFirst()
                    .orElseThrow();
            Node content = findDescendantWithStyleClass(scrollPane.getContent(), "content");

            assertTrue(
                    scrollPane.getViewportBounds().getHeight() >= content.getBoundsInParent().getHeight(),
                    () -> "viewport=" + scrollPane.getViewportBounds().getHeight()
                            + ", scaled content=" + content.getBoundsInParent().getHeight()
            );
        });
    }

    private static Node findDescendantWithStyleClass(Node node, String styleClass) {
        if (node.getStyleClass().contains(styleClass)) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node match = findDescendantWithStyleClass(child, styleClass);
                if (match != null) {
                    return match;
                }
            }
        }
        throw new IllegalStateException("unable to find node with style class: " + styleClass);
    }
}
