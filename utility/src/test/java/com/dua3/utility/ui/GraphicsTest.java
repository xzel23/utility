package com.dua3.utility.ui;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.VerticalAlignment;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphicsTest {

    @Test
    void testEnumsAndConstants() {
        assertArrayEquals(new float[0], Graphics.EMPTY_DASHES);

        assertEquals(Graphics.TextWrapping.WRAP, Graphics.TextWrapping.valueOf("WRAP"));
        assertEquals(Graphics.TextWrapping.NO_WRAP, Graphics.TextWrapping.valueOf("NO_WRAP"));
        assertEquals(2, Graphics.TextWrapping.values().length);

        assertEquals(Graphics.TextRotationMode.ROTATE_OUTPUT_AREA, Graphics.TextRotationMode.valueOf("ROTATE_OUTPUT_AREA"));
        assertEquals(Graphics.TextRotationMode.ROTATE_AND_TRANSLATE, Graphics.TextRotationMode.valueOf("ROTATE_AND_TRANSLATE"));
        assertEquals(Graphics.TextRotationMode.ROTATE_LINES, Graphics.TextRotationMode.valueOf("ROTATE_LINES"));
        assertEquals(Graphics.TextRotationMode.ROTATE_AND_TRANSLATE_LINES, Graphics.TextRotationMode.valueOf("ROTATE_AND_TRANSLATE_LINES"));
        assertEquals(4, Graphics.TextRotationMode.values().length);
    }

    @Test
    void testApproximateArcValid() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(10, 0);
        Vector2f radii = Vector2f.of(10, 10);

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, moveTos::add, segments::add);

        assertEquals(1, moveTos.size());
        assertFalse(segments.isEmpty());
        for (Vector2f[] seg : segments) {
            assertEquals(3, seg.length);
        }
    }

    @Test
    void testApproximateArcObject() {
        Path2f path = Path2f.builder()
                .moveTo(0, 0)
                .arcTo(Vector2f.of(10, 10), Vector2f.of(10, 10), 0.0f, true, false)
                .build();
        Arc2f arc = (Arc2f) path.segments().get(1);

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(arc, moveTos::add, segments::add);

        assertEquals(1, moveTos.size());
        assertFalse(segments.isEmpty());
    }

    @Test
    void testApproximateArcDegenerateStraightLine() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(5, 0);
        Vector2f radii = Vector2f.of(0, 10); // rx is zero

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, moveTos::add, segments::add);

        assertEquals(0, moveTos.size());
        assertEquals(1, segments.size());
        assertEquals(3, segments.getFirst().length);
    }

    @Test
    void testApproximateArcInvalidDistanceThrows() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(100, 0);
        Vector2f radii = Vector2f.of(0, 10); // rx is zero and distance 100 > 2 * 10

        assertThrows(IllegalArgumentException.class, () ->
                Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, p -> {}, s -> {}));
    }

    @SuppressWarnings("java:S1186")
    static class DummyGraphics implements Graphics {
        AffineTransformation2f transformation = AffineTransformation2f.identity();
        Font font = FontUtil.getInstance().getDefaultFont();
        final List<String> drawnTexts = new ArrayList<>();
        Color strokeColor = Color.BLACK;
        Color fillColor = Color.BLACK;
        float strokeWidth = 1.0f;
        float[] lineDashes = Graphics.EMPTY_DASHES;
        float dashOffset = 0.0f;

        @Override public float getWidth() { return 800; }
        @Override public float getHeight() { return 600; }
        @Override public FontUtil getFontUtil() { return FontUtil.getInstance(); }
        @Override public Font getDefaultFont() { return font; }
        @Override public void reset() {}
        @Override public void drawImage(Image image, float x, float y) {}
        @Override public void strokeRect(float x, float y, float w, float h) {}
        @Override public void fillRect(float x, float y, float w, float h) {}
        @Override public void strokeEllipse(float x, float y, float rx, float ry, float angle) {}
        @Override public void fillEllipse(float x, float y, float rx, float ry, float angle) {}
        @Override public void strokeLine(float x1, float y1, float x2, float y2) {}
        @Override public void strokePath(Path2f path) {}
        @Override public void fillPath(Path2f path) {}
        @Override public void clip(Path2f path) {}
        @Override public void clip(Rectangle2f r) {}
        @Override public void resetClip() {}
        @Override public void setStroke(Color c, float width) { this.strokeColor = c; this.strokeWidth = width; }
        @Override public void setStrokeColor(Color c) { this.strokeColor = c; }
        @Override public void setStrokeWidth(float width) { this.strokeWidth = width; }
        @Override public Color getStrokeColor() { return strokeColor; }
        @Override public float getStrokeWidth() { return strokeWidth; }
        @Override public void setLineDashes(float[] lineDash) { this.lineDashes = lineDash; }
        @Override public float[] getLineDashes() { return lineDashes; }
        @Override public void setLineDashOffset(float lineDashOffset) { this.dashOffset = lineDashOffset; }
        @Override public float getLineDashOffset() { return dashOffset; }
        @Override public void setFill(Color c) { this.fillColor = c; }
        @Override public Color getFill() { return fillColor; }
        @Override public void setTransformation(AffineTransformation2f t) { this.transformation = t; }
        @Override public AffineTransformation2f getTransformation() { return transformation; }
        @Override public void setFont(Font f) { this.font = f; }
        @Override public Font getFont() { return font; }
        @Override public void drawText(CharSequence text, float x, float y) { drawnTexts.add(text.toString() + "@" + x + "," + y); }
        @Override public void close() {}
    }

    @Test
    void testDrawTextAndRenderTextModes() {
        DummyGraphics g = new DummyGraphics();
        assertEquals(new Dimension2f(800, 600), g.getDimension());

        // test inverse transform
        Vector2f pt = Vector2f.of(10, 20);
        assertEquals(pt, g.inverseTransform(pt));
        assertEquals(pt, g.inverseTransform(10, 20));

        // test drawText anchors
        for (HAnchor ha : HAnchor.values()) {
            for (VAnchor va : VAnchor.values()) {
                g.drawText("Test", 100, 100, ha, va);
            }
        }
        assertFalse(g.drawnTexts.isEmpty());

        // test renderText modes
        RichText text = RichText.valueOf("Line 1\nLine 2");
        Vector2f pos = Vector2f.of(50, 50);
        Dimension2f dim = new Dimension2f(200, 100);

        g.renderText(pos, text, HAnchor.LEFT, VAnchor.TOP, Alignment.LEFT, VerticalAlignment.TOP, dim, Graphics.TextWrapping.WRAP);

        for (Graphics.TextRotationMode mode : Graphics.TextRotationMode.values()) {
            g.renderText(pos, text, HAnchor.LEFT, VAnchor.TOP, Alignment.LEFT, VerticalAlignment.TOP, dim, Graphics.TextWrapping.WRAP, Math.PI / 4, mode);
        }
    }
}
