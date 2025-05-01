package com.dua3.utility.fx;

import com.dua3.utility.data.ImageBuffer;
import com.dua3.utility.data.MutableImage;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;

/**
 * The FxBufferedImage class represents an image that integrates JavaFX and AWT components.
 * It is a subclass of BufferedImage and implements the Image interface.
 * This class provides the functionality to link a JavaFX Image with a BufferedImage
 * that shares the same underlying data buffer, enabling seamless interoperability
 * between these two image representations.
 */
public final class FxBufferedImage extends BufferedImage implements FxImage, MutableImage {
    private final int width;
    private final int height;
    private final IntBuffer buffer;
    private final javafx.scene.image.Image fxImage;

    private record Data(
            int width,
            int height,
            IntBuffer buffer,
            javafx.scene.image.Image fxImage,
            ColorModel colorModel,
            WritableRaster raster
    ) {}

    private static Data buildArgs(int width, int height) {
        // create the data array
        IntBuffer buffer = IntBuffer.allocate(width * height);

        // create a JavaFX Image backed by the data array
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(width, height, buffer, PixelFormat.getIntArgbPreInstance());
        WritableImage fxImage = new WritableImage(pixelBuffer);

        // create a BufferedImage backed by the same data array
        DataBuffer dataBuffer = new DataBufferInt(buffer.array(), buffer.capacity());

        DirectColorModel colorModel = new DirectColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                32,         // Bits (ARGB: 8+8+8+8)
                0x00FF0000, // Red mask
                0x0000FF00, // Green mask
                0x000000FF, // Blue mask
                0xFF000000, // Alpha mask
                true,       // Non-premultiplied alpha
                DataBuffer.TYPE_INT);

        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);

        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        return new Data(width, height, buffer, fxImage, colorModel, raster);
    }

    /**
     * Create a new FxBufferedImage instance.
     * @param width the image width
     * @param height the image height
     */
    public FxBufferedImage(int width, int height) {
        this(buildArgs(width, height));
    }

    private FxBufferedImage(Data data) {
        super(data.colorModel, data.raster, true, null);
        this.width = data.width();
        this.height = data.height();
        this.buffer = data.buffer();
        this.fxImage = data.fxImage();
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int[] getArgb() {
        return buffer.array();
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

    /**
     * Creates and returns a new {@link ImageBuffer} instance using the pixel data, width,
     * and height of this {@code FxBufferedImage}. This buffer can be used to manipulate the
     * image data.
     *
     * @return a new {@link ImageBuffer} containing the image data, width, and height
     */
    @Override
    public ImageBuffer getBuffer() {
        return new ImageBuffer(getArgb(), width, height);
    }
}
