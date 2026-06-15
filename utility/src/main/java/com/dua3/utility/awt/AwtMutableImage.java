package com.dua3.utility.awt;

import com.dua3.utility.data.MutableImage;
import com.dua3.utility.io.Payload;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * A class that represents an image using the Swing BufferedImage API.
 * It implements the Image interface.
 */
public final class AwtMutableImage extends MutableImage implements AwtImage {

    /**
     * Constructs an AWT mutable image.
     *
     * @param w image width in pixels
     * @param h image height in pixels
     * @param data optional premultiplied ARGB pixel data; if {@code null}, a new array of size {@code w*h} is allocated
     */
    AwtMutableImage(int w, int h, int @Nullable[] data) {
        super(w, h, data != null ? data : new int[w * h]);
    }

    /**
     * Constructs an AWT mutable image.
     *
     * @param w image width in pixels
     * @param h image height in pixels
     */
    AwtMutableImage(int w, int h) {
        this(w, h, null);
    }

    /**
     * Loads an image from the provided payload and returns an instance of {@code AwtMutableImage}.
     *
     * @param payload the payload containing the image data to be loaded
     * @return an {@code AwtMutableImage} instance created from the provided payload
     * @throws IOException if an I/O error occurs during the image loading process
     */
    static AwtMutableImage loadImage(Payload payload) throws IOException {
        return loadImage(payload, AwtMutableImage::new);
    }

}
