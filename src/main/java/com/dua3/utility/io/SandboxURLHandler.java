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
        this.root = Objects.requireNonNull(path);
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        String name = root+url.getHost()+url.getPath();
		URL localURL = clazz.getResource(name);
        if (localURL==null) {
        	throw new IOException("Missing local file: "+name+" ["+url+"]");
        }
        URLConnection connection = localURL.openConnection();
        return connection;
    }

}
