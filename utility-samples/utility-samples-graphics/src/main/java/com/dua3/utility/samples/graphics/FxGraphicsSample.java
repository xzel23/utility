package com.dua3.utility.samples.graphics;

import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.samples.graphics.slides.ArcToAndEllipse;
import com.dua3.utility.samples.graphics.slides.DrawText;
import com.dua3.utility.samples.graphics.slides.RenderRotatedText;
import com.dua3.utility.samples.graphics.slides.RenderText;
import com.dua3.utility.ui.Graphics;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The IconViewSample class is a JavaFX application that displays an icon from the icon provider.
 * The main method prints the available icon providers to the standard output and launches the JavaFX application.
 * The start method sets up the primary stage with an IconView displaying an icon with specified parameters.
 */
public class FxGraphicsSample extends Application {

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        float w = 1600;
        float h = 1000;
        TabPane tabPane = new TabPane(
                createSlides(w, h)
        );

        Scene scene = new Scene(tabPane);
        primaryStage.setTitle("FxGraphics");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    Tab[] createSlides(float w, float h) {
        List<Tab> tabs = new ArrayList<>(List.of(
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
        return tabs.toArray(Tab[]::new);
    }

    Tab createSlide(Supplier<Slide> factory, float w, float h) {
        Slide slide = factory.get();
        Canvas canvas = new Canvas(w, h);
        FxGraphics g = new FxGraphics(canvas);
        slide.draw(g);
        return new Tab(slide.title(), new ScrollPane(canvas));
    }

    Tab createBigSlide(Supplier<Slide> factory, float w, float h) {
        Tab tab = new Tab(factory.get().title());
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected() && tab.getContent() == null) {
                Slide slide = factory.get();
                Canvas canvas = new Canvas(w, 2 * h);
                FxGraphics g = new FxGraphics(canvas);
                slide.draw(g);
                tab.setContent(new ScrollPane(canvas));
            } else if (!tab.isSelected() && tab.getContent() != null) {
                tab.setContent(null);
            }
        });
        return tab;
    }

    public interface Slide {
        String title();

        void draw(Graphics g);
    }
}

