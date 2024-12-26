package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.PinBoard;
import javafx.application.Application;
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
import javafx.stage.Stage;

import java.util.Formatter;

public class PinBoardSample extends Application {

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

        PinBoard pinBoard = new PinBoard();
        for (int i = 0; i < 50; i++) {
            createItem(pinBoard, i);
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1000, 800);
        root.setCenter(pinBoard);
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefWidth(400);
        root.setLeft(textArea);

        pinBoard.addEventFilter(MouseEvent.MOUSE_MOVED,evt -> {
            Formatter text = new Formatter();

            int x = (int) evt.getX();
            int y = (int) evt.getY();
            text.format("Mouse position: (%d,%d)", x, y);

            pinBoard.getItemAt(evt.getX(), evt.getY()).ifPresentOrElse(item -> {
                        text.format("\n\ngetItemAt(%d, %d):\n%s", x, y, item.name());
                        text.format("\n\narea:\n%s", item.area());
                    },
                    () -> text.format("\n\ngetItemAt(%d, %d):\n-", x, y)
            );

            pinBoard.getPositionInItem(evt.getX(), evt.getY()).ifPresentOrElse(pii -> {
                        text.format("\n\ngetPositionInItem(%d, %d):\n%s", x, y, pii.item().name());
                        text.format("\nposition in item: (%f, %f)", pii.x(), pii.y());
                    },
                    () -> text.format("\n\ngetPositionInItem(%d, %d):\n-", x, y)
            );

            textArea.setText(text.toString());
        });

        stage.setScene(scene);
        stage.setTitle("Shape");
        stage.show();
    }

    private void createItem(PinBoard pinBoard, int i) {
        int minWidth = 200;
        int maxWidth = 600;
        int width = minWidth + (int) ((maxWidth - minWidth) * Math.random());
        int minHeight = 200;
        int maxHeight = 600;
        int height = minHeight + (int) ((maxHeight - minHeight) * Math.random());

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