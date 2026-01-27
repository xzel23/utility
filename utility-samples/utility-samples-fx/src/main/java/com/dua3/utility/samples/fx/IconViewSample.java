package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.icons.IconUtil;
import com.dua3.utility.fx.icons.IconView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The IconViewSample class is a JavaFX application that displays an icon from the icon provider.
 * The main method prints the available icon providers to the standard output and launches the JavaFX application.
 * The start method sets up the primary stage with an IconView displaying an icon with specified parameters.
 */
public class IconViewSample extends Application {

    private static final Logger LOG = LogManager.getLogger(IconViewSample.class.getName());

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        LOG.info("available icon providers: {}", IconUtil.iconProviderNames());
        launch(args);
    }

    /**
     * Constructor.
     */
    public IconViewSample() { /* nothing to do */ }

    @Override
    public void start(Stage primaryStage) {
        IconView iv = new IconView("fas-exclamation-triangle", 80, Paint.valueOf("DARKBLUE"));

        StackPane root = new StackPane(iv);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("IconView");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

