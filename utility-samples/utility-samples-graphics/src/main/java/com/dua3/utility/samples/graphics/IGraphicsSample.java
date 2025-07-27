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

/**
 * The IGraphicsSample interface defines a framework for creating graphical slides
 * for rendering purposes. It provides a structure for generating and rendering
 * content for graphical user interfaces or other rendering contexts.
 *
 * @param <T> The type of container used to hold the rendered graphical content,
 *            such as a JComponent in Swing or a Tab in JavaFX.
 */
@FunctionalInterface
public interface IGraphicsSample<T> {
    /**
     * The Slide interface represents a component that can render graphical content
     * on a slide. Implementations of this interface define specific slide content
     * and rendering behavior. Slides are typically used to represent individual
     * scenes or visual elements in a graphical framework or user interface.
     */
    interface Slide {
        /**
         * Retrieves the title of the slide.
         *
         * @return the title of the slide as a string
         */
        String title();

        /**
         * Renders the graphical content of the slide with a scaled and translated
         * transformation applied to the graphics context. This method sets up the
         * graphics transformation and calls {@link #drawContent(Graphics)} to render
         * the specific content of the slide.
         *
         * @param g the graphics context used for rendering the slide
         */
        default void draw(Graphics g) {
            g.transform(
                    AffineTransformation2f.scale(0.9f),
                    AffineTransformation2f.translate(TILE_WIDTH / 20.0f, TILE_HEIGHT / 20.0f)
            );
            drawContent(g);
        }

        /**
         * Renders the specific graphical content of the slide. This method is intended
         * to be implemented by classes that define the visual representation of a slide.
         * It is called after the graphical transformation is set up in the {@link #draw(Graphics)}
         * method.
         *
         * @param g the graphics context used for rendering the content of the slide
         */
        void drawContent(Graphics g);
    }

    /**
     * Represents the width of a tile in pixels.
     */
    int TILE_WIDTH = 1440;
    /**
     * Represents the height of a tile in pixels, derived as a scaled value of the tile width.
     */
    int TILE_HEIGHT = TILE_WIDTH * 10 / 16;

    /**
     * Creates a list of slides, each represented by a specific graphical implementation.
     * The slides are generated using predefined slide factories and additional rotated text rendering configurations.
     *
     * @param w The width of the slides to be created.
     * @param h The height of the slides to be created.
     * @return A list of tabs, where each tab holds the content of a generated slide.
     */
    default List<T> createSlides(float w, float h) {
        List<T> tabs = new ArrayList<>(List.of(
                createSlide(ArcToAndEllipse::new, w, h),
                createSlide(DrawText::new, w, h),
                createSlide(RenderText::new, w, h)
        ));
        double[] angles = {
                0, 45, 90, -45, -90
        };

        for (Graphics.HAnchor hAnchor : Graphics.HAnchor.values()) {
            for (Graphics.VAnchor vAnchor : Graphics.VAnchor.values()) {
                tabs.add(createSlide(() -> new RenderRotatedText(hAnchor, vAnchor, angles), w, h));
            }
        }
        return tabs;
    }

    /**
     * Creates a new tab containing a slide with graphical content.
     * The slide is generated using the provided factory, with the specified width and height
     * dimensions applied to configure the graphical rendering context.
     *
     * @param factory A supplier that provides an implementation of the {@link Slide} interface
     *                to define specific content and rendering behavior for the created slide.
     * @param w       The width of the slide to be created, measured in pixels.
     * @param h       The height of the slide to be created, measured in pixels.
     * @return The created tab containing the slide, which holds the rendered graphical content
     *         provided by the factory.
     */
    T createSlide(Supplier<Slide> factory, float w, float h);
}
