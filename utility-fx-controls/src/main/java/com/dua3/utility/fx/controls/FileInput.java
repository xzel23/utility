package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.fx.FxUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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

public class FileInput extends CustomControl<HBox> implements InputControl<Path> {

    private static final StringConverter<Path> PATH_CONVERTER = new PathConverter();

    static class PathConverter extends StringConverter<Path> {
        @Override
        public String toString(@Nullable Path path) {
            return path == null ? "" : path.toString();
        }

        @Override
        public Path fromString(@Nullable String s) {
            return s == null ? Paths.get("") : Paths.get(s);
        }
    }

    private final TextField tfFilename;
    private final Button button;

    private final ObjectProperty<Path> value = new SimpleObjectProperty<>();

    private final FileDialogMode mode;
    private final FileChooser.ExtensionFilter[] filters;
    private final Supplier<Path> dflt;
    private final boolean existingOnly;

    private final StringProperty error = new SimpleStringProperty("");
    private final BooleanProperty valid = new SimpleBooleanProperty(true);

    public FileInput(
            FileDialogMode mode,
            boolean existingOnly,
            Supplier<Path> dflt,
            Collection<FileChooser.ExtensionFilter> filters,
            Function<Path, Optional<String>> validate) {
        super(new HBox());

        getStyleClass().setAll("file-input");

        this.mode = mode;
        this.existingOnly = existingOnly;
        this.filters = filters.toArray(FileChooser.ExtensionFilter[]::new);
        this.dflt = dflt;

        this.tfFilename = new TextField();
        this.button = new Button("…");

        HBox.setHgrow(tfFilename, Priority.ALWAYS);

        button.setOnAction(evt -> {

            Path initialDir = value.get();
            if (initialDir != null && !Files.isDirectory(initialDir)) {
                initialDir = initialDir.getParent();
            }
            if (initialDir == null) {
                initialDir = Paths.get(".");
            }

            switch (this.mode) {
                case OPEN -> Dialogs.chooseFile()
                        .initialDir(initialDir)
                        .filter(this.filters)
                        .showOpenDialog(null)
                        .ifPresent(value::setValue);
                case SAVE -> Dialogs.chooseFile()
                        .initialDir(initialDir)
                        .filter(this.filters)
                        .showSaveDialog(null)
                        .ifPresent(value::setValue);
                case DIRECTORY -> Dialogs.chooseDirectory()
                        .initialDir(initialDir)
                        .showDialog(null)
                        .ifPresent(value::setValue);
            }
        });

        container.getChildren().setAll(tfFilename, button);

        tfFilename.textProperty().bindBidirectional(valueProperty(), PATH_CONVERTER);

        // error property
        StringExpression errorText = Bindings.createStringBinding(
                () -> {
                    Path file = value.get();
                    if (file == null) {
                        return "No file selected.";
                    }
                    if (mode == FileDialogMode.OPEN && !Files.exists(file)) {
                        return "File does not exist: " + file;
                    }
                    return "";
                },
                value
        );

        error.bind(errorText);

        // valid property
        valid.bind(Bindings.createBooleanBinding(() -> validate.apply(getPath()).isEmpty(), value));

        // enable drag&drop
        Function<List<Path>, List<TransferMode>> acceptPath = list ->
                list.isEmpty() ? Collections.emptyList() : List.of(TransferMode.MOVE);
        tfFilename.setOnDragOver(FxUtil.dragEventHandler(acceptPath));
        tfFilename.setOnDragDropped(FxUtil.dropEventHandler(list -> valueProperty().setValue(list.get(0))));

        // set initial path
        Path p = dflt.get();
        if (p != null) {
            set(p);
        }
    }

    public static Function<Path, Optional<String>> defaultValidate(FileDialogMode mode, boolean existingOnly) {
        return p -> {
            if (p == null) {
                return Optional.of("Nothing selected");
            }

            boolean exists = Files.exists(p);
            boolean isDirectory = Files.isDirectory(p);

            switch (mode) {
                case DIRECTORY -> {
                    // is a directory or existingOnly is not set and doesn't exist
                    if (exists && !isDirectory) {
                        return Optional.of("Not a directory: " + p);
                    }
                    if (existingOnly && !exists) {
                        return Optional.of("Does not exist: " + p);
                    }
                    return Optional.empty();
                }
                case OPEN, SAVE -> {
                    if (isDirectory) {
                        return Optional.of("Is a directory: " + p);
                    }
                    if (existingOnly && !exists) {
                        return Optional.of("Does not exist: " + p);
                    }
                    return Optional.empty();
                }
                default -> throw new IllegalArgumentException("Unknown FileDialogMode: " + mode);
            }
        };
    }

    private Path getPath() {
        return value.get();
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public void reset() {
        value.setValue(dflt.get());
    }

    @Override
    public Property<Path> valueProperty() {
        return value;
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return error;
    }

}
