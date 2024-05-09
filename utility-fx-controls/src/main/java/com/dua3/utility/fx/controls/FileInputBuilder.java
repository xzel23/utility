package com.dua3.utility.fx.controls;

import com.dua3.cabe.annotations.Nullable;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public final class FileInputBuilder {

    private final FileDialogMode mode;
    private final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
    private Supplier<Path> initialPath = () -> null;
    private boolean existingOnly = true;
    private Function<Path, Optional<String>> validate;
    private ObservableValue<Boolean> disabled;

    FileInputBuilder(FileDialogMode mode) {
        this.mode = mode;
        this.validate = this::defaultValidate;
    }

    public FileInputBuilder disabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return this;
    }

    public FileInputBuilder validate(Function<Path, Optional<String>> validate) {
        this.validate = validate;
        return this;
    }

    public FileInputBuilder initialPath(@Nullable Path initialPath) {
        this.initialPath = () -> initialPath;
        return this;
    }

    public FileInputBuilder initialPath(Supplier<Path> initialPath) {
        this.initialPath = initialPath;
        return this;
    }

    public FileInputBuilder filter(FileChooser.ExtensionFilter... filter) {
        extensionFilters.addAll(Arrays.asList(filter));
        return this;
    }

    public FileInputBuilder existingOnly(boolean flag) {
        this.existingOnly = flag;
        return this;
    }

    public FileInput build() {
        FileInput control = new FileInput(mode, existingOnly, initialPath, extensionFilters, validate);
        if (disabled != null) {
            control.disableProperty().bind(disabled);
        }

        return control;
    }

    private String itemText(boolean captitalize) {
        return switch (mode) {
            case DIRECTORY -> captitalize ? "Directory" : "directory";
            case OPEN, SAVE -> captitalize ? "File" : "file";
        };
    }

    private Optional<String> defaultValidate(@Nullable Path p) {
        if (p == null) {
            return Optional.of("No " + itemText(false) + "selected");
        }
        if (existingOnly && !Files.exists(p)) {
            return Optional.of(itemText(true) + " does not exist");
        }
        if (!existingOnly) {
            return Optional.empty();
        }
        final boolean isDirectory = Files.isDirectory(p);
        return switch (mode) {
            case DIRECTORY -> isDirectory
                    ? Optional.empty()
                    : Optional.of("Selection is not a " + itemText(false));
            case OPEN -> isDirectory
                    ? Optional.of("Selection is a directory")
                    : Optional.empty();
            case SAVE -> isDirectory
                    ? Optional.of("Selection is a directory")
                    : (!Files.isWritable(p) ? Optional.of(itemText(true) + "is not writeable") : Optional.empty());
        };
    }
}
