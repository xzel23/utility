package com.dua3.utility.data;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Image {
    
    public abstract void write(OutputStream out) throws IOException;
    
    public abstract int width();
    public abstract int height();
    
}
