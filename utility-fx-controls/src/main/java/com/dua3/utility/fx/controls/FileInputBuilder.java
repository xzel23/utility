package com.dua3.utility.fx.controls;

import com.dua3.utility.i18n.I18N;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Builder for creating instances of {@link FileInput} with customizable options.
 * The builder provides methods to configure the file dialog mode, initial path,
 * extension filters, validation, and disabled state.
 */
public final class FileInputBuilder extends InputControlBuilder<FileInputBuilder, Path> {

    private final @Nullable Window owner;
    private final FileDialogMode mode;
    private final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
    private boolean existingOnly = true;
    private Function<Path, Optional<String>> validate;
    private @Nullable ObservableValue<Boolean> disabled;

    /**
     * Creates a new instance of FileInputBuilder with the specified file dialog mode.
     *
     * @param mode the {@link FileDialogMode} indicating the type of file dialog (OPEN, SAVE, or DIRECTORY)
     */
    FileInputBuilder(@Nullable Window owner, FileDialogMode mode) {
        this.owner = owner;
        this.mode = mode;
        this.validate = this::defaultValidate;
    }

    /**
     * Sets the 'disabled' property for the FileInput control.
     *
     * @param disabled an ObservableValue object representing the disabled state of the FileInput control
     * @return the current instance of FileInputBuilder with the 'disabled' property set
     */
    public FileInputBuilder disabled(ObservableValue<Boolean> disabled) {
        this.disabled = disabled;
        return self();
    }

    /**
     * Sets the validation function for the file input.
     *
     * @param validate a function that takes a Path and returns an Optional containing
     *                 an error message if validation fails, or an empty Optional if
     *                 validation succeeds
     * @return the updated FileInputBuilder instance
     */
    public FileInputBuilder validate(Function<Path, Optional<String>> validate) {
        this.validate = validate;
        return self();
    }

    /**
     * Adds the specified file extension filters to the FileChooser.
     *
     * @param filter One or more FileChooser.ExtensionFilter objects representing
     *               the file extension filters to be added.
     * @return The current instance of FileInputBuilder, allowing for method chaining.
     */
    public FileInputBuilder filter(FileChooser.ExtensionFilter... filter) {
        extensionFilters.addAll(Arrays.asList(filter));
        return self();
    }

    /**
     * Sets the flag indicating whether only existing files or directories should be selectable.
     *
     * @param flag a boolean flag; if true, only existing files or directories can be selected, otherwise new ones can also be selected
     * @return the current instance of FileInputBuilder for method chaining
     */
    public FileInputBuilder existingOnly(boolean flag) {
        this.existingOnly = flag;
        return self();
    }

    /**
     * Builds a {@link FileInput} object using the properties specified in the {@code FileInputBuilder}.
     *
     * @return a constructed {@link FileInput} control based on the current configuration.
     */
    @Override
    public FileInput build() {
        FileInput control = new FileInput(owner, mode, existingOnly, getDefault(), extensionFilters, validate);
        applyTo(control);
        if (disabled != null) {
            control.disableProperty().bind(disabled);
        }

        return control;
    }

    /**
     * Returns a string representation of the item based on the mode and whether the text should be capitalized.
     *
     * @param captitalize boolean indicating whether the text should be capitalized
     * @return a string representing the item, either "Directory" or "directory" when the mode is DIRECTORY,
     * and "File" or "file" when the mode is OPEN or SAVE
     */
    private String itemText(boolean captitalize) {
        return switch (mode) {
            case DIRECTORY -> captitalize
                    ? I18NInstance.get().get("dua3.fx.file.input.builder.directory.cap")
                    : I18NInstance.get().get("dua3.fx.file.input.builder.directory");
            case OPEN, SAVE -> captitalize
                    ? I18NInstance.get().get("dua3.fx.file.input.builder.file.cap")
                    : I18NInstance.get().get("dua3.fx.file.input.builder.file");
        };
    }

    /**
     * Validates a given file path based on the specified file dialog mode and whether
     * only existing files or directories should be selectable.
     *
     * @param p the path to validate, can be null
     * @return an Optional containing an error message if validation fails, or an empty Optional if validation succeeds
     */
    private Optional<String> defaultValidate(@Nullable Path p) {
        if (p == null) {
            return Optional.of(I18NInstance.get().format("dua3.fx.file.input.builder.no.item.selected", itemText(false)));
        }
        if (existingOnly && !Files.exists(p)) {
            return Optional.of(I18NInstance.get().format("dua3.fx.file.input.builder.item.does.not.exist", itemText(true)));
        }
        if (!existingOnly) {
            return Optional.empty();
        }
        final boolean isDirectory = Files.isDirectory(p);
        return switch (mode) {
            case DIRECTORY -> isDirectory
                    ? Optional.empty()
                    : Optional.of(I18NInstance.get().format("dua3.fx.file.input.builder.selection.is.not.a", itemText(false)));
            case OPEN -> isDirectory
                    ? Optional.of(I18NInstance.get().get("dua3.fx.file.input.builder.selection.is.a.directory"))
                    : Optional.empty();
            case SAVE -> isDirectory
                    ? Optional.of(I18NInstance.get().get("dua3.fx.file.input.builder.selection.is.a.directory"))
                    : (!Files.isWritable(p) ? Optional.of(I18NInstance.get().format("dua3.fx.file.input.builder.item.is.not.writable", itemText(true))) : Optional.empty());
        };
    }
}
