package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.TextPane;
import com.dua3.utility.text.RichText;
import com.dua3.utility.fx.controls.RichTextBuilderFx;
import com.dua3.utility.text.Style;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Sample application for {@link TextPane}.
 */
public class TextPaneSample extends Application {

    /**
     * Entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Constructor.
     */
    public TextPaneSample() { /* nothing to do */ }

    @Override
    public void start(Stage stage) {
        Label status = new Label("No action yet.");
        RichText text = createSampleText(status);

        TextPane textPane = new TextPane(text);
        textPane.setWrapText(true);
        textPane.setMaxHeight(220);
        textPane.setPrefWidth(500);

        CheckBox wrap = new CheckBox("Wrap text");
        wrap.setSelected(true);
        textPane.wrapTextProperty().bind(wrap.selectedProperty());

        Slider width = new Slider(220, 700, 500);
        width.setShowTickLabels(true);
        width.setShowTickMarks(true);
        textPane.prefWidthProperty().bind(width.valueProperty());

        HBox controls = new HBox(12, wrap, new Label("Width:"), width);
        controls.setPadding(new Insets(0, 0, 8, 0));
        HBox.setHgrow(width, Priority.ALWAYS);

        VBox content = new VBox(8, controls, textPane, status);
        content.setPadding(new Insets(12));

        BorderPane root = new BorderPane(content);
        Scene scene = new Scene(root, 760, 420);

        stage.setTitle("TextPane Sample");
        stage.setScene(scene);
        stage.show();
    }

    private static RichText createSampleText(Label status) {
        RichTextBuilderFx b = new RichTextBuilderFx();
        b.push(Style.BOLD).append("TextPane demo").pop(Style.BOLD).append('\n');
        b.append("This sample renders RichText via FxGraphics with automatic line wrapping. ");
        b.append("Resize the width slider and toggle wrapping to see the layout update.").append("\n\n");

        b.append("Inline controls: ");
        b.appendHyperlink("Hyperlink with space", () -> status.setText("Hyperlink clicked"));
        b.append(" followed by text, and ");
        b.appendButton("Button 1", () -> status.setText("Inline button 1 clicked"));
        b.append(" followed by text.\n\n");

        b.append("Wrap test: this sentence is intentionally long so that the inline ");
        b.appendButton("Button 2", () -> status.setText("Inline button 2 clicked"));
        b.append(" is likely moved to a new line while text before and after keeps normal spacing.\n\n");

        b.append("Long paragraph: ");
        b.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ");
        b.append("ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ");
        b.append("laboris nisi ut aliquip ex ea commodo consequat.");

        return b.toRichText();
    }
}
