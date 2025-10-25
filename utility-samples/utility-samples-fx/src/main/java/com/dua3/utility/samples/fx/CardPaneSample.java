package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.CardPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Minimal sample showcasing the CardPane control.
 *
 * The sample creates three simple cards and a few buttons to switch between them.
 */
public class CardPaneSample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        // Create the CardPane and three cards with different preferred sizes
        CardPane cardPane = new CardPane();
        cardPane.setStyle("-fx-background-color: -fx-background; -fx-border-color: -fx-accent; -fx-border-width: 1;");
        cardPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane redCard = coloredCard("Red Card 300x180", Color.rgb(220, 80, 80), 300, 180);
        StackPane greenCard = coloredCard("Green Card 220x260", Color.rgb(80, 180, 120), 220, 260);
        StackPane blueCard = coloredCard("Blue Card 420x200", Color.rgb(80, 120, 220), 420, 200);

        cardPane.getStyleClass().add("card-pane-sample");
        cardPane.addCard("red", redCard);
        cardPane.addCard("green", greenCard);
        cardPane.addCard("blue", blueCard);

        // Show initial card
        cardPane.show("red");

        // Simple controls to switch between cards
        Button showRed = new Button("Red");
        showRed.setOnAction(e -> cardPane.show("red"));
        Button showGreen = new Button("Green");
        showGreen.setOnAction(e -> cardPane.show("green"));
        Button showBlue = new Button("Blue");
        showBlue.setOnAction(e -> cardPane.show("blue"));

        HBox buttons = new HBox(8, showRed, showGreen, showBlue);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(8));

        ToolBar toolBar = new ToolBar(showRed, showGreen, showBlue);

        // Wrap CardPane so it won't be stretched to fill all available space
        StackPane center = new StackPane(cardPane);
        StackPane.setAlignment(cardPane, Pos.TOP_LEFT);
        center.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(center);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 640, 480);
        stage.setTitle("CardPane Sample – Different Card Sizes");
        stage.setScene(scene);
        stage.show();
    }

    private static StackPane coloredCard(String title, Color color, double prefW, double prefH) {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(20));
        pane.setPrefSize(prefW, prefH);
        pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.setStyle("-fx-background-color: " + toRgb(color) + "; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(0,0,0,0.2); " +
                "-fx-border-radius: 8; " +
                "-fx-border-width: 1;");

        Text text = new Text(title);
        text.setFill(Color.WHITE);
        text.setFont(Font.font(22));

        VBox content = new VBox(10, text);
        content.setAlignment(Pos.CENTER);

        pane.getChildren().add(content);
        return pane;
    }

    private static String toRgb(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }
}
