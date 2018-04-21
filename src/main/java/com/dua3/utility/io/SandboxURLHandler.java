package com.dua3.utility.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Objects;


public class SandboxURLHandler extends URLStreamHandler {

    private final Class<?> clazz;
	private final String root;

    SandboxURLHandler(Class<?> clazz, String path) {
        this.clazz = Objects.requireNonNull(clazz);
        Objects.requireNonNull(path);
    
        String prefix = clazz.getPackage().getName().replace('.', '/');
        this.root = path.isEmpty() ? prefix : prefix + "/" + path;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        String name = root+url.getHost()+url.getPath();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        URL localURL = cl.getResource(name);
        
        if (localURL==null) {
        	throw new IOException("Missing local file for "+url);
        }

        return localURL.openConnection();
    }

}
