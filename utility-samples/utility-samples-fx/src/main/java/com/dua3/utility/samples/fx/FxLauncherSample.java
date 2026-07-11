package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.FxLauncher;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

/**
 * Minimal JavaFX application launched through {@link FxLauncher}.
 */
public final class FxLauncherSample {

    private static final String APP_NAME = "FxLauncher Sample";
    private static final String APP_VERSION = "1.0.0";
    private static final String COPYRIGHT = "(c)2026";
    private static final String DEVELOPER_MAIL = "apps@dua3.com";
    private static final String APP_DESCRIPTION = "Minimal JavaFX application launched with FxLauncher.";
    private static final String APPLICATION_CLASS_NAME = FxLauncherSampleApplication.class.getName();

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        FxLauncher.launchApplication(
                APPLICATION_CLASS_NAME,
                args,
                APP_NAME,
                APP_VERSION,
                COPYRIGHT,
                DEVELOPER_MAIL,
                APP_DESCRIPTION,
                apb -> apb.positionalArgs(0, Integer.MAX_VALUE, "message")
        );
    }

    /**
     * Private constructor.
     */
    private FxLauncherSample() { /* nothing to do */ }

    /**
     * The sample application class extending {@link Application}.
     */
    public static class FxLauncherSampleApplication extends Application {

        /**
         * Constructor.
         */
        public FxLauncherSampleApplication() { /* nothing to do */ }

        @Override
        public void start(Stage primaryStage) {
            List<String> args = getParameters().getRaw();
            String message = args.isEmpty() ? "Hello from FxLauncher." : String.join(" ", args);

            Label label = new Label(message);

            StackPane root = new StackPane(label);
            root.setPadding(new Insets(24));

            Scene scene = new Scene(root, 420, 140);
            primaryStage.setTitle(FxLauncherSample.APP_NAME);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}