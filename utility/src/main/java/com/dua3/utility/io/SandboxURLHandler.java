// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import com.dua3.utility.logging.LogUtil;
import com.dua3.cabe.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

public class SandboxURLHandler extends URLStreamHandler {

    private static final Logger LOG = Logger.getLogger(SandboxURLHandler.class.getName());
    private final FileSystemView localFiles;

    SandboxURLHandler(@NotNull FileSystemView localFiles) {
        this.localFiles = Objects.requireNonNull(localFiles);
    }

    @Override
    protected URLConnection openConnection(@NotNull URL url) throws IOException {
        LOG.fine(LogUtil.format("opening connection to local version of: %s", url));
        String stripped = url.toExternalForm().replaceFirst(".*://", "");
        Path path = localFiles.resolve(stripped);
        return path.toUri().toURL().openConnection();
    }

}
