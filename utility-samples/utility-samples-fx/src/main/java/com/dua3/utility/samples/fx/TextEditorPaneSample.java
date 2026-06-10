package com.dua3.utility.samples.fx;

import com.dua3.utility.fx.controls.RichTextBuilderFx;
import com.dua3.utility.fx.controls.TextEditorPane;
import com.dua3.utility.text.RichText;
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
 * Sample application for {@link TextEditorPane}.
 */
public class TextEditorPaneSample extends Application {

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
    public TextEditorPaneSample() { /* nothing to do */ }

    @Override
    public void start(Stage stage) {
        TextEditorPane textPane = new TextEditorPane(createSampleText());
        textPane.setWrapText(true);
        textPane.setPrefWidth(600);
        textPane.setMaxHeight(1000);

        CheckBox wrap = new CheckBox("Wrap text");
        wrap.setSelected(true);
        textPane.wrapTextProperty().bind(wrap.selectedProperty());

        CheckBox editable = new CheckBox("Editable");
        editable.setSelected(textPane.isEditable());
        editable.selectedProperty().bindBidirectional(textPane.editableProperty());

        Slider width = new Slider(220, 700, 500);
        width.setShowTickLabels(true);
        width.setShowTickMarks(true);
        textPane.prefWidthProperty().bind(width.valueProperty());

        Label selectionInfo = new Label();
        Runnable updateSelectionInfo = () -> selectionInfo.setText(
                "Caret: " + textPane.getCaretPosition()
                        + "  Selection: " + textPane.getSelection()
                        + "  Selected: \"" + textPane.getSelectedText().toString().replace("\n", "\\n") + "\""
        );
        textPane.caretPositionProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        textPane.selectionProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        textPane.selectedTextProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        updateSelectionInfo.run();

        HBox controls = new HBox(12, wrap, editable, new Label("Width:"), width);
        controls.setPadding(new Insets(0, 0, 8, 0));
        HBox.setHgrow(width, Priority.ALWAYS);

        VBox content = new VBox(8, controls, textPane, selectionInfo);
        content.setPadding(new Insets(12));

        BorderPane root = new BorderPane(content);
        Scene scene = new Scene(root, 760, 420);

        stage.setTitle("TextEditorPane Sample");
        stage.setScene(scene);
        stage.show();
    }

    private static RichText createSampleText() {
        RichTextBuilderFx b = new RichTextBuilderFx();
        b.push(Style.BOLD).append("TextEditorPane stub demo").pop(Style.BOLD).append('\n');
        b.append("This sample currently demonstrates the API surface only. ");
        b.append("Editing behavior is intentionally incomplete and will be implemented incrementally.");
        return b.toRichText();
    }
}
