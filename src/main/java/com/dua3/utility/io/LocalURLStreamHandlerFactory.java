package com.dua3.utility.io;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class LocalURLStreamHandlerFactory implements URLStreamHandlerFactory{

    private final Class<?> clazz;

    public LocalURLStreamHandlerFactory(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        switch (protocol) {
        case "file":
        case "jar":
            return null;
        default:
            return new LocalURLHandler(clazz);
        }
    }

}
