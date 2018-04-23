package com.dua3.utility.io;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class SandboxURLHandler extends URLStreamHandler {

    private final FileSystemView localFiles;

    SandboxURLHandler(FileSystemView localFiles) {
        this.localFiles = Objects.requireNonNull(localFiles);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        Path path;
		try {
			path = localFiles.resolve(Paths.get(url.toURI()));
            return path.toUri().toURL().openConnection();
		} catch (URISyntaxException e) {
            throw new IOException(e);
		}
    }

}
