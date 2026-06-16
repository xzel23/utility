package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextEditorPaneEmptyLineCaretTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testCaretCanResolveToEmptyLine() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("A\n\nB");
            editor.setWrapText(false);
            addToScene(editor);

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(400.0);
            int emptyLineIndex = findEmptyLine(lines);
            assertTrue(emptyLineIndex >= 0, "expected an empty visual line");

            TextEditorPane.VisualLine emptyLine = lines.get(emptyLineIndex);
            editor.positionCaret(emptyLine.start());

            int resolvedLine = TextEditorPane.lineIndexForCaret(lines, editor.getCaretPosition());
            assertEquals(emptyLineIndex, resolvedLine);
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testHitTestingCanTargetEmptyLine() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("A\n\nB");
            editor.setWrapText(false);
            addToScene(editor);

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(400.0);
            int emptyLineIndex = findEmptyLine(lines);
            assertTrue(emptyLineIndex >= 0, "expected an empty visual line");

            TextEditorPane.VisualLine emptyLine = lines.get(emptyLineIndex);
            double y = emptyLine.top() + emptyLine.height() * 0.5;
            int caret = TextEditorPane.indexForPoint(lines, 0.0, y);
            assertEquals(emptyLine.start(), caret);
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testTrailingEmptyLinesAfterInsertAtEnd() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("abc");
            editor.setWrapText(false);
            addToScene(editor);

            editor.positionCaret(editor.getLength());
            editor.processKeyTyped(keyTyped("\r"));
            editor.processKeyTyped(keyTyped("\r"));

            assertEquals("abc\n\n", editor.getText().toString());

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(400.0);
            int caret = editor.getCaretPosition();
            int lineIndex = TextEditorPane.lineIndexForCaret(lines, caret);
            assertEquals(lines.size() - 1, lineIndex);
            assertEquals(caret, lines.get(lineIndex).start());
            assertEquals(caret, lines.get(lineIndex).end());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testTrailingEmptyLinesAfterInsertTextAtDocumentEnd() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane("a\nb");
            editor.setWrapText(false);
            addToScene(editor);

            editor.insertText(editor.getLength(), "\n\n");
            assertEquals("a\nb\n\n", editor.getText().toString());

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(400.0);
            assertTrue(lines.size() >= 4, "expected four logical lines after trailing insertion");

            int textEnd = editor.getLength();
            int lastLineIndex = TextEditorPane.lineIndexForCaret(lines, textEnd);
            assertEquals(lines.size() - 1, lastLineIndex);

            TextEditorPane.VisualLine lastLine = lines.getLast();
            assertEquals(textEnd, lastLine.start());
            assertEquals(textEnd, lastLine.end());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testClickingLineAfterInlineNodeTargetsCorrectLine() throws Exception {
        runOnFxThreadAndWait(() -> {
            RichText text = createInlineSampleText();

            TextEditorPane editor = new TextEditorPane(text);
            editor.setWrapText(false);
            addToScene(editor);

            double width = 500.0;
            List<TextEditorPane.VisualLine> visualLines = editor.buildVisualLines(width);
            TextPane.Layout layout = editor.createLayout(width);

            int targetPos = text.toString().indexOf("after-inline");
            assertTrue(targetPos >= 0);

            LayoutLine targetLayoutLine = findLayoutLineForSourcePosition(layout, targetPos);
            assertTrue(targetLayoutLine != null, "target layout line not found");

            double y = targetLayoutLine.top() + targetLayoutLine.height() * 0.5;
            int hitCaret = TextEditorPane.indexForPoint(visualLines, 0.0, y);
            assertEquals(targetPos, hitCaret, "hit test resolved to wrong source position");
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testClickingLineAfterInlineControlTargetsCorrectLine() throws Exception {
        runOnFxThreadAndWait(() -> {
            RichText text = createInlineControlSampleText();

            TextEditorPane editor = new TextEditorPane(text);
            editor.setWrapText(false);
            addToScene(editor);

            double width = 600.0;
            List<TextEditorPane.VisualLine> visualLines = editor.buildVisualLines(width);
            TextPane.Layout layout = editor.createLayout(width);

            int targetPos = text.toString().indexOf("line-after-control");
            assertTrue(targetPos >= 0);

            LayoutLine targetLayoutLine = findLayoutLineForSourcePosition(layout, targetPos);
            assertTrue(targetLayoutLine != null, "target layout line not found");

            double y = targetLayoutLine.top() + targetLayoutLine.height() * 0.5;
            int hitCaret = TextEditorPane.indexForPoint(visualLines, 0.0, y);
            assertEquals(targetPos, hitCaret, "hit test resolved to wrong source position");
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testPageDownMovesCaretByViewportHeight() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane(numberedLines(80));
            editor.setWrapText(false);
            addToScene(editor);

            int start = lineStart(editor.getText().toString(), 10);
            editor.positionCaret(start);

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(600.0);
            int currentLine = TextEditorPane.lineIndexForCaret(lines, editor.getCaretPosition());
            assertTrue(currentLine >= 0);

            TextEditorPane.VisualLine line = lines.get(currentLine);
            double x = TextEditorPane.xForIndex(line, editor.getCaretPosition());
            double viewportHeight = viewportHeight(editor);
            assertTrue(Double.isFinite(viewportHeight) && viewportHeight > 1.0, "expected a visible editor viewport");

            int expectedCaret = TextEditorPane.indexForPoint(lines, x, line.top() + viewportHeight);
            editor.processKeyPressed(keyPressed(KeyCode.PAGE_DOWN, false));

            assertEquals(expectedCaret, editor.getCaretPosition());
            assertEquals(expectedCaret, editor.getAnchor());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testPageUpMovesCaretByViewportHeight() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane(numberedLines(80));
            editor.setWrapText(false);
            addToScene(editor);

            int start = lineStart(editor.getText().toString(), 30);
            editor.positionCaret(start);

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(600.0);
            int currentLine = TextEditorPane.lineIndexForCaret(lines, editor.getCaretPosition());
            assertTrue(currentLine >= 0);

            TextEditorPane.VisualLine line = lines.get(currentLine);
            double x = TextEditorPane.xForIndex(line, editor.getCaretPosition());
            double viewportHeight = viewportHeight(editor);
            assertTrue(Double.isFinite(viewportHeight) && viewportHeight > 1.0, "expected a visible editor viewport");

            int expectedCaret = TextEditorPane.indexForPoint(lines, x, line.top() - viewportHeight);
            editor.processKeyPressed(keyPressed(KeyCode.PAGE_UP, false));

            assertEquals(expectedCaret, editor.getCaretPosition());
            assertEquals(expectedCaret, editor.getAnchor());
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testShiftPageDownExtendsSelectionFromAnchor() throws Exception {
        runOnFxThreadAndWait(() -> {
            TextEditorPane editor = new TextEditorPane(numberedLines(80));
            editor.setWrapText(false);
            addToScene(editor);

            int anchor = lineStart(editor.getText().toString(), 12);
            editor.positionCaret(anchor);

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(600.0);
            int currentLine = TextEditorPane.lineIndexForCaret(lines, editor.getCaretPosition());
            assertTrue(currentLine >= 0);

            TextEditorPane.VisualLine line = lines.get(currentLine);
            double x = TextEditorPane.xForIndex(line, editor.getCaretPosition());
            double viewportHeight = viewportHeight(editor);
            assertTrue(Double.isFinite(viewportHeight) && viewportHeight > 1.0, "expected a visible editor viewport");

            int expectedCaret = TextEditorPane.indexForPoint(lines, x, line.top() + viewportHeight);
            editor.processKeyPressed(keyPressed(KeyCode.PAGE_DOWN, true));

            assertEquals(anchor, editor.getAnchor());
            assertEquals(expectedCaret, editor.getCaretPosition());
        });
    }

    private static RichText createInlineSampleText() {
        RichTextBuilderFx builder = new RichTextBuilderFx();
        builder.append("first line\n");

        Image image = createPatternImage(48, 24);

        builder.append("line with inline ");
        builder.appendImage(image);
        builder.append("\n\n");
        builder.append("after-inline");
        return builder.toRichText();
    }

    private static RichText createInlineControlSampleText() {
        RichTextBuilderFx builder = new RichTextBuilderFx();
        builder.append("first line\n");
        builder.append("line with inline button ");
        builder.appendButton("Button 1", () -> {});
        builder.append(" and trailing text\n\n");
        builder.append("line-after-control");
        return builder.toRichText();
    }

    private static LayoutLine findLayoutLineForSourcePosition(TextPane.Layout layout, int sourcePosition) {
        List<List<FragmentedText.Fragment>> lines = layout.renderLines();
        TextPane.LayoutTextData mapping = layout.layoutTextData();
        for (List<FragmentedText.Fragment> line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            double top = line.getFirst().y();
            double height = line.stream().mapToDouble(FragmentedText.Fragment::h).max().orElse(1.0);
            for (FragmentedText.Fragment fragment : line) {
                if (!(fragment.text() instanceof Run run)) {
                    continue;
                }
                int sourceStart = mapping.layoutToSourcePosition(run.getStart());
                int sourceEnd = mapping.layoutToSourcePosition(run.getEnd());
                if (sourcePosition >= sourceStart && sourcePosition <= sourceEnd) {
                    return new LayoutLine(top, height);
                }
            }
        }
        return null;
    }

    private static KeyEvent keyTyped(String character) {
        return new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED, false, false, false, false);
    }

    private static KeyEvent keyPressed(KeyCode code, boolean shift) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, false, false, false);
    }

    private static double viewportHeight(TextEditorPane editor) {
        return editor.lookupAll(".scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .mapToDouble(sp -> sp.getViewportBounds().getHeight())
                .filter(h -> Double.isFinite(h) && h > 1.0)
                .findFirst()
                .orElse(Double.NaN);
    }

    private static String numberedLines(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append("line-").append(i);
        }
        return sb.toString();
    }

    private static int lineStart(String text, int lineNumber) {
        int start = 0;
        for (int i = 0; i < lineNumber && start < text.length(); i++) {
            int newline = text.indexOf('\n', start);
            if (newline < 0) {
                return text.length();
            }
            start = newline + 1;
        }
        return start;
    }

    private static Image createPatternImage(int width, int height) {
        int[] colors = new int[]{Color.RED.argb(), Color.GREEN.argb(), Color.BLUE.argb(), Color.YELLOW.argb()};
        int[] data = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * width + x] = colors[(x + y) % colors.length];
            }
        }
        return ImageUtil.getInstance().createImage(width, height, data);
    }

    private record LayoutLine(double top, double height) {}

    private static int findEmptyLine(List<TextEditorPane.VisualLine> lines) {
        for (int i = 0; i < lines.size(); i++) {
            TextEditorPane.VisualLine line = lines.get(i);
            if (line.start() == line.end()) {
                return i;
            }
        }
        return -1;
    }
}
