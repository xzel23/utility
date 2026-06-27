package com.dua3.utility.samples.fx;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.fx.controls.RichTextBuilderFx;
import com.dua3.utility.fx.controls.TextEditorPane;
import com.dua3.utility.fx.controls.TextPane;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.VAnchor;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Combined sample application for {@link TextEditorPane} and {@link TextPane}.
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
        Label status = new Label("No action yet.");
        RichText text = createSampleText(status);

        TextEditorPane editor = new TextEditorPane(text);
        editor.setWrapText(true);
        editor.setToolbarVisible(true);
        editor.setPrefWidth(600);
        editor.setMaxHeight(1000);

        TextPane liveDocumentPane = new TextPane();
        liveDocumentPane.setWrapText(true);
        liveDocumentPane.setPrefWidth(600);
        liveDocumentPane.setMaxHeight(1000);
        ObjectBinding<RichText> liveDocumentBinding = Bindings.createObjectBinding(
                editor::getDocumentText,
                editor.documentVersionProperty()
        );
        liveDocumentPane.textProperty().bind(liveDocumentBinding);
        liveDocumentPane.wrapTextProperty().bind(editor.wrapTextProperty());

        TextPane committedValuePane = new TextPane();
        committedValuePane.setWrapText(true);
        committedValuePane.setPrefWidth(600);
        committedValuePane.setMaxHeight(1000);
        committedValuePane.textProperty().bind(editor.valueProperty());
        committedValuePane.wrapTextProperty().bind(editor.wrapTextProperty());
        editor.setHyperlinkHandler(uri -> handleSampleUri(status, uri));
        liveDocumentPane.setHyperlinkHandler(uri -> handleSampleUri(status, uri));
        committedValuePane.setHyperlinkHandler(uri -> handleSampleUri(status, uri));

        CheckBox wrap = new CheckBox("Wrap text");
        wrap.setSelected(true);
        editor.wrapTextProperty().bind(wrap.selectedProperty());

        CheckBox editable = new CheckBox("Editable");
        editable.setSelected(editor.isEditable());
        editable.selectedProperty().bindBidirectional(editor.editableProperty());

        CheckBox toolbar = new CheckBox("Toolbar");
        toolbar.setSelected(editor.isToolbarVisible());
        toolbar.selectedProperty().bindBidirectional(editor.toolbarVisibleProperty());

        Label selectionInfo = new Label();
        Runnable updateSelectionInfo = () -> selectionInfo.setText(
                "Caret: " + editor.getCaretPosition()
                        + "  Selection: " + editor.getSelection()
                        + "  Selected: \"" + editor.getSelectedText().toString().replace("\n", "\\n") + "\""
        );
        editor.caretPositionProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        editor.selectionProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        editor.selectedTextProperty().addListener((obs, o, n) -> updateSelectionInfo.run());
        updateSelectionInfo.run();

        Label committedInfo = new Label();
        Runnable updateCommittedInfo = () -> {
            RichText value = editor.get();
            int length = value == null ? 0 : value.length();
            committedInfo.setText("Committed value length: " + length + " characters");
        };
        editor.valueProperty().addListener((obs, oldValue, newValue) -> updateCommittedInfo.run());
        updateCommittedInfo.run();

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(evt -> {
            editor.commitValue();
            status.setText("Applied editor value.");
        });

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(evt -> {
            editor.reset();
            editor.cancelEdit();
            status.setText("Reset to default value.");
        });

        HBox controls = new HBox(12, wrap, editable, toolbar, applyButton, resetButton);
        controls.setPadding(new Insets(0, 0, 8, 0));

        Label liveDocumentHeader = new Label("Live Document (documentVersionProperty + getDocumentText)");
        Label committedHeader = new Label("Committed Value (InputControl valueProperty)");

        VBox content = new VBox(
                8,
                controls,
                editor,
                selectionInfo,
                liveDocumentHeader,
                liveDocumentPane,
                committedHeader,
                committedValuePane,
                status
        );
        VBox.setVgrow(editor, Priority.ALWAYS);
        VBox.setVgrow(liveDocumentPane, Priority.ALWAYS);
        VBox.setVgrow(committedValuePane, Priority.ALWAYS);
        content.setPadding(new Insets(12));

        BorderPane root = new BorderPane(content);
        Scene scene = new Scene(root, 900, 760);

        stage.setTitle("TextEditorPane / TextPane Sample");
        stage.setScene(scene);
        stage.show();
    }

    private static RichText createSampleText(Label status) {
        RichTextBuilderFx b = new RichTextBuilderFx();
        b.push(Style.BOLD).append("Combined TextEditorPane/TextPane demo").pop(Style.BOLD).append('\n');
        b.append("Edit in the upper pane, then press Apply to commit to InputControl state.\n");
        b.append("Reset restores the default InputControl value.\n\n");

        b.append("Inline controls: ");
        b.appendHyperlink("Hyperlink with space", statusUri("Hyperlink clicked"));
        b.append(" followed by text, and ");
        b.appendButton("Button 1", () -> status.setText("Button 1 clicked"));
        b.append(" followed by text.\n\n");

        Image imageOriginal = createDemoImage(96, 48, 0xFF147BDA, 0xFF13BFA7);
        Image imageScaled = createDemoImage(240, 140, 0xFFE38C22, 0xFFE34F6A);
        float maxWidth = 120.0f;
        float maxHeight = 20.0f;

        String separator = "---------------------------------------------\n";

        b.append(separator);
        b.append("Images (default vAnchor): original ");
        b.appendImage(imageOriginal);
        b.append(" and scaled ");
        b.appendImage(imageScaled, maxWidth, maxHeight);
        b.append("\n");

        for (VAnchor vAnchor : VAnchor.values()) {
            b.append(separator);
            b.append("Images (vAnchor=").append(vAnchor.name()).append("): original ");
            b.appendImage(imageOriginal, vAnchor);
            b.append(" and scaled ");
            b.appendImage(imageScaled, maxWidth, maxHeight, vAnchor);
            b.append('\n');
        }
        b.append(separator);

        b.append("Wrap test: this sentence is intentionally long so that the inline ");
        b.appendButton("Button 2", () -> status.setText("Button 2 clicked"));
        b.append(" is likely moved to a new line while text before and after keeps normal spacing.\n\n");

        b.append("Long paragraph: ");
        b.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ");
        b.append("ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ");
        b.append("laboris nisi ut aliquip ex ea commodo consequat.");
        return b.toRichText();
    }

    private static URI statusUri(String text) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
        return URI.create("testapp://setStatus?text=" + encoded);
    }

    private static void handleSampleUri(Label status, URI uri) {
        if ("testapp".equalsIgnoreCase(uri.getScheme()) && "setStatus".equalsIgnoreCase(uri.getHost())) {
            Optional<String> text = queryParameter(uri, "text");
            status.setText(text.orElse("No status text provided."));
            return;
        }

        if (RichTextBuilderExtBase.INLINE_BUTTON_FALLBACK_URI_SCHEME.equalsIgnoreCase(uri.getScheme())
                && "action".equalsIgnoreCase(uri.getHost())) {
            Optional<String> text = queryParameter(uri, "text");
            status.setText(text.orElse("Button clicked"));
        }
    }

    private static Optional<String> queryParameter(URI uri, String key) {
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        String prefix = key + "=";
        for (String part : query.split("&")) {
            if (part.startsWith(prefix)) {
                String value = part.substring(prefix.length());
                return Optional.of(URLDecoder.decode(value, StandardCharsets.UTF_8));
            }
        }
        return Optional.empty();
    }

    private static Image createDemoImage(int width, int height, int argbA, int argbB) {
        int[] data = new int[width * height];
        for (int y = 0; y < height; y++) {
            float fy = height > 1 ? (float) y / (height - 1) : 0.0f;
            for (int x = 0; x < width; x++) {
                float fx = width > 1 ? (float) x / (width - 1) : 0.0f;
                float f = 0.65f * fx + 0.35f * fy;
                data[y * width + x] = blendArgb(argbA, argbB, f);
            }
        }
        return ImageUtil.getInstance().createImage(width, height, data);
    }

    private static int blendArgb(int argbA, int argbB, float factor) {
        float f = Math.clamp(factor, 0.0f, 1.0f);
        int a = blendChannel((argbA >>> 24) & 0xFF, (argbB >>> 24) & 0xFF, f);
        int r = blendChannel((argbA >>> 16) & 0xFF, (argbB >>> 16) & 0xFF, f);
        int g = blendChannel((argbA >>> 8) & 0xFF, (argbB >>> 8) & 0xFF, f);
        int b = blendChannel(argbA & 0xFF, argbB & 0xFF, f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int blendChannel(int a, int b, float factor) {
        return Math.clamp(Math.round(a + (b - a) * factor), 0, 255);
    }
}
