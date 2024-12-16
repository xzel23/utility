package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.samples.graphics.Slide;
import com.dua3.utility.ui.Graphics;

/**
 * The ShapeFx class extends the JavaFX Application class to create and display a window
 * with two different paths drawn on a pane. The paths are created using different methods,
 * demonstrating the use of both JavaFX Path elements and a custom Path2f class with segmented paths.
 */
public class ShapeFx implements Slide {

    @Override
    public String title() {
        return "Paths";
    }

    @Override
    public void draw(Graphics g) {
        g.setStroke(Color.BLUE, 5);
        g.strokePath(createPath());

        g.setStroke(Color.LIGHTGRAY, 3);
        g.strokePath(createPath());

    }

    private static Path2f createPath() {
        return Path2f.builder()
                .moveTo(Vector2f.of(0.0f, 0.0f))
                .lineTo(Vector2f.of(70.0f, 0.0f))
                .curveTo(Vector2f.of(100.0f, 0.0f), Vector2f.of(120.0f, 60.0f))
                .lineTo(Vector2f.of(175.0f, 55.0f))
                .arcTo(Vector2f.of(50.0f, 50.0f), Vector2f.of(50.0f, 50.0f), 0.0f, false, false)
                .build();
    }
}