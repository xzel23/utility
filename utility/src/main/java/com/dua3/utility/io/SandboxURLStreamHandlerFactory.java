// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Objects;

public class SandboxURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final FileSystemView localFiles;

    public SandboxURLStreamHandlerFactory(@NotNull FileSystemView localFiles) {
        this.localFiles = Objects.requireNonNull(localFiles);
    }

    @Override
    public @Nullable URLStreamHandler createURLStreamHandler(@NotNull String protocol) {
        return switch (protocol) {
            case "file", "jar" -> null;
            default -> new SandboxURLHandler(localFiles);
        };
    }

}
