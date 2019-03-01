// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.io;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Objects;

public class SandboxURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private final FileSystemView localFiles;

    public SandboxURLStreamHandlerFactory(FileSystemView localFiles) {
        this.localFiles = Objects.requireNonNull(localFiles);
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        switch (protocol) {
        case "file":
        case "jar":
            return null;
        default:
            return new SandboxURLHandler(localFiles);
        }
    }

}
