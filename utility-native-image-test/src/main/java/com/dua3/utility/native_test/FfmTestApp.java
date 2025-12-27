package com.dua3.utility.native_test;

import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.UiMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Minimal application to exercise FFM-based features for GraalVM configuration generation.
 */
public class FfmTestApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("FFM Test App started.");
        System.out.println("This app will exercise FFM-based dark mode detection and window decorations.");
        System.out.format("""
                        
                        JDK
                          Java version:               %s
                          Java vendor:                %s
                          Java Home:                  %s
                          has native-image:           %s
                        
                        JVM
                          VM Name:                    %s
                          VM Vendor:                  %s
                        
                        Application
                          running as native code:     %s
                        
                        """,
                System.getProperty("java.version", "unknown"),
                System.getProperty("java.vendor", "unknown"),
                System.getProperty("java.home", "unknown"),
                Files.isExecutable(Paths.get(System.getProperty("java.home"), "bin", "native-image"))
                || Files.isExecutable(Paths.get(System.getProperty("java.home"), "bin", "native-image.exe")),
                System.getProperty("java.vm.name", "unknown"),
                System.getProperty("java.vm.vendor", "unknown"),
                System.getProperty("org.graalvm.nativeimage.imagecode") != null
        );

        // 1. Exercise DarkModeDetector
        ApplicationUtil.setUiMode(UiMode.SYSTEM_DEFAULT);
        System.out.println("Dark Mode Supported: " + ApplicationUtil.isDarkModeDetectionSupported());
        System.out.println("Is Dark Mode: " + ApplicationUtil.isDarkMode());

        CountDownLatch latch = new CountDownLatch(1);
        ApplicationUtil.addDarkModeListener(dark -> {
            System.out.println("[EVENT] System Dark Mode changed to: " + dark);
            latch.countDown();
        });

        // 2. Exercise NativeHelper with a window
        Label label = new Label("Please toggle your system Light/Dark mode now.");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 200);
        primaryStage.setTitle("FFM Test Window");
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("Setting dark decorations (true)...");
        Platform.runLater(() -> ApplicationUtil.setUiMode(UiMode.DARK));

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Setting dark decorations (false)...");
                Platform.runLater(() -> ApplicationUtil.setUiMode(UiMode.LIGHT));

                System.out.println("Waiting up to 30 seconds for you to toggle system dark mode...");
                if (latch.await(30, TimeUnit.SECONDS)) {
                    System.out.println("System dark mode change detected!");
                } else {
                    System.out.println("Timed out waiting for system dark mode change. Proceeding anyway.");
                }

                System.out.println("Exercising remaining FFM paths...");
                // Ensure some more calls are made
                ApplicationUtil.isDarkMode();

                System.out.println("FFM Test App finishing in 5 seconds. You can close the window now.");
                Thread.sleep(5000);
                System.out.println("Done. Please close the application.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
