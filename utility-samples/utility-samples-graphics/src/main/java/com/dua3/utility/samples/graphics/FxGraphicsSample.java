package com.dua3.utility.samples.graphics;

import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.samples.graphics.slides.DrawText;
import com.dua3.utility.samples.graphics.slides.RenderText;
import com.dua3.utility.samples.graphics.slides.ShapeFx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

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
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        float w = 1400;
        float h = 800;
        TabPane tabPane = new TabPane(
            createSlides(w, h)
        );

        Scene scene = new Scene(tabPane);
        primaryStage.setTitle("FxGraphics");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    Tab[] createSlides(float w, float h) {
        return new Tab[]{
                createSlide(DrawText::new, w, h),
                createSlide(RenderText::new, w, h),
                createSlide(ShapeFx::new, w, h)
        };
    }

    Tab createSlide(Supplier<Slide> factory, float w, float h) {
        Slide slide = factory.get();
        Canvas canvas = new Canvas(w, h);
        FxGraphics g = new FxGraphics(canvas);
        slide.draw(g);
        return new Tab(slide.title(), canvas);

    }
}

