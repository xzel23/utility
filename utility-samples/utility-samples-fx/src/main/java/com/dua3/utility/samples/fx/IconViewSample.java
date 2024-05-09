package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.icons.IconUtil;
import com.dua3.utility.fx.icons.IconView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

public class IconViewSample extends Application {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        System.out.println("available icon providers: " + IconUtil.iconProviderNames());
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        IconView iv = new IconView("fa-exclamation-triangle", 80, Paint.valueOf("DARKBLUE"));

        StackPane root = new StackPane(iv);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("IconView");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

