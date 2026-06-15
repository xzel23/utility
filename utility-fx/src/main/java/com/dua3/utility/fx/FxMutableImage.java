package com.dua3.utility.fx;

import com.dua3.utility.awt.AwtMutableImage;
import com.dua3.utility.data.MutableImage;
import com.dua3.utility.io.Payload;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.jspecify.annotations.Nullable;

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

    /**
     * Loads an image from the specified payload and constructs an FxMutableImage instance.
     *
     * @param payload the {@code Payload} object containing the image data to be loaded
     * @return an {@code FxMutableImage} instance created from the provided {@code Payload}
     * @throws IOException if an error occurs during reading or processing the image data
     */
    static FxMutableImage loadImage(Payload payload) throws IOException {
        return loadImage(payload, FxMutableImage::new);
    }

    /**
     * Constructs an FxMutableImage instance with the specified width, height, and optional pixel data.
     * This constructor initializes the underlying pixel data and creates a WritableImage
     * that shares the same pixel buffer, enabling interoperability with JavaFX.
     *
     * @param width the width of the image, in pixels
     * @param height the height of the image, in pixels
     * @param data an optional array of pixel data in ARGB format. If {@code null}, a new array of appropriate length is created.
     *             The expected length is {@code width * height}.
     * @throws IllegalArgumentException if the provided {@code data} array length does not match {@code width * height}.
     */
    FxMutableImage(int width, int height, int @Nullable [] data) {
        super(width, height, data != null ? data : new int[width * height]);

        IntBuffer buffer = IntBuffer.wrap(getArgb());
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(width, height, buffer, PixelFormat.getIntArgbPreInstance());
        this.fxImage = new WritableImage(pixelBuffer);
    }

    /**
     * Create a new FxBufferedImage instance.
     * @param width the image width
     * @param height the image height
     */
    FxMutableImage(int width, int height) {
        this(width, height, null);
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
