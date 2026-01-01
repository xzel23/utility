package com.dua3.utility.fx.controls;

import javafx.geometry.Pos;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.FxUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * FileInput is a custom control for selecting files using a FileChooser dialog.
 * It is composed of a {@link TextField} that contains the file path and a button that
 * opens a {@link FileChooser} to select a file.
 *
 * <p>The control can operate in different modes: OPEN, SAVE, or DIRECTORY, as specified
 * by the FileDialogMode.</p>
 *
 * <p>The control also includes properties for error messages and validation status.
 * These properties are updated based on the path selected by the user and the
 * specified validation function.</p>
 */
public class FileInput extends CustomControl<HBox> implements InputControl<Path> {

    private static final StringConverter<Path> PATH_CONVERTER = new PathConverter();
    private final InputControlState<Path> state;

    static class PathConverter extends StringConverter<Path> {
        @Override
        public @Nullable String toString(@Nullable Path path) {
            return path == null ? null : path.toString();
        }

        @Override
        public @Nullable Path fromString(@Nullable String s) {
            return s == null || s.isEmpty() ? null : Paths.get(s);
        }
    }

    private final ObjectProperty<@Nullable Path> value = new SimpleObjectProperty<>();

    private final FileDialogMode mode;
    private final FileChooser.ExtensionFilter[] filters;

    /**
     * Constructs a FileInput instance with specified parameters.
     *
     * @param parentWindow the parent window
     * @param mode         the mode of the file dialog, which can be OPEN, SAVE, or DIRECTORY
     * @param existingOnly boolean indicating whether only existing files or directories should be selectable
     * @param dflt         a supplier providing the default path
     * @param filters      collection of file extension filters to apply in the file chooser
     * @param validate     a function to validate the selected file path, returning an optional error message
     */
    public FileInput(
            @Nullable Window parentWindow,
            FileDialogMode mode,
            boolean existingOnly,
            Supplier<@Nullable Path> dflt,
            Collection<FileChooser.ExtensionFilter> filters,
            Function<@Nullable Path, Optional<String>> validate) {
        super(new HBox());
        container.setAlignment(Pos.BASELINE_LEFT);
        container.setFillHeight(false);

        getStyleClass().setAll("file-input");

        this.mode = mode;
        this.filters = filters.toArray(FileChooser.ExtensionFilter[]::new);
        this.state = new InputControlState<>(value, dflt, validate);

        TextField tfFilename = new TextField();
        Button button = new Button(I18NInstance.get().get("dua3.utility.fx.controls.file.input.button"));

        HBox.setHgrow(tfFilename, Priority.ALWAYS);

        button.setOnAction(evt -> {

            Path path = value.get();
            Path initialDir = path;
            if (initialDir != null && !Files.isDirectory(initialDir)) {
                initialDir = initialDir.getParent();
            }
            if (initialDir == null) {
                initialDir = Paths.get(".");
            }
            if (mode == FileDialogMode.DIRECTORY && (path == null || (Files.exists(path) && !Files.isDirectory(path)))) {
                value.setValue(initialDir);
            }

            switch (this.mode) {
                case OPEN -> Dialogs.chooseFile(parentWindow)
                        .initialDir(initialDir)
                        .filter(this.filters)
                        .showOpenDialog()
                        .ifPresent(value::setValue);
                case SAVE -> Dialogs.chooseFile(parentWindow)
                        .initialDir(initialDir)
                        .filter(this.filters)
                        .showSaveDialog()
                        .ifPresent(value::setValue);
                case DIRECTORY -> Dialogs.chooseDirectory(parentWindow)
                        .initialDir(initialDir)
                        .showDialog()
                        .ifPresent(value::setValue);
            }
        });

        container.getChildren().setAll(tfFilename, button);

        tfFilename.textProperty().bindBidirectional(valueProperty(), PATH_CONVERTER);

        // enable drag&drop
        Function<List<Path>, List<TransferMode>> acceptPath = list ->
                list.isEmpty() ? Collections.emptyList() : List.of(TransferMode.MOVE);
        tfFilename.setOnDragOver(FxUtil.dragEventHandler(acceptPath));
        tfFilename.setOnDragDropped(FxUtil.dropEventHandler(list -> valueProperty().setValue(list.getFirst())));

    }

    /**
     * Returns a function object that validates the file selection based on the specified file dialog mode and whether
     * only existing files or directories are allowed.
     *
     * <p>The returned function object is for example used in
     * {@link InputBuilder#inputFile(String, String, Supplier, FileDialogMode, boolean, Collection)}
     * to add validation.
     *
     * @param mode the mode of the file dialog; can be OPEN, SAVE, or DIRECTORY
     * @param existingOnly indicates whether only existing files or directories should be selectable
     * @return a function that takes a Path and returns an Optional containing an error message if validation fails, or an empty Optional if validation succeeds
     */
    public static Function<@Nullable Path, Optional<String>> defaultValidate(FileDialogMode mode, boolean existingOnly) {
        return p -> {
            if (p == null || p.toString().isBlank()) {
                return Optional.of(I18NInstance.get().get("dua3.utility.fx.controls.file.input.nothing.selected"));
            }

            boolean exists = Files.exists(p);
            boolean isDirectory = Files.isDirectory(p);

            switch (mode) {
                case DIRECTORY -> {
                    // is a directory or existingOnly is not set and doesn't exist
                    if (exists && !isDirectory) {
                        return Optional.of(I18NInstance.get().format("dua3.utility.fx.controls.file.input.not.a.directory", p));
                    }
                    if (existingOnly && !exists) {
                        return Optional.of(I18NInstance.get().format("dua3.utility.fx.controls.file.input.does.not.exist", p));
                    }
                    return Optional.empty();
                }
                case OPEN, SAVE -> {
                    if (isDirectory) {
                        return Optional.of(I18NInstance.get().format("dua3.utility.fx.controls.file.input.is.a.directory", p));
                    }
                    if (existingOnly && !exists) {
                        return Optional.of(I18NInstance.get().format("dua3.utility.fx.controls.file.input.does.not.exist", p));
                    }
                    return Optional.empty();
                }
                default -> throw new IllegalArgumentException("Unknown FileDialogMode: " + mode);
            }
        };
    }

    @Override
    public InputControlState<Path> state() {
        return state;
    }

    @Override
    public Node node() {
        return this;
    }

}
