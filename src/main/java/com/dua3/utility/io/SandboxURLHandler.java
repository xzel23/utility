package com.dua3.utility.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


public class SandboxURLHandler extends URLStreamHandler {

    private final Class<?> clazz;

    SandboxURLHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        URL localURL = clazz.getResource("/"+url.getHost()+url.getPath());
        if (localURL==null) {
        	throw new IOException("Missing local file for "+url);
        }
        URLConnection connection = localURL.openConnection();
        return connection;
    }

}
