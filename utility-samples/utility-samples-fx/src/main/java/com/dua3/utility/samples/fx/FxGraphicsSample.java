package com.dua3.utility.samples.fx;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.icons.IconUtil;
import com.dua3.utility.ui.Graphics;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
        System.out.println("available icon providers: " + IconUtil.iconProviderNames());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        float w = 500;
        float h = 500;
        float margin = 10;

        Canvas canvas = new Canvas(w, h);
        FxGraphics g = new FxGraphics(canvas);

        float x = margin;
        float y = margin;

        g.setFont(g.getDefaultFont().withBold(true));
        g.drawText("drawText(CharSequence, float, float, HAnchor, VAnchor):", x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);
        y+= 40;

        g.setStroke(Color.RED, 1);
        g.strokeLine(x, y, (float) canvas.getWidth() - margin, y);
        for (Graphics.VAnchor v : Graphics.VAnchor.values()) {
            g.drawText(v.name(), x, y, Graphics.HAnchor.LEFT, v);
            x += (w - 2 * margin) / Graphics.VAnchor.values().length;
        }

        StackPane root = new StackPane(canvas);

        Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight());

        primaryStage.setTitle("FxGraphics");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

