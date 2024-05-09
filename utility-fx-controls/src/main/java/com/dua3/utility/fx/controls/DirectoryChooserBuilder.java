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

import com.dua3.cabe.annotations.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


/**
 * Builder for directory chooser dialogs.
 * <p>
 * Provides a fluent interface to create file dialogs.
 */
public class DirectoryChooserBuilder {
    private static final Logger LOG = LogManager.getLogger(DirectoryChooserBuilder.class);

    public static final Path USER_HOME = Paths.get(System.getProperty("user.home"));

    private Path initialDir = USER_HOME;

    DirectoryChooserBuilder() {
    }

    /**
     * Show "Open" dialog.
     *
     * @param parent the parent window
     * @return an Optional containing the selected file.
     */
    public Optional<Path> showDialog(@Nullable Window parent) {
        DirectoryChooser chooser = build();
        return Optional.ofNullable(chooser.showDialog(parent)).map(File::toPath);
    }

    private DirectoryChooser build() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (initialDir != null) {
            // NOTE there's an inconsistency between Paths.get("").toFile() and new File(""), so convert Path to File
            // before testing for directory and do not use Files.isDirectory(Path)
            try {
                File initialFile = initialDir.toFile();
                if (initialFile.isDirectory()) {
                    LOG.debug("initial directory: {}", initialFile);
                    chooser.setInitialDirectory(initialFile);
                }
            } catch (UnsupportedOperationException e) {
                LOG.warn("could not set initial directory", e);
            }
        }
        return chooser;
    }

    /**
     * Set initial directory.
     *
     * @param initialDir the initial directory
     * @return this instance
     */
    public DirectoryChooserBuilder initialDir(@Nullable Path initialDir) {
        this.initialDir = initialDir != null ? initialDir : USER_HOME;
        return this;
    }

}
