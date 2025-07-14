package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.Controls;
import com.dua3.utility.fx.controls.Dialogs;
import com.dua3.utility.fx.controls.InputPane;
import com.dua3.utility.fx.controls.PinBoard;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Formatter;

/**
 * A sample JavaFX application demonstrating the use of a PinBoard component.
 * The application showcases various features such as pinning items to the board,
 * scrolling to specific positions, and handling mouse events to display contextual
 * information about the board's content.
 */
public class PinBoardSample extends Application {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final PinBoard pinBoard = new PinBoard();
    private @Nullable InputPane input;

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        pinBoard.clear();

        for (int i = 0; i < 500; i++) {
            createItem(pinBoard, i);
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 800);
        root.setCenter(pinBoard);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(400);

        BooleanProperty inputValid = new SimpleBooleanProperty(false);
        input = Dialogs.inputPane()
                .header("Input target coordinates.")
                .integer("item", "item", () -> 0L)
                .decimal("x", "x", () -> 0.0)
                .decimal("y", "y", () -> 0.0)
                .decimal("xrelvp", "x relative in VP", () -> 0.0)
                .decimal("yrelvp", "y relative in VP", () -> 0.0)
                .node("buttons", new HBox(
                        Controls.button().text("scrollToPositionInItem(item, x, y)").action(this::scrollToPositionInItem).build(),
                        Controls.button().text("scrollTo(x, y)").action(this::scrollTo).build(),
                        Controls.button().text("scrollTo(x, y, xRelVP, yRelVP)").action(this::scrollToRelVP).build(),
                        Controls.button().text("scrollIntoView()").action(this::scrollIntoView).build()
                ))
                .add("Scale", Double.class, () -> 1.0, Controls.slider().min(0.25).max(2.0).setDefault(pinBoard::getDisplayScale).onChange(pinBoard::setDisplayScale).build())
                .build();
        inputValid.bind(input.validProperty());
        root.setLeft(new VBox(textArea, input));

        pinBoard.addEventFilter(MouseEvent.MOUSE_MOVED, evt -> {
            try (Formatter text = new Formatter()) {
                int x = (int) evt.getX();
                int y = (int) evt.getY();
                text.format("Mouse position: (%d,%d)", x, y);

                text.format("%nvisible items: %s%n", pinBoard.getVisibleItems().size());

                pinBoard.getItemAt(evt.getX(), evt.getY()).ifPresentOrElse(item -> {
                            text.format("%n%ngetItemAt(%d, %d):%n%s", x, y, item.name());
                            text.format("%n%narea:%n%s", item.area());
                        },
                        () -> text.format("%n%ngetItemAt(%d, %d):%n-", x, y)
                );

                pinBoard.getPositionInItem(evt.getX(), evt.getY()).ifPresentOrElse(pii -> {
                            text.format("%n%ngetPositionInItem(%d, %d):%n%s", x, y, pii.item().name());
                            text.format("%nposition in item: (%f, %f)", pii.x(), pii.y());
                        },
                        () -> text.format("%n%ngetPositionInItem(%d, %d):%n-", x, y)
                );

                textArea.setText(text.toString());
            }
        });

        stage.setScene(scene);
        stage.setTitle("Shape");
        stage.show();
    }

    private void scrollToPositionInItem() {
        PinBoard.PositionInItem pos = new PinBoard.PositionInItem(
                pinBoard.getItems().get(getIntegerInput("item")),
                getDoubleInput("x"),
                getDoubleInput("y")
        );
        pinBoard.scrollTo(pos, 0, 0);
    }

    private void scrollTo() {
        pinBoard.scrollTo(getDoubleInput("x"), getDoubleInput("y"), 0, 0);
    }

    private void scrollToRelVP() {
        pinBoard.scrollTo(
                getDoubleInput("x"),
                getDoubleInput("y"),
                getDoubleInput("xrelvp"),
                getDoubleInput("yrelvp")
        );
    }

    private void scrollIntoView() {
        pinBoard.scrollIntoView(getDoubleInput("x"), getDoubleInput("y"));
    }

    double getDoubleInput(String name) {
        var v = input.get().get(name);
        return Double.parseDouble(toNonEmptyString(v, "0.0"));
    }

    int getIntegerInput(String name) {
        var v = input.get().get(name);
        return Integer.parseInt(toNonEmptyString(v, "0"));
    }

    String toNonEmptyString(Object obj, String ifEmpty) {
        String v = String.valueOf(obj);
        return v.isBlank() ? ifEmpty : v;
    }

    private void createItem(PinBoard pinBoard, int i) {
        int minWidth = 200;
        int maxWidth = 600;
        int width = RANDOM.nextInt(minWidth, maxWidth + 1);
        int minHeight = 200;
        int maxHeight = 600;
        int height = RANDOM.nextInt(minHeight, maxHeight + 1);

        String text = "Item " + i + "\n" +
                "width: " + width + ", height: " + height + "\n";
        Node node = createNode(text, width, height);
        pinBoard.pinBottom(
                text,
                () -> node,
                new Dimension2D(width, height)
        );
    }

    private static Label createNode(String text, double width, double height) {
        Label label = new Label(text);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setBorder(new Border(new BorderStroke(
                javafx.scene.paint.Color.BLACK,
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new javafx.scene.layout.BorderWidths(1)
        )));
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setPrefSize(width, height);
        return label;
    }

}