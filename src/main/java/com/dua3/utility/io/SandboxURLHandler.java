package com.dua3.utility.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandboxURLHandler extends URLStreamHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SandboxURLHandler.class);
    
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
        LOG.debug("redirecting {} to {}.", url, localURL);
        URLConnection connection = localURL.openConnection();
        return connection;
    }
    
}
