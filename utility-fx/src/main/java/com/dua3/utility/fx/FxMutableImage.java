package com.dua3.utility.fx;

import com.dua3.utility.awt.AwtMutableImage;
import com.dua3.utility.data.MutableImage;
import com.dua3.utility.io.Payload;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.io.IOException;
import java.nio.IntBuffer;

/**
 * The FxBufferedImage class represents an image that integrates JavaFX and AWT components.
 * It is a subclass of BufferedImage and implements the Image interface.
 * This class provides the functionality to link a JavaFX Image with a BufferedImage
 * that shares the same underlying data buffer, enabling seamless interoperability
 * between these two image representations.
 */
public final class FxMutableImage extends MutableImage implements FxImage {

    private final javafx.scene.image.Image fxImage;

    static FxMutableImage loadImage(Payload payload) throws IOException {
        return loadImage(payload, FxMutableImage::new);
    }

    /**
     * Create a new FxBufferedImage instance.
     * @param width the image width
     * @param height the image height
     */
    public FxMutableImage(int width, int height) {
        super(width, height, new int[width * height]);

        IntBuffer buffer = IntBuffer.wrap(getArgb());
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(width, height, buffer, PixelFormat.getIntArgbPreInstance());
        this.fxImage = new WritableImage(pixelBuffer);
    }

    /**
     * Retrieves the JavaFX Image instance associated with this FxBufferedImage.
     *
     * @return the JavaFX Image instance that is backed by the same pixel data as this FxBufferedImage.
     */
    @Override
    public javafx.scene.image.Image fxImage() {
        return fxImage;
    }
}
