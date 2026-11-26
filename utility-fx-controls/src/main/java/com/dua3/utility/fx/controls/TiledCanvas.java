package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.ui.Graphics;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

final class TiledCanvas extends Region {
    static final int MAX_TEXTURE_SIZE = 4096;

    private Map<Rectangle2D, Canvas> tiles = Map.of();
    private @Nullable BiConsumer<Graphics, Rectangle2D> painter;
    private boolean dirty;

    TiledCanvas() {
        setManaged(false);
        setMouseTransparent(true);
    }

    void setPainter(BiConsumer<Graphics, Rectangle2D> painter) {
        this.painter = painter;
        dirty = true;
    }

    void paint(Rectangle2D viewport) {
        var draw = painter;
        if (draw == null) {
            return;
        }

        double edge = maxLogicalTextureSize();
        double right = Math.min(getWidth(), viewport.getMaxX());
        double bottom = Math.min(getHeight(), viewport.getMaxY());
        double left = Math.max(0.0, viewport.getMinX());
        double top = Math.max(0.0, viewport.getMinY());
        Map<Rectangle2D, Canvas> visible = new HashMap<>();
        for (double y = tileStart(top, edge); top < bottom && y < bottom; y += edge) {
            for (double x = tileStart(left, edge); left < right && x < right; x += edge) {
                Rectangle2D bounds = new Rectangle2D(x, y, Math.min(edge, getWidth() - x), Math.min(edge, getHeight() - y));
                Canvas tile = tiles.get(bounds);
                if (tile == null) {
                    tile = new Canvas(bounds.getWidth(), bounds.getHeight());
                    tile.relocate(x, y);
                    paintTile(tile, bounds, draw);
                } else if (dirty) {
                    paintTile(tile, bounds, draw);
                }
                visible.put(bounds, tile);
            }
        }
        if (!visible.equals(tiles)) {
            getChildren().setAll(visible.values());
            tiles = visible;
        }
        dirty = false;
    }

    static double maxLogicalTextureSize() {
        // Prism backs nodes at the highest output scale of any screen, rounded up.
        double scale = Math.ceil(Screen.getScreens().stream()
                .mapToDouble(screen -> Math.max(screen.getOutputScaleX(), screen.getOutputScaleY()))
                .max().orElse(1.0));
        return Math.max(1.0, Math.floor(MAX_TEXTURE_SIZE / scale));
    }

    private static double tileStart(double coordinate, double edge) {
        return Math.floor(coordinate / edge) * edge;
    }

    private static void paintTile(Canvas tile, Rectangle2D bounds, BiConsumer<Graphics, Rectangle2D> draw) {
        GraphicsContext context = tile.getGraphicsContext2D();
        context.clearRect(0.0, 0.0, tile.getWidth(), tile.getHeight());
        context.save();
        context.translate(-bounds.getMinX(), -bounds.getMinY());
        try (Graphics graphics = new FxGraphics(tile)) {
            draw.accept(graphics, bounds);
        } finally {
            // FxGraphics saves its initial state, but does not restore it on close.
            context.restore();
            context.restore();
        }
    }
}