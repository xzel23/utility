// Copyright 2019 Axel Howind
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import com.dua3.utility.io.FileType;
import com.dua3.utility.io.OpenMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for creating various types of dialogs and panes.
 */
public final class Dialogs {

    // utility - no instances
    private Dialogs() {}

    /**
     * Start definition of new Warning dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder warning(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.WARNING, parentWindow);
    }

    /**
     * Start definition of new Warning pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder warningPane() {
        return new AlertPaneBuilder(AlertType.WARNING);
    }

    /**
     * Start definition of new Error dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder error(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.ERROR, parentWindow);
    }

    /**
     * Start definition of new Error pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder errorPane() {
        return new AlertPaneBuilder(AlertType.ERROR);
    }

    /**
     * Start definition of new Information dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder information(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.INFORMATION, parentWindow);
    }

    /**
     * Start definition of new Information pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder informationPane() {
        return new AlertPaneBuilder(AlertType.INFORMATION);
    }

    /**
     * Start definition of new Confirmation dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AlertBuilder} instance
     */
    public static AlertBuilder confirmation(@Nullable Window parentWindow) {
        return new AlertBuilder(AlertType.CONFIRMATION, parentWindow);
    }

    /**
     * Start definition of new Confirmation pane.
     *
     * @return new {@link AlertPaneBuilder} instance
     */
    public static AlertPaneBuilder confirmationPane() {
        return new AlertPaneBuilder(AlertType.CONFIRMATION);
    }

    /**
     * Start definition of new FileChooser.
     *
     * @return new {@link FileChooserBuilder} instance
     */
    public static FileChooserBuilder chooseFile() {
        return new FileChooserBuilder();
    }

    /**
     * Start definition of new DirectoryChooser.
     *
     * @return new {@link DirectoryChooserBuilder} instance
     */
    public static DirectoryChooserBuilder chooseDirectory() {
        return new DirectoryChooserBuilder();
    }

    /**
     * Start definition of new AboutDialog.
     *
     * @param parentWindow the parent window
     * @return new {@link AboutDialogBuilder} instance
     */
    public static AboutDialogBuilder about(@Nullable Window parentWindow) {
        return new AboutDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new prompt dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link PromptBuilder} instance
     */
    public static PromptBuilder prompt(@Nullable Window parentWindow) {
        return new PromptBuilder(parentWindow);
    }

    /**
     * Start definition of new prompt pane.
     *
     * @return new {@link PromptBuilder} instance
     */
    public static PromptPaneBuilder promptPane() {
        return new PromptPaneBuilder();
    }

    /**
     * Start definition of a new input dialog.
     *
     * @param parentWindow the parent window
     * @return new {@link InputDialogBuilder} instance
     */
    public static InputDialogBuilder input(@Nullable Window parentWindow) {
        return new InputDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new input pane.
     *
     * @return new {@link InputPaneBuilder} instance
     */
    public static InputPaneBuilder inputPane() {
        return new InputPaneBuilder();
    }

    /**
     * Start definition of new input dialog.
     *
     * @return new {@link InputDialogBuilder} instance
     */
    public static InputGridBuilder inputGrid() {
        return new InputGridBuilder();
    }

    /**
     * Start the definition of a new {@link OptionsDialog}.
     *
     * @param parentWindow the parent window
     * @return new {@link OptionsDialogBuilder} instance
     */
    public static OptionsDialogBuilder options(@Nullable Window parentWindow) {
        return new OptionsDialogBuilder(parentWindow);
    }

    /**
     * Start definition of new wizard dialog.
     *
     * @return new {@link WizardDialogBuilder} instance
     */
    public static WizardDialogBuilder wizard() {
        return new WizardDialogBuilder();
    }

    /**
     * Exception thrown to indicate that an unsupported file type was encountered.
     * This exception is generally used in scenarios where certain file types are not
     * suitable for a given operation or are not supported by the application.
     */
    public static class UnsupportedFileTypeException extends IOException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * The file path associated with the exception, if available.
         */
        private final transient @Nullable Path path;

        /**
         * Constructs a new UnsupportedFileTypeException with a specified detail message.
         *
         * @param message the detail message describing the reason for the exception.
         */
        public UnsupportedFileTypeException(String message) {
            this(message, null);
        }

        /**
         * Constructs a new {@code UnsupportedFileTypeException} with the specified detail message
         * and an optional file path associated with the exception.
         *
         * @param message the detail message explaining the reason for the exception
         * @param path the file path associated with the unsupported file type, or {@code null} if not applicable
         */
        public UnsupportedFileTypeException(String message, @Nullable Path path) {
            super(message);
            this.path = path;
        }

        /**
         * Retrieves the file path associated with the exception, if available.
         *
         * @return an {@link Optional} containing the file path if it exists, or an empty {@code Optional} if the path is not set.
         */
        Optional<Path> getPath() {
            return Optional.ofNullable(path);
        }
    }

    /**
     * Opens a file selection dialog for the user to choose a file, with support for filtering by file types.
     * The selected file is then read and returned wrapped in an {@link Optional}, or {@link Optional#empty()} if no file is chosen.
     * If the file type is unsupported or cannot be determined, an {@link UnsupportedFileTypeException} is thrown.
     *
     * @param <T>               The type of object that the selected file will be read into.
     * @param stage             The parent {@link Stage} for the file chooser dialog.
     * @param type              The class type of the objects to read from the selected file.
     * @param defaultFileType   The default file type to preselect in the file chooser, or {@code null} if no default is specified.
     * @return An {@link Optional} containing an object of type {@code T} read from the selected file,
     *         or an empty {@link Optional} if no file is selected.
     * @throws IOException                  If an I/O error occurs while reading the selected file.
     * @throws UnsupportedFileTypeException If the type of the selected file cannot be determined or is unsupported.
     */
    public static <T> Optional<T> openFile(Stage stage, Class<T> type, @Nullable FileType<? extends T> defaultFileType) throws IOException {
        FileChooser fileChooser = new FileChooser();

        var types = FileType.allReadersForType(type).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        Dialogs::createExtensionFilter,
                        (a, b) -> b,
                        IdentityHashMap::new)
                );

        fileChooser.getExtensionFilters().addAll(
                types.values().stream()
                        .sorted(Comparator.comparing(FileChooser.ExtensionFilter::getDescription))
                        .toList()
        );
        fileChooser.setSelectedExtensionFilter(types.get(defaultFileType));

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return Optional.empty();
        }

        FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
        Optional<? extends FileType<? extends T>> fileType = types.entrySet().stream()
                .filter(entry -> entry.getValue() == selectedFilter)
                .map(Map.Entry::getKey)
                .findFirst();

        if (fileType.isEmpty()) {
            fileType = types.keySet().stream()
                    .filter(ft -> ft.isSupported(OpenMode.READ))
                    .filter(ft -> type.isAssignableFrom(ft.getDocumentClass()))
                    .findFirst();
        }

        if (fileType.isEmpty()) {
            throw new UnsupportedFileTypeException("The type of the file could not be determined");
        }

        return Optional.of(fileType.orElseThrow().read(selectedFile.toPath()));
    }

    /**
     * Creates a new {@link FileChooser.ExtensionFilter} instance based on the provided {@link FileType}.
     *
     * @param <T> The type that this file type can handle.
     * @param ft  The file type containing the name and extension patterns to be used for the filter.
     * @return A new {@link FileChooser.ExtensionFilter} with the specified name and extension patterns.
     */
    public static <T> FileChooser.ExtensionFilter createExtensionFilter(FileType<? extends T> ft) {
        return new FileChooser.ExtensionFilter(ft.getName(), ft.getExtensionPatterns()
        );
    }

    /**
     * Opens a file save dialog to allow the user to save the given object to a file.
     * The dialog allows filtering by supported file types and preselects a default file type if specified.
     * The method writes the object's data to the selected file in the appropriate format.
     *
     * @param <T>            The type of object to be saved.
     * @param stage          The parent {@link Stage} for the file chooser dialog.
     * @param object         The object to be saved to a file.
     * @param defaultFileType The default file type to preselect in the file chooser, or {@code null} if no default is specified.
     * @param defaultPath    The default path to preselect in the file chooser, or {@code null} if no default is specified.
     * @return {@code true} if the object was successfully saved to a file; {@code false} if the user canceled the operation.
     * @throws IOException If an I/O error occurs during file saving.
     * @throws UnsupportedFileTypeException If the type of the file cannot be determined or is unsupported.
     */
    public static <T> boolean saveToFile(
            Stage stage, T object,
            @Nullable FileType<? extends T> defaultFileType,
            @Nullable Path defaultPath
    ) throws IOException {
        FileChooser fileChooser = new FileChooser();

        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) object.getClass();
        var types = FileType.allWritersForType(type).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        Dialogs::createExtensionFilter,
                        (a, b) -> b,
                        IdentityHashMap::new)
                );

        fileChooser.getExtensionFilters().addAll(
                types.values().stream()
                        .sorted(Comparator.comparing(FileChooser.ExtensionFilter::getDescription))
                        .toList()
        );
        fileChooser.setSelectedExtensionFilter(types.get(defaultFileType));

        if (defaultPath != null) {
            Path filename = defaultPath.getFileName();
            if (filename != null) {
                fileChooser.setInitialFileName(filename.toString());
            }

            Path parentPath = defaultPath.getParent();
            if (parentPath != null) {
                fileChooser.setInitialDirectory(parentPath.toFile());
            }
        }

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile == null) {
            return false;
        }

        FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
        Optional<? extends FileType<? super T>> fileType = types.entrySet().stream()
                .filter(entry -> entry.getValue() == selectedFilter)
                .map(Map.Entry::getKey)
                .findFirst();

        if (fileType.isEmpty()) {
            fileType = types.keySet().stream()
                    .filter(ft -> ft.isSupported(OpenMode.WRITE))
                    .filter(ft -> type.isAssignableFrom(ft.getWriteableClass()))
                    .findFirst();
        }

        if (fileType.isEmpty()) {
            throw new UnsupportedFileTypeException("The type of the file could not be determined");
        }

        fileType.orElseThrow().write(selectedFile.toPath(), object);

        return true;
    }
}
