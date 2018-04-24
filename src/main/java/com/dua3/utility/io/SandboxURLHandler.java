package com.dua3.utility.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
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
        Path path;
		try {
            LOG.info(LangUtil.msgs("opening connecting to local version of: %s", url));
            path = localFiles.resolve(Paths.get(url.toURI()));
            return path.toUri().toURL().openConnection();
		} catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, "Invalid URL: "+url, e);
            throw new IOException(e);
		}
    }

}
