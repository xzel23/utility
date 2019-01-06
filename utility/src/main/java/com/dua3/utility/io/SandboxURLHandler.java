package com.dua3.utility.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

import com.dua3.utility.lang.LangUtil;


public class SandboxURLHandler extends URLStreamHandler {

    private static final Logger LOG = Logger.getLogger(SandboxURLHandler.class.getName());
    private final FileSystemView localFiles;

    SandboxURLHandler(FileSystemView localFiles) {
        this.localFiles = Objects.requireNonNull(localFiles);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        LOG.info(LangUtil.msgs("opening connection to local version of: %s", url));
        String stripped = url.toExternalForm().replaceFirst(".*://", "");
        Path path = localFiles.resolve(stripped);
        return path.toUri().toURL().openConnection();
    }

}
