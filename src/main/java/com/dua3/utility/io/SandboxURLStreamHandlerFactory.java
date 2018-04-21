package com.dua3.utility.io;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Objects;

public class SandboxURLStreamHandlerFactory implements URLStreamHandlerFactory{

    private final Class<?> clazz;
    private final String root;

    public SandboxURLStreamHandlerFactory(Class<?> clazz, String root) {
        this.clazz = Objects.requireNonNull(clazz);
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        switch (protocol) {
        case "file":
        case "jar":
            return null;
        default:
            return new SandboxURLHandler(clazz, root);
        }
    }

}
