package com.dua3.utility.awt;

import com.dua3.utility.data.MutableImage;
import com.dua3.utility.io.Payload;
import org.jspecify.annotations.Nullable;

import java.awt.image.WritableRaster;
import java.io.IOException;

/**
 * A class that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public final class AwtMutableImage extends MutableImage implements AwtImage {

    AwtMutableImage(int w, int h, int @Nullable[] data) {
        super(w, h, data != null ? data : new int[w * h]);
    }

    AwtMutableImage(int w, int h) {
        this(w, h, null);
    }

    static AwtMutableImage loadImage(Payload payload) throws IOException {
        return loadImage(payload, AwtMutableImage::new);
    }

}
