package com.dua3.utility.samples.graphics;

import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.samples.graphics.slides.ArcToAndEllipse;
import com.dua3.utility.samples.graphics.slides.DrawText;
import com.dua3.utility.samples.graphics.slides.RenderRotatedText;
import com.dua3.utility.samples.graphics.slides.RenderText;
import com.dua3.utility.ui.Graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface IGraphicsSample<TAB> {
    interface Slide {
        String title();

        default void draw(Graphics g) {
            g.setTransformation(
                    AffineTransformation2f.combine(
                            g.getTransformation(),
                            AffineTransformation2f.scale(0.5f),
                            AffineTransformation2f.translate(TILE_WIDTH/4f, TILE_HEIGHT/4f)
                    )
            );
            drawContent(g);
        }

        void drawContent(Graphics g);


    }

    int TILE_WIDTH = 1440;
    int TILE_HEIGHT = TILE_WIDTH * 10 / 16;

    default List<TAB> createSlides(float w, float h) {
        List<TAB> tabs = new ArrayList<>(List.of(
                createSlide(ArcToAndEllipse::new, w, h),
                createSlide(DrawText::new, w, h),
                createSlide(RenderText::new, w, h)
        ));
        double[] angles = {
                0, 45, 90, -45, -90
        };

        for (Graphics.HAnchor hAnchor : Graphics.HAnchor.values()) {
            for (Graphics.VAnchor vAnchor : Graphics.VAnchor.values()) {
                tabs.add(createBigSlide(() -> new RenderRotatedText(hAnchor, vAnchor, angles), w, h));
            }
        }
        return tabs;
    }

    TAB createSlide(Supplier<Slide> factory, float w, float h);

    TAB createBigSlide(Supplier<Slide> factory, float w, float h);
}
