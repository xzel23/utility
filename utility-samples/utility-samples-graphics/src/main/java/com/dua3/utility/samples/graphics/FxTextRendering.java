package com.dua3.utility.samples.graphics;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * FxTextRendering is a JavaFX application that demonstrates rendering text with
 * various styles, alignments, rotations, and effects. The application visualizes
 * text transformations like rotation and alignment in a grid layout using a variety
 * of customization options for rendering graphics.
 * <p>
 * The application showcases different combinations of horizontal and vertical
 * anchors, and utilizes text rotation modes for visual effects.
 * <p>
 * It uses rich text styles, including bold, italic, underline, and line-through,
 * and applies them to displayed text. The text rendering is demonstrated with
 * rotating graphics and dynamically updated angles using animation.
 */
public class FxTextRendering extends Application {

    /**
     * A predefined {@link RichText} constant that demonstrates the usage of rotated text with various
     * styles, including bold, italic, underline, and line-through. This text is constructed using
     * a {@link RichTextBuilder} and utilizes different formatting styles for illustrative purposes.
     */
    public static final RichText TEXT = new RichTextBuilder()
            .append("rotated text\n")
            .append("using different modes and angles\n")
            .push(Style.BOLD)
            .append("bold ")
            .pop(Style.BOLD)
            .push(Style.ITALIC)
            .append("italic\n")
            .pop(Style.ITALIC)
            .push(Style.UNDERLINE)
            .append("underline ")
            .pop(Style.UNDERLINE)
            .push(Style.LINE_THROUGH)
            .append("line through")
            .pop(Style.LINE_THROUGH)
            .toRichText();

    private double rotationAngle = 0;

    /**
     * The main entry point of the FxGraphicsSample application. This method initializes
     * the JavaFX runtime and launches the graphical application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public FxTextRendering() { /* nothing to do */ }

    @Override
    public void start(Stage primaryStage) {
        float w = 500.0f;
        float h = 500.0f;

        GridPane grid = new GridPane();

        float tileWidth = w;
        float tileHeight = h;

        List<Tile> tiles = new ArrayList<>();
        for (var vAnchor : Graphics.VAnchor.values()) {
            int i = vAnchor.ordinal();
            for (var hAnchor : Graphics.HAnchor.values()) {
                int j = hAnchor.ordinal();

                Consumer<Graphics> renderer = g -> render(g, hAnchor, vAnchor, rotationAngle);
                Tile tile = createTile(hAnchor, vAnchor, renderer);
                tile.setMinSize(tileWidth, tileHeight);
                grid.add(tile, j, i);
                tiles.add(tile);
            }
        }

        AnimationTimer timer = new AnimationTimer() {

            public static final int RPM = 4;
            private static final double F_RPM = 1.0E-9 / 60;

            @Override
            public void handle(long now) {
                for (Tile tile : tiles) {
                    tile.redraw();
                }
                rotationAngle = 360 * ((RPM * now * F_RPM) % 1.0);
            }
        };
        timer.start();

        primaryStage.setScene(new Scene(new ScrollPane(grid)));
        primaryStage.setTitle("Rotating Text");
        primaryStage.show();
    }

    private void render(Graphics g, Graphics.HAnchor hAnchor, Graphics.VAnchor vAnchor, double rotationAngle) {
        g.reset();

        float width = g.getWidth();
        float height = g.getHeight();

        float centerX = width / 2;
        float centerY = height / 2;
        float size = width / 3;

        Vector2f pos = Vector2f.of(centerX, centerY);
        Dimension2f dim = Dimension2f.of(size, size);

        // draw rectangle
        g.setStroke(Color.BLUE, 1);
        g.strokeRect(pos, dim);

        // draw pivot
        g.setFill(Color.RED);
        g.fillCircle(centerX, centerY, 3);

        double phi = Math.toRadians(rotationAngle);
        Graphics.TextWrapping wrap = Graphics.TextWrapping.WRAP;

        // ROTATE_BLOCK
        Color color = Color.GREEN;

        AffineTransformation2f at = AffineTransformation2f.combine(g.getTransformation(), AffineTransformation2f.rotate(phi, pos));
        at = AffineTransformation2f.combine(AffineTransformation2f.translate(pos), at);
        g.setStroke(color, 1.0f);
        g.strokePolygon(
                at.transform(Vector2f.ORIGIN),
                at.transform(Vector2f.ORIGIN.translate(dim.width(), 0)),
                at.transform(Vector2f.ORIGIN.translate(dim.width(), dim.height())),
                at.transform(Vector2f.ORIGIN.translate(0, dim.height()))
        );

        g.setFont(g.getFont().withColor(color));
        g.renderText(pos, TEXT, hAnchor, vAnchor, Alignment.LEFT, VerticalAlignment.TOP, dim, wrap,
                phi, Graphics.TextRotationMode.ROTATE_OUTPUT_AREA
        );

        // ROTATE_AND_TRANSLATE_BLOCK
        color = Color.BLUE;
        at = AffineTransformation2f.combine(g.getTransformation(), AffineTransformation2f.translate(pos));
        g.setStroke(color, 1.0f);
        g.strokePolygon(
                at.transform(Vector2f.ORIGIN),
                at.transform(Vector2f.ORIGIN.translate(dim.width(), 0)),
                at.transform(Vector2f.ORIGIN.translate(dim.width(), dim.height())),
                at.transform(Vector2f.ORIGIN.translate(0, dim.height()))
        );

        g.setFont(g.getFont().withColor(color));
        g.renderText(pos, TEXT, hAnchor, vAnchor, Alignment.LEFT, VerticalAlignment.TOP, dim, wrap,
                phi, Graphics.TextRotationMode.ROTATE_AND_TRANSLATE
        );

        // ROTATE_LINES
        color = Color.BLACK;
        g.setFont(g.getFont().withColor(color));
        g.renderText(pos, TEXT, hAnchor, vAnchor, Alignment.LEFT, VerticalAlignment.TOP, dim, wrap,
                phi, Graphics.TextRotationMode.ROTATE_AND_TRANSLATE_LINES
        );

        g.close();
    }

    private Tile createTile(Graphics.HAnchor hAnchor, Graphics.VAnchor vAnchor, Consumer<Graphics> renderer) {
        return new Tile("HAnchor: %s, VAnchor: %s".formatted(hAnchor, vAnchor), renderer);
    }

    /**
     * The Tile class is a custom UI component extending BorderPane and is designed to encapsulate
     * a rendered canvas with a configurable header. It provides functionality to draw custom
     * graphics using a user-supplied renderer.
     * <p>
     * The header of the tile displays a title in bold white text with a dark blue background.
     * The body of the tile contains a resizable canvas that binds its dimensions to the dimensions of the Tile.
     */
    public static class Tile extends BorderPane {
        private static final Font font = FxFontUtil.getInstance().getDefaultFont();
        private final Canvas canvas;
        private final FxGraphics graphics;
        private final Consumer<Graphics> renderer;

        Tile(String title, Consumer<Graphics> renderer) {
            this.canvas = new Canvas(50, 50);
            this.graphics = new FxGraphics(canvas);
            this.renderer = renderer;

            canvas.widthProperty().bind(widthProperty());
            canvas.heightProperty().bind(heightProperty());

            StackPane header = new StackPane(
                    Controls.text(title)
                            .font(font.withBold(true).withColor(Color.WHITE))
                            .build()
            );
            header.setBackground(Controls.background(Color.DARKBLUE));
            setTop(header);

            setCenter(canvas);

            redraw();
        }

        /**
         * Redraws the canvas by invoking the renderer with the associated graphics context.
         * This method is typically called to update the visual representation of the canvas
         * after changes to the rendering logic or other data affecting the graphical content.
         */
        public void redraw() {
            renderer.accept(graphics);
        }
    }
}
