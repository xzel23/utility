package com.dua3.utility.fx.controls;

import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VisualLine;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TextEditorPaneScaledMouseHitTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void hitTestUsesLogicalCoordinatesWhenDisplayIsScaled() throws Exception {
        runOnFxThreadAndWait(() -> {
            String text = "xxxAxxxBxxx " + "scaled hit testing ".repeat(80);
            TextEditorPane editor = new TextEditorPane(text);
            editor.setWrapText(true);
            editor.setDisplayScale(2.0);
            editor.setMinSize(700.0, 500.0);
            editor.setPrefSize(700.0, 500.0);
            editor.setMaxSize(700.0, 500.0);
            addToScene(editor);

            editor.applyCss();
            editor.layout();

            Node content = findScaledContent(editor);
            double logicalViewportWidth = findEditorScrollPane(editor).getViewportBounds().getWidth() / editor.getDisplayScale();
            List<VisualLine> lines = editor.buildVisualLines(logicalViewportWidth);

            VisualLine firstLine = lineForPosition(lines, text.indexOf('A'));
            double x = (RichTextVisualLayoutHelper.xForIndex(firstLine, 3)
                    + RichTextVisualLayoutHelper.xForIndex(firstLine, 4)) / 2.0;
            assertHitTestMatchesLogicalLayout(editor, content, lines, x, firstLine.top() + firstLine.height() / 2.0);

            VisualLine laterLine = lines.get(5);
            assertHitTestMatchesLogicalLayout(editor, content, lines, 1.0, laterLine.top() + laterLine.height() / 2.0);
        });
    }

    private static void assertHitTestMatchesLogicalLayout(
            TextEditorPane editor,
            Node content,
            List<VisualLine> lines,
            double x,
            double y
    ) {
        Point2D scenePoint = content.localToScene(x, y);
        assertNotNull(scenePoint);

        int expected = RichTextVisualLayoutHelper.indexForPoint(lines, x, y);
        assertEquals(expected, hitTest(editor, scenePoint));
    }

    private static VisualLine lineForPosition(List<VisualLine> lines, int position) {
        return lines.stream()
                .filter(line -> position >= line.start() && position <= line.end())
                .findFirst()
                .orElseThrow();
    }

    private static Node findScaledContent(TextEditorPane editor) {
        ScrollPane scrollPane = findEditorScrollPane(editor);
        Node content = findDescendantWithStyleClass(scrollPane.getContent(), "content");
        assertNotNull(content);
        return content;
    }

    private static ScrollPane findEditorScrollPane(TextEditorPane editor) {
        return editor.lookupAll(".scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .filter(scroll -> findDescendantWithStyleClass(scroll.getContent(), "content") != null)
                .findFirst()
                .orElseThrow();
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
        return null;
    }

    private static int hitTest(TextEditorPane editor, Point2D scenePoint) {
        MouseEvent event = new MouseEvent(
                MouseEvent.MOUSE_PRESSED,
                scenePoint.getX(),
                scenePoint.getY(),
                scenePoint.getX(),
                scenePoint.getY(),
                MouseButton.PRIMARY,
                1,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                null
        );
        try {
            Method method = TextEditorPane.class.getDeclaredMethod("hitTest", MouseEvent.class);
            method.setAccessible(true);
            return (int) method.invoke(editor, event);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("cannot invoke text-editor hit test", ex);
        }
    }
}
