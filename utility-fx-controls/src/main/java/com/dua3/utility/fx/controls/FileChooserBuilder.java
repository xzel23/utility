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

import com.dua3.utility.io.IoUtil;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Builder for file open/save dialogs.
 * <p>
 * Provides a fluent interface to create file dialogs.
 */
public class FileChooserBuilder {
    private static final Logger LOG = LogManager.getLogger(FileChooserBuilder.class);

    private final @Nullable Window parentWindow;
    private Path initialDir = IoUtil.getUserHome();
    private String initialFileName = "";
    private List<ExtensionFilter> filters = new ArrayList<>();
    private @Nullable ExtensionFilter selectedFilter = null;

    FileChooserBuilder(@Nullable Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    /**
     * Show "Open" dialog.
     *
     * @return an Optional containing the selected file.
     */
    public Optional<Path> showOpenDialog() {
        FileChooser chooser = build();
        return Optional.ofNullable(chooser.showOpenDialog(parentWindow)).map(File::toPath);
    }

    private FileChooser build() {
        FileChooser chooser = new FileChooser();

        chooser.getExtensionFilters().setAll(filters);
        if (selectedFilter != null) {
            chooser.setSelectedExtensionFilter(selectedFilter);
        }

        Controls.setInitialDirectory(chooser::setInitialDirectory, initialDir);
        LOG.trace("initial directory: {}", chooser::getInitialDirectory);

        chooser.setInitialFileName(initialFileName);

        return chooser;
    }

    /**
     * Show "Open multiple" dialog.
     *
     * @return a List containing the selected files, or an empty list if no files were selected
     */
    public List<Path> showOpenMultipleDialog() {
        FileChooser chooser = build();
        List<File> files = chooser.showOpenMultipleDialog(parentWindow);
        return files == null ? Collections.emptyList() : files.stream().map(File::toPath).toList();
    }

    /**
     * Show "Save" dialog.
     *
     * @return an Optional containing the selected file.
     */
    public Optional<Path> showSaveDialog() {
        FileChooser chooser = build();
        return Optional.ofNullable(chooser.showSaveDialog(parentWindow)).map(File::toPath);
    }

    /**
     * Set initial filename and directory.
     *
     * @param file the file
     * @return this instance
     */
    public FileChooserBuilder initialFile(Path file) {
        if (Files.isDirectory(file)) {
            initialDir(file);
        } else {
            Path fileName = file.getFileName();
            initialFileName(fileName == null ? null : fileName.toString());
            initialDir(file.getParent());
        }
        return this;
    }

    /**
     * Set initial filename.
     *
     * @param initialFileName the initial filename
     * @return this instance
     */
    public FileChooserBuilder initialFileName(@Nullable String initialFileName) {
        this.initialFileName = initialFileName != null ? initialFileName : "";
        return this;
    }

    /**
     * Set initial directory. If the initial directory is inaccessible or non-existent, it will be ignored when
     * creating the dialog. If it exists, but is a regular file, the dialog will be created with the file's parent
     * directory set as its initial directory.
     *
     * @param initialDir the initial directory
     * @return this instance
     */
    public FileChooserBuilder initialDir(@Nullable Path initialDir) {
        this.initialDir = initialDir != null ? initialDir : IoUtil.getUserHome();
        return this;
    }

    /**
     * Add filter to the list of filters.
     *
     * @param name    the filter name
     * @param pattern the pattern(s) to use for this filter
     * @return this instance
     */
    public FileChooserBuilder addFilter(String name, String... pattern) {
        ExtensionFilter f = new ExtensionFilter(name, pattern);
        filters.add(f);
        return this;
    }

    /**
     * Set filters.
     * <p>
     * The current filters will be replaced.
     *
     * @param filters the filters to set
     * @return this instance
     */
    public FileChooserBuilder filter(Collection<ExtensionFilter> filters) {
        this.filters = new ArrayList<>(filters);
        return this;
    }

    /**
     * Set filters.
     * <p>
     * The current filters will be replaced.
     *
     * @param filters the filters to set
     * @return this instance
     */
    public FileChooserBuilder filter(ExtensionFilter... filters) {
        this.filters = new ArrayList<>(List.of(filters)); // list must be mutable!
        return this;
    }

    /**
     * Set selected filter.
     * <p>
     * The filter is appended to the list of filters if not present.
     *
     * @param f the selected filter
     * @return this instance
     */
    public FileChooserBuilder selectedFilter(@Nullable ExtensionFilter f) {
        this.selectedFilter = f;
        LangUtil.addIf(f != null && !filters.contains(f), filters, f);
        return this;
    }

}
